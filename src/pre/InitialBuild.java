package pre;

import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
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
			
			
			if(!map_name.equals("Hunter")){
				//헌터 아닌맵 테란전

				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//8서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//11 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//13 scv 찍고 서치
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//15 scv 찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//16scv 찍고 팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//마린
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//2팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//머신샵
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//마인업부터
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines);
				//벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//scv				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);				
				//벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				

			}else if(map_name.equals("Hunter")){
				//테란전 헌터
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//8서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//11 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//13 scv 찍고 서치
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//16 째는 마린
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//16 찍고 팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//팩찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//18 찍고 2팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//머신샵
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//결과적으로 20찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//머신샵 완성 마인업 벌쳐2
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//3벌쳐는 나가고 탱크는 대기
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Siege_Mode,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//나가면서 확장 커맨드				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//시즈모드
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode);
				//scv				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//3팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				
				
				
			}
			
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Zerg) {
			
			if(!map_name.equals("Hunter")){
			//헌터 아닌맵
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//8scv 찍어놓고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//10 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//14 scv 찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

				//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
	
				//15 찍어놓고 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//마인업부터
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				
			}else{
				//헌터저그전
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//8scv 찍어놓고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//11 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);*/
				//14 scv 찍고 벙커, 마린
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);*/
				//16인구 찍고 서플, 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//17, 18scv 찍고 2팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//21인구수 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//골리앗 사업
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				
			}

		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			
			if(!map_name.equals("Hunter")){
				
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
		
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//마린
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
	
				//16 찍어놓고 팩토리
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
				
			}else if(map_name.equals("Hunter")){
				//헌터플토전
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);

				//9scv 찍어놓고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//10 scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//11 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//배럭 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//14scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//마린
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//팩
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//머신샵
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//탱크
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Siege_Mode,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//시즈업
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//머신샵 추가
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//탱크추가
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Siege_Mode,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//마인업
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines);
				//scv
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//벌쳐 속업
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ion_Thrusters);
				
				
			}

		}else{ 
			//랜덤은 저그 따라 가기
			if(!map_name.equals("Hunter")){
			//헌터 아닌맵
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//8scv 찍어놓고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//10 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//14 scv 찍고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

				//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
	
				//15 찍어놓고 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				//마인업부터
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				
			}else{
				//헌터저그전
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//8scv 찍어놓고 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//11 scv 찍고 배럭
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//12 scv 찍고 가스
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);*/
				//14 scv 찍고 벙커, 마린
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Marine,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);*/
				//16인구 찍고 서플, 팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//17, 18scv 찍고 2팩토리
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				//벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//21인구수 서플
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//19 찍어놓고 1벌쳐
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				//골리앗 사업
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				
				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters,
						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
				
			}

		}
	}



	
}