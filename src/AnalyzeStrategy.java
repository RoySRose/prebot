
import java.util.List;

import bwapi.Order;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

	int MutalStrategyOnTime;
	public Boolean MutalStrategyOnTimeChecker;
	int CarrierStrategyToBasicOnTime;
	public Boolean hydraStrategy;
	public Boolean mutalStrategy;
	
	private static AnalyzeStrategy instance = new AnalyzeStrategy();

	public static AnalyzeStrategy Instance() {
		return instance;
	}

	public AnalyzeStrategy(){
		
		CarrierStrategyToBasicOnTime =0;
		MutalStrategyOnTime = 0;
		hydraStrategy = false;
		mutalStrategy = false;
		MutalStrategyOnTimeChecker = false;
	}
	
	public void AnalyzeEnemyStrategy() {
		// config 건물 관련 여백 조정용 함수
		// 건물 여백이 0인채로 서플 2개째를 짓고있으면 원래대로 원복
//		if (Config.BuildingSpacing == 0 && InformationManager.Instance().getNumUnits(UnitType.Terran_Supply_Depot,
//				MyBotModule.Broodwar.self()) >= 2) {
//			BlockingEntrance.Instance().ReturnBuildSpacing();
//		}
		
		
		// 최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			AnalyzeVsProtoss();
		} else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			AnalyzeVsTerran();
		} else {
			AnalyzeVsZerg();
		}
		
		
		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple){
			if(MyBotModule.Broodwar.getFrameCount() > 25000){
				
				
				Boolean checkisland = true;
				List<BaseLocation> occupiedBaseLocation = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);
				
				//섬 지역 아닌곳에 상대 유닛이 있으면 패스
				for (BaseLocation base : occupiedBaseLocation)
				{
					if(base.isIsland() == false){
						checkisland = false;
					}
				}
				
				if(checkisland == true){
					for (BaseLocation base : occupiedBaseLocation){
						List<Unit> enemy = MapGrid.Instance().getUnitsNear(base.getPosition(), 600, false, true, null);
						
						if(enemy.size()>0){
							BaseLocation enemymainbase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
							if(enemymainbase.isIsland()){
								StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.AttackIsland);
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
		StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic);
		} else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic);
		} else {
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic);
		}

	}

	private void AnalyzeVsProtoss() {
		StrategyManager.StrategysException selectedSE = null;
		StrategyManager.Strategys selectedS = null;

		Chokepoint enemy_second_choke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);

		Chokepoint my_first_choke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
		Chokepoint my_second_choke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);

		
		if(StrategyManager.Instance().getCurrentStrategyException() != StrategyManager.StrategysException.protossException_Dark){
			if (MyBotModule.Broodwar.getFrameCount() < 10000) {
				// 게이트가 없이 포지더블이나 생더블
				// FirstExpansionLocation
				// 완성되지 않은 넥서스나 완성된 넥서스가 2개이상이면 더블넥서스
				if (MyBotModule.Broodwar.enemy().incompleteUnitCount(UnitType.Protoss_Nexus) >= 1
						|| MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Nexus) >= 2) {
					selectedSE = StrategyManager.StrategysException.protossException_DoubleNexus;
				}
	
				// 포톤이나 포지를 보았을때
				if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon,InformationManager.Instance().enemyPlayer) >= 1
						|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Forge,
								InformationManager.Instance().enemyPlayer) >= 1) {
					BaseLocation base = null;
					// 1. 본진에 적 포톤캐논이 있는지 본다.
					base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					Region myRegion = base.getRegion();
					List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
							InformationManager.Instance().enemyPlayer);
					if (enemyUnitsInRegion.size() >= 1) {
						for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
							if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
								selectedSE = StrategyManager.StrategysException.protossException_PhotonRush;
								break;
							}
						}
					} else
					// 2. 앞마당 지역은 익셉션이 포톤러쉬가 아닌 상태면 찾아본다.
					if (selectedSE != StrategyManager.StrategysException.protossException_PhotonRush) {
						base = InformationManager.Instance()
								.getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
						myRegion = base.getRegion();
						enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
								InformationManager.Instance().enemyPlayer);
						if (enemyUnitsInRegion.size() >= 1) {
							for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
								if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
									selectedSE = StrategyManager.StrategysException.protossException_PhotonRush;
									break;
								}
							}
						}
					} else
					// 3. first choke point 도 익셉션이 포톤러쉬가 아니라면 찾아본다.
					if (selectedSE != StrategyManager.StrategysException.protossException_PhotonRush) {
						// myRegion = choke.getRegions() ;
						enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_first_choke.getCenter(), 300);
						if (enemyUnitsInRegion.size() >= 1) {
							for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
								if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
									selectedSE = StrategyManager.StrategysException.protossException_PhotonRush;
									break;
								}
							}
						}
					} 
	//				else
	//
	//				// 4. second choke point 도 익셉션이 포톤러쉬가 아니라면 찾아본다.
	//				if (selectedSE != StrategyManager.StrategysException.protossException_PhotonRush) {
	//					// myRegion = choke.getRegions() ;
	//					enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_second_choke.getCenter(), 300);
	//					if (enemyUnitsInRegion.size() >= 1) {
	//						for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
	//							if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
	//								selectedSE = StrategyManager.StrategysException.protossException_PhotonRush;
	//								break;
	//							}
	//						}
	//					}
	//				} 
					else {
	
						// 포톤 or 포지를 보았는데 우리지역이 아니라면 더블넥서스다.
						if (selectedSE != StrategyManager.StrategysException.protossException_PhotonRush) {
							selectedSE = StrategyManager.StrategysException.protossException_DoubleNexus;
						}
					}
				}
			}
			
			if(MyBotModule.Broodwar.getFrameCount() < 8500){
				if (MyBotModule.Broodwar.getFrameCount() < 5000 && InformationManager.Instance().isFirstScoutAlive()) {
					// 4300 프레임이전에
					if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,
							InformationManager.Instance().enemyPlayer) >= 2
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Assimilator,
									InformationManager.Instance().enemyPlayer) < 1)
							|| (InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,
									InformationManager.Instance().enemyPlayer) >= 2
									&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core,
											InformationManager.Instance().enemyPlayer) < 1)) {
						// 어시밀레이터나 코어전에 2게이트라면 질럿 푸시
						selectedSE = StrategyManager.StrategysException.protossException_ReadyToZealot;
					}
		
					if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Gateway,
							InformationManager.Instance().enemyPlayer) == 1
							&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Cybernetics_Core,
									InformationManager.Instance().enemyPlayer) >= 1) {
						// 원게이트에서 코어 올라가면 드래군 레디
						selectedSE = StrategyManager.StrategysException.protossException_ReadyToDragoon;
					}
				}
		
				// 레디투드래군 상태에서 정찰 scv가 죽으면 드래군 푸시
				if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToZealot
						&& MyBotModule.Broodwar.getFrameCount() < 8500
						) {
		
					if (!InformationManager.Instance().isFirstScoutAlive()) {
						selectedSE = StrategyManager.StrategysException.protossException_ZealotPush;
					}
				}
				if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToDragoon) {
					if (!InformationManager.Instance().isFirstScoutAlive()) {
						selectedSE = StrategyManager.StrategysException.protossException_DragoonPush;
					}
		
				}
		
				// 푸시 판단
				if (enemy_second_choke != null && MyBotModule.Broodwar.getFrameCount() < 8500) {
					List<Unit> chkPush = MyBotModule.Broodwar.getUnitsInRadius(enemy_second_choke.getCenter(), 100);
					int temp = 0;
					for (Unit enemy : chkPush) {
						if (enemy.getType() == UnitType.Protoss_Dragoon) {
							selectedSE = StrategyManager.StrategysException.protossException_DragoonPush;
							break;
						}
						if (enemy.getType() == UnitType.Protoss_Zealot) {
							temp++;
						}
						if (temp > 3) {
							selectedSE = StrategyManager.StrategysException.protossException_ZealotPush;
							break;
						}
					}
				}
			}
		

			if (selectedSE == StrategyManager.StrategysException.protossException_ReadyToZealot
					|| selectedSE == StrategyManager.StrategysException.protossException_ZealotPush
					|| StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToZealot
					|| StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ZealotPush) {
				if(7000 >  MyBotModule.Broodwar.getFrameCount()){
					if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot,InformationManager.Instance().enemyPlayer)
						+ MyBotModule.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Zealot) < 5){
							RespondToStrategy.Instance().prepareDark = true;
						}
				}
						 
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
					selectedSE = StrategyManager.StrategysException.Init;
				}
			}
	
			if (selectedSE == StrategyManager.StrategysException.protossException_ReadyToDragoon
					|| selectedSE == StrategyManager.StrategysException.protossException_DragoonPush
					|| StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToDragoon
					|| StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DragoonPush) {
				if(7000 >  MyBotModule.Broodwar.getFrameCount()){
					if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon,InformationManager.Instance().enemyPlayer)
						+ MyBotModule.Broodwar.enemy().deadUnitCount(UnitType.Protoss_Dragoon) < 4){
							RespondToStrategy.Instance().prepareDark = true;
						}
				}
				if (MyBotModule.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) 
						&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
						+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
					selectedSE = StrategyManager.StrategysException.Init;
				}
			}

		}

		if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DoubleNexus
				|| selectedSE == StrategyManager.StrategysException.protossException_DoubleNexus) {
			if (CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY
					&& StrategyManager.Instance().MyunitPoint >= 60
					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_PhotonRush
				|| selectedSE == StrategyManager.StrategysException.protossException_PhotonRush) {
			boolean photon_rush = false;
			BaseLocation base = null;
//			// 1. 본진에 적 포톤캐논수를 센다
//			base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//			Region myRegion = base.getRegion();
//			List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
//					InformationManager.Instance().enemyPlayer);
//
//			for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
//				if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
//					photon_rush = true;
//					break;
//				}
//			}
//
//			// 2. 앞마당 지역은 현재 포톤러쉬 상태라면
//			if (!photon_rush) {
//				base = InformationManager.Instance()
//						.getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//				myRegion = base.getRegion();
//				enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
//				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
//					if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
//						photon_rush = true;
//						break;
//					}
//				}
//			}
//			// 3. first choke point 도 현재 포톤러쉬 상태라면
//			if (!photon_rush) {
//				// myRegion = choke.getRegions() ;
//				enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_first_choke.getCenter(), 300);
//				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
//					if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
//						photon_rush = true;
//						break;
//					}
//				}
//			}
//
//			// 3. second choke point 도 현재 포톤러쉬 상태라면
//			if (!photon_rush) {
//				// myRegion = choke.getRegions() ;
//				enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_second_choke.getCenter(), 300);
//				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
//					if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
//						photon_rush = true;
//						break;
//					}
//				}
//			}
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
					+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}
		
		if (RespondToStrategy.Instance().enemy_scout == false && (InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout,InformationManager.Instance().enemyPlayer) >= 1)){
			selectedSE = StrategyManager.StrategysException.protossException_Scout;
		}
		
		if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Scout
				||selectedSE == StrategyManager.StrategysException.protossException_Scout) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) >= 2) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}
		
		if (RespondToStrategy.Instance().enemy_shuttle == false && (InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,InformationManager.Instance().enemyPlayer) >= 1)){
			selectedSE = StrategyManager.StrategysException.protossException_Shuttle;
		}
		
		if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Shuttle
				||selectedSE == StrategyManager.StrategysException.protossException_Shuttle) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) >= 2) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		if (RespondToStrategy.Instance().enemy_arbiter == false && (InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter_Tribunal,InformationManager.Instance().enemyPlayer) >= 1)){
			selectedSE = StrategyManager.StrategysException.protossException_Arbiter;
		}
		if (StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Arbiter
				||selectedSE == StrategyManager.StrategysException.protossException_Arbiter) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) >= 4) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

