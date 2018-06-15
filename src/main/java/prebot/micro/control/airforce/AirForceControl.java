package prebot.micro.control.airforce;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceManager.AirForceUnit;

public class AirForceControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList, Position targetPosition) {

		for (Unit wraith : unitList) {
			AirForceUnit airForceUnit = AirForceManager.Instance().getAirForceUnit(wraith.getID());
			
			if (airForceUnit.leaderUnit.getID() == wraith.getID()) {
				Decision decision = decision(wraith, euiList);
				if (decision.type == DecisionType.FLEE_FROM_UNIT) {
					// TODO region 밖일 경우
					Position airDrivingPosition = MicroUtils.airDrivingPosition(wraith.getPosition(), StrategyIdea.campPosition, airForceUnit.driveAngle);
					if (airDrivingPosition == null) {
						airForceUnit.switchDriveAngle();
						airDrivingPosition = MicroUtils.airDrivingPosition(wraith.getPosition(), StrategyIdea.campPosition, airForceUnit.driveAngle);
					}
					if (airDrivingPosition == null) {
						airDrivingPosition = StrategyIdea.campPosition;
					}
					CommandUtils.move(wraith, airDrivingPosition);
					AirForceManager.Instance().retreat(wraith.getID());
					airForceUnit.leaderOrderPosition = airDrivingPosition;
					
				} else {
					Position airDrivingPosition = MicroUtils.airDrivingPosition(wraith.getPosition(), airForceUnit.getTargetPosition(), airForceUnit.driveAngle);
					if (airDrivingPosition == null) {
						airForceUnit.switchDriveAngle();
						airDrivingPosition = MicroUtils.airDrivingPosition(wraith.getPosition(), airForceUnit.getTargetPosition(), airForceUnit.driveAngle);
					}
					if (airDrivingPosition == null) {
						airDrivingPosition = airForceUnit.getTargetPosition();
					}
					
					if (airDrivingPosition.getDistance(airForceUnit.getTargetPosition()) < 10) {
						AirForceManager.Instance().changeTargetIndex(airForceUnit);
					}
					CommandUtils.move(wraith, airDrivingPosition);
					airForceUnit.leaderOrderPosition = airDrivingPosition;
				}
			} else {
				CommandUtils.move(wraith, airForceUnit.leaderOrderPosition);
			}
		}
	}

	private Decision decision(Unit wraith, List<UnitInfo> euiList) {
		return Decision.attackPosition();
	}
}
