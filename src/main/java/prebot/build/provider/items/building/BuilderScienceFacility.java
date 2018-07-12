package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.RespondToStrategy;

public class BuilderScienceFacility extends DefaultBuildableItem {

    public BuilderScienceFacility(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("ScienceFacility build condition check");
    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
    		return false;
    	}
        
//		활성화된 커맨드가 2개 이상일 경우
		int myCommand = 0;
		
		List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		for(Unit unit : CommandCenter) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
				myCommand++;
			}
		}
        
        if ((RespondToStrategy.Instance().need_vessel == true && myCommand >= 2) || myCommand >= 3) {
			
			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Science_Facility)) {
				return true;
			}
		}
        
        
        if ((RespondToStrategy.Instance().need_vessel == true && BuildConditionChecker.Instance().CC >= 2) || BuildConditionChecker.Instance().CC >= 3) {
			
			if (BuildConditionChecker.Instance().science == false && BuildConditionChecker.Instance().starComplete) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Facility, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, false);
					return true;
				}
			}
			
		}
        return false;
    }


}
