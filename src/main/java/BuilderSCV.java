

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class BuilderSCV extends DefaultBuildableItem {

	public BuilderSCV(MetaType metaType) {
		super(metaType);
	}

	// public boolean EXOK = false;

	public final boolean buildCondition() {

		if (MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed() < 2) {
			return false;
		}
		if (MyBotModule.Broodwar.self().minerals() < 50) {
			return false;
		}

		// executeFirstex();
		// if(!InitialBuildProvider.Instance().InitialBuildFinished) {
		// return false;
		// }

		List<Unit> commandCenters = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL, UnitType.Terran_Command_Center);
		if (!StrategyIdea.EXOK) {
			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
				Unit secondCommandCenter = null;

				for (Unit commandCenter : commandCenters) {
					if (commandCenter.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX()
							&& commandCenter.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
						continue;
					}

					secondCommandCenter = commandCenter;
					break;
				}

				if (secondCommandCenter != null) {
					BaseLocation temp = InfoUtils.myFirstExpansion();
					if (secondCommandCenter.getTilePosition().getX() != temp.getTilePosition().getX()
							|| secondCommandCenter.getTilePosition().getY() != temp.getTilePosition().getY()) {
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

		int tot_mineral_self = 0;
		for (Unit commandCenter : commandCenters) {
			int minerals = WorkerManager.Instance().getWorkerData().getMineralsNearDepot(commandCenter);
			if (minerals > 0) {
				if (!commandCenter.isCompleted()) {
					minerals = minerals * commandCenter.getHitPoints() / 1500;
				}
				tot_mineral_self += minerals;
			}
		}

		int maxworkerCount = tot_mineral_self * 2 + 8 * MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
		int workerCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_SCV); // workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
		// List CommandCenter = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		for (Unit commandcenter : UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
			if (commandcenter.isTraining()) {
				workerCount += commandcenter.getTrainingQueue().size();
			}
		}

		int nomorescv = 65;
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			nomorescv = 60;
		}
		// System.out.println("maxworkerCount: " + maxworkerCount);
		if (workerCount >= nomorescv || workerCount >= maxworkerCount) {
			return false;
		}

		for (Unit commandcenter : UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
			if (commandcenter.isTraining()) {
//				return false;
				continue;
			}

			BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
			BuildOrderItem checkItem = null;

			if (!tempbuildQueue.isEmpty()) {
				checkItem = tempbuildQueue.getHighestPriorityItem();
				while (true) {
					if (checkItem.blocking) {
						break;
					}
					if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
						return false;
					}
					if (tempbuildQueue.canSkipCurrentItem()) {
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
				// BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
				setHighPriority(true);
				return true;
				
			} else if (checkItem.metaType.isUnit()) {
				if (checkItem.metaType.getUnitType() == UnitType.Terran_Comsat_Station) {
					return false;
					
				} else if (checkItem.metaType.getUnitType() != UnitType.Terran_SCV) {
					if (workerCount < 4) {
						// BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
						setHighPriority(true);
						return true;
					} else {
						int checkgas = checkItem.metaType.getUnitType().gasPrice() - MyBotModule.Broodwar.self().gas();
						if (checkgas < 0) {
							checkgas = 0;
						}
						if (MyBotModule.Broodwar.self().minerals() > checkItem.metaType.getUnitType().mineralPrice() + 50 - checkgas) {
							// BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
							setHighPriority(true);
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean checkInitialBuild() {
		return TimeUtils.afterTime(1, 40);
	}

}
