

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

/// 嫄댁꽕�쐞移� �깘�깋�쓣 �쐞�븳 class
public class ConstructionPlaceFinder {

	/// 嫄댁꽕�쐞移� �깘�깋 諛⑸쾿
	public enum ConstructionPlaceSearchMethod { 
		SpiralMethod,	///< �굹�꽑�삎�쑝濡� �룎�븘媛�硫� �깘�깋
		SupplyDepotMethod, /// < �꽌�뵆�씪�씠 �뵒�뙚 硫붿룜�뱶. 媛�濡� �꽭濡쒕�� �꽌�뵆�씪�씠 �겕湲곕쭔�겮 �뜑�빐�꽌 李얘린
		ExistAddonPosition, /// < �븷�뱶�삩留� �궓�� �뙥�넗由� �뒪���룷�듃�쓽 寃쎌슦
		NewMethod 		///< �삁鍮�
	};
	
//	20180815. hkk. for lastBuilding Location Debug
	private ArrayList<TilePosition> lastBuilding = new ArrayList<TilePosition>(); 
	private ArrayList<TilePosition> lastBuildingFinal = new ArrayList<TilePosition>(); 
	
//	public int maxSupplyCntX = 3;
//	public int maxSupplyCntY = 4;
	
	/// 嫄대Ъ 嫄댁꽕 �삁�젙 ���씪�쓣 ���옣�빐�넃湲� �쐞�븳 2李⑥썝 諛곗뿴<br>
	/// TilePosition �떒�쐞�씠湲� �븣臾몄뿉 蹂댄넻 128*128 �궗�씠利덇� �맂�떎<br>
	/// 李멸퀬濡�, 嫄대Ъ�씠 �씠誘� 吏��뼱吏� ���씪�� ���옣�븯吏� �븡�뒗�떎
	private boolean[][] reserveMap = new boolean[128][128];
	
	/// BaseLocation 怨� Mineral / Geyser �궗�씠�쓽 ���씪�뱾�쓣 �떞�뒗 �옄猷뚭뎄議�. �뿬湲곗뿉�뒗 Addon �씠�쇅�뿉�뒗 嫄대Ъ�쓣 吏볦� �븡�룄濡� �빀�땲�떎	
//	private Set<TilePosition> tilesToAvoid = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoid = new boolean[128][128];
//	private Set<TilePosition> tilesToAvoidAbsolute = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidAbsolute = new boolean[128][128];

	//�꽌�뵆�씪�씠 吏볥뒗 吏��뿭
//	private Set<TilePosition> tilesToAvoidSupply = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidSupply = new boolean[128][128];
	//�뙥�넗由� 嫄댁꽕 吏��뿭
//	private Set<TilePosition> tilesToAvoidAddonBuilding = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidAddonBuilding = new boolean[128][128];

	private boolean[][] tilesToBaseLocationAvoid = new boolean[128][128];
//	private Set<TilePosition> tilesToAvoidEntranceTurret = new HashSet<TilePosition>();
	private boolean[][] tilesToAvoidEntranceTurret = new boolean[128][128];
	
	
	private static ConstructionPlaceFinder instance = new ConstructionPlaceFinder();
	
	private static boolean isInitialized = false;
	
	/// static singleton 媛앹껜瑜� 由ы꽩�빀�땲�떎
	public static ConstructionPlaceFinder Instance() {
		if (!isInitialized) {
			instance.setTilesToAvoid();
			instance.setTilesToAvoidForFirstGas();
			isInitialized = true;
		}
		return instance;
	}

	/// seedPosition 諛� seedPositionStrategy �뙆�씪硫뷀꽣瑜� �솢�슜�빐�꽌 嫄대Ъ 嫄댁꽕 媛��뒫 �쐞移섎�� �깘�깋�빐�꽌 由ы꽩�빀�땲�떎<br>
	/// seedPosition 二쇱쐞�뿉�꽌 媛��뒫�븳 怨녹쓣 �꽑�젙�븯嫄곕굹, seedPositionStrategy �뿉 �뵲�씪 吏��삎 遺꾩꽍寃곌낵 �빐�떦 吏��젏 二쇱쐞�뿉�꽌 媛��뒫�븳 怨녹쓣 �꽑�젙�빀�땲�떎<br>
	/// seedPosition, seedPositionStrategy �쓣 �엯�젰�븯吏� �븡�쑝硫�, MainBaseLocation 二쇱쐞�뿉�꽌 媛��뒫�븳 怨녹쓣 由ы꽩�빀�땲�떎
	public final TilePosition getBuildLocationWithSeedPositionAndStrategy(UnitType buildingType, TilePosition seedPosition, BuildOrderItem.SeedPositionStrategy seedPositionStrategy)
	{
		// seedPosition �쓣 �엯�젰�븳 寃쎌슦 洹� 洹쇱쿂�뿉�꽌 李얜뒗�떎
		if (TilePositionUtils.isValidTilePosition(seedPosition)) {
//			//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy before PlaceFinder seedPosition true ==>> " + buildingType + " :: " + seedPosition);
			TilePosition desiredPosition = getBuildLocationNear(buildingType, seedPosition, true, true);
			return desiredPosition;
		}
		
		// seedPosition �쓣 �엯�젰�븯吏� �븡�� 寃쎌슦
		TilePosition desiredPosition = TilePosition.None;
		
//		//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy SeedPositionStrategy ==>> " + buildingType + " :: " + seedPositionStrategy);
		
		switch (seedPositionStrategy) {

            case MainBaseLocation:
//            	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myBase().getTilePosition(), true);
//                //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                if (desiredPosition == null) {
                    BuildManager.Instance().mainBaseLocationFull = true;
                }
                break;

            case FirstExpansionLocation:
                if (InfoUtils.myFirstExpansion() != null) {
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myFirstExpansion().getTilePosition());
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    if (desiredPosition == null) {
                        BuildManager.Instance().firstExpansionLocationFull = true;
                    }
                }
                break;

            case FirstChokePoint:
                if (InfoUtils.myFirstChoke() != null) {
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myFirstChoke().getCenter().toTilePosition());
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    if (desiredPosition == null) {
                        BuildManager.Instance().firstChokePointFull = true;
                    }
                }
                break;

            case SecondChokePoint:
                if (InfoUtils.mySecondChoke() != null) {
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.mySecondChoke().getCenter().toTilePosition());
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                    if (desiredPosition == null) {
                        BuildManager.Instance().secondChokePointFull = true;
                    }
                }
                break;

            case NextExpansionPoint: // TODO NextSupplePoint �쟾�뿉 以묎컙�룷�씤�듃濡� 遊먯빞�븯�굹?
//            	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy NextExpansionPoint start");
//            	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                BaseLocation nextExpansionLocation = InformationManager.Instance().getNextExpansionLocation();
//                //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                if (nextExpansionLocation != null) {
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy seedPosition true ==>> " + buildingType + " :: " + nextExpansionLocation.getTilePosition());
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation != null");
                    desiredPosition = getBuildLocationNear(buildingType, nextExpansionLocation.getTilePosition());
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation != null");
                } else {
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation == null");
                    desiredPosition = getBuildLocationNear(buildingType, InfoUtils.myBase().getTilePosition());
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: nextExpansionLocation == null");
                }
//                //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy desiredPosition ==>> " + buildingType + " :: " + desiredPosition);
                break;

            case NextSupplePoint:
            	if(buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory){
                    if (BuildManager.Instance().fisrtSupplePointFull != true) {
                        TilePosition supplyPosition = BlockingEntrance.Instance().getSupplyPosition();
//                        //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                        desiredPosition = getBuildLocationNear(buildingType, supplyPosition);
//                        //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);

                        if (desiredPosition == null) {
                            BuildManager.Instance().fisrtSupplePointFull = true;
                        }
                        break;
                    }
                }
                break;

            case SecondMainBaseLocation:
            	
            	if((buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory) && BuildManager.Instance().fisrtSupplePointFull) {
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: supple");
                    desiredPosition = getBuildLocationNear(buildingType, BlockingEntrance.Instance().getSupplyPosition(InformationManager.Instance().secondStartPosition.getTilePosition()));
//                    //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: supple");
//                    System.out.println(" getSupplyPosition ==>>>> " + desiredPosition);
                }else {
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: else");
                	desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getSecondStartPosition().getTilePosition(), true);
