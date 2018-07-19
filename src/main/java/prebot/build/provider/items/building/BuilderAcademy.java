package prebot.build.provider.items.building;

import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;

public class BuilderAcademy extends DefaultBuildableItem {

    public BuilderAcademy(MetaType metaType){
        super(metaType);
    }
    
//    public boolean EXOK = false;

    public final boolean buildCondition(){
        //System.out.println("Academy build condition check");
    	if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Academy) > 0) {
    		return false;
    	}
    	
    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) +
				 ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) != 0) {
    		return false;
    	}
    	
    	if(StrategyIdea.academyFrame < Prebot.Broodwar.getFrameCount()) {
    		setBlocking(true);
    		setHighPriority(true);
    		return true;
    	}

    		
//		15000 프레임 이후
		if (Prebot.Broodwar.getFrameCount() > 15000) {
			return true;
		}
		
		
//		활성화된 커맨드가 2개 이상일 경우
		int myCommand = 0;
		
		List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		for(Unit unit : CommandCenter) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
				myCommand++;
			}
		}
		
    	if (myCommand >= 2) {
    		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0) {
				return true;
			}
		}

        return false;
    }

}
