package prebot.strategy.action.impl;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
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
		int workerCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_SCV);
		if (workerCount > 8) {
			adjustGasWorkerCount = 3;
			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Factory)) {
				if (Prebot.Broodwar.self().gas() >= 92) {
					adjustGasWorkerCount = 1;
				}
			} else {
				if (StrategyIdea.expansionOption == ExpansionOption.ONE_FACTORY) {
					if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Command_Center) < 2) {
						if (Prebot.Broodwar.self().gas() <= 250) {
							adjustGasWorkerCount = 2;
						} else {
							adjustGasWorkerCount = 1;
						}
					} else {
						gasAjustmentFinshed = true;
					}
				} else {
					if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Command_Center) < 2) {
						if (Prebot.Broodwar.self().gas() <= 100) {
							adjustGasWorkerCount = 3;
						} else {
							adjustGasWorkerCount = 2;
						}
					} else {
						gasAjustmentFinshed = true;
					}
				}
			}
		}
		StrategyIdea.gasAdjustmentWorkerCount = adjustGasWorkerCount;
	}
}
