package prebot.micro.control;

import bwapi.Color;
import bwapi.Unit;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;

public class ScvDefense extends Control {

	@Override
	public void control() {
		for (Unit unit : unitList) {
			Prebot.Game.drawCircleMap(unit.getPosition().getX(), unit.getPosition().getY(), 4, Color.Yellow, true);
			Unit target = getClosestEnemyUnitFromWorker(unit);

			if (target != null) {
				CommandUtils.attackUnit(unit, target);
			}
		}
	}

	/// 해당 일꾼 유닛으로부터 가장 가까운 적군 유닛을 리턴합니다
	private Unit getClosestEnemyUnitFromWorker(Unit worker) {
		if (worker == null)
			return null;

		Unit closestUnit = null;
		double closestDist = 10000;

		for (Unit unit : Prebot.Game.enemy().getUnits()) {
			double dist = unit.getDistance(worker);

			if ((dist < 400) && (closestUnit == null || (dist < closestDist))) {
				closestUnit = unit;
				closestDist = dist;
			}
		}

		return closestUnit;
	}
}
