package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class HydraDenAnalyser extends UnitAnalyser {

	public HydraDenAnalyser() {
		super(UnitType.Zerg_Hydralisk_Den);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(ClueType.HYDRADEN)) {
			return;
		}
		
		int oneHatchFrame = EnemyStrategy.ZERG_1HAT_FAST_LURKER.defaultTimeMap.time(UnitType.Zerg_Hydralisk_Den, 15);
		int noLairHydraFrame = EnemyStrategy.ZERG_NO_LAIR_HYDRA.defaultTimeMap.time(UnitType.Zerg_Hydralisk_Den, 15);
		int lairHydraFrame = EnemyStrategy.ZERG_LAIR_HYDRA_TECH.defaultTimeMap.time(UnitType.Zerg_Hydralisk_Den, 15);
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			if (buildFrame < oneHatchFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.HYDRADEN_FASTEST);
			} else if (buildFrame < noLairHydraFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.HYDRADEN_NO_LAIR);
			} else if (buildFrame < lairHydraFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.HYDRADEN_TWO_HATCH);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.HYDRADEN);
			}
			ClueManager.Instance().updateExpectBuildFrame(UnitType.Zerg_Hydralisk_Den, buildFrame);
		}
	}

}
