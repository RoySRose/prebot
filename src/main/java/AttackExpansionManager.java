

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;

public class AttackExpansionManager {
	
	private static AttackExpansionManager instance = new AttackExpansionManager();

	/// static singleton 객체를 리턴합니다
	public static AttackExpansionManager Instance() {
		return instance;
	}

	public boolean pushSiegeLine = false;
	public int combatTime = 0;
	public int combatStartCase = 0;
	public int attackPoint = 0;
	public int myUnitPoint = 0;
	public int expansionPoint = 0;
	public int unitPoint = 0;

	public void executeCombat() {

		int unitPoint = 0;
		int expansionPoint = 0;

		int selfBaseCount = InfoUtils.myOccupiedBases().size();
		int enemyBaseCount = InfoUtils.enemyOccupiedBases().size();
		if (enemyBaseCount == 0) {
			return;
		}

		int myFactoryUnitPoint = UnitUtils.myFactoryUnitSupplyCount();

		// 공통 기본 로직, 팩토리 유닛 50마리 기준 유닛 Kill Death 상황에 따라 변경.

		int killedcombatunit = getTotKilledCombatUnits();
		int deadCombatunit = myDeadCombatUnitSupplies();
		
		int totworkerkilled = MyBotModule.Broodwar.self().killedUnitCount(InformationManager.Instance().getWorkerType(InfoUtils.enemyRace())) * 2;
		int totworkerdead = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_SCV) * 2;
		
		if (TimeUtils.beforeTime(15, 0)) { // 약 시작 ~ 15분까지
			unitPoint += (totworkerkilled - totworkerdead) * (-MyBotModule.Broodwar.getFrameCount() / 40000.0 * 3.0 + 3.0);
			unitPoint += (killedcombatunit - deadCombatunit);

		} else if (TimeUtils.beforeTime(28, 0)) { // 약 15분 ~ 28분까지 // 여기서부턴 시간보다.... 현재 전체 규모수가 중요할듯?
			unitPoint += (totworkerkilled - totworkerdead) * (-MyBotModule.Broodwar.getFrameCount() / 40000.0 * 3.0 + 3.0);
			unitPoint += (killedcombatunit - deadCombatunit) * (-MyBotModule.Broodwar.getFrameCount() / 20000.0 + 2.0);
		}
		
		int myExpansioningCount = selfExspansioningCount();
		
		boolean myExpansioning = myExpansioningCount > 0;
		boolean enemyExpansioning = enemyExspansioning();

