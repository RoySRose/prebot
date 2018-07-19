package prebot.build.provider.items.unit;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.strategy.RespondToStrategy;

public class BuilderSiegeTank extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderSiegeTank(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) == 0) {
    		return false;
    	}
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop) == 0) {
    		return false;
    	}

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
//        	FileUtils.appendTextToFile("log.txt", "\n BuilderSiegeTank || SiegeTank selected");
//        	if(RespondToStrategy.Instance().once_tank) {
//        		setBlocking(true);
//        		RespondToStrategy.Instance().once_tank = false;
//        	}
       	
        	return true;
        	
        }
	    return false;
    }
}