//		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,
//				InformationManager.Instance().enemyPlayer) >= 1
//				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
//						InformationManager.Instance().selfPlayer)
//						/ InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,
//								InformationManager.Instance().enemyPlayer) < 1) {
//			// 셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
//			// 셔틀대비 골리앗 비율이 맞다면 넘어간다.
//			// 로직 추가할것. 생각중
//			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_ShuttleMix);
//			selectedSE = StrategyManager.StrategysException.protossException_ShuttleMix;
//
//		}
//
//		if (selectedSE == StrategyManager.StrategysException.protossException_ShuttleMix) {
//			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
//					InformationManager.Instance().selfPlayer)
//					/ InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,
//							InformationManager.Instance().enemyPlayer) >= 1) {
//				// 셔틀 1대에 골리앗 2기정도 추가
//				// 완료되면 exit
//				selectedSE = StrategyManager.StrategysException.Init;
//			}
//		}
//
//		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Robotics_Support_Bay,
//				InformationManager.Instance().enemyPlayer) >= 1
//				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Reaver,
//						InformationManager.Instance().enemyPlayer) >= 1) {
//			// 셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
//			// 본진 방어가 되면 넘어간다. 로직 추가할것.
//			// 이건 exit 조건을 본진에 방어병력이 있는 지로 판단해야 하는데 애매해서 일단 넘겼음
//
//			selectedSE = StrategyManager.StrategysException.protossException_Reaver;
//			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Reaver);
//		}

