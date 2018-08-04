package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

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

    	if(!liftChecker) {
//    		FileUtils.appendTextToFile("log.txt", "\n BuilderMarine liftChecker is false;");
    		return false;
    	}

    	int nowMarine = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine);
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderMarine :: nowMarine :: " + nowMarine);
    	
//    	마린이 2마리가 생산된 상태에서 팩토리가 없다면 팩토리 먼저
    	if(nowMarine == 2 && UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Factory) == 0) {
    		return false;
    	}
    	
        if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Marine, null) > 0) {
//        	FileUtils.appendTextToFile("log.txt", "\n BuilderMarine :: quere marine > 0");
        	return false;
        }
	        
//        FileUtils.appendTextToFile("log.txt", "\n BuilderMarine :: marineCount :: " + StrategyIdea.marineCount + " & nowMarine :: " + nowMarine);
		if(nowMarine < StrategyIdea.marineCount) {
			setHighPriority(true);
       		return true;
    	}
       
    	return false;

    }
    
    @Override
    public boolean checkInitialBuild(){
    	
//    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 5) {
//    		FileUtils.appendTextToFile("log.txt", "\n checkInitialBuild of Terran_Marine ==>>> override true");
//    	}
    	
		return true;
    }
}
