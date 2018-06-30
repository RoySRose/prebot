package prebot.build.provider.items.unit;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;

public class BuilderSCV extends DefaultBuildableItem {

    public BuilderSCV(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {
    	
	    	if(!InitialBuildProvider.Instance().InitialBuildFinished) {
	    		return false;
	    	}
	    	
	    	if (Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed() < 2) {
				return false;
			}
	
	    	
	    	if (!BuildConditionChecker.Instance().EXOK) {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
					Unit checkCC = null;
					for (Unit unit : Prebot.Broodwar.self().getUnits()) {
	
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
							return false;
						}
					}
				}
			}
	
			int tot_mineral_self = 0;
			for (Unit unit : Prebot.Broodwar.self().getUnits()) {
				if (unit == null)
					continue;
				if (unit.getType() == UnitType.Terran_Command_Center) {
					int minerals = WorkerManager.Instance().getWorkerData().getMineralsNearDepot(unit);
					if (minerals > 0) {
						if (unit.isCompleted() == false) {
							minerals = minerals * unit.getHitPoints() / 1500;
						}
						tot_mineral_self += minerals;
					}
				}
	
			}
	
			if (Prebot.Broodwar.self().minerals() >= 50) {
				int maxworkerCount = tot_mineral_self * 2 + 8 * Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
				int workerCount = Prebot.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType()); // workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining()) {
							workerCount += unit.getTrainingQueue().size();
						}
					}
				}
	
				int nomorescv = 65;
				if (InformationManager.Instance().enemyRace == Race.Terran) {
					nomorescv = 60;
				}
				// System.out.println("maxworkerCount: " + maxworkerCount);
				if (workerCount < nomorescv && workerCount < maxworkerCount) {
					for (Unit unit : Prebot.Broodwar.self().getUnits()) {
						if (unit.getType().isResourceDepot() && unit.isCompleted() && unit.isTraining() == false) {
	
							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
							BuildOrderItem checkItem = null;
	
							if (!tempbuildQueue.isEmpty()) {
								checkItem = tempbuildQueue.getHighestPriorityItem();
								while (true) {
									if (checkItem.blocking == true) {
										break;
									}
									if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
										return false;
									}
									// if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
									// return;
									// }
									if (tempbuildQueue.canSkipCurrentItem() == true) {
										tempbuildQueue.skipCurrentItem();
									} else {
										break;
									}
									checkItem = tempbuildQueue.getItem();
								}
								if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
									return false;
								}
							}
							// System.out.println("checkItem: " + checkItem.metaType.getName());
							if (checkItem == null) {
	//							BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
								setHighPriority(true);
								return true;
							} else if (checkItem.metaType.isUnit()) {
								if (checkItem.metaType.getUnitType() == UnitType.Terran_Comsat_Station) {
									return false;
								} else if (checkItem.metaType.getUnitType() != UnitType.Terran_SCV) {
									if (workerCount < 4) {
	//									BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
										setHighPriority(true);
										return true;
									} else {
										int checkgas = checkItem.metaType.getUnitType().gasPrice() - Prebot.Broodwar.self().gas();
										if (checkgas < 0) {
											checkgas = 0;
										}
										if (Prebot.Broodwar.self().minerals() > checkItem.metaType.getUnitType().mineralPrice() + 50 - checkgas) {
	//										BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
											setHighPriority(true);
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
    	}

        return false;
    }


}
