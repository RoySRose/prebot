import bwapi.Color;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;

//
public class BlockingEntrance {
	//필요한것
	//스타팅 위치
	//첫 서플 위치
	//배럭 위치
	//첫팩 위치
	//투서플 위치
	//테란&플토는 입막, 저그는 심시티
	/// 건물과 건물간 띄울 최소한의 간격 - 일반적인 건물의 경우
	private static int BuildingSpacingOld = Config.BuildingSpacing;
	/// 건물과 건물간 띄울 최소한의 간격 - ResourceDepot 건물의 경우 (Nexus, Hatchery, Command Center)
	private static int BuildingResourceDepotSpacingOld = Config.BuildingResourceDepotSpacing;
	
	private int startingX = 0;
	private int startingY = 0;
	
	public int first_suppleX = 0;
	public int first_suppleY = 0;
	
	public int second_suppleX = 0;
	public int second_suppleY = 0;
	
	public int barrackX = 0;
	public int barrackY = 0;
	
	public int factoryX = 0;
	public int factoryY = 0;
	
	public int bunkerX = 0;
	public int bunkerY = 0;
	
	public int build_first_suppleX = 0;
	public int build_first_suppleY = 0;
	
	public int build_barrackX = 0;
	public int build_barrackY = 0;
	
	public static boolean blockingEntranceNow = true;
	
	private static int first_suppleX_array[] = null;// new int [];//{29, 52, 96, 102, 93, 55, 12, 23};
	private static int first_suppleY_array[] = new int []{19, 23, 21,   61, 95, 94, 97, 54};

	private static int second_suppleX_array[] = new int []{29, 52, 96, 102, 93, 55, 12, 23};
	private static int second_suppleY_array[] = new int []{19, 23, 21,   61, 95, 94, 97, 54};

	private static int barrackX_array[] = new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int barrackY_array[] = new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private static int factoryX_array[] = new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int factoryY_array[] = new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private static int bunkerX_array[] = new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int bunkerY_array[] = new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private int starting_int = 0;

	private static BlockingEntrance instance = new BlockingEntrance();
	
	public static BlockingEntrance Instance() {
		return instance;
	}
	
	/*public void SetBlockingPosition(){
		getStartingLocation();
	}*/
	

