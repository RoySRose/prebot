package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions.AddOnOption;

public class BuilderVulture extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderVulture(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	//FileUtils.appendTextToFile("log.txt", "\n BuilderVulture ==>>> " + metaType.getName());
    	
    	if(StrategyIdea.addOnOption == AddOnOption.IMMEDIATELY && !UnitUtils.myUnitDiscovered(UnitType.Terran_Machine_Shop)){
			return false;
		}
    	

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }
        	
        return false;
    }
}


