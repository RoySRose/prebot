package prebot.strategy.analyse.zerg;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class ZerglingAnalyser extends UnitAnalyser {

	public ZerglingAnalyser() {
		super(UnitType.Zerg_Zergling);
	}

	@Override
	public void analyse() {
		fastPoolByZergling();
	}

	private void fastPoolByZergling() {
		if (ClueManager.Instance().containsClueType(ClueType.POOL_ASSUME)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}

		int fiveDroneFrame = EnemyStrategy.ZERG_5DRONE.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 5);
		int nineDroneFrame = EnemyStrategy.ZERG_9DRONE.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 5);
		int overPoolFrame = EnemyStrategy.ZERG_OVERPOOL.defaultTimeMap.frame(UnitType.Zerg_Spawning_Pool, 20); // 오버풀,11풀,12풀
		int buildToZerglingFrame = UnitType.Zerg_Spawning_Pool.buildTime() + UnitType.Zerg_Zergling.buildTime();
		
		RegionType foundRegionType = PositionUtils.positionToRegionType(found.get(0).getLastPosition());
		int movedFrame = 0;
		if (foundRegionType == RegionType.ENEMY_BASE) {
			movedFrame = 0;
		} else if (foundRegionType == RegionType.ENEMY_FIRST_EXPANSION) {
			movedFrame = 5 * TimeUtils.SECOND;
		} else if (foundRegionType == RegionType.MY_FIRST_EXPANSION
				|| foundRegionType == RegionType.MY_BASE) {
			movedFrame = baseToBaseFrame(UnitType.Zerg_Zergling) + 5 * TimeUtils.SECOND;
		} else {
			movedFrame = 10 * TimeUtils.SECOND; // 발견위치로 더 상세하게 판단 가능
		}
		
		int foundFrame = found.get(0).getUpdateFrame();
		if (foundFrame < fiveDroneFrame + buildToZerglingFrame + movedFrame) {
			ClueManager.Instance().addClueInfo(ClueInfo.POOL_5DRONE);
		} else if (foundFrame < nineDroneFrame + buildToZerglingFrame + movedFrame) {
			ClueManager.Instance().addClueInfo(ClueInfo.POOL_9DRONE_UNDER);
		} else if (foundFrame < overPoolFrame + buildToZerglingFrame + movedFrame) {
			ClueManager.Instance().addClueInfo(ClueInfo.POOL_OVERPOOL_UNDER);
		}
	}

}
