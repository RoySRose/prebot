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
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;

public class BuilderComsatStation extends DefaultBuildableItem {

    public BuilderComsatStation(MetaType metaType){
        super(metaType);
    }
    
    public boolean EXOK = false;

    public final boolean buildCondition(){
        //System.out.println("ComsatStation build condition check");
    	
    	
    	
    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0) {
    		return false;
    	}
    	
    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) != 0) {
			return false;
		}
        
//    	executeFirstex();
    	
    	if(StrategyIdea.academyFrame < Prebot.Broodwar.getFrameCount()) {
//    		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) > 0) {
//	    		setBlocking(true);
//	    		setHighPriority(true);
//	    		return true;
//    		}
    		return false;
    	}
        
    
    	List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
    	for (Unit unit : CommandCenter) {
			if (unit.canBuildAddon()
					&& Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4) {

//						if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
				if (Prebot.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
						&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
//							if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Comsat_Station)) {
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
//						break;
						setBlocking(true);
						setHighPriority(true);
						return true;
				}
			}
		}

        
        
        return false;
    }

}