//                	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy + " :: else");
                }
//                System.out.println(" SecondMainBaseLocation ==>>>> " + desiredPosition);
                if (desiredPosition == null) {
                    BuildManager.Instance().secondStartLocationFull = true;
                }
                break;

            case LastBuilingPoint:
//            	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                BaseLocation temp = InformationManager.Instance().getLastBuildingLocation();
//                //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder after :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
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

            case getLastBuilingFinalLocation: // �씠�냸�씠 留덉�留됱씠�땲源�.... NULL �씪�닔媛� �뾾�떎.
//            	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
                TilePosition lastBuilingFinalLocation = InformationManager.Instance().getLastBuilingFinalLocation();
                desiredPosition = getBuildLocationNear(buildingType, lastBuilingFinalLocation);
//                //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy placeFinder before :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + seedPositionStrategy);
//                //FileUtils.appendTextToFile("log.txt", "\n getLastBuilingFinalLocation ==>> "+ buildingType + " :: " + lastBuilingFinalLocation + " :: " + desiredPosition);
                break;

            default:
                break;
        }

		return desiredPosition;
	}

	/// desiredPosition 洹쇱쿂�뿉�꽌 嫄대Ъ 嫄댁꽕 媛��뒫 �쐞移섎�� �깘�깋�빐�꽌 由ы꽩�빀�땲�떎<br>
	/// desiredPosition 二쇱쐞�뿉�꽌 媛��뒫�븳 怨녹쓣 李얠븘 諛섑솚�빀�땲�떎<br>
	/// desiredPosition �씠 valid �븳 怨녹씠 �븘�땲�씪硫�, desiredPosition 瑜� MainBaseLocation 濡� �빐�꽌 二쇱쐞瑜� 李얜뒗�떎<br>
	/// Returns a suitable TilePosition to build a given building type near specified TilePosition aroundTile.<br>
	/// Returns BWAPI::TilePositions::None, if suitable TilePosition is not exists (�떎瑜� �쑀�떅�뱾�씠 �옄由ъ뿉 �엳�뼱�꽌, Pylon, Creep, 嫄대Ъ吏��쓣 ���씪 怨듦컙�씠 �쟾�� �뾾�뒗 寃쎌슦 �벑)
	
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

		// TODO 怨쇱젣 : 嫄댁꽕 �쐞移� �깘�깋 諛⑸쾿�� ConstructionPlaceSearchMethod::SpiralMethod 濡� �븯�뒗�뜲, �뜑 醫뗭� 諛⑸쾿�� �깮媛곹빐蹂� 怨쇱젣�씠�떎
		int constructionPlaceSearchMethod = 0;

		if((buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory)
				&& methodFix == false){
//		if(buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory){
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal();
		} else {
//			//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear spiralMethod set ==>> "+ buildingType + " :: " + desiredPosition);
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SpiralMethod.ordinal();
		}
		
		int buildingGapSpace = getBuildingSpaceGap(buildingType, desiredPosition, methodFix, spaceZero, constructionPlaceSearchMethod);
//		//FileUtils.appendTextToFile(buildingType, "log.txt", "\n getBuildLocationNear :: getBuildingSpaceGap "+ buildingType + " :: " + desiredPosition);
		TilePosition buildPosition = TilePosition.None;
		
		if (buildingType == UnitType.Terran_Missile_Turret) {
			while (buildingGapSpace >= 0) {
//				//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear4 turret while ==>> "+ buildingType + " :: " + desiredPosition + " :: buildingGapSpace => " +buildingGapSpace);
				buildPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);
				
				if (TilePositionUtils.isValidTilePosition(buildPosition)) {
					return buildPosition;
				}
						
				// 李얠쓣 �닔 �뾾�떎硫�, buildingGapSpace 媛믪쓣 以꾩뿬�꽌 �떎�떆 �깘�깋�븳�떎
				// buildingGapSpace 媛믪씠 1�씠硫� 吏��긽�쑀�떅�씠 紐살��굹媛��뒗 寃쎌슦媛� 留롮븘  �젣�쇅�븯�룄濡� �븳�떎 
				// 4 -> 3 -> 2 -> 0 -> �깘�깋 醫낅즺
				//      3 -> 2 -> 0 -> �깘�깋 醫낅즺 
				//           1 -> 0 -> �깘�깋 醫낅즺
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
		
//		//FileUtils.appendTextToFile(buildingType, "log.txt", "\n getBuildLocationNear :: final buildPosition :: " + buildPosition);

//		System.out.println("getBuildLocationNear :: " + buildingType + " :: " + desiredPosition);
		if (TilePositionUtils.isValidTilePosition(buildPosition)) {
			return buildPosition;
		} else {
			return TilePosition.None;
		}
	}

	private int getBuildingSpaceGap(UnitType buildingType, TilePosition desiredPosition, Boolean methodFix, Boolean spaceZero, int constructionPlaceSearchMethod) {
		// �씪諛섏쟻�씤 嫄대Ъ�뿉 ���빐�꽌�뒗 嫄대Ъ �겕湲곕낫�떎 Config::Macro::BuildingSpacing 移� 留뚰겮 �긽�븯醫뚯슦濡� �뜑 �꼻寃� �뿬�쑀怨듦컙�쓣 �몢�뼱�꽌 鍮� �옄由щ�� 寃��깋�븳�떎
		int buildingGapSpace = BuildConfig.buildingSpacing;
		
//		//FileUtils.appendTextToFile("log.txt","\n getBuildingSpaceGap :: " + buildingType + " :: " + desiredPosition + " :: " + buildingGapSpace);

		// ResourceDepot (Nexus, Command Center, Hatchery), Protoss_Pylon, Terran_Supply_Depot, 
		// Protoss_Photon_Cannon, Terran_Bunker, Terran_Missile_Turret, Zerg_Creep_Colony �뒗 �떎瑜� 嫄대Ъ 諛붾줈 �쁿�뿉 遺숈뿬 吏볥뒗 寃쎌슦媛� 留롮쑝誘�濡� buildingGapSpace�쓣 �떎瑜� Config 媛믪쑝濡� �꽕�젙�븯�룄濡� �븳�떎
		buildingGapSpace = 0 ;
//		if (buildingType.isResourceDepot()) {
//			buildingGapSpace = BuildConfig.buildingResourceDepotSpacing;
//			
//		} else
		if(buildingType == UnitType.Terran_Supply_Depot || buildingType == UnitType.Terran_Academy || buildingType == UnitType.Terran_Armory){
			buildingGapSpace = BuildConfig.BUILDING_SUPPLY_DEPOT_SPACING;
			if (spaceZero) {
				buildingGapSpace = 0;
			} else if (methodFix) {
				buildingGapSpace = 1;
//			} else if ((desiredPosition.getX() == BlockingEntrance.Instance().first_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().first_supple.getY())
//					|| (desiredPosition.getX() == BlockingEntrance.Instance().second_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().second_supple.getY())) {
			} else if (TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().first_supple)
					|| TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().second_supple)) {			
				// 20180719. hkk. ��洹몄쟾�뿉 ��鍮꾪빐�꽌 泥レ꽌�뵆怨� �몢踰덉㎏ �꽌�뵆�� 嫄대Ъ�뱾�쓣 遺숈뿬吏��뼱�빞 �븿
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
////					//FileUtils.appendTextToFile("log.txt","\n entrance_turret1 : buildingGapSpace : " + buildingGapSpace);
//				}
//			}
//			if(BlockingEntrance.Instance().entrance_turret2 != TilePosition.None) {
//				if(TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().entrance_turret2)) {
//					buildingGapSpace = 0;
////					//FileUtils.appendTextToFile("log.txt","\n entrance_turret2 : buildingGapSpace : " + buildingGapSpace);
//				}
//			}
			
		} else if (buildingType == UnitType.Terran_Bunker) {
			buildingGapSpace = 0;
		}
			
