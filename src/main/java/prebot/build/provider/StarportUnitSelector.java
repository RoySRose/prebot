package prebot.build.provider;


import bwapi.UnitType;

public class StarportUnitSelector implements Selector<UnitType>{

    UnitType unitType;
    //BuildCondition buildCondition;

    public final UnitType getSelected(){
        return unitType;
    }

    public final void select(){
    	if(BuildQueueProvider.Instance().respondSet) {
    		unitType = UnitType.None;
    	}else {
    		unitType = UnitType.None;
    	}
    }
}
