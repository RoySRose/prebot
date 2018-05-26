package prebot.common;

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
import bwta.Region;
import prebot.common.main.Prebot;
import prebot.common.util.MicroUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyManager;
import prebot.strategy.constant.EnemyStrategy.Strategy;
import prebot.strategy.constant.EnemyStrategy.StrategyException;
import prebot.strategy.constant.StrategyCode.GameMap;

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

		if (randomchecker == true && Prebot.Broodwar.enemy().getRace() == Race.Unknown
				&& InformationManager.Instance().enemyRace != Race.Unknown) {
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
			if (Prebot.Broodwar.getFrameCount() > 25000) {

				Boolean checkisland = true;
				List<BaseLocation> occupiedBaseLocation = InformationManager.Instance()
						.getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);

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
							BaseLocation enemymainbase = InformationManager.Instance()
									.getMainBaseLocation(InformationManager.Instance().enemyPlayer);
							if (enemymainbase.isIsland()) {
								StrategyManager.Instance()
										.setCurrentStrategyBasic(Strategy.ATTACKISLAND);
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
		StrategyManager.Instance().setCurrentStrategyException(StrategyException.INIT);
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			StrategyManager.Instance().setCurrentStrategyBasic(Strategy.PROTOSSBASIC);
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
			StrategyManager.Instance().setCurrentStrategyBasic(Strategy.TERRANBASIC);
		} else {
			StrategyManager.Instance().setCurrentStrategyBasic(Strategy.ZERGBASIC);
		}

	}

	private void AnalyzeVsProtoss() {
		StrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
		Strategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();

		Chokepoint enemy_second_choke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);

		
		if(StrategyManager.Instance().getCurrentStrategyException() != StrategyException.PROTOSSEXCEPTION_DARK){
			if (Prebot.Broodwar.getFrameCount() < 10000) {
				if (Prebot.Broodwar.enemy().incompleteUnitCount(UnitType.Protoss_Nexus) >= 1
						|| Prebot.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Nexus) >= 2) {
					selectedSE = StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS;
				}
	
				// 포톤이나 포지를 보았을때
				if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon,InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Forge,InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Pylon,InformationManager.Instance().enemyPlayer) >= 1) {
					BaseLocation base = null;
					// 1. 본진에 적 포톤캐논이 있는지 본다.
					base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					Region myRegion = base.getRegion();
					List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,InformationManager.Instance().enemyPlayer);
					
					
					
					if(enemyUnitsInRegion.size() >0){
						for (Unit enemy : enemyUnitsInRegion) {
							if (enemy.getType() == UnitType.Protoss_Photon_Cannon) {
								selectedSE = StrategyException.PROTOSSEXCEPTION_PHOTONRUSH;
								break;
							}
						}
					} 
					// 2. 앞마당 지역은 익셉션이 포톤러쉬가 아닌 상태면 찾아본다.
					if (selectedSE != StrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
						base = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
						myRegion = base.getRegion();
						enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
								InformationManager.Instance().enemyPlayer);
						if(enemyUnitsInRegion.size() >0){
							for (Unit enemy : enemyUnitsInRegion) {
								if (enemy.getType() == UnitType.Protoss_Photon_Cannon) {
									selectedSE = StrategyException.PROTOSSEXCEPTION_PHOTONRUSH;
									break;
								}
							}
						} 
					} 
					
					if (selectedSE != StrategyException.PROTOSSEXCEPTION_PHOTONRUSH) {
						
						base = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
						if(base!=null){
							myRegion = base.getRegion();
							enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,InformationManager.Instance().enemyPlayer);
							if(enemyUnitsInRegion.size() >0){
								for (Unit enemy : enemyUnitsInRegion) {
									if (enemy.getType() == UnitType.Protoss_Photon_Cannon || enemy.getType() == UnitType.Protoss_Forge
											) {
											selectedS = Strategy.PROTOSSBASIC_DOUBLEPHOTO;
										break;
									}
								}
							} 
						}
					} 
					// 포톤 or 포지를 보았는데 우리지역이 아니라면 더블넥서스다.
					if (selectedSE != StrategyException.PROTOSSEXCEPTION_PHOTONRUSH && InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon,InformationManager.Instance().enemyPlayer) >= 1) {
						selectedSE = StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS;
					}
				}
				
				
			}
			
			if(selectedS != Strategy.PROTOSSBASIC_DOUBLEPHOTO){
				if(Prebot.Broodwar.getFrameCount() < 8500
						&& (selectedSE != StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS 
						&& selectedSE != StrategyException.PROTOSSEXCEPTION_PHOTONRUSH)){
					if (Prebot.Broodwar.getFrameCount() < 6000 && InformationManager.Instance().isFirstScoutAlive()) {
						// 4300 프레임이전에
						if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,InformationManager.Instance().enemyPlayer) >= 2
								&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Assimilator,InformationManager.Instance().enemyPlayer) == 0)
								|| (InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,	InformationManager.Instance().enemyPlayer) >= 2
										&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core,	InformationManager.Instance().enemyPlayer) == 0)) {
							// 어시밀레이터나 코어전에 2게이트라면 질럿 푸시
							TilePosition center = new TilePosition(64,64);
							for(Unit gateway : Prebot.Broodwar.enemy().getUnits()){
								
								if(gateway.getType() == UnitType.Protoss_Gateway|| gateway.getType() == UnitType.Protoss_Pylon){
									if(center.getDistance(gateway.getTilePosition()) < 600){
										RespondToStrategy.Instance().center_gateway= true;
									}
								}
							}
							
							selectedSE = StrategyException.PROTOSSEXCEPTION_READYTOZEALOT;
						}
			
						if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,InformationManager.Instance().enemyPlayer) >= 1
								&& (InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core,
										InformationManager.Instance().enemyPlayer) >= 1)
								|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Assimilator,
												InformationManager.Instance().enemyPlayer) == 1)) {
							// 원게이트에서 코어 올라가면 드래군 레디
							selectedSE = StrategyException.PROTOSSEXCEPTION_READYTODRAGOON;
						}
					}
			
					// 레디투드래군 상태에서 정찰 scv가 죽으면 드래군 푸시
					if (StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_READYTOZEALOT) {
						if (!InformationManager.Instance().isFirstScoutAlive()) {
							selectedSE = StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
						}
					}
					if (StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
						if (!InformationManager.Instance().isFirstScoutAlive()) {
							selectedSE = StrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
						}
					}
					if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,InformationManager.Instance().enemyPlayer) >= 2
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core,	InformationManager.Instance().enemyPlayer) >= 1)
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Citadel_of_Adun,	InformationManager.Instance().enemyPlayer) == 0
							) {
						// 원게이트에서 코어 올라가면 드래군 레디
						selectedSE = StrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
					}
					
			
					// 푸시 판단
					if (enemy_second_choke != null && Prebot.Broodwar.getFrameCount() < 8500) {
						List<Unit> chkPush = Prebot.Broodwar.getUnitsInRadius(enemy_second_choke.getCenter(), 400);
						int temp = 0;
						for (Unit enemy : chkPush) {
							if (enemy.getType() == UnitType.Protoss_Dragoon) {
								selectedSE = StrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
								break;
							}
							if (enemy.getType() == UnitType.Protoss_Zealot) {
								temp++;
							}
							if (temp > 3) {
								selectedSE = StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
								break;
							}
						}
					}

					int dragooncnt = 0;
					int zealotcnt = 0;

					bwapi.Position checker = InformationManager.Instance()
							.getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
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
						selectedSE = StrategyException.PROTOSSEXCEPTION_DRAGOONPUSH;
					}
					if (zealotcnt > 3) {
						selectedSE = StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
					}

				}
			
	
				if (selectedSE == StrategyException.PROTOSSEXCEPTION_READYTOZEALOT
						|| StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_READYTOZEALOT
						|| selectedSE == StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
						|| StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH) {
					if(7000 <  Prebot.Broodwar.getFrameCount()){
						if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot,InformationManager.Instance().enemyPlayer)
							+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Zealot) < 4){
							if (StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
									|| selectedSE == StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
								
							}else{
								RespondToStrategy.Instance().prepareDark = true;
							}
						}
					}
							 
					if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
						selectedSE = StrategyException.INIT;
					}
				}
		
				
			 
				
			
				if (selectedSE == StrategyException.PROTOSSEXCEPTION_READYTODRAGOON
						|| StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_READYTODRAGOON
						|| selectedSE == StrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
						|| StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_DRAGOONPUSH) {
					if(7000 <  Prebot.Broodwar.getFrameCount()){
						if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon,InformationManager.Instance().enemyPlayer)
							+ Prebot.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Dragoon) < 3){
							if (StrategyManager.Instance().getCurrentStrategyException() == StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
									|| selectedSE == StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS) {
								
							}else{
								RespondToStrategy.Instance().prepareDark = true;
							}
						}
					}
					if (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) 
							&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
							+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
						selectedSE = StrategyException.INIT;
					}
				}
			}
		}

	if(selectedSE==StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH||StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH)

	{
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
			selectedSE = StrategyException.INIT;
		}
	}if(selectedSE==StrategyException.PROTOSSEXCEPTION_READYTODRAGOON||StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_READYTODRAGOON)
	{
		if (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)
				&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
						+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
			selectedSE = StrategyException.INIT;
		}
	}

	if(StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS||selectedSE==StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS)
	{
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 2
				&& StrategyManager.Instance().getFacUnits() > 64) {
			selectedSE = StrategyException.INIT;
		}
	}

	if(StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_PHOTONRUSH||selectedSE==StrategyException.PROTOSSEXCEPTION_PHOTONRUSH)
	{
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
				+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
			selectedSE = StrategyException.INIT;
		}
	}

	if(RespondToStrategy.Instance().enemy_scout==false&&(InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout,InformationManager.Instance().enemyPlayer)>=1))
	{
		selectedSE = StrategyException.PROTOSSEXCEPTION_SCOUT;
	}

	if(StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_SCOUT||selectedSE==StrategyException.PROTOSSEXCEPTION_SCOUT)
	{
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) >= 2) {
			selectedSE = StrategyException.INIT;
		}
	}

	if(RespondToStrategy.Instance().enemy_shuttle==false&&(InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,InformationManager.Instance().enemyPlayer)>=1))
	{
		selectedSE = StrategyException.PROTOSSEXCEPTION_SHUTTLE;
	}

	if(RespondToStrategy.Instance().enemy_arbiter==false&&(InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer)>=1||InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter_Tribunal,InformationManager.Instance().enemyPlayer)>=1))
	{
		selectedSE = StrategyException.PROTOSSEXCEPTION_ARBITER;
	}if(StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_ARBITER||selectedSE==StrategyException.PROTOSSEXCEPTION_ARBITER)
	{
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
				InformationManager.Instance().selfPlayer) >= 4) {
			selectedSE = StrategyException.INIT;
		}
	}

	if(RespondToStrategy.Instance().enemy_dark_templar==false&&(InformationManager.Instance().getNumUnits(UnitType.Protoss_Templar_Archives,InformationManager.Instance().enemyPlayer)>=1||InformationManager.Instance().getNumUnits(UnitType.Protoss_Citadel_of_Adun,InformationManager.Instance().enemyPlayer)>=1||InformationManager.Instance().getNumUnits(UnitType.Protoss_Dark_Templar,InformationManager.Instance().enemyPlayer)>=1))
	{

		selectedSE = StrategyException.PROTOSSEXCEPTION_DARK;
	}if(RespondToStrategy.Instance().enemy_dark_templar==false)
	{
		for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
					&& unit.getPosition().isValid()) {
				selectedSE = StrategyException.PROTOSSEXCEPTION_DARK;
			}
		}
	}

	if(selectedSE==StrategyException.PROTOSSEXCEPTION_DARK||StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_DARK)
	{

		boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
		boolean firstChokeTurret = RespondToStrategy.Instance().firstChokeTurret;

		if (mainBaseTurret && firstChokeTurret) {
			selectedSE = StrategyException.INIT;
		}

	}

	if (Prebot.Broodwar.getFrameCount() < 6000) {
		if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,InformationManager.Instance().enemyPlayer) >= 2
				&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Assimilator,InformationManager.Instance().enemyPlayer) == 0)
				|| (InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,	InformationManager.Instance().enemyPlayer) >= 2
						&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core,	InformationManager.Instance().enemyPlayer) == 0)) {
			TilePosition center = new TilePosition(64,64);
			for(Unit gateway : Prebot.Broodwar.enemy().getUnits()){
				
				if(gateway.getType() == UnitType.Protoss_Gateway|| gateway.getType() == UnitType.Protoss_Pylon){
					if(center.getDistance(gateway.getTilePosition()) < 17){
						RespondToStrategy.Instance().center_gateway= true;
					}
				}
			}
			
			selectedSE = StrategyException.PROTOSSEXCEPTION_ZEALOTPUSH;
		}
	}

		
	if(selectedSE!=null)
	{
		StrategyManager.Instance().setCurrentStrategyException(selectedSE);
	}

	if((InformationManager.Instance().getNumUnits(UnitType.Protoss_Fleet_Beacon,InformationManager.Instance().enemyPlayer)>=1&&InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier,InformationManager.Instance().enemyPlayer)>=1)||(Prebot.Broodwar.getFrameCount()<10000&&InformationManager.Instance().getNumUnits(UnitType.Protoss_Stargate,InformationManager.Instance().enemyPlayer)>=1)
	// && InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier,
	// InformationManager.Instance().enemyPlayer) >= 1)
	)
	{

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier,
				InformationManager.Instance().enemyPlayer) >= 1) {
			CarrierStrategyToBasicOnTime = Prebot.Broodwar.getFrameCount() + 2500;
		}

		selectedS = Strategy.PROTOSSBASIC_CARRIER;
	}

	if(StrategyManager.Instance().getCurrentStrategyException()==StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS||selectedSE==StrategyException.PROTOSSEXCEPTION_DOUBLENEXUS)
	{
		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon,
				InformationManager.Instance().enemyPlayer) >= 4) {
			selectedS = Strategy.PROTOSSBASIC_DOUBLEPHOTO;
		}
	}

	if(StrategyManager.Instance().getCurrentStrategyBasic()==Strategy.PROTOSSBASIC_CARRIER)
	{
		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier,
				InformationManager.Instance().enemyPlayer) == 0) {
			if (CarrierStrategyToBasicOnTime != 0 && CarrierStrategyToBasicOnTime > 0
					&& CarrierStrategyToBasicOnTime < Prebot.Broodwar.getFrameCount()) {
				selectedS = Strategy.PROTOSSBASIC;
			}
		} else {
			CarrierStrategyToBasicOnTime = Prebot.Broodwar.getFrameCount() + 2500;
		}
	}

	if(selectedS!=null)
	{
		StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
	}

	}

	boolean after = false;

	private void AnalyzeVsTerran() {
		StrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
		Strategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();

		if (after == false) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks,
					InformationManager.Instance().enemyPlayer) == 1
					&& InformationManager.Instance().getNumUnits(UnitType.Terran_Refinery,
							InformationManager.Instance().enemyPlayer) == 1) {

				selectedS = Strategy.TERRANBASIC_MECHANIC;
			}

			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Marine,
					InformationManager.Instance().enemyPlayer) >= 3
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks,
							InformationManager.Instance().enemyPlayer) >= 2) {

				selectedS = Strategy.TERRANBASIC_BIONIC;
			}

			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Factory,
					InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Machine_Shop,
							InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,
							InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode,
							InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
							InformationManager.Instance().enemyPlayer) >= 1
					|| InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode,
							InformationManager.Instance().enemyPlayer) >= 1) {
				selectedS = Strategy.TERRANBASIC_MECHANIC;
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
			if (StrategyManager.Instance().getCurrentStrategyBasic() != Strategy.TERRANBASIC_BIONIC) {
				selectedS = Strategy.TERRANBASIC_MECHANICAFTER;
				after = true;
			}
		}
		if (after) {
			selectedS = Strategy.TERRANBASIC_MECHANICAFTER;
		}
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Starport,
				InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,
						InformationManager.Instance().enemyPlayer) >= 1) {
			selectedS = Strategy.TERRANBASIC_MECHANICWITHWRAITH;
		}

		if (RespondToStrategy.Instance().enemy_wraithcloak == false && InformationManager.Instance()
				.getNumUnits(UnitType.Terran_Control_Tower, InformationManager.Instance().enemyPlayer) >= 1) {
			selectedSE = StrategyException.TERRANEXCEPTION_WRAITHCLOAK;
		}

		if (RespondToStrategy.Instance().enemy_wraithcloak == false) {
			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)) {
					selectedSE = StrategyException.TERRANEXCEPTION_WRAITHCLOAK;
				}
			}
		}

		if (selectedSE == StrategyException.TERRANEXCEPTION_WRAITHCLOAK || StrategyManager.Instance()
				.getCurrentStrategyException() == StrategyException.TERRANEXCEPTION_WRAITHCLOAK) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Comsat_Station,
					InformationManager.Instance().selfPlayer) >= 1) {
				selectedSE = StrategyException.INIT;
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

		// 치즈러시 대비
		// int nongbong_cnt = 0;
		// Unit enemy_bunker = null;
		// bwta.BaseLocation base =
		// InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		// bwta.Region myRegion = base.getRegion();
		// List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
		// InformationManager.Instance().enemyPlayer);
		// if (enemyUnitsInRegion.size() >= 5) {
		// for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
		// if (enemyUnitsInRegion.get(enemy).getType().equals("Terran_Marine")
		// || enemyUnitsInRegion.get(enemy).getType().equals("Terran_SCV")) {
		// nongbong_cnt++;
		// }
		// // 치즈러쉬 와서 벙커를 지으면 벙커부터 쳐준다.
		// // .... 어쩔까...... 마린은 왠지 도망갈것 같고..........
		// // 벙커를 치는게 낫지 않을까 농봉이니까
		// if (enemyUnitsInRegion.get(enemy).getType() ==
		// UnitType.Terran_Bunker) {
		// enemy_bunker = enemyUnitsInRegion.get(enemy);
		// }
		// }
		// }
		//
		// // 벌쳐가 없는 상태에서 상대 유닛이 내본진에 5기 이상있다면 농봉이다
		// if
		// (InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture)
		// < 1
		// && nongbong_cnt >= 5) {
		// SE = StrategysException.terranException_CheeseRush;
		// //
		// StrategyManager.Instance().setCurrentStrategyException(StrategysException.terranException_CheeseRush);
		// }
		// // 벌쳐가 있거나 마린이 있다면 농봉없이 벙커짓고 막자.
		// // 단 치즈러쉬가 와서 벙커를 짓고 있다면 농봉상태 유지
		// if ((nongbong_cnt <= 4
		// &&
		// InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture)
		// > 0)
		// || nongbong_cnt <= 4
		// &&
		// InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Marine)
		// > 2) {
		// // 인구수대비 해제된 상태에서 적 벙커가 없다면 농봉해제. 아니라면 유지.
		// if (enemy_bunker == null) {
		// SE = StrategysException.Init;
		// //
		// StrategyManager.Instance().setCurrentStrategyException(StrategysException.Init);
		// }
		// }

		// 핵에 대한건 보류 조건이 까다롭다. 원하는 조건을 어찌 해야할지 모르겄네
		/*
		 * if(InformationManager.Instance().getNumUnits(UnitType.
		 * Terran_Nuclear_Silo, InformationManager.Instance().enemyPlayer) >=
		 * 1){ StrategyManager.Instance().setCurrentStrategyException(
		 * StrategysException.terranException_NuClear); }
		 */
	}

	private void AnalyzeVsZerg() {
		StrategyException selectedSE = StrategyManager.Instance().getCurrentStrategyException();
		Strategy selectedS = StrategyManager.Instance().getCurrentStrategyBasic();

		Chokepoint my_first_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().selfPlayer);
		Chokepoint my_second_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().selfPlayer);

		int cntHatchery = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hatchery,
				InformationManager.Instance().enemyPlayer);
		int cntLair = InformationManager.Instance().getNumUnits(UnitType.Zerg_Lair,
				InformationManager.Instance().enemyPlayer);

		int ling_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling,
				InformationManager.Instance().enemyPlayer);
		int hydra_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk,
				InformationManager.Instance().enemyPlayer);
		int lurker_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker,
				InformationManager.Instance().enemyPlayer)
				+ InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker_Egg,
						InformationManager.Instance().enemyPlayer);
		int mutal_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,
				InformationManager.Instance().enemyPlayer);
		int ultra_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Ultralisk,
				InformationManager.Instance().enemyPlayer);

		if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Hive,
				InformationManager.Instance().enemyPlayer) >= 1 || ultra_cnt > 0) {
			selectedSE = StrategyException.ZERGEXCEPTION_HIGHTECH;
		}

		if (selectedSE == StrategyException.ZERGEXCEPTION_HIGHTECH && InformationManager.Instance()
				.getNumUnits(UnitType.Terran_Science_Vessel, InformationManager.Instance().selfPlayer) > 3) {
			selectedSE = StrategyException.INIT;
		}

		// hydra check with lurker
		if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den,
				InformationManager.Instance().enemyPlayer) >= 1 || hydra_cnt >= 1 || lurker_cnt >= 1) {

			selectedS = Strategy.ZERGBASIC_HYDRAWAVE;

			if (RespondToStrategy.Instance().enemy_lurker == false && cntLair >= 1) {
				selectedSE = StrategyException.ZERGEXCEPTION_PREPARELURKER;
			}
			hydraStrategy = true;
		}
		// end hydra exception

		// mutal check with time
		if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire,
				InformationManager.Instance().enemyPlayer) >= 1
				|| mutal_cnt >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Greater_Spire,
						InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian,
						InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Devourer,
						InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Scourge,
						InformationManager.Instance().enemyPlayer) >= 1
				|| (hydraStrategy == false && cntLair >= 1)) {

			selectedS = Strategy.ZERGBASIC_MUTAL;

			if ((mutal_cnt > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18) || mutal_cnt > 24) {
				selectedS = Strategy.ZERGBASIC_MUTALMANY;
			}

			if (mutalStrategy == false && MutalStrategyOnTimeChecker == false) {
				MutalStrategyOnTime = Prebot.Broodwar.getFrameCount() + 3000; // 5000
																					// frame동안
																					// 일단
																					// 뮤탈
			}
			mutalStrategy = true;
		}

		if (hydra_cnt == 0 && lurker_cnt == 0 && mutalStrategy == false
				&& Prebot.Broodwar.getFrameCount() > 8000) {
			selectedS = Strategy.ZERGBASIC_MUTAL;
		}
		// mutal exception

		// lurker in 조건
		if (RespondToStrategy.Instance().enemy_lurker == false) {
			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
				// 인비저블 유닛이 있다면 일단 럴커 대비 로직
				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
						&& unit.getPosition().isValid()) {
					// StrategyManager.Instance().setCurrentStrategyException(StrategysException.protossException_Dark);
					selectedSE = StrategyException.ZERGEXCEPTION_PREPARELURKER;
				}
			}
			if (lurker_cnt > 0) {
				selectedSE = StrategyException.ZERGEXCEPTION_PREPARELURKER;
			}
		}

		// 러커 out 조건
		if (RespondToStrategy.Instance().enemy_lurker == false
				&& selectedSE == StrategyException.ZERGEXCEPTION_PREPARELURKER
				|| StrategyManager.Instance()
						.getCurrentStrategyException() == StrategyException.ZERGEXCEPTION_PREPARELURKER) {

			boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
			boolean firstChokeTurret = RespondToStrategy.Instance().firstChokeTurret;

			if (mainBaseTurret && firstChokeTurret) {
				selectedSE = StrategyException.INIT;
			}
		}

		// TODO 상대 업글 감지 가능?
		if (mutalStrategy == false && hydraStrategy == false && cntHatchery >= 3
				&& Prebot.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost) == 1) {
			// System.out.println("저글링 업글 상태: " +
			// MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost));
			selectedSE = StrategyException.ZERGEXCEPTION_ONLYLING;
		}
		if (selectedSE == StrategyException.ZERGEXCEPTION_ONLYLING && (InformationManager.Instance()
				.getNumUnits(UnitType.Terran_Vulture, InformationManager.Instance().selfPlayer) > 5
				|| mutalStrategy == true || hydraStrategy == true)) {
			selectedSE = StrategyException.INIT;
		}

		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		if (mutalStrategy && hydraStrategy) { // Mutal과 히드라 중에 고른다

			// System.out.print(", choose betwee H,M: " );

			if (MutalStrategyOnTime != 0 && MutalStrategyOnTime > Prebot.Broodwar.getFrameCount()) {
				selectedS = Strategy.ZERGBASIC_MUTAL;
				// System.out.print(", chose M1" );
			} else {
				if (mutal_cnt >= (hydra_cnt * 1.4 + lurker_cnt * 0.9)) {
					selectedS = Strategy.ZERGBASIC_MUTAL;
					// System.out.print(", chose M2" );
					if ((mutal_cnt > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18) || mutal_cnt > 24) {
						selectedS = Strategy.ZERGBASIC_MUTALMANY;
					}
				} else if (mutal_cnt >= (hydra_cnt * 0.7 + lurker_cnt * 0.45)) {
					selectedS = Strategy.ZERGBASIC_HYDRAMUTAL;
					// System.out.print(", chose H1" );
				} else {
					selectedS = Strategy.ZERGBASIC_HYDRAWAVE;
					// System.out.print(", chose H2" );
				}
			}
		}

		if (selectedS == Strategy.ZERGBASIC_HYDRAWAVE && ling_cnt > 12) {
			selectedS = Strategy.ZERGBASIC_LINGHYDRA;
		}

		if (selectedS == Strategy.ZERGBASIC_MUTAL && ling_cnt > 12) {
			selectedS = Strategy.ZERGBASIC_LINGMUTAL;
		}

		if (selectedS != Strategy.ZERGBASIC_MUTALMANY && ultra_cnt > 0) {
			selectedS = Strategy.ZERGBASIC_ULTRA;
		}

		if (selectedS == Strategy.ZERGBASIC_ULTRA && ling_cnt > 12) {
			selectedS = Strategy.ZERGBASIC_LINGULTRA;
		}

		if (selectedS == Strategy.ZERGBASIC_HYDRAWAVE && (hydra_cnt + lurker_cnt > 0)) {
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