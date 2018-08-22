package prebot.build.prebot1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import prebot.build.constant.BuildConfig;
import prebot.build.initialProvider.BlockingEntrance.Location;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.main.MyBotModule;
import prebot.common.util.FileUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TilePositionUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation;

/// 건설위치 탐색을 위한 class
public class ConstructionPlaceFinder {

	/// 건설위치 탐색 방법
	public enum ConstructionPlaceSearchMethod { 
		SpiralMethod,	///< 나선형으로 돌아가며 탐색
		SupplyDepotMethod, /// < 서플라이 디팟 메쏘드. 가로 세로를 서플라이 크기만큼 더해서 찾기
		ExistAddonPosition, /// < 애드온만 남은 팩토리 스타포트의 경우
		NewMethod 		///< 예비
	};
	
//	20180815. hkk. for lastBuilding Location Debug
	private ArrayList<TilePosition> lastBuilding = new ArrayList<TilePosition>(); 
	private ArrayList<TilePosition> lastBuildingFinal = new ArrayList<TilePosition>(); 
	
//	public int maxSupplyCntX = 3;
//	public int maxSupplyCntY = 4;
	
	/// 건물 건설 예정 타일을 저장해놓기 위한 2차원 배열<br>
	/// TilePosition 단위이기 때문에 보통 128*128 사이즈가 된다<br>
	/// 참고로, 건물이 이미 지어진 타일은 저장하지 않는다
	private boolean[][] reserveMap = new boolean[128][128];
	
	/// BaseLocation 과 Mineral / Geyser 사이의 타일들을 담는 자료구조. 여기에는 Addon 이외에는 건물을 짓지 않도록 합니다	
//	private Set<TilePosition> tilesToAvoid = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoid = new boolean[128][128];
//	private Set<TilePosition> tilesToAvoidAbsolute = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidAbsolute = new boolean[128][128];

	//서플라이 짓는 지역
//	private Set<TilePosition> tilesToAvoidSupply = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidSupply = new boolean[128][128];
	//팩토리 건설 지역
//	private Set<TilePosition> tilesToAvoidAddonBuilding = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidAddonBuilding = new boolean[128][128];
	//커맨드 센터 와 컴셋 건설지역
//	private Set<TilePosition> tilesToBaseLocationAvoid = new HashSet<TilePosition>();
//	private Set<TilePosition> tilesToAvoidComSat = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidComSat = new boolean[128][128];
	
	
	private static ConstructionPlaceFinder instance = new ConstructionPlaceFinder();
	
	private static boolean isInitialized = false;
	
	/// static singleton 객체를 리턴합니다
	public static ConstructionPlaceFinder Instance() {
		if (!isInitialized) {
			instance.setTilesToAvoid();
			instance.setTilesToAvoidForFirstGas();
			isInitialized = true;
		}
		return instance;
	}

	/// seedPosition 및 seedPositionStrategy 파라메터를 활용해서 건물 건설 가능 위치를 탐색해서 리턴합니다<br>
	/// seedPosition 주위에서 가능한 곳을 선정하거나, seedPositionStrategy 에 따라 지형 분석결과 해당 지점 주위에서 가능한 곳을 선정합니다<br>
	/// seedPosition, seedPositionStrategy 을 입력하지 않으면, MainBaseLocation 주위에서 가능한 곳을 리턴합니다
	public final TilePosition getBuildLocationWithSeedPositionAndStrategy(UnitType buildingType, TilePosition seedPosition, BuildOrderItem.SeedPositionStrategy seedPositionStrategy)
	{
		// seedPosition 을 입력한 경우 그 근처에서 찾는다
		if (TilePositionUtils.isValidTilePosition(seedPosition)) {
			FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy before PlaceFinder seedPosition true ==>> " + buildingType + " :: " + seedPosition);
			TilePosition desiredPosition = getBuildLocationNear(buildingType, seedPosition, true, true);
			return desiredPosition;
		}
		
		// seedPosition 을 입력하지 않은 경우
		TilePosition desiredPosition = TilePosition.None;
		
//		FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy SeedPositionStrategy ==>> " + buildingType + " :: " + seedPositionStrategy);
		
		switch (seedPositionStrategy) {

            case MainBaseLocation:
//            	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myBase().getTilePosition(), true);
//                FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                if (desiredPosition == null) {
                    BuildManager.Instance().mainBaseLocationFull = true;
                }
                break;

            case FirstExpansionLocation:
                if (InfoUtils.myFirstExpansion() != null) {
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myFirstExpansion().getTilePosition());
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    if (desiredPosition == null) {
                        BuildManager.Instance().firstExpansionLocationFull = true;
                    }
                }
                break;

            case FirstChokePoint:
                if (InfoUtils.myFirstChoke() != null) {
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myFirstChoke().getCenter().toTilePosition());
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    if (desiredPosition == null) {
                        BuildManager.Instance().firstChokePointFull = true;
                    }
                }
                break;

            case SecondChokePoint:
                if (InfoUtils.mySecondChoke() != null) {
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.mySecondChoke().getCenter().toTilePosition());
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    if (desiredPosition == null) {
                        BuildManager.Instance().secondChokePointFull = true;
                    }
                }
                break;

            case NextExpansionPoint: // TODO NextSupplePoint 전에 중간포인트로 봐야하나?
//            	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy NextExpansionPoint start");
//            	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                BaseLocation nextExpansionLocation = InformationManager.Instance().getNextExpansionLocation();
//                FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                if (nextExpansionLocation != null) {
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy seedPosition true ==>> " + buildingType + " :: " + nextExpansionLocation.getTilePosition());
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation != null");
                    desiredPosition = getBuildLocationNear(buildingType, nextExpansionLocation.getTilePosition());
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation != null");
                } else {
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation == null");
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myBase().getTilePosition());
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation == null");
                }
//                FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy desiredPosition ==>> " + buildingType + " :: " + desiredPosition);
                break;

            case NextSupplePoint:
            	if(buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory){
                    if (BuildManager.Instance().fisrtSupplePointFull != true) {
                        TilePosition supplyPosition = BlockingEntrance.Instance().getSupplyPosition();
//                        FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                        desiredPosition = getBuildLocationNear(buildingType, supplyPosition);
//                        FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);

                        if (desiredPosition == null) {
                            BuildManager.Instance().fisrtSupplePointFull = true;
                        }
                        break;
                    }
                }
                break;

            case SecondMainBaseLocation:
            	
            	if((buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory) && BuildManager.Instance().fisrtSupplePointFull) {
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: supple");
                    desiredPosition = getBuildLocationNear(buildingType, BlockingEntrance.Instance().getSupplyPosition(InformationManager.Instance().secondStartPosition.getTilePosition()));
//                    FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: supple");
//                    System.out.println(" getSupplyPosition ==>>>> " + desiredPosition);
                }else {
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: else");
                	desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getSecondStartPosition().getTilePosition(), true);
//                	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: else");
                }
//                System.out.println(" SecondMainBaseLocation ==>>>> " + desiredPosition);
                if (desiredPosition == null) {
                    BuildManager.Instance().secondStartLocationFull = true;
                }
                break;

            case LastBuilingPoint:
//            	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                BaseLocation temp = InformationManager.Instance().getLastBuildingLocation();
//                FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                if(temp != null) {
                	desiredPosition = temp.getTilePosition();
                }else {
                	desiredPosition = null;
                }
              
                break;

//            case LastBuilingPoint2:
//                TilePosition lastBuildingTilePosition2 = InformationManager.Instance().getLastBuildingLocation2();
//                if (lastBuildingTilePosition2 != null) {
//                    desiredPosition = getBuildLocationNear(buildingType, lastBuildingTilePosition2);
//                }else{
//                    desiredPosition = null;
//                }
//                break;

            case getLastBuilingFinalLocation: // 이놈이 마지막이니까.... NULL 일수가 없다.
//            	FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                TilePosition lastBuilingFinalLocation = InformationManager.Instance().getLastBuilingFinalLocation();
                desiredPosition = getBuildLocationNear(buildingType, lastBuilingFinalLocation);
