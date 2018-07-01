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
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;

public class BuilderArmory extends DefaultBuildableItem {

    public BuilderArmory(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        //System.out.println("Armory build condition check");
        
        boolean chk_armory = UnitUtils.myUnitDiscovered(UnitType.Terran_Armory);
        
        
        if(BuildQueueProvider.Instance().respondSet) {
        
        
	        if(RespondToStrategy.Instance().enemy_scout 
	        		|| RespondToStrategy.Instance().enemy_shuttle 
	        		|| RespondToStrategy.Instance().enemy_wraith){
	        	
				if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
					if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
							if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
									&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
//										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
								setBlocking(true);
								setHighPriority(true);
								setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
								return true;
							}
						}
					}
				}
	        }
        
	        if(RespondToStrategy.Instance().enemy_arbiter){
	        	if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
//									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							setBlocking(true);
							setHighPriority(true);
							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
							return true;
						}
					}
				}
			}
        
//	        if (BuildConditionChecker.Instance().armory == false) {
//				if (BuildConditionChecker.Instance().CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
//					if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Armory)) {
//	//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
//	//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
//	//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
//						return true;
//					}
//				}
//			}
        }else {
        	if (BuildConditionChecker.Instance().armory == false) {
    			if (BuildConditionChecker.Instance().CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
    				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
    						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
//    					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
    					return true;
    				}
    			}
    		}
        }
        return false;
    }


}
