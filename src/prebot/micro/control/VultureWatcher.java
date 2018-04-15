package prebot.micro.control;

import bwapi.Unit;
import prebot.brain.WatcherCombatPredictor;
import prebot.common.code.Code.WatcherCombatPredictResult;
import prebot.common.util.CommandUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.KitingOption;

public class VultureWatcher extends Control {

	@Override
	public void control() {
		WatcherCombatPredictResult result = WatcherCombatPredictor.predictByUnitInfo(sqaudUnitList, enemyUnitInfoList);
		
		if (result == WatcherCombatPredictResult.ATTACK || result == WatcherCombatPredictResult.ATTACK_NO_MERCY) {
			for (Unit unit : sqaudUnitList) {
				if (enemyUnitInfoList.isEmpty()) {
					CommandUtils.attackMove(unit, idea.attackPosition);
				} else {
					KitingOption kOption = KitingOption.defaultOption();
					kOption.fOption.goalPosition = idea.campPosition;
					CommandUtils.kiting(unit, enemyUnitInfoList.get(0).unit, kOption);
				}
			}
		} else {
			Unit leader = UnitUtils.getClosestUnitToPosition(sqaudUnitList, idea.attackPosition);
			for (Unit unit : sqaudUnitList) {
				if (unit.getID() == leader.getID() || unit.getDistance(leader) <= 300) {
					CommandUtils.move(unit, idea.campPosition);
				} else {
					CommandUtils.move(unit, leader.getPosition());
				}
			}
		}
	}
}
