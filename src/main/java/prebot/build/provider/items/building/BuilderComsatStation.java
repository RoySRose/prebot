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
        
    	executeFirstex();
        
    	if (RespondToStrategy.Instance().enemy_dark_templar 
    		|| RespondToStrategy.Instance().enemy_wraith 
    		|| RespondToStrategy.Instance().enemy_lurker 
    		|| RespondToStrategy.Instance().enemy_arbiter 
    		|| RespondToStrategy.Instance().prepareDark && (UnitUtils.myUnitDiscovered(UnitType.Terran_Academy))) {
			if (RespondToStrategy.Instance().need_vessel_time == 0) {
				RespondToStrategy.Instance().need_vessel_time = Prebot.Broodwar.getFrameCount();
			}

			if (UnitUtils.myFactoryUnitSupplyCount() >= 32) {
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
    	
    	List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);

    	
    	
		if (EXOK == false) {
			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
				Unit checkCC = null;
				for (Unit checkunit : CommandCenter) {

//					if (checkunit.getType() != UnitType.Terran_Command_Center) {
//						continue;
//					}
					if (checkunit.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX()
							&& checkunit.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
						continue;
					} else {
						checkCC = checkunit;
						break;
					}
				}

				if (checkCC != null) {
					BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
					if (checkCC.getTilePosition().getX() == temp.getTilePosition().getX() && checkCC.getTilePosition().getY() == temp.getTilePosition().getY()) {

					} else {
						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
						BuildOrderItem checkItem = null;

						if (!tempbuildQueue.isEmpty()) {
							checkItem = tempbuildQueue.getHighestPriorityItem();
							while (true) {
								if (tempbuildQueue.canGetNextItem() == true) {
									tempbuildQueue.canGetNextItem();
								} else {
									break;
								}
								tempbuildQueue.PointToNextItem();
								checkItem = tempbuildQueue.getItem();

								if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
									tempbuildQueue.removeCurrentItem();
								}
							}
						}
					}
				}
			}
		}
		
		
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
    
    public void executeFirstex() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
			Unit checkCC = null;
			BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
			
			List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
			for (Unit unit : CommandCenter) {

				if (unit.getType() != UnitType.Terran_Command_Center) {
					continue;
				}
				if (unit.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX() && unit.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
					continue;
				} else {
					checkCC = unit;
					break;
				}
			}
			if (checkCC != null) {
				if (checkCC.isLifted() == false) {
					if (checkCC.getTilePosition().getX() != temp.getTilePosition().getX() || checkCC.getTilePosition().getY() != temp.getTilePosition().getY()) {
						checkCC.lift();
					}
				} else {
					checkCC.land(new TilePosition(temp.getTilePosition().getX(), temp.getTilePosition().getY()));
				}
				if (checkCC.isLifted() == false && checkCC.getTilePosition().getX() == temp.getTilePosition().getX()
						&& checkCC.getTilePosition().getY() == temp.getTilePosition().getY()) {
					EXOK = true;
				}
			}
		}
	}


}
