package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class SpireAnalyser extends UnitAnalyser {

	public SpireAnalyser() {
		super(UnitType.Zerg_Spire);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(ClueType.SPIRE)) {
			return;
		}
		
		int oneHatSpire = EnemyStrategy.ZERG_1HAT_FAST_MUTAL.defaultTimeMap.time(UnitType.Zerg_Spire, 15);
		int twoHatSpire = EnemyStrategy.ZERG_2HAT_SPIRE.defaultTimeMap.time(UnitType.Zerg_Spire, 20);
		int threeHatSpire = EnemyStrategy.ZERG_3HAT_SPIRE.defaultTimeMap.time(UnitType.Zerg_Spire, 30);
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			if (buildFrame < oneHatSpire) {
				ClueManager.Instance().addClueInfo(ClueInfo.SPIRE_1HAT);
			} else if (buildFrame < twoHatSpire) {
				ClueManager.Instance().addClueInfo(ClueInfo.SPIRE_2HAT);
			} else if (buildFrame < threeHatSpire) {
				ClueManager.Instance().addClueInfo(ClueInfo.SPIRE_3HAT);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.SPIRE);
			}
			ClueManager.Instance().updateExpectBuildFrame(UnitType.Zerg_Spire, buildFrame);
		}
	}

}
