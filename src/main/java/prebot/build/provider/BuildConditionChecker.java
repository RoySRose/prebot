package prebot.build.provider;

import java.util.List;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.common.MapGrid;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.FileUtils;
//import prebot.common.util.*;
import prebot.common.util.InfoUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyManager;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuildConditionChecker {
	
	private static BuildConditionChecker instance = new BuildConditionChecker();
    /// static singleton 객체를 리턴합니다
    public static BuildConditionChecker Instance() {
        return instance;
    }
    
    public int vultureratio = 0;
	public int tankratio = 0;
	public int goliathratio = 0;
	public int wgt = 1;
	private int InitFaccnt = 0;
	public boolean EXOK = false;
	public boolean LiftChecker = false;

	public int WraithTime = 0;
	public int nomorewraithcnt = 0;

	public boolean aca = false;
	public boolean acaComplete = false;
	public boolean engineering = false;
	public boolean star = false;
	public boolean starComplete = false;
	public boolean science = false;
	public boolean scienceComplete = false;
	public boolean physiclab = false;
	public boolean armory = false;
	public boolean vessel = false;
	public int barrackcnt = 0;
	public int marinecnt = 0;
	public int vulturecnt = 0;
	public int wraithcnt = 0;
	// int valkyriecnt = 0;
	// int battlecnt =0;
	public int engineeringcnt = 0;
	public Unit starportUnit = null;
	public Unit barrackUnit = null;
	public Unit engineeringUnit = null;
	public boolean controltower = false;
	public int CC = 0;
	
	public boolean buildGas = false;
	public TilePosition findGeyser = null;
    
    

	public void executeFirstex() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
			Unit checkCC = null;
			BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
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

	public void executeFly() {

		// 12000프레임 이후 : 배럭, 엔지니어링 베이 띄우기
		if (Prebot.Broodwar.getFrameCount() > 12000) {
			// 완성된 factory가 2개 이상이어야 함
			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 1) {
				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
					if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {

						unit.lift();
						LiftChecker = true;
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

								if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
									tempbuildQueue.removeCurrentItem();
								}
							}
						}
					}
					if (InformationManager.Instance().enemyRace != Race.Zerg) {
						if (unit.getType() == UnitType.Terran_Marine) {
							CommandUtils.move(unit, InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
						}
					}
				}
			}
		}

		// 12000프레임 이하
		else {
			// TODO-REFACTORING 적이 테란 또는 프로토스인 경우 배럭스 들고 내리고 -> combat으로 이동
			if (InformationManager.Instance().enemyRace == Race.Terran) {

				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
					if (unit.isLifted() == true && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
						if (unit.isLifted()) {
							if (unit.canLand(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()))) {
								unit.land(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()));
								LiftChecker = false;
							} else {
								unit.land(unit.getTilePosition());
								LiftChecker = false;
							}
						}
					}
				}
				Boolean lift = false;
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) > InfoUtils.enemyNumUnits(UnitType.Terran_Vulture)) {
					lift = true;
				}
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
						+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) > 2) {

					lift = true;
				}
				if (lift) {
					for (Unit unit : Prebot.Broodwar.self().getUnits()) {
						if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
							unit.lift();
							LiftChecker = true;
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

									if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
										tempbuildQueue.removeCurrentItem();
									}
								}
							}
						}
					}
				}
			}
			if (InformationManager.Instance().enemyRace == Race.Protoss) {

				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
					if (unit.isLifted() == true && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
						if (unit.isLifted()) {
							if (unit.canLand(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()))) {
								unit.land(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()));
								LiftChecker = false;
							} else {
								unit.land(unit.getTilePosition());
								LiftChecker = false;
							}
						}
					}
				}

				int dragooncnt = 0;
				int zealotcnt = 0;

				// if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1
				// && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 1) {

				bwapi.Position checker = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
				List<Unit> eniemies = MapGrid.Instance().getUnitsNear(checker, 500, false, true, null);

				Boolean lift = false;
				for (Unit enemy : eniemies) {

					if (enemy.getType() == UnitType.Protoss_Dragoon) {
						dragooncnt++;
					}
					if (enemy.getType() == UnitType.Protoss_Zealot) {
						zealotcnt++;
					}
				}

				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
						+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 3 && dragooncnt + zealotcnt == 0) {
					lift = true;
				}

				EnemyStrategyException currentStrategyException = StrategyManager.Instance().currentStrategyException;

				if (currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
						&& currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
						&& currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT
						&& currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
					lift = true;
				}

				if (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
						|| currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH) {
					lift = false;
				}
				if (zealotcnt + dragooncnt > 0) {
					lift = false;
				}

				if (zealotcnt + dragooncnt > 8) {
					if ((currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
							|| currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
							|| currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
							|| currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT) && dragooncnt + zealotcnt > 0
							&& InfoUtils.myNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Tank_Mode) >= dragooncnt
							&& InfoUtils.myNumUnits(UnitType.Terran_Vulture) > zealotcnt) {
						lift = true;
					}
				}
				if (dragooncnt + zealotcnt == 0) {
					lift = true;
				}

				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
					lift = true;
				}
				if (lift) {
					for (Unit unit : Prebot.Broodwar.self().getUnits()) {
						if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
							unit.lift();
							LiftChecker = true;
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

									if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
										tempbuildQueue.removeCurrentItem();
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void executeSustainUnits() {
		
		aca = false;
		acaComplete = false;
		engineering = false;
		star = false;
		starComplete = false;
		science = false;
		scienceComplete = false;
		physiclab = false;
		armory = false;
		vessel = false;
		barrackcnt = 0;
		marinecnt = 0;
		vulturecnt = 0;
		wraithcnt = 0;
		// int valkyriecnt = 0;
		// int battlecnt =0;
		engineeringcnt = 0;
		starportUnit = null;
		barrackUnit = null;
		engineeringUnit = null;
		controltower = false;
		CC = 0;
		buildGas = false;
		findGeyser = null;
		
		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
			if (unit == null)
				continue;
	
			// 컴샛 start
			if (unit.getType() == UnitType.Terran_Academy) {
				aca = true;
				if (unit.isCompleted()) {
					acaComplete = true;
				}
			}
			// 컴샛 end
	
			// engineering start
			if (unit.getType() == UnitType.Terran_Armory) {
				armory = true;
			}
			// engineering end
	
			// scienceVessel start
			if (unit.getType() == UnitType.Terran_Starport) {
				star = true;
				if (unit.isCompleted()) {
					starportUnit = unit;
					starComplete = true;
				}
			}
			if (unit.getType() == UnitType.Terran_Control_Tower && unit.isCompleted()) {
				controltower = true;
			}
			if (unit.getType() == UnitType.Terran_Science_Facility) {
				science = true;
				if (unit.isCompleted()) {
					scienceComplete = true;
				}
			}
			if (unit.getType() == UnitType.Terran_Science_Vessel) {
				vessel = true;
			}
			// scienceVessel end
	
			// battle start
			// if(unit.getType() == UnitType.Terran_Physics_Lab && unit.isCompleted()){
			// physiclab = true;
			// }
			// if(unit.getType() == UnitType.Terran_Battlecruiser && unit.isCompleted()){
			// battlecnt ++;
			// }
			// battle end
	
			// marine for fast zergling and zealot start
			if (unit.getType() == UnitType.Terran_Marine && unit.isCompleted()) {
				marinecnt++;
			}
	
			if (unit.getType() == UnitType.Terran_Vulture && unit.isCompleted()) {
				vulturecnt++;
			}
			// marine for fast zergling and zealot end
	
			// wraith for TvT start
			if (unit.getType() == UnitType.Terran_Wraith && unit.isCompleted()) {
				wraithcnt++;
			}
			// wraith for TvT end
	
			// barrack start
			if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
				barrackcnt++;
				barrackUnit = unit;
			}
			// barrack end
			if (unit.getType() == UnitType.Terran_Engineering_Bay) {
	
				engineering = true;
				if (unit.isCompleted()) {
					engineeringUnit = unit;
					engineeringcnt++;
				}
			}
	
			// 가스 start
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {
	
				Unit geyserAround = null;
	
				boolean refineryAlreadyBuilt = false;
				for (Unit unitsAround : Prebot.Broodwar.getUnitsInRadius(unit.getPosition(), 300)) {
					if (unitsAround == null)
						continue;
					if (unitsAround.getType() == UnitType.Terran_Refinery || unitsAround.getType() == UnitType.Zerg_Extractor
							|| unitsAround.getType() == UnitType.Protoss_Assimilator) {
						refineryAlreadyBuilt = true;
						break;
					}
					if (unitsAround.getType() == UnitType.Resource_Vespene_Geyser) {
						geyserAround = unitsAround;
					}
				}
	
				if (InitialBuildProvider.Instance().InitialBuildFinished == false && geyserAround != null) {
					if (InformationManager.Instance().getMyfirstGas() != null) {
						if (geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())) {
							continue;
						}
					}
				}
	
				if (refineryAlreadyBuilt == false) {
					findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
					if (findGeyser != null) {
						if (findGeyser.getDistance(unit.getTilePosition()) * 32 > 300) {
							continue;
						}
						if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 9) {
							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
								//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
								buildGas = true;
							}
						}
					}
				}
	
				if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
					CC++;
				}
			}
			// 가스 end
		}
	
		// 컴샛 start
