package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.EnemyBuildTimer;

public class SpireAnalyser extends UnitAnalyser {

	public SpireAnalyser() {
		super(UnitType.Zerg_Spire);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(ClueType.SPIRE)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int lairBuildFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			if (buildFrame < lairBuildFrame + UnitType.Zerg_Lair.buildTime() + 25 * TimeUtils.SECOND) {
				ClueManager.Instance().addClueInfo(ClueInfo.FAST_SPIRE);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.SPIRE);
			}
		}
	}

}
