package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;

public class BuilderBarracks extends DefaultBuildableItem {

    public BuilderBarracks(MetaType metaType){
        super(metaType);
    }
    

    public final boolean buildCondition(){
    	
    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) +
			 ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) != 0) {
			return false;
		}
    	
		
		if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks) == 0) {
			
			return true;

//			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) +
//				 ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
//				return true;
//			}
		}else if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks) == 1) {
			List<Unit> BarracksList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Barracks);
			
			for(Unit unit : BarracksList) {
				if(unit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints() / 3) {
					return true;
				}
			}
		}
//	        //System.out.println("barracks build condition check");
//	//    	배럭이 2개만들어질 경우가 있는지 확인은 필요. 현재 조건 1개를 전제로 함
//	    	if (InitialBuildProvider.Instance().InitialBuildFinished) {
//	//    		FileUtils.appendTextToFile("log.txt", "\n BuilderBarracks || InitialBuildFinished end");
//	    		if(BuildConditionChecker.Instance().barrackcnt == 0) {
//	    			FileUtils.appendTextToFile("log.txt", "\n BuilderBarracks || barrackcnt ==> " + BuildConditionChecker.Instance().barrackcnt);
//	    			
//	
//		    		if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Barracks)) {
//	//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) == 0
//	//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
//	//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, false);
//						return true;
//					}
//	    		}else if(BuildConditionChecker.Instance().barrackcnt == 1 && BuildConditionChecker.Instance().barrackUnit != null) {
//	    			
//	    			FileUtils.appendTextToFile("log.txt", "\n BuilderBarracks || barrack getHitPoints ==> " + BuildConditionChecker.Instance().barrackUnit.getHitPoints());
//	    			if(BuildConditionChecker.Instance().barrackUnit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints() / 3) {
//	    				
//		    			if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Barracks)) {
//		    				
//		    				FileUtils.appendTextToFile("log.txt", "\n BuilderBarracks || barrack fire!!!!!! and not in quere");
//	//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) == 0
//	//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
//	//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, false);
//							return true;
//						}
//	    			}
//	    		}
//			}

    	return false;
    }


}
