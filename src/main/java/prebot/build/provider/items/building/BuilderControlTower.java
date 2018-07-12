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

public class BuilderControlTower extends DefaultBuildableItem {

    public BuilderControlTower(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        //System.out.println("ControlTower build condition check");
//        int cc = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center);
    	
    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
    		return false;
    	}
    	
    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) != 0) {
    		return false;
    	}
    	
    	int CC = 0;
    	
    	List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
    	
    	List<Unit> StarPort = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Starport);
    	for (Unit unit : CommandCenter) {
	    	if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
				CC++;
			}
    	}
        
        if ((RespondToStrategy.Instance().need_vessel == true && CC >= 2) || CC >= 3) {
        	for (Unit unit : StarPort) {
				if (unit.canBuildAddon() && Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
	//						if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Control_Tower)) {
					
						//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower, true);
					setBlocking(true);
					setHighPriority(true);
					return true;
				}
        	}
		}
        
        
        if ((RespondToStrategy.Instance().need_vessel == true && BuildConditionChecker.Instance().CC >= 2) || BuildConditionChecker.Instance().CC >= 3) {
			if (BuildConditionChecker.Instance().starComplete) {
				// 컨트롤 타워가 없다면
				if (BuildConditionChecker.Instance().starportUnit != null && BuildConditionChecker.Instance().starportUnit.canBuildAddon()) {
					if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower, null)
								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower, true);
							return true;
						}
					}
				}
			}
		}
        
        return false;
    }


}
