package pre;

import bwapi.Race;
import bwapi.UnitType;
import pre.main.MyBotModule;
import pre.manager.BuildManager;
import pre.manager.InformationManager;

/// 봇 프로그램 설정
public class InitialBuild {

	private static InitialBuild instance = new InitialBuild();
	
	public static InitialBuild Instance() {
		return instance;
	}
	
	
	public void setInitialBuildOrder() {
		String map_name = MyBotModule.Broodwar.mapFileName();
		
		//System.out.println("map_name ===>>>> " + map_name);
		
		if(map_name.contains("Hunter")){
			map_name = "Hunter";
		}else if(map_name.contains("Lost")){
			map_name = "Lotem";
		}else if(map_name.contains("Spirit")){
			map_name = "Spirit";
		}
			
		

		//@@@@@@ 맵과 상대 종족에 따른 initial build 를 따져봐야된다.
		//테란전 플토전은 벙커 없이 가자
		
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			
			if(map_name.equals("Hunter")){
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//8scv 찍어놓고 서플( 아마 얘가 만들어지면 서플 지을걸)
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//10 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*12가스를 위해 주석처리 13가스 하려면 풀어주면 됨.
				 * BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//14 scv 찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				
				/*벙커없이 간다.
				 * BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/

				//15 찍어놓고 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			}
			
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Zerg) {
			
			if(map_name.equals("Spirit")){
			
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//8scv 찍어놓고 서플( 아마 얘가 만들어지면 서플 지을걸)
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//10 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*12가스를 위해 주석처리 13가스 하려면 풀어주면 됨.
				 * BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//14 scv 찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				
				/*벙커없이 간다.
				 * BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
	
				//15 찍어놓고 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
			}

		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			
			if(map_name.equals("Spirit")){
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//8scv 찍어놓고 서플( 아마 얘가 만들어지면 서플 지을걸)
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//10 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*12가스를 위해 주석처리 13가스 하려면 풀어주면 됨.
				 * BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//14 scv 찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				
				/*벙커없이 간다.
				 * BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
	
				//15 찍어놓고 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
			}

		}else{
		
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			//테란전 8서플
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			/*BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			//10배럭 - 서플완성 scv 정찰
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			//13가스
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			//현재 14scv - 1정찰 / 10 미네랄 / 3가스
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			//15팩토리
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		
			/*
			// 가스 리파이너리
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getRefineryBuildingType());

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Comsat_Station);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Medic);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Firebat);

			// 지상유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Infantry_Weapons, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Infantry_Armor, false);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret);

			// 마린 스팀팩
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Stim_Packs, false);
			// 마린 사정거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.U_238_Shells, false);

			// 메딕
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Optical_Flare, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Restoration, false);
			// 메딕 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Caduceus_Reactor, false);

			// 팩토리
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop);
			// 벌쳐 스파이더 마인
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines, false);
			// 벌쳐 이동속도 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ion_Thrusters, false);
			// 시즈탱크 시즈모드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode);

			// 벌쳐
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture);

			// 시즈탱크
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode, false);

			// 아머니
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory);
			// 지상 메카닉 유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
			// 공중 유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Ship_Plating, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Ship_Weapons, false);
			// 골리앗 사정거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters, false);

			// 골리앗
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath);

			// 스타포트
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower);
			// 레이쓰 클러킹
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Cloaking_Field, false);
			// 레이쓰 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Apollo_Reactor, false);

			// 레이쓰
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Wraith);

			// 발키리
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Valkyrie);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center);

			// 사이언스 퍼실리티
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility);
			// 사이언스 베슬 - 기술
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.EMP_Shockwave, false);
			// 사이언스 베슬 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Titan_Reactor, false);

			// 사이언스 베슬
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel);
			// 사이언스 퍼실리티 - 배틀크루저 생산 가능
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Physics_Lab);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Yamato_Gun, false);
			// 배틀크루저 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Colossus_Reactor, false);
			// 배틀크루저
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Battlecruiser);

			// 사이언스 퍼실리티 - 고스트 생산 가능
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Covert_Ops);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Lockdown, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Personnel_Cloaking, false);
			// 고스트 가시거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ocular_Implants, false);
			// 고스트 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Moebius_Reactor, false);

			// 고스트
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Ghost);

			// 핵폭탄
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Nuclear_Silo);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Nuclear_Missile);
			*/
		
		}
	}



	
}