//		if (selectedSE == StrategyManager.StrategysException.protossException_Reaver) {
//			Position tempBaseLocation = null;
//			boolean mainBaseTurret = false;
//
//			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Terran_Command_Center) {
//					tempBaseLocation = unit.getPosition();
//				}
//				int mainBaseTurret_cnt = 0;
//				if (tempBaseLocation != null) {
//					List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempBaseLocation, 300);
//
//					for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
//						if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
//							// System.out.println("Turret Exists at Main Base
//							// Location !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//							mainBaseTurret_cnt++;
//							// break;
//						}
//					}
//					if (mainBaseTurret_cnt >= 3) {
//						System.out.println("방어 터렛 3기 이상");
//						mainBaseTurret = true;
//						selectedSE = StrategyManager.StrategysException.Init;
//						// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
//					}
//				}
//			}
//		}

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier,
				InformationManager.Instance().enemyPlayer) >= 8
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Ghost,
						InformationManager.Instance().selfPlayer) < InformationManager.Instance()
								.getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) / 2) {
			// 캐리어가 8기 이상일때부터 대비시작. 고스트 & 락다운 준비

			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_CarrierMany);
			selectedSE = StrategyManager.StrategysException.protossException_CarrierMany;
		}

		if (selectedSE == StrategyManager.StrategysException.protossException_CarrierMany) {

			if (InformationManager.Instance()
					.getNumUnits(UnitType.Terran_Ghost, InformationManager.Instance().selfPlayer) >= InformationManager
							.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer)
							/ 2) {
				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		// 다크템플러 exception
		// 아카데미로 체크하지 않는다.
		if (RespondToStrategy.Instance().enemy_dark_templar == false && (InformationManager.Instance().getNumUnits(UnitType.Protoss_Templar_Archives,
				InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Citadel_of_Adun,
						InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Dark_Templar,
						InformationManager.Instance().enemyPlayer) >= 1)) {

			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Dark);
			System.out.println("dark checked1");
			selectedSE = StrategyManager.StrategysException.protossException_Dark;
			// RespondToStrategy.instance().enemy_dark_templar = true;
			// 컴셋 마나가 부족할수 있으므로 필수 조건은 터렛으로
			// if(InformationManager.Instance().getNumUnits(UnitType.Terran_Academy,
			// InformationManager.Instance().selfPlayer) >= 1){
		}
		// 잠시주석처리
		// if(InformationManager.Instance().getNumUnits(UnitType.Terran_Academy,
		// InformationManager.Instance().selfPlayer) < 1){
		// 아카데미가 없다면
		if (RespondToStrategy.Instance().enemy_dark_templar == false){
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
				// 인비저블 유닛이 있다면 일단 다크 로직
				if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
						&& unit.getPosition().isValid()) {
					// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Dark);
					
					System.out.println("dark checked2");
					selectedSE = StrategyManager.StrategysException.protossException_Dark;
				}
			}
		}

		if (selectedSE == StrategyManager.StrategysException.protossException_Dark
				|| StrategyManager.Instance().getCurrentStrategyException()== StrategyManager.StrategysException.protossException_Dark) {
			
			
			boolean mainBaseTurret = RespondToStrategy.Instance().mainBaseTurret;
			boolean firstChokeTurret =  RespondToStrategy.Instance().firstChokeTurret;
			
			if (mainBaseTurret && firstChokeTurret) {
				selectedSE = StrategyManager.StrategysException.Init;
				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}
			
//			BaseLocation tempBaseLocation = InformationManager.Instance()
//					.getMainBaseLocation(MyBotModule.Broodwar.self());
//			// Position tempPosition;
//
//			
//					
//			// BaseLocation base =
//			// InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//			Region myRegion = null;
//
//			boolean mainBaseTurret = false;
//			boolean firstChokeTurret = false;
//			boolean secondChokeTurret = false;
//
//			if (tempBaseLocation != null) {
//				myRegion = tempBaseLocation.getRegion();
//				List<Unit> turretInRegion = MicroUtils.getUnitsInRegion(myRegion,
//						InformationManager.Instance().selfPlayer);
//
//				for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
//					if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
//						// System.out.println("Turret Exists at Main Base
//						// Location !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//						mainBaseTurret = true;
//						break;
//					}
//
//				}
//			}
//
//			if (my_first_choke != null) {
//				// myRegion = BWTA.getRegion(tempChokePoint.getPoint());
//				List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_first_choke.getCenter(), 300);
//
//				for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
//					if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
//						// System.out.println("Turret Exists at First Choke
//						// Point !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//						firstChokeTurret = true;
//						break;
//					}
//
//				}
//			}
//			if (my_second_choke != null) {
//				// myRegion = BWTA.getRegion(tempChokePoint.getPoint());
//				List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_second_choke.getCenter(), 300);
//
//				for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
//					if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
//						// System.out.println("Turret Exists at First Choke
//						// Point !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//						secondChokeTurret = true;
//						break;
//					}
//
//				}
//			}
			

		}

		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Fleet_Beacon, InformationManager.Instance().enemyPlayer) >= 1
				&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) >= 1) {
			
			CarrierStrategyToBasicOnTime = 0;
			selectedS = StrategyManager.Strategys.protossBasic_Carrier;
		}

		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.protossBasic_Carrier){
			if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) == 0) {
				if(CarrierStrategyToBasicOnTime == 0){
					CarrierStrategyToBasicOnTime = MyBotModule.Broodwar.getFrameCount() + 7000;
				}
				else if(CarrierStrategyToBasicOnTime < MyBotModule.Broodwar.getFrameCount()){
					selectedS = StrategyManager.Strategys.protossBasic;
				}
			}
		}
		
		if (selectedS != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
		}
		
	}

	private void AnalyzeVsTerran() {
		StrategyManager.Strategys selectedS = null;
		StrategyManager.StrategysException selectedSE = null;

//		,terranBasic
//		,terranBasic_Bionic
//		,terranBasic_Mechanic
//		,terranBasic_MechanicWithWraith
//		,terranBasic_MechanicWithWraithMany
//		,terranBasic_BattleCruiser
		
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks,InformationManager.Instance().enemyPlayer) == 1 
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Refinery,InformationManager.Instance().enemyPlayer) == 1){
			
			selectedS = StrategyManager.Strategys.terranBasic_Mechanic;
		}
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Marine,InformationManager.Instance().enemyPlayer) >= 3
				|| InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks,InformationManager.Instance().enemyPlayer) >= 2){
			
			selectedS = StrategyManager.Strategys.terranBasic_Bionic;
		}
		
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Factory,InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Terran_Machine_Shop,InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode,InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode,InformationManager.Instance().enemyPlayer) >= 1
				){
			selectedS = StrategyManager.Strategys.terranBasic_Mechanic;
		}
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Starport,InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,InformationManager.Instance().enemyPlayer) >= 1
				){
			selectedS = StrategyManager.Strategys.terranBasic_MechanicWithWraith;
		}
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,InformationManager.Instance().enemyPlayer) >= 12){
			selectedS = StrategyManager.Strategys.terranBasic_MechanicWithWraithMany;
		}
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Battlecruiser,InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Terran_Physics_Lab,InformationManager.Instance().enemyPlayer) >= 1){
			selectedS = StrategyManager.Strategys.terranBasic_BattleCruiser;
		}
		
		
		
		if (InformationManager.Instance().getNumUnits(UnitType.Terran_Control_Tower,InformationManager.Instance().enemyPlayer) >= 1){
			selectedSE = StrategyManager.StrategysException.terranException_WraithCloak;
		}

		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			// 인비저블 유닛이 있다면 일단 클로킹 로직
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
					&& unit.getPosition().isValid()) {
				selectedSE = StrategyManager.StrategysException.terranException_WraithCloak;
			}
		}
		
		if (selectedSE == StrategyManager.StrategysException.terranException_WraithCloak
				|| StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.terranException_WraithCloak ) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Academy, InformationManager.Instance().selfPlayer) >= 1) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}
		
		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}
		if (selectedS != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
		}
		
		

        
		// 치즈러시 대비