//			�봽由щ큸 1議곌굔 �뀒�뒪�듃 異붽�
//		} else if(buildingType == UnitType.Terran_Barracks){
//			buildingGapSpace = 0;
//		} else if(buildingType == UnitType.Terran_Factory){
//			buildingGapSpace = 0;
//		} else if(buildingType == UnitType.Terran_Starport){
//			buildingGapSpace = 0;
//		} else if(buildingType == UnitType.Terran_Science_Facility){
//			buildingGapSpace = 0;
//		}
		
		
		
//		20180728. hkk. �씠�땲�뀥 鍮뚮뱶 吏��젙嫄대Ъ�뱾�� �뿬諛� 0
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
		
		
//		20180821. hkk. �쟻�슜 �뀒�뒪�듃以�
		
		
		if( !TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().factory)
				&& buildingType == UnitType.Terran_Factory)
			{
				buildingGapSpace = 1;
			}
		
//		20180728. hkk. �씠�땲�뀥 鍮뚮뱶 吏��젙嫄대Ъ�뱾�� �뿬諛� 0
		if( (!TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().starport1)
			&& !TilePositionUtils.equals(desiredPosition, BlockingEntrance.Instance().starport2))
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
//			//FileUtils.appendTextToFile("log.txt","\n getBuildingSpaceGap initial set " + buildingType + " :: " + desiredPosition + " :: " + buildingGapSpace);
//		}
		
//		//FileUtils.appendTextToFile("log.txt","\n getBuildingSpaceGap return space :: " + buildingType + " :: " + desiredPosition + " :: " + buildingGapSpace);
		return buildingGapSpace;
	}

	/// �빐�떦 buildingType �씠 嫄댁꽕�맆 �닔 �엳�뒗 �쐞移섎�� desiredPosition 洹쇱쿂�뿉�꽌 �깘�깋�빐�꽌 �깘�깋寃곌낵瑜� 由ы꽩�빀�땲�떎<br>
	/// buildingGapSpace瑜� 諛섏쁺�빐�꽌 canBuildHereWithSpace 瑜� �궗�슜�빐�꽌 泥댄겕<br>
	/// 紐살갼�뒗�떎硫� BWAPI::TilePositions::None �쓣 由ы꽩�빀�땲�떎<br>
	/// TODO 怨쇱젣 : 嫄대Ъ�쓣 怨꾪쉷�뾾�씠 吏��쓣�닔 �엳�뒗 怨녹뿉 吏볥뒗 寃껋쓣 怨꾩냽 �븯�떎蹂대㈃, �쑀�떅�씠 嫄대Ъ �궗�씠�뿉 媛뉙엳�뒗 寃쎌슦媛� 諛쒖깮�븷 �닔 �엳�뒗�뜲, �씠瑜� 諛⑹��븯�뒗 諛⑸쾿�� �깮媛곹빐蹂� 怨쇱젣�엯�땲�떎
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, int buildingGapSpace, int constructionPlaceSearchMethod) {
		//returns a valid build location near the desired tile position (x,y).
		TilePosition resultPosition = TilePosition.None;
		ConstructionTask b = new ConstructionTask(buildingType, desiredPosition);

//		//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear PlaceFinder Start :: " + System.currentTimeMillis() + " :: " + buildingType + " :: " + desiredPosition);
		// maxRange 瑜� �꽕�젙�븯吏� �븡嫄곕굹, maxRange 瑜� 128�쑝濡� �꽕�젙�븯硫� 吏��룄 �쟾泥대�� �떎 �깘�깋�븯�뒗�뜲, 留ㅼ슦 �뒓�젮吏덈퓧留� �븘�땲�씪, ��遺�遺꾩쓽 寃쎌슦 遺덊븘�슂�븳 �깘�깋�씠 �맂�떎
		// maxRange �뒗 16 ~ 64媛� �쟻�떦�븯�떎
		// 媛믪쓣 李얠븘�궡�씪. = BaseLocation.isStartingLocation �쓣 泥댄겕�빐�꽌 硫붿씤�씠硫� ��媛�. �굹癒몄쭊 �떎瑜멸컪
		int maxRange = 0; // maxRange = BWAPI::Broodwar->mapWidth()/4;
			
		if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SpiralMethod.ordinal()) {

			// desiredPosition �쑝濡쒕��꽣 �떆�옉�빐�꽌 spiral �븯寃� �깘�깋�븯�뒗 諛⑸쾿
			// 泥섏쓬�뿉�뒗 �븘�옒 諛⑺뼢 (0,1) -> �삤瑜몄そ�쑝濡�(1,0) -> �쐞濡�(0,-1) -> �쇊履쎌쑝濡�(-1,0) -> �븘�옒濡�(0,1) -> ..
			
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
			
//			//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear maxrange ==>> "+ buildingType + " :: " + desiredPosition + " ::" + maxRange + " :: buildingGapSpace => " +buildingGapSpace);
	            
            int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			int spiralMaxLength = 1;
			int numSteps = 0;
			boolean isFirstStep = true;

			int spiralDirectionX = 0;
			int spiralDirectionY = 1;
			while (spiralMaxLength < maxRange) {
				if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {
//					//FileUtils.appendTextToFile(b.getType(),"log.txt", "\n canBuildHereWithSpace before PlaceFinder seedPosition true :: " + buildingType + " :: " + desiredPosition + " :: " + new TilePosition(currentX, currentY) + " :: " + buildingGapSpace);
					boolean isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, buildingGapSpace);
//					//FileUtils.appendTextToFile(b.getType(),"log.txt", "\n canBuildHereWithSpace after PlaceFinder seedPosition true  :: " + buildingType + " :: " + desiredPosition + " :: " + new TilePosition(currentX, currentY) + " :: " + buildingGapSpace + " :: " + isPossiblePlace);
					if (isPossiblePlace) {
                        if (b.getType() == UnitType.Terran_Factory && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 2) {
						
//                        	System.out.println("finding place for fac: " + BlockingEntrance.Instance().loc);
                            int currentXPlus = currentX;
                            int adjust =0;

                            while (true) {
                                if (BlockingEntrance.Instance().loc == Location.Eleven || BlockingEntrance.Instance().loc == Location.Seven ) {
                                    currentXPlus--;
                                    adjust = 1;
//                                    System.out.println("finding place for fac minus");
                                } else {
                                    currentXPlus++;
                                    adjust = -1;
//                                    System.out.println("finding place for fac plus");
                                }
//                                System.out.println(" final location of factory :: " + currentXPlus + " / " + currentY);
                                if (currentXPlus < 0 || currentXPlus + 3 > MyBotModule.Broodwar.mapWidth()) {
                                    break;
                                }
                                
                                boolean isPossiblePlaceAjust = canBuildHereWithSpace(new TilePosition(currentXPlus, currentY), b, buildingGapSpace);
//                                //FileUtils.appendTextToFile(b.getType(),"log.txt", "\n canBuildHereWithSpace :: isPossiblePlaceAjust :: "  + resultPosition);
                                if (!isPossiblePlaceAjust) {
//                                	System.out.println("finding place for fac stop move");
                                    break;
                                }
//                                System.out.println(" real     final location of factory :: " + currentXPlus + " / " + currentY);
                            }

                            resultPosition = new TilePosition(currentXPlus+adjust, currentY);
//                            //FileUtils.appendTextToFile(b.getType(),"log.txt", "\n canBuildHereWithSpace :: resultPosition :: "  + resultPosition);
//                            System.out.println(" resultPosition of factory :: " + resultPosition);
                            break;
                        }else {
//                        	//FileUtils.appendTextToFile(b.getType(),"log.txt", "\n canBuildHereWithSpace :: resultPosition :: "  + resultPosition);
                            resultPosition = new TilePosition(currentX, currentY);
                            break;
                        }
					}
				}

				currentX = currentX + spiralDirectionX;
				currentY = currentY + spiralDirectionY;
				numSteps++;
				
				// �떎瑜� 諛⑺뼢�쑝濡� �쟾�솚�븳�떎
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
			//�꽌�뵆�씪�씠 �뵒�뙚 �슜 濡쒖쭅(4X4)
			// y異뺣��꽣 �떆�옉. 理쒖큹 �룷吏��뀡�뿉�꽌 X異� + 3 �꽆�뼱媛�硫� Y異� +2
			int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			int depostSizeX = 3;
			int depostSizeY = 2;
			boolean isPossiblePlace = false;
			
			if(BlockingEntrance.Instance().xinc) {
				if(BlockingEntrance.Instance().yinc) {
//					1�떆
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
//						//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 1�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
					
				}else {
//					5�떆
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
//							//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 5�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
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
//						//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 5�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
					
				}
				
			}else {
				if(BlockingEntrance.Instance().yinc) {
//					11�떆
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
//							//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 11�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
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
//						//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 11�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
				}else {
//					7�떆
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
//							//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 7�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
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
//						//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear 7�떆:: supply tile :: currentX : " + currentX + " / currentY : " + currentY);
					}
				}
			}
			
		} else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		} else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		}

