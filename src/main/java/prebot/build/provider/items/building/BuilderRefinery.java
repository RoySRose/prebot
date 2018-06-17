package prebot.build.provider.items.building;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;

public class BuilderRefinery extends DefaultBuildableItem {

    public BuilderRefinery(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("Refinery build condition check");
        
        boolean isInitialBuildOrderFinished = false;
        
     // initial이 끝났는지에 대한 체크
 		if (!isInitialBuildOrderFinished && BuildManager.Instance().buildQueue.isEmpty()) {
 			isInitialBuildOrderFinished = true;
 		}
        
        
        for (Unit unit : Prebot.Broodwar.self().getUnits()) {
        	// 가스 start
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {

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

				if (isInitialBuildOrderFinished == false && geyserAround != null) {
					if (InformationManager.Instance().getMyfirstGas() != null) {
						if (geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())) {
							continue;
						}
					}
				}

				if (refineryAlreadyBuilt == false) {
					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
					if (findGeyser != null) {
						if (findGeyser.getDistance(unit.getTilePosition()) * 32 > 300) {
							continue;
						}
						if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 9) {
							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
								//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
								return true;
							}
						}
					}
				}

				/*if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
					CC++;
				}*/
			}
			// 가스 end
        }
        
        
        return false;
    }


}
