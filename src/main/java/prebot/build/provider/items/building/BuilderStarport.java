package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.RespondToStrategy;

public class BuilderStarport extends DefaultBuildableItem {

    public BuilderStarport(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("Starport build condition check");
        int wraithcnt = 0;
    	int nomorewraithcnt = 0;
    	
		int myCommand = 0;
    	
    	List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		for(Unit unit : CommandCenter) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
				myCommand++;
			}
		}
        if (RespondToStrategy.Instance().max_wraith > wraithcnt && nomorewraithcnt <= RespondToStrategy.Instance().max_wraith) {

			if (myCommand >= 2) {
				if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Starport)) {
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
						return true;
					}
				}
			}
		}
        
        if ((RespondToStrategy.Instance().need_vessel == true && myCommand >= 2) || myCommand >= 3) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
					return true;
				}
		}

        
        
        return false;
    }


}
