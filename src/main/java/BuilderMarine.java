

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;

public class BuilderMarine extends DefaultBuildableItem {

    public BuilderMarine(MetaType metaType){
        super(metaType);
    }
    
    public boolean liftChecker = false;

    public final boolean buildCondition(){
    	
    	if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
    	
    	List<Unit> barracks = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Barracks);
		for(Unit unit : barracks) {
			if(!unit.isLifted()) {
				liftChecker = true;
			}
		}

    	if(!liftChecker) {
    		return false;
    	}

    	int nowMarine = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine);
    	
//    	마린이 2마리가 생산된 상태에서 팩토리가 없다면 팩토리 먼저
    	if(nowMarine == 2 && UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Factory) == 0) {
    		return false;
    	}
    	
        if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Marine, null) > 0) {
        	return false;
        }
	        
		if(nowMarine < StrategyIdea.marineCount) {
			setHighPriority(true);
       		return true;
    	}
       
    	return false;

    }
    
    @Override
    public boolean checkInitialBuild(){
		return true;
    }
}