//		//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear PlaceFinder End :: " + System.currentTimeMillis()+ " :: " + buildingType + " :: " + desiredPosition);
		return resultPosition;
	}

	/// �빐�떦 �쐞移섏뿉 嫄대Ъ 嫄댁꽕�씠 媛��뒫�븳吏� �뿬遺�瑜� buildingGapSpace 議곌굔�쓣 �룷�븿�빐�꽌 �뙋�떒�븯�뿬 由ы꽩�빀�땲�떎<br>
	/// Broodwar �쓽 canBuildHere, isBuildableTile, isReservedTile 瑜� 泥댄겕�빀�땲�떎
	public final boolean canBuildHereWithSpace(TilePosition position, final ConstructionTask b, int buildingGapSpace)
	{
		//if we can't build here, we of course can't build here with space
//		//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace PlaceFinder start :: " + System.currentTimeMillis() + " :: " + b.getType() + " :: " + position);
//		//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace start :: " + position);
		if (!canBuildHere(position, b)) {
//			//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace :: canBuildHere false :: "+ b.getType() + " // " + position  +" // buildingGapSpace :: " + buildingGapSpace);
			return false;
		}

		// height and width of the building
		int width = b.getType().tileWidth();
		int height = b.getType().tileHeight();

		// define the rectangle of the building spot
		// 嫄대Ъ �겕湲곕낫�떎 �긽�븯醫뚯슦濡� �뜑 �겙 �궗媛곹삎
		int startx;
		int starty;
		int endx;
		int endy;

		//buildingGapSpace = 0;@@@@@@
		
		boolean horizontalOnly = false;

		if (b.getType().isAddon()) { // Addon ���엯�쓽 嫄대Ъ�씪 寃쎌슦�뿉�뒗, 洹� Addon 嫄대Ъ �쇊履쎌뿉 whatBuilds 嫄대Ъ�씠 �엳�뒗吏�瑜� 泥댄겕�븳�떎
			final UnitType builderType = b.getType().whatBuilds().first;
			TilePosition builderTile = new TilePosition(position.getX() - builderType.tileWidth(), position.getY() + 2 - builderType.tileHeight());
			

			startx = builderTile.getX() - buildingGapSpace;
			starty = builderTile.getY() - buildingGapSpace;
			endx = position.getX() + width + buildingGapSpace;
			endy = position.getY() + height + buildingGapSpace;

			// builderTile�뿉 Lifted 嫄대Ъ�씠 �븘�땲怨� whatBuilds 嫄대Ъ�씠 �븘�땶 嫄대Ъ�씠 �엳�뒗吏� 泥댄겕
			
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
			if (b.getType() == UnitType.Terran_Factory
//				b.getType() == UnitType.Terran_Starport 
				) {
				width += 2;
//				width += 2;
//				buildingGapSpace = 0;
//				horizontalOnly = true;
			}
			else if(b.getType() == UnitType.Terran_Starport) {
				width += 2;

			}
			
			
			if( (TilePositionUtils.equals(position, BlockingEntrance.Instance().first_supple)
					||TilePositionUtils.equals(position, BlockingEntrance.Instance().second_supple)
					||TilePositionUtils.equals(position, BlockingEntrance.Instance().starport1)
					||TilePositionUtils.equals(position, BlockingEntrance.Instance().starport2)
					||TilePositionUtils.equals(position, BlockingEntrance.Instance().factory)
					||TilePositionUtils.equals(position, BlockingEntrance.Instance().barrack))
					&& InitialBuildProvider.Instance().getAdaptStrategyStatus() != InitialBuildProvider.AdaptStrategyStatus.COMPLETE
				){
					buildingGapSpace = 0; 
				}
	
//			if( (position.getX() == BlockingEntrance.Instance().starport1.getX() && position.getY() == BlockingEntrance.Instance().starport1.getY() && b.getType() == UnitType.Terran_Starport)
//				|| (position.getX() == BlockingEntrance.Instance().starport2.getX() && position.getY() == BlockingEntrance.Instance().starport2.getY() && b.getType() == UnitType.Terran_Starport)
//				|| (position.getX() == BlockingEntrance.Instance().factory.getX() && position.getY() == BlockingEntrance.Instance().factory.getY() && b.getType() == UnitType.Terran_Factory)
//				|| (position.getX() == BlockingEntrance.Instance().barrack.getX() && position.getY() == BlockingEntrance.Instance().barrack.getY() && b.getType() == UnitType.Terran_Barracks)){
//				
////				//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace chk initial point:: buildingType "+ b.getType() + " // position :: " + position  +" // buildingGapSpace :: " + buildingGapSpace);
//				width = 0;
//				height = 0;
//				buildingGapSpace = 0;
//			}


			// �긽�븯醫뚯슦�뿉 buildingGapSpace 留뚰겮 媛꾧꺽�쓣 �쓣�슫�떎
			if (horizontalOnly == false) {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY() - buildingGapSpace;
//				endx = startx + width + buildingGapSpace;
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height + buildingGapSpace;
			}
			// 醫뚯슦濡쒕쭔 buildingGapSpace 留뚰겮 媛꾧꺽�쓣 �쓣�슫�떎
			else {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY();
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height;
			}
			
			


//			//FileUtils.appendTextToFile(b.getType(), "log.txt", "\n canBuildHereWithSpace for loop :: "+ b.getType() + " :: " + "["+startx+","+starty+"] :: ["+endx+","+endy+"]"  +" :: buildingGapSpace :: " + buildingGapSpace);

			// 嫄대Ъ�씠 李⑥��븷 怨듦컙 肉� �븘�땲�씪 二쇱쐞�쓽 buildingGapSpace 怨듦컙源뚯� �떎 鍮꾩뼱�엳�뒗吏�, 嫄댁꽕媛��뒫�븳 ���씪�씤吏�, �삁�빟�릺�뼱�엳�뒗寃껋� �븘�땶吏�, TilesToAvoid �뿉 �빐�떦�븯吏� �븡�뒗吏� 泥댄겕
			for (int x = startx < 0 ? 0 : startx; x >= 0 && x < endx; x++)
			{
				for (int y = starty < 0 ? 0 : starty; y >= 0 && y < endy; y++)
				{
					
//					//FileUtils.appendTextToFile(b.getType(), "log.txt", "\n canBuildHereWithSpace loop :: "+ b.getType() + " :: " + "["+x+","+y+"] :: ["+endx+","+endy+"]"  +" :: buildingGapSpace :: " + buildingGapSpace);
					
//					if(canAddonBuilding == true && 
//						((x == position.getX() + 4 && y == position.getY())
//						||(x == position.getX() + 5 && y == position.getY()))) {
////						//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace :: hereis up of addon :: no tile check :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
//						continue;
//					}
					
					if( (TilePositionUtils.equals(position, BlockingEntrance.Instance().first_supple)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().second_supple)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().starport1)
						||TilePositionUtils.equals(position, BlockingEntrance.Instance().starport2)
//						||TilePositionUtils.equals(position, BlockingEntrance.Instance().factory)
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
//								//FileUtils.appendTextToFile(b.getType(), "log.txt", "\n canBuildHereWithSpace :: isBuildableTile false :: "+ b.getType() + " :: " + "["+x+","+y+"]"  +" :: buildingGapSpace :: " + buildingGapSpace);
//									//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isBuildableTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
								return false;
							}
						}else {
							if (isBuildableTileFac(b, x, y) == false) {
//								//FileUtils.appendTextToFile(b.getType(), "log.txt", "\n canBuildHereWithSpace :: isBuildableTileFac false :: "+ b.getType() + " :: " + "["+x+","+y+"]"  +" :: buildingGapSpace :: " + buildingGapSpace);
	//								//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isBuildableTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
								return false;
							}
						}
					}else {
					// if we can't build here, or space is reserved, we can't build here
						if (isBuildableTile(b, x, y) == false) {
//							//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isBuildableTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
					}

//					if (isReservedTile(x, y)) {
//						//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isReservedTile false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
//						return false;
//					}
					
//					if(b.getType() != UnitType.Terran_Command_Center){
//						if(isTilesToAvoidBase(x, y)) {
//							return false;
//						}
//					}
					
////					20180815. hkk. 而ㅻ㎤�뱶 �꽱�꽣 �씪寃쎌슦. new 而ㅻ㎤�뱶 �꽱�꽣 蹂몄쭊�씠, old 而ㅻ㎤�뱶 �꽱�꽣 而댁뀑 �쐞移섎�� 移⑤쾾�븯硫� �븞�맂�떎.
					if(b.getType() == UnitType.Terran_Command_Center) {
						if(isTilesToAvoidEntranceTurret(x, y)) {
							return false;
						}
					}

					// ResourceDepot / Addon 嫄대Ъ�씠 �븘�땶 �씪諛� 嫄대Ъ�쓽 寃쎌슦, BaseLocation 怨� Geyser �궗�씠 ���씪 (TilesToAvoid) �뿉�뒗 嫄대Ъ�쓣 吏볦� �븡�뒗�떎
//					20180719. hkk. ��洹몄쟾 ��鍮� 諛곕윮怨� �꽌�뵆 媛��뒪 二쇰��뿉 遺숈뿬吏볤린 �븘�슂
					if (b.getType().isResourceDepot() == false && b.getType().isAddon() == false
							&& b.getType() != UnitType.Terran_Bunker && b.getType() != UnitType.Terran_Missile_Turret
							&& b.getType() != UnitType.Terran_Barracks && b.getType() != UnitType.Terran_Supply_Depot
							&& b.getType() != UnitType.Terran_Factory && b.getType() != UnitType.Terran_Starport) {
						if (isTilesToAvoid(x, y)) {
//							//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isTilesToAvoid false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
					}
					
					if(b.getType() == UnitType.Terran_Missile_Turret || b.getType() == UnitType.Terran_Supply_Depot) {
//						tilesToAvoidAddonBuilding[x][y] = false;
						if (isTilesToAvoidAddonBuilding(x, y)) {
							return false;
						}
						
//						//FileUtils.appendTextToFile("log.txt", "\n setTilesToAvoidAddonBuilding :: " + unit.getType() + " :: ( " + x + " , " + y + ")");
					}
					
					if (b.getType() == UnitType.Terran_Factory ||
						b.getType() == UnitType.Terran_Starport ) {
						if(x > startx && x < endx -1 && y > starty && y < endy-1) {
//							System.out.println(" avoid tile check :: " + position + " :: (" + x + " , " + y + ")");
							if (isTilesToAvoid(x, y)) {
//								//FileUtils.appendTextToFile(b.getType(), "log.txt", "\n canBuildHereWithSpace :: isTilesToAvoid false :: "+ b.getType() + " :: " + "["+x+","+y+"]"  +" :: buildingGapSpace :: " + buildingGapSpace);
								return false;
							}
						}
					}
//					20180806. hkk. 1,2 踰덉�� �꽣�젢�� 吏��젙
//					}else if (b.getType() == UnitType.Terran_Missile_Turret) {
					
//					if (b.getType() == UnitType.Terran_Missile_Turret && InitialBuildProvider.Instance().getAdaptStrategyStatus() != InitialBuildProvider.AdaptStrategyStatus.COMPLETE) {
//							if (isTilesToAvoidAddonBuilding(x, y)) {
//								return false;
//							}
//					}else if(b.getType() != UnitType.Terran_Factory && b.getType() != UnitType.Terran_Starport){
//						if (isTilesToAvoidAddonBuilding(x, y)) {
////							//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace isTilesToAvoidFac false :: "+ b.getType() + " // " + "["+x+","+y+"]"  +" // buildingGapSpace :: " + buildingGapSpace);
//							return false;
//						}
//					}
					
					
					
					//�꽌�뵆�씪�씠 吏��뿭�� �꽌�뵆�씪�씠 / �븘移대뜲誘� / �븘癒몃━ �쇅�뿉�뒗 吏��쓣�닔 �뾾�떎.
					if (b.getType() != UnitType.Terran_Supply_Depot
						&& b.getType() != UnitType.Terran_Academy
						&& b.getType() != UnitType.Terran_Armory
//						&& b.getType() != UnitType.Terran_Barracks
						) {
						if (isTilesToAvoidSupply(x, y)) {
//							//FileUtils.appendTextToFile(b.getType(), "log.txt", "\n canBuildHereWithSpace :: isTilesToAvoidSupply false :: "+ b.getType() + " :: " + "["+x+","+y+"]"  +" :: buildingGapSpace :: " + buildingGapSpace);
							return false;
						}
					}
					
					
					if(b.getType() != UnitType.Terran_Command_Center) {
						if (isTilesToAvoidAbsolute(x, y)) {
							return false;
						}
					}
				}
			}
		}

		// if this rectangle doesn't fit on the map we can't build here
		if (b.getType() != UnitType.Terran_Factory && b.getType() != UnitType.Terran_Starport &&(startx < 0 || starty < 0 || endx > MyBotModule.Broodwar.mapWidth() || endx < position.getX() + width || endy > MyBotModule.Broodwar.mapHeight())) {
//			//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace return false :: "+ b.getType() + " // buildingGapSpace :: " + buildingGapSpace);
			return false;
		} else {
//			//FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace return true :: "+ b.getType() + " // buildingGapSpace :: " + buildingGapSpace);
			return true;
		}

	}

	/// �빐�떦 �쐞移섏뿉 嫄대Ъ 嫄댁꽕�씠 媛��뒫�븳吏� �뿬遺�瑜� 由ы꽩�빀�땲�떎 <br>
	/// Broodwar �쓽 canBuildHere 諛� _reserveMap �� isOverlapsWithBaseLocation �쓣 泥댄겕
	public final boolean canBuildHere(TilePosition position, final ConstructionTask b) {
		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType())) {
//			//FileUtils.appendTextToFile("log.txt", "\n canBuildHere ==> !Prebot.Broodwar.canBuildHere :: " + position);
			return false;
		}
		
		int addWidth = 0;
		int addHeight = 0;
		
		if(b.getType() == UnitType.Terran_Factory || b.getType() == UnitType.Terran_Starport) {
			addWidth = 2;
		}
		
		int rwidth = reserveMap.length;
		int rheight = tilesToAvoid[0].length;
