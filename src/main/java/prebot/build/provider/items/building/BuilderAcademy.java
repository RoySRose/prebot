package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.RespondToStrategy;

public class BuilderAcademy extends DefaultBuildableItem {

    public BuilderAcademy(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        //System.out.println("Academy build condition check");
    	
    	if(BuildQueueProvider.Instance().respondSet) {
    		if (RespondToStrategy.Instance().enemy_dark_templar 
	    		|| RespondToStrategy.Instance().enemy_wraith 
	    		|| RespondToStrategy.Instance().enemy_lurker 
	    		|| RespondToStrategy.Instance().enemy_arbiter 
	    		|| RespondToStrategy.Instance().prepareDark) {
	        	
	        	if (RespondToStrategy.Instance().need_vessel_time == 0) {
	        		RespondToStrategy.Instance().need_vessel_time = Prebot.Broodwar.getFrameCount();
				}
	        	
	        	if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Academy)) {
					// 아카데미가 없다면
	        		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1
	        			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
	//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1
	//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
						// 지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
	//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Academy,
	//							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						setBlocking(true);
						setHighPriority(true);
						return true;
					}
				}
			}
    		return false;
    	}else {
    	
	    	if(!BuildConditionChecker.Instance().aca) {
		    	if (BuildConditionChecker.Instance().CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
		    		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0
    						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
	
	//						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0
	//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
						return true;
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy, false);
					}
				}
	    	}
    	}

        return false;
    }


}