//		int nongbong_cnt = 0;
//		Unit enemy_bunker = null;
//		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//		bwta.Region myRegion = base.getRegion();
//		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
//				InformationManager.Instance().enemyPlayer);
//		if (enemyUnitsInRegion.size() >= 5) {
//			for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
//				if (enemyUnitsInRegion.get(enemy).getType().equals("Terran_Marine")
//						|| enemyUnitsInRegion.get(enemy).getType().equals("Terran_SCV")) {
//					nongbong_cnt++;
//				}
//				// 치즈러쉬 와서 벙커를 지으면 벙커부터 쳐준다.
//				// .... 어쩔까...... 마린은 왠지 도망갈것 같고..........
//				// 벙커를 치는게 낫지 않을까 농봉이니까
//				if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Terran_Bunker) {
//					enemy_bunker = enemyUnitsInRegion.get(enemy);
//				}
//			}
//		}
//
//		// 벌쳐가 없는 상태에서 상대 유닛이 내본진에 5기 이상있다면 농봉이다
//		if (InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture) < 1
//				&& nongbong_cnt >= 5) {
//			SE = StrategyManager.StrategysException.terranException_CheeseRush;
//			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_CheeseRush);
//		}
//		// 벌쳐가 있거나 마린이 있다면 농봉없이 벙커짓고 막자.
//		// 단 치즈러쉬가 와서 벙커를 짓고 있다면 농봉상태 유지
//		if ((nongbong_cnt <= 4
//				&& InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture) > 0)
//				|| nongbong_cnt <= 4
//						&& InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Marine) > 2) {
//			// 인구수대비 해제된 상태에서 적 벙커가 없다면 농봉해제. 아니라면 유지.
//			if (enemy_bunker == null) {
//				SE = StrategyManager.StrategysException.Init;
//				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
//			}
//		}

		// 핵에 대한건 보류 조건이 까다롭다. 원하는 조건을 어찌 해야할지 모르겄네
		/*
		 * if(InformationManager.Instance().getNumUnits(UnitType.
		 * Terran_Nuclear_Silo, InformationManager.Instance().enemyPlayer) >=
		 * 1){ StrategyManager.Instance().setCurrentStrategyException(
		 * StrategyManager.StrategysException.terranException_NuClear); }
		 */
	}

	private void AnalyzeVsZerg() {
		StrategyManager.Strategys selectedS = null;
		StrategyManager.StrategysException selectedSE = null;

		Chokepoint enemy_first_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		Chokepoint enemy_second_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().enemyPlayer);

		Chokepoint my_first_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().selfPlayer);
		Chokepoint my_second_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().selfPlayer);

		
		int cntHatchery = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hatchery,
				InformationManager.Instance().enemyPlayer);
		int cntLair = InformationManager.Instance().getNumUnits(UnitType.Zerg_Lair,
				InformationManager.Instance().enemyPlayer);
		int cntHive = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hive,
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
		;

		
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hive, InformationManager.Instance().enemyPlayer) >=1
				|| ultra_cnt > 0){
			selectedSE = StrategyManager.StrategysException.zergException_HighTech;
		}
		
		if(selectedSE == StrategyManager.StrategysException.zergException_HighTech && InformationManager.Instance().getNumUnits(UnitType.Terran_Science_Vessel, InformationManager.Instance().selfPlayer) > 3){
			selectedSE = StrategyManager.StrategysException.Init;
		}
		
