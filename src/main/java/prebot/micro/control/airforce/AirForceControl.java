package prebot.micro.control.airforce;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.MicroUtils;
import prebot.micro.control.Control;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;

public class AirForceControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList, Position targetPosition) {

		for (Unit wraith : unitList) {
			MicroUtils.airDriving(wraith, targetPosition);

			if (wraith.getDistance(targetPosition) < 100) {
				AirForceManager.Instance().changeTargetIndex();
				break;
			}
		}
	}
}
