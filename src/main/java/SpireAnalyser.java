

import java.util.List;

import bwapi.UnitType;

public class SpireAnalyser extends UnitAnalyser {

	public SpireAnalyser() {
		super(UnitType.Zerg_Spire);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.SPIRE)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int lairBuildFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			if (buildFrame < lairBuildFrame + UnitType.Zerg_Lair.buildTime() + 25 * TimeUtils.SECOND) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_SPIRE);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.SPIRE);
			}
		}
	}

}
