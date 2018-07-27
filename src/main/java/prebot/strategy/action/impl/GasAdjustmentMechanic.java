package prebot.strategy.action.impl;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.Action;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/**
 * 메카닉 테란 가스 조절
 */
public class GasAdjustmentMechanic extends Action {

	@Override
	public boolean exitCondition() {
		if (TimeUtils.elapsedSeconds() > 210 || UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Command_Center) >= 2) {
			StrategyIdea.gasAdjustment = false;
			StrategyIdea.gasAdjustmentWorkerCount = 0;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void action() {
		StrategyIdea.gasAdjustment = true;
		int workerCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_SCV);
		if (workerCount < 8) {
			StrategyIdea.gasAdjustmentWorkerCount = 0;
		} else {
			if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.ONE_FACTORY) {
				if (UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Factory) > 0 || Prebot.Broodwar.self().gas() >= 100) {
					StrategyIdea.gasAdjustmentWorkerCount = 1;
				} else {
					StrategyIdea.gasAdjustmentWorkerCount = 3;
				}
			} else {
				if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Factory) && (Prebot.Broodwar.self().gas() >= 100 || Prebot.Broodwar.self().gas() > Prebot.Broodwar.self().minerals())) {
					StrategyIdea.gasAdjustmentWorkerCount = 1;
				}else {
					StrategyIdea.gasAdjustmentWorkerCount = 3;
				}
//				StrategyIdea.gasAdjustment = false;
//				StrategyIdea.gasAdjustmentWorkerCount = 0;
			}
		}
	}
}
