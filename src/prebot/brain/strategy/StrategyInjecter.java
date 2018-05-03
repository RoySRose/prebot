package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.build.BuildOrderItem;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.UnitUtils;
import prebot.common.util.UpgradeUtils;
import prebot.main.manager.BuildManager;

public class StrategyInjecter {
	
	public static void inject(Strategy strategy) {
		Idea.of().strategy = strategy;
		if (strategy instanceof FixedStrategy) {
			injectBuildQueue(((FixedStrategy) strategy).buildItemList);
		}
	}
	
	// 빌드큐에 직접 입력한다.
	private static void injectBuildQueue(List<BuildOrderItem> buildItemList) {
		List<BuildOrderItem> resultBuildItemList = new ArrayList<>();
		Map<UnitType, Integer> buildUnitCountMap = new HashMap<>();
		for (BuildOrderItem buildOrderItem : buildItemList) {
			if (buildOrderItem.metaType.isUnit()) {
				UnitType unitType = buildOrderItem.metaType.getUnitType();
				Integer count = buildUnitCountMap.get(unitType);
				count = count == null ? 1 : count + 1;
				buildUnitCountMap.put(unitType, count);
				if (UnitUtils.hasUnit(unitType, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, count)) {
					continue;
				}
				resultBuildItemList.add(buildOrderItem);
				
			} else if (buildOrderItem.metaType.isTech()) {
				if (UpgradeUtils.hasResearched(buildOrderItem.metaType.getTechType())) {
					continue;
				}
				resultBuildItemList.add(buildOrderItem);
			}
		}
		
		for (BuildOrderItem buildOrderItem : resultBuildItemList) {
			BuildManager.Instance().buildQueue.queueItem(buildOrderItem);
		}
	}
}