//                FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
//                FileUtils.appendTextToFile("log.txt", "\n getLastBuilingFinalLocation ==>> "+ buildingType + " :: " + lastBuilingFinalLocation + " :: " + desiredPosition);
                break;

            default:
                break;
        }

		return desiredPosition;
	}

	/// desiredPosition 근처에서 건물 건설 가능 위치를 탐색해서 리턴합니다<br>
	/// desiredPosition 주위에서 가능한 곳을 찾아 반환합니다<br>
	/// desiredPosition 이 valid 한 곳이 아니라면, desiredPosition 를 MainBaseLocation 로 해서 주위를 찾는다<br>
	/// Returns a suitable TilePosition to build a given building type near specified TilePosition aroundTile.<br>
	/// Returns BWAPI::TilePositions::None, if suitable TilePosition is not exists (다른 유닛들이 자리에 있어서, Pylon, Creep, 건물지을 타일 공간이 전혀 없는 경우 등)
	
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition) {
		return getBuildLocationNear(buildingType, desiredPosition, false);
	}
	
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, Boolean MethodFix) {
		return getBuildLocationNear(buildingType, desiredPosition, MethodFix, false);
	}
	
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, Boolean methodFix, Boolean spaceZero) {
		if (buildingType.isRefinery()) {
			return getRefineryPositionNear(desiredPosition);
		}

		if (!TilePositionUtils.isValidTilePosition(desiredPosition)) {
			desiredPosition = InfoUtils.myBase().getTilePosition();
		}

		// TODO 과제 : 건설 위치 탐색 방법은 ConstructionPlaceSearchMethod::SpiralMethod 로 하는데, 더 좋은 방법은 생각해볼 과제이다
		int constructionPlaceSearchMethod = 0;

		if((buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory)
				&& methodFix == false){
//		if(buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory){
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal();
		} else {
//			FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear spiralMethod set ==>> "+ buildingType + " :: " + desiredPosition);
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SpiralMethod.ordinal();
		}
		
		int buildingGapSpace = getBuildingSpaceGap(buildingType, desiredPosition, methodFix, spaceZero, constructionPlaceSearchMethod);

		TilePosition buildPosition = TilePosition.None;
		
		if (buildingType == UnitType.Terran_Missile_Turret) {
			while (buildingGapSpace >= 0) {
//				FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear4 turret while ==>> "+ buildingType + " :: " + desiredPosition + " :: buildingGapSpace => " +buildingGapSpace);
				buildPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);
				
				if (TilePositionUtils.isValidTilePosition(buildPosition)) {
					return buildPosition;
				}
						
				// 찾을 수 없다면, buildingGapSpace 값을 줄여서 다시 탐색한다
				// buildingGapSpace 값이 1이면 지상유닛이 못지나가는 경우가 많아  제외하도록 한다 
				// 4 -> 3 -> 2 -> 0 -> 탐색 종료
				//      3 -> 2 -> 0 -> 탐색 종료 
				//           1 -> 0 -> 탐색 종료
				if (buildingGapSpace > 2) {
					buildingGapSpace -= 1;
				} else if (buildingGapSpace == 2) {
					buildingGapSpace = 0;
				} else if (buildingGapSpace == 1) {
					buildingGapSpace = 0;
				} else {
					break;
				}
			}
			
		} else {
			buildPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);

		}
		
//		FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear PlanceFinder0 END :: " + System.currentTimeMillis() + " :: " + buildingType + " :: " + desiredPosition);

//		System.out.println("getBuildLocationNear :: " + buildingType + " :: " + desiredPosition);
		if (TilePositionUtils.isValidTilePosition(buildPosition)) {
			return buildPosition;
		} else {
			return TilePosition.None;
		}
	}

	private int getBuildingSpaceGap(UnitType buildingType, TilePosition desiredPosition, Boolean methodFix, Boolean spaceZero, int constructionPlaceSearchMethod) {
		// 일반적인 건물에 대해서는 건물 크기보다 Config::Macro::BuildingSpacing 칸 만큼 상하좌우로 더 넓게 여유공간을 두어서 빈 자리를 검색한다
		int buildingGapSpace = BuildConfig.buildingSpacing;
		
//		FileUtils.appendTextToFile("log.txt","\n getBuildingSpaceGap :: " + buildingType + " :: " + desiredPosition + " :: " + buildingGapSpace);

		// ResourceDepot (Nexus, Command Center, Hatchery), Protoss_Pylon, Terran_Supply_Depot, 
		// Protoss_Photon_Cannon, Terran_Bunker, Terran_Missile_Turret, Zerg_Creep_Colony 는 다른 건물 바로 옆에 붙여 짓는 경우가 많으므로 buildingGapSpace을 다른 Config 값으로 설정하도록 한다
		if (buildingType.isResourceDepot()) {
			buildingGapSpace = BuildConfig.buildingResourceDepotSpacing;
			
		} else if(buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory){
			buildingGapSpace = BuildConfig.BUILDING_SUPPLY_DEPOT_SPACING;
			if (spaceZero) {
				buildingGapSpace = 0;
			} else if (methodFix) {
				buildingGapSpace = 1;
//			} else if ((desiredPosition.getX() == BlockingEntrance.Instance().first_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().first_supple.getY())
//					|| (desiredPosition.getX() == BlockingEntrance.Instance().second_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().second_supple.getY())) {
			} else if (TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().first_supple)
					|| TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().second_supple)) {			
				// 20180719. hkk. 저그전에 대비해서 첫서플과 두번째 서플은 건물들을 붙여지어야 함
				buildingGapSpace = 0;
			} else if(constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal()){
				buildingGapSpace = 0;
			}
			
		} else if (buildingType == UnitType.Terran_Missile_Turret) {
//			buildingGapSpace = BuildConfig.BUILDING_DEFENSE_TOWER_SPACING;
			buildingGapSpace = 0;

//			if(BlockingEntrance.Instance().entrance_turret1 != TilePosition.None) {
//				if(TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().entrance_turret1)) {
////				if(desiredPosition.getX() == BlockingEntrance.Instance().entrance_turret1.getX()
////				 &&desiredPosition.getX() == BlockingEntrance.Instance().entrance_turret1.getX()) {
//					buildingGapSpace = 0;
////					FileUtils.appendTextToFile("log.txt","\n entrance_turret1 : buildingGapSpace : " + buildingGapSpace);
//				}
//			}
//			if(BlockingEntrance.Instance().entrance_turret2 != TilePosition.None) {
//				if(TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().entrance_turret2)) {
//					buildingGapSpace = 0;
////					FileUtils.appendTextToFile("log.txt","\n entrance_turret2 : buildingGapSpace : " + buildingGapSpace);
//				}
//			}
			
		} else if (buildingType == UnitType.Terran_Bunker) {
			buildingGapSpace = 0;
		}
			
//			프리봇 1조건 테스트 추가
//		} else if(buildingType == UnitType.Terran_Barracks){
//			buildingGapSpace = 0;
//		} else if(buildingType == UnitType.Terran_Factory){
//			buildingGapSpace = 0;
//		} else if(buildingType == UnitType.Terran_Starport){
//			buildingGapSpace = 0;
//		} else if(buildingType == UnitType.Terran_Science_Facility){
//			buildingGapSpace = 0;
//		}
		
		
		
//		20180728. hkk. 이니셜 빌드 지정건물들은 여백 0
//		if( (TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().starport1)
//				||TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().starport2) )
//				&& buildingType == UnitType.Terran_Starport
//		){
//			buildingGapSpace = 0;
//		}
//		
//		if( TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().factory)
//			&& buildingType == UnitType.Terran_Factory)
//		{
//			buildingGapSpace = 0;
//		}
//		
//		if( TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().barrack)
//				&& buildingType == UnitType.Terran_Barracks)
//		{
//			buildingGapSpace = 0;
//		}
		
		
//		20180821. hkk. 적용 테스트중
		
		
		if( !TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().factory)
				&& buildingType == UnitType.Terran_Factory)
			{
				buildingGapSpace = 1;
			}
		
//		20180728. hkk. 이니셜 빌드 지정건물들은 여백 0
		if( !TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().starport1)
			&& !TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().starport2)
			&& buildingType == UnitType.Terran_Starport)
		{
			buildingGapSpace = 1;
		}

