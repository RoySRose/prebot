

import java.util.List;

import bwapi.UnitType;

public class AdunAnalyser extends UnitAnalyser {

	public AdunAnalyser() {
		super(UnitType.Protoss_Citadel_of_Adun);
	}

	@Override
	public void analyse() {
		fastAdun();
	}

	private void fastAdun() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_ADUN)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastAdunFrame = EnemyStrategy.PROTOSS_FAST_DARK.buildTimeMap.frame(UnitType.Protoss_Citadel_of_Adun, 30);

			if (buildFrame < fastAdunFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.ADUN_FAST);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.ADUN_FOUND);
			}
		}
	}

}
