package prebot.build.provider.items.building;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.RespondToStrategy;

public class BuilderComsatStation extends DefaultBuildableItem {

    public BuilderComsatStation(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        //System.out.println("ComsatStation build condition check");
        
        
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {
	    	if (RespondToStrategy.Instance().enemy_dark_templar 
	    		|| RespondToStrategy.Instance().enemy_wraith 
	    		|| RespondToStrategy.Instance().enemy_lurker 
	    		|| RespondToStrategy.Instance().enemy_arbiter 
	    		|| RespondToStrategy.Instance().prepareDark && (UnitUtils.myUnitDiscovered(UnitType.Terran_Academy))) {
				if (RespondToStrategy.Instance().need_vessel_time == 0) {
					RespondToStrategy.Instance().need_vessel_time = Prebot.Broodwar.getFrameCount();
				}
	
				if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Comsat_Station) && UnitUtils.myFactoryUnitSupplyCount() >= 32) {
					if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) > 0) {
						// 아카데미가 완성되었고
						if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Comsat_Station)) {
	//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1
	//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0) {
							// 빌드큐에 컴셋이 없는데, 아카데미가 완성되었다면빌드큐에 컴셋 입력
							if (Prebot.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
									&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
	//							BuildManager.Instance().buildQueue
	//									.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
								setBlocking(true);
								setHighPriority(true);
								return true;
							}
						}
					}
				}
	    	}
	    	
	    	if (BuildConditionChecker.Instance().acaComplete) {
		    	for (Unit unit : Prebot.Broodwar.self().getUnits()) {
					if (unit == null)
						continue;
					if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() && unit.canBuildAddon()
							&& Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4) {
		
						if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
							if(BuildConditionChecker.Instance().getQueueCount(UnitType.Terran_Comsat_Station)) {
		//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null)
		//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0) {
		//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
		//						break;
								setBlocking(true);
								setHighPriority(true);
								return true;
							}
						}
					}
				}
	    	}
    	}
        
        
        return false;
    }


}