////		if( (desiredPosition.equals(BlockingEntrance.Instance().first_supple))
////			|| (desiredPosition.equals(BlockingEntrance.Instance().second_supple))
////			|| (desiredPosition.equals(BlockingEntrance.Instance().starport1))
////			|| (desiredPosition.equals(BlockingEntrance.Instance().starport2))
////			|| (desiredPosition.equals(BlockingEntrance.Instance().factory))
////			|| (desiredPosition.equals(BlockingEntrance.Instance().barrack))){
////		if( (desiredPosition.getX() == BlockingEntrance.Instance().first_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().first_supple.getY())
////			|| (desiredPosition.getX() == BlockingEntrance.Instance().second_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().second_supple.getY())
////			|| (desiredPosition.getX() == BlockingEntrance.Instance().starport1.getX() && desiredPosition.getY() == BlockingEntrance.Instance().starport1.getY())
////			|| (desiredPosition.getX() == BlockingEntrance.Instance().starport2.getX() && desiredPosition.getY() == BlockingEntrance.Instance().starport2.getY())
////			|| (desiredPosition.getX() == BlockingEntrance.Instance().factory.getX() && desiredPosition.getY() == BlockingEntrance.Instance().factory.getY())
////			|| (desiredPosition.getX() == BlockingEntrance.Instance().barrack.getX() && desiredPosition.getY() == BlockingEntrance.Instance().barrack.getY())
////		){
//			buildingGapSpace = 0;
//			FileUtils.appendTextToFile("log.txt","\n getBuildingSpaceGap initial set " + buildingType + " :: " + desiredPosition + " :: " + buildingGapSpace);
//		}
		
//		FileUtils.appendTextToFile("log.txt","\n getBuildingSpaceGap return space :: " + buildingType + " :: " + desiredPosition + " :: " + buildingGapSpace);
		return buildingGapSpace;
	}

	/// 해당 buildingType 이 건설될 수 있는 위치를 desiredPosition 근처에서 탐색해서 탐색결과를 리턴합니다<br>
	/// buildingGapSpace를 반영해서 canBuildHereWithSpace 를 사용해서 체크<br>
	/// 못찾는다면 BWAPI::TilePositions::None 을 리턴합니다<br>
	/// TODO 과제 : 건물을 계획없이 지을수 있는 곳에 짓는 것을 계속 하다보면, 유닛이 건물 사이에 갇히는 경우가 발생할 수 있는데, 이를 방지하는 방법은 생각해볼 과제입니다
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, int buildingGapSpace, int constructionPlaceSearchMethod) {
		//returns a valid build location near the desired tile position (x,y).
		TilePosition resultPosition = TilePosition.None;
		ConstructionTask b = new ConstructionTask(buildingType, desiredPosition);

//		FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear PlaceFinder Start :: " + System.currentTimeMillis() + " :: " + buildingType + " :: " + desiredPosition);
		// maxRange 를 설정하지 않거나, maxRange 를 128으로 설정하면 지도 전체를 다 탐색하는데, 매우 느려질뿐만 아니라, 대부분의 경우 불필요한 탐색이 된다
		// maxRange 는 16 ~ 64가 적당하다
		// 값을 찾아내라. = BaseLocation.isStartingLocation 을 체크해서 메인이면 저값. 나머진 다른값
		int maxRange = 0; // maxRange = BWAPI::Broodwar->mapWidth()/4;
			
		if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SpiralMethod.ordinal()) {

			// desiredPosition 으로부터 시작해서 spiral 하게 탐색하는 방법
			// 처음에는 아래 방향 (0,1) -> 오른쪽으로(1,0) -> 위로(0,-1) -> 왼쪽으로(-1,0) -> 아래로(0,1) -> ..
			
			if(InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.CIRCUITBREAKER) {
				maxRange = 23;
				for (BaseLocation base : BWTA.getStartLocations()) {
//					if(base.isStartLocation() && TilePositionUtils.equals(base.getTilePosition(), desiredPosition)) {
					if(TilePositionUtils.equals(base.getTilePosition(), desiredPosition)) {
						maxRange = 32;
						break;
					}
				}

			}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.FIGHTING_SPIRITS) {
				maxRange = 25;
				for (BaseLocation base : BWTA.getStartLocations()) {
//					if(base.isStartLocation() && TilePositionUtils.equals(base.getTilePosition(), desiredPosition)) {
					if(TilePositionUtils.equals(base.getTilePosition(), desiredPosition)) {
						maxRange = 36;
						break;
					}
				}
			}else {
				maxRange = 20;
				for (BaseLocation base : BWTA.getStartLocations()) {
//					if(base.isStartLocation() && TilePositionUtils.equals(base.getTilePosition(), desiredPosition)) {
					if(TilePositionUtils.equals(base.getTilePosition(), desiredPosition)) {
						maxRange = 35;
						break;
					}
				}
			}
			
//			FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear maxrange ==>> "+ buildingType + " :: " + desiredPosition + " ::" + maxRange + " :: buildingGapSpace => " +buildingGapSpace);
	            
            int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			int spiralMaxLength = 1;
			int numSteps = 0;
			boolean isFirstStep = true;

			int spiralDirectionX = 0;
			int spiralDirectionY = 1;
			while (spiralMaxLength < maxRange) {
				if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {
//					FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace before PlaceFinder seedPosition true :: " + System.currentTimeMillis() + " :: " + buildingType + " :: " + desiredPosition);
					boolean isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, buildingGapSpace);
//					FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace after PlaceFinder seedPosition true ==>> "  + System.currentTimeMillis() + " :: " + buildingType + " :: " + desiredPosition + " :: " + isPossiblePlace);
					if (isPossiblePlace) {
                        if (b.getType() == UnitType.Terran_Factory) {
                        	System.out.println("finding place for fac: " + BlockingEntrance.Instance().loc);
                            int currentXPlus = currentX;
                            int adjust =0;

                            while (true) {
                                if (BlockingEntrance.Instance().loc == Location.Eleven || BlockingEntrance.Instance().loc == Location.Seven ) {
                                    currentXPlus--;
                                    adjust = 1;
                                    System.out.println("finding place for fac minus");
                                } else {
                                    currentXPlus++;
                                    adjust = -1;
                                    System.out.println("finding place for fac plus");
                                }
                                if (currentXPlus < 0 || currentXPlus + 3 > MyBotModule.Broodwar.mapWidth()) {
                                    break;
                                }
                                boolean isPossiblePlaceAjust = canBuildHereWithSpace(new TilePosition(currentXPlus, currentY), b, buildingGapSpace);
                                if (!isPossiblePlaceAjust) {
                                	System.out.println("finding place for fac stop move");
                                    break;
                                }
                            }

                            resultPosition = new TilePosition(currentXPlus+adjust, currentY);
                            break;
                        }else {
                            resultPosition = new TilePosition(currentX, currentY);
                            break;
                        }
					}
				}

				currentX = currentX + spiralDirectionX;
				currentY = currentY + spiralDirectionY;
				numSteps++;
				
				// 다른 방향으로 전환한다
				if (numSteps == spiralMaxLength) {
					numSteps = 0;

					if (!isFirstStep)
						spiralMaxLength++;

					isFirstStep = !isFirstStep;

					if (spiralDirectionX == 0) {
						spiralDirectionX = spiralDirectionY;
						spiralDirectionY = 0;
					} else {
						spiralDirectionY = -spiralDirectionX;
						spiralDirectionX = 0;
					}
				}
			}
			
		} else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal()) {
			//서플라이 디팟 용 로직(4X4)
			// y축부터 시작. 최초 포지션에서 X축 + 3 넘어가면 Y축 +2
			int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			int depostSizeX = 3;
			int depostSizeY = 2;
			boolean isPossiblePlace = false;
			
			if(BlockingEntrance.Instance().xinc) {
				if(BlockingEntrance.Instance().yinc) {
//					1시
					for(int y_position  = 0; y_position < BlockingEntrance.Instance().maxSupplyCntY ; y_position ++){
						for(int x_position= 0; x_position < BlockingEntrance.Instance().maxSupplyCntX ; x_position ++){	
							if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

								isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

								if (isPossiblePlace) {
									resultPosition = new TilePosition(currentX, currentY);
									break;
								}
								//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
							}
							
							currentX = currentX + depostSizeX;
//							currentY = currentY + depostSizeY;
							//currentY = currentY + spiralDirectionY;
						}
						if (isPossiblePlace) {
							break;
						}
						
//						currentY = desiredPosition.getY();
//						currentX = currentX + depostSizeX;
						currentX = desiredPosition.getX();
						currentY = currentY + depostSizeY;
						
						resultPosition = TilePosition.None;
//						FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 1시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
					
				}else {
//					5시
					for(int y_position  = BlockingEntrance.Instance().maxSupplyCntY; y_position > 0 ; y_position --){
						for(int x_position= 0; x_position < BlockingEntrance.Instance().maxSupplyCntX ; x_position ++){	
							if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

								isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

								if (isPossiblePlace) {
									resultPosition = new TilePosition(currentX, currentY);
									break;
								}
								//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
							}
	
							currentX = currentX + depostSizeX;
//							FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 5시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
//							currentY = currentY + depostSizeY;
							//currentY = currentY + spiralDirectionY;
						}
						
						if (isPossiblePlace) {
							break;
						}

						
//						currentY = desiredPosition.getY();
//						currentX = currentX + depostSizeX;
						currentX = desiredPosition.getX();
						currentY = currentY - depostSizeY;
						resultPosition = TilePosition.None;
//						FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 5시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
					
				}
				
			}else {
				if(BlockingEntrance.Instance().yinc) {
//					11시
					for(int y_position  = 0; y_position < BlockingEntrance.Instance().maxSupplyCntY ; y_position ++){
						for(int x_position= BlockingEntrance.Instance().maxSupplyCntX; x_position > 0 ; x_position --){	
							if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

								isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

								if (isPossiblePlace) {
									resultPosition = new TilePosition(currentX, currentY);
									break;
								}
								//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
							}
							
							currentX = currentX - depostSizeX;
//							FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 11시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
//							currentY = currentY + depostSizeY;
							//currentY = currentY + spiralDirectionY;
						}
						if (isPossiblePlace) {
							break;
						}
						
//						currentY = desiredPosition.getY();
//						currentX = currentX + depostSizeX;
						currentX = desiredPosition.getX();
						currentY = currentY + depostSizeY;
						resultPosition = TilePosition.None;
//						FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 11시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
				}else {
//					7시
					for(int y_position  = BlockingEntrance.Instance().maxSupplyCntY; y_position > 0 ; y_position --){
						for(int x_position= BlockingEntrance.Instance().maxSupplyCntX; x_position > 0 ; x_position --){	
							if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

								isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

								if (isPossiblePlace) {
									resultPosition = new TilePosition(currentX, currentY);
									break;
								}
								//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
							}
							
							currentX = currentX - depostSizeX;
//							FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 7시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
//							currentY = currentY + depostSizeY;
							//currentY = currentY + spiralDirectionY;
						}
						if (isPossiblePlace) {
							break;
						}
						
//						currentY = desiredPosition.getY();
//						currentX = currentX + depostSizeX;
						currentX = desiredPosition.getX();
						currentY = currentY - depostSizeY;
						resultPosition = TilePosition.None;
//						FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 7시:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
				}
			}
			
		} else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		} else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		}

