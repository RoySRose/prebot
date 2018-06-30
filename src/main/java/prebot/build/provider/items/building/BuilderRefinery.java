package prebot.build.provider.items.building;

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
import prebot.common.main.Prebot;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;

public class BuilderRefinery extends DefaultBuildableItem {

    public BuilderRefinery(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("Refinery build condition check");
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {
    		if (InitialBuildProvider.Instance().InitialBuildFinished == false && Prebot.Broodwar.getFrameCount() % 23 == 0) {
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
    		}
	        if(BuildConditionChecker.Instance().buildGas) {
	        	settilePosition(BuildConditionChecker.Instance().findGeyser);
	        	return true;
	        }

    	}
        
        return false;
    }


}
