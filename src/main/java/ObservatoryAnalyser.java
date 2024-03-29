

import java.util.List;

import bwapi.UnitType;

public class ObservatoryAnalyser extends UnitAnalyser {

	public ObservatoryAnalyser() {
		super(UnitType.Protoss_Observatory);
	}

	@Override
	public void analyse() {
		fastObserver();
	}

	private void fastObserver() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_OB)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastObFrame = EnemyStrategy.PROTOSS_ROBOTICS_OB_DRAGOON.buildTimeMap.frame(UnitType.Protoss_Observatory, 30);

			if (buildFrame < fastObFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.OBSERVERTORY_FAST);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.OBSERVERTORY_FOUND);
			}
		}
	}

}
