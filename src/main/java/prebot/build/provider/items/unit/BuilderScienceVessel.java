package prebot.build.provider.items.unit;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.build.provider.StarportUnitSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.strategy.RespondToStrategy;

public class BuilderScienceVessel extends DefaultBuildableItem {

	StarportUnitSelector starportUnitSelector;

    public BuilderScienceVessel(MetaType metaType, StarportUnitSelector starportUnitSelector){
        super(metaType);
        this.starportUnitSelector = starportUnitSelector;
    }

    public final boolean buildCondition(){

        if(starportUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
        	
        	if(BuildQueueProvider.Instance().respondSet) {
        		
        	}else {
        	
	        	if ((RespondToStrategy.Instance().need_vessel == true && BuildConditionChecker.Instance().CC >= 2) || BuildConditionChecker.Instance().CC >= 3) {
	    			if (BuildConditionChecker.Instance().starComplete && BuildConditionChecker.Instance().scienceComplete && BuildConditionChecker.Instance().controltower) {
	    				if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Science_Vessel) < RespondToStrategy.Instance().max_vessel) {
	    					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel, null)
	    							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Vessel, null) == 0) {
	//    						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel, false);
	    						return true;
	    					}
	    				}
	    			}
	    		}
        	}
        	
            return false;
        }
    }
}
