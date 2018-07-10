package prebot.micro.control.airforce;

import java.util.Arrays;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.control.Control;
import prebot.micro.targeting.WraithTargetCalculator;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceTeam;

public class AirForceControl extends Control {

	@Override
	public void control(List<Unit> wraithList, List<UnitInfo> euiList) {
		if (wraithList.isEmpty()) {
			return;
		}

		// 팀 단위로 wraithList가 세팅되어야 한다.
		AirForceTeam airForceTeam = AirForceManager.Instance().airForTeamOfWraith(wraithList.get(0).getID());
		DecisionMaker decisionMaker = new DecisionMaker(new WraithTargetCalculator());

		// 결정: 유닛공격(방어기지 안 또는 바깥쪽의 적), 전진, 후퇴
		// * 유닛공격 - 제압가능한 공격유닛 공격 OR SCV, 탱크 등 학살
		Decision decision = null;
		if (!airForceTeam.retreating()) {
			decision = decisionMaker.makeDecisionForAirForce(airForceTeam, euiList);
		} else {
			decision = Decision.fleeFromPosition();
		}

		// 결정상세(유닛공격, 전진): 쿨타임 및 적유닛타입에 따른 공격, 뭉치기, 카이팅방향 결정
		Decision decisionDetail = null;
		if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
			decisionDetail = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, Arrays.asList(decision.eui), false);
		} else if (decision.type == DecisionType.ATTACK_POSITION) { // 목적지 이동시 준수할 세부사항
			decisionDetail = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, euiList, true);
		}

		this.applyAirForceDecision(airForceTeam, decision, decisionDetail);

		if (decisionDetail != null) {
			if (decisionDetail.type == DecisionType.ATTACK_UNIT) {
				if (decision.type == DecisionType.ATTACK_POSITION) {
					for (Unit wraith : wraithList) {
						wraith.rightClick(decisionDetail.eui.getUnit());
					}
				} else if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
					boolean unitInRange = false;
					Unit unitInSight = UnitUtils.unitInSight(decision.eui);
					if (unitInSight != null && airForceTeam.leaderUnit.isInWeaponRange(unitInSight)) {
						unitInRange = true;
					}
					for (Unit wraith : wraithList) {
						if (unitInRange) {
							// wraith.rightClick(decisionMoving.eui.getUnit());
							CommandUtils.attackUnit(wraith, decisionDetail.eui.getUnit());

						} else {
							wraith.rightClick(airForceTeam.leaderOrderPosition);
						}
					}
				}

			} else if (decisionDetail.type == DecisionType.UNITE) {
//				Position centerOfWraith = UnitUtils.centerPositionOfUnit(wraithList);
				for (Unit wraith : wraithList) {
//					wraith.rightClick(centerOfWraith);
					wraith.rightClick(airForceTeam.leaderUnit.getPosition());
				}
			} else if (decisionDetail.type == DecisionType.ATTACK_POSITION || decisionDetail.type == DecisionType.FLEE_FROM_POSITION) {
				for (Unit wraith : wraithList) {
					wraith.rightClick(airForceTeam.leaderOrderPosition);
				}
			}

		} else if (decision.type == DecisionType.FLEE_FROM_POSITION) { // 도망
			for (Unit wraith : wraithList) {
				wraith.rightClick(airForceTeam.leaderOrderPosition);
			}
		}
	}

	private void applyAirForceDecision(AirForceTeam airForceTeam, Decision decision, Decision decisionDetail) {
		Position airDrivingPosition = null;
		if (decision.type == DecisionType.ATTACK_UNIT) {
			if (decisionDetail.type == DecisionType.FLEE_FROM_POSITION &&
					(decision.eui.getType().airWeapon() != WeaponType.None && decision.eui.getType().airWeapon().maxRange() < UnitType.Terran_Wraith.groundWeapon().maxRange())) { // kiting
				airDrivingPosition = airDrivingPosition(airForceTeam, AirForceManager.Instance().getRetreatPosition(), true); // 가까운 base로 변경
			} else {
				airDrivingPosition = airForceTeam.leaderUnit.getPosition();
			}

		} else if (decision.type == DecisionType.KITING_UNIT) {
			if (decisionDetail.type == DecisionType.FLEE_FROM_POSITION &&
					(decision.eui.getType().airWeapon() != WeaponType.None && decision.eui.getType().airWeapon().maxRange() < UnitType.Terran_Wraith.groundWeapon().maxRange())) { // kiting
				airDrivingPosition = airDrivingPosition(airForceTeam, AirForceManager.Instance().getRetreatPosition(), true); // 가까운 base로 변경
			} else {
				airDrivingPosition = decision.eui.getLastPosition();
			}
			
		} else if (decision.type == DecisionType.ATTACK_POSITION) {
			airDrivingPosition = airDrivingPosition(airForceTeam, airForceTeam.getTargetPosition(), true);
			if (airForceTeam.leaderUnit.getPosition().getDistance(airForceTeam.getTargetPosition()) < 100) {
				airForceTeam.changeTargetIndex();
			}

		} else if (decision.type == DecisionType.FLEE_FROM_POSITION) {
			airDrivingPosition = airDrivingPosition(airForceTeam, AirForceManager.Instance().getRetreatPosition(), false); // 가까운 base로 변경
			airForceTeam.retreat();
		}
		airForceTeam.leaderOrderPosition = airDrivingPosition;
	}

	private Position airDrivingPosition(AirForceTeam airForceTeam, Position goalPosition, boolean avoidEnemyUnit) {
		Position leaderPosition = airForceTeam.leaderUnit.getPosition();
		Position airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, goalPosition, airForceTeam.driveAngle, avoidEnemyUnit);
		if (airDrivingPosition == null) {
			airForceTeam.switchDriveAngle();
			airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, goalPosition, airForceTeam.driveAngle, avoidEnemyUnit);
		}
		if (airDrivingPosition == null) {
			airDrivingPosition = goalPosition;
		}
		return airDrivingPosition;
	}
}
