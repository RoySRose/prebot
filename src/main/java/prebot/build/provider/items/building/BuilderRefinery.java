package prebot.build.provider.items.building;

import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.InitialBuildProvider.AdaptStrategyStatus;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;

public class BuilderRefinery extends DefaultBuildableItem {

    public BuilderRefinery(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery, null);
		if (buildQueueCount > 0) {
			return false;
		}
		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Refinery, null);
		if (constructionCount > 0) {
			return false;
		}
    	
		if (!InformationManager.Instance().isGasRushed() && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Refinery) == 0) {
			int barrackCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks);
			int suppleCompleteCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Supply_Depot);
			int scvCompleteCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_SCV);
			
			if (barrackCount > 0 && suppleCompleteCount > 0 && scvCompleteCount > 10) {
				setBlocking(true);
				setHighPriority(true);
				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				return true;
			}
		}

		// 리파이너리는 커맨드 센터보다 많을 필요 없다.
		int refineryCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Refinery);
		List<Unit> commandCenterList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		if (commandCenterList.size() <= refineryCount) {
			return false;
		}
		
		TilePosition findGeyser = null;
		for (Unit commandCenter : commandCenterList) {
			Unit geyserAround = null;
			boolean refineryAlreadyBuilt = false;
			for (Unit unitsAround : Prebot.Broodwar.getUnitsInRadius(commandCenter.getPosition(), 300)) {
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

			if (InitialBuildProvider.Instance().getAdaptStrategyStatus() != AdaptStrategyStatus.COMPLETE && geyserAround != null) {
				if (InformationManager.Instance().getMyfirstGas() != null) {
					if (geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())) {
						continue;
					}
				}
			}

			if (refineryAlreadyBuilt == false) {
				findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(commandCenter.getTilePosition());
				if (findGeyser != null) {
					if (findGeyser.getDistance(commandCenter.getTilePosition()) * 32 > 300) {
						continue;
					}
					if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(commandCenter) > 9) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
							setTilePosition(findGeyser);
							return true;
						}
					}
				}
			}
		}

        return false;
    }

    
    @Override
    public boolean checkInitialBuild(){
		return true;
    }

}
