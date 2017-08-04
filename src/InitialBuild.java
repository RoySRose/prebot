

import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

/// 봇 프로그램 설정
public class InitialBuild {

	private static InitialBuild instance = new InitialBuild();
	
	public static InitialBuild Instance() {
		return instance;
	}
	
	
	public void setInitialBuildOrder() {
		//String map_name = MyBotModule.Broodwar.mapFileName();
		
		//System.out.println("map_name ===>>>> " + map_name);
		
		/*if(map_name.contains("Hunter")){
			map_name = "Hunter";
		}else if(map_name.contains("Lost")){
			map_name = "Lotem";
		}else if(map_name.contains("Spirit")){
			map_name = "Spirit";
		}*/
			
		

		//@@@@@@ 맵과 상대 종족에 따른 initial build 를 따져봐야된다.
		//테란전 플토전은 벙커 없이 가자
		
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			
			
			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//11 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);//13 scv 찍고 서치
				queueBuild(true, UnitType.Terran_Supply_Depot);//15 scv 찍고 서플
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);//16scv 찍고 팩
				queueBuild(true, UnitType.Terran_Marine);//마린
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);// 2팩
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop, UnitType.Terran_SCV);//머신샵
				queueBuild(false, UnitType.Terran_SCV);// scv
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(TechType.Spider_Mines);//마인업부터
				queueBuild(true, UnitType.Terran_Vulture);// 벌처
				//queueBuild(false, UnitType.Terran_SCV);// 벌처

			}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				//테란전 헌터
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//11 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);//13 scv 찍고 서치
				queueBuild(true, UnitType.Terran_Marine);//16 째는 마린
				queueBuild(true, UnitType.Terran_Factory);//16 찍고 팩
				queueBuild(true, UnitType.Terran_Supply_Depot);//팩찍고 서플
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);//18 찍고 2팩
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);//머신샵
				queueBuild(true, UnitType.Terran_Supply_Depot);//결과적으로 20찍고 서플
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(TechType.Spider_Mines);//머신샵 완성 마인업 벌쳐2
				queueBuild(true, UnitType.Terran_Vulture, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);//3벌쳐는 나가고 탱크는 대기
				queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);// 나가면서 확장 커맨드
				queueBuild(TechType.Tank_Siege_Mode);//시즈모드
				queueBuild(false, UnitType.Terran_SCV);//scv
				queueBuild(true, UnitType.Terran_Supply_Depot);//서플
				queueBuild(true, UnitType.Terran_Factory);//3팩
				queueBuild(true, UnitType.Terran_Armory, UnitType.Terran_Academy);
			}
			
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Zerg) {
			
			//8배럭
			
//			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
//				
//				//헌터 아닌맵
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Marine);
//				queueBuild(true, UnitType.Terran_Bunker);//14 scv 찍고 벙커, 마린
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_Marine);
//				queueBuild(true, UnitType.Terran_Refinery);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_Marine);
//				queueBuild(true, UnitType.Terran_Supply_Depot);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				/*queueBuild(true, UnitType.Terran_Marine);*/
//				queueBuild(true, UnitType.Terran_Factory);//15 찍어놓고 팩토리
//				//queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				
//				//queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
//				
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Machine_Shop);
//				//queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				//queueBuild(true, UnitType.Terran_Factory);
//				/*queueBuild(true, UnitType.Terran_Armory);*/
//				//queueBuild(TechType.Spider_Mines);//마인업부터
//				/*queueBuild(true, UpgradeType.Charon_Boosters);*/
//				
//			}
			//10배럭 변경완료
			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
			
				//헌터 아닌맵
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV); //TODO 짧은 맵은 이거 한마리 날리면 산다. 
				//queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				/*queueBuild(true, UnitType.Terran_Marine);*/
				queueBuild(true, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Bunker);//14 scv 찍고 벙커, 마린
				//queueBuild(false, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Supply_Depot);//14 scv 찍고 서플
				
				//queueBuild(false, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Factory);//15 찍어놓고 팩토리
				//queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				
				//queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
				
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				//queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				//queueBuild(true, UnitType.Terran_Factory);
				/*queueBuild(true, UnitType.Terran_Armory);*/
				//queueBuild(TechType.Spider_Mines);//마인업부터
				/*queueBuild(true, UpgradeType.Charon_Boosters);*/
				
			}
			else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				//헌터저그전
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Barracks);//11 scv 찍고 배럭
//				queueBuild(true, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				/*queueBuild(false, UnitType.Terran_SCV);*/
//				queueBuild(true, UnitType.Terran_Bunker);//14 scv 찍고 벙커, 마린
//				queueBuild(true, UnitType.Terran_Marine);
//				/*queueBuild(false, UnitType.Terran_SCV);*/
//				queueBuild(true, UnitType.Terran_Factory);//16인구 찍고 서플, 팩토리
//				queueBuild(true, UnitType.Terran_Supply_Depot);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Factory);//17, 18scv 찍고 2팩토리
//				queueBuild(true, UnitType.Terran_Vulture);//벌쳐
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);//21인구수 서플
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Machine_Shop);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Armory);
//				queueBuild(true, UnitType.Terran_Factory);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(UpgradeType.Charon_Boosters);//골리앗 사업
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Factory);
				
				
				//헌터 아닌맵
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV); //TODO 짧은 맵은 이거 한마리 날리면 산다. 
				//queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				/*queueBuild(true, UnitType.Terran_Marine);*/
				queueBuild(true, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Bunker);//14 scv 찍고 벙커, 마린
				//queueBuild(false, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Supply_Depot);//14 scv 찍고 서플
				
				//queueBuild(false, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Factory);//15 찍어놓고 팩토리
				//queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				
				//queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
				
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				//queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				//queueBuild(true, UnitType.Terran_Factory);
				/*queueBuild(true, UnitType.Terran_Armory);*/
				//queueBuild(TechType.Spider_Mines);//마인업부터
				/*queueBuild(true, UpgradeType.Charon_Boosters);*/
				/*queueBuild(true, UpgradeType.Charon_Boosters);*/
				
			}

		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			