//		FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear PlaceFinder End :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + desiredPosition);
		return resultPosition;
	}

	/// 해당 위치에 건물 건설이 가능한지 여부를 buildingGapSpace 조건을 포함해서 판단하여 리턴합니다<br>
	/// Broodwar 의 canBuildHere, isBuildableTile, isReservedTile 를 체크합니다
	public final boolean canBuildHereWithSpace(TilePosition position, final ConstructionTask b, int buildingGapSpace)
	{
		//if we can't build here, we of course can't build here with space
//		FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace PlaceFinder start :: " + System.currentTimeMillis() + " :: " + b.getType() + " :: " + position);
//		FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace start :: " + position);
		if (!canBuildHere(position, b)) {
//			FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace :: canBuildHere false :: "+ b.getType() + " // " + position  +" // buildingGapSpace :: " + buildingGapSpace);
			return false;
		}

		// height and width of the building
		int width = b.getType().tileWidth();
		int height = b.getType().tileHeight();

		// define the rectangle of the building spot
		// 건물 크기보다 상하좌우로 더 큰 사각형
		int startx;
		int starty;
		int endx;
		int endy;

		//buildingGapSpace = 0;@@@@@@
		
		boolean horizontalOnly = false;
		boolean canAddonBuilding = false;

		if (b.getType().isAddon()) { // Addon 타입의 건물일 경우에는, 그 Addon 건물 왼쪽에 whatBuilds 건물이 있는지를 체크한다
			final UnitType builderType = b.getType().whatBuilds().first;
			TilePosition builderTile = new TilePosition(position.getX() - builderType.tileWidth(), position.getY() + 2 - builderType.tileHeight());
			

			startx = builderTile.getX() - buildingGapSpace;
			starty = builderTile.getY() - buildingGapSpace;
			endx = position.getX() + width + buildingGapSpace;
			endy = position.getY() + height + buildingGapSpace;

			// builderTile에 Lifted 건물이 아니고 whatBuilds 건물이 아닌 건물이 있는지 체크
			
			if(!hasWhatBuilds(builderTile, builderType)) return false;
//			for (int i = 0; i <= builderType.tileWidth(); ++i) {
//				for (int j = 0; j <= builderType.tileHeight(); ++j) {
//					for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(builderTile.getX() + i, builderTile.getY() + j)) {
//						if ((unit.getType() != builderType) && (!unit.isLifted())) {
//							return false;
//						}
//					}
//				}
//			}
		} else {
			//make sure we leave space for add-ons. These types of units can have addon:
			if (b.getType() == UnitType.Terran_Starport ||
				b.getType() == UnitType.Terran_Factory
//				b.getType() == UnitType.Terran_Starport 
				) {
				width += 3;
//				width += 2;
				canAddonBuilding = true;
//				buildingGapSpace = 0;
//				horizontalOnly = true;
			}
//			else if(b.getType() == UnitType.Terran_Command_Center) {
//				width += 2;
//				canAddonBuilding = true;
//			}
	
//			if( (position.getX() == BlockingEntrance.Instance().starport1.getX() && position.getY() == BlockingEntrance.Instance().starport1.getY() && b.getType() == UnitType.Terran_Starport)
//				|| (position.getX() == BlockingEntrance.Instance().starport2.getX() && position.getY() == BlockingEntrance.Instance().starport2.getY() && b.getType() == UnitType.Terran_Starport)
//				|| (position.getX() == BlockingEntrance.Instance().factory.getX() && position.getY() == BlockingEntrance.Instance().factory.getY() && b.getType() == UnitType.Terran_Factory)
//				|| (position.getX() == BlockingEntrance.Instance().barrack.getX() && position.getY() == BlockingEntrance.Instance().barrack.getY() && b.getType() == UnitType.Terran_Barracks)){
//				
////				FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace chk initial point:: buildingType "+ b.getType() + " // position :: " + position  +" // buildingGapSpace :: " + buildingGapSpace);
//				width = 0;
//				height = 0;
//				buildingGapSpace = 0;
//			}


			// 상하좌우에 buildingGapSpace 만큼 간격을 띄운다
			if (horizontalOnly == false) {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY() - buildingGapSpace;
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height + buildingGapSpace;
			}
			// 좌우로만 buildingGapSpace 만큼 간격을 띄운다
			else {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY();
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height;
			}



			// 건물이 차지할 공간 뿐 아니라 주위의 buildingGapSpace 공간까지 다 비어있는지, 건설가능한 타일인지, 예약되어있는것은 아닌지, TilesToAvoid 에 해당하지 않는지 체크
			for (int x = startx; x < endx; x++)
			{
				for (int y = starty; y < endy; y++)
				{
					
//					if(canAddonBuilding == true && 
//						((x == position.getX() + 4 && y == position.getY())
//						||(x == position.getX() + 5 && y == position.getY()))) {
////						FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace :: hereis up of addon :: no tile check :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
//						continue;
//					}
					
					if( (TilePositionUtils.equals(position, BlockingEntrance.Instance().first_supple)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().second_supple)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().starport1)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().starport2)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().factory)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().barrack))
						&& InitialBuildProvider.Instance().getAdaptStrategyStatus() != InitialBuildProvider.AdaptStrategyStatus.COMPLETE
					){
						continue;
					}
					
					if (b.getType() == UnitType.Terran_Factory ||
						b.getType() == UnitType.Terran_Starport ) {
						if(x > startx && x < endx -1 && y > starty && y < endy-1) {
//								System.out.println(" avoid tile check :: " + position + " :: (" + x + " , " + y + ")");
							if (isBuildableTile(b, x, y) == false) {
//									FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isBuildableTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
								return false;
							}
						}
					}else {
					// if we can't build here, or space is reserved, we can't build here
						if (isBuildableTile(b, x, y) == false) {
	//						FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isBuildableTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
					}

//					if (isReservedTile(x, y)) {
////						FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isReservedTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
//						return false;
//					}
					
//					if(b.getType() != UnitType.Terran_Command_Center){
//						if(isTilesToAvoidBase(x, y)) {
//							return false;
//						}
//					}
					
//					20180815. hkk. 커맨드 센터 일경우. new 커맨드 센터 본진이, old 커맨드 센터 컴셋 위치를 침범하면 안된다.
					if(b.getType() == UnitType.Terran_Command_Center) {
						if(x != position.getX() + 4 && x != position.getX() + 5 && x != position.getX() + 6) {
							if(isTilesToAvoidComsat(x, y)) {
								return false;
							}
						}
					}

					// ResourceDepot / Addon 건물이 아닌 일반 건물의 경우, BaseLocation 과 Geyser 사이 타일 (TilesToAvoid) 에는 건물을 짓지 않는다
//					20180719. hkk. 저그전 대비 배럭과 서플 가스 주변에 붙여짓기 필요
					if (b.getType().isResourceDepot() == false && b.getType().isAddon() == false
							&& b.getType() != UnitType.Terran_Bunker && b.getType() != UnitType.Terran_Missile_Turret
							&& b.getType() != UnitType.Terran_Barracks && b.getType() != UnitType.Terran_Supply_Depot
							&& b.getType() != UnitType.Terran_Factory && b.getType() != UnitType.Terran_Starport) {
						if (isTilesToAvoid(x, y)) {
//							FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isTilesToAvoid false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
					}
					
					if (b.getType() == UnitType.Terran_Factory ||
						b.getType() == UnitType.Terran_Starport ) {
						if(x > startx && x < endx -1 && y > starty && y < endy-1) {
//							System.out.println(" avoid tile check :: " + position + " :: (" + x + " , " + y + ")");
							if (isTilesToAvoid(x, y)) {
								return false;
							}
						}
					}
//					20180806. hkk. 1,2 번쨰 터렛은 지정
//					}else if (b.getType() == UnitType.Terran_Missile_Turret) {
					
//					if (b.getType() == UnitType.Terran_Missile_Turret && InitialBuildProvider.Instance().getAdaptStrategyStatus() != InitialBuildProvider.AdaptStrategyStatus.COMPLETE) {
//							if (isTilesToAvoidAddonBuilding(x, y)) {
//								return false;
//							}
//					}else if(b.getType() != UnitType.Terran_Factory && b.getType() != UnitType.Terran_Starport){
//						if (isTilesToAvoidAddonBuilding(x, y)) {
////							FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isTilesToAvoidFac false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
//							return false;
//						}
//					}
					
					
					
					//서플라이 지역은 서플라이 / 아카데미 / 아머리 외에는 지을수 없다.
					if (b.getType() != UnitType.Terran_Supply_Depot
						&& b.getType() != UnitType.Terran_Academy
						&& b.getType() != UnitType.Terran_Armory
//						&& b.getType() != UnitType.Terran_Barracks
						) {
						if (isTilesToAvoidSupply(x, y)) {
//							FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isTilesToAvoidSupply false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
					}
					
					
//					if (b.getType() != UnitType.Terran_Factory && b.getType() != UnitType.Terran_Starport) {
						if (isTilesToAvoidAbsolute(x, y)) {
	//						FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isTilesToAvoidAbsolute false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
//					}
				}
			}
		}

		// if this rectangle doesn't fit on the map we can't build here
		if (startx < 0 || starty < 0 || endx > MyBotModule.Broodwar.mapWidth() || endx < position.getX() + width || endy > MyBotModule.Broodwar.mapHeight()) {
//			FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace return false :: "+ b.getType() + " // buildingGapSpace :: " + buildingGapSpace);
			return false;
		} else {
//			FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace return true :: "+ b.getType() + " // buildingGapSpace :: " + buildingGapSpace);
			return true;
		}

	}

	/// 해당 위치에 건물 건설이 가능한지 여부를 리턴합니다 <br>
	/// Broodwar 의 canBuildHere 및 _reserveMap 와 isOverlapsWithBaseLocation 을 체크
	public final boolean canBuildHere(TilePosition position, final ConstructionTask b) {
		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType())) {
//			FileUtils.appendTextToFile("log.txt", "\n canBuildHere ==> !Prebot.Broodwar.canBuildHere :: " + position);
			return false;
		}
		
		// check the reserve map
		for (int x = position.getX() ; x < position.getX() + b.getType().tileWidth(); x++)
		{
			for (int y = position.getY() ; y < position.getY() + b.getType().tileHeight(); y++)
			{
				//if (reserveMap.get(x).get(y))
				if (reserveMap[x][y])
				{
//					FileUtils.appendTextToFile("log.txt", "\n canBuildHere ==> can not build here reserveMap :: " + x + " , " + y);
					return false;
				}
			}
		}

		// if it overlaps a base location return false
		// ResourceDepot 건물이 아닌 다른 건물은 BaseLocation 위치에 짓지 못하도록 한다
		if (isOverlapsWithBaseLocation(position, b.getType())) {
			return false;
		} else {
			return true;
		}
	}

	/// seedPosition 근처에서 Refinery 건물 건설 가능 위치를 탐색해서 리턴합니다 <br>
	/// 지도상의 여러 가스 광산 (Resource_Vespene_Geyser) 중 예약되어있지 않은 곳(isReservedTile), 다른 섬이 아닌 곳, 이미 Refinery 가 지어져있지않은 곳 중<br> 
	/// seedPosition 과 가장 가까운 곳을 리턴합니다
	public final TilePosition getRefineryPositionNear(TilePosition seedPosition) {
		
		if (!TilePositionUtils.isValidTilePosition(seedPosition)) {
			seedPosition = InfoUtils.myBase().getTilePosition();
		}
		
//		FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear start :: " + seedPosition + " :: " + Prebot.Broodwar.getFrameCount());

		//TODO BASICBOT 1.1 버젼의 가스 처리다.. 확인해 봐야함.
//		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers())
//		{
//			// geyser->getPosition() 을 하면, Unknown 으로 나올 수 있다.
//			// 반드시 geyser->getInitialPosition() 을 사용해야 한다
//			Position geyserPos = geyser.getInitialPosition();
//			TilePosition geyserTilePos = geyser.getInitialTilePosition();
//
//			// 이미 예약되어있는가
//			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
//				continue;
//			}
//
//			// geyser->getType() 을 하면, Unknown 이거나, Resource_Vespene_Geyser 이거나, Terran_Refinery 와 같이 건물명이 나오고, 
//			// 건물이 파괴되어도 자동으로 Resource_Vespene_Geyser 로 돌아가지 않는다
//			// geyser 위치에 있는 유닛들에 대해 isRefinery() 로 체크를 해봐야 한다
//
//			// seedPosition 으로부터 16 TILE_SIZE 거리 이내에 있는가
//			// Fighting Spirit 맵처럼 seedPosition 으로부터 동일한 거리 내에 geyser 가 여러개 있을 수 있는 경우 Refinery 건물을 짓기 위해서는 seedPosition 을 정확하게 입력해야 한다
//			double thisDistance = geyserTilePos.getDistance(seedPosition);
//			
//			if (thisDistance <= 16 && thisDistance < minGeyserDistanceFromSeedPosition)
//			{
//				minGeyserDistanceFromSeedPosition = thisDistance;
//				closestGeyser = geyser.getInitialTilePosition();
//			}
//		}

		TilePosition closestGeyser = TilePosition.None;
		double minGeyserDistanceFromSeedPosition = 100000000;
		
		// 전체 geyser 중에서 seedPosition 으로부터 16 TILE_SIZE 거리 이내에 있는 것을 찾는다
		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers()) {
			// geyser->getPosition() 을 하면, Unknown 으로 나올 수 있다.
			// 반드시 geyser->getInitialPosition() 을 사용해야 한다
//			FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear getStaticGeysers :: " + geyser.getTilePosition());

			Position geyserPos = geyser.getInitialPosition();
			TilePosition geyserTilePos = geyser.getInitialTilePosition();

			// 이미 예약되어있는가
			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
//				FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear geyserTilePos is reserved :: " + geyserTilePos);
				continue;
			}

			// if it is not connected fron seedPosition, it is located in another island
			if (!BWTA.isConnected(seedPosition, geyserTilePos)) {
//				FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear geyserTilePos not connected :: " + geyserTilePos + " :: " + seedPosition);
				continue;
			}

			// 이미 지어져 있는가
			boolean refineryAlreadyBuilt = false;
			List<Unit> alreadyBuiltUnits = MyBotModule.Broodwar.getUnitsInRadius(geyserPos, 4 * BuildConfig.TILE_SIZE);
			for (Unit u : alreadyBuiltUnits) {
				if (u.getType().isRefinery() && u.exists()) {
//					FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear is alreadyBuiltUnits");
					refineryAlreadyBuilt = true;
					break;
				}
			}

			if (!refineryAlreadyBuilt) {
				double thisDistance = PositionUtils.getGroundDistance(geyserPos, seedPosition.toPosition());
				if (thisDistance < minGeyserDistanceFromSeedPosition) {
					minGeyserDistanceFromSeedPosition = thisDistance;
					closestGeyser = geyser.getInitialTilePosition();
//					FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear set closestGeyser :: " + closestGeyser);
				}
			}
		}
		return closestGeyser;
	}

    public final Unit getGeyserNear(TilePosition seedPosition) {
        if (!TilePositionUtils.isValidTilePosition(seedPosition)) {
            seedPosition = InfoUtils.myBase().getTilePosition();
        }

        Unit closestGeyser = null;
        double minGeyserDistanceFromSeedPosition = 100000000;

        // 전체 geyser 중에서 seedPosition 으로부터 16 TILE_SIZE 거리 이내에 있는 것을 찾는다
        for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers()) {
            // geyser->getPosition() 을 하면, Unknown 으로 나올 수 있다.
            // 반드시 geyser->getInitialPosition() 을 사용해야 한다

            Position geyserPos = geyser.getInitialPosition();
            TilePosition geyserTilePos = geyser.getInitialTilePosition();

            // 이미 예약되어있는가
            if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
                continue;
            }

            // if it is not connected fron seedPosition, it is located in another island
            if (!BWTA.isConnected(seedPosition, geyserTilePos)) {
                continue;
            }

            double thisDistance = PositionUtils.getGroundDistance(geyserPos, seedPosition.toPosition());
            if (thisDistance < minGeyserDistanceFromSeedPosition) {
                minGeyserDistanceFromSeedPosition = thisDistance;
                closestGeyser = geyser;
            }
        }
        return closestGeyser;
    }

	/// 해당 위치가 BaseLocation 과 겹치는지 여부를 리턴합니다<br>
	/// BaseLocation 에는 ResourceDepot 건물만 건설하고, 다른 건물은 건설하지 않기 위함입니다
	public final boolean isOverlapsWithBaseLocation(TilePosition tile, UnitType type)
	{
		// if it's a resource depot we don't care if it overlaps
		if (type.isResourceDepot() || type == UnitType.Terran_Barracks  || type == UnitType.Terran_Bunker) {
			return false;
		}

		// dimensions of the proposed location
		int tx1 = tile.getX();
		int ty1 = tile.getY();
		int tx2 = tx1 + type.tileWidth()-1;
		int ty2 = ty1 + type.tileHeight()-1;

		// for each base location
		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// dimensions of the base location
			int bx1 = base.getTilePosition().getX();
			int by1 = base.getTilePosition().getY();
			int bx2 = bx1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileWidth()-1;
			int by2 = by1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileHeight()-1;

			// conditions for non-overlap are easy
			boolean noOverlap = (tx2 < bx1) || (tx1 > bx2) || (ty2 < by1) || (ty1 > by2);

			// if the reverse is true, return true
			if (!noOverlap) {
				return true;
			}
		}

		// otherwise there is no overlap
		return false;
	}

	/// 건물 건설 가능 타일인지 여부를 리턴합니다
	public final boolean isBuildableTile(final ConstructionTask b, int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			return false;
		}

		// 맵 데이터 뿐만 아니라 빌딩 데이터를 모두 고려해서 isBuildable 체크
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (MyBotModule.Broodwar.isBuildable(x, y, true) == false)
		{
			return false;
		}

		// constructionWorker 이외의 다른 유닛이 있으면 false를 리턴한다
		for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(x, y))
		{
			if ((b.getConstructionWorker() != null) && (unit != b.getConstructionWorker()))
			{
				return false;
			}
		}

		return true;
	}

	/// 건물 건설 예정 타일로 예약해서, 다른 건물을 중복해서 짓지 않도록 합니다
	public void reserveTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, true);
				// C++ : reserveMap[x][y] = true;
			}
		}*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, true);
				reserveMap[x][y] = true;
				// C++ : reserveMap[x][y] = true;
			}
		}
	}
	
	/// 건물 건설 예정 타일로 예약했던 것을 해제합니다
	public void freeTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, false);
				// C++ : reserveMap[x][y] = false;
			}
		}*/
		if(position == TilePosition.None) {
			return;
		}
		
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, false);
				reserveMap[x][y] = false;
				// C++ : reserveMap[x][y] = false;
			}
		}
	}

	// 건물 건설 예약되어있는 타일인지 체크
	public final boolean isReservedTile(int x, int y)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap.get(x).get(y);*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap[x][y];
	}

	/// reserveMap을 리턴합니다
	public boolean[][] getReserveMap() {
		return reserveMap;
	}

	/// (x, y) 가 BaseLocation 과 Mineral / Geyser 사이의 타일에 해당하는지 여부를 리턴합니다
	public final boolean isTilesToAvoid(int x, int y)
	{
//		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
//			FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
//			return true;
//		}
		int rwidth = tilesToAvoid.length;
		int rheight = tilesToAvoid[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}
		
//		for (TilePosition t : tilesToAvoid) {
		return tilesToAvoid[x][y];
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}

