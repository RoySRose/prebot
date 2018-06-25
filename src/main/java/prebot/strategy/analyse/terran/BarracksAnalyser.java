package prebot.strategy.analyse.terran;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.RaceActionManager;
import prebot.strategy.manage.RaceActionManager.LastCheckLocation;

public class BarracksAnalyser extends UnitAnalyser {

	public BarracksAnalyser() {
		super(UnitType.Terran_Barracks);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			if (found.size() >= 2) { // 게이트 웨이 2개 이상
				int firstBuildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				int secondBuildFrame = buildStartFrameDefaultJustBefore(found.get(1));
				int twoBarrackSecondBarrackFrame = EnemyStrategy.TERRAN_2BARRACKS.defaultTimeMap.timeOfIndex(UnitType.Terran_Barracks, 1, 20);
				
				if (firstBuildFrame < twoBarrackSecondBarrackFrame && secondBuildFrame < twoBarrackSecondBarrackFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.BARRACK_FAST_TWO);
				} else {
					ClueManager.Instance().addClueInfo(ClueInfo.BARRACK_TWO);
				}
			
			} else if (found.size() == 1) { // 게이트 웨이 1개
				int firstBuildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				int mechanicFirstBarrackFrame = EnemyStrategy.TERRAN_MECHANIC.defaultTimeMap.time(UnitType.Terran_Barracks, 15);
				if (firstBuildFrame < mechanicFirstBarrackFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.BARRACK_FAST_ONE);
				} else {
					ClueManager.Instance().addClueInfo(ClueInfo.BARRACK_ONE);
				}
			}
		} else {
			int mechanicFirstBarrackFrame = EnemyStrategy.TERRAN_MECHANIC.defaultTimeMap.time(UnitType.Terran_Barracks, 15);
			int baseLastCheckFrame = RaceActionManager.Instance().lastCheckFrame(LastCheckLocation.BASE);
			if (baseLastCheckFrame > mechanicFirstBarrackFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.BARRACK_NOT_FOUND);
			}
		}
	}
}
