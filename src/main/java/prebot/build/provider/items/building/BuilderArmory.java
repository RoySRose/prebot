package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
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
        System.out.println("Armory build condition check");
        
        boolean chk_armory = UnitUtils.myUnitDiscovered(UnitType.Terran_Armory);
        
        if(RespondToStrategy.Instance().enemy_scout 
        		|| RespondToStrategy.Instance().enemy_shuttle 
        		|| RespondToStrategy.Instance().enemy_wraith){
			if(!chk_armory){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
					if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						return true;
					}
				}
			}else{
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 2){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
							/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
							return true;
						}
					}
				}
			}
		}
        
        if(RespondToStrategy.Instance().enemy_arbiter){
			if(!chk_armory){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
					if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						/*BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
						return true;
					}
				}
			}else{
				if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
						InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 4)
						|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 4){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
							/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
							return true;
						}
					}
				}
			}
		}
        
        return false;
    }


}
