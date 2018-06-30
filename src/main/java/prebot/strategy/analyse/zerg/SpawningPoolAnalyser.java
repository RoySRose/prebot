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

public class SpawningPoolAnalyser extends UnitAnalyser {

	public SpawningPoolAnalyser() {
		super(UnitType.Zerg_Spawning_Pool);
	}

	@Override
	public void analyse() {
		fastPool();
	}

	private void fastPool() {
		if (ClueManager.Instance().containsClueType(ClueType.POOL)) {
			return;
		}
		
		int fiveDroneFrame = EnemyStrategy.ZERG_5DRONE.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 5);
		int nineDroneFrame = EnemyStrategy.ZERG_9DRONE.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 5);
		int overPoolFrame = EnemyStrategy.ZERG_OVERPOOL.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 20); // 오버풀,11풀,12풀
		int doubleFrame = EnemyStrategy.ZERG_2HAT_GAS.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 10);
		
		List<UnitInfo> found = found(RegionType.ENEMY_BASE);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			if (buildFrame < fiveDroneFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.POOL_5DRONE);
			} else if (buildFrame < nineDroneFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.POOL_9DRONE);
			} else if (buildFrame < overPoolFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.POOL_OVERPOOL);
			} else if (buildFrame < doubleFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.POOL_2HAT);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.POOL_2HAT); // 아주 늦은 스포닝풀
			}
			
		} else {
			int baseLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.BASE);
			if (baseLastCheckFrame > doubleFrame) { // 더블 타이밍 지남
				ClueManager.Instance().addClueInfo(ClueInfo.NO_POOL);
			}
		}
	}

}
