package prebot.build.provider.items.building;

import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;

public class BuilderRefinery extends DefaultBuildableItem {

    public BuilderRefinery(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	
    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Refinery, null) != 0) {
    		return false;
    	}
    	
//        System.out.println("Refinery build condition check");
//    		if (InitialBuildProvider.Instance().InitialBuildFinished == false && Prebot.Broodwar.getFrameCount() % 23 == 0) {
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery, null) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Refinery)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Refinery, null) == 0) {
			if (InformationManager.Instance().isGasRushed() == false) {
				int barrack = 0;
				int supple = 0;
				int scv = 0;
				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
					if (unit == null)
						continue;
					if (unit.getType() == UnitType.Terran_Barracks) {
						barrack++;
					}
					if (unit.getType() == UnitType.Terran_Supply_Depot && unit.isCompleted()) {
						supple++;
					}
					if (unit.getType() == UnitType.Terran_SCV && unit.isCompleted()) {
						scv++;
					}
				}
				if (barrack > 0 && supple > 0 && scv > 10) {
//    						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					setBlocking(true);
					setHighPriority(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
					return true;
				}
			}
		}
//    		}
		
		int refineryCnt = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Refinery);
		List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
//		리파이너리는 커맨드 센터보다 많을 필요 없다.
		if(CommandCenter.size() <= refineryCnt) {
			return false;
		}
		TilePosition findGeyser = null;
		for(Unit unit : CommandCenter) {
		
			Unit geyserAround = null;
			
			boolean refineryAlreadyBuilt = false;
			for (Unit unitsAround : Prebot.Broodwar.getUnitsInRadius(unit.getPosition(), 300)) {
				if (unitsAround == null)
					continue;
				if (unitsAround.getType() == UnitType.Terran_Refinery || unitsAround.getType() == UnitType.Zerg_Extractor
						|| unitsAround.getType() == UnitType.Protoss_Assimilator) {
					refineryAlreadyBuilt = true;
					break;
				}
				if (unitsAround.getType() == UnitType.Resource_Vespene_Geyser) {
					geyserAround = unitsAround;
				}
			}

			if (InitialBuildProvider.Instance().InitialBuildFinished == false && geyserAround != null) {
				if (InformationManager.Instance().getMyfirstGas() != null) {
					if (geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())) {
						continue;
					}
				}
			}

			if (refineryAlreadyBuilt == false) {
				findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
				if (findGeyser != null) {
					if (findGeyser.getDistance(unit.getTilePosition()) * 32 > 300) {
						continue;
					}
					if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 9) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
							//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
							setTilePosition(findGeyser);
							return true;
						}
					}
				}
			}
		}

        return false;
    }


}
