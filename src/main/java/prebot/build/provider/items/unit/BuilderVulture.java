package prebot.build.provider.items.unit;

import bwapi.Race;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.util.FileUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyManager;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuilderVulture extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderVulture(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	//FileUtils.appendTextToFile("log.txt", "\n BuilderVulture ==>>> " + metaType.getName());

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
        	
        	if(BuildQueueProvider.Instance().respondSet) {
        		if(InformationManager.Instance().enemyRace == Race.Zerg) {
		        	if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_ONLYLING){
		    			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().selfPlayer) < 5	){
		    				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) < 1){
	//	    						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
		    					setHighPriority(true);
		    					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
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
