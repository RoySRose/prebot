package prebot.build.provider;


import bwapi.TechType;
import bwapi.UnitType;
import prebot.build.MetaType;

public class FactoryUnitSelector implements Selector<UnitType>{

    UnitType unitType;
    //BuildCondition buildCondition;

    public final UnitType getSelected(){
        return unitType;
    }

    public final void select(){
        unitType = UnitType.Terran_Goliath;
    }
}
