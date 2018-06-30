package prebot.build.provider;


import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.common.main.Prebot;
import prebot.strategy.StrategyManager;
import prebot.strategy.TempBuildSourceCode;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BarrackUnitSelector implements Selector<UnitType>{

    UnitType unitType;
    //BuildCondition buildCondition;

    public final UnitType getSelected(){
        return unitType;
    }
    
    public boolean block;
    public boolean highpriority;
    public boolean tileposition;
    public boolean seedposition;

    public final void select(){
    	if(BuildQueueProvider.Instance().respondSet) {
    		unitType = UnitType.None;
    	}else {
    		unitType = UnitType.None;
    	}
    }
}
