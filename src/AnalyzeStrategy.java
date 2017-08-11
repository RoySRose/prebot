
import java.util.List;

import bwapi.Order;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

	int MutalStrategyOnTime;
	public Boolean hydraStrategy;
	public Boolean multalStrategy;
	
	private static AnalyzeStrategy instance = new AnalyzeStrategy();

	public static AnalyzeStrategy Instance() {
		return instance;
	}

	public void AnalyzeEnemyStrategy() {
		// config 건물 관련 여백 조정용 함수
		// 건물 여백이 0인채로 서플 2개째를 짓고있으면 원래대로 원복
		if (Config.BuildingSpacing == 0 && InformationManager.Instance().getNumUnits(UnitType.Terran_Supply_Depot,
				MyBotModule.Broodwar.self()) >= 2) {
			BlockingEntrance.Instance().ReturnBuildSpacing();
		}

		MutalStrategyOnTime = 0;
		hydraStrategy = false;
		multalStrategy = false;
		
		// 최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			AnalyzeVsProtoss();
		} else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			AnalyzeVsTerran();
		} else {
			AnalyzeVsZerg();
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

		Chokepoint enemy_first_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		Chokepoint enemy_second_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().enemyPlayer);

		Chokepoint my_first_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().selfPlayer);
		Chokepoint my_second_choke = InformationManager.Instance()
				.getFirstChokePoint(InformationManager.Instance().selfPlayer);
		// StrategyManager.StrategysException SE =
		// StrategyManager.Instance().getCurrentStrategyException();

		/*
		 * if(StrategyManager.Instance().getCurrentStrategyBasic().equals(
		 * StrategyManager.Strategys.protossBasic_Templar) &&
		 * !StrategyManager.Strategys.values().equals(StrategyManager.Strategys.
		 * protossBasic_Carrier)){
		 * 
		 * //String test = StrategyManager.Strategys.protossBasic_Templar;
		 * //캐리어와 템플러 대비 기본이 아니라면 일반 기본으로
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.protossBasic); // 이런식으로 기본 전략 세팅가능 }
		 */

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
		if (StrategyManager.Instance()
				.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToZealot) {

			if (!InformationManager.Instance().isFirstScoutAlive()) {
				selectedSE = StrategyManager.StrategysException.protossException_ZealotPush;
			}
		}
		if (StrategyManager.Instance()
				.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToDragoon) {
			if (!InformationManager.Instance().isFirstScoutAlive()) {
				selectedSE = StrategyManager.StrategysException.protossException_DragoonPush;
			}

		}

		// 푸시 판단
		if (enemy_second_choke != null && MyBotModule.Broodwar.getFrameCount() < 8500) {
			List<Unit> chkPush = MyBotModule.Broodwar.getUnitsInRadius(enemy_second_choke.getCenter(), 180);
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
				}
			}
		}
		// //세컨 초크포인트에 드래군 보이면 드래군 푸시
		// if(enemy_second_choke != null){
		// List<Unit> chkDragoondPush =
		// MyBotModule.Broodwar.getUnitsInRadius(enemy_second_choke.getCenter(),
		// 160);
		// for(int dragoon_cnt = 0; dragoon_cnt < chkDragoondPush.size();
		// dragoon_cnt ++){
		// if(chkDragoondPush.get(dragoon_cnt).getType() ==
		// UnitType.Protoss_Dragoon){
		// selectedSE =
		// StrategyManager.StrategysException.protossException_DragoonPush;
		// break;
		// }
		// }
		// }

		if (selectedSE == StrategyManager.StrategysException.protossException_ReadyToZealot
				|| selectedSE == StrategyManager.StrategysException.protossException_ZealotPush
				|| StrategyManager.Instance()
						.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToZealot
				|| StrategyManager.Instance()
						.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ZealotPush) {
			if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 4) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		if (selectedSE == StrategyManager.StrategysException.protossException_ReadyToDragoon
				|| selectedSE == StrategyManager.StrategysException.protossException_DragoonPush
				|| StrategyManager.Instance()
						.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ReadyToDragoon
				|| StrategyManager.Instance()
						.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DragoonPush) {
			if (MyBotModule.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode) && MyBotModule.Broodwar.self()
					.completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
					+ MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 2) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		if (MyBotModule.Broodwar.getFrameCount() < 10000) {
			// 게이트가 없이 포지더블이나 생더블
			// FirstExpansionLocation
			// 완성되지 않은 넥서스나 완성된 넥서스가 2개이상이면 더블넥서스
			if (MyBotModule.Broodwar.enemy().incompleteUnitCount(UnitType.Protoss_Nexus) >= 1
					|| MyBotModule.Broodwar.enemy().completedUnitCount(UnitType.Protoss_Nexus) >= 2) {
				selectedSE = StrategyManager.StrategysException.protossException_DoubleNexus;
			}

			// 포톤이나 포지를 보았을때
			if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Photon_Cannon,
					InformationManager.Instance().enemyPlayer) >= 1
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
				} else

				// 4. second choke point 도 익셉션이 포톤러쉬가 아니라면 찾아본다.
				if (selectedSE != StrategyManager.StrategysException.protossException_PhotonRush) {
					// myRegion = choke.getRegions() ;
					enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_second_choke.getCenter(), 300);
					if (enemyUnitsInRegion.size() >= 1) {
						for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
							if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
								selectedSE = StrategyManager.StrategysException.protossException_PhotonRush;
								break;
							}
						}
					}
				} else {

					// 포톤 or 포지를 보았는데 우리지역이 아니라면 더블넥서스다.
					if (selectedSE != StrategyManager.StrategysException.protossException_PhotonRush) {
						selectedSE = StrategyManager.StrategysException.protossException_DoubleNexus;
					}
				}
			}
		}

		if (StrategyManager.Instance()
				.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DoubleNexus
				|| selectedSE == StrategyManager.StrategysException.protossException_DoubleNexus) {
			if (CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY
					&& StrategyManager.Instance().MyunitPoint >= 60
					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3) {
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		if (StrategyManager.Instance()
				.getCurrentStrategyException() == StrategyManager.StrategysException.protossException_PhotonRush) {
			boolean photon_rush = false;
			BaseLocation base = null;
			// 1. 본진에 적 포톤캐논수를 센다
			base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			Region myRegion = base.getRegion();
			List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
					InformationManager.Instance().enemyPlayer);

			for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
				if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
					photon_rush = true;
					break;
				}
			}

			// 2. 앞마당 지역은 현재 포톤러쉬 상태라면
			if (!photon_rush) {
				base = InformationManager.Instance()
						.getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
				myRegion = base.getRegion();
				enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
					if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
						photon_rush = true;
						break;
					}
				}
			}
			// 3. first choke point 도 현재 포톤러쉬 상태라면
			if (!photon_rush) {
				// myRegion = choke.getRegions() ;
				enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_first_choke.getCenter(), 300);
				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
					if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
						photon_rush = true;
						break;
					}
				}
			}

			// 3. second choke point 도 현재 포톤러쉬 상태라면
			if (!photon_rush) {
				// myRegion = choke.getRegions() ;
				enemyUnitsInRegion = MyBotModule.Broodwar.getUnitsInRadius(my_second_choke.getCenter(), 300);
				for (int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy++) {
					if (enemyUnitsInRegion.get(enemy).getType() == UnitType.Protoss_Photon_Cannon) {
						photon_rush = true;
						break;
					}
				}
			}
			// 끝까지 돌려봤을때 캐논이 전부 없다면 아웃
			if (!photon_rush) {
				selectedSE = StrategyManager.StrategysException.Init;
			}

		}

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout,
				InformationManager.Instance().enemyPlayer) >= 1
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
						InformationManager.Instance().selfPlayer) < InformationManager.Instance()
								.getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer)) {
			// 스카웃은 큰 위협이 안되므로, 골리앗만 맞춰줄것

			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Scout);
			selectedSE = StrategyManager.StrategysException.protossException_Scout;

		}

		if (selectedSE == StrategyManager.StrategysException.protossException_Scout) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
					InformationManager.Instance().selfPlayer) >= InformationManager.Instance()
							.getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer)) {
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}
		}

		if ((InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,
				InformationManager.Instance().enemyPlayer) >= 1
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
						InformationManager.Instance().selfPlayer) < InformationManager.Instance()
								.getNumUnits(UnitType.Protoss_Arbiter, InformationManager.Instance().enemyPlayer) * 4)
				// 아비터를 봤는데 골리앗이 아비터*4 보다 작거나
				|| (InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter_Tribunal,
						InformationManager.Instance().enemyPlayer) >= 1
						&& InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
								InformationManager.Instance().selfPlayer) < 4)) {
			// 아비터 트리뷰널을 봤는데 골리앗이 4마리가 안될경우(기본적으로 섞어 다닐것)

			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Scout);
			selectedSE = StrategyManager.StrategysException.protossException_Arbiter;
			// RespondToStrategy.instance().enemy_dark_templar = true;

		}

		if (selectedSE == StrategyManager.StrategysException.protossException_Arbiter) {
			if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,
					InformationManager.Instance().enemyPlayer) >= 1) {
				if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
						InformationManager.Instance().selfPlayer) >= InformationManager.Instance()
								.getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer) * 4) {
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
				}
			} else {
				if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
						InformationManager.Instance().selfPlayer) >= 4) {
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
				}
			}
		}

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,
				InformationManager.Instance().enemyPlayer) >= 1
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
						InformationManager.Instance().selfPlayer)
						/ InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,
								InformationManager.Instance().enemyPlayer) < 1) {
			// 셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
			// 셔틀대비 골리앗 비율이 맞다면 넘어간다.
			// 로직 추가할것. 생각중
			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_ShuttleMix);
			selectedSE = StrategyManager.StrategysException.protossException_ShuttleMix;

		}

		if (selectedSE == StrategyManager.StrategysException.protossException_ShuttleMix) {
			if (InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
					InformationManager.Instance().selfPlayer)
					/ InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle,
							InformationManager.Instance().enemyPlayer) >= 1) {
				// 셔틀 1대에 골리앗 2기정도 추가
				// 완료되면 exit
				selectedSE = StrategyManager.StrategysException.Init;
			}
		}

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Robotics_Support_Bay,
				InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Reaver,
						InformationManager.Instance().enemyPlayer) >= 1) {
			// 셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
			// 본진 방어가 되면 넘어간다. 로직 추가할것.
			// 이건 exit 조건을 본진에 방어병력이 있는 지로 판단해야 하는데 애매해서 일단 넘겼음

			selectedSE = StrategyManager.StrategysException.protossException_Reaver;
			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Reaver);
		}

		if (selectedSE == StrategyManager.StrategysException.protossException_Reaver) {
			Position tempBaseLocation = null;
			boolean mainBaseTurret = false;

			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
				if (unit.getType() == UnitType.Terran_Command_Center) {
					tempBaseLocation = unit.getPosition();
				}
				int mainBaseTurret_cnt = 0;
				if (tempBaseLocation != null) {
					List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempBaseLocation, 300);

					for (int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt++) {
						if (turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)) {
							// System.out.println("Turret Exists at Main Base
							// Location !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

							mainBaseTurret_cnt++;
							// break;
						}
					}
					if (mainBaseTurret_cnt >= 3) {
						System.out.println("방어 터렛 3기 이상");
						mainBaseTurret = true;
						selectedSE = StrategyManager.StrategysException.Init;
						// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
					}
				}
			}
		}

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
		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Templar_Archives,
				InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Citadel_of_Adun,
						InformationManager.Instance().enemyPlayer) >= 1
				|| InformationManager.Instance().getNumUnits(UnitType.Protoss_Dark_Templar,
						InformationManager.Instance().enemyPlayer) >= 1) {

			// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Dark);
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
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			// 인비저블 유닛이 있다면 일단 다크 로직
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
					&& unit.getPosition().isValid()) {
				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Dark);
				selectedSE = StrategyManager.StrategysException.protossException_Dark;
			}
		}

		if (selectedSE == StrategyManager.StrategysException.protossException_Dark) {
			BaseLocation tempBaseLocation = InformationManager.Instance()
					.getMainBaseLocation(MyBotModule.Broodwar.self());
			// Position tempPosition;

			// BaseLocation base =
			// InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
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
						secondChokeTurret = true;
						break;
					}

				}
			}
			if (mainBaseTurret && firstChokeTurret && secondChokeTurret) {
				selectedSE = StrategyManager.StrategysException.Init;
				// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}

		}

		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		if (InformationManager.Instance().getNumUnits(UnitType.Protoss_Stargate,
				InformationManager.Instance().enemyPlayer) >= 1
				// 캐리어 대비 전략
				&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Fleet_Beacon,
						InformationManager.Instance().enemyPlayer) >= 1) {
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic_Carrier);
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

			if (mutal_cnt > 15 && ling_cnt + hydra_cnt + lurker_cnt < 18) {
				selectedS = StrategyManager.Strategys.zergBasic_MutalMany;
			}

			if (multalStrategy == false) {
				MutalStrategyOnTime = MyBotModule.Broodwar.getFrameCount() + 5000; // 5000 frame동안 일단 뮤탈
			}
			multalStrategy = true;
		}
		
		if(hydraStrategy == false && multalStrategy ==false && MyBotModule.Broodwar.getFrameCount() > 8000){
			
//			BaseLocation enemy_firstbase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
//			List<BaseLocation> enemy_bases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);
//			Boolean enemy_expanded = false; 
//			
//			if(enemy_bases.size() > 0 && enemy_firstbase!=null){
//				for(BaseLocation check : enemy_bases){
//					if (check.getTilePosition().equals(enemy_firstbase.getTilePosition())){
//						enemy_expanded = true;
//					}
//				}
//			}
			//뮤탈 추정... 히드라가 보이면 바로 히드라 전환 가능
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
		if(multalStrategy == false && hydraStrategy == false && cntHatchery >= 3 && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost)==1){
			System.out.println("저글링 업글 상태: " + MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost));
			selectedSE = StrategyManager.StrategysException.zergException_OnLyLing;
		}
		if(selectedSE == StrategyManager.StrategysException.zergException_OnLyLing && (InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture, InformationManager.Instance().selfPlayer) > 11
				|| multalStrategy ==true || hydraStrategy == true)){
			selectedSE = StrategyManager.StrategysException.Init;
		}
		
		if (selectedSE != null) {
			StrategyManager.Instance().setCurrentStrategyException(selectedSE);
		}

		if (multalStrategy && hydraStrategy) { // Mutal과 히드라 중에 고른다


			if (MutalStrategyOnTime != 0 && MutalStrategyOnTime > MyBotModule.Broodwar.getFrameCount()) {
				selectedS = StrategyManager.Strategys.zergBasic_Mutal;
			} else{
				if (mutal_cnt >= hydra_cnt * 1.4) {
					selectedS = StrategyManager.Strategys.zergBasic_Mutal;
				} else {
					selectedS = StrategyManager.Strategys.zergBasic_HydraWave;
				}
				if ((mutal_cnt > 15 && ling_cnt + hydra_cnt + lurker_cnt < 18)||mutal_cnt>24) {
					selectedS = StrategyManager.Strategys.zergBasic_MutalMany;
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
		
		//TODO 필요할지 확인  zergBasic_GiftSet
		//TODO 필요할지 확인  zergBasic_Guardian
		
		if (selectedS != null) {
			StrategyManager.Instance().setCurrentStrategyBasic(selectedS);
		}
		
		
	
		/*
		 * 기존로직은일단 주석처리 if(mutal_cnt >= 1){ //||
		 * InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire,
		 * InformationManager.Instance().enemyPlayer) >= 1){ //뮤탈을 봤을경우 //건물을
		 * 보는것은 일단 보류.
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_Mutal); if(mutal_cnt >= 15){ //뮤탈 올인 or 뮤탈쪽에 무게를
		 * 싣는 전략 대비
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_MutalMany); } }
		 * 
		 * if(InformationManager.Instance().getNumUnits(UnitType.
		 * Zerg_Ultralisk_Cavern, InformationManager.Instance().enemyPlayer) >=1
		 * && InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,
		 * InformationManager.Instance().enemyPlayer) < 10 &&
		 * (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk,
		 * InformationManager.Instance().enemyPlayer) <=
		 * InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,
		 * InformationManager.Instance().selfPlayer))){ //울트라리스크 커번을 봤을때, 울트라가
		 * 나오고 나서 비율조정을 하게 되면 늦으므로, 미리 조정을 한다. //단, 히드라는 탱크가 많아도 괜찮지만, 뮤탈은 골리앗
		 * 비중이 적어지면 어려우므로, //1. 일정 뮤탈 숫자 이하일때, 비중을 변경하던지 //2. 발키리를 몇기정도 생산하는걸로.
		 * 이건 고민해볼걸
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_Ultra);
		 * 
		 * if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling,
		 * InformationManager.Instance().enemyPlayer) >=8){ //링+ 울트라. 울트라가 나온다면
		 * 이 전략일 확률이 크겠지.
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_LingUltra); }
		 * 
		 * }
		 * 
		 * if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling,
		 * InformationManager.Instance().enemyPlayer) >=8
		 * &&InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk,
		 * InformationManager.Instance().enemyPlayer) >=5){ // 링+히드라 조합. 링 >=8 &
		 * 히드라 >=5
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_LingHydra); }
		 * 
		 * if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling,
		 * InformationManager.Instance().enemyPlayer) >=8 &&
		 * (InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker,
		 * InformationManager.Instance().enemyPlayer) >=1
		 * ||InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker_Egg,
		 * InformationManager.Instance().enemyPlayer) >=1)){ // 링+럴커 조합. 럴커를 보거나
		 * 럴커에그를 보았을때
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_LingLurker); }
		 * 
		 * if((ling_cnt >=8 &&
		 * InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire,
		 * InformationManager.Instance().enemyPlayer) >=1 &&
		 * InformationManager.Instance().getNumUnits(UnitType.
		 * Zerg_Hydralisk_Den, InformationManager.Instance().enemyPlayer) ==0)
		 * || ling_cnt >= 8 && mutal_cnt >= 5){ // 링+뮤탈. 링 >=8 & 스파이어 & 히드라덴 없음
		 * // 또는 링 >=8 & 뮤탈 발견
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_LingMutal); }
		 * 
		 * //아마 우리가 상대하게될 가장 많은 조합 //저그전은 이것때문에 특히 컴셋이 중요한데 //시즈 탱크와 시즈업은 히드라가
		 * 나오는 순간 찍어줘도 되기 때문이다. if(hydra_cnt >=8 && mutal_cnt >=8){ // 히드라+뮤탈.
		 * 히드라 >=8 & 뮤탈 >= 8
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_HydraMutal); }
		 * 
		 * if(ling_chk + hydra_chk + mutal_chk + ultra_chk >= 3){
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_GiftSet); }
		 * 
		 * 
		 * //};
		 * 
		 * //exception 상태가 아닐때는 Init 으로
		 * //StrategyManager.Instance().setCurrentStrategyException("Init");
		 * if(MyBotModule.Broodwar.getFrameCount() % 48 == 0){
		 * System.out.println("Current Basic strategy ===>>>>  " +
		 * StrategyManager.Instance().getCurrentStrategyBasic());
		 * 
		 * System.out.println("Current Exception strategy ===>>>>  " +
		 * StrategyManager.Instance().getCurrentStrategyException()); } 기존로직
		 * 주석처리 끝
		 */

		// 기본 저그 전술 세팅
		/*
		 * if(StrategyManager.Instance().getCurrentStrategyBasic() ==
		 * StrategyManager.Strategys.zergBasic_HighTech){
		 * 
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic_HighTech); }else{
		 * System.out.println("zergBasic 으로 세팅");
		 * StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.
		 * Strategys.zergBasic); } System.out.println("zergBasic 으로 세팅");
		 */
		// 익셉션은 init 기본
		// StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);

		/*
		 * //20170802 농봉은 잠깐 막기 //start zergException_NongBong int nongbong_cnt
		 * = 0; int haveMarine = 0; int haveBunker = 0;
		 * 
		 * Unit theBunker = null; Unit theCommand = null; BaseLocation base =
		 * InformationManager.Instance().getMainBaseLocation(InformationManager.
		 * Instance().selfPlayer); Region myRegion = base.getRegion();
		 * List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion,
		 * InformationManager.Instance().enemyPlayer); if
		 * (enemyUnitsInRegion.size() >= 4){
		 * //System.out.println("적 유닛 4이상 난입");
		 * 
		 * //내 본진에 적유닛이 4마리 이상이고(드론 저글링 오버로드 일수 있음) //haveBunker =
		 * InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.
		 * Terran_Bunker); for (Unit unit : MyBotModule.Broodwar.getAllUnits()){
		 * if(unit.getType() == UnitType.Terran_Bunker && unit.isCompleted()){
		 * haveBunker ++; theBunker = unit; }
		 * 
		 * if(unit.getType() == UnitType.Terran_Marine){ haveMarine ++;
		 * if(haveBunker > 0 && haveMarine >0){ theBunker.load(unit); } }
		 * 
		 * if(unit.getType() == UnitType.Terran_Command_Center){ theCommand =
		 * unit; }
		 * 
		 * } //벙커가 없는 경우. if(haveBunker == 0){ //System.out.println("벙커없다.");
		 * for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
		 * if(enemyUnitsInRegion.get(enemy).getType() == UnitType.Zerg_Zergling
		 * && enemyUnitsInRegion.get(enemy).getDistance(theCommand) < 200){
		 * nongbong_cnt ++; } }
		 * //StrategyManager.Instance().setCurrentStrategyException(
		 * StrategyManager.StrategysException.zergException_NongBong); SE =
		 * StrategyManager.StrategysException.zergException_NongBong; //농봉 되는순간
		 * 호출 } // 벙커가 1개인데 마린이 2 미만인 경우 if(haveBunker == 1 && haveMarine < 2){
		 * for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
		 * if(enemyUnitsInRegion.get(enemy).getType() == UnitType.Zerg_Zergling
		 * && enemyUnitsInRegion.get(enemy).getDistance(theBunker) < 100){
		 * NongbongScv.Instance().NongBong_At_Unit =
		 * enemyUnitsInRegion.get(enemy); nongbong_cnt ++; } }
		 * //StrategyManager.Instance().setCurrentStrategyException(
		 * StrategyManager.StrategysException.zergException_NongBong); SE =
		 * StrategyManager.StrategysException.zergException_NongBong;
		 * 
		 * }
		 * 
		 * } if(StrategyManager.Instance().getCurrentStrategyException() ==
		 * StrategyManager.StrategysException.zergException_NongBong &&
		 * nongbong_cnt < 4){ //본진 저글링이 3미만 이라면 농봉을 안타고 init으로 exit
		 * //StrategyManager.Instance().setCurrentStrategyException(
		 * StrategyManager.StrategysException.Init); SE =
		 * StrategyManager.StrategysException.Init; //Exception 이 init 이 되는순간 농봉
		 * 체크 변수 초기화 NongbongScv.Instance().NongBong_Chk = 0; }
		 */

		/*
		 * if(StrategyManager.Instance().getCurrentStrategyException() ==
		 * StrategyManager.StrategysException.zergException_NongBong){ //농봉은 즉각
		 * 반응 NongbongScv.Instance().executeNongBong(); }
		 */

		// end zergException_NongBong

		// start guardian exception
		// 가디언은 그냥 골리앗으로
		/*
		 * if(
		 * (InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian,
		 * InformationManager.Instance().enemyPlayer) >= 1
		 * ||InformationManager.Instance().getNumUnits(UnitType.
		 * Zerg_Greater_Spire, InformationManager.Instance().enemyPlayer) >= 1)
		 * && (InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,
		 * InformationManager.Instance().selfPlayer) < 5
		 * &&Math.ceil(InformationManager.Instance().getNumUnits(UnitType.
		 * Zerg_Guardian, InformationManager.Instance().enemyPlayer) /3) <
		 * InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,
		 * InformationManager.Instance().selfPlayer))
		 * 
		 * ){ //StrategyManager.Instance().setCurrentStrategyException(
		 * StrategyManager.StrategysException.zergException_Guardian);
		 * selectedSE =
		 * StrategyManager.StrategysException.zergException_Guardian;
		 * 
		 * //그레이터 스파이어 or 가디언을 발견할 경우 //레이스가 round(가디언/3) or 5기를 만족한다면 exit
		 * 
		 * if(InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,
		 * InformationManager.Instance().selfPlayer) == 5
		 * ||Math.ceil(InformationManager.Instance().getNumUnits(UnitType.
		 * Zerg_Guardian, InformationManager.Instance().enemyPlayer) /3) >=
		 * InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith,
		 * InformationManager.Instance().selfPlayer) ){
		 * //StrategyManager.Instance().setCurrentStrategyException(
		 * StrategyManager.StrategysException.Init); SE =
		 * StrategyManager.StrategysException.Init;
		 * 
		 * } }
		 */

	}

}