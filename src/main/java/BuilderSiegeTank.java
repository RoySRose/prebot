

import bwapi.UnitType;

public class BuilderSiegeTank extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderSiegeTank(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) == 0) {
    		return false;
    	}
    	
    	if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop) == 0) {
    		return false;
    	}

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {

//        	if(RespondToStrategy.Instance().once_tank) {
//        		setBlocking(true);
//        		RespondToStrategy.Instance().once_tank = false;
//        	}
        	if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Siege_Tank_Tank_Mode)
        			&& StrategyIdea.factoryRatio.weight == EnemyStrategyOptions.FactoryRatio.Weight.TANK) {
        		setHighPriority(true);
        	}
        	return true;
        	
        }
	    return false;
    }
}
