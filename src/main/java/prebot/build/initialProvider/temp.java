package prebot.build.initialProvider;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.common.constant.CommonCode;
import prebot.common.util.UnitUtils;
import prebot.common.util.UpgradeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//public class temp{
//public static class injectBuildQueue(List<BuildOrderItem> buildItemList) {
//        List<BuildOrderItem> resultBuildItemList = new ArrayList<>();
//        Map<UnitType, Integer> buildUnitCountMap = new HashMap<>();
//        for (BuildOrderItem buildOrderItem : buildItemList) {
//        if (buildOrderItem.metaType.isUnit()) {
//        UnitType unitType = buildOrderItem.metaType.getUnitType();
//        Integer count = buildUnitCountMap.get(unitType);
//        count = count == null ? 1 : count + 1;
//        buildUnitCountMap.put(unitType, count);
//        if (UnitUtils.hasUnit(unitType, CommonCode.UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, count)) {
//        continue;
//        }
//        resultBuildItemList.add(buildOrderItem);
//
//        } else if (buildOrderItem.metaType.isTech()) {
//        if (UpgradeUtils.hasResearched(buildOrderItem.metaType.getTechType())) {
//        continue;
//        }
//        resultBuildItemList.add(buildOrderItem);
//        }
//        }
//
//        for (BuildOrderItem buildOrderItem : resultBuildItemList) {
//        BuildManager.Instance().buildQueue.queueItem(buildOrderItem);
//        }
//}
//        }