		if (selfBaseCount == enemyBaseCount && !myExpansioning && !enemyExspansioning()) { // 베이스가 동일할때
			if (InfoUtils.enemyRace() == Race.Zerg || InfoUtils.enemyRace() == Race.Protoss) {
				expansionPoint += 5;
			}
			
		} else if (selfBaseCount > enemyBaseCount) {// 우리가 베이스가 많으면
			if (selfBaseCount - enemyBaseCount == 1) {
				if (myExpansioning) {
					if (!enemyExspansioning()) {
						expansionPoint -= 15;
					} else {
						expansionPoint += 10;
					}
				} else {
					if (enemyExspansioning()) {
						expansionPoint += 40;
					} else {
						expansionPoint += 20;
					}
				}
			} else if (selfBaseCount - enemyBaseCount > 1) {
				 if (selfBaseCount - enemyBaseCount > myExpansioningCount) {
					 expansionPoint += 20 + 20 * (selfBaseCount - enemyBaseCount);
				 }
			}
			
		} else if (selfBaseCount < enemyBaseCount) {// 우리가 베이스가 적으면
			if (enemyBaseCount - selfBaseCount == 1) {
				if (myExpansioning) {
					if (!enemyExpansioning) {
						expansionPoint -= 20;
						if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
							expansionPoint += 5;
						}
					} else {
						expansionPoint -= 10;
					}
				} else {
					if (enemyExpansioning) {
						expansionPoint += 10;
					}
				}
			} else {
				expansionPoint -= 20;
			}
		}

		// 종족별 예외 상황
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if (myFactoryUnitPoint > 80 && killedcombatunit - deadCombatunit > myFactoryUnitPoint / 4) {// 죽인수 - 죽은수 가 현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			if (myFactoryUnitPoint > 80 && killedcombatunit - deadCombatunit > myFactoryUnitPoint / 3) {// 죽인수 - 죽은수 가 현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}

			unitPoint += InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) * 4;
			
		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {
			if (myFactoryUnitPoint > 80 && killedcombatunit - deadCombatunit > myFactoryUnitPoint / 3) {// 죽인수 - 죽은수 가 현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
			unitPoint += InformationManager.Instance().getNumUnits(UnitType.Zerg_Sunken_Colony, InformationManager.Instance().enemyPlayer) * 4;
			// triple hatchery
		}

		// 내 팩토리 유닛 인구수 만큼 추가
		int totPoint = myFactoryUnitPoint + expansionPoint + unitPoint;
		
		boolean isAttackMode = StrategyIdea.mainSquadMode.isAttackMode;
		boolean fastAttack = StrategyIdea.mainSquadMode == MicroConfig.MainSquadMode.SPEED_ATTCK;
		boolean noMercyAttack = StrategyIdea.mainSquadMode == MicroConfig.MainSquadMode.NO_MERCY;

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			int plus = 0;
			if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.BIONIC)) {
				plus = 2;
			}

			if (unitPoint > 10 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 4 + plus) {
				isAttackMode = true;
			} else if (unitPoint > 0 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 5 + plus) {
				isAttackMode = true;
			} else if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 6 + plus) {
				isAttackMode = true;
			}
			int completeCommandCenterCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
			if (isAttackMode && unitPoint < 0) {
				if (completeCommandCenterCount == 1 && myFactoryUnitPoint + unitPoint < 0) {
					isAttackMode = false;
				} else if (completeCommandCenterCount == 2 && myFactoryUnitPoint + unitPoint / 2 < 0) {
					isAttackMode = false;
				} else if (completeCommandCenterCount > 3 && myFactoryUnitPoint < 20) {
					isAttackMode = false;
				}
			}
			if (completeCommandCenterCount > 4) {
				completeCommandCenterCount = 4;
			}
			
			pushSiegeLine = false;
			if ((myFactoryUnitPoint > 250 - completeCommandCenterCount * 10 || MyBotModule.Broodwar.self().supplyUsed() > 392)) {
				pushSiegeLine = true;
				combatStartCase = 1;
			}

			if (combatStartCase == 1 && myFactoryUnitPoint < 90) {
				pushSiegeLine = false;
			}

			if (myFactoryUnitPoint > 100 && unitPoint > 40) {
				pushSiegeLine = true;
				combatStartCase = 2;
			}
			if (combatStartCase == 2 && unitPoint < 10) {
				pushSiegeLine = false;
			}

		} else {
			// 공통 예외 상황
			if ((myFactoryUnitPoint > 170 || MyBotModule.Broodwar.self().supplyUsed() > 392) && !isAttackMode) {// 팩토리 유닛 130 이상 또는 서플 196 이상
				isAttackMode = true;
				combatStartCase = 1;
			}

			if (totPoint > 120 && isAttackMode && myFactoryUnitPoint > 80) {// 팩토리 유닛이 30마리(즉 스타 인구수 200 일때)

				if (InformationManager.Instance().enemyRace == Race.Zerg
						&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) > 6) {
					if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) < MyBotModule.Broodwar.self()
							.completedUnitCount(UnitType.Terran_Goliath)) {
						isAttackMode = true;
					}
					// triple hatchery
				} else {
					isAttackMode = true;
				}
				combatStartCase = 2;
			}

			if (!isAttackMode && StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.QUICK_ATTACK)
					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) + MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 1) {
				isAttackMode = true;
				fastAttack = true;
				combatStartCase = 5;

				if (combatTime == 0) {
					combatTime = MyBotModule.Broodwar.getFrameCount() + 5000;
				}
			}

			if (isAttackMode) {
				if (combatStartCase == 1 && myFactoryUnitPoint < 30) {
					isAttackMode = false;
					
				} else if (combatStartCase == 2) {

					if (InformationManager.Instance().enemyRace == Race.Zerg
							&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) > 6) {
						if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) > MyBotModule.Broodwar.self()
								.completedUnitCount(UnitType.Terran_Goliath)) {
							isAttackMode = false;
						}
						// triple hatchery
						if (totPoint < 50) {
							isAttackMode = false;
						}
					} else if (totPoint < 50) {
						isAttackMode = false;
					}
				} else if (combatStartCase == 5) {
					if (combatTime < MyBotModule.Broodwar.getFrameCount() && ((myFactoryUnitPoint < 20 && unitPoint < 20) || unitPoint < -10)) {
						isAttackMode = false;
						
					} else if (InfoUtils.enemyNumUnits(UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon)
							+ InfoUtils.enemyDeadNumUnits(UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon) > 20) {
						if (totPoint < 50) {
							isAttackMode = false;
						}
					}
				}

				if (InformationManager.Instance().enemyRace == Race.Protoss) {
					
					if (UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Dark_Templar)) {
						if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Science_Vessel) == 0
								&& UnitUtils.availableScanningCount() == 0) {
//							MyBotModule.Broodwar.printf("dark and no comsat or vessel");
							isAttackMode = false;
						}
					}
				}

				if (isAttackMode && expansionPoint >= 0 && myFactoryUnitPoint > 30 * 4 && unitPoint + totPoint * 0.1 > 55) {
					if (InformationManager.Instance().enemyRace == Race.Protoss && InfoUtils.enemyNumUnits(UnitType.Protoss_Photon_Cannon) <= 3) {
						noMercyAttack = true;
					}
					if (InformationManager.Instance().enemyRace == Race.Zerg && InfoUtils.enemyNumUnits(UnitType.Zerg_Sunken_Colony) <= 3) {
						noMercyAttack = true;
					}
				}

				if (!isAttackMode || (noMercyAttack && (expansionPoint < 0 || (myFactoryUnitPoint < 40 || unitPoint < 10)))) {
					noMercyAttack = false;
				}
			}
		}

		this.myUnitPoint = myFactoryUnitPoint;
		this.expansionPoint = expansionPoint;
		this.unitPoint = unitPoint;
		this.attackPoint = totPoint;
		
		if (isAttackMode) {
			if (!StrategyIdea.mainSquadMode.isAttackMode) {
				StrategyIdea.attackStartedFrame = TimeUtils.elapsedFrames();
			}
			
			if (noMercyAttack) {
				StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.NO_MERCY;
			} else if (fastAttack) {
				StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.SPEED_ATTCK;
			} else {
				StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.ATTCK;
			}

		} else {
			if (StrategyIdea.mainSquadMode.isAttackMode) {
				StrategyIdea.retreatFrame = TimeUtils.elapsedFrames();
			}
			
			StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.NORMAL;
		}
	}

