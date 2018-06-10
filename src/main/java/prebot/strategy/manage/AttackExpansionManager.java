package prebot.strategy.manage;

import java.util.List;

import bwapi.Order;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.micro.constant.MicroCode.CombatStrategy;
import prebot.micro.constant.MicroCode.CombatStrategyDetail;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.old.OldCombatManager;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.StrategyManager;
import prebot.strategy.TempBuildSourceCode;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;

public class AttackExpansionManager {
	
	private static AttackExpansionManager instance = new AttackExpansionManager();

	/// static singleton 객체를 리턴합니다
	public static AttackExpansionManager Instance() {
		return instance;
	}

	public int CombatTime = 0;
	public int CombatStartCase = 0;
	public int Attackpoint = 0;
	public int MyunitPoint = 0;
	public int ExpansionPoint = 0;
	public int UnitPoint = 0;

	public void executeCombat() {
		// TODO 상대방 신규 멀티를 찾았을때 공격 여부 한번더 돌려야함(상대 멀티 진행 여부 판단해야되므로
		int unitPoint = 0;
		int expansionPoint = 0;
		int totPoint = 0;

		int selfbasecnt = 0;
		int enemybasecnt = 0;

		// boolean allaware = InformationManager.Instance().isReceivingEveryMultiInfo();

		// TODO 아군은 돌아가기 시작하고 scv 특정 수 이상 붙은 컴맨드만 쳐야 하지 않나?
		selfbasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().selfPlayer);
		enemybasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().enemyPlayer);
		// TODO상대가 다른곳에 파일론만 지으면.. 문제 되는데..
		// CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_INSIDE);

		if (enemybasecnt == 0) {
			return;
		}

		int myunitPoint = UnitUtils.myFactoryUnitSupplyCount();

		// 공통 기본 로직, 팩토리 유닛 50마리 기준 유닛 Kill Death 상황에 따라 변경.

		int deadcombatunit = getTotDeadCombatUnits();
		int killedcombatunit = getTotKilledCombatUnits();
		int totworkerdead = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_SCV) * 2;
		int totworkerkilled = Prebot.Broodwar.self().killedUnitCount(InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace)) * 2;
		int totaldeadunit = Prebot.Broodwar.self().deadUnitCount();
		int totalkilledunit = Prebot.Broodwar.self().deadUnitCount();

		// if(selfbasecnt > enemybasecnt){ //약 시작 ~ 15분까지
		if (Prebot.Broodwar.getFrameCount() < 20000) { // 약 시작 ~ 15분까지
			unitPoint += (totworkerkilled - totworkerdead) * (-Prebot.Broodwar.getFrameCount() / 40000.0 * 3.0 + 3.0);
			unitPoint += (killedcombatunit - deadcombatunit);

		} else if (Prebot.Broodwar.getFrameCount() < 40000) { // 약 15분 ~ 28분까지 // 여기서부턴 시간보다.... 현재 전체 규모수가 중요할듯?
			unitPoint += (totworkerkilled - totworkerdead) * (-Prebot.Broodwar.getFrameCount() / 40000.0 * 3.0 + 3.0);
			unitPoint += (killedcombatunit - deadcombatunit) * (-Prebot.Broodwar.getFrameCount() / 20000.0 + 2.0);
		}

		if (selfbasecnt == enemybasecnt && selfExspansioning() == 0 && enemyExspansioning() == false) { // 베이스가 동일할때
			if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
				expansionPoint += 5;
			}
		} else if (selfbasecnt > enemybasecnt) {// 우리가 베이스가 많으면
			if (selfbasecnt - enemybasecnt == 1) {
				if (selfExspansioning() > 0) {
					if (enemyExspansioning() == false) {
						expansionPoint -= 15;
					} else {
						expansionPoint += 10;
					}
				} else if (selfExspansioning() == 0) {
					if (enemyExspansioning() == true) {
						expansionPoint += 40;
					}
					if (enemyExspansioning() == false) {
						expansionPoint += 20;
					}
				}
			}
			if (selfbasecnt - enemybasecnt > 1 && selfbasecnt - enemybasecnt > selfExspansioning()) {
				expansionPoint += 20 + 20 * (selfbasecnt - enemybasecnt);
			}
		} else if (selfbasecnt < enemybasecnt) {// 우리가 베이스가 적으면
			if (enemybasecnt - selfbasecnt == 1) {
				if (selfExspansioning() > 0) {
					if (enemyExspansioning() == false) {
						expansionPoint -= 20;
						if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
							expansionPoint += 5;
						}
					} else {
						expansionPoint -= 10;
					}
				} else {
					if (enemyExspansioning() == true) {
						expansionPoint += 10;
					}
				}
			} else {
				expansionPoint -= 20;
			}
		}

		// 종족별 예외 상황
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if (myunitPoint > 80 && killedcombatunit - deadcombatunit > myunitPoint / 4) {// 죽인수 - 죽은수 가 현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
		}
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			if (myunitPoint > 80 && killedcombatunit - deadcombatunit > myunitPoint / 3) {// 죽인수 - 죽은수 가 현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}

			unitPoint += InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) * 4;
		}

		if (InformationManager.Instance().enemyRace == Race.Zerg) {
			if (myunitPoint > 80 && killedcombatunit - deadcombatunit > myunitPoint / 3) {// 죽인수 - 죽은수 가 현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
			unitPoint += InformationManager.Instance().getNumUnits(UnitType.Zerg_Sunken_Colony, InformationManager.Instance().enemyPlayer) * 4;
			// triple hatchery
		}

		// 내 팩토리 유닛 인구수 만큼 추가
		totPoint = myunitPoint + expansionPoint + unitPoint;

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			int plus = 0;
			if (StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.TERRANBASIC_BIONIC) {
				plus = 2;
			}

			if (unitPoint > 10 && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 4 + plus) {
				OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			} else if (unitPoint > 0 && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 5 + plus) {
				OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			} else if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 6 + plus) {
				OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			}

			int CC = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
			if (OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && unitPoint < 0) {
				if (CC == 1 && myunitPoint + unitPoint < 0) {
					OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
				if (CC == 2 && myunitPoint + unitPoint / 2 < 0) {
					OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
				if (CC > 3 && myunitPoint < 20) {
					OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
			}
			if (CC > 4) {
				CC = 4;
			}
			if ((myunitPoint > 250 - CC * 10 || Prebot.Broodwar.self().supplyUsed() > 392)) {
				OldCombatManager.Instance().pushSiegeLine = true;
				CombatStartCase = 1;
			}

			if (CombatStartCase == 1 && myunitPoint < 90) {
				OldCombatManager.Instance().pushSiegeLine = false;
			}

			if (myunitPoint > 100 && unitPoint > 40) {
				OldCombatManager.Instance().pushSiegeLine = true;
				CombatStartCase = 2;
			}
			if (CombatStartCase == 2 && unitPoint < 10) {
				OldCombatManager.Instance().pushSiegeLine = false;
			}

		} else {
			// 공통 예외 상황
			if ((myunitPoint > 170 || Prebot.Broodwar.self().supplyUsed() > 392) && OldCombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY) {// 팩토리 유닛 130 이상 또는 서플 196 이상
																																										// 사용시(스타 내부에서는 2배)

				OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
				CombatStartCase = 1;
			}

			if (totPoint > 120 && OldCombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY && myunitPoint > 80) {// 팩토리 유닛이 30마리(즉 스타 인구수 200 일때)

				if (InformationManager.Instance().enemyRace == Race.Zerg
						&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) > 6) {
					if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) < Prebot.Broodwar.self()
							.completedUnitCount(UnitType.Terran_Goliath)) {
						OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
					}
					// triple hatchery
				} else {
					OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
				}
				CombatStartCase = 2;
			}

			if ((StrategyManager.Instance().lastStrategy == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO || StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO)
					&& OldCombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY
					&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
							+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 1) {
				OldCombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
				CombatStartCase = 5;
				OldCombatManager.Instance().setDetailStrategy(CombatStrategyDetail.NO_CHECK_NO_GUERILLA, 500 * 24);
				OldCombatManager.Instance().setDetailStrategy(CombatStrategyDetail.NO_WAITING_CHOKE, 500 * 24);

				if (CombatTime == 0) {
					CombatTime = Prebot.Broodwar.getFrameCount() + 5000;
				}
			}

			if (OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY) {
				if (CombatStartCase == 1 && myunitPoint < 30) {
					OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
				if (CombatStartCase == 2) {

					if (InformationManager.Instance().enemyRace == Race.Zerg
							&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) > 6) {
						if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) > Prebot.Broodwar.self()
								.completedUnitCount(UnitType.Terran_Goliath)) {
							OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
						}
						// triple hatchery
						if (totPoint < 50) {
							OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
						}
					} else if (totPoint < 50) {
						OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
					}
				}
				if (CombatStartCase == 5) {

					if (CombatTime < Prebot.Broodwar.getFrameCount() && ((myunitPoint < 20 && unitPoint < 20) || unitPoint < -10)) {

						OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
						if (StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO) {
							StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.PROTOSSBASIC);
						}
					}

					if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot, InformationManager.Instance().enemyPlayer)
							+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Zealot)
							+ InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon, InformationManager.Instance().enemyPlayer)
							+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Dragoon) > 20) {
						if (totPoint < 50) {
							OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
							if (StrategyManager.Instance().currentStrategy == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO) {
								StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.PROTOSSBASIC);
							}
						}
					}
				}
				// if(CombatStartCase == 3 && (myunitPoint < 8 || unitPoint < -20) ){
				// if(Config.BroodwarDebugYN){
				// MyBotModule.Broodwar.printf("Retreat3");
				// }
				// CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				// }
				// if(CombatStartCase == 4 && myunitPoint < 60){
				// if(Config.BroodwarDebugYN){
				// MyBotModule.Broodwar.printf("Retreat4");
				// }
				// LastStrategyException = StrategysException.Init;
				// CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				// }

				if (InformationManager.Instance().enemyRace == Race.Protoss) {
					if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Science_Vessel) == 0
							&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Comsat_Station) == 0) {
						for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
							if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid() && unit.isFlying() == false) {
								Prebot.Broodwar.printf("dark and no comsat or vessel");
								OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
							}
						}
					} else if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Comsat_Station) > 0) {
						boolean energy = false;
						boolean dark = false;
						for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
							if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid() && unit.isFlying() == false) {
								dark = true;
							}
						}
						if (dark) {
							for (Unit myunit : Prebot.Broodwar.self().getUnits()) {
								if (myunit.getType() == UnitType.Terran_Comsat_Station && myunit.isCompleted() && myunit.getEnergy() > 50) {
									energy = true;
								}
							}
							if (energy == false) {
								OldCombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
							}
						}
					}
				}

				if (OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && expansionPoint >= 0 && myunitPoint > 120 && unitPoint + totPoint * 0.1 > 55) {
					if (InformationManager.Instance().enemyRace == Race.Protoss
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) <= 3) {
						OldCombatManager.Instance().setDetailStrategy(CombatStrategyDetail.ATTACK_NO_MERCY, 500 * 24);
					}
					if (InformationManager.Instance().enemyRace == Race.Zerg
							&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Sunken_Colony, InformationManager.Instance().enemyPlayer) <= 3) {
						OldCombatManager.Instance().setDetailStrategy(CombatStrategyDetail.ATTACK_NO_MERCY, 500 * 24);
					}

				}

				if (OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.DEFENCE_CHOKEPOINT
						|| (OldCombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.ATTACK_NO_MERCY) > 0
								&& (expansionPoint < 0 || (myunitPoint < 40 || unitPoint < 10)))) {
					OldCombatManager.Instance().setDetailStrategy(CombatStrategyDetail.ATTACK_NO_MERCY, 0);
				}
			}
		}

		MyunitPoint = myunitPoint;
		ExpansionPoint = expansionPoint;
		UnitPoint = unitPoint;
		Attackpoint = totPoint;
		
		if (OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY) {
			if (OldCombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.ATTACK_NO_MERCY) > 0) {
				StrategyIdea.mainSquadMode = MainSquadMode.NO_MERCY;
			} else if (OldCombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.NO_WAITING_CHOKE) > 0) {
				StrategyIdea.mainSquadMode = MainSquadMode.SPEED_ATTCK;
			} else {
				StrategyIdea.mainSquadMode = MainSquadMode.ATTCK;
			}
			
		} else {
			StrategyIdea.mainSquadMode = MainSquadMode.NORMAL;
		}
	}

	public void executeExpansion() {

		if (Prebot.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center) > 0) {
			return;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) != 0) {
			return;
		}

		int MaxCCcount = 4;
		int CCcnt = 0;

		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
			if (unit == null)
				continue;
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()) {
				if (getValidMineralsForExspansionNearDepot(unit) > 6) {
					CCcnt++;
				}
			}
		}
		if (CCcnt >= MaxCCcount) {
			return;
		}

		int factoryUnitCount = UnitUtils.myFactoryUnitSupplyCount();
		int RealCCcnt = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);

		// 앞마당 전
		if (RealCCcnt == 1) {// TODO 이거 손봐야된다... 만약 위로 띄어서 해야한다면?? 본진에 지어진거 카운트 안되는 상황에서 앞마당에 지어버리겟네
			if (!TempBuildSourceCode.Instance().isInitialBuildOrderFinished()) {
				return;
			}

			if ((Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 40 && UnitPoint > 15) || (factoryUnitCount > 60 && Attackpoint > 60 && ExpansionPoint > 0)) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if (Prebot.Broodwar.getFrameCount() > 9000 && Prebot.Broodwar.self().minerals() > 400) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if (Prebot.Broodwar.getFrameCount() > 12000 && factoryUnitCount > 40) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if (Prebot.Broodwar.getFrameCount() > 80000 && factoryUnitCount > 80) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if ((Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 40)) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}

		}

		// 앞마당 이후
		else if (RealCCcnt >= 2) {

			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
			if (Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 120) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			// 공격시 돈 250 넘으면 멀티하기
			if (OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && Prebot.Broodwar.self().minerals() > 250) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			// 800 넘으면 멀티하기
			if (Prebot.Broodwar.self().minerals() > 800) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}

			// 500 넘고 유리하면
			if (Prebot.Broodwar.self().minerals() > 500 && factoryUnitCount > 50 && Attackpoint > 30) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			// 200 넘고 유리하면
			if (Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 80 && Attackpoint > 40 && ExpansionPoint >= 0) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			// 공격시 돈 250 넘으면 멀티하기

			if (Prebot.Broodwar.getFrameCount() > 14000) {

				if (factoryUnitCount > 80 && Prebot.Broodwar.self().deadUnitCount() - Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture) < 12) {
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					}
				}
			}

			int temp = 0;
			for (Unit units : Prebot.Broodwar.self().getUnits()) {
				if (units.getType() == UnitType.Terran_Command_Center && units.isCompleted()) {
					temp += WorkerManager.Instance().getWorkerData().getMineralsSumNearDepot(units);
				}
			}
			if (temp < 8000 && OldCombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			if (factoryUnitCount > 160) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
		}
	}

	private int getTotDeadCombatUnits() {

		int res = 0;

		int totmarine = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Marine);
		int tottank = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
		int totgoliath = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Goliath);
		int totvulture = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture);
		int totwraith = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith);
		int totvessel = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Science_Vessel);

		res = tottank + totgoliath + totvulture + totwraith + totvessel;
		res = res * 2 + totmarine;

		return res * 2;// 스타에서는 두배
	}

	private int getTotKilledCombatUnits() {

		int res = 0;

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			int totbio = 0;

			int tottank = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
			int totgoliath = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Goliath);
			int totvulture = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture);
			int totwraith = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith);
			int totvessel = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Science_Vessel);
			totbio = Prebot.Broodwar.self().killedUnitCount(UnitType.Terran_Marine) + Prebot.Broodwar.self().killedUnitCount(UnitType.Terran_Firebat)
					+ Prebot.Broodwar.self().killedUnitCount(UnitType.Terran_Medic);

			res = tottank + totgoliath + totvulture + totwraith + totvessel;
			res = res * 2 + totbio;

			return res * 2;

		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {

			int totzerling = 0;
			int tothydra = 0;
			int totmutal = 0;
			int totoverload = 0;
			int totlurker = 0;

			// tot = MyBotModule.Broodwar.self().killedUnitCount(UnitType.AllUnits);
			totzerling = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Zergling);
			tothydra = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Hydralisk);
			totlurker = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Lurker) + Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Lurker_Egg);
			totmutal = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Mutalisk);
			totoverload = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Overlord);
			int totguardian = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Guardian);
			int totultra = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Ultralisk);
			int totsunken = Prebot.Broodwar.self().killedUnitCount(UnitType.Zerg_Sunken_Colony);

			res = totzerling + +totsunken * 2 + tothydra * 2 + totmutal * 4 + totoverload * 3 + totlurker * 5 + totguardian * 8 + totultra * 10;
			return res; // 저그는 저글링 때문에 이미 2배 함

		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {

			// tot = MyBotModule.Broodwar.self().killedUnitCount();

			int totzealot = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_Zealot);
			int totdragoon = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_Dragoon);
			int totarchon = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_Archon);
			int totphoto = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_Photon_Cannon);
			int tothigh = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_High_Templar);
			int totdark = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_Dark_Templar);
			int totcarrier = Prebot.Broodwar.self().killedUnitCount(UnitType.Protoss_Carrier);

			res = (totzealot + totdragoon + totphoto + tothigh + totdark) * 2 + totarchon * 4 + totcarrier * 8;

			return res * 2;
		} else {
			return 0;
		}
	}

	private boolean enemyExspansioning() {
		List<UnitInfo> enemyCC = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL, InformationManager.Instance().getBasicResourceDepotBuildingType(InformationManager.Instance().enemyRace));

		for (UnitInfo target : enemyCC) {
			if (target.isCompleted()) {
				return true;
			}
		}
		return false;
	}

	private int selfExspansioning() {

		int cnt = 0;
		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
			if (unit == null)
				continue;
			if (unit.getType() == UnitType.Terran_Command_Center) {
				if (unit.isCompleted() == false) {
					cnt++;
				} else if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) < 7) {
					cnt++;
				}
			}
		}
		return cnt;
	}

	private int getValidMineralsForExspansionNearDepot(Unit depot) {
		if (depot == null) {
			return 0;
		}

		int mineralsNearDepot = 0;

		for (Unit unit : Prebot.Broodwar.getAllUnits()) {
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 450 && unit.getResources() > 200) {
				mineralsNearDepot++;
			}
		}

		return mineralsNearDepot;
	}


}
