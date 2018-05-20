package prebot.build.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.TechType;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.Info;
import prebot.build.BuildOrderItem;
import prebot.build.temp.strategy.FixedBuildBuilder;
import prebot.common.code.Code.InitialBuildType;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.HardCodePositionUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.UpgradeUtils;
import prebot.common.util.internal.HardCodeInfo;
import prebot.main.GameManager;

public class TestManager extends GameManager {

	private static TestManager instance = new TestManager();

	/// static singleton 객체를 리턴합니다
	public static TestManager Instance() {
		return instance;
	}

	@Override
	public GameManager update() {
		HardCodeInfo hardCodeInfo = HardCodePositionUtils.getHardCodeInfo(Info.of().myBase.getPosition());
		InitialBuildType initialBuildType = Idea.of().initialBuildType;
		
		if (initialBuildType == InitialBuildType.ONE_FACTORY_MULTI) {
			List<BuildOrderItem> buildItemList = getOneFactoryBuild(hardCodeInfo);
			injectBuildQueue(buildItemList);
			
		} else if (initialBuildType == InitialBuildType.TWO_FACTORY_MULTI) {
			List<BuildOrderItem> buildItemList = getOneFactoryBuild(hardCodeInfo);
			injectBuildQueue(buildItemList);
			
		} else if (initialBuildType == InitialBuildType.FAST_BARRACKS_ONE_FACOTRY_MULTI) {
			List<BuildOrderItem> buildItemList = getFastBarracksBuild(hardCodeInfo);
			injectBuildQueue(buildItemList);
		}
		
		return this;
	}

	private List<BuildOrderItem> getOneFactoryBuild(HardCodeInfo hardCodeInfo) {
		return new FixedBuildBuilder().
				add(UnitType.Terran_Command_Center, true).
				add(UnitType.Terran_SCV, true).times(8).
				add(UnitType.Terran_SCV, false).times(1).
				add(UnitType.Terran_Supply_Depot, false, hardCodeInfo.firstDepotTile).
				add(UnitType.Terran_SCV, false).times(2).
				add(UnitType.Terran_Barracks, true, hardCodeInfo.firstBarracksTile).
				add(UnitType.Terran_SCV, false).times(1).
				add(UnitType.Terran_Refinery, true).
				add(UnitType.Terran_SCV, false).times(3).
				add(UnitType.Terran_Marine, false).
				add(UnitType.Terran_Factory, true, hardCodeInfo.firstFactoryTile).
				add(UnitType.Terran_Supply_Depot, true, hardCodeInfo.secondDepotTile).
				add(UnitType.Terran_Marine, false).times(3).
				add(UnitType.Terran_SCV, false).times(4).
				add(UnitType.Terran_Machine_Shop, true).
				add(UnitType.Terran_Command_Center, false).
				add(UnitType.Terran_Siege_Tank_Tank_Mode, true).
				add(UnitType.Terran_Supply_Depot, true).
				add(UnitType.Terran_SCV, false).times(2).
				build();
	}

	private List<BuildOrderItem> getFastBarracksBuild(HardCodeInfo hardCodeInfo) {
		return new FixedBuildBuilder().
				add(UnitType.Terran_Command_Center, true).
				add(UnitType.Terran_SCV, true).times(8).
				add(UnitType.Terran_Barracks, true, hardCodeInfo.firstBarracksTile).
				add(UnitType.Terran_SCV, true).times(1).
				add(UnitType.Terran_Supply_Depot, true, hardCodeInfo.firstDepotTile).
				add(UnitType.Terran_SCV, true).times(2).
				add(UnitType.Terran_Refinery, false).
				add(UnitType.Terran_Marine, false).times(3).
				add(UnitType.Terran_SCV, false).times(2).
				add(UnitType.Terran_Factory, true, hardCodeInfo.firstFactoryTile).
				add(UnitType.Terran_Marine, false).times(1).
				add(UnitType.Terran_Supply_Depot, true, hardCodeInfo.secondDepotTile).
				add(UnitType.Terran_SCV, false).times(4).
				add(UnitType.Terran_Vulture, true).times(1).
				add(UnitType.Terran_Starport, true).
				add(UnitType.Terran_Machine_Shop, true).
				add(UnitType.Terran_SCV, false).times(3).
				add(UnitType.Terran_Supply_Depot, false).
				add(TechType.Spider_Mines, false).
				add(UnitType.Terran_Vulture, false).times(1).
				add(UnitType.Terran_Wraith, false).times(1).
				add(UnitType.Terran_SCV, false).times(1).
				build();
	}

	private void injectBuildQueue(List<BuildOrderItem> buildItemList) {
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
