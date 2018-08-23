

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class TempBuildSourceCode {

//	public int vultureratio = 0;
//	public int tankratio = 0;
//	public int goliathratio = 0;
//	public int wgt = 1;
//	private int InitFaccnt = 0;
//	public boolean EXOK = false;
//	public boolean LiftChecker = false;
//
//	public int WraithTime = 0;
//	public int nomorewraithcnt = 0;
//
//	private static TempBuildSourceCode instance = new TempBuildSourceCode();
//
//	public TempBuildSourceCode() {
////		InitialBuild.Instance().setInitialBuildOrder();
//		InitFaccnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory);
//		setCombatUnitRatio();
//	}
//
//	/// static singleton 객체를 리턴합니다
//	public static TempBuildSourceCode Instance() {
//		return instance;
//	}
//
//	private boolean isInitialBuildOrderFinished;
//
//	public boolean isInitialBuildOrderFinished() {
//		return isInitialBuildOrderFinished;
//	}
//
//	public void update() {
//
//		// TODO-REFACTORING 멀티지역 커맨드센터 건설
//		// STRATEGY MANAGER : 멀티지역이 안전하지 않을 경우를 판단한다.
//		// BUILD MANAGER : 다른 위치에 커맨드를 짓는다.(하드코딩 또는 FIRST CHOKE 뒤쪽 등 처리방법 고민필요)
//		// COMBAT MANAGER : STRATEGY MANAGER에서 멀티지역이 안전해졌다고 판단이 들어오면 커맨드 센터를 띄워서 이동시킨다.
//		if (EXOK == false && Prebot.Broodwar.getFrameCount() % 2 == 0) {
//			//executeFirstex();
//		}
//
//		if (Prebot.Broodwar.getFrameCount() % 43 == 0) {
//			executeResearch();
//		}
//
//		// initial이 끝났는지에 대한 체크
//		if (!isInitialBuildOrderFinished && BuildManager.Instance().buildQueue.isEmpty()) {
//			isInitialBuildOrderFinished = true;
//		}
//
//		// initial이 끝났는지에 대한 체크
//		if (!isInitialBuildOrderFinished && BuildManager.Instance().buildQueue.isEmpty()) {
//			isInitialBuildOrderFinished = true;
//		}
//
//		// 서플라이 디폿 추가 로직
//		// TODO-REFACTORING
//		// frame으로 처리되어 있는 이유. initial이 끝난 후로 하면 되지 않나?
//		if (Prebot.Broodwar.getFrameCount() % 29 == 0 && Prebot.Broodwar.getFrameCount() > 4500) {
//			executeSupplyManagement();
//		}
//
//		// 확장, 메카닉 업그레이드
//		// TODO-REFACTORING 서플라이 management가 frame으로 처리되어 있는 이유 확인
//		// 별 다른 이유가 없다면 initial이 끝난 후로 하면 될듯?
//
//		// 업그레이드
//		if (isInitialBuildOrderFinished == true) {
//			if (Prebot.Broodwar.getFrameCount() % 53 == 0) {
//				executeUpgrade();
//			}
//		}
//		if (isInitialBuildOrderFinished == false) {
//			// INITIAL의 가스기지, 팩토리 증설 로직(특수상황)
//			if (Prebot.Broodwar.getFrameCount() % 23 == 0) {
//				executeAddBuildingInit();
//			}
//		} else if (Prebot.Broodwar.getFrameCount() % 113 == 0) { // 5초에 한번 팩토리 추가 여부 결정
//			// INITIAL 종료 후 팩토리 증설, 머신샵 증설
//			executeAddFactory();
//		}
//
//		// 일꾼 생산, 팩토리 유닛
//		// TODO-REFACTORING frame으로 처리되어 있는 이유. initial이 끝난 후로 하면 되지 않나?
//		if (Prebot.Broodwar.getFrameCount() % 5 == 0 && Prebot.Broodwar.getFrameCount() > 2500) {
//			executeWorkerTraining();
//			executeCombatUnitTrainingBlocked();
//
//			if (isInitialBuildOrderFinished == true) {
//				executeCombatUnitTraining();
//			}
//		}
//		// TODO-REFACTORING
//		if (Prebot.Broodwar.getFrameCount() % 239 == 0) {
//			executeSustainUnits();
//		}
//
//
//		if ((Prebot.Broodwar.getFrameCount() < 13000 && Prebot.Broodwar.getFrameCount() % 5 == 0)
//				|| (Prebot.Broodwar.getFrameCount() >= 13000 && Prebot.Broodwar.getFrameCount() % 23 == 0)) { // Analyze 와 동일하게
//			RespondToStrategy.Instance().update();
//			// RespondToStrategyOld.Instance().update();// 다른 유닛 생성에 비해 제일 마지막에 돌아야 한다. highqueue 이용하면 제일 앞에 있을 것이므로
//			// AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
//		}
//	}
//
//	// 일꾼 계속 추가 생산
//	private void executeWorkerTraining() {
//
//		if (Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed() < 2) {
//			return;
//		}
//
//		if (EXOK == false) {
//			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
//				Unit checkCC = null;
//				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//
//					if (unit.getType() != UnitType.Terran_Command_Center) {
//						continue;
//					}
//					if (unit.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX() && unit.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
//						continue;
//					} else {
//						checkCC = unit;
//						break;
//					}
//				}
//
//				if (checkCC != null) {
//					BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//					if (checkCC.getTilePosition().getX() == temp.getTilePosition().getX() && checkCC.getTilePosition().getY() == temp.getTilePosition().getY()) {
//
//					} else {
//						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//						BuildOrderItem checkItem = null;
//
//						if (!tempbuildQueue.isEmpty()) {
//							checkItem = tempbuildQueue.getHighestPriorityItem();
//							while (true) {
//								if (tempbuildQueue.canGetNextItem() == true) {
//									tempbuildQueue.canGetNextItem();
//								} else {
//									break;
//								}
//								tempbuildQueue.PointToNextItem();
//								checkItem = tempbuildQueue.getItem();
//
//								if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
//									tempbuildQueue.removeCurrentItem();
//								}
//							}
//						}
//						return;
//					}
//				}
//			}
//		}
//
//		int tot_mineral_self = 0;
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//			if (unit.getType() == UnitType.Terran_Command_Center) {
//				int minerals = WorkerManager.Instance().getWorkerData().getMineralsNearDepot(unit);
//				if (minerals > 0) {
//					if (unit.isCompleted() == false) {
//						minerals = minerals * unit.getHitPoints() / 1500;
//					}
//					tot_mineral_self += minerals;
//				}
//			}
//
//		}
//
//		if (Prebot.Broodwar.self().minerals() >= 50) {
//			int maxworkerCount = tot_mineral_self * 2 + 8 * Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
//			int workerCount = Prebot.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType()); // workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
//			for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//				if (unit.getType().isResourceDepot()) {
//					if (unit.isTraining()) {
//						workerCount += unit.getTrainingQueue().size();
//					}
//				}
//			}
//
//			int nomorescv = 65;
//			if (InformationManager.Instance().enemyRace == Race.Terran) {
//				nomorescv = 60;
//			}
//			if (workerCount < nomorescv && workerCount < maxworkerCount) {
//				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//					if (unit.getType().isResourceDepot() && unit.isCompleted() && unit.isTraining() == false) {
//
//						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//						BuildOrderItem checkItem = null;
//
//						if (!tempbuildQueue.isEmpty()) {
//							checkItem = tempbuildQueue.getHighestPriorityItem();
//							while (true) {
//								if (checkItem.blocking == true) {
//									break;
//								}
//								if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
//									return;
//								}
//								// if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
//								// return;
//								// }
//								if (tempbuildQueue.canSkipCurrentItem() == true) {
//									tempbuildQueue.skipCurrentItem();
//								} else {
//									break;
//								}
//								checkItem = tempbuildQueue.getItem();
//							}
//							if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
//								return;
//							}
//						}
//						if (checkItem == null) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
//						} else if (checkItem.metaType.isUnit()) {
//							if (checkItem.metaType.getUnitType() == UnitType.Terran_Comsat_Station) {
//								return;
//							} else if (checkItem.metaType.getUnitType() != UnitType.Terran_SCV) {
//								if (workerCount < 4) {
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
//								} else {
//									int checkgas = checkItem.metaType.getUnitType().gasPrice() - Prebot.Broodwar.self().gas();
//									if (checkgas < 0) {
//										checkgas = 0;
//									}
//									if (Prebot.Broodwar.self().minerals() > checkItem.metaType.getUnitType().mineralPrice() + 50 - checkgas) {
//										BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//	// 부족한 인구수 충원
//	private void executeSupplyManagement() {
//
//		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//		BuildOrderItem checkItem = null;
//
//		if (!tempbuildQueue.isEmpty()) {
//			checkItem = tempbuildQueue.getHighestPriorityItem();
//			while (true) {
//				if (checkItem.blocking == true) {
//					break;
//				}
//				// if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
//				// return;
//				// }
//				if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret) {
//					return;
//				}
//				if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
//					return;
//				}
//				if (tempbuildQueue.canSkipCurrentItem() == true) {
//					tempbuildQueue.skipCurrentItem();
//				} else {
//					break;
//				}
//				checkItem = tempbuildQueue.getItem();
//			}
//			if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
//				return;
//			}
//		}
//
//		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
//		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
//		if (Prebot.Broodwar.self().supplyTotal() < 400) {
//
//			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
//			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
//			int supplyMargin = 4;
//			boolean barrackflag = false;
//			boolean factoryflag = false;
//
//			if (factoryflag == false) {
//				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//					if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()) {
//						factoryflag = true;
//					}
//					if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
//						barrackflag = true;
//					}
//				}
//			}
//
//			int Faccnt = 0;
//			int CCcnt = 0;
//			int facFullOperating = 0;
//			for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//				if (unit == null)
//					continue;
//				if (unit.getType().isResourceDepot() && unit.isCompleted()) {
//					CCcnt++;
//				}
//				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()) {
//					Faccnt++;
//					if (unit.isTraining() == true) {
//						facFullOperating++;
//					}
//				}
//			}
//
//			if (CCcnt == 1) {// TODO 이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨
//				if (factoryflag == false && barrackflag == true) {
//					supplyMargin = 5;
//				} else if (factoryflag == true) {
//					supplyMargin = 6 + 4 * Faccnt + facFullOperating * 2;
//				}
//			} else { // if((MyBotModule.Broodwar.getFrameCount()>=6000 && MyBotModule.Broodwar.getFrameCount()<10000) || (Faccnt > 3 && CCcnt == 2)){
//				supplyMargin = 11 + 4 * Faccnt + facFullOperating * 2;
//			}
//
//			// currentSupplyShortage 를 계산한다
//			int currentSupplyShortage = Prebot.Broodwar.self().supplyUsed() + supplyMargin + 1 - Prebot.Broodwar.self().supplyTotal();
//
//			if (currentSupplyShortage > 0) {
//				// 생산/건설 중인 Supply를 센다
//				int onBuildingSupplyCount = 0;
//				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
//				onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
//						* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
//
//				if (currentSupplyShortage > onBuildingSupplyCount) {
//
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextSupplePoint, false);
//					//
//					//// boolean isToEnqueue = true;
//					//// if (!BuildManager.Instance().buildQueue.isEmpty()) {
//					//// BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
//					//// if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType())
//					//// {
//					//// isToEnqueue = false;
//					//// }
//					//// }
//					// if(InformationManager.Instance().getMainBaseSuppleLimit() <= MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Supply_Depot)){
//					//// MyBotModule.Broodwar.printf("currentSupplyShortage: " + currentSupplyShortage);
//					//// MyBotModule.Broodwar.printf("onBuildingSupplyCount: " + onBuildingSupplyCount);
//					//// MyBotModule.Broodwar.printf("supplyMargin: " + supplyMargin);
//					//// MyBotModule.Broodwar.printf("supplyTotal: " + MyBotModule.Broodwar.self().supplyTotal());
//					//
//					// }else{
//					//// MyBotModule.Broodwar.printf("currentSupplyShortage: " + currentSupplyShortage);
//					//// MyBotModule.Broodwar.printf("onBuildingSupplyCount: " + onBuildingSupplyCount);
//					//// MyBotModule.Broodwar.printf("supplyMargin: " + supplyMargin);
//					//// MyBotModule.Broodwar.printf("supplyTotal: " + MyBotModule.Broodwar.self().supplyTotal());
//					// BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Supply_Depot,BuildOrderItem.SeedPositionStrategy.NextSupplePoint, true);
//					// }
//				}
//			}
//		}
//	}
//
//	private void executeAddBuildingInit() {
//		// 팩토리 수가 모자르면 팩토리 추가(INITIAL에서 동작하는 로직)
//		// TODO 빌드큐 + 전체FACTORY + 컨스트럭션큐 < INIT_FAC
//		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory)
//				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) < InitFaccnt) {
//			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//		}
//		// 가스가 없으면 가스기지를 추가(INITIAL에서 동작. 가스러시 대비)
//		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery, null) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Refinery)
//				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Refinery, null) == 0) {
//			if (InformationManager.Instance().isGasRushed() == false) {
//				int barrack = 0;
//				int supple = 0;
//				int scv = 0;
//				for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//					if (unit == null)
//						continue;
//					if (unit.getType() == UnitType.Terran_Barracks) {
//						barrack++;
//					}
//					if (unit.getType() == UnitType.Terran_Supply_Depot && unit.isCompleted()) {
//						supple++;
//					}
//					if (unit.getType() == UnitType.Terran_SCV && unit.isCompleted()) {
//						scv++;
//					}
//				}
//				if (barrack > 0 && supple > 0 && scv > 10) {
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//				}
//			}
//		}
//	}
//
//	private void executeSustainUnits() {
//
//		boolean aca = false;
//		boolean acaComplete = false;
//		boolean engineering = false;
//		boolean star = false;
//		boolean starComplete = false;
//		boolean science = false;
//		boolean scienceComplete = false;
//		boolean physiclab = false;
//		boolean armory = false;
//		boolean vessel = false;
//		int barrackcnt = 0;
//		int marinecnt = 0;
//		int vulturecnt = 0;
//		int wraithcnt = 0;
//		// int valkyriecnt = 0;
//		// int battlecnt =0;
//		int engineeringcnt = 0;
//		Unit starportUnit = null;
//		Unit barrackUnit = null;
//		Unit engineeringUnit = null;
//		boolean controltower = false;
//		int CC = 0;
//
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//
//			// 컴샛 start
//			if (unit.getType() == UnitType.Terran_Academy) {
//				aca = true;
//				if (unit.isCompleted()) {
//					acaComplete = true;
//				}
//			}
//			// 컴샛 end
//
//			// engineering start
//			if (unit.getType() == UnitType.Terran_Armory) {
//				armory = true;
//			}
//			// engineering end
//
//			// scienceVessel start
//			if (unit.getType() == UnitType.Terran_Starport) {
//				star = true;
//				if (unit.isCompleted()) {
//					starportUnit = unit;
//					starComplete = true;
//				}
//			}
//			if (unit.getType() == UnitType.Terran_Control_Tower && unit.isCompleted()) {
//				controltower = true;
//			}
//			if (unit.getType() == UnitType.Terran_Science_Facility) {
//				science = true;
//				if (unit.isCompleted()) {
//					scienceComplete = true;
//				}
//			}
//			if (unit.getType() == UnitType.Terran_Science_Vessel) {
//				vessel = true;
//			}
//			// scienceVessel end
//
//			// battle start
//			// if(unit.getType() == UnitType.Terran_Physics_Lab && unit.isCompleted()){
//			// physiclab = true;
//			// }
//			// if(unit.getType() == UnitType.Terran_Battlecruiser && unit.isCompleted()){
//			// battlecnt ++;
//			// }
//			// battle end
//
//			// marine for fast zergling and zealot start
//			if (unit.getType() == UnitType.Terran_Marine && unit.isCompleted()) {
//				marinecnt++;
//			}
//
//			if (unit.getType() == UnitType.Terran_Vulture && unit.isCompleted()) {
//				vulturecnt++;
//			}
//			// marine for fast zergling and zealot end
//
//			// wraith for TvT start
//			if (unit.getType() == UnitType.Terran_Wraith && unit.isCompleted()) {
//				wraithcnt++;
//			}
//			// wraith for TvT end
//
//			// barrack start
//			if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
//				barrackcnt++;
//				barrackUnit = unit;
//			}
//			// barrack end
//			if (unit.getType() == UnitType.Terran_Engineering_Bay) {
//
//				engineering = true;
//				if (unit.isCompleted()) {
//					engineeringUnit = unit;
//					engineeringcnt++;
//				}
//			}
//
//			// 가스 start
//			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {
//
//				Unit geyserAround = null;
//
//				boolean refineryAlreadyBuilt = false;
//				for (Unit unitsAround : Prebot.Broodwar.getUnitsInRadius(unit.getPosition(), 300)) {
//					if (unitsAround == null)
//						continue;
//					if (unitsAround.getType() == UnitType.Terran_Refinery || unitsAround.getType() == UnitType.Zerg_Extractor
//							|| unitsAround.getType() == UnitType.Protoss_Assimilator) {
//						refineryAlreadyBuilt = true;
//						break;
//					}
//					if (unitsAround.getType() == UnitType.Resource_Vespene_Geyser) {
//						geyserAround = unitsAround;
//					}
//				}
//
//				if (isInitialBuildOrderFinished == false && geyserAround != null) {
//					if (InformationManager.Instance().getMyfirstGas() != null) {
//						if (geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())) {
//							continue;
//						}
//					}
//				}
//
//				if (refineryAlreadyBuilt == false) {
//					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
//					if (findGeyser != null) {
//						if (findGeyser.getDistance(unit.getTilePosition()) * 32 > 300) {
//							continue;
//						}
//						if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 9) {
//							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
//								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
//							}
//						}
//					}
//				}
//
//				if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
//					CC++;
//				}
//			}
//			// 가스 end
//		}
//
//		// 컴샛 start
//		if (aca == false) {
//			if (CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
//				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy, false);
//				}
//			}
//		}
//		if (acaComplete) {
//			if (EXOK == false) {
//				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
//					Unit checkCC = null;
//					for (Unit checkunit : Prebot.Broodwar.self().getUnits()) {
//
//						if (checkunit.getType() != UnitType.Terran_Command_Center) {
//							continue;
//						}
//						if (checkunit.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX()
//								&& checkunit.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
//							continue;
//						} else {
//							checkCC = checkunit;
//							break;
//						}
//					}
//
//					if (checkCC != null) {
//						BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//						if (checkCC.getTilePosition().getX() == temp.getTilePosition().getX() && checkCC.getTilePosition().getY() == temp.getTilePosition().getY()) {
//
//						} else {
//							BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//							BuildOrderItem checkItem = null;
//
//							if (!tempbuildQueue.isEmpty()) {
//								checkItem = tempbuildQueue.getHighestPriorityItem();
//								while (true) {
//									if (tempbuildQueue.canGetNextItem() == true) {
//										tempbuildQueue.canGetNextItem();
//									} else {
//										break;
//									}
//									tempbuildQueue.PointToNextItem();
//									checkItem = tempbuildQueue.getItem();
//
//									if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV) {
//										tempbuildQueue.removeCurrentItem();
//									}
//								}
//							}
//							return;
//						}
//					}
//				}
//			}
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
//		}
//		// 컴샛 end
//
//		// barrack start
//		if (isInitialBuildOrderFinished == true && (barrackcnt == 0 || (barrackcnt == 1 && barrackUnit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints() / 3))) {
//			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) == 0
//					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, false);
//			}
//		}
//		// barrack end
//
//		// engineering start
//		if (isInitialBuildOrderFinished == true
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
//		// engineering end2
//
//		// scienceVessel start
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
//		// scienceVessel end
//
//		// armory start1
//		if (armory == false) {
//			if (CC >= 2 || Prebot.Broodwar.getFrameCount() > 15000) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
//				}
//			}
//		}
//		// armory end2
//
//		// battlecruiser start
//
//		// if(RespondToStrategy.Instance().need_battlecruiser == true && CC>=3 && RespondToStrategy.Instance().max_battlecruiser > battlecnt){
//		//
//		// nomorewraithcnt = 100;
//		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Starport) < 4){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
//		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) < 4) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make starport for battle");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
//		// }
//		// }
//		// if(starComplete){
//		// //컨트롤 타워가 없다면
//		// if(starportUnit != null && starportUnit.getAddon() == null){
//		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Control_Tower) < 4){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0
//		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) < 4) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make starport for battle");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower, false);
//		// }
//		// }
//		// }
//		// }
//		// if(science == false && starComplete){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0
//		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Science_Facility, null) == 0) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make scienceFacil since we have many CC");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, false);
//		// }
//		// }
//		// if(scienceComplete){
//		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Physics_Lab) < 1){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Physics_Lab, null)
//		// + ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Physics_Lab, null) == 0){
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Physics_Lab, false);
//		// }
//		// }else if(physiclab){
//		// if(MyBotModule.Broodwar.self().hasResearched(TechType.Yamato_Gun) == false && MyBotModule.Broodwar.self().isResearching(TechType.Yamato_Gun) ==false){
//		// if(BuildManager.Instance().buildQueue.getItemCount(TechType.Yamato_Gun) == 0){
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Yamato_Gun, false);
//		// }
//		// }
//		//
//		// }
//		// }
//		//
//		// if(scienceComplete && physiclab){
//		// if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Battlecruiser) < RespondToStrategy.Instance().max_battlecruiser){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Battlecruiser, null) == 0) {
//		// BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Battlecruiser, false);
//		// }
//		// }
//		// }
//		// }
//		// battlecruiser end
//
//		// wraith for TvT start
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
//		// wraith for TvT end
//
//		// //valkyrie for TvT start
//		// if(RespondToStrategy.Instance().max_valkyrie > valkyriecnt && CC >2){
//		// if(star == false){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
//		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make starport for valkyrie");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
//		// }
//		// }
//		// if(armory == false){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
//		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make armory for valkyrie");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, false);
//		// }
//		// }
//		// if(starComplete){
//		// //컨트롤 타워가 없다면
//		// if(starportUnit != null && starportUnit.getAddon() == null){
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0
//		// && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make Terran_Control_Tower for valkyrie");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower, true);
//		// }
//		// }
//		// }
//		// if(starComplete && starportUnit.getAddon() != null && starportUnit.getAddon().isCompleted() == true){
//		// if(starportUnit.isTraining() == false){// && valkyriecnt < needwraithvalkyriecnt){ //TODO
//		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Valkyrie, null) == 0) {
//		// if(Config.BroodwarDebugYN){
//		// MyBotModule.Broodwar.printf("make valkyrie");
//		// }
//		// BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Valkyrie, false);
//		// }
//		// }
//		// }
//		// }
//		// valkyrie for TvT end
//
//	}
//
//	private void executeAddFactory() {
//
//		int CCcnt = 0;
//		int maxFaccnt = 0;
//		int Faccnt = 0;
//		int FaccntForMachineShop = 0;
//		int MachineShopcnt = 0;
//		boolean facFullOperating = true;
//
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//			if (unit.getType().isResourceDepot() && unit.isCompleted()) {
//				CCcnt++;
//			}
//			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()) {
//				Faccnt++;
//				FaccntForMachineShop++;
//				if (BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 4, unit.getTilePosition().getY() + 1) == false
//						|| BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 5, unit.getTilePosition().getY() + 1) == false
//						|| BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 4, unit.getTilePosition().getY() + 2) == false
//						|| BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 5, unit.getTilePosition().getY() + 2) == false) {
//					FaccntForMachineShop--;
//				}
//			}
//			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()) {
//				if (unit.isTraining() == false) {
//					facFullOperating = false;
//				}
//			}
//			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() == false) {
//				facFullOperating = false;
//			}
//			if (unit.getType() == UnitType.Terran_Machine_Shop) {
//				MachineShopcnt++;
//			}
//		}
//
//		if (CCcnt <= 1) {
//			if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.THE_HUNTERS) {
//				maxFaccnt = 4;
//			} else {
//				maxFaccnt = 3;
//			}
//		} else if (CCcnt == 2) {
//			if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.THE_HUNTERS) {
//				maxFaccnt = 6;
//			} else {
//				maxFaccnt = 6;
//			}
//		} else if (CCcnt >= 3) {
//			maxFaccnt = 9;
//		}
//
//		Faccnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);
//
//		int additonalmin = 0;
//		int additonalgas = 0;
//		if (facFullOperating == false) {
//			additonalmin = (Faccnt - 1) * 40;
//			additonalgas = (Faccnt - 1) * 20;
//		}
//
//		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) == 0) {
//			if (Faccnt == 0) {
//				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			} else if (Faccnt < maxFaccnt) {
//				if (Prebot.Broodwar.self().minerals() > 200 + additonalmin && Prebot.Broodwar.self().gas() > 100 + additonalgas) {
//					if (Faccnt == 2) {
//						if (UnitUtils.myFactoryUnitSupplyCount() > 24 && facFullOperating) {
//							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//						}
//					} else if (Faccnt <= 6) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//					} else {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.LastBuilingPoint, false);
//					}
//				}
//			}
//		}
//
//		if (MachineShopcnt < FaccntForMachineShop) {
//			for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//				if (unit == null)
//					continue;
//				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() && unit.isCompleted() && unit.getAddon() == null && unit.canBuildAddon()) {
//					int addition = 3;
//					if (Prebot.Broodwar.self().gas() > 300) {
//
//						int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
//						int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
//						int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
//						UnitType selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
//
//						if (selected == UnitType.Terran_Siege_Tank_Tank_Mode) {
//							addition = 0;
//						}
//					}
//
//					if (MachineShopcnt + addition < FaccntForMachineShop) {
//						if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
//							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null) == 0
//									&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) == 0) {
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop, true);
//								break;
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//	public void setCombatUnitRatio() {
//		vultureratio = 0;
//		tankratio = 0;
//		goliathratio = 0;
//		wgt = 1;
//
//		// config setting 가지고 오기
//		if (StrategyManager.Instance().currentStrategyException == EnemyStrategyException.INIT) {
//			vultureratio = StrategyManager.Instance().currentStrategy.vultureRatio;
//			tankratio = StrategyManager.Instance().currentStrategy.tankRatio;
//			goliathratio = StrategyManager.Instance().currentStrategy.goliathRatio;
//			wgt = StrategyManager.Instance().currentStrategy.weight;
//		} else {
//			vultureratio = StrategyManager.Instance().currentStrategyException.vultureRatio;
//			tankratio = StrategyManager.Instance().currentStrategyException.tankRatio;
//			goliathratio = StrategyManager.Instance().currentStrategyException.goliathRatio;
//			wgt = StrategyManager.Instance().currentStrategyException.weight;
//
//			if (vultureratio == 0 && tankratio == 0 && goliathratio == 0) {
//				vultureratio = StrategyManager.Instance().currentStrategy.vultureRatio;
//				tankratio = StrategyManager.Instance().currentStrategy.tankRatio;
//				goliathratio = StrategyManager.Instance().currentStrategy.goliathRatio;
//				wgt = StrategyManager.Instance().currentStrategy.weight;
//			}
//		}
//	}
//
//	private void executeCombatUnitTrainingBlocked() {
//
//		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//		BuildOrderItem currentItem = null;
//		Boolean goliathInTheQueue = false;
//		Boolean tankInTheQueue = false;
//		Boolean isfacexists = false;
//
//		if (Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed() < 4) {
//			return;
//		}
//		if (!tempbuildQueue.isEmpty()) {
//			currentItem = tempbuildQueue.getHighestPriorityItem();
//			while (true) {
//
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath) {
//					goliathInTheQueue = true;
//				}
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
//					tankInTheQueue = true;
//				}
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
//					return;
//				}
//				// if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType().isAddon()){
//				// return;
//				// }
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret) {
//					return;
//				}
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Vulture) {
//					return;
//				}
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_SCV) {
//					return;
//				}
//				if (currentItem.blocking == true) {
//					break;
//				}
//				if (tempbuildQueue.canSkipCurrentItem() == true) {
//					tempbuildQueue.skipCurrentItem();
//				} else {
//					break;
//				}
//				currentItem = tempbuildQueue.getItem();
//			}
//		} else {
//			return;
//		}
//
//		boolean isarmoryexists = false;
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//			if (unit.getType() == UnitType.Terran_Armory && unit.isCompleted()) {
//				isarmoryexists = true;
//			}
//			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()) {
//				isfacexists = true;
//			}
//		}
//		if (isfacexists == false) {
//			return;
//		}
//
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() && unit.isTraining() == false) {
//
//				if (unit.isConstructing() == true) {
//					continue;
//				}
//
//				// TODO else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop && unit.getAddon() == null) {
//					continue;
//				}
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
//					if (unit.getAddon() != null && unit.getAddon().isCompleted() != true) {
//						continue;
//					}
//				}
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath) {
//					if (isarmoryexists) {
//						break;
//					}
//				}
//
//				boolean eventually_vulture = true;
//
//				int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
//				int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
//				int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
//
//				UnitType selected = null;
//
//				selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
//
//				int minNeed = selected.mineralPrice();
//
//				if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() != selected) {
//					if (selected == UnitType.Terran_Siege_Tank_Tank_Mode && tankInTheQueue == false) {
//						if (unit.getAddon() != null && unit.getAddon().isCompleted() == true) {
//							if (currentItem.metaType.mineralPrice() + minNeed < Prebot.Broodwar.self().minerals()
//									&& currentItem.metaType.gasPrice() + selected.gasPrice() < Prebot.Broodwar.self().gas() && Prebot.Broodwar.self().supplyUsed() <= 392) {
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//								eventually_vulture = false;
//							}
//						}
//					} else if (selected == UnitType.Terran_Goliath && goliathInTheQueue == false) {
//						if (isarmoryexists) {
//							if (currentItem.metaType.mineralPrice() + minNeed < Prebot.Broodwar.self().minerals()
//									&& currentItem.metaType.gasPrice() + selected.gasPrice() < Prebot.Broodwar.self().gas() && Prebot.Broodwar.self().supplyUsed() <= 392) {
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//								eventually_vulture = false;
//							}
//						}
//					}
//				}
//
//				if (eventually_vulture) {
//					if (Prebot.Broodwar.self().gas() < 250) {
//						minNeed = 75;
//					}
//
//					if (currentItem.metaType.mineralPrice() + minNeed < Prebot.Broodwar.self().minerals() && Prebot.Broodwar.self().supplyUsed() <= 392) {
//						if ((unit.isConstructing() == true) || ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) != 0) {
//							continue;
//						}
//						// if(selected == UnitType.Terran_Goliath && isarmoryexists == false){
//						// continue;
//						// }
//						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) == 0) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//						}
//					}
//				}
//			}
//		}
//	}
//
//	private void executeCombatUnitTraining() {
//
//		// if(CurrentStrategyBasic == Strategys.terranBasic_BattleCruiser
//		// && MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Goliath) > 14
//		// && MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//		// + MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)> 14){
//		// return;
//		// }
//		int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
//		int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
//		int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
//
//		UnitType selected = null;
//		int currentinbuildqueuecnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null)
//				+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode, null)
//				+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath, null);
//
//		if (currentinbuildqueuecnt == 0) {
//			selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
//
//			if (selected.mineralPrice() < Prebot.Broodwar.self().minerals() && selected.gasPrice() < Prebot.Broodwar.self().gas() && Prebot.Broodwar.self().supplyUsed() <= 392) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//			}
//		}
//	}
//
//	private void executeUpgrade() {
//
//		Unit armory = null;
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit.getType() == UnitType.Terran_Armory) {
//				armory = unit;
//			}
//		}
//		if (armory == null) {
//			return;
//		}
//
//		boolean standard = false;
//		int unitPoint = AttackExpansionManager.Instance().UnitPoint;
//
//		int myFactoryUnitSupplyCount = UnitUtils.myFactoryUnitSupplyCount();
//		if (myFactoryUnitSupplyCount > 42 || (unitPoint > 10 && myFactoryUnitSupplyCount > 30)) {
//			standard = true;
//		}
//		if (unitPoint < -10 && myFactoryUnitSupplyCount < 60) {
//			standard = false;
//		}
//		// Fac Unit 18 마리 이상 되면 1단계 업그레이드 시도
//		if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 0 && standard && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)
//				&& Prebot.Broodwar.self().minerals() > 100 && Prebot.Broodwar.self().gas() > 100) {
//			if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
//			}
//		} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 0 && standard && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)
//				&& Prebot.Broodwar.self().minerals() > 100 && Prebot.Broodwar.self().gas() > 100) {
//			if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
//			}
//		}
//		// Fac Unit 30 마리 이상, 일정 이상의 자원 2단계
//		else if (Prebot.Broodwar.self().minerals() > 250 && Prebot.Broodwar.self().gas() > 225) {
//			if ((Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 2 && myFactoryUnitSupplyCount > 140)
//					|| (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 3 && myFactoryUnitSupplyCount > 80)) {
//
//				if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 1 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
//					}
//				} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 1 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
//					}
//				} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 2 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)) {// 3단계
//					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
//					}
//				} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 2 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
//					}
//				}
//			}
//		}
//	}
//
//	private void executeResearch() {
//
//		boolean VS = (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1 ? true : false)
//				|| (Prebot.Broodwar.self().isUpgrading(UpgradeType.Ion_Thrusters) ? true : false);
//		boolean VM = (Prebot.Broodwar.self().hasResearched(TechType.Spider_Mines)) || (Prebot.Broodwar.self().isResearching(TechType.Spider_Mines));
//		boolean TS = (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)) || (Prebot.Broodwar.self().isResearching(TechType.Tank_Siege_Mode));
//		boolean GR = (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false)
//				|| (Prebot.Broodwar.self().isUpgrading(UpgradeType.Charon_Boosters) ? true : false);
//
//		if (VS && VM && TS && GR)
//			return; // 4개 모두 완료이면
//
//		int currentResearched = 0;
//		if (VS) {
//			currentResearched++;
//		}
//		if (VM) {
//			currentResearched++;
//		}
//		if (TS) {
//			currentResearched++;
//		}
//		if (GR) {
//			currentResearched++;
//		}
//
//		int myFactoryUnitSupplyCount = UnitUtils.myFactoryUnitSupplyCount();
//		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) < 2 && currentResearched >= 2 && myFactoryUnitSupplyCount < 32
//				&& !(Prebot.Broodwar.self().minerals() > 300 && Prebot.Broodwar.self().gas() > 250))
//			return;
//
//		MetaType vultureSpeed = new MetaType(UpgradeType.Ion_Thrusters);
//		MetaType vultureMine = new MetaType(TechType.Spider_Mines);
//		MetaType TankSiegeMode = new MetaType(TechType.Tank_Siege_Mode);
//		MetaType GoliathRange = new MetaType(UpgradeType.Charon_Boosters);
//
//		MetaType vsZerg[] = new MetaType[] { vultureMine, GoliathRange, TankSiegeMode, vultureSpeed };
//		boolean vsZergbool[] = new boolean[] { VM, GR, TS, VS };
//		MetaType vsZergHydra[] = new MetaType[] { vultureMine, TankSiegeMode, GoliathRange, vultureSpeed };
//		boolean vsZergHydrabool[] = new boolean[] { VM, TS, GR, VS };
//		MetaType vsZergLurker[] = new MetaType[] { TankSiegeMode, GoliathRange, vultureMine, vultureSpeed };
//		boolean vsZergLurkerbool[] = new boolean[] { TS, GR, VM, VS };
//		MetaType vsTerran[] = new MetaType[] { vultureMine, TankSiegeMode, vultureSpeed, GoliathRange };
//		boolean vsTerranbool[] = new boolean[] { VM, TS, VS, GR };
//		MetaType vsTerranBio[] = new MetaType[] { vultureSpeed, TankSiegeMode, vultureMine, GoliathRange };
//		boolean vsTerranBiobool[] = new boolean[] { VS, TS, VM, GR };
//		// MetaType vsProtoss[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
//		// boolean vsProtossbool[] = new boolean[]{VM, VS, TS, GR};
//		MetaType vsProtoss[] = new MetaType[] { TankSiegeMode, vultureMine, vultureSpeed, GoliathRange };
//		boolean vsProtossbool[] = new boolean[] { TS, VM, VS, GR };
//		MetaType vsProtossZealot[] = new MetaType[] { vultureSpeed, vultureMine, TankSiegeMode, GoliathRange };
//		boolean vsProtossZealotbool[] = new boolean[] { VS, VM, TS, GR };
//		MetaType vsProtossDragoon[] = new MetaType[] { TankSiegeMode, vultureMine, vultureSpeed, GoliathRange };
//		boolean vsProtossDragoonbool[] = new boolean[] { TS, VM, VS, GR };
//		MetaType vsProtossDouble[] = new MetaType[] { vultureMine, TankSiegeMode, vultureSpeed, GoliathRange };
//		boolean vsProtossDoublebool[] = new boolean[] { VM, TS, VS, GR };
//		MetaType vsProtossBasic_DoublePhoto[] = new MetaType[] { TankSiegeMode, vultureSpeed, vultureMine, GoliathRange };
//		boolean vsProtossBasic_DoublePhotobool[] = new boolean[] { TS, VS, VM, GR };
//
//		MetaType[] Current = null;
//		boolean[] Currentbool = null;
//		boolean air = true;
//		boolean terranBio = false;
//
//		EnemyStrategy currentStrategy = StrategyManager.Instance().currentStrategy;
//		EnemyStrategyException currentStrategyException = StrategyManager.Instance().currentStrategyException;
//
//		EnemyStrategyException lastStrategyException = StrategyManager.Instance().lastStrategyException;
//
//		if (InformationManager.Instance().enemyRace == Race.Protoss) {
//			Current = vsProtoss;
//			Currentbool = vsProtossbool;
//			if (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
//					|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH)) {
//				Current = vsProtossZealot;
//				Currentbool = vsProtossZealotbool;
//			}
//			if ((currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
//					|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH))
//					|| (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH
//							|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH))) {
//				Current = vsProtossDragoon;
//				Currentbool = vsProtossDragoonbool;
//			}
//			if (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//					|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS)) {
//				Current = vsProtossDouble;
//				Currentbool = vsProtossDoublebool;
//			}
//
//			if (currentStrategy == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO) {
//				Current = vsProtossBasic_DoublePhoto;
//				Currentbool = vsProtossBasic_DoublePhotobool;
//			}
//
//			air = false;
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Protoss_Stargate || unit.getType() == UnitType.Protoss_Arbiter || unit.getType() == UnitType.Protoss_Carrier
//						|| unit.getType() == UnitType.Protoss_Corsair || unit.getType() == UnitType.Protoss_Scout || unit.getType() == UnitType.Protoss_Arbiter_Tribunal
//						|| unit.getType() == UnitType.Protoss_Fleet_Beacon || unit.getType() == UnitType.Protoss_Shuttle) {
//					air = true;
//				}
//			}
//		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
//			Current = vsTerran;
//			Currentbool = vsTerranbool;
//			if (currentStrategy == EnemyStrategy.TERRANBASIC_BIONIC) {
//				Current = vsTerranBio;
//				Currentbool = vsTerranBiobool;
//				terranBio = true;
//			}
//			air = false;
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Terran_Starport || unit.getType() == UnitType.Terran_Science_Facility || unit.getType() == UnitType.Terran_Dropship
//						|| unit.getType() == UnitType.Terran_Science_Vessel || unit.getType() == UnitType.Terran_Wraith || unit.getType() == UnitType.Terran_Battlecruiser
//						|| unit.getType() == UnitType.Terran_Physics_Lab || unit.getType() == UnitType.Terran_Control_Tower) {
//					air = true;
//				}
//			}
//		} else {
//			Current = vsZerg;
//			Currentbool = vsZergbool;
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Zerg_Mutalisk || unit.getType() == UnitType.Zerg_Lair || unit.getType() == UnitType.Zerg_Spire
//						|| unit.getType() == UnitType.Zerg_Scourge || unit.getType() == UnitType.Zerg_Guardian || unit.getType() == UnitType.Zerg_Devourer) {
//					air = true;
//				}
//			}
//			if (currentStrategy == EnemyStrategy.ZERGBASIC_HYDRAWAVE || currentStrategy == EnemyStrategy.ZERGBASIC_LINGHYDRA) {
//				Current = vsZergHydra;
//				Currentbool = vsZergHydrabool;
//			}
//
//			if (currentStrategyException == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER) {
//				Current = vsZergLurker;
//				Currentbool = vsZergLurkerbool;
//			}
//		}
//
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//
//			if (unit.getType() == UnitType.Terran_Machine_Shop && unit.isCompleted() && unit.canUpgrade()) {
//				if (Currentbool == null)
//					return;
//				for (int i = 0; i < 4; i++) {
//					if (Currentbool[i] == true) {
//						continue;
//					} else {
//						if (i == 3 && Current[3].getUpgradeType() == UpgradeType.Charon_Boosters) {
//							if (!air && !(Prebot.Broodwar.self().minerals() > 200 && Prebot.Broodwar.self().gas() > 150)) {
//								continue;
//							}
//						}
//						if (terranBio && i == 2 && Current[2].getTechType() == TechType.Spider_Mines) {
//							if ((myFactoryUnitSupplyCount > 48 && Prebot.Broodwar.self().minerals() > 200 && Prebot.Broodwar.self().gas() > 150)
//									|| myFactoryUnitSupplyCount > 100) {
//
//							} else {
//								continue;
//							}
//						}
//						if (BuildManager.Instance().buildQueue.getItemCount(Current[i]) == 0) {
//							UpgradeType tempU = null;
//							if (Current[i].isUpgrade()) {
//								boolean booster = false;
//								for (Unit unitcheck : Prebot.Broodwar.self().getUnits()) {
//									if (unitcheck.getType() == UnitType.Terran_Armory && unitcheck.isCompleted()) {
//										booster = true;
//									}
//								}
//								tempU = Current[i].getUpgradeType();
//								if (tempU == UpgradeType.Charon_Boosters && booster == false) {
//									return;
//								}
//							}
//							if (currentResearched <= 2) {
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(Current[i], true);
//							} else {
//								BuildManager.Instance().buildQueue.queueAsLowestPriority(Current[i], false);
//							}
//						}
//						break;
//					}
//				}
//			}
//		}
//	}
//
//
//	private static int least(double a, double b, double c, int checker) {
//
//		int ret = 0;
//		if (a > b) {
//			if (b > c) {
//				ret = 3; // a>b>c
//			} else {
//				ret = 2; // a>b, b>=c
//			}
//		} else {
//			if (a > c) { // a<=b, a>c
//				ret = 3;
//			} else { // a<=b, a<=c
//				ret = 1;
//			}
//		}
//		if (ret == 1) {
//			if (a == b && checker != 3) {
//				ret = checker;
//			} else if (a == c && checker != 2) {
//				ret = checker;
//			}
//		} else if (ret == 2) {
//			if (b == a && checker != 3) {
//				ret = checker;
//			} else if (b == c && checker != 1) {
//				ret = checker;
//			}
//		} else if (ret == 3) {
//			if (c == a && checker != 2) {
//				ret = checker;
//			} else if (c == b && checker != 1) {
//				ret = checker;
//			}
//		}
//		return ret;
//	}
//
//	private static UnitType chooseunit(int ratea, int rateb, int ratec, int wgt, int tota, int totb, int totc) {
//
//		if (wgt < 1 || wgt > 3) {
//			wgt = 1;
//		}
//		double tempa = 0;
//		double tempb = 0;
//		double tempc = 0;
//		if (ratea == 0) {
//			tempa = 99999999;
//		} else {
//			tempa = 1.0 / ratea * tota;
//		}
//		if (rateb == 0) {
//			tempb = 99999999;
//		} else {
//			tempb = 1.0 / rateb * totb;
//		}
//		if (ratec == 0) {
//			tempc = 99999999;
//		} else {
//			tempc = 1.0 / ratec * totc;
//		}
//		int num = least(tempa, tempb, tempc, wgt);
//		if (num == 3) {// 1:벌쳐, 2:시즈, 3:골리앗
//			return UnitType.Terran_Goliath;
//		} else if (num == 2) {
//			return UnitType.Terran_Siege_Tank_Tank_Mode;
//		} else {
//			return UnitType.Terran_Vulture;
//		}
//	}
//
//	private int GetCurrentTot(UnitType checkunit) {
//		return BuildManager.Instance().buildQueue.getItemCount(checkunit) + Prebot.Broodwar.self().allUnitCount(checkunit);
//	}
//
//	private int GetCurrentTotBlocked(UnitType checkunit) {
//		int cnt = Prebot.Broodwar.self().allUnitCount(checkunit);
//		return cnt;
//	}

}