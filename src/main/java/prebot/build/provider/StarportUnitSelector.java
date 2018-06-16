package prebot.build.provider;


import bwapi.UnitType;

public class StarportUnitSelector implements Selector<UnitType>{

    UnitType unitType;
    //BuildCondition buildCondition;

    public final UnitType getSelected(){
        return unitType;
    }

    public final void select(){
        unitType = UnitType.Terran_Wraith;
    }
}
