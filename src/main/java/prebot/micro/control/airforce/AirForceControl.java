package prebot.micro.control.airforce;

import java.util.Arrays;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.common.main.Prebot;
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

		// 결정: 도망(유닛), 공격(유닛), 카이팅(유닛), 클로킹, 이동
		Decision decision = null;
		if (!airForceTeam.retreating()) {
			decision = decisionMaker.makeDecisionForAirForce(airForceTeam, euiList, AirForceManager.Instance().getStrikeLevel());
		} else {
			decision = Decision.fleeFromUnit(airForceTeam.leaderUnit, null);
		}

		// 결정상세(공격, 카이팅, 이동): 공격, 뭉치기, 카이팅
		Decision decisionDetail = null;
		if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
			decisionDetail = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, Arrays.asList(decision.eui), false);
		} else if (decision.type == DecisionType.ATTACK_POSITION) { // 목적지 이동시 준수할 세부사항
			decisionDetail = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, euiList, true);
		}

//		System.out.println("decision: " + decision + " / " + decisionDetail);
		this.applyAirForceDecision(airForceTeam, decision, decisionDetail);

		if (decisionDetail != null) {
			// 공격 쿨타임
			if (decisionDetail.type == DecisionType.ATTACK_UNIT) {
				// ATTACK_UNIT, KITING_UNIT 동일
				if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
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
				// 지나가면서 한대씩 때리기
				else if (decision.type == DecisionType.ATTACK_POSITION) {
					for (Unit wraith : wraithList) {
						wraith.rightClick(decisionDetail.eui.getUnit());
					}
				}

			}
			// 뭉치기
			else if (decisionDetail.type == DecisionType.UNITE) {
//				Position centerOfWraith = UnitUtils.centerPositionOfUnit(wraithList);
				for (Unit wraith : wraithList) {
//					wraith.rightClick(centerOfWraith);
					wraith.rightClick(airForceTeam.leaderUnit.getPosition());
				}
				
			}
			// 전진 카이팅, 카이팅
			else if (decisionDetail.type == DecisionType.ATTACK_POSITION || decisionDetail.type == DecisionType.KITING_UNIT) {
				for (Unit wraith : wraithList) {
					wraith.rightClick(airForceTeam.leaderOrderPosition);
				}
			}

		} else if (decision.type == DecisionType.FLEE_FROM_UNIT) { // 도망
			for (Unit wraith : wraithList) {
				wraith.rightClick(airForceTeam.leaderOrderPosition);
			}
		} else if (decision.type == DecisionType.CHANGE_MODE) { // 클로킹
			airForceTeam.cloak();
			for (Unit wraith : wraithList) {
				wraith.cloak();
			}
		}
	}

	private void applyAirForceDecision(AirForceTeam airForceTeam, Decision decision, Decision decisionDetail) {
		Position airDrivingPosition = null;
		if (decision.type == DecisionType.ATTACK_UNIT) {
			if (decisionDetail.type == DecisionType.KITING_UNIT &&
					(decision.eui.getType().airWeapon() != WeaponType.None && decision.eui.getType().airWeapon().maxRange() < UnitType.Terran_Wraith.groundWeapon().maxRange())) {
				airDrivingPosition = airDrivingPosition(airForceTeam, AirForceManager.Instance().getRetreatPosition(), airForceTeam.driveAngle); // kiting
			} else {
				airDrivingPosition = airForceTeam.leaderUnit.getPosition();
			}

		} else if (decision.type == DecisionType.KITING_UNIT) {
			if (decisionDetail.type == DecisionType.KITING_UNIT &&
					(decision.eui.getType().airWeapon() != WeaponType.None && decision.eui.getType().airWeapon().maxRange() < UnitType.Terran_Wraith.groundWeapon().maxRange())) {
				airDrivingPosition = airDrivingPosition(airForceTeam, AirForceManager.Instance().getRetreatPosition(), airForceTeam.driveAngle); // kiting
			} else {
				airDrivingPosition = decision.eui.getLastPosition();
			}
			
		} else if (decision.type == DecisionType.ATTACK_POSITION) {
			airDrivingPosition = airDrivingPosition(airForceTeam, airForceTeam.getTargetPosition(), airForceTeam.driveAngle);
			if (airForceTeam.leaderUnit.getPosition().getDistance(airForceTeam.getTargetPosition()) < 100) {
				airForceTeam.changeTargetIndex();
			}

		} else if (decision.type == DecisionType.FLEE_FROM_UNIT) {
			airForceTeam.retreat(decision.eui);
			airDrivingPosition = airFeePosition(airForceTeam, airForceTeam.driveAngle);
		}
		airForceTeam.leaderOrderPosition = airDrivingPosition;
	}

	private Position airDrivingPosition(AirForceTeam airForceTeam, Position goalPosition, int[] angle) {
		Position leaderPosition = airForceTeam.leaderUnit.getPosition();
		Position airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, goalPosition, angle, true);
		if (airDrivingPosition == null) {
			airForceTeam.switchDriveAngle();
			airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, goalPosition, angle, true);
		}
		if (airDrivingPosition == null) {
			airDrivingPosition = goalPosition;
		}
		return airDrivingPosition;
	}

	private Position airFeePosition(AirForceTeam airForceTeam, int[] angle) {
		Position leaderPosition = airForceTeam.leaderUnit.getPosition();
		
		Position enemyPosition;
		Unit enemyUnit = Prebot.Broodwar.getUnit(airForceTeam.fleeEui.getUnitID());
		if (enemyUnit != null) {
			enemyPosition = enemyUnit.getPosition();
		} else {
			enemyPosition = airForceTeam.fleeEui.getLastPosition();
		}
		Position airDrivingPosition = MicroUtils.airFleePosition(leaderPosition, enemyPosition);
		if (airDrivingPosition == null) {
			airDrivingPosition = AirForceManager.Instance().getRetreatPosition();
		}
		airDrivingPosition = airDrivingPosition(airForceTeam, airDrivingPosition, angle);
		return airDrivingPosition;
	}
}
