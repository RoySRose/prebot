package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.EnemyBuildTimer;
import prebot.strategy.manage.StrategyAnalyseManager;
import prebot.strategy.manage.StrategyAnalyseManager.LastCheckLocation;

public class LairAnalyser extends UnitAnalyser {

	public LairAnalyser() {
		super(UnitType.Zerg_Lair);
	}

	@Override
	public void analyse() {
		fastLair();
		lairStatus();
	}

	private void fastLair() {
		if (ClueManager.Instance().containsClueType(ClueType.FAST_LAIR)) {
			return;
		}

		int oneHatFrame = EnemyStrategy.ZERG_OVERPOOL_GAS.defaultTimeMap.time(UnitType.Zerg_Lair, 30);
		int twoHatFrame = EnemyStrategy.ZERG_2HAT_GAS.defaultTimeMap.time(UnitType.Zerg_Lair, 30);
		int threeHatFrame = EnemyStrategy.ZERG_3HAT.defaultTimeMap.time(UnitType.Zerg_Lair, 30);
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));

			if (buildFrame < oneHatFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.LAIR_1HAT_FAST);
			} else if (buildFrame < twoHatFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.LAIR_2HAT_FAST);
			} else if (buildFrame < threeHatFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.LAIR_3HAT_FAST);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.LAIR_3HAT_FAST);
			}
		}
	}

	private void lairStatus() {
		if (ClueManager.Instance().containsClueInfo(ClueInfo.LAIR_COMPLETE)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			if (found.get(0).isCompleted()) {
				ClueManager.Instance().addClueInfo(ClueInfo.LAIR_COMPLETE);
			} else {
				int updateFrame = found.get(0).getUpdateFrame();
				if (TimeUtils.after(updateFrame + UnitType.Zerg_Lair.buildTime())) {
					ClueManager.Instance().addClueInfo(ClueInfo.LAIR_COMPLETE);
				} else {
					ClueManager.Instance().addClueInfo(ClueInfo.LAIR_INCOMPLETE);
				}				
			}
			
		} else {
			int lairExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
			int baseLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.BASE);
			if (baseLastCheckFrame > lairExpect + 5 * TimeUtils.SECOND) {
				ClueManager.Instance().addClueInfo(ClueInfo.NO_LAIR);
			}
		}
	}

}