//		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
//		{
//			return false;
//		}
		
		// check the reserve map
		for (int x = position.getX() ; x < position.getX() + b.getType().tileWidth() + addWidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + b.getType().tileHeight() + addHeight; y++)
			{
				
//				20180823. hkk. �븷�뱶�삩 吏��뿭�씠 留듬컰�쑝濡� �굹媛꾨떎硫� 洹멸납�� 嫄댁꽕 遺덇�
				if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
				{
					return false;
				}
				
				//if (reserveMap.get(x).get(y))
				if (reserveMap[x][y])
				{
//							//FileUtils.appendTextToFile("log.txt", "\n canBuildHere ==> can not build here reserveMap :: " + x + " , " + y);
					return false;
				}
			}
		}

		// if it overlaps a base location return false
		// ResourceDepot 嫄대Ъ�씠 �븘�땶 �떎瑜� 嫄대Ъ�� BaseLocation �쐞移섏뿉 吏볦� 紐삵븯�룄濡� �븳�떎
		if (isOverlapsWithBaseLocation(position, b.getType())) {
			return false;
		} else {
			return true;
		}
	}

	/// seedPosition 洹쇱쿂�뿉�꽌 Refinery 嫄대Ъ 嫄댁꽕 媛��뒫 �쐞移섎�� �깘�깋�빐�꽌 由ы꽩�빀�땲�떎 <br>
	/// 吏��룄�긽�쓽 �뿬�윭 媛��뒪 愿묒궛 (Resource_Vespene_Geyser) 以� �삁�빟�릺�뼱�엳吏� �븡�� 怨�(isReservedTile), �떎瑜� �꽟�씠 �븘�땶 怨�, �씠誘� Refinery 媛� 吏��뼱�졇�엳吏��븡�� 怨� 以�<br> 
	/// seedPosition 怨� 媛��옣 媛�源뚯슫 怨녹쓣 由ы꽩�빀�땲�떎
	public final TilePosition getRefineryPositionNear(TilePosition seedPosition) {
		
		if (!TilePositionUtils.isValidTilePosition(seedPosition)) {
			seedPosition = InfoUtils.myBase().getTilePosition();
		}
		
//		//FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear start :: " + seedPosition + " :: " + Prebot.Broodwar.getFrameCount());

		//TODO BASICBOT 1.1 踰꾩졏�쓽 媛��뒪 泥섎━�떎.. �솗�씤�빐 遊먯빞�븿.
//		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers())
//		{
//			// geyser->getPosition() �쓣 �븯硫�, Unknown �쑝濡� �굹�삱 �닔 �엳�떎.
//			// 諛섎뱶�떆 geyser->getInitialPosition() �쓣 �궗�슜�빐�빞 �븳�떎
//			Position geyserPos = geyser.getInitialPosition();
//			TilePosition geyserTilePos = geyser.getInitialTilePosition();
//
//			// �씠誘� �삁�빟�릺�뼱�엳�뒗媛�
//			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
//				continue;
//			}
//
//			// geyser->getType() �쓣 �븯硫�, Unknown �씠嫄곕굹, Resource_Vespene_Geyser �씠嫄곕굹, Terran_Refinery �� 媛숈씠 嫄대Ъ紐낆씠 �굹�삤怨�, 
//			// 嫄대Ъ�씠 �뙆愿대릺�뼱�룄 �옄�룞�쑝濡� Resource_Vespene_Geyser 濡� �룎�븘媛�吏� �븡�뒗�떎
//			// geyser �쐞移섏뿉 �엳�뒗 �쑀�떅�뱾�뿉 ���빐 isRefinery() 濡� 泥댄겕瑜� �빐遊먯빞 �븳�떎
//
//			// seedPosition �쑝濡쒕��꽣 16 TILE_SIZE 嫄곕━ �씠�궡�뿉 �엳�뒗媛�
//			// Fighting Spirit 留듭쿂�읆 seedPosition �쑝濡쒕��꽣 �룞�씪�븳 嫄곕━ �궡�뿉 geyser 媛� �뿬�윭媛� �엳�쓣 �닔 �엳�뒗 寃쎌슦 Refinery 嫄대Ъ�쓣 吏볤린 �쐞�빐�꽌�뒗 seedPosition �쓣 �젙�솗�븯寃� �엯�젰�빐�빞 �븳�떎
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
		
		// �쟾泥� geyser 以묒뿉�꽌 seedPosition �쑝濡쒕��꽣 16 TILE_SIZE 嫄곕━ �씠�궡�뿉 �엳�뒗 寃껋쓣 李얜뒗�떎
		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers()) {
			// geyser->getPosition() �쓣 �븯硫�, Unknown �쑝濡� �굹�삱 �닔 �엳�떎.
			// 諛섎뱶�떆 geyser->getInitialPosition() �쓣 �궗�슜�빐�빞 �븳�떎
//			//FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear getStaticGeysers :: " + geyser.getTilePosition());

			Position geyserPos = geyser.getInitialPosition();
			TilePosition geyserTilePos = geyser.getInitialTilePosition();

			// �씠誘� �삁�빟�릺�뼱�엳�뒗媛�
			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
//				//FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear geyserTilePos is reserved :: " + geyserTilePos);
				continue;
			}

			// if it is not connected fron seedPosition, it is located in another island
			if (!BWTA.isConnected(seedPosition, geyserTilePos)) {
//				//FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear geyserTilePos not connected :: " + geyserTilePos + " :: " + seedPosition);
				continue;
			}

			// �씠誘� 吏��뼱�졇 �엳�뒗媛�
			boolean refineryAlreadyBuilt = false;
			List<Unit> alreadyBuiltUnits = MyBotModule.Broodwar.getUnitsInRadius(geyserPos, 4 * BuildConfig.TILE_SIZE);
			for (Unit u : alreadyBuiltUnits) {
				if (u.getType().isRefinery() && u.exists()) {
//					//FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear is alreadyBuiltUnits");
					refineryAlreadyBuilt = true;
					break;
				}
			}

			if (!refineryAlreadyBuilt) {
				double thisDistance = PositionUtils.getGroundDistance(geyserPos, seedPosition.toPosition());
				if (thisDistance < minGeyserDistanceFromSeedPosition) {
					minGeyserDistanceFromSeedPosition = thisDistance;
					closestGeyser = geyser.getInitialTilePosition();
//					//FileUtils.appendTextToFile("log.txt", "\n getRefineryPositionNear set closestGeyser :: " + closestGeyser);
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

        // �쟾泥� geyser 以묒뿉�꽌 seedPosition �쑝濡쒕��꽣 16 TILE_SIZE 嫄곕━ �씠�궡�뿉 �엳�뒗 寃껋쓣 李얜뒗�떎
        for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers()) {
            // geyser->getPosition() �쓣 �븯硫�, Unknown �쑝濡� �굹�삱 �닔 �엳�떎.
            // 諛섎뱶�떆 geyser->getInitialPosition() �쓣 �궗�슜�빐�빞 �븳�떎

            Position geyserPos = geyser.getInitialPosition();
            TilePosition geyserTilePos = geyser.getInitialTilePosition();

            // �씠誘� �삁�빟�릺�뼱�엳�뒗媛�
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

	/// �빐�떦 �쐞移섍� BaseLocation 怨� 寃뱀튂�뒗吏� �뿬遺�瑜� 由ы꽩�빀�땲�떎<br>
	/// BaseLocation �뿉�뒗 ResourceDepot 嫄대Ъ留� 嫄댁꽕�븯怨�, �떎瑜� 嫄대Ъ�� 嫄댁꽕�븯吏� �븡湲� �쐞�븿�엯�땲�떎
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

	/// 嫄대Ъ 嫄댁꽕 媛��뒫 ���씪�씤吏� �뿬遺�瑜� 由ы꽩�빀�땲�떎
	public final boolean isBuildableTile(final ConstructionTask b, int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			return false;
		}

		// 留� �뜲�씠�꽣 肉먮쭔 �븘�땲�씪 鍮뚮뵫 �뜲�씠�꽣瑜� 紐⑤몢 怨좊젮�빐�꽌 isBuildable 泥댄겕
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (MyBotModule.Broodwar.isBuildable(x, y, true) == false)
		{
			return false;
		}

		// constructionWorker �씠�쇅�쓽 �떎瑜� �쑀�떅�씠 �엳�쑝硫� false瑜� 由ы꽩�븳�떎
		for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(x, y))
		{
			if ((b.getConstructionWorker() != null) && (unit != b.getConstructionWorker()))
			{
				if (unit.getType().isBuilding() && unit.isLifted()) {
					continue;
				}
				return false;
			}
			
			
		}

		return true;
	}
	
	
	/// 嫄대Ъ 嫄댁꽕 媛��뒫 ���씪�씤吏� �뿬遺�瑜� 由ы꽩�빀�땲�떎
		public final boolean isBuildableTileFac(final ConstructionTask b, int x, int y)
		{
			TilePosition tp = new TilePosition(x, y);
			if (!tp.isValid())
			{
				return false;
			}

			// 留� �뜲�씠�꽣 肉먮쭔 �븘�땲�씪 鍮뚮뵫 �뜲�씠�꽣瑜� 紐⑤몢 怨좊젮�빐�꽌 isBuildable 

			// constructionWorker �씠�쇅�쓽 �떎瑜� �쑀�떅�씠 �엳�쑝硫� false瑜� 由ы꽩�븳�떎
			for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(x, y))
			{
				if (unit.getType().isBuilding() && !unit.isLifted())
				{
//					System.out.println(" there is something for const :: " + b.getType() + " :: "+unit.getType()+"(" + x + " , " + y +")");
					return false;
				}
			}

			return true;
		}

	/// 嫄대Ъ 嫄댁꽕 �삁�젙 ���씪濡� �삁�빟�빐�꽌, �떎瑜� 嫄대Ъ�쓣 以묐났�빐�꽌 吏볦� �븡�룄濡� �빀�땲�떎
	public void reserveTiles(TilePosition position, int width, int height, UnitType unit)
	{
//		System.out.println(" Set reserveMap start:: " + unit + " :: " + position + " :: width :: " + width + " :: height :: " + height);
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
		int x = position.getX();
		int y = position.getY();
		int widthx = width;
		int heighty = height;
		if (
				unit == UnitType.Terran_Factory ||
				unit == UnitType.Terran_Starport)
//				b.getType() == UnitType.Terran_Science_Facility)
			{
				x = x>0?x-1:0;
				y = y>0?y-1:0;
				widthx = widthx+3;
				heighty = heighty + 1;
//				height += 1;
		}
		
//		int startx = x;
		int tox = position.getX() + widthx;
		int toy = position.getY() + heighty;
		
		
//		if()
//		System.out.println(" Set reserveMap :: "+ unit +" :: to X :: " + (position.getX() + widthx) + " :: to Y :: " + (position.getY() + heighty));
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
//		System.out.println(" Set reserveMap :: "+ unit +" :: to X :: " + tox + " :: to Y :: " + toy
//				+ " :: rwidth :: " + rwidth + " :: rheight :: " + rheight);
		for (int startx = x ; startx < tox && startx < rwidth; startx++)
		{
			for (int starty = y ; starty < toy && starty < rheight; starty++)
			{
//				System.out.println(" Set reserveMap :: " + unit + " :: (" + startx +" , "+ starty + ")");
				//reserveMap.get(x).set(y, true);
				reserveMap[startx][starty] = true;
				// C++ : reserveMap[x][y] = true;
			}
		}
	}
	
	/// 嫄대Ъ 嫄댁꽕 �삁�젙 ���씪濡� �삁�빟�뻽�뜕 寃껋쓣 �빐�젣�빀�땲�떎
	public void freeTiles(TilePosition position, int width, int height, UnitType unit)
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
		
		int x = position.getX();
		int y = position.getY();
		int widthx = width;
		int heighty = height;
		if (
				unit == UnitType.Terran_Factory ||
				unit == UnitType.Terran_Starport)
