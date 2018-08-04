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

	private boolean gasAjustmentFinshed = false;

	@Override
	public boolean exitCondition() {
		if (gasAjustmentFinshed || TimeUtils.afterTime(5, 0)) {
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

		int adjustGasWorkerCount = 0;
		int workerCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_SCV);
		if (workerCount > 8) {
			adjustGasWorkerCount = 3;
			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Factory)) {
				if (Prebot.Broodwar.self().gas() >= 92) {
					adjustGasWorkerCount = 1;
				}
			} else {
				if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.ONE_FACTORY) {
					if (UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Command_Center) < 2) {
						adjustGasWorkerCount = 2;
					} else {
						gasAjustmentFinshed = true;
					}
				} else {
					gasAjustmentFinshed = true;
				}
			}
		}
		StrategyIdea.gasAdjustmentWorkerCount = adjustGasWorkerCount;
	}
}
