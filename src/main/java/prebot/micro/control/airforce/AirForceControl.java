package prebot.micro.control.airforce;

import java.util.Arrays;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.control.Control;
import prebot.micro.targeting.WraithTargetCalculator;
import prebot.strategy.StrategyIdea;
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

		// 유닛공격, 전진, 후퇴에 대한 결정 (attack unit, attack position, flee)
		Decision decision;
		if (!airForceTeam.retreating()) {
			decision = decisionMaker.makeDecisionForAirForce(airForceTeam, euiList);
		} else {
			decision = Decision.fleeFromPosition();
		}
		
		this.applyAirForceDecision(decision, airForceTeam);
		if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) { // 1) 제압가능한 공격유닛 공격 OR 2) SCV, 탱크 등 학살 
			// 디테일 행동
			Decision decisionMoving = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, Arrays.asList(decision.eui), false);
			if (decisionMoving.type == DecisionType.ATTACK_UNIT) {
				boolean unitInRange = false;
				Unit unitInSight = UnitUtils.unitInSight(decision.eui);
				if (unitInSight != null && airForceTeam.leaderUnit.isInWeaponRange(unitInSight)) {
					unitInRange = true;
				}
				for (Unit wraith : wraithList) {
					if (unitInRange) {
//						wraith.rightClick(decisionMoving.eui.getUnit());
						CommandUtils.attackUnit(wraith, decisionMoving.eui.getUnit());
						
					} else {
						wraith.rightClick(airForceTeam.leaderOrderPosition);
					}
				}
				
			} else if (decisionMoving.type == DecisionType.UNITE) {
				for (Unit wraith : wraithList) {
					wraith.rightClick(airForceTeam.leaderUnit.getPosition());
				}
			} else if (decisionMoving.type == DecisionType.ATTACK_POSITION) {
				for (Unit wraith : wraithList) {
					wraith.rightClick(airForceTeam.leaderOrderPosition);
				}
			}

		} else if (decision.type == DecisionType.ATTACK_POSITION) { // 목적지로 이동
			Decision decisionMoving = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, euiList, true); // 목적지 이동시 준수할 세부사항
			for (Unit wraith : wraithList) {
				if (decisionMoving.type == DecisionType.ATTACK_UNIT) {
					wraith.rightClick(decisionMoving.eui.getUnit());
				} else if (decisionMoving.type == DecisionType.UNITE) {
					wraith.rightClick(airForceTeam.leaderUnit.getPosition());
				} else if (decisionMoving.type == DecisionType.ATTACK_POSITION) {
					wraith.rightClick(airForceTeam.leaderOrderPosition);
				}
			}
			
		} else if (decision.type == DecisionType.FLEE_FROM_POSITION) { // 도망
			for (Unit wraith : wraithList) {
				wraith.rightClick(airForceTeam.leaderOrderPosition);
			}
		}
	}

	private void applyAirForceDecision(Decision decision, AirForceTeam airForceTeam) {
		if (decision.type == DecisionType.ATTACK_UNIT) {
			airForceTeam.leaderOrderPosition = airForceTeam.leaderUnit.getPosition();
			
		} else if (decision.type == DecisionType.KITING_UNIT) {
			Position leaderPosition = airForceTeam.leaderUnit.getPosition();
			Position airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, decision.eui.getLastPosition(), airForceTeam.driveAngle);
			if (airDrivingPosition == null) {
				airForceTeam.switchDriveAngle();
				airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, decision.eui.getLastPosition(), airForceTeam.driveAngle);
			}
			if (airDrivingPosition == null) {
				airDrivingPosition = decision.eui.getLastPosition();
			}
			airForceTeam.leaderOrderPosition = airDrivingPosition;

		} else if (decision.type == DecisionType.ATTACK_POSITION) {
			Position leaderPosition = airForceTeam.leaderUnit.getPosition();
			Position airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, airForceTeam.getTargetPosition(), airForceTeam.driveAngle);
			if (airDrivingPosition == null) {
				airForceTeam.switchDriveAngle();
				airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, airForceTeam.getTargetPosition(), airForceTeam.driveAngle);
			}
			if (airDrivingPosition == null) {
				airDrivingPosition = airForceTeam.getTargetPosition();
			}

			if (leaderPosition.getDistance(airForceTeam.getTargetPosition()) < 100) {
				airForceTeam.changeTargetIndex();
			}
			airForceTeam.leaderOrderPosition = airDrivingPosition;

		} else if (decision.type == DecisionType.FLEE_FROM_POSITION) {
			// TODO region 밖일 경우
			Position leaderPosition = airForceTeam.leaderUnit.getPosition();
			Position airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, StrategyIdea.campPosition, airForceTeam.driveAngle);
			if (airDrivingPosition == null) {
				airForceTeam.switchDriveAngle();
				airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, StrategyIdea.campPosition, airForceTeam.driveAngle);
			}
			if (airDrivingPosition == null) {
				airDrivingPosition = StrategyIdea.campPosition;
			}
			airForceTeam.retreat();
			airForceTeam.leaderOrderPosition = airDrivingPosition;
		}
	}
}
