package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.StarportUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.RespondToStrategy;

public class BuilderScienceVessel extends DefaultBuildableItem {

	StarportUnitSelector starportUnitSelector;

    public BuilderScienceVessel(MetaType metaType, StarportUnitSelector starportUnitSelector){
        super(metaType);
        this.starportUnitSelector = starportUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
    		return false;
    	}else {
    		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Science_Facility) == 0) {
    			return false;
    		}
    		
    	}
    	
    	
    	
//		활성화된 커맨드가 2개 이상일 경우
		int myCommand = 0;
		
		int controlTower = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Control_Tower);
		
		List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		for(Unit unit : CommandCenter) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
				myCommand++;
			}
		}

        if(starportUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
	        	if ((RespondToStrategy.Instance().need_vessel == true && myCommand >= 2) || myCommand >= 3) {
	    			if (controlTower != 0) {
	    				if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Science_Vessel) < RespondToStrategy.Instance().max_vessel) {
	    					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel, null)
	    							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Vessel, null) == 0) {
	//    						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel, false);
	    						return true;
	    					}
	    				}
	    			}
	    		}
            return false;
        }
    }
}
