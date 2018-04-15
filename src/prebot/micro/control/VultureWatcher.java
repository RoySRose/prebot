package prebot.micro.control;

import bwapi.Unit;
import prebot.common.util.UnitUtils;

public class VultureWatcher extends Control {

	@Override
	public void control() {

		if (enemyUnitInfoList.size() == 0) {
			for (Unit unit : sqaudUnitList) {
				unit.attack(idea.attackPosition);
			}
		} else {
			Unit leader = UnitUtils.getClosestUnitToPosition(sqaudUnitList, idea.attackPosition);

			for (Unit unit : sqaudUnitList) {
				if (unit.getDistance(leader) > 300) {
					unit.move(idea.campPosition);
				}

				unit.move(idea.campPosition);
			}
		}

	}
}