//		System.out.print("frame: " + MyBotModule.Broodwar.getFrameCount());
//		
//		System.out.print("mutaframe: " + MutalStrategyOnTime);
//		System.out.print("hydra den: " + InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den,
//				InformationManager.Instance().enemyPlayer));
//		
//		System.out.print(", lair: " + InformationManager.Instance().getNumUnits(UnitType.Zerg_Lair,
//				InformationManager.Instance().enemyPlayer));
//		System.out.print(", hatchery: " + InformationManager.Instance().getNumUnits(UnitType.Zerg_Hatchery,
//				InformationManager.Instance().enemyPlayer));
//		System.out.print(", mutal: " + InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,
//				InformationManager.Instance().enemyPlayer));
//		
//		System.out.print(", hydra: " + InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk,
//				InformationManager.Instance().enemyPlayer));
//		
//		System.out.print(", lurker: " + InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker,
//				InformationManager.Instance().enemyPlayer)+ "("+lurker_cnt+")");
//		System.out.print(", hydraStrategy: " + hydraStrategy);
//		System.out.print(", mutalStrategy: " + mutalStrategy);
		
		// hydra check with lurker
		if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den,
				InformationManager.Instance().enemyPlayer) >= 1
				|| hydra_cnt >= 1 || lurker_cnt >= 1 ) {
			
			selectedS = StrategyManager.Strategys.zergBasic_HydraWave;

			if (cntLair >= 1) {
				selectedSE = StrategyManager.StrategysException.zergException_PrepareLurker;
			}
			hydraStrategy = true;
		}
		// end hydra exception

		// mutal check with time
		if (InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire,InformationManager.Instance().enemyPlayer) >= 1
				|| mutal_cnt >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Greater_Spire,InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian,InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Devourer,InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Scourge,InformationManager.Instance().enemyPlayer) >= 1
				|| (hydraStrategy == false && cntLair >= 1)) {

			selectedS = StrategyManager.Strategys.zergBasic_Mutal;

			if ((mutal_cnt > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18)||mutal_cnt>24) {
				selectedS = StrategyManager.Strategys.zergBasic_MutalMany;
			}

			if (mutalStrategy == false && MutalStrategyOnTimeChecker == false) {
				MutalStrategyOnTime = MyBotModule.Broodwar.getFrameCount() + 3000; // 5000 frame동안 일단 뮤탈
			}
			mutalStrategy = true;
		}
		
		if(hydra_cnt == 0 && lurker_cnt == 0 && mutalStrategy ==false && MyBotModule.Broodwar.getFrameCount() > 8000){
			selectedS = StrategyManager.Strategys.zergBasic_Mutal;
		}
		// mutal exception

		// lurker in 조건
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			// 인비저블 유닛이 있다면 일단 럴커 대비 로직
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
					&& unit.getPosition().isValid()) {
				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Dark);
				selectedSE = StrategyManager.StrategysException.zergException_PrepareLurker;
			}
		}
		if (lurker_cnt > 0) {
			selectedSE = StrategyManager.StrategysException.zergException_PrepareLurker;
		}

		// 러커 out 조건
		if (selectedSE == StrategyManager.StrategysException.zergException_PrepareLurker) {
			BaseLocation tempBaseLocation = InformationManager.Instance()
					.getMainBaseLocation(MyBotModule.Broodwar.self());

			Region myRegion = null;
			boolean mainBaseTurret = false;
			boolean firstChokeTurret = false;
			boolean secondChokeTurret = false;

			if (tempBaseLocation != null) {
				myRegion = tempBaseLocation.getRegion();
				List<Unit> turretInRegion = MicroUtils.getUnitsInRegion(myRegion,
						InformationManager.Instance().selfPlayer);

				for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
					if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
						// System.out.println("Turret Exists at Main Base
						// Location !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						mainBaseTurret = true;
						break;
					}

				}
			}

			if (my_first_choke != null) {
				// myRegion = BWTA.getRegion(tempChokePoint.getPoint());
				List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_first_choke.getCenter(), 300);

				for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
					if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
						// System.out.println("Turret Exists at First Choke
						// Point !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						firstChokeTurret = true;
						break;
					}

				}
			}

			if (my_second_choke != null) {
				// myRegion = BWTA.getRegion(tempChokePoint.getPoint());
				List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_second_choke.getCenter(), 300);

				for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
					if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
						// System.out.println("Turret Exists at First Choke
						// Point !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						firstChokeTurret = true;
						break;
					}

				}
			}
			if (mainBaseTurret && firstChokeTurret && secondChokeTurret) {
				selectedSE = StrategyManager.StrategysException.Init;
				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}

		}

		//TODO 상대 업글 감지 가능?
		if(mutalStrategy == false && hydraStrategy == false && cntHatchery >= 3 && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost)==1){
			System.out.println("저글링 업글 상태: " + MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost));
			selectedSE = StrategyManager.StrategysException.zergException_OnLyLing;
		}
		if(selectedSE == StrategyManager.StrategysException.zergException_OnLyLing && (InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture, InformationManager.Instance().selfPlayer) > 5
				|| mutalStrategy ==true || hydraStrategy == true)){
			selectedSE = StrategyManager.StrategysException.Init;
		}
		
		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		if (mutalStrategy && hydraStrategy) { // Mutal과 히드라 중에 고른다

//			System.out.print(", choose betwee H,M: " );

			if (MutalStrategyOnTime != 0 && MutalStrategyOnTime > MyBotModule.Broodwar.getFrameCount()) {
				selectedS = StrategyManager.Strategys.zergBasic_Mutal;
//				System.out.print(", chose M1" );
			} else{
				if (mutal_cnt >= (hydra_cnt * 1.4 +lurker_cnt * 0.9)) {
					selectedS = StrategyManager.Strategys.zergBasic_Mutal;
//					System.out.print(", chose M2" );
					if ((mutal_cnt > 18 && ling_cnt + hydra_cnt + lurker_cnt < 18)||mutal_cnt>24) {
						selectedS = StrategyManager.Strategys.zergBasic_MutalMany;
					}
				} else if(mutal_cnt >= (hydra_cnt * 0.7 +lurker_cnt * 0.45)){
					selectedS = StrategyManager.Strategys.zergBasic_HydraMutal;
//					System.out.print(", chose H1" );
				}else{
					selectedS = StrategyManager.Strategys.zergBasic_HydraWave;
//					System.out.print(", chose H2" );
				}
			}
		}
		
		if(selectedS == StrategyManager.Strategys.zergBasic_HydraWave && ling_cnt > 12){
			selectedS = StrategyManager.Strategys.zergBasic_LingHydra;
		}
		
		if(selectedS == StrategyManager.Strategys.zergBasic_Mutal && ling_cnt > 12){
			selectedS = StrategyManager.Strategys.zergBasic_LingMutal;
		}
		
		if(selectedS != StrategyManager.Strategys.zergBasic_MutalMany && ultra_cnt > 0){
			selectedS = StrategyManager.Strategys.zergBasic_Ultra;
		}
		
		if(selectedS == StrategyManager.Strategys.zergBasic_Ultra && ling_cnt > 12){
			selectedS = StrategyManager.Strategys.zergBasic_LingUltra;
		}
		
		if(selectedS == StrategyManager.Strategys.zergBasic_HydraWave && (hydra_cnt + lurker_cnt > 0)){
			MutalStrategyOnTimeChecker = true;
		}
				 
		System.out.println();
		//TODO 필요할지 확인  zergBasic_GiftSet
		//TODO 필요할지 확인  zergBasic_Guardian
		
		if (selectedS != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
		}
	}

}