//		return false;
	}
	
	public final boolean isTilesToAvoidAddonBuilding(int x, int y)
	{
//		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
//			FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
//			return true;
//		}
		
//		for (TilePosition t : tilesToAvoidAbsolute) {
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}
		int rwidth = tilesToAvoidAddonBuilding.length;
		int rheight = tilesToAvoidAddonBuilding[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}
//
//		return false;
		return tilesToAvoidAddonBuilding[x][y];
	}
	
//	public final boolean isTilesToAvoidBase(int x, int y)
//	{
////		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
////		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
////		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
////		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
////		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
////		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
////			FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
////			return true;
////		}
//		
//		for (TilePosition t : tilesToBaseLocationAvoid) {
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}
//
//		return false;
//	}
	
	public final boolean isTilesToAvoidComsat(int x, int y)
	{
//		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
//			FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
//			return true;
//		}
		
//		for (TilePosition t : tilesToAvoidComSat) {
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}
		
//
//		return false;
		int rwidth = tilesToAvoidComSat.length;
		int rheight = tilesToAvoidComSat[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}
		return tilesToAvoidComSat[x][y];
	}
	
	
	public final boolean isTilesToAvoidAbsolute(int x, int y)
	{
//		for (TilePosition t : tilesToAvoidAbsolute) {
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}
//
//		return false;
		int rwidth = tilesToAvoidAbsolute.length;
		int rheight = tilesToAvoidAbsolute[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}
		return tilesToAvoidAbsolute[x][y];
	}
	
	/// (x, y) 가 서플라이 지역이라면 지을수 없다.
	public final boolean isTilesToAvoidSupply(int x, int y)
	{
//		for (TilePosition t : tilesToAvoidSupply) {
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}
//
//		return false;
		int rwidth = tilesToAvoidSupply.length;
		int rheight = tilesToAvoidSupply[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}
		return tilesToAvoidSupply[x][y];
	}

	/// BaseLocation 과 Mineral / Geyser 사이의 타일들을 찾아 _tilesToAvoid 에 저장합니다<br>
	/// BaseLocation 과 Geyser 사이, ResourceDepot 건물과 Mineral 사이 공간으로 건물 건설 장소를 정하면<br> 
	/// 일꾼 유닛들이 장애물이 되어서 건설 시작되기까지 시간이 오래걸리고, 지어진 건물이 장애물이 되어서 자원 채취 속도도 느려지기 때문에, 이 공간은 건물을 짓지 않는 공간으로 두기 위함입니다
	public void setTilesToAvoid()
	{
		// ResourceDepot 건물의 width = 4 타일, height = 3 타일
		// Geyser 의            width = 4 타일, height = 2 타일
		// Mineral 의           width = 2 타일, height = 1 타일

		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// Island 일 경우 건물 지을 공간이 절대적으로 좁기 때문에 건물 안짓는 공간을 두지 않는다
			if (base.isIsland()) continue;
			if (BWTA.isConnected(base.getTilePosition(), InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition()) == false) continue;

			// dimensions of the base location
			int bx0 = base.getTilePosition().getX();
			int by0 = base.getTilePosition().getY();
			int bx4 = base.getTilePosition().getX() + 4;
			int by3 = base.getTilePosition().getY() + 3;

			// BaseLocation 과 Geyser 사이의 타일을 BWTA::getShortestPath 를 사용해서 구한 후 _tilesToAvoid 에 추가
			for (Unit geyser : base.getGeysers())
			{
				TilePosition closeGeyserPosition = geyser.getInitialTilePosition();

				// dimensions of the closest geyser
				int gx0 = closeGeyserPosition.getX();
				int gy0 = closeGeyserPosition.getY();
				int gx4 = closeGeyserPosition.getX() + 4;
				int gy2 = closeGeyserPosition.getY() + 2;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = gx0; k < gx4; k++) {
							for (int l = gy0; l < gy2; l++) {
								List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, l));
								for (TilePosition t : tileList) {
//									tilesToAvoid.add(t);
									tilesToAvoid[t.getX()][t.getY()] = true;
								}
							}
						}
					}
				}

			}

			// BaseLocation 과 Mineral 사이의 타일을 BWTA::getShortestPath 를 사용해서 구한 후 _tilesToAvoid 에 추가
			for (Unit mineral : base.getMinerals())
			{
				TilePosition closeMineralPosition = mineral.getInitialTilePosition();

				// dimensions of the closest mineral
				int mx0 = closeMineralPosition.getX();
				int my0 = closeMineralPosition.getY();
				int mx2 = mx0 + 2;
				int my1 = my0 + 1;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = mx0; k < mx2; k++) {
							List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, my0));
							for (TilePosition t : tileList) {
//								tilesToAvoid.add(t);	
								tilesToAvoid[t.getX()][t.getY()] = true;
							}
						}
					}
				}
				if(InformationManager.Instance().enemyRace != Race.Protoss) {
					
					int fromx = mineral.getTilePosition().getX()-2;
					int fromy = mineral.getTilePosition().getY()-2;
					
					for (int x = fromx; x > 0 && x < fromx + 6 && x < MyBotModule.Broodwar.mapWidth(); x++)
				        {
				            for (int y = fromy ; y > 0 && y < fromy + 6 && y < MyBotModule.Broodwar.mapHeight(); y++)
				            {
//							TilePosition temp = new TilePosition(x,y);
//							tilesToAvoid.add(temp);
							tilesToAvoid[x][y] = true;
						}
					}
				}
			}
		}
	}
	
	

    public void setTilesToAvoidForFirstGas()
	{
		Unit firstgas = InformationManager.Instance().getMyfirstGas();
	
		int fromx = firstgas.getTilePosition().getX()-1;
		int fromy = firstgas.getTilePosition().getY()-1;
		
		for (int x = fromx; x > 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth(); x++)
	        {
	            for (int y = fromy ; y > 0 && y < fromy + 6 && y < MyBotModule.Broodwar.mapHeight(); y++)
	            {
	            	if(fromx < x && x < fromx+5 && fromy < y && y < fromy+3){
						continue;
					}
//				TilePosition temp = new TilePosition(x,y);
//				tilesToAvoid.add(temp);
	            tilesToAvoid[x][y] = true;
			}
		}
	}
    
    public void setTilesToAvoidTurret() {
		
    	for(int a = 1; a<=2; a++) {
	    	int turret_x = BlockingEntrance.Instance().getTurretPosition(a).getX();
			int turret_y = BlockingEntrance.Instance().getTurretPosition(a).getY();
			
			TilePosition turret = new TilePosition(turret_x,turret_y);
			if(TilePositionUtils.isValidTilePosition(turret)) {
//				tilesToAvoid.add(turret);
				tilesToAvoid[turret.getX()][turret.getY()] = true;
			}
    	}
		
	}
	/// BaseLocation 과 Mineral / Geyser 사이의 타일들의 목록을 리턴합니다		