//				b.getType() == UnitType.Terran_Science_Facility)
			{
				x = x>0?x-1:0;
				y = y>0?y-1:0;
				widthx = widthx+3;
				heighty = heighty + 1;
//				height += 1;
		}
		
//		int startx = x;
		int tox = position.getX() + widthx;
		int toy = position.getY() + heighty;
		
		
//		if()
//		System.out.println(" Set reserveMap :: "+ unit +" :: to X :: " + (position.getX() + widthx) + " :: to Y :: " + (position.getY() + heighty));
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
//		System.out.println(" Set reserveMap :: "+ unit +" :: to X :: " + tox + " :: to Y :: " + toy
//				+ " :: rwidth :: " + rwidth + " :: rheight :: " + rheight);
		for (int startx = x ; startx < tox && startx < rwidth; startx++)
		{
			for (int starty = y ; starty < toy && starty < rheight; starty++)
			{
//				System.out.println(" Set reserveMap :: " + unit + " :: (" + startx +" , "+ starty + ")");
				//reserveMap.get(x).set(y, true);
				reserveMap[startx][starty] = false;
				// C++ : reserveMap[x][y] = true;
			}
		}
	}

	// 嫄대Ъ 嫄댁꽕 �삁�빟�릺�뼱�엳�뒗 ���씪�씤吏� 泥댄겕
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

	/// reserveMap�쓣 由ы꽩�빀�땲�떎
	public boolean[][] getReserveMap() {
		return reserveMap;
	}

	/// (x, y) 媛� BaseLocation 怨� Mineral / Geyser �궗�씠�쓽 ���씪�뿉 �빐�떦�븯�뒗吏� �뿬遺�瑜� 由ы꽩�빀�땲�떎
	public final boolean isTilesToAvoid(int x, int y)
	{
//		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
//			//FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
//			return true;
//		}
		int rwidth = tilesToAvoid.length;
		int rheight = tilesToAvoid[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return true;
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
//			//FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
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
			return true;
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
////			//FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
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
	
	public final boolean isTilesToAvoidEntranceTurret(int x, int y)
	{
//		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
//		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
//			//FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
//			return true;
//		}
		
//		for (TilePosition t : tilesToAvoidEntranceTurret) {
//			if (t.getX() == x && t.getY() == y) {
//				return true;
//			}
//		}
		
//
//		return false;
		int rwidth = tilesToAvoidEntranceTurret.length;
		int rheight = tilesToAvoidEntranceTurret[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return true;
		}
		return tilesToAvoidEntranceTurret[x][y];
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
			return true;
		}
		return tilesToAvoidAbsolute[x][y];
	}
	
	/// (x, y) 媛� �꽌�뵆�씪�씠 吏��뿭�씠�씪硫� 吏��쓣�닔 �뾾�떎.
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
			return true;
		}
		return tilesToAvoidSupply[x][y];
	}

	/// BaseLocation 怨� Mineral / Geyser �궗�씠�쓽 ���씪�뱾�쓣 李얠븘 _tilesToAvoid �뿉 ���옣�빀�땲�떎<br>
	/// BaseLocation 怨� Geyser �궗�씠, ResourceDepot 嫄대Ъ怨� Mineral �궗�씠 怨듦컙�쑝濡� 嫄대Ъ 嫄댁꽕 �옣�냼瑜� �젙�븯硫�<br> 
	/// �씪袁� �쑀�떅�뱾�씠 �옣�븷臾쇱씠 �릺�뼱�꽌 嫄댁꽕 �떆�옉�릺湲곌퉴吏� �떆媛꾩씠 �삤�옒嫄몃━怨�, 吏��뼱吏� 嫄대Ъ�씠 �옣�븷臾쇱씠 �릺�뼱�꽌 �옄�썝 梨꾩랬 �냽�룄�룄 �뒓�젮吏�湲� �븣臾몄뿉, �씠 怨듦컙�� 嫄대Ъ�쓣 吏볦� �븡�뒗 怨듦컙�쑝濡� �몢湲� �쐞�븿�엯�땲�떎
	public void setTilesToAvoid()
	{
		// ResourceDepot 嫄대Ъ�쓽 width = 4 ���씪, height = 3 ���씪
		// Geyser �쓽            width = 4 ���씪, height = 2 ���씪
		// Mineral �쓽           width = 2 ���씪, height = 1 ���씪

		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// Island �씪 寃쎌슦 嫄대Ъ 吏��쓣 怨듦컙�씠 �젅���쟻�쑝濡� 醫곴린 �븣臾몄뿉 嫄대Ъ �븞吏볥뒗 怨듦컙�쓣 �몢吏� �븡�뒗�떎
			if (base.isIsland()) continue;
			if (BWTA.isConnected(base.getTilePosition(), InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition()) == false) continue;

			// dimensions of the base location
			int bx0 = base.getTilePosition().getX();
			int by0 = base.getTilePosition().getY();
			int bx4 = base.getTilePosition().getX() + 4;
			int by3 = base.getTilePosition().getY() + 3;

			// BaseLocation 怨� Geyser �궗�씠�쓽 ���씪�쓣 BWTA::getShortestPath 瑜� �궗�슜�빐�꽌 援ы븳 �썑 _tilesToAvoid �뿉 異붽�
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

			// BaseLocation 怨� Mineral �궗�씠�쓽 ���씪�쓣 BWTA::getShortestPath 瑜� �궗�슜�빐�꽌 援ы븳 �썑 _tilesToAvoid �뿉 異붽�
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
					
					for (int x = fromx; x >= 0 && x < fromx + 6 && x < MyBotModule.Broodwar.mapWidth(); x++)
				        {
				            for (int y = fromy ; y >= 0 && y < fromy + 6 && y < MyBotModule.Broodwar.mapHeight(); y++)
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
		
		for (int x = fromx; x >= 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth(); x++)
	        {
	            for (int y = fromy ; y >= 0 && y < fromy + 6 && y < MyBotModule.Broodwar.mapHeight(); y++)
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
    
   
	/// BaseLocation 怨� Mineral / Geyser �궗�씠�쓽 ���씪�뱾�쓽 紐⑸줉�쓣 由ы꽩�빀�땲�떎		
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
		
		int px = unit.getTilePosition().getX();
		int py = unit.getTilePosition().getY();
		
		int fromx = px > 0 ? px - 1 : px;
		int fromy = py > 0 ? py - 1 : py;
		
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		
		if(unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport) {
//			//FileUtils.appendTextToFile("log.txt", "\n setTilesToAvoidAddonBuilding :: fromx & fromy :: ( " + fromx + " , " + fromy + ") :: rwidth & rheight :: ( " + rwidth + " , " + rheight + ")");
		}
		

		for (int x = fromx; x >= 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth() && x < rwidth ; x++)
//		for (int x = fromx; x > 0 && x < fromx + 7 && x < Prebot.Broodwar.mapWidth(); x++)
	        {
//	            for (int y = fromy ; y > 0 && y < fromy + 5 && y < Prebot.Broodwar.mapHeight(); y++)
			for (int y = fromy; y >= 0 && y < fromy + 5 && y < MyBotModule.Broodwar.mapHeight() && y < rheight; y++)
            {
				if( (x==fromx + 5 || x==fromx + 6 || x==fromx + 7) && y == fromy){
					continue;
//					else if(y > fromy) {
//						tilesToAvoidAddonBuilding[x][y] = true;
//					}
				}
				if(unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport) {
					tilesToAvoidAddonBuilding[x][y] = true;
//					//FileUtils.appendTextToFile("log.txt", "\n setTilesToAvoidAddonBuilding :: " + unit.getType() + " :: ( " + x + " , " + y + ")");
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
		
//		//FileUtils.appendTextToFile("log.txt", "\n setTilesToAvoidAddonBuilding :: tilesToAvoidAddonBuilding ::" + tilesToAvoidAddonBuilding);
//		
//		for(TilePosition point : tilesToAvoidAddonBuilding) {
//			//FileUtils.appendTextToFile("avoidTileAddonBuilding.txt", "\n setTilesToAvoidAddonBuilding :: " + point);
//			System.out.println("setTilesToAvoidAddonBuildingFree :: " + point);
//		}
	}
	
	public void setTilesToAvoidAddonBuildingFree(Unit unit) {
		
		int px = unit.getTilePosition().getX();
		int py = unit.getTilePosition().getY();
		
		int fromx = px > 0 ? px - 1 : px;
		int fromy = py > 0 ? py - 1 : py;
		
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		
//		int fromx = unit.getTilePosition().getX()-1;
//		int fromy = unit.getTilePosition().getY()-1;
		
		boolean allFree = false;

//		20180821. hkk. �븷�뱶�삩 嫄대Ъ�씠 遺��뀛議뚮뒗�뜲, 留덉뒪�꽣 嫄대Ъ�씠 �엳嫄곕굹, 留덉뒪�꽣嫄대Ъ�씠 遺��뀛議뚮뒗�뜲, �븷�뱶�삩 嫄대Ъ�씠 �엳�떎硫� 洹� Avoid Tile �� 蹂댁〈�븳�떎.
		if(unit.getType().isAddon()) {
			final UnitType builderType = unit.getType().whatBuilds().first;
			TilePosition builderTile = new TilePosition(fromx + 1 - builderType.tileWidth(), fromy + 1 + 2 - builderType.tileHeight());
			
			
			if(hasWhatBuilds(builderTile, builderType)) {
//				留덉뒪�꽣 嫄대Ъ�씠 �엳�쓣寃쎌슦 ���씪 �떆�옉吏��젏�룄 留덉뒪�꽣 �룷吏��뀡�쑝濡� �옱 �꽕�젙
				allFree = true;
				fromx = builderTile.getX() > 0 ? builderTile.getX() - 1 : 0;
				fromy = builderTile.getY() > 0 ? builderTile.getY() - 1 : 0;
			}

		}else {
			
			if(unit.getAddon() == null) {
				allFree = true;
			}
		}
		
		if(allFree) {
			for (int x = fromx; x >= 0 && x < fromx + 8 && x < MyBotModule.Broodwar.mapWidth() && x < rwidth ; x++)
				{
				for (int y = fromy; y >= 0 && y < fromy + 5 && y < MyBotModule.Broodwar.mapHeight() && y < rheight; y++)
					{
			
					if( (x==fromx + 5 || x==fromx + 6 || x==fromx + 7) && y == fromy){
						continue;
//						else if(y > fromy) {
//							tilesToAvoidAddonBuilding[x][y] = true;
//						}
					}
					if(unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport) {
						tilesToAvoidAddonBuilding[x][y] = false;
//						//FileUtils.appendTextToFile("log.txt", "\n setTilesToAvoidAddonBuilding :: " + unit.getType() + " :: ( " + x + " , " + y + ")");
					}
					tilesToAvoid[x][y] = false;

				}
			}
			
			for(Unit resetAvoid : UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL, UnitType.Terran_Factory, UnitType.Terran_Starport)) {
				if(resetAvoid.isCompleted() || resetAvoid.isConstructing()) {
					ConstructionPlaceFinder.Instance().setTilesToAvoidAddonBuilding(resetAvoid);
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
//					2018015. hkk. 而댁뀑 �옄由щ뒗 而ㅻ㎤�뱶�꽱�꽣�룄 吏볦� 紐삵븯寃뚮걫 �뵾�빐以��떎.
//					if( (x == 4 || x == 5 || x == 6) && TilePositionUtils.equals(cc,InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition())) {
//					if( x == 0 || x == 1 || x == 6) {
//						System.out.println("comsat position of main command :: " + t);
						tilesToAvoidAbsolute[addonX+x][addonY+y] = true;
//					}
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
		
		for (int x = fromx; x >= 0 && x < fromx + 5 && x < MyBotModule.Broodwar.mapWidth(); x++)
//		for (int x = fromx; x > 0 && x < fromx + 7 && x < Prebot.Broodwar.mapWidth(); x++)
	        {
//	            for (int y = fromy ; y > 0 && y < fromy + 5 && y < Prebot.Broodwar.mapHeight(); y++)
			for (int y = fromy ; y >= 0 && y < fromy + 4 && y < MyBotModule.Broodwar.mapHeight(); y++)
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
				
				
//				x異� y異� 紐⑤몢 利앷��뒗 蹂��룞�뾾�쓬
				if(BlockingEntrance.Instance().xinc) {
					if(!BlockingEntrance.Instance().yinc) {
//						5�떆
//						supply_x = BlockingEntrance.Instance().getSupplyPosition(mainbase).getX();
						supply_y = supply_y - (2 * (BlockingEntrance.Instance().maxSupplyCntY -1) ); 
					}

					
				}else {
					if(BlockingEntrance.Instance().yinc) {
//						11�떆
						supply_x = supply_x - (3 * (BlockingEntrance.Instance().maxSupplyCntX -1) ) ;  
					}else {
//						7�떆
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
						tilesToAvoidEntranceTurret[turret_x+x][turret_y+y] = true;
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
				
//				//FileUtils.appendTextToFile("logFinalBuilding.txt", "\n debugBuildLocation :: "+ a +" of lastBuildingPosition :: " + lastBuildingP);
//				System.out.println(" syso debugBuildLocation :: " + lastBuildingP);
				a++;
			}
		}
		
		if(lastBuildingFinal.size() != 0) {
//			System.out.println("lastBuildingFinal is not null :: " + lastBuildingFinal);
			for(TilePosition lastBuildingFinalP : lastBuildingFinal) {
//				//FileUtils.appendTextToFile("logFinalBuilding.txt", "\n debugBuildLocation :: " + b+ " of lastBuildingFinalPosition :: " + lastBuildingFinalP);
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
//				//FileUtils.appendTextToFile("log.txt", "\n Builder MissileTurret :: there is invisible unit in turret radius :: " + radiusP);
//				if(UnitUtils.availableScanningCount() == 0) {
//					//FileUtils.appendTextToFile("log.txt", "\n Builder MissileTurret :: But can't use comsat :: don't construct turret");
//					return false;
//				}
//				
//			}
//		}
//		
//		return true;
//	}
}