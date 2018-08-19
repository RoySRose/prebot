package prebot.strategy.analyse.terran;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.StrategyAnalyseManager;

public class RefineryAnalyser extends UnitAnalyser {

	public RefineryAnalyser() {
		super(UnitType.Terran_Refinery);
	}

	@Override
	public void analyse() {
		fastRefinery();
	}

	private void fastRefinery() {
		List<UnitInfo> found = found(CommonCode.RegionType.ENEMY_BASE);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int mechanicGasFrame = EnemyStrategy.TERRAN_MECHANIC.buildTimeMap.frame(UnitType.Terran_Refinery, 25);
			if (buildFrame < mechanicGasFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.REFINERY_FAST);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.REFINERY_LATE);
			}
		} else {
			int gasLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(StrategyAnalyseManager.LastCheckLocation.GAS);
			int mechanicGasFrame = EnemyStrategy.TERRAN_MECHANIC.buildTimeMap.frame(UnitType.Terran_Refinery, 25);
			if (gasLastCheckFrame > mechanicGasFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.NO_REFINERY);
			}
		}
	}
}