//	public void executeExpansion() {
//
//		if (Prebot.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center) > 0) {
//			return;
//		}
//		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) != 0) {
//			return;
//		}
//
//		int MaxCCcount = 4;
//		int CCcnt = 0;
//
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			if (unit == null)
//				continue;
//			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {
//				if (getValidMineralsForExspansionNearDepot(unit) > 6) {
//					CCcnt++;
//				}
//			}
//		}
//		if (CCcnt >= MaxCCcount) {
//			return;
//		}
//
//		int factoryUnitCount = UnitUtils.myFactoryUnitSupplyCount();
//		int RealCCcnt = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
//
//		// 앞마당 전
//		if (RealCCcnt == 1) {// TODO 이거 손봐야된다... 만약 위로 띄어서 해야한다면?? 본진에 지어진거 카운트 안되는 상황에서 앞마당에 지어버리겟네
//			if (!InitialBuildProvider.Instance().initialBuildFinished()) {
//				return;
//			}
//
//			if ((Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 40 && unitPoint > 15) || (factoryUnitCount > 60 && attackPoint > 60 && expansionPoint > 0)) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//			if (Prebot.Broodwar.getFrameCount() > 9000 && Prebot.Broodwar.self().minerals() > 400) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//			if (Prebot.Broodwar.getFrameCount() > 12000 && factoryUnitCount > 40) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//			if (Prebot.Broodwar.getFrameCount() > 80000 && factoryUnitCount > 80) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//			if ((Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 40)) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//
//		}
//
//		// 앞마당 이후
//		else if (RealCCcnt >= 2) {
//			
//			boolean isAttackMode = StrategyIdea.mainSquadMode.isAttackMode;
//
//			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
//			if (Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 120) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//			// 공격시 돈 250 넘으면 멀티하기
//			if (isAttackMode && Prebot.Broodwar.self().minerals() > 250) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//			// 800 넘으면 멀티하기
//			if (Prebot.Broodwar.self().minerals() > 800) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//
//			// 500 넘고 유리하면
//			if (Prebot.Broodwar.self().minerals() > 500 && factoryUnitCount > 50 && attackPoint > 30) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//			// 200 넘고 유리하면
//			if (Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 80 && attackPoint > 40 && expansionPoint >= 0) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//			// 공격시 돈 250 넘으면 멀티하기
//
//			if (Prebot.Broodwar.getFrameCount() > 14000) {
//
//				if (factoryUnitCount > 80 && Prebot.Broodwar.self().deadUnitCount() - Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture) < 12) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//					}
//				}
//			}
//
//			int temp = 0;
//			for (Unit units : Prebot.Broodwar.self().getUnits()) {
//				if (units.getType() == UnitType.Terran_Command_Center && units.isCompleted()) {
//					temp += WorkerManager.Instance().getWorkerData().getMineralsSumNearDepot(units);
//				}
//			}
//			if (temp < 8000 && isAttackMode) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//			if (factoryUnitCount > 160) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
//				}
//			}
//		}
//	}

	private int myDeadCombatUnitSupplies() {

		int totmarine = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Marine);
		int tottank = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
		int totgoliath = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Goliath);
		int totvulture = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture);
		int totwraith = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith);
		int totvessel = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Science_Vessel);

		int result = (tottank + totgoliath + totvulture + totwraith + totvessel) * 2 + totmarine;
		return result * 2;// 스타에서는 두배
	}

	private int getTotKilledCombatUnits() {

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			int totbio = 0;

			int tottank = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
			int totgoliath = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Goliath);
			int totvulture = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture);
			int totwraith = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith);
			int totvessel = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Science_Vessel);
			totbio = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Marine) + MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Firebat)
					+ MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Medic);

			int res = tottank + totgoliath + totvulture + totwraith + totvessel;
			res = res * 2 + totbio;

			return res * 2;

		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {

			int totzerling = 0;
			int tothydra = 0;
			int totmutal = 0;
			int totoverload = 0;
			int totlurker = 0;

			// tot = MyBotModule.Broodwar.self().killedUnitCount(UnitType.AllUnits);
			totzerling = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Zergling);
			tothydra = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Hydralisk);
			totlurker = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Lurker) + MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Lurker_Egg);
			totmutal = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Mutalisk);
			totoverload = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Overlord);
			int totguardian = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Guardian);
			int totultra = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Ultralisk);
			int totsunken = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Sunken_Colony);

			int res = totzerling + (totsunken * 2) + (tothydra * 2) + (totmutal * 4) + (totoverload * 3) + (totlurker * 5) + (totguardian * 8) + (totultra * 10);
			return res; // 저그는 저글링 때문에 이미 2배 함

		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {

			// tot = MyBotModule.Broodwar.self().killedUnitCount();

			int totzealot = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Zealot);
			int totdragoon = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Dragoon);
			int totarchon = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Archon);
			int totphoto = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Photon_Cannon);
			int tothigh = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_High_Templar);
			int totdark = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Dark_Templar);
			int totcarrier = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Carrier);

			int res = (totzealot + totdragoon + totphoto + tothigh + totdark) * 2 + totarchon * 4 + totcarrier * 8;
			return res * 2;
			
		} else {
			return 0;
		}
	}

	private boolean enemyExspansioning() {
		List<UnitInfo> enemyResourceDepot = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL, InformationManager.Instance().getBasicResourceDepotBuildingType(InfoUtils.enemyRace()));
		for (UnitInfo enemyDepot : enemyResourceDepot) {
			if (enemyDepot.isCompleted()) {
				return true;
			}
		}
		return false;
	}

	private int selfExspansioningCount() {
		int count = 0;
		List<Unit> incompleteCenters = UnitUtils.getUnitList(CommonCode.UnitFindRange.INCOMPLETE, UnitType.Terran_Command_Center);
		count += incompleteCenters.size();
		
		for (Unit commandcenter : UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(commandcenter) < 7) {
				count++;
			}
		}
		return count;
	}

	public int getValidMineralsForExspansionNearDepot(Unit depot) {
		if (depot == null) {
			return 0;
		}

		int mineralsNearDepot = 0;

		for (Unit unit : MyBotModule.Broodwar.getAllUnits()) {
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 450 && unit.getResources() > 200) {
				mineralsNearDepot++;
			}
		}

		return mineralsNearDepot;
	}


}