//	public Set<TilePosition> getTilesToAvoid() {
//		return tilesToAvoid;
//	}
//
//	public Set<TilePosition> getTilesToAvoidAbsolute() {
//		return tilesToAvoidAbsolute;
//	}
//	
//	public Set<TilePosition> getTilesToAvoidSupply() {
//		return tilesToAvoidSupply;
//	}
    
    public boolean getTilesToAvoid(int x, int y) {
		return tilesToAvoid[x][y];
	}

	public boolean getTilesToAvoidAbsolute(int x, int y) {
		return tilesToAvoidAbsolute[x][y];
	}
	
	public boolean getTilesToAvoidSupply(int x, int y) {
		return tilesToAvoidSupply[x][y];
	}
	
	public boolean getTilesToAvoidAddonBuilding(int x, int y) {
		return tilesToAvoidAddonBuilding[x][y];
	}
	
//	public Set<TilePosition> getTilesToAvoidAddonBuilding() {
//		return tilesToAvoidAbsolute;
//	}
	
//	public Set<TilePosition> getTilesToAvoidBaseLocation() {
//		return tilesToBaseLocationAvoid;
//	}
	
	public void setTilesToAvoidAddonBuilding(Unit unit) {
		
		int fromx = unit.getTilePosition().getX() - 1;
		int fromy = unit.getTilePosition().getY() - 1;
		

		for (int x = fromx; x > 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth(); x++)
//		for (int x = fromx; x > 0 && x < fromx + 7 && x < Prebot.Broodwar.mapWidth(); x++)
	        {
//	            for (int y = fromy ; y > 0 && y < fromy + 5 && y < Prebot.Broodwar.mapHeight(); y++)
			for (int y = fromy; y > 0 && y < fromy + 5 && y < MyBotModule.Broodwar.mapHeight(); y++)
            {
				if(x==fromx + 5 || x==fromx + 6 || x==fromx + 7){
					if(y == fromy) {
						continue;
					}
//					else if(y > fromy) {
//						tilesToAvoidAddonBuilding[x][y] = true;
//					}
				}
				tilesToAvoid[x][y] = true;
				
//				if(!isTilesToAvoidSupply(x, y)) {
//					TilePosition temp = new TilePosition(x,y);
////					System.out.println("setTilesToAvoidAddonBuilding :: " + temp);
////					tilesToAvoidAddonBuilding.add(temp);
////					tilesToAvoid.add(temp);
//					tilesToAvoidAbsolute.add(temp);
//            	}
			}
		}
		
//		FileUtils.appendTextToFile("log.txt", "\n setTilesToAvoidAddonBuilding :: tilesToAvoidAddonBuilding ::" + tilesToAvoidAddonBuilding);
//		
//		for(TilePosition point : tilesToAvoidAddonBuilding) {
//			FileUtils.appendTextToFile("avoidTileAddonBuilding.txt", "\n setTilesToAvoidAddonBuilding :: " + point);
//			System.out.println("setTilesToAvoidAddonBuildingFree :: " + point);
//		}
	}
	
	public void setTilesToAvoidAddonBuildingFree(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()-1;
		int fromy = unit.getTilePosition().getY()-1;
		
		boolean allFree = false;

//		20180821. hkk. 애드온 건물이 부셔졌는데, 마스터 건물이 있거나, 마스터건물이 부셔졌는데, 애드온 건물이 있다면 그 Avoid Tile 은 보존한다.
		if(unit.getType().isAddon()) {
			final UnitType builderType = unit.getType().whatBuilds().first;
			TilePosition builderTile = new TilePosition(fromx + 1 - builderType.tileWidth(), fromy + 1 + 2 - builderType.tileHeight());
			
			
			if(hasWhatBuilds(builderTile, builderType)) {
//				마스터 건물이 있을경우 타일 시작지점도 마스터 포지션으로 재 설정
				allFree = true;
				fromx = builderTile.getX() - 1;
				fromy = builderTile.getY() - 1;
			}

		}else {
			
			if(unit.getAddon() == null) {
				allFree = true;
			}
		}
		
		if(allFree) {
			for (int x = fromx; x > 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth(); x++){
				
				for (int y = fromy; y > 0 && y < fromy + 5 && y < MyBotModule.Broodwar.mapHeight(); y++){
				
			
					if(x==fromx + 5 || x==fromx + 6 || x==fromx + 7){
						if(y == fromy) {
							continue;
						}
//						else if(y > fromy) {
//							tilesToAvoidAddonBuilding[x][y] = false;
//						}
					}
					tilesToAvoid[x][y] = false;

				}
			}
		}

	}
	
	
	public void setTilesToAvoidBaseLocation() {
		
		for(BaseLocation baseLocation : BWTA.getBaseLocations())
		{
			TilePosition cc = baseLocation.getTilePosition();
			int addonX = cc.getX() + 4;
			int addonY = cc.getY();
			for(int x = 0; x < 3 ; x++){
				for(int y = 0;  y < 3 ; y++){

//					TilePosition t = new TilePosition(addonX+x,addonY+y);
//					2018015. hkk. 컴셋 자리는 커맨드센터도 짓지 못하게끔 피해준다.
					if( (x == 4 || x == 5 || x == 6) && TilePositionUtils.equals(cc,InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition())) {
//						System.out.println("comsat position of main command :: " + t);
						tilesToAvoidComSat[addonX+x][addonY+y] = true;
					}
//					tilesToBaseLocationAvoid.add(t);
//					tilesToAvoid.add(t);
//					tilesToAvoidAddonBuilding.add(t);
					//System.out.println("supply region ==>>>>  ("+t.getX()+","+t.getY()+")");
				}
			}
		}
	}
	
	public void setTilesToAvoidSupply(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()-1;
//		int fromx = unit.getTilePosition().getX();
		int fromy = unit.getTilePosition().getY()-1;
//		int fromy = unit.getTilePosition().getY();
		
		/*if(fromx<0){
			fromx=0;
		}
		if(fromy<0){
			fromy =0;
		}*/
		
		for (int x = fromx; x > 0 && x < fromx + 5 && x < MyBotModule.Broodwar.mapWidth(); x++)
//		for (int x = fromx; x > 0 && x < fromx + 7 && x < Prebot.Broodwar.mapWidth(); x++)
	        {
//	            for (int y = fromy ; y > 0 && y < fromy + 5 && y < Prebot.Broodwar.mapHeight(); y++)
			for (int y = fromy ; y > 0 && y < fromy + 4 && y < MyBotModule.Broodwar.mapHeight(); y++)
            {

//				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidSupply[x][y] = true;

			}
		}
	}
	
	public void setTilesToAvoidSupply() {
		
		boolean xinc = BlockingEntrance.Instance().xinc;
		boolean yinc = BlockingEntrance.Instance().yinc;
		
//		System.out.println("setTilesToAvoidSupply start()");
		
		if(InformationManager.Instance().getMapSpecificInformation().getMap() != MapSpecificInformation.GameMap.UNKNOWN) {
			
//			System.out.println("setTilesToAvoidSupply map is not UNKNOWN");
			
//			for(BaseLocation baseLocation : BWTA.getBaseLocations())
			for(BaseLocation baseLocation : BWTA.getStartLocations())
			{
				TilePosition mainbase = baseLocation.getTilePosition();
				
				int supply_x = BlockingEntrance.Instance().getSupplyPosition(mainbase).getX();
				int supply_y = BlockingEntrance.Instance().getSupplyPosition(mainbase).getY();
				
//				System.out.println("main base ==>> " + supply_x +" / " + supply_y);
				
				
//				x축 y축 모두 증가는 변동없음
				if(BlockingEntrance.Instance().xinc) {
					if(!BlockingEntrance.Instance().yinc) {
//						5시
//						supply_x = BlockingEntrance.Instance().getSupplyPosition(mainbase).getX();
						supply_y = supply_y - (2 * (BlockingEntrance.Instance().maxSupplyCntY -1) ); 
					}

					
				}else {
					if(BlockingEntrance.Instance().yinc) {
//						11시
						supply_x = supply_x - (3 * (BlockingEntrance.Instance().maxSupplyCntX -1) ) ;  
					}else {
//						7시
						supply_x = supply_x - (3 * (BlockingEntrance.Instance().maxSupplyCntX -1) ) ;
						supply_y = supply_y - (2 * (BlockingEntrance.Instance().maxSupplyCntY -1) );
					}
				}
				
				int max_supX = BlockingEntrance.Instance().maxSupplyCntX * 3;
				int max_supY = BlockingEntrance.Instance().maxSupplyCntY * 2;
				
				for(int x = 0; x < max_supX + 1  ; x++){
					for(int y = 0;  y < max_supY; y++){
//						for(int x = 0; x < max_supX + 1  ; x++){
//							for(int y = 0;  y < max_supY + 1; y++){		
//						TilePosition t = new TilePosition(supply_x+x,supply_y+y);
						tilesToAvoidSupply[supply_x+x][supply_y+y] = true;
//						System.out.println("supply region ==>>>>  ("+t.getX()+","+t.getY()+")");
					}
				}
				
			}
		}
		
		BlockingEntrance.Instance().xinc = xinc; 
		BlockingEntrance.Instance().yinc = yinc;
		
		for(int a = 1; a<=2; a++) {
	    	TilePosition turret = BlockingEntrance.Instance().getTurretPosition(a);
//			int turret_y = BlockingEntrance.Instance().getTurretPosition(a).getY();
			
//			TilePosition turret = new TilePosition(turret_x,turret_y);
	    	
			if(turret != TilePosition.None) {
				int turret_x = turret.getX();
		    	int turret_y = turret.getY();
				for(int x = 0; x<2; x++) {
					for(int y = 0; y<2; y++) {
//						TilePosition turret_p = new TilePosition(turret_x+x,turret_y+y);
						tilesToAvoid[turret_x+x][turret_y+y] = true;
						tilesToAvoidComSat[turret_x+x][turret_y+y] = true;
					}
					
				}
			}
    	}
	}
	
	public void debugBuildLocationSet() {
//		System.out.println("start debugBuildLocationSet :: " + lastBuilding.size());
		BaseLocation temp = InformationManager.Instance().getLastBuildingLocation();
//		System.out.println("start debugBuildLocationSet :: " + temp.getTilePosition());
		if(lastBuilding.size() == 0) {
//			System.out.println("lastBuilding is null && add debugBuildLocationSet :: " + temp.getTilePosition());
			lastBuilding.add(temp.getTilePosition());
//			System.out.println("lastBuilding is null && add debugBuildLocationSet :: " + temp.getTilePosition());
		}else {
//			System.out.println("lastBuilding is not null && add debugBuildLocationSet :: " + temp.getTilePosition());
			for(TilePosition lastBuildingP : lastBuilding) {
				if(TilePositionUtils.equals(lastBuildingP, temp.getTilePosition())) {
//					System.out.println("lastBuilding is exists :: break" + temp.getTilePosition());
					break;
				}
				lastBuilding.add(temp.getTilePosition());
//				System.out.println("add lastBuilding");
			}
		}
		
		TilePosition lastBuilingFinalLocation = InformationManager.Instance().getLastBuilingFinalLocation();
		if( lastBuildingFinal.size() == 0) {
			lastBuilding.add(lastBuilingFinalLocation);
		}else {

		
			for(TilePosition lastBuildingFinalP : lastBuildingFinal) {
				if(TilePositionUtils.equals(lastBuildingFinalP, lastBuilingFinalLocation)) {
					break;
				}
				lastBuildingFinal.add(lastBuilingFinalLocation);
//				System.out.println("add lastBuildingFinal");
			}
		}
		
		

	}
	
	public void debugBuildLocationPrint() {
//		System.out.println("print debugBuildLocationPrint ");
		int a = 0, b = 0;
		if(lastBuilding.size() != 0) {
//			System.out.println("lastBuilding is not null :: " + lastBuilding);
			for(TilePosition lastBuildingP : lastBuilding) {
				
//				FileUtils.appendTextToFile("logFinalBuilding.txt", "\n debugBuildLocation :: "+ a +" of lastBuildingPosition :: " + lastBuildingP);
//				System.out.println(" syso debugBuildLocation :: " + lastBuildingP);
				a++;
			}
		}
		
		if(lastBuildingFinal.size() != 0) {
//			System.out.println("lastBuildingFinal is not null :: " + lastBuildingFinal);
			for(TilePosition lastBuildingFinalP : lastBuildingFinal) {
//				FileUtils.appendTextToFile("logFinalBuilding.txt", "\n debugBuildLocation :: " + b+ " of lastBuildingFinalPosition :: " + lastBuildingFinalP);
				b++;
			}
		}
	}
	
	public boolean hasWhatBuilds(TilePosition position, UnitType unitType) {

		for (int i = 0; i <= unitType.tileWidth(); ++i) {
			for (int j = 0; j <= unitType.tileHeight(); ++j) {
				for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(position.getX() + i, position.getY() + j)) {
					if ((unit.getType() != unitType) && (!unit.isLifted())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
//	public boolean turretInvisibleUnitCheck(Position centerPosition) {
//	
//		Race enemyRace = Prebot.Broodwar.enemy().getRace();
//		
//		int radiusP = 0;
//		
//		if(enemyRace == Race.Zerg) {
//			radiusP = UnitType.Zerg_Lurker.groundWeapon().maxRange();
//		}else {
//			radiusP = 80;
//		}
//		
//		List<Unit> nearInvisibleUnit = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, centerPosition, radiusP);
//		for(Unit unit : nearInvisibleUnit) {
//			if(unit.getType() == UnitType.Protoss_Dark_Templar || unit.getType() == UnitType.Zerg_Lurker) {
//				FileUtils.appendTextToFile("log.txt", "\n Builder MissileTurret :: there is invisible unit in turret radius :: " + radiusP);
//				if(UnitUtils.availableScanningCount() == 0) {
//					FileUtils.appendTextToFile("log.txt", "\n Builder MissileTurret :: But can't use comsat :: don't construct turret");
//					return false;
//				}
//				
//			}
//		}
//		
//		return true;
//	}
}