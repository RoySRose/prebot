

import bwapi.UnitType;

public class BuilderVulture extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderVulture(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	if(StrategyIdea.addOnOption == EnemyStrategyOptions.AddOnOption.IMMEDIATELY && !UnitUtils.myUnitDiscovered(UnitType.Terran_Machine_Shop)){
			return false;
		}
    	

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }
        	
        return false;
    }
}


