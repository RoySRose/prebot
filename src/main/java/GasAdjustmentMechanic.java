

import bwapi.UnitType;

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
				if (MyBotModule.Broodwar.self().gas() >= 92) {
					adjustGasWorkerCount = 1;
				}
			} else {
				if (StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.ONE_FACTORY) {
					if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Command_Center) < 2) {
						if (MyBotModule.Broodwar.self().gas() <= 250) {
							adjustGasWorkerCount = 2;
						} else {
							adjustGasWorkerCount = 1;
						}
					} else {
						gasAjustmentFinshed = true;
					}
				} else {
					if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Command_Center) < 2) {
						if (MyBotModule.Broodwar.self().gas() <= 100) {
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
