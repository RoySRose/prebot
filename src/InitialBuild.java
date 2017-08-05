

import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

/// 봇 프로그램 설정
public class InitialBuild {

	private static InitialBuild instance = new InitialBuild();
	
	public static InitialBuild Instance() {
		return instance;
	}
	
	
	public void setInitialBuildOrder() {

		//@@@@@@ 맵과 상대 종족에 따른 initial build 를 따져봐야된다.
		BlockingEntrance.Instance().SetBlockingPosition();
		
		TilePosition firstSupplyPos = new TilePosition(BlockingEntrance.Instance().first_suppleX,BlockingEntrance.Instance().first_suppleY);
		TilePosition secondSupplyPos = new TilePosition(BlockingEntrance.Instance().second_suppleX,BlockingEntrance.Instance().second_suppleY);
		TilePosition barrackPos = new TilePosition(BlockingEntrance.Instance().barrackX,BlockingEntrance.Instance().barrackY);
		TilePosition factoryPos = new TilePosition(BlockingEntrance.Instance().factoryX,BlockingEntrance.Instance().factoryY);
		TilePosition bunkerPos = new TilePosition(BlockingEntrance.Instance().bunkerX,BlockingEntrance.Instance().bunkerY);
		
		
		
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			
			
			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				//기타맵 테란전
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
				queueBuild(false, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				queueBuild(true, UnitType.Terran_Marine);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(TechType.Spider_Mines);
				queueBuild(true, UnitType.Terran_Vulture);

			}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				//헌터 테란전
				
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Marine);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Factory);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(TechType.Spider_Mines);
				queueBuild(true, UnitType.Terran_Vulture, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(true, UnitType.Terran_Siege_Tank_Siege_Mode);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(TechType.Tank_Siege_Mode);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Supply_Depot);
				queueBuild(true, UnitType.Terran_Factory);
				queueBuild(true, UnitType.Terran_Armory, UnitType.Terran_Academy);
			}
			
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				//기타맵 프로토스전
				
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
				queueBuild(true, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Marine);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(true, UnitType.Terran_Vulture);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				
			}
			else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				//헌터플토전
				//20170731 hkk test
				/*queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				int suppleX[] = new int []{29, 52, 96, 102, 93, 55, 12, 23};
				int suppleY[] = new int []{19, 23, 21,   61, 95, 94, 97, 54};
				
				int barrackX[] = new int []{26, 54, 98, 104, 90, 52, 14, 20};
				int barrackY[] = new int []{21, 25, 23,   63, 97, 96, 99, 56};
				
				for(int a= 0; a < suppleX.length ; a ++){
					TilePosition supplyPos = new TilePosition(suppleX[a],suppleY[a]);
					TilePosition barrackPos = new TilePosition(barrackX[a],barrackY[a]);
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot,
							//MyBotModule.Broodwar.getRegionAt(BlockingEntrance.Instance().suppleX,BlockingEntrance.Instance().suppleY).getPoint().toTilePosition(),true);
							supplyPos,true);
					queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks,
							//MyBotModule.Broodwar.getRegionAt(BlockingEntrance.Instance().barrackX,BlockingEntrance.Instance().barrackY).getPoint().toTilePosition(),true);
							barrackPos,true);
					queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				}*/
				
				
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
	  			queueBuild(true, UnitType.Terran_SCV);
	  			queueBuild(true, UnitType.Terran_Refinery);//12 scv 찍고 가스
	  			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
	  			queueBuild(false, UnitType.Terran_SCV);
	  			queueBuild(true, UnitType.Terran_Marine);
	  			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
	  			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
	  			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Machine_Shop);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(true, UnitType.Terran_Vulture);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					
			}

		}else{
			//if(InformationManager.Instance().getMapSpecificInformation().getMap() != MAP.TheHunters){
				//기타맵 저그전
				
				
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV); //TODO 짧은 맵은 이거 한마리 날리면 산다. 
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Refinery);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_SCV);
				queueBuild(true, UnitType.Terran_Marine);
				//ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
				//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true);
				queueBuild(false, UnitType.Terran_Marine);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
				queueBuild(true, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Machine_Shop);
				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
				queueBuild(false, UnitType.Terran_Vulture);
				queueBuild(false, UnitType.Terran_Vulture);
				queueBuild(true, UnitType.Terran_Factory);
				queueBuild(true, UnitType.Terran_Armory);
//			}
//			else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
//				//헌터저그전
//				/*
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true);
//				*/
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_SCV); //TODO 짧은 맵은 이거 한마리 날리면 산다. 
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true);
//				queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Refinery);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(false, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Marine);
//				//ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true);
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true);
//				queueBuild(false, UnitType.Terran_Vulture);
//				queueBuildSeed(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
//				queueBuild(true, UnitType.Terran_Machine_Shop);
				
//			}
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