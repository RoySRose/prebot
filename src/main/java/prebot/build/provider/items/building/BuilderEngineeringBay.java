package prebot.build.provider.items.building;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;

public class BuilderEngineeringBay extends DefaultBuildableItem {

    public BuilderEngineeringBay(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("EngineeringBay build condition check");
    	
    	
    	if(BuildQueueProvider.Instance().respondSet) {
    		
    		if (RespondToStrategy.Instance().max_turret_to_mutal != 0) {
    			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay)) {
    				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) 
    						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay,	null) == 0) {
//    					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
//    							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
    					setBlocking(true);
    					setHighPriority(true);
						setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
						return true;
    				}
    			} 
    		}
    		
    	}else {
    	
	    	int cc = 0;
	    	
	//    	boolean engineeringChk = UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay);
	    	
	    	
	    	
	//    	FileUtils.appendTextToFile("log.txt", "\n BuilderEngineeringBay || RespondToStrategy.Instance().enemy_dark_templar ==> " + RespondToStrategy.Instance().enemy_dark_templar);
	    	
	        if(!BuildConditionChecker.Instance().engineering) {
	        	if(InformationManager.Instance().enemyRace != Race.Protoss || Prebot.Broodwar.getFrameCount() > 5000){
	//			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
	//					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
	//				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
	        		if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
	        				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
		        		setBlocking(true);
						return true;
	        		}
				}
	        	
	        	if (Prebot.Broodwar.getFrameCount() > 17000) {
	//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
//	        		if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Engineering_Bay)) {
	        		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay, null)
							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
	        			return true;
	        		}
				}
	        	
	        	for (Unit unit : Prebot.Broodwar.self().getUnits()) {
	        		if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
	        			cc++;
					}
				}
	        	
	        	if(cc>=2) {
	        		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay, null)
							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
	        			return true;
	        		}
				}
			}
	 
//	        if (InitialBuildProvider.Instance().InitialBuildFinished == true && Prebot.Broodwar.getFrameCount() > 11000) {
//				if(!BuildConditionChecker.Instance().engineering) {
//					return true;
//				}else if(BuildConditionChecker.Instance().engineering) {
//					for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//						if (unit.getType() == UnitType.Terran_Engineering_Bay) {
//							if(unit.getHitPoints() < UnitType.Terran_Engineering_Bay.maxHitPoints() / 3) {
//								if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Engineering_Bay)) {
//									return true;
//								}
//							}
//						}
//					}
//				}
//			}
	        
	        if (InitialBuildProvider.Instance().InitialBuildFinished == true
					&& (BuildConditionChecker.Instance().engineeringcnt == 0 || (BuildConditionChecker.Instance().engineeringcnt == 1 && BuildConditionChecker.Instance().engineeringUnit.getHitPoints() < UnitType.Terran_Engineering_Bay.maxHitPoints() / 3))
					&& Prebot.Broodwar.getFrameCount() > 11000) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
					return true;
				}
			}
	
	        
	        if((RespondToStrategy.Instance().enemy_dark_templar 
	    		|| RespondToStrategy.Instance().enemy_wraith 
	    		|| RespondToStrategy.Instance().enemy_lurker 
	    		|| RespondToStrategy.Instance().enemy_shuttle) && (!BuildConditionChecker.Instance().engineering)){
	        		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
			        	setBlocking(true);
			        	setHighPriority(true);
						return true;
		        	}
			}
        
    	}
        
        return false;
    }


}
