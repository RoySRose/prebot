package prebot.strategy.analyse.terran;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.RaceActionManager;
import prebot.strategy.manage.RaceActionManager.LastCheckLocation;

public class RefineryAnalyser extends UnitAnalyser {

	public RefineryAnalyser() {
		super(UnitType.Terran_Refinery);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found(RegionType.ENEMY_BASE);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int mechanicGasFrame = EnemyStrategy.TERRAN_MECHANIC.defaultTimeMap.time(UnitType.Terran_Refinery, 25);
			if (buildFrame < mechanicGasFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.REFINERY_FAST);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.REFINERY_LATE);
			}
		} else {
			int gasLastCheckFrame = RaceActionManager.Instance().lastCheckFrame(LastCheckLocation.GAS);
			int mechanicGasFrame = EnemyStrategy.TERRAN_MECHANIC.defaultTimeMap.time(UnitType.Terran_Refinery, 25);
			if (gasLastCheckFrame > mechanicGasFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.NO_REFINERY);
			}
		}
	}
}