//			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				queueBuildSeed(true, UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8서플
//				
//				
//				
//
//			}
			
			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플( 아마 얘가 만들어지면 서플 지을걸)
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Marine);
				queueBuild(true, UnitType.Terran_Factory);//16 찍어놓고 팩토리
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
				//queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV);
				//queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);//탱크추가
				
			}
//			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플( 아마 얘가 만들어지면 서플 지을걸)
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
//				queueBuild(true, UnitType.Terran_SCV);
//				/*12가스를 위해 주석처리 13가스 하려면 풀어주면 됨.
//				 * queueBuild(false, UnitType.Terran_SCV);*/
//				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				//queueBuild(true, UnitType.Terran_Supply_Depot);//14 scv 찍고 서플
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Bunker);//15 scv 찍고 벙커, 마린
//				queueBuild(true, UnitType.Terran_Marine);
//				/*queueBuild(true, UnitType.Terran_Marine);*/
//				queueBuild(true, UnitType.Terran_Factory);//16 찍어놓고 팩토리
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Machine_Shop);
//				
//			}
			
			else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
//				//헌터플토전
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Supply_Depot);//9scv 찍어놓고 서플
//				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
//						BWTA.getRegion(EntranceBlocking.Instance().suppleX,EntranceBlocking.Instance().suppleY).getPoint().toTilePosition(),true);*/
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);//10 scv
//				queueBuild(true, UnitType.Terran_Barracks);//11 scv 찍고 배럭
//				/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
//						BWTA.getRegion(EntranceBlocking.Instance().barrackX,EntranceBlocking.Instance().barrackY).getPoint().toTilePosition(),true);*/
//				queueBuild(true, UnitType.Terran_SCV);//10 scv
//				queueBuild(true, UnitType.Terran_Refinery);//배럭 찍고 가스
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);//14scv
//				queueBuild(true, UnitType.Terran_Bunker);//14 scv 찍고 벙커, 마린
//				queueBuild(true, UnitType.Terran_Marine);
//				queueBuild(false, UnitType.Terran_SCV);//SCV
//				queueBuild(true, UnitType.Terran_Factory);//팩
//				//queueBuild(true, UnitType.Terran_Supply_Depot);//서플
//				queueBuild(false, UnitType.Terran_SCV);//scv
//				queueBuild(true, UnitType.Terran_Factory);//팩
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);//scv
//				//queueBuild(true, UnitType.Terran_Supply_Depot);//서플
//				queueBuild(true, UnitType.Terran_Machine_Shop);//머신샵
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);//scv
//				queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);//탱크
//				queueBuild(TechType.Tank_Siege_Mode);//시즈업
//				queueBuild(false, UnitType.Terran_SCV);//scv
//				queueBuild(true, UnitType.Terran_Machine_Shop);//머신샵 추가
//				queueBuild(false, UnitType.Terran_SCV);//scv
//				queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);//탱크추가
//				queueBuild(TechType.Spider_Mines);//마인업
//				queueBuild(false, UnitType.Terran_SCV);//scv
//				queueBuild(UpgradeType.Ion_Thrusters);//벌쳐 속업
					queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
					queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플( 아마 얘가 만들어지면 서플 지을걸)
					queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
					queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
					queueBuild(true, UnitType.Terran_SCV);
					queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
					queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
					queueBuild(false, UnitType.Terran_SCV);
					queueBuild(true, UnitType.Terran_Marine);
					queueBuild(true, UnitType.Terran_Factory);//16 찍어놓고 팩토리
					queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
					queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
					//queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					queueBuild(false, UnitType.Terran_SCV);
					//queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
					queueBuild(false, UnitType.Terran_SCV);
					queueBuild(true, UnitType.Terran_Machine_Shop);
					queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);//탱크추가
					
			}

		}else{ 
			//랜덤은 저그 따라 가기
			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
			//헌터 아닌맵
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//10 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//14 scv 찍고 서플
				queueBuild(false, UnitType.Terran_SCV);//현재 14scv - 1정찰 / 10 미네랄 / 3가스
				/*queueBuild(true, UnitType.Terran_Marine);*/
				queueBuild(true, UnitType.Terran_Factory);//15 찍어놓고 팩토리
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);
				/*queueBuild(true, UnitType.Terran_Armory);*/
				queueBuild(TechType.Spider_Mines);//마인업부터
				/*queueBuild(true, UpgradeType.Charon_Boosters);*/
				
			}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				//헌터저그전
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//8scv 찍어놓고 서플
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Barracks);//11 scv 찍고 배럭
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				/*queueBuild(false, UnitType.Terran_SCV);*/
				queueBuild(true, UnitType.Terran_Bunker);//14 scv 찍고 벙커, 마린
				queueBuild(true, UnitType.Terran_Marine);
				/*queueBuild(false, UnitType.Terran_SCV);*/
				queueBuild(true, UnitType.Terran_Factory);//16인구 찍고 서플, 팩토리
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);//17, 18scv 찍고 2팩토리
				queueBuild(true, UnitType.Terran_Vulture);//벌쳐
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);//21인구수 서플
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Armory);
				queueBuild(true, UnitType.Terran_Factory);
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);//19 찍어놓고 1벌쳐
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(UpgradeType.Charon_Boosters);//골리앗 사업
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);
				/*queueBuild(true, UpgradeType.Charon_Boosters);*/
			}

		}
	}
	
	public void queueBuild(boolean blocking, UnitType... types) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		BuildOrderItem.SeedPositionStrategy defaultSeedPosition = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
		for (UnitType type : types) {
			bq.queueAsLowestPriority(type, defaultSeedPosition, blocking);
		}
	}
	
	public void queueBuildSeed(boolean blocking, UnitType type, BuildOrderItem.SeedPositionStrategy seedPosition) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type, seedPosition, blocking);
	}
	
	public void queueBuild(TechType type) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type);
	}
	
	public void queueBuild(UpgradeType type) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type);
	}
}