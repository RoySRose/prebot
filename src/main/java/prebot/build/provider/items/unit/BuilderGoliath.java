package prebot.build.provider.items.unit;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;

public class BuilderGoliath extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderGoliath(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	
    	if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
    		return true;
    	}else {
        	
	    	if(BuildQueueProvider.Instance().respondSet) {
	    		if(RespondToStrategy.Instance().enemy_scout 
		        		|| RespondToStrategy.Instance().enemy_shuttle 
		        		|| RespondToStrategy.Instance().enemy_wraith){
	    			
	    			if(UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
	    				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 2){
	    					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
	    						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
	    								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
	//        							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
	//        									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
	    							setBlocking(true);
	    							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
	    							return true;
	    						}
	    					}
	    				}
	    			}
	    		}
	    		
	    		if(RespondToStrategy.Instance().enemy_arbiter){
	    			if(UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
	    				if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
	    						InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 4)
	    						|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 4){
	    					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
	    						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
	    								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
	//        							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
	//        									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
	    							setBlocking(true);
	    							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
	    							return true;
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}
	    	return false;
    	}
    }
}
