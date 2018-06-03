package prebot.strategy;

import java.util.List;

import bwapi.Order;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.common.MapGrid;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.PositionRegion;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.constant.StrategyCode.GameMap;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

	int MutalStrategyOnTime;
	public Boolean MutalStrategyOnTimeChecker;
	int CarrierStrategyToBasicOnTime;
	public Boolean hydraStrategy;
	public Boolean mutalStrategy;
	public Boolean randomchecker;

	private static AnalyzeStrategy instance = new AnalyzeStrategy();

	public static AnalyzeStrategy Instance() {
		return instance;
	}

	public AnalyzeStrategy() {

		CarrierStrategyToBasicOnTime = 0;
		MutalStrategyOnTime = 0;
		hydraStrategy = false;
		mutalStrategy = false;
		MutalStrategyOnTimeChecker = false;
		randomchecker = true;
	}

	public void AnalyzeEnemyStrategy() {
		// config 건물 관련 여백 조정용 함수
		// 건물 여백이 0인채로 서플 2개째를 짓고있으면 원래대로 원복
		// if (Config.BuildingSpacing == 0 &&
		// InformationManager.Instance().getNumUnits(UnitType.Terran_Supply_Depot,
		// MyBotModule.Broodwar.self()) >= 2) {
		// BlockingEntrance.Instance().ReturnBuildSpacing();
		// }

		if (randomchecker == true && Prebot.Broodwar.enemy().getRace() == Race.Unknown && InformationManager.Instance().enemyRace != Race.Unknown) {
			AnalyzeEnemyStrategyInit();
			randomchecker = false;
		}

		// 최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			AnalyzeVsProtoss();
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
			AnalyzeVsTerran();
		} else {
			AnalyzeVsZerg();
		}

		if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.LOST_TEMPLE) {
			if (TimeUtils.after(25000)) {

				Boolean checkisland = true;
				List<BaseLocation> occupiedBaseLocation = InfoUtils.enemyOccupiedBases();

				// 섬 지역 아닌곳에 상대 유닛이 있으면 패스
				for (BaseLocation base : occupiedBaseLocation) {
					if (base.isIsland() == false) {
						checkisland = false;
					}
				}

				if (checkisland == true) {
					for (BaseLocation base : occupiedBaseLocation) {
						List<Unit> enemy = MapGrid.Instance().getUnitsNear(base.getPosition(), 600, false, true, null);

						if (enemy.size() > 0) {
							BaseLocation enemymainbase = InfoUtils.enemyBase();
							if (enemymainbase.isIsland()) {
								StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.ATTACKISLAND);
							}
						}
					}
				}
			}
		}

	}

	public void AnalyzeEnemyStrategyInit() {

		// 최초 각 종족별 Basic 세팅
		// Exception 은기본 init 처리
		StrategyManager.Instance().setCurrentStrategyException(EnemyStrategyException.INIT);
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.PROTOSSBASIC);
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
			StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.TERRANBASIC);
		} else {
			StrategyManager.Instance().setCurrentStrategyBasic(EnemyStrategy.ZERGBASIC);
		}

	}

	private void AnalyzeVsProtoss() {
		EnemyStrategyException strategyException = StrategyManager.Instance().getCurrentStrategyException();
		EnemyStrategy strategy = StrategyManager.Instance().getCurrentStrategyBasic();

		if (strategyException != EnemyStrategyException.PROTOSSEXCEPTION_DARK) {
			if (TimeUtils.before(10000)) {
				if (Prebot.Broodwar.enemy().incompleteUnitCount(UnitType.Protoss_Nexus) >= 1 || Prebot.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Nexus) >= 2) {
					strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS;
				}

				// 포톤이나 포지를 보았을때
				if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Forge, InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Pylon, InformationManager.Instance().enemyPlayer) >= 1) {
					// 1. 본진에 적 포톤캐논이 있는지 본다.
					List<Unit> enemysInMyBase = UnitUtils.getUnitsInRegion(PositionRegion.MY_BASE, PlayerRange.ENEMY);
					for (Unit enemy : enemysInMyBase) {
						if (enemy.getType() == UnitType.Protoss_Photon_Cannon) {
							strategyException = EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH;
							break;
						}
					}
					
					// 2. 앞마당 지역은 익셉션이 포톤러쉬가 아닌 상태면 찾아본다.
					if (strategyException != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
						List<Unit> enemysInMyFirstExpansion = UnitUtils.getUnitsInRegion(PositionRegion.MY_FIRST_EXPANSION, PlayerRange.ENEMY);
						for (Unit enemy : enemysInMyFirstExpansion) {
							if (enemy.getType() == UnitType.Protoss_Photon_Cannon) {
								strategyException = EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH;
								break;
							}
						}
					}

					if (strategyException != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
						if (InfoUtils.enemyFirstExpansion() != null) {
							List<Unit> enemysInEnemyFirstExpansion = UnitUtils.getUnitsInRegion(PositionRegion.ENEMY_FIRST_EXPANSION, PlayerRange.ENEMY);
							for (Unit enemy : enemysInEnemyFirstExpansion) {
								if (enemy.getType() == UnitType.Protoss_Photon_Cannon || enemy.getType() == UnitType.Protoss_Forge) {
									strategy = EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO;
									break;
								}
							}
						}
					}
					// 포톤 or 포지를 보았는데 우리지역이 아니라면 더블넥서스다.
					if (strategyException != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) >= 1) {
						strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS;
					}
				}
			}

			if (strategy != EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO) {
				if (TimeUtils.before(8500) && (strategyException != EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS && strategyException != EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH)) {
					if (TimeUtils.before(6000) && InformationManager.Instance().isFirstScoutAlive()) {
						// 4300 프레임이전에
						if (InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 2
								&& (InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator) == 0 || InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) == 0)) {
							// 어시밀레이터나 코어전에 2게이트라면 질럿 푸시
							TilePosition center = TilePositionUtils.getCenterTilePosition();

							List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, UnitType.Protoss_Gateway, UnitType.Protoss_Pylon);
							for (UnitInfo gatewayOrPlyon : euiList) {
								if (center.getDistance(gatewayOrPlyon.getLastPosition().toTilePosition()) < 600) {
									RespondToStrategy.Instance().center_gateway = true;
								}
							}

							strategyException = EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT;
						}

						if (InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 1 &&
								(InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) >= 1 || InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator) == 1)) {
							// 원게이트에서 코어 올라가면 드래군 레디
							strategyException = EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON;
						}
					}

					// 레디투드래군 상태에서 정찰 scv가 죽으면 드래군 푸시
					if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT) {
						if (!InformationManager.Instance().isFirstScoutAlive()) {
							strategyException = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
						}
					}
					if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
						if (!InformationManager.Instance().isFirstScoutAlive()) {
							strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
						}
					}
					if ((InfoUtils.enemyNumUnits(UnitType.Protoss_Gateway) >= 2
							&& InfoUtils.enemyNumUnits(UnitType.Protoss_Cybernetics_Core) >= 1)
							&& InfoUtils.enemyNumUnits(UnitType.Protoss_Citadel_of_Adun) == 0) {
						// 원게이트에서 코어 올라가면 드래군 레디
						strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
					}

					// 푸시 판단
					if (InfoUtils.enemySecondChoke() != null && Prebot.Broodwar.getFrameCount() < 8500) {
						List<Unit> chkPush = Prebot.Broodwar.getUnitsInRadius(InfoUtils.enemySecondChoke().getCenter(), 400);
						int temp = 0;
						for (Unit enemy : chkPush) {
							if (enemy.getType() == UnitType.Protoss_Dragoon) {
								strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
								break;
							}
							if (enemy.getType() == UnitType.Protoss_Zealot) {
								temp++;
							}
							if (temp > 3) {
								strategyException = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
								break;
							}
						}
					}

					int dragooncnt = 0;
					int zealotcnt = 0;

					bwapi.Position checker = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
					List<Unit> eniemies = MapGrid.Instance().getUnitsNear(checker, 500, false, true, null);
					for (Unit enemy : eniemies) {
						if (enemy.getType() == UnitType.Protoss_Dragoon) {
							dragooncnt++;
						}
						if (enemy.getType() == UnitType.Protoss_Zealot) {
							zealotcnt++;
						}
					}
					if (dragooncnt > 2) {
						strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
					}
					if (zealotcnt > 3) {
						strategyException = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
					}

				}

				if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT
						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT
						|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH) {
					if (7000 < Prebot.Broodwar.getFrameCount()) {
						if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot, InformationManager.Instance().enemyPlayer)
								+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Zealot) < 4) {
							if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
									|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {

							} else {
								RespondToStrategy.Instance().prepareDark = true;
							}
						}
					}

					if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
						strategyException = EnemyStrategyException.INIT;
					}
				}

				if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
						|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
						|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH) {
					if (7000 < Prebot.Broodwar.getFrameCount()) {
						if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon, InformationManager.Instance().enemyPlayer)
								+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Dragoon) < 3) {
							if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
									|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {

							} else {
								RespondToStrategy.Instance().prepareDark = true;
							}
						}
					}
					if (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
							+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
						strategyException = EnemyStrategyException.INIT;
					}
				}
			}
		}

		if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH)
		{
			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
				strategyException = EnemyStrategyException.INIT;
			}
		}
		if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
			if (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
				strategyException = EnemyStrategyException.INIT;
			}
		}

		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
				|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 2 && UnitUtils.myFactoryUnitSupplyCount() > 64) {
				strategyException = EnemyStrategyException.INIT;
			}
		}

		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH
				|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
					+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
				strategyException = EnemyStrategyException.INIT;
			}
		}

		if (RespondToStrategy.Instance().enemy_scout == false
				&& (InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer) >= 1)) {
			strategyException = EnemyStrategyException.PROTOSSEXCEPTION_SCOUT;
		}

		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_SCOUT
				|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_SCOUT) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer) >= 2) {
				strategyException = EnemyStrategyException.INIT;
			}
		}

		if (RespondToStrategy.Instance().enemy_shuttle == false
				&& (InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle, InformationManager.Instance().enemyPlayer) >= 1)) {
			strategyException = EnemyStrategyException.PROTOSSEXCEPTION_SHUTTLE;
		}

		if (RespondToStrategy.Instance().enemy_arbiter == false
				&& (InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter, InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter_Tribunal, InformationManager.Instance().enemyPlayer) >= 1)) {
			strategyException = EnemyStrategyException.PROTOSSEXCEPTION_ARBITER;
		}
		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ARBITER
				|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_ARBITER) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer) >= 4) {
				strategyException = EnemyStrategyException.INIT;
			}
		}

		if (RespondToStrategy.Instance().enemy_dark_templar == false
				&& (InformationManager.Instance().getNumUnits(UnitType.Protoss_Templar_Archives, InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Citadel_of_Adun, InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Dark_Templar, InformationManager.Instance().enemyPlayer) >= 1)) {

			strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DARK;
		}
		if (RespondToStrategy.Instance().enemy_dark_templar == false) {
			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
					strategyException = EnemyStrategyException.PROTOSSEXCEPTION_DARK;
				}
			}
		}

		if (strategyException == EnemyStrategyException.PROTOSSEXCEPTION_DARK
				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DARK) {

			boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
			boolean firstChokeTurret = RespondToStrategy.Instance().firstChokeTurret;

			if (mainBaseTurret && firstChokeTurret) {
				strategyException = EnemyStrategyException.INIT;
			}

		}

		if (Prebot.Broodwar.getFrameCount() < 6000) {
			if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway, InformationManager.Instance().enemyPlayer) >= 2
					&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Assimilator, InformationManager.Instance().enemyPlayer) == 0)
					|| (InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway, InformationManager.Instance().enemyPlayer) >= 2
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core, InformationManager.Instance().enemyPlayer) == 0)) {
				TilePosition center = new TilePosition(64, 64);
				for (Unit gateway : Prebot.Broodwar.enemy().getUnits()) {

					if (gateway.getType() == UnitType.Protoss_Gateway || gateway.getType() == UnitType.Protoss_Pylon) {
						if (center.getDistance(gateway.getTilePosition()) < 17) {
							RespondToStrategy.Instance().center_gateway = true;
						}
					}
				}

				strategyException = EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
			}
		}

		if (strategyException != null) {
			StrategyManager.Instance().setCurrentStrategyException(strategyException);
		}

		if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Fleet_Beacon, InformationManager.Instance().enemyPlayer) >= 1
				&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) >= 1)
				|| (Prebot.Broodwar.getFrameCount() < 10000 && InformationManager.Instance().getNumUnits(UnitType.Protoss_Stargate, InformationManager.Instance().enemyPlayer) >= 1)
		// && InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier,
		// InformationManager.Instance().enemyPlayer) >= 1)
		) {

			if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) >= 1) {
				CarrierStrategyToBasicOnTime = Prebot.Broodwar.getFrameCount() + 2500;
			}

			strategy = EnemyStrategy.PROTOSSBASIC_CARRIER;
		}

		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
				|| strategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
			if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon, InformationManager.Instance().enemyPlayer) >= 4) {
				strategy = EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO;
			}
		}

		if (StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.PROTOSSBASIC_CARRIER) {
			if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) == 0) {
				if (CarrierStrategyToBasicOnTime != 0 && CarrierStrategyToBasicOnTime > 0 && CarrierStrategyToBasicOnTime < Prebot.Broodwar.getFrameCount()) {
					strategy = EnemyStrategy.PROTOSSBASIC;
				}
			} else {
				CarrierStrategyToBasicOnTime = Prebot.Broodwar.getFrameCount() + 2500;
			}
		}

		if (strategy != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(strategy);
		}

	}

	boolean after = false;

	private void AnalyzeVsTerran() {
		EnemyStrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
		EnemyStrategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();

		if (after == false) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks, InformationManager.Instance().enemyPlayer) == 1
					&& InformationManager.Instance().getNumUnits(UnitType.Terran_Refinery, InformationManager.Instance().enemyPlayer) == 1) {

				selectedS = EnemyStrategy.TERRANBASIC_MECHANIC;
			}

			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Marine, InformationManager.Instance().enemyPlayer) >= 3
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks, InformationManager.Instance().enemyPlayer) >= 2) {

				selectedS = EnemyStrategy.TERRANBASIC_BIONIC;
			}

			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Machine_Shop, InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture, InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode, InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode, InformationManager.Instance().enemyPlayer) >= 1) {
				selectedS = EnemyStrategy.TERRANBASIC_MECHANIC;
			}
		}

		// if
		// (InformationManager.Instance().getNumUnits(UnitType.Terran_Battlecruiser,InformationManager.Instance().enemyPlayer)
		// >= 1
		// ||
		// InformationManager.Instance().getNumUnits(UnitType.Terran_Physics_Lab,InformationManager.Instance().enemyPlayer)
		// >= 1){
		// selectedS = Strategys.terranBasic_BattleCruiser;
		// }

		if (after == false && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 3) {
			if (StrategyManager.Instance().getCurrentStrategyBasic() != EnemyStrategy.TERRANBASIC_BIONIC) {
				selectedS = EnemyStrategy.TERRANBASIC_MECHANICAFTER;
				after = true;
			}
		}
		if (after) {
			selectedS = EnemyStrategy.TERRANBASIC_MECHANICAFTER;
		}
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Starport, InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith, InformationManager.Instance().enemyPlayer) >= 1) {
			selectedS = EnemyStrategy.TERRANBASIC_MECHANICWITHWRAITH;
		}

		if (RespondToStrategy.Instance().enemy_wraithcloak == false
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Control_Tower, InformationManager.Instance().enemyPlayer) >= 1) {
			selectedSE = EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK;
		}

		if (RespondToStrategy.Instance().enemy_wraithcloak == false) {
			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)) {
					selectedSE = EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK;
				}
			}
		}

		if (selectedSE == EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK
				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Comsat_Station, InformationManager.Instance().selfPlayer) >= 1) {
				selectedSE = EnemyStrategyException.INIT;
			}
		}

		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		// if(MyBotModule.Broodwar.getFrameCount() > 18000){
		// selectedS = Strategys.terranBasic_BattleCruiser;
		// }
		if (selectedS != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
		}
	}

	private void AnalyzeVsZerg() {
		EnemyStrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
		EnemyStrategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();

		int cntHatchery = InfoUtils.enemyNumUnits(UnitType.Zerg_Hatchery);
		int cntHive = InfoUtils.enemyNumUnits(UnitType.Zerg_Hive);
		int ling_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling);
		int hydra_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk);
		int lurker_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Lurker) + InfoUtils.enemyNumUnits(UnitType.Zerg_Lurker_Egg);
		int ultra_cnt = InfoUtils.enemyNumUnits(UnitType.Zerg_Ultralisk);

		if (cntHive >= 1 || ultra_cnt > 0) {
			selectedSE = EnemyStrategyException.ZERGEXCEPTION_HIGHTECH;
		}

		if (selectedSE == EnemyStrategyException.ZERGEXCEPTION_HIGHTECH && InfoUtils.myNumUnits(UnitType.Terran_Science_Vessel) > 3) {
			selectedSE = EnemyStrategyException.INIT;
		}

		// hydra check with lurker
		if (InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk_Den) >= 1 || hydra_cnt >= 1 || lurker_cnt >= 1) {
			selectedS = EnemyStrategy.ZERGBASIC_HYDRAWAVE;
			if (RespondToStrategy.Instance().enemy_lurker == false && InfoUtils.enemyNumUnits(UnitType.Zerg_Lair) >= 1) {
				selectedSE = EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER;
			}
			hydraStrategy = true;
		}
		// end hydra exception

		// mutal check with time
		if (InfoUtils.enemyNumUnits(UnitType.Zerg_Spire) >= 1
				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) >= 1
				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Greater_Spire) >= 1
				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Guardian) >= 1
				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Devourer) >= 1
				|| InfoUtils.enemyNumUnits(UnitType.Zerg_Scourge) >= 1
				|| (!hydraStrategy && InfoUtils.enemyNumUnits(UnitType.Zerg_Lair) >= 1)) {

			selectedS = EnemyStrategy.ZERGBASIC_MUTAL;

			if ((InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18) || InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 24) {
				selectedS = EnemyStrategy.ZERGBASIC_MUTALMANY;
			}

			if (mutalStrategy == false && MutalStrategyOnTimeChecker == false) {
				MutalStrategyOnTime = Prebot.Broodwar.getFrameCount() + 3000; // 5000
																				// frame동안
																				// 일단
																				// 뮤탈
			}
			mutalStrategy = true;
		}

		if (hydra_cnt == 0 && lurker_cnt == 0 && mutalStrategy == false && Prebot.Broodwar.getFrameCount() > 8000) {
			selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
		}
		// mutal exception

		// lurker in 조건
		if (RespondToStrategy.Instance().enemy_lurker == false) {
			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
				// 인비저블 유닛이 있다면 일단 럴커 대비 로직
				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
					// StrategyManager.Instance().setCurrentStrategyException(StrategysException.protossException_Dark);
					selectedSE = EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER;
				}
			}
			if (lurker_cnt > 0) {
				selectedSE = EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER;
			}
		}

		// 러커 out 조건
		if (RespondToStrategy.Instance().enemy_lurker == false && selectedSE == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER
				|| StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER) {

			boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
			boolean firstChokeTurret = RespondToStrategy.Instance().firstChokeTurret;

			if (mainBaseTurret && firstChokeTurret) {
				selectedSE = EnemyStrategyException.INIT;
			}
		}

		// TODO 상대 업글 감지 가능?
		if (mutalStrategy == false && hydraStrategy == false && cntHatchery >= 3 && Prebot.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost) == 1) {
			// System.out.println("저글링 업글 상태: " +
			// MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost));
			selectedSE = EnemyStrategyException.ZERGEXCEPTION_ONLYLING;
		}
		if (selectedSE == EnemyStrategyException.ZERGEXCEPTION_ONLYLING
				&& (InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture, InformationManager.Instance().selfPlayer) > 5 || mutalStrategy == true
						|| hydraStrategy == true)) {
			selectedSE = EnemyStrategyException.INIT;
		}

		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		if (mutalStrategy && hydraStrategy) { // Mutal과 히드라 중에 고른다

			// System.out.print(", choose betwee H,M: " );

			if (MutalStrategyOnTime != 0 && MutalStrategyOnTime > Prebot.Broodwar.getFrameCount()) {
				selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
				// System.out.print(", chose M1" );
			} else {
				if (InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) >= (hydra_cnt * 1.4 + lurker_cnt * 0.9)) {
					selectedS = EnemyStrategy.ZERGBASIC_MUTAL;
					// System.out.print(", chose M2" );
					if ((InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18) || InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) > 24) {
						selectedS = EnemyStrategy.ZERGBASIC_MUTALMANY;
					}
				} else if (InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk) >= (hydra_cnt * 0.7 + lurker_cnt * 0.45)) {
					selectedS = EnemyStrategy.ZERGBASIC_HYDRAMUTAL;
					// System.out.print(", chose H1" );
				} else {
					selectedS = EnemyStrategy.ZERGBASIC_HYDRAWAVE;
					// System.out.print(", chose H2" );
				}
			}
		}

		if (selectedS == EnemyStrategy.ZERGBASIC_HYDRAWAVE && ling_cnt > 12) {
			selectedS = EnemyStrategy.ZERGBASIC_LINGHYDRA;
		}

		if (selectedS == EnemyStrategy.ZERGBASIC_MUTAL && ling_cnt > 12) {
			selectedS = EnemyStrategy.ZERGBASIC_LINGMUTAL;
		}

		if (selectedS != EnemyStrategy.ZERGBASIC_MUTALMANY && ultra_cnt > 0) {
			selectedS = EnemyStrategy.ZERGBASIC_ULTRA;
		}

		if (selectedS == EnemyStrategy.ZERGBASIC_ULTRA && ling_cnt > 12) {
			selectedS = EnemyStrategy.ZERGBASIC_LINGULTRA;
		}

		if (selectedS == EnemyStrategy.ZERGBASIC_HYDRAWAVE && (hydra_cnt + lurker_cnt > 0)) {
			MutalStrategyOnTimeChecker = true;
		}

		// System.out.println();
		// TODO 필요할지 확인 zergBasic_GiftSet
		// TODO 필요할지 확인 zergBasic_Guardian

		if (selectedS != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
		}
	}

}