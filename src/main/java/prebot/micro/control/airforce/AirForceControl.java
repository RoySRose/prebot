package prebot.micro.control.airforce;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.control.Control;
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
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);

		// 유닛공격, 전진, 후퇴에 대한 결정 (attack unit, attack position, flee)
		Decision decision = decisionMaker.makeDecisionForAirForceDrivePosition(airForceTeam.leaderUnit, euiList);
		this.applyAirForceDecision(decision, airForceTeam);
		if (decision.type == DecisionType.ATTACK_UNIT) {
			for (Unit wraith : wraithList) {
				wraith.rightClick(decision.eui.getUnit());
			}

		} else if (decision.type == DecisionType.ATTACK_POSITION) {
			// 이동시에 공격할지에 대한 결정
			Decision decisionMoving = decisionMaker.makeDecisionForAirForceMoving(airForceTeam, euiList);
			for (Unit wraith : wraithList) {
				if (decisionMoving.type == DecisionType.ATTACK_UNIT) {
					wraith.rightClick(decisionMoving.eui.getUnit());
				} else if (decisionMoving.type == DecisionType.UNITE) {
					wraith.rightClick(airForceTeam.leaderUnit.getPosition());
				} else if (decisionMoving.type == DecisionType.ATTACK_POSITION) {
					wraith.rightClick(airForceTeam.leaderOrderPosition);
				}
			}
			
		} else if (decision.type == DecisionType.FLEE_FROM_UNIT) {
			for (Unit wraith : wraithList) {
				wraith.rightClick(airForceTeam.leaderOrderPosition);
			}
		}
	}

	private void applyAirForceDecision(Decision decision, AirForceTeam airForceTeam) {
		if (decision.type == DecisionType.ATTACK_UNIT) {
			airForceTeam.leaderOrderPosition = decision.eui.getLastPosition();

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

		} else if (decision.type == DecisionType.FLEE_FROM_UNIT) {
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
