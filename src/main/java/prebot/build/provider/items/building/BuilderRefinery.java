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
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.MyBotModule;
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
		
//		//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: check gasrush :: " + Prebot.Broodwar.getFrameCount() + " :: " + InformationManager.Instance().isGasRushed());
    	
		if (!InformationManager.Instance().isGasRushed() && MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Refinery) == 0) {
			
			int barrackCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Barracks);
			int suppleCompleteCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Supply_Depot);
			int scvCompleteCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV);
			
			if (barrackCount > 0 && suppleCompleteCount > 0 && scvCompleteCount > 10) {
				
//				//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: no gasrush :: no refinery :: immediately :: " + Prebot.Broodwar.getFrameCount() + " :: " + InformationManager.Instance().myfirstGas.getTilePosition());
				setBlocking(true);
				setHighPriority(true);
//				setTilePosition(InformationManager.Instance().myfirstGas.getTilePosition());
				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				return true;
			}
		}

		// 리파이너리는 커맨드 센터보다 많을 필요 없다.
		int refineryCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Refinery);
		List<Unit> commandCenterList = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		if (commandCenterList.size() <= refineryCount) {
			return false;
		}
		
		TilePosition findGeyser = null;
		for (Unit commandCenter : commandCenterList) {
			Unit geyserAround = null;
			boolean refineryAlreadyBuilt = false;
			for (Unit unitsAround : MyBotModule.Broodwar.getUnitsInRadius(commandCenter.getPosition(), 300)) {
				if (unitsAround == null)
					continue;
				if (unitsAround.getType() == UnitType.Terran_Refinery || unitsAround.getType() == UnitType.Zerg_Extractor
						|| unitsAround.getType() == UnitType.Protoss_Assimilator) {
//					//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: already built :: " + unitsAround.getTilePosition());
					refineryAlreadyBuilt = true;
					break;
				}
				if (unitsAround.getType() == UnitType.Resource_Vespene_Geyser) {
//					//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: find gas :: " + unitsAround.getTilePosition());
					geyserAround = unitsAround;
				}
			}
//			
//			if(ConstructionPlaceFinder.Instance().isReservedTile(geyserAround.getTilePosition().getX(),geyserAround.getTilePosition().getY())) {
//				System.out.println("isReservedTile :: true :: " + geyserAround.getTilePosition());
//			}

			if (InitialBuildProvider.Instance().getAdaptStrategyStatus() != InitialBuildProvider.AdaptStrategyStatus.COMPLETE && geyserAround != null) {
//				//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: aaa :: " + geyserAround.getTilePosition());
				if (InformationManager.Instance().getMyfirstGas() != null) {
//					//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: bbb :: " + geyserAround.getTilePosition());
					if (geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())) {
//						//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: ccc :: " + geyserAround.getTilePosition());
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
//							//FileUtils.appendTextToFile("log.txt", "\n  BuilderRefinery :: set BuildQuere :: " + findGeyser);
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