//		if (aca == false) {
//			if (CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
//				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy, false);
//				}
//			}
//		}
		if (acaComplete) {
			if (EXOK == false) {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
					Unit checkCC = null;
					for (Unit checkunit : Prebot.Broodwar.self().getUnits()) {
	
						if (checkunit.getType() != UnitType.Terran_Command_Center) {
							continue;
						}
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
							return;
						}
					}
				}
			}
//			for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//				if (unit == null)
//					continue;
//				if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() && unit.canBuildAddon()
//						&& Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4) {
//	
//					if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
//						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null)
//								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
//							break;
//						}
//					}
//				}
//			}
		}
		// 컴샛 end
	
		// barrack start
//		if (InitialBuildProvider.Instance().InitialBuildFinished == true && (barrackcnt == 0 || (barrackcnt == 1 && barrackUnit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints() / 3))) {
//			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) == 0
//					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, false);
//			}
//		}
		// barrack end
	
		// engineering start
//		if (InitialBuildProvider.Instance().InitialBuildFinished == true
//				&& (engineeringcnt == 0 || (engineeringcnt == 1 && engineeringUnit.getHitPoints() < UnitType.Terran_Engineering_Bay.maxHitPoints() / 3))
//				&& Prebot.Broodwar.getFrameCount() > 11000) {
//			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0
//					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
//			}
//		}
//		// engineering end
//	
//		// engineering start1
//		if (engineering == false) {
//			if (CC >= 2 || Prebot.Broodwar.getFrameCount() > 17000) {
//				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) == 0
//						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
//				}
//			}
//		}
		// engineering end2
	
		// scienceVessel start
