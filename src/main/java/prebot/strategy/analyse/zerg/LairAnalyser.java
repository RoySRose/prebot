package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.RaceActionManager;
import prebot.strategy.manage.RaceActionManager.LastCheckLocation;

public class LairAnalyser extends UnitAnalyser {

	public LairAnalyser() {
		super(UnitType.Zerg_Lair);
	}

	@Override
	public void analyse() {
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
			int twoHatchLairBuildFrame = EnemyStrategy.ZERG_2HAT_GAS.defaultTimeMap.time(UnitType.Zerg_Lair, 15);
			int baseLastCheckFrame = RaceActionManager.Instance().lastCheckFrame(LastCheckLocation.BASE);
			if (baseLastCheckFrame > twoHatchLairBuildFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.NO_LAIR);
			}
		}
	}

}
