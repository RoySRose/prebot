package prebot.strategy;

import java.util.List;

import bwapi.Order;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.common.MapGrid;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

//	int MutalStrategyOnTime;
//	public Boolean MutalStrategyOnTimeChecker;
//	int CarrierStrategyToBasicOnTime;
//	public Boolean hydraStrategy;
//	public Boolean mutalStrategy;
//	public Boolean randomchecker;
//
//	private static AnalyzeStrategy instance = new AnalyzeStrategy();
//
//	public static AnalyzeStrategy Instance() {
//		return instance;
//	}
//
//	public AnalyzeStrategy() {
//
//		CarrierStrategyToBasicOnTime = 0;
//		MutalStrategyOnTime = 0;
//		hydraStrategy = false;
//		mutalStrategy = false;
//		MutalStrategyOnTimeChecker = false;
//		randomchecker = true;
//	}
//
//	public void update() {
//		if (randomchecker && Prebot.Broodwar.enemy().getRace() == Race.Unknown && InformationManager.Instance().enemyRace != Race.Unknown) {
//			AnalyzeEnemyStrategyInit();
//			randomchecker = false;
//		}
//
//		// 최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행
//		if (InformationManager.Instance().enemyRace == Race.Protoss) {
//			AnalyzeVsProtoss();
//		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
//			AnalyzeVsTerran();
//		} else {
//			AnalyzeVsZerg();
//		}
//	}
//
//	public void AnalyzeEnemyStrategyInit() {
//
//		// 최초 각 종족별 Basic 세팅
//		// Exception 은기본 init 처리
//		StrategyManager.Instance().setCurrentStrategyException(EnemyStrategyException.INIT);
//		if (InformationManager.Instance().enemyRace == Race.Protoss) {
//			StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.PROTOSSBASIC);
//		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
//			StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.TERRANBASIC);
//		} else {
//			StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.ZERGBASIC);
//		}
//
//	}
//
//	private void AnalyzeVsProtoss() {
//		EnemyStrategyException strategyEx = StrategyManager.Instance().getCurrentStrategyException();
//		EnemyStrategy strategy = StrategyManager.Instance().getCurrentStrategyBasic();
//
//		if (strategyEx != EnemyStrategyException.PROTOSSEXCEPTION_DARK) {
//			if (TimeUtils.before(10000)) {
//				if (Prebot.Broodwar.enemy().incompleteUnitCount(UnitType.Protoss_Nexus) >= 1
//						|| Prebot.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Nexus) >= 2) {
//					strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS;
//				}
//
//				// 포톤이나 포지를 보았을때
//				if (InfoUtils.enemyNumUnits(UnitType.Protoss_Photon_Cannon) >= 1
//						|| InfoUtils.enemyNumUnits(UnitType.Protoss_Forge) >= 1
//						|| InfoUtils.enemyNumUnits(UnitType.Protoss_Pylon) >= 1) {
//					// 1. 본진에 적 포톤캐논이 있는지 본다.
//					List<Unit> enemysInMyBase = UnitUtils.getUnitsInRegion(CommonCode.RegionType.MY_BASE, PlayerRange.ENEMY);
//					for (Unit enemy : enemysInMyBase) {
//						if (enemy.getType() == UnitType.Protoss_Photon_Cannon) {
//							strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH;
//							break;
//						}
//					}
//					
//					// 2. 앞마당 지역은 익셉션이 포톤러쉬가 아닌 상태면 찾아본다.
//					if (strategyEx != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
//						List<Unit> enemysInMyFirstExpansion = UnitUtils.getUnitsInRegion(CommonCode.RegionType.MY_FIRST_EXPANSION, PlayerRange.ENEMY);
//						for (Unit enemy : enemysInMyFirstExpansion) {
//							if (enemy.getType() == UnitType.Protoss_Photon_Cannon) {
//								strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH;
//								break;
//							}
//						}
//					}
//
//					if (strategyEx != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
//						if (InfoUtils.enemyFirstExpansion() != null) {
//							List<Unit> enemysInEnemyFirstExpansion = UnitUtils.getUnitsInRegion(CommonCode.RegionType.ENEMY_FIRST_EXPANSION, PlayerRange.ENEMY);
//							for (Unit enemy : enemysInEnemyFirstExpansion) {
//								if (enemy.getType() == UnitType.Protoss_Photon_Cannon || enemy.getType() == UnitType.Protoss_Forge) {
//									strategy = EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO;
//									break;
//								}
//							}
//						}
//					}
//					// 포톤 or 포지를 보았는데 우리지역이 아니라면 더블넥서스다.
//					if (strategyEx != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH
//							&& InfoUtils.enemyNumUnits(UnitType.Protoss_Photon_Cannon) >= 1) {
//						strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS;
//					}
//				}
//			}
//
//			if (strategy != EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO) {
//				if (TimeUtils.before(8500) && (strategyEx != EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS && strategyEx != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH)) {
//					if (TimeUtils.before(6000) && InformationManager.Instance().isFirstScoutAlive()) {
//						// 4300 프레임이전에
//						if (InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 2
//								&& (InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator) == 0 || InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) == 0)) {
//							// 어시밀레이터나 코어전에 2게이트라면 질럿 푸시
//							TilePosition center = TilePositionUtils.getCenterTilePosition();
//
//							List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.VISIBLE, UnitType.Protoss_Gateway, UnitType.Protoss_Pylon);
//							for (UnitInfo gatewayOrPlyon : euiList) {
//								if (center.getDistance(gatewayOrPlyon.getLastPosition().toTilePosition()) < 600) {
//									RespondToStrategy.Instance().center_gateway = true;
//								}
//							}
//
//							strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT;
//						}
//
//						if (InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 1 &&
//								(InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) >= 1 || InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator) == 1)) {
//							// 원게이트에서 코어 올라가면 드래군 레디
//							strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON;
//						}
//					}
//
//					// 레디투드래군 상태에서 정찰 scv가 죽으면 드래군 푸시
//					if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT) {
//						if (!InformationManager.Instance().isFirstScoutAlive()) {
//							strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
//						}
//					}
//					if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
//						if (!InformationManager.Instance().isFirstScoutAlive()) {
//							strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
//						}
//					}
//					if ((InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 2
//							&& InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) >= 1)
//							&& InfoUtils.enemyNumUnits(UnitType.Protoss_Citadel_of_Adun) == 0) {
//						// 원게이트에서 코어 올라가면 드래군 레디
//						strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
//					}
//
//					// 푸시 판단
//					if (InfoUtils.enemySecondChoke() != null && TimeUtils.before(8500)) {
//						List<Unit> chkPush = Prebot.Broodwar.getUnitsInRadius(InfoUtils.enemySecondChoke().getCenter(), 400);
//						int temp = 0;
//						for (Unit enemy : chkPush) {
//							if (enemy.getType() == UnitType.Protoss_Dragoon) {
//								strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
//								break;
//							}
//							if (enemy.getType() == UnitType.Protoss_Zealot) {
//								temp++;
//							}
//							if (temp > 3) {
//								strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
//								break;
//							}
//						}
//					}
//
//					int dragooncnt = 0;
//					int zealotcnt = 0;
//
//					bwapi.Position checker = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
//					List<Unit> eniemies = MapGrid.Instance().getUnitsNear(checker, 500, false, true, null);
//					for (Unit enemy : eniemies) {
//						if (enemy.getType() == UnitType.Protoss_Dragoon) {
//							dragooncnt++;
//						}
//						if (enemy.getType() == UnitType.Protoss_Zealot) {
//							zealotcnt++;
//						}
//					}
//					if (dragooncnt > 2) {
//						strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
//					}
//					if (zealotcnt > 3) {
//						strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
//					}
//
//				}
//
//				if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT
//						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT
//						|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
//						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH) {
//					if (TimeUtils.after(7000)) {
//						if (InfoUtils.enemyNumUnits(UnitType.Protoss_Zealot)
//								+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Zealot) < 4) {
//							if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//									|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
//
//							} else {
//								RespondToStrategy.Instance().prepareDark = true;
//							}
//						}
//					}
//
//					if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
//						strategyEx = EnemyStrategyException.INIT;
//					}
//				}
//
//				if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
//						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
//						|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
//						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH) {
//					if (TimeUtils.after(7000)) {
//						if (InfoUtils.enemyNumUnits(UnitType.Protoss_Dragoon)
//								+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Dragoon) < 3) {
//							if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//									|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
//
//							} else {
//								RespondToStrategy.Instance().prepareDark = true;
//							}
//						}
//					}
//					if (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//							+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
//						strategyEx = EnemyStrategyException.INIT;
//					}
//				}
//			}
//		}
//
//		if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
//				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH)
//		{
//			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//		}
//		if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
//				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
//			if (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//		}
//
//		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//				|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
//			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 2 && UnitUtils.myFactoryUnitSupplyCount() > 64) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//		}
//
//		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH
//				|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
//			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//		}
//
//		if (RespondToStrategy.Instance().enemy_scout == false && InfoUtils.enemyNumUnits(UnitType.Protoss_Scout) >= 1) {
//			strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_SCOUT;
//		}
//
//		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_SCOUT
//				|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_SCOUT) {
//			if (InfoUtils.myNumUnits(UnitType.Terran_Goliath) >= 2) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//		}
//
//		if (RespondToStrategy.Instance().enemy_shuttle == false && InfoUtils.enemyNumUnits(UnitType.Protoss_Shuttle) >= 1) {
//			strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_SHUTTLE;
//		}
//
//		if (RespondToStrategy.Instance().enemy_arbiter == false
//				&& (InfoUtils.enemyNumUnits(UnitType.Protoss_Arbiter) >= 1 || InfoUtils.enemyNumUnits(UnitType.Protoss_Arbiter_Tribunal) >= 1)) {
//			strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_ARBITER;
//		}
//		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ARBITER
//				|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_ARBITER) {
//			if (InfoUtils.myNumUnits(UnitType.Terran_Goliath) >= 4) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//		}
//
//		if (RespondToStrategy.Instance().enemy_dark_templar == false
//				&& (InfoUtils.enemyNumUnits(UnitType.Protoss_Templar_Archives) >= 1
//						|| InfoUtils.enemyNumUnits(UnitType.Protoss_Citadel_of_Adun) >= 1
//						|| InfoUtils.enemyNumUnits(UnitType.Protoss_Dark_Templar) >= 1)) {
//
//			strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DARK;
//		}
//		if (RespondToStrategy.Instance().enemy_dark_templar == false) {
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
//					strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_DARK;
//				}
//			}
//		}
//
//		if (strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_DARK
//				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DARK) {
//
//			boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
//			boolean firstChokeTurret = RespondToStrategy.Instance().firstChokeTurret;
//
//			if (mainBaseTurret && firstChokeTurret) {
//				strategyEx = EnemyStrategyException.INIT;
//			}
//
//		}
//
//		if (TimeUtils.before(6000)) {
//			if (InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 2
//					&& (InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator) == 0 || InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) == 0)) {
//				TilePosition center = TilePositionUtils.getCenterTilePosition();
//				for (Unit gateway : Prebot.Broodwar.enemy().getUnits()) {
//					if (gateway.getType() == UnitType.Protoss_Gateway || gateway.getType() == UnitType.Protoss_Pylon) {
//						if (center.getDistance(gateway.getTilePosition()) < 17) {
//							RespondToStrategy.Instance().center_gateway = true;
//						}
//					}
//				}
//
//				strategyEx = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
//			}
//		}
//
//		if (strategyEx != null) {
//			StrategyManager.Instance().setCurrentStrategyException(strategyEx);
//		}
//
//		if ((InfoUtils.enemyNumUnits(UnitType.Protoss_Fleet_Beacon) >= 1 && InfoUtils.enemyNumUnits(UnitType.Protoss_Carrier) >= 1)
//				|| (TimeUtils.before(10000) && InfoUtils.enemyNumUnits(UnitType.Protoss_Stargate) >= 1)) {
//			if (InfoUtils.enemyNumUnits(UnitType.Protoss_Carrier) >= 1) {
//				CarrierStrategyToBasicOnTime = Prebot.Broodwar.getFrameCount() + 2500;
//			}
//
//			strategy = EnemyStrategy.PROTOSSBASIC_CARRIER;
//		}
//
//		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//				|| strategyEx == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
//			if (InfoUtils.enemyNumUnits(UnitType.Protoss_Photon_Cannon) >= 4) {
//				strategy = EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO;
//			}
//		}
//
//		if (StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.PROTOSSBASIC_CARRIER) {
//			if (InfoUtils.enemyNumUnits(UnitType.Protoss_Carrier) == 0) {
//				if (CarrierStrategyToBasicOnTime != 0 && CarrierStrategyToBasicOnTime > 0 && TimeUtils.after(CarrierStrategyToBasicOnTime)) {
//					strategy = EnemyStrategy.PROTOSSBASIC;
//				}
//			} else {
//				CarrierStrategyToBasicOnTime = Prebot.Broodwar.getFrameCount() + 2500;
//			}
//		}
//
//		if (strategy != null) {
//			StrategyManager.Instance().setCurrentStrategyBasic(strategy);
//		}
//
//	}
//
//	boolean after = false;
//
//	private void AnalyzeVsTerran() {
//		EnemyStrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
//		EnemyStrategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();
//
//		if (after == false) {
//			if (InfoUtils.enemyNumUnits(UnitType.Terran_Barracks) == 1
//					&& InfoUtils.enemyNumUnits(UnitType.Terran_Refinery) == 1) {
//
//				selectedS = EnemyStrategy.TERRANBASIC_MECHANIC;
//			}
//
//			if (InfoUtils.enemyNumUnits(UnitType.Terran_Marine) >= 3
//					|| InfoUtils.enemyNumUnits(UnitType.Terran_Barracks) >= 2) {
//
//				selectedS = EnemyStrategy.TERRANBASIC_BIONIC;
//			}
//
//			if (InfoUtils.enemyNumUnits(UnitType.Terran_Factory) >= 1
//					|| InfoUtils.enemyNumUnits(UnitType.Terran_Machine_Shop) >= 1
//					|| InfoUtils.enemyNumUnits(UnitType.Terran_Vulture) >= 1
//					|| InfoUtils.enemyNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode) >= 1
//					|| InfoUtils.enemyNumUnits(UnitType.Terran_Goliath) >= 1
//					|| InfoUtils.enemyNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode) >= 1) {
//				selectedS = EnemyStrategy.TERRANBASIC_MECHANIC;
//			}
//		}
//
//		// if
//		// (InfoUtils.enemyNumUnits(UnitType.Terran_Battlecruiser,InformationManager.Instance().enemyPlayer)
//		// >= 1
//		// ||
//		// InfoUtils.enemyNumUnits(UnitType.Terran_Physics_Lab,InformationManager.Instance().enemyPlayer)
//		// >= 1){
//		// selectedS = Strategys.terranBasic_BattleCruiser;
//		// }
//
//		if (after == false && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 3) {
//			if (StrategyManager.Instance().getCurrentStrategyBasic() != EnemyStrategy.TERRANBASIC_BIONIC) {
//				selectedS = EnemyStrategy.TERRANBASIC_MECHANICAFTER;
//				after = true;
//			}
//		}
//		if (after) {
//			selectedS = EnemyStrategy.TERRANBASIC_MECHANICAFTER;
//		}
//		if (InfoUtils.enemyNumUnits(UnitType.Terran_Starport) >= 1
//				|| InfoUtils.enemyNumUnits(UnitType.Terran_Wraith) >= 1) {
//			selectedS = EnemyStrategy.TERRANBASIC_MECHANICWITHWRAITH;
//		}
//
//		if (RespondToStrategy.Instance().enemy_wraithcloak == false
//				&& InfoUtils.enemyNumUnits(UnitType.Terran_Control_Tower) >= 1) {
//			selectedSE = EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK;
//		}
//
//		if (RespondToStrategy.Instance().enemy_wraithcloak == false) {
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)) {
//					selectedSE = EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK;
//				}
//			}
//		}
//
//		if (selectedSE == EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK
//				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK) {
//			if (InfoUtils.myNumUnits(UnitType.Terran_Comsat_Station) >= 1) {
//				selectedSE = EnemyStrategyException.INIT;
//			}
//		}
//
//		if (selectedSE != null) {
//			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
//		}
//
//		// if(MyBotModule.Broodwar.getFrameCount() > 18000){
//		// selectedS = Strategys.terranBasic_BattleCruiser;
//		// }
//		if (selectedS != null) {
//			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
//		}
//	}
//
//	private void AnalyzeVsZerg() {
//		EnemyStrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
//		EnemyStrategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();
//
//		int cntHatchery = InfoUtils.enemyNumUnits(UnitType.Zerg_Hatchery);
//		int cntHive = InfoUtils.enemyNumUnits(UnitType.Zerg_Hive);
//		int ling_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling);
//		int hydra_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk);
//		int lurker_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Lurker) + InfoUtils.enemyNumUnits(UnitType.Zerg_Lurker_Egg);
//		int ultra_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Ultralisk);
//
//		if (cntHive >= 1 || ultra_cnt > 0) {
//			selectedSE = EnemyStrategyException.ZERGEXCEPTION_HIGHTECH;
//		}
//
//		if (selectedSE == EnemyStrategyException.ZERGEXCEPTION_HIGHTECH && InfoUtils.myNumUnits(UnitType.Terran_Science_Vessel) > 3) {
//			selectedSE = EnemyStrategyException.INIT;
//		}
//
//		// hydra check with lurker
//		if (InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk_Den) >= 1 || hydra_cnt >= 1 || lurker_cnt >= 1) {
//			selectedS = EnemyStrategy.ZERGBASIC_HYDRAWAVE;
//			if (RespondToStrategy.Instance().enemy_lurker == false && InfoUtils.enemyNumUnits(UnitType.Zerg_Lair) >= 1) {
//				selectedSE = EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER;
//			}
//			hydraStrategy = true;
//		}
//		// end hydra exception
//
//		// mutal check with time
//		if (InfoUtils.enemyNumUnits(UnitType.Zerg_Spire) >= 1
//				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) >= 1
//				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Greater_Spire) >= 1
//				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Guardian) >= 1
//				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Devourer) >= 1
//				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Scourge) >= 1
//				|| (!hydraStrategy && InfoUtils.enemyNumUnits(UnitType.Zerg_Lair) >= 1)) {
//
//			selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
//
//			if ((InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18) || InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 24) {
//				selectedS = EnemyStrategy.ZERGBASIC_MUTALMANY;
//			}
//
//			if (mutalStrategy == false && MutalStrategyOnTimeChecker == false) {
//				MutalStrategyOnTime = Prebot.Broodwar.getFrameCount() + 3000; // 5000 frame동안 일단 뮤탈
//			}
//			mutalStrategy = true;
//		}
//
//		if (hydra_cnt == 0 && lurker_cnt == 0 && mutalStrategy == false && TimeUtils.after(8000)) {
//			selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
//		}
//		// mutal exception
//
//		// lurker in 조건
//		if (RespondToStrategy.Instance().enemy_lurker == false) {
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				// 인비저블 유닛이 있다면 일단 럴커 대비 로직
//				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
//					// StrategyManager.Instance().setCurrentStrategyException(StrategysException.protossException_Dark);
//					selectedSE = EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER;
//				}
//			}
//			if (lurker_cnt > 0) {
//				selectedSE = EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER;
//			}
//		}
//
//		// 러커 out 조건
//		if (RespondToStrategy.Instance().enemy_lurker == false && selectedSE == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER
//				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER) {
//
//			boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
//			boolean firstChokeTurret = RespondToStrategy.Instance().firstChokeTurret;
//
//			if (mainBaseTurret && firstChokeTurret) {
//				selectedSE = EnemyStrategyException.INIT;
//			}
//		}
//
//		// TODO 상대 업글 감지 가능?
//		if (mutalStrategy == false && hydraStrategy == false && cntHatchery >= 3 && Prebot.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost) == 1) {
//			// System.out.println("저글링 업글 상태: " +
//			// MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost));
//			selectedSE = EnemyStrategyException.ZERGEXCEPTION_ONLYLING;
//		}
//		if (selectedSE == EnemyStrategyException.ZERGEXCEPTION_ONLYLING
//				&& (InfoUtils.myNumUnits(UnitType.Terran_Vulture) > 5 || mutalStrategy == true
//						|| hydraStrategy == true)) {
//			selectedSE = EnemyStrategyException.INIT;
//		}
//
//		if (selectedSE != null) {
//			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
//		}
//
//		if (mutalStrategy && hydraStrategy) { // Mutal과 히드라 중에 고른다
//
//			// System.out.print(", choose betwee H,M: " );
//
//			if (MutalStrategyOnTime != 0 && TimeUtils.before(MutalStrategyOnTime)) {
//				selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
//				// System.out.print(", chose M1" );
//			} else {
//				if (InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) >= (hydra_cnt * 1.4 + lurker_cnt * 0.9)) {
//					selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
//					// System.out.print(", chose M2" );
//					if ((InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18) || InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 24) {
//						selectedS = EnemyStrategy.ZERGBASIC_MUTALMANY;
//					}
//				} else if (InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) >= (hydra_cnt * 0.7 + lurker_cnt * 0.45)) {
//					selectedS = EnemyStrategy.ZERGBASIC_HYDRAMUTAL;
//					// System.out.print(", chose H1" );
//				} else {
//					selectedS = EnemyStrategy.ZERGBASIC_HYDRAWAVE;
//					// System.out.print(", chose H2" );
//				}
//			}
//		}
//
//		if (selectedS == EnemyStrategy.ZERGBASIC_HYDRAWAVE && ling_cnt > 12) {
//			selectedS = EnemyStrategy.ZERGBASIC_LINGHYDRA;
//		}
//
//		if (selectedS == EnemyStrategy.ZERGBASIC_MUTAL && ling_cnt > 12) {
//			selectedS = EnemyStrategy.ZERGBASIC_LINGMUTAL;
//		}
//
//		if (selectedS != EnemyStrategy.ZERGBASIC_MUTALMANY && ultra_cnt > 0) {
//			selectedS = EnemyStrategy.ZERGBASIC_ULTRA;
//		}
//
//		if (selectedS == EnemyStrategy.ZERGBASIC_ULTRA && ling_cnt > 12) {
//			selectedS = EnemyStrategy.ZERGBASIC_LINGULTRA;
//		}
//
//		if (selectedS == EnemyStrategy.ZERGBASIC_HYDRAWAVE && (hydra_cnt + lurker_cnt > 0)) {
//			MutalStrategyOnTimeChecker = true;
//		}
//
//		// System.out.println();
//		// TODO 필요할지 확인 zergBasic_GiftSet
//		// TODO 필요할지 확인 zergBasic_Guardian
//
//		if (selectedS != null) {
//			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
//		}
//	}

}