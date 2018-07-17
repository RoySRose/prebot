package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;

public class BuilderEngineeringBay extends DefaultBuildableItem {

    public BuilderEngineeringBay(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("EngineeringBay build condition check");
    	
    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) != 0) {
    		return false;
    	}
    	
    	
    	if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) 
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) != 0){
    		return false;
    	}
    	
    	if(StrategyIdea.engineeringBayBuildStartFrame < Prebot.Broodwar.getFrameCount()) {
    		setBlocking(true);
    		setHighPriority(true);
    		return true;
    	}
        
        return false;
    }


}