	public void SetBlockingPosition() {
		Config.BuildingSpacing = 0;
		Config.BuildingResourceDepotSpacing = 0;
		//헌터
		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
			//입막용 11시 부터 시계방향으로 세팅
			if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss || MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
				int [] first_suppleXX_array = {29, 52, 96, 102, 93, 55, 12, 23};//private static intnew int []
				first_suppleX_array = first_suppleXX_array;
				int [] first_suppleYY_array = {19, 23, 21,   61, 95, 94, 97, 54};
				first_suppleY_array = first_suppleYY_array;
				int [] second_suppleXX_array= {29, 52, 96, 102, 93, 55, 12, 23};
				second_suppleX_array = second_suppleXX_array;
				int [] second_suppleYY_array = {19, 23, 21,   61, 95, 94, 97, 54};
				second_suppleY_array = second_suppleYY_array;
				int [] barrackXX_array = {26, 54, 98, 104, 90, 52, 14, 20};
				barrackX_array = barrackXX_array;
				int [] barrackYY_array = {21, 25, 23,   63, 97, 96, 99, 56};
				barrackY_array = barrackYY_array;
				int [] factoryXX_array = {26, 54, 98, 104, 90, 52, 14, 20};
				factoryX_array = factoryXX_array;
				int [] factoryYY_array = {21, 25, 23,   63, 97, 96, 99, 56};
				factoryY_array = factoryYY_array;
				int [] bunkerXX_array = {26, 54, 98, 104, 90, 52, 14, 20};
				bunkerX_array = bunkerXX_array;
				int [] bunkerYY_array = {21, 25, 23,   63, 97, 96, 99, 56};
				bunkerY_array = bunkerYY_array;
			}else{
				int [] first_suppleXX_array = {11,70,113,114,114,63,10,9};
				first_suppleX_array = first_suppleXX_array;
				int [] first_suppleYY_array = {11,13,11,83,112,113,111,50};
				first_suppleY_array = first_suppleYY_array;
				int [] second_suppleXX_array= {3,74,110,119,117,56,3,12};
				second_suppleX_array = second_suppleXX_array;
				int [] second_suppleYY_array = {9,7,3,75,114,116,113,42};
				second_suppleY_array = second_suppleYY_array;
				int [] barrackXX_array = {14,66,109,111,110,66,13,12};
				barrackX_array = barrackXX_array;
				int [] barrackYY_array = {9,11,5,77,113,114,112,44};
				barrackY_array = barrackYY_array;
				int [] factoryXX_array = {16,73,107,108,117,66,13,14};
				factoryX_array = factoryXX_array;
				int [] factoryYY_array = {6,11,8,80,111,111,109,47};
				factoryY_array = factoryYY_array;
				int [] bunkerXX_array = {11,70,113,115,114,63,10,8};
				bunkerX_array = bunkerXX_array;
				int [] bunkerYY_array = {9,11,6,78,114,115,113,45};
				bunkerY_array = bunkerYY_array;
			}
		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple){
			if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss || MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
				int [] first_suppleXX_array = {81,113,57,10};//private static intnew int []
				first_suppleX_array = first_suppleXX_array;
				int [] first_suppleYY_array = {6,51,106,61};
				first_suppleY_array = first_suppleYY_array;
				int [] second_suppleXX_array= {78,116,54,13};
				second_suppleX_array = second_suppleXX_array;
				int [] second_suppleYY_array = {6,51,106,61};
				second_suppleY_array = second_suppleYY_array;
				int [] barrackXX_array = {76,118,52,14};
				barrackX_array = barrackXX_array;
				int [] barrackYY_array = {8,53,108,63};
				barrackY_array = barrackYY_array;
				int [] factoryXX_array = {63,117,38,14};
				factoryX_array = factoryXX_array;
				int [] factoryYY_array = {6,37,112,77};
				factoryY_array = factoryYY_array;
				int [] bunkerXX_array = {79,110,55,16};
				bunkerX_array = bunkerXX_array;
				int [] bunkerYY_array = {4,50,104,61};
				bunkerY_array = bunkerYY_array;
			}else{
				int [] first_suppleXX_array = {58,114,28,11};//private static intnew int []
				first_suppleX_array = first_suppleXX_array;
				int [] first_suppleYY_array = {11,26,114,84};
				first_suppleY_array = first_suppleYY_array;
				int [] second_suppleXX_array= {55,117,33,8};
				second_suppleX_array = second_suppleXX_array;
				int [] second_suppleYY_array = {9,30,119,90};
				second_suppleY_array = second_suppleYY_array;
				int [] barrackXX_array = {61,113,31,3};
				barrackX_array = barrackXX_array;
				int [] barrackYY_array = {9,23,116,81};
				barrackY_array = barrackYY_array;
				int [] factoryXX_array = {63,110,31,13};
				factoryX_array = factoryXX_array;
				int [] factoryYY_array = {6,27,113,8};
				factoryY_array = factoryYY_array;
				int [] bunkerXX_array = {58,117,28,5};
				bunkerX_array = bunkerXX_array;
				int [] bunkerYY_array = {9,25,116,84};
				bunkerY_array = bunkerYY_array;
			}
		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.FightingSpririts){
			if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss || MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
				int [] first_suppleXX_array = {10,97,117,22};//private static intnew int []
				first_suppleX_array = first_suppleXX_array;
				int [] first_suppleYY_array = {26,5,120,118};
				first_suppleY_array = first_suppleYY_array;
				int [] second_suppleXX_array= {7,100,125,7};
				second_suppleX_array = second_suppleXX_array;
				int [] second_suppleYY_array = {26,7,108,109};
				second_suppleY_array = second_suppleYY_array;
				int [] barrackXX_array = {4,102,114,24};
				barrackX_array = barrackXX_array;
				int [] barrackYY_array = {28,9,101,120};
				barrackY_array = barrackYY_array;
				int [] factoryXX_array = {11,109,118,28};
				factoryX_array = factoryXX_array;
				int [] factoryYY_array = {13,1,98,121};
				factoryY_array = factoryYY_array;
				int [] bunkerXX_array = {8,102,111,25};
				bunkerX_array = bunkerXX_array;
				int [] bunkerYY_array = {24,5,102,123};
				bunkerY_array = bunkerYY_array;
			}else{
				int [] first_suppleXX_array = {11,121,121,11};//private static intnew int []
				first_suppleX_array = first_suppleXX_array;
				int [] first_suppleYY_array = {5,3,113,115};
				first_suppleY_array = first_suppleYY_array;
				int [] second_suppleXX_array= {0,117,117,8};
				second_suppleX_array = second_suppleXX_array;
				int [] second_suppleYY_array = {14,10,120,119};
				second_suppleY_array = second_suppleYY_array;
				int [] barrackXX_array = {11,113,113,11};
				barrackX_array = barrackXX_array;
				int [] barrackYY_array = {2,4,114,112};
				barrackY_array = barrackYY_array;
				int [] factoryXX_array = {11,111,111,13};
				factoryX_array = factoryXX_array;
				int [] factoryYY_array = {13,9,117,117};
				factoryY_array = factoryYY_array;
				int [] bunkerXX_array = {7,117,117,7};
				bunkerX_array = bunkerXX_array;
				int [] bunkerYY_array = {4,5,115,114};
				bunkerY_array = bunkerYY_array;
			}
		}
		
		
		
		
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if(unit.getType() == UnitType.Terran_Command_Center){
				//System.out.println("unit.getTilePosition().getX() ==>> " + unit.getTilePosition().getX() + "  //  unit.getTilePosition().getY() ==>> " +unit.getTilePosition().getY());
				startingX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
				startingY = unit.getTilePosition().getY();
				//System.out.println("unit.getPosition().getX() ==>> " + startingX + "  //  unit.getPosition().getY() ==>> " + startingY);
			}
		}

		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
			if(startingX == 10 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 70 && startingY == 8){
				starting_int = 1;
			}else if(startingX == 113 && startingY == 8){
				starting_int = 2;
			}else if(startingX == 114 && startingY == 80){
				starting_int = 3;
			}else if(startingX == 114 && startingY == 116){
				starting_int = 4;
			}else if(startingX == 63 && startingY == 117){
				starting_int = 5;
			}else if(startingX == 10 && startingY == 115){
				starting_int = 6;
			}else if(startingX == 8 && startingY == 47){
				starting_int = 7;
			}
		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple){
			if(startingX == 57 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 117 && startingY == 27){
				starting_int = 1;
			}else if(startingX == 27 && startingY == 118){
				starting_int = 2;
			}else if(startingX == 7&& startingY == 87){
				starting_int = 3;
			}
		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.FightingSpririts){
			if(startingX == 7 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 117 && startingY == 7){
				starting_int = 1;
			}else if(startingX == 117 && startingY == 117){
				starting_int = 2;
			}else if(startingX == 7 && startingY == 116){
				starting_int = 3;
			}
		}
		first_suppleX = first_suppleX_array[starting_int];
		first_suppleY = first_suppleY_array[starting_int];
		second_suppleX = second_suppleX_array[starting_int];
		second_suppleY = second_suppleY_array[starting_int];
		//suppleX = 102;
		//suppleY = 61;
		barrackX = barrackX_array[starting_int];
		barrackY = barrackY_array[starting_int];
		factoryX = factoryX_array[starting_int];
		factoryY = factoryY_array[starting_int];
		bunkerX = bunkerX_array[starting_int];
		bunkerY = bunkerY_array[starting_int];
	
	}
	
	public void CheckBlockingPosition() {
		if(MyBotModule.Broodwar.enemy().getRace() == Race.Protoss || MyBotModule.Broodwar.enemy().getRace() == Race.Terran){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if(unit.getType() == UnitType.Terran_Supply_Depot){
					//System.out.println("unit.getTilePosition().getX() ==>> " + unit.getTilePosition().getX() + "  //  unit.getTilePosition().getY() ==>> " +unit.getTilePosition().getY());
					build_first_suppleX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
					build_first_suppleY = unit.getTilePosition().getY();
					if(first_suppleX != build_first_suppleX || first_suppleY != build_first_suppleY){
						/*System.out.println("서플 위치 다름");
						System.out.println("supple : (" + suppleX + " , " + suppleY + ") <> (" + build_suppleX + " , " + build_suppleY + ")" );*/
						blockingEntranceNow = false;
					}
					//System.out.println("unit.getPosition().getX() ==>> " + startingX + "  //  unit.getPosition().getY() ==>> " + startingY);
				}
				if(unit.getType() == UnitType.Terran_Barracks){
					build_barrackX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
					build_barrackY = unit.getTilePosition().getY();
					if(barrackX != build_barrackX || barrackY != build_barrackY){
						/*System.out.println("배럭 위치 다름");
						System.out.println("barrack : (" + barrackX + " , " + barrackY + ") <> (" + build_barrackX + " , " + build_barrackY + ")" );*/
						blockingEntranceNow = false;
					}
				}
				/*if(suppleX != build_suppleX || suppleY != build_suppleY || barrackX != build_barrackX || barrackX != build_barrackY){
					System.out.println("입구안막힘");
					System.out.println("supple : (" + suppleX + " , " + suppleY + ") <> (" + build_suppleX + " , " + build_suppleY + ")" );
					System.out.println("barrack : (" + barrackX + " , " + barrackY + ") <> (" + build_barrackX + " , " + build_barrackY + ")" );
					blockingEntrance = false;
				}*/
			}
		}
	}
	
	public void ReturnBuildSpacing() {
		Config.BuildingSpacing = BuildingSpacingOld;
		Config.BuildingResourceDepotSpacing = BuildingResourceDepotSpacingOld;

	}
	
	public int getStartingInt(){
		return starting_int;
	}
}