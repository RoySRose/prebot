package prebot.strategy.action.impl;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.main.MyBotModule;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.Action;

/**
 * 111 가스조절
 */
public class GasAdjustment8Barrack111 extends Action {

	@Override
	public boolean exitCondition() {
		if (TimeUtils.elapsedSeconds() > 300) {
			StrategyIdea.gasAdjustment = false;
			StrategyIdea.gasAdjustmentWorkerCount = 0;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void action() {
		if (TimeUtils.elapsedSeconds() > 180) {
			StrategyIdea.gasAdjustment = true;
			StrategyIdea.gasAdjustmentWorkerCount = 2;
		} else {
			StrategyIdea.gasAdjustment = true;
			int workerCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_SCV);
			if (workerCount < 8) {
				StrategyIdea.gasAdjustmentWorkerCount = 0;
			} else {
				if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Factory) > 0 || MyBotModule.Broodwar.self().gas() >= 100) {
					StrategyIdea.gasAdjustmentWorkerCount = 1;
				} else {
					StrategyIdea.gasAdjustmentWorkerCount = 2;
				}
			}
		}
		
	}
}