//		if ((RespondToStrategy.Instance().need_vessel == true && CC >= 2) || CC >= 3) {
//			if (star == false) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
//				}
//			}
//			if (starComplete) {
//				// 컨트롤 타워가 없다면
//				if (starportUnit != null && starportUnit.canBuildAddon()) {
//					if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
//						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower, null)
//								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower, true);
//						}
//					}
//				}
//			}
//			if (science == false && starComplete) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Facility, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, false);
//				}
//			}
//			if (starComplete && scienceComplete && controltower) {
//				if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Science_Vessel) < RespondToStrategy.Instance().max_vessel) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel, null)
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Vessel, null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel, false);
//					}
//				}
//			}
//		}
		// scienceVessel end
	
		// armory start1
//		if (armory == false) {
//			if (CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
//				}
//			}
//		}
		// armory end2
	
		// battlecruiser start
		// System.out.println("need_battlecruiser: " + RespondToStrategy.Instance().need_battlecruiser);
		// System.out.println("CC: " + CC);
		// System.out.println("max_battlecruiser: " + RespondToStrategy.Instance().max_battlecruiser);
		// System.out.println("battlecnt: " + battlecnt);
	
		// if(RespondToStrategy.Instance().need_battlecruiser == true && CC>=3 && RespondToStrategy.Instance().max_battlecruiser > battlecnt){
		//
		// nomorewraithcnt = 100;
		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Starport) < 4){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) < 4) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make starport for battle");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
		// }
		// }
		// if(starComplete){
		// //컨트롤 타워가 없다면
		// if(starportUnit != null && starportUnit.getAddon() == null){
		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Control_Tower) < 4){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0
		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) < 4) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make starport for battle");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower, false);
		// }
		// }
		// }
		// }
		// if(science == false && starComplete){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0
		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Facility, null) == 0) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make scienceFacil since we have many CC");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, false);
		// }
		// }
		// if(scienceComplete){
		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Physics_Lab) < 1){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Physics_Lab, null)
		// + ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Physics_Lab, null) == 0){
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Physics_Lab, false);
		// }
		// }else if(physiclab){
		// if(MyBotModule.Broodwar.self().hasResearched(TechType.Yamato_Gun) == false && MyBotModule.Broodwar.self().isResearching(TechType.Yamato_Gun) ==false){
		// if(BuildManager.Instance().buildQueue.getItemCount(TechType.Yamato_Gun) == 0){
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Yamato_Gun, false);
		// }
		// }
		//
		// }
		// }
		//
		// if(scienceComplete && physiclab){
		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Battlecruiser) < RespondToStrategy.Instance().max_battlecruiser){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Battlecruiser, null) == 0) {
		// BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Battlecruiser, false);
		// }
		// }
		// }
		// }
		// battlecruiser end
	
		// wraith for TvT start
//		if (RespondToStrategy.Instance().max_wraith > wraithcnt && nomorewraithcnt <= RespondToStrategy.Instance().max_wraith) {
//	
//			if (CC >= 2) {
//				if (star == false) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
//					}
//				}
//			}
//			if (starComplete) {
//				if (starportUnit.isTraining() == false && (Prebot.Broodwar.getFrameCount() - WraithTime > 2400 || wraithcnt < 1)) { // TODO && wraithcnt <= needwraith){
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Wraith, null) == 0) {
//						WraithTime = Prebot.Broodwar.getFrameCount();
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Wraith, false);
//						nomorewraithcnt++;
//					}
//				}
//			}
//		}
		// wraith for TvT end
	
		// //valkyrie for TvT start
		// if(RespondToStrategy.Instance().max_valkyrie > valkyriecnt && CC >2){
		// if(star == false){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make starport for valkyrie");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
		// }
		// }
		// if(armory == false){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make armory for valkyrie");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
		// }
		// }
		// if(starComplete){
		// //컨트롤 타워가 없다면
		// if(starportUnit != null && starportUnit.getAddon() == null){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0
		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make Terran_Control_Tower for valkyrie");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower, true);
		// }
		// }
		// }
		// if(starComplete && starportUnit.getAddon() != null && starportUnit.getAddon().isCompleted() == true){
		// if(starportUnit.isTraining() == false){// && valkyriecnt < needwraithvalkyriecnt){ //TODO
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Valkyrie, null) == 0) {
		// if(Config.BroodwarDebugYN){
		// MyBotModule.Broodwar.printf("make valkyrie");
		// }
		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Valkyrie, false);
		// }
		// }
		// }
		// }
		// valkyrie for TvT end
	
	}


}
