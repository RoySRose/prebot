package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class GundamControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		for (Unit unit : unitList) {
			Unit ataackWorkerScv = null;
			if (skipControl(unit)) {
				continue;
			}
			for(UnitInfo enemyWorker : euiList){
				if(enemyWorker.getType().isWorker()){
					ataackWorkerScv = UnitUtils.getClosestCombatWorkerToPosition(unitList, enemyWorker.getUnit().getPosition());
					CommandUtils.attackUnit(ataackWorkerScv, enemyWorker.getUnit());
				}
			}
			if(unit == ataackWorkerScv){
				continue;
			}
			Unit target = getClosestEnemyUnitFromWorker(unit);
			
			if (target != null) {
				CommandUtils.attackUnit(unit, target);
			} else {
				CommandUtils.attackMove(unit, StrategyIdea.campPosition);
			}
		}
	}

	/// 해당 일꾼 유닛으로부터 가장 가까운 적군 유닛을 리턴합니다
	private Unit getClosestEnemyUnitFromWorker(Unit worker) {
		if (worker == null)
			return null;

		Unit closestUnit = null;
		double closestDist = 10000;

		for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
			double dist = unit.getDistance(worker);

			if ((dist < 400) && (closestUnit == null || (dist < closestDist))) {
				closestUnit = unit;
				closestDist = dist;
			}
		}

		return closestUnit;
	}
}
