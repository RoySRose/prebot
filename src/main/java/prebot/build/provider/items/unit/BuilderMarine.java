package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BarrackUnitSelector;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;
import prebot.strategy.StrategyManager;
import prebot.strategy.TempBuildSourceCode;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuilderMarine extends DefaultBuildableItem {

    public BuilderMarine(MetaType metaType){
        super(metaType);
    }
    
    public boolean liftChecker = false;

    public final boolean buildCondition(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
    	
    	List<Unit> barracks = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Barracks);
		for(Unit unit : barracks) {
			if(!unit.isLifted()) {
				liftChecker = true;
			}
		}

    	if(liftChecker) {
    		return false;
    	}
//    	liftChecker = false;
    	
    	

       
    	return false;

    }
}
