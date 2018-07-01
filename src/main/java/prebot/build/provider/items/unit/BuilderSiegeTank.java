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
    	int machine_shop_cnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Machine_Shop);

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
//        	FileUtils.appendTextToFile("log.txt", "\n BuilderSiegeTank || SiegeTank selected");
//        	if(RespondToStrategy.Instance().once_tank) {
//        		setBlocking(true);
//        		RespondToStrategy.Instance().once_tank = false;
//        	}
        	if(machine_shop_cnt > 0) {
//        		FileUtils.appendTextToFile("log.txt", "\n BuilderSiegeTank || machine_shop_cnt > 0");
        		return true;
        	}
        	
        	return false;
        	
        }else{
        	if(BuildQueueProvider.Instance().respondSet) {
        		return false;
        	}else {
	            return false;
	        }
        }
    }
}
