package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.StrategyAnalyseManager;
import prebot.strategy.manage.StrategyAnalyseManager.LastCheckLocation;

public class HatcheryAnalyser extends UnitAnalyser {

	public HatcheryAnalyser() {
		super(UnitType.Zerg_Hatchery);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(ClueType.DOUBLE_HATCH)) {
			return;
		}
		
		int doubleFrame = EnemyStrategy.ZERG_2HAT_GAS.defaultTimeMap.time(UnitType.Zerg_Hatchery, 10);
		int overPoolFrame = EnemyStrategy.ZERG_OVERPOOL.defaultTimeMap.time(UnitType.Zerg_Hatchery, 10);
		int nineDroneFrame = EnemyStrategy.ZERG_9DRONE.defaultTimeMap.time(UnitType.Zerg_Hatchery, 10);
		
		List<UnitInfo> found = found(RegionType.ENEMY_FIRST_EXPANSION);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			
			if (buildFrame < doubleFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.DOUBLE_HATCH_12HAT);
			} else if (buildFrame < overPoolFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.DOUBLE_HATCH_OVERPOOL);
			} else if (buildFrame < nineDroneFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.DOUBLE_HATCH_9DRONE);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.DOUBLE_HATCH_LATE);
			}
			
		} else {
			int expansionLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.FIRST_EXPANSION);
			if (expansionLastCheckFrame > nineDroneFrame) { // 더블 타이밍 지남
				ClueManager.Instance().addClueInfo(ClueInfo.DOUBLE_HATCH_LATE);
			}

			found = found(RegionType.ENEMY_BASE);
			if (found.size() >= 2) {
				int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				if (buildFrame < nineDroneFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.TWIN_HATCH);
				}
			}
		}
	}

}
