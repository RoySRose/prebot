

import java.util.List;

import bwapi.UnitType;

public class StarportAnalyser extends UnitAnalyser {

	public StarportAnalyser() {
		super(UnitType.Terran_Starport);
	}

	@Override
	public void analyse() {
		fastStarport();
	}

	private void fastStarport() {
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}
		
		if (found.size() >= 2) { // 스타포트 2개 이상
			int firstBuildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int secondBuildFrame = buildStartFrameDefaultJustBefore(found.get(1));
			int twoFacSecondFacFrame = EnemyStrategy.TERRAN_2STAR.buildTimeMap.frameOfIndex(UnitType.Terran_Starport, 1, 20);
			
			if (firstBuildFrame < twoFacSecondFacFrame && secondBuildFrame < twoFacSecondFacFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARPORT_FAST_TWO);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARPORT_TWO);
			}
		
		} else if (found.size() == 1) { // 스타포트 1개
			int firstBuildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int mechanicFirstBarrackFrame = EnemyStrategy.TERRAN_2STAR.buildTimeMap.frame(UnitType.Terran_Starport, 15);
			if (firstBuildFrame < mechanicFirstBarrackFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARPORT_FAST_ONE);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARPORT_ONE);
			}
		}
	}
}
