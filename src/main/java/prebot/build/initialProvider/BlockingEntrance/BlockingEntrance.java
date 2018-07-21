package prebot.build.initialProvider.BlockingEntrance;

import java.util.HashMap;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation;
import prebot.strategy.MapSpecificInformation.GameMap;

//
public class BlockingEntrance {

	private static BlockingEntrance instance = new BlockingEntrance();
	
	public static BlockingEntrance Instance() {
		return instance;
	}
	
    //필요한것
    //스타팅 위치
    //첫 서플 위치
    //배럭 위치
    //첫팩 위치
    //투서플 위치
    //테란&플토는 입막, 저그는 심시티

    public TilePosition starting = TilePosition.None;
    public TilePosition first_supple = TilePosition.None;
    public TilePosition second_supple = TilePosition.None;
    public TilePosition barrack = TilePosition.None;
    public TilePosition factory = TilePosition.None;
    public TilePosition bunker = TilePosition.None;
    public TilePosition entrance_turret = TilePosition.None;
    public TilePosition supply_area = TilePosition.None;
    //public TilePosition entrance_turret = TilePosition.None;

    public static boolean entranceBlock = true;

//    private static int first_suppleX_array[] = null;// new int [];//{29, 52, 96, 102, 93, 55, 12, 23};
//    private static int first_suppleY_array[] = null;//new int []{19, 23, 21,   61, 95, 94, 97, 54};
//
//    private static int second_suppleX_array[] = null; //new int []{29, 52, 96, 102, 93, 55, 12, 23};
//    private static int second_suppleY_array[] = null; //new int []{19, 23, 21,   61, 95, 94, 97, 54};
//
//    private static int barrackX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
//    private static int barrackY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
//
//    private static int factoryX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
//    private static int factoryY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
//
//    private static int bunkerX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
//    private static int bunkerY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
//
//    private static int fix_supplyX[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
//    private static int fix_supplyY[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};

//    private int starting_int = 0;

    private final int SMALL = 42;
    private final int BIG = 84;
    private final int CENTER = 64;
    
    private static Location loc_t = null;
    
    private Map mapName = null;
    
    /*private static int fix_supplyX[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int fix_supplyY[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};*/

    private HashMap<Integer, TilePosition> postitionStorage = new HashMap<>();

    private final int combine(Map map, Location location, Building building) {
        return map.getValue() * 100 + location.getValue() * 10 + building.getValue();
    }

    public void SetBlockingTilePosition() {
//    	FileUtils.appendTextToFile("log.txt", "\n SetBlockingTilePosition start ==>> ");

        //starting position..... needed?
        for (Unit unit : Prebot.Broodwar.self().getUnits()) {
            if (unit.getType() == UnitType.Terran_Command_Center) {

                //System.out.println("unit.getTilePosition().getX() ==>> " + unit.getTilePosition().getX() + "  //  unit.getTilePosition().getY() ==>> " +unit.getTilePosition().getY());
                starting = new TilePosition(unit.getTilePosition().getX(),unit.getTilePosition().getY());
            }
        }



      //TODO MAP, 지도의 ABCD 이름에 맞춰 바꾸면 될듯
//        mapName = Map.CIRCUITBREAKER;
        if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.FIGHTING_SPRIRITS) {
        	mapName = Map.FIGHTING_SPRIRITS;
        }else if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.CIRCUITBREAKER) {
        	mapName = Map.CIRCUITBREAKER;
        }else {
        	mapName = Map.UNKNOWN;
        }
        //System.out.println("this map ==>> " + map.toString());

        Location loc = Location.START;

        if(starting.getX() < SMALL
                && starting.getY() < SMALL){
            loc = Location.Eleven;
        }
        if(SMALL < starting.getX()  && starting.getX() < BIG
                && starting.getY() < SMALL){
            loc = Location.Twelve;
        }
        if(BIG < starting.getX()
                && starting.getY() < SMALL){
            loc = Location.One;
        }
        if(starting.getX() < SMALL
                && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Nine;
        }
        //center
        if(SMALL < starting.getX()  && starting.getX() < BIG
                 && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Twelve;
        }
        if(BIG < starting.getX()
                && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Three;
        }
        if(starting.getX() < SMALL
                && starting.getY() > SMALL){
            loc = Location.Seven;
        }
       if(SMALL < starting.getX()  && starting.getX() < BIG
                && starting.getY() > SMALL){
            loc = Location.Six;
        }
        if(BIG < starting.getX()
                && starting.getY() > SMALL){
            loc = Location.Five;
        }

        first_supple = postitionStorage.get(combine(mapName, loc, Building.FIRST_SUPPLY));
        second_supple = postitionStorage.get(combine(mapName, loc, Building.SECOND_SUPPLY));
        barrack = postitionStorage.get(combine(mapName, loc, Building.BARRACK));
        factory = postitionStorage.get(combine(mapName, loc, Building.FACTORY));
        bunker = postitionStorage.get(combine(mapName, loc, Building.BUNKER));
        entrance_turret = postitionStorage.get(combine(mapName, loc, Building.ENTRANCE_TURRET));
        supply_area = postitionStorage.get(combine(mapName, loc, Building.SUPPLY_AREA));
        
        loc_t = loc;
        
        System.out.println("this location ==>>>>  " + loc);
        
    }

    public void setBlockingEntrance() {
//    	기존 프리봇1 에서 입구 터렛 위치 추가
//    	FileUtils.appendTextToFile("log.txt", "\n setBlockingEntrance start ==>> ");
    	
//    	맵 : Over_wath
    	if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.OVERWATCH) {
    		
    		
			/*int[] fix_supplyXX = { 0, 115, 94, 0, 21 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 28, 121, 95, 0 };
			fix_supplyY = fix_supplyYY;*/
			
			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.SUPPLY_AREA), new TilePosition(115, 25));
	    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.SUPPLY_AREA), new TilePosition(94, 121));
	    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.SUPPLY_AREA), new TilePosition(0, 95));
	    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.SUPPLY_AREA), new TilePosition(21, 0));
    		
    		if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FIRST_SUPPLY)  , new TilePosition(108, 14));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BARRACK)   		  , new TilePosition(104, 15));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.SECOND_SUPPLY)   , new TilePosition(110, 12));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FACTORY)   		  , new TilePosition(115, 21));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BUNKER)   		  , new TilePosition(108, 16));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.ENTRANCE_TURRET), new TilePosition(111, 14));

		    	
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FIRST_SUPPLY)  , new TilePosition(113, 107));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BARRACK)   		 , new TilePosition(109, 107));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.SECOND_SUPPLY)   , new TilePosition(107, 105));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FACTORY)   		 , new TilePosition(107, 115));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BUNKER)   		   , new TilePosition(113, 109));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.ENTRANCE_TURRET), new TilePosition(116, 108));

		    	
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FIRST_SUPPLY)  , new TilePosition(20, 110));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BARRACK)   		  , new TilePosition(16, 112));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.SECOND_SUPPLY)   , new TilePosition(23, 110));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FACTORY)   		  , new TilePosition(7 , 105));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BUNKER)   		  , new TilePosition(20, 108));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.ENTRANCE_TURRET), new TilePosition(18, 110));
		
		
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FIRST_SUPPLY)  , new TilePosition(16, 20));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BARRACK)   		 , new TilePosition(12, 18));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.SECOND_SUPPLY)   , new TilePosition(18, 22));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FACTORY)   		 , new TilePosition(17, 10));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BUNKER)   		   , new TilePosition(16, 18));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.ENTRANCE_TURRET), new TilePosition(19, 20));
    		}else {
    			
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FIRST_SUPPLY)  , new TilePosition(114, 13));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BARRACK)   		  , new TilePosition(116, 15));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.SECOND_SUPPLY)   , new TilePosition(120, 44));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FACTORY)   		  , new TilePosition(120, 16));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BUNKER)   		  , new TilePosition(117, 13));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.ENTRANCE_TURRET), TilePosition.None);


    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FIRST_SUPPLY)  , new TilePosition(121, 113));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BARRACK)   		 , new TilePosition(113, 112));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.SECOND_SUPPLY)   , new TilePosition(113, 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FACTORY)   		 , new TilePosition(117, 110));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BUNKER)   		   , new TilePosition(116, 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.ENTRANCE_TURRET), TilePosition.None);


    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FIRST_SUPPLY)  , new TilePosition(11, 113));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BARRACK)   		  , new TilePosition(11, 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.SECOND_SUPPLY)   , new TilePosition(4 , 113));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FACTORY)   		  , new TilePosition(7 , 110));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BUNKER)   		  , new TilePosition(6 , 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.ENTRANCE_TURRET), TilePosition.None);


    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FIRST_SUPPLY)  , new TilePosition(0 , 15));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BARRACK)   		 , new TilePosition(10, 14));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.SECOND_SUPPLY)   , new TilePosition(7 , 15));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FACTORY)   		 , new TilePosition(3 , 16));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BUNKER)   		   , new TilePosition(7 , 13));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.ENTRANCE_TURRET), TilePosition.None);

    			
    		}
    		
    		
    	}
    	
    	else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.CIRCUITBREAKER) {
			
			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.SUPPLY_AREA), new TilePosition(101, 0));
	    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.SUPPLY_AREA), new TilePosition(101, 119));
	    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.SUPPLY_AREA), new TilePosition(18, 119));
	    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.SUPPLY_AREA), new TilePosition(18, 0));
    	
    	
//    	맵 : CIRCUITBREAKER
    		if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FIRST_SUPPLY)  , new TilePosition(122, 24));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BARRACK)   		 , new TilePosition(118, 23));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.SECOND_SUPPLY)   , new TilePosition(125, 25));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FACTORY)   		 , new TilePosition(113, 17));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BUNKER)   		   , new TilePosition(122, 22));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.ENTRANCE_TURRET), new TilePosition(125, 23));
		
		
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FIRST_SUPPLY)  , new TilePosition(122, 101));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BARRACK)   		  , new TilePosition(118, 102));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.SECOND_SUPPLY)   , new TilePosition(125, 100));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FACTORY)   		  , new TilePosition(118, 109));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BUNKER)   		  , new TilePosition(122, 103));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.ENTRANCE_TURRET), new TilePosition(125, 102));
		
		
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FIRST_SUPPLY)  , new TilePosition(4 , 101));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BARRACK)   		 , new TilePosition(0 , 100));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.SECOND_SUPPLY)   , new TilePosition(7 , 102));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FACTORY)   		 , new TilePosition(14, 110));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BUNKER)   		   , new TilePosition(4 , 103));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.ENTRANCE_TURRET), new TilePosition(7 , 104));
		
		
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FIRST_SUPPLY)  , new TilePosition(5 , 23));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BARRACK)   		  , new TilePosition(1 , 24));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.SECOND_SUPPLY)   , new TilePosition(8 , 23));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FACTORY)   		  , new TilePosition(12, 17));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BUNKER)   		  , new TilePosition(5 , 21));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.ENTRANCE_TURRET), new TilePosition(8 , 21));
		    	
    		}else {
    			
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FIRST_SUPPLY)  , new TilePosition(114, 13));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BARRACK)   		 , new TilePosition(116, 15));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.SECOND_SUPPLY)   , new TilePosition(120, 44));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FACTORY)   		 , new TilePosition(120, 16));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BUNKER)   		   , new TilePosition(117, 13));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.ENTRANCE_TURRET), TilePosition.None);

    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FIRST_SUPPLY)  , new TilePosition(121, 113));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BARRACK)   		  , new TilePosition(113, 112));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.SECOND_SUPPLY)   , new TilePosition(113, 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FACTORY)   		  , new TilePosition(117, 110));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BUNKER)   		  , new TilePosition(116, 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.ENTRANCE_TURRET), TilePosition.None);


    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FIRST_SUPPLY)  , new TilePosition(11, 113));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BARRACK)   		 , new TilePosition(11, 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.SECOND_SUPPLY)   , new TilePosition(4 , 113));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FACTORY)   		 , new TilePosition(7 , 110));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BUNKER)   		   , new TilePosition(6 , 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.ENTRANCE_TURRET), TilePosition.None);


    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FIRST_SUPPLY)  , new TilePosition(0 , 15));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BARRACK)   		  , new TilePosition(10, 14));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.SECOND_SUPPLY)   , new TilePosition(7 , 15));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FACTORY)   		  , new TilePosition(3 , 16));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BUNKER)   		  , new TilePosition(7 , 13));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.ENTRANCE_TURRET), TilePosition.None);


    			
    		}
    	}
    	else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.FIGHTING_SPRIRITS) {
    		
//    		System.out.println("맵 ==>> 투혼");
    		
    		
    		postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.SUPPLY_AREA), new TilePosition(118, 23));
	    	postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.SUPPLY_AREA), new TilePosition(103, 119));
	    	postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.SUPPLY_AREA), new TilePosition(0, 98));
	    	postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.SUPPLY_AREA), new TilePosition(18, 13));
	    	
	    	
//        	맵 : 투혼
    		if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.FIRST_SUPPLY)     , new TilePosition(100	,7));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.BARRACK)   		   , new TilePosition(102	,9));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.SECOND_SUPPLY)    , new TilePosition(97	,5));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.FACTORY)   		   , new TilePosition(110	,9));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.BUNKER)   		     , new TilePosition(100	,5));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.ENTRANCE_TURRET)  , new TilePosition(103	,7));

    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.FIRST_SUPPLY)    , new TilePosition(118	,100));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.BARRACK)   		   , new TilePosition(114	,101));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.SECOND_SUPPLY)   , new TilePosition(120	,98 ));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.FACTORY)   		   , new TilePosition(115	,109));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.BUNKER)   		   , new TilePosition(118	,102));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.ENTRANCE_TURRET) , new TilePosition(121	,100));

    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.FIRST_SUPPLY)   , new TilePosition(25	,120));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.BARRACK)   		 , new TilePosition(21	,118));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.SECOND_SUPPLY)  , new TilePosition(28	,121));  
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.FACTORY)   		 , new TilePosition(14	,115));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.BUNKER)   		   , new TilePosition(25	,122));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.ENTRANCE_TURRET), new TilePosition(23	,122));

    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.FIRST_SUPPLY)   , new TilePosition(7	,26  ));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.BARRACK)   		  , new TilePosition(4	,28  ));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.SECOND_SUPPLY)  , new TilePosition(10	,26));  
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.FACTORY)   		  , new TilePosition(13	,11));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.BUNKER)   		  , new TilePosition(7	,24  ));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.ENTRANCE_TURRET), new TilePosition(10	,24));

		    	
    		}else {
    			
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.FIRST_SUPPLY)     , new TilePosition(114	,2));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.BARRACK)   		   , new TilePosition(111	,4));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.SECOND_SUPPLY)    , new TilePosition(114	,8));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.FACTORY)   		   , new TilePosition(110	,9));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.BUNKER)   		     , new TilePosition(115	,4));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.One, Building.ENTRANCE_TURRET), TilePosition.None);


    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.FIRST_SUPPLY)    , new TilePosition(114	,112));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.BARRACK)   		   , new TilePosition(111	,114));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.SECOND_SUPPLY)   , new TilePosition(114	,118));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.FACTORY)   		   , new TilePosition(115	,109));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.BUNKER)   		   , new TilePosition(115	,114));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Five, Building.ENTRANCE_TURRET), TilePosition.None);

    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.FIRST_SUPPLY)   , new TilePosition(8	  ,122));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.BARRACK)   		 , new TilePosition(11	,120));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.SECOND_SUPPLY)  , new TilePosition(0	  ,122));  
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.FACTORY)   		 , new TilePosition(14	,115));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.BUNKER)   		   , new TilePosition(8	  ,120));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Seven, Building.ENTRANCE_TURRET), TilePosition.None);

    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.FIRST_SUPPLY)   , new TilePosition(6	,13));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.BARRACK)   		  , new TilePosition(9	,11));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.SECOND_SUPPLY)  , new TilePosition(0	,12));  
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.FACTORY)   		  , new TilePosition(13	,11));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.BUNKER)   		  , new TilePosition(6	,11));
    			postitionStorage.put(combine(Map.FIGHTING_SPRIRITS, Location.Eleven, Building.ENTRANCE_TURRET), TilePosition.None);



    			
    		}
    		
    	}else {
    		
    		System.out.println("맵 ==>> UNKNOWN");
    		
    		
    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.SUPPLY_AREA), TilePosition.None);         
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.SUPPLY_AREA), TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.SUPPLY_AREA), TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.SUPPLY_AREA), TilePosition.None);

    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.FIRST_SUPPLY)     , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.BARRACK)   		   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.SECOND_SUPPLY)    , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.FACTORY)   		   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.BUNKER)   		     , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.One, Building.ENTRANCE_TURRET), TilePosition.None);


    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.FIRST_SUPPLY)    , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.BARRACK)   		   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.SECOND_SUPPLY)   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.FACTORY)   		   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.BUNKER)   		   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Five, Building.ENTRANCE_TURRET), TilePosition.None);

    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.FIRST_SUPPLY)   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.BARRACK)   		 , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.SECOND_SUPPLY)  , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.FACTORY)   		 , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.BUNKER)   		   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Seven, Building.ENTRANCE_TURRET), TilePosition.None);

    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.FIRST_SUPPLY)   , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.BARRACK)   		  , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.SECOND_SUPPLY)  , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.FACTORY)   		  , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.BUNKER)   		  , TilePosition.None);
    		postitionStorage.put(combine(Map.UNKNOWN, Location.Eleven, Building.ENTRANCE_TURRET), TilePosition.None);
    		
    	}

    }
	
	public final TilePosition getSupplyPosition(TilePosition tilepos)
	{
		
//		Location loc = Location.START;
//
//        if(tilepos.getX() < SMALL
//                && tilepos.getY() < SMALL){
//            loc = Location.Eleven;
//        }
//
//        if(BIG < tilepos.getX()
//                && tilepos.getY() < SMALL){
//            loc = Location.One;
//        }
//     
//        if(tilepos.getX() < SMALL
//                && tilepos.getY() > SMALL){
//            loc = Location.Seven;
//        }
//
//        if(BIG < tilepos.getX()
//                && tilepos.getY() > SMALL){
//            loc = Location.Five;
//        }
		Location loc = Location.START;

        if(starting.getX() < SMALL
                && starting.getY() < SMALL){
            loc = Location.Eleven;
        }
        if(SMALL < starting.getX()  && starting.getX() < BIG
                && starting.getY() < SMALL){
            loc = Location.Twelve;
        }
        if(BIG < starting.getX()
                && starting.getY() < SMALL){
            loc = Location.One;
        }
        if(starting.getX() < SMALL
                && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Nine;
        }
        //center
        if(SMALL < starting.getX()  && starting.getX() < BIG
                 && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Twelve;
        }
        if(BIG < starting.getX()
                && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Three;
        }
        if(starting.getX() < SMALL
                && starting.getY() > SMALL){
            loc = Location.Seven;
        }
       if(SMALL < starting.getX()  && starting.getX() < BIG
                && starting.getY() > SMALL){
            loc = Location.Six;
        }
        if(BIG < starting.getX()
                && starting.getY() > SMALL){
            loc = Location.Five;
        }
        
		TilePosition supply_pos = postitionStorage.get(combine(mapName, loc, Building.SUPPLY_AREA));
		return supply_pos;

	}
	
	public final TilePosition getSupplyPosition()
	{
		System.out.println("getSupplyPosition start");
		System.out.println("getSupplyPosition mapName :: " + mapName);
		System.out.println("getSupplyPosition loc_t :: " + loc_t);
		System.out.println("getSupplyPosition SUPPLY_AREA :: " + Building.SUPPLY_AREA);
		
		TilePosition supply_pos = postitionStorage.get(combine(mapName, loc_t, Building.SUPPLY_AREA));
		System.out.println(" supply_pos end==>>> ( " + supply_pos.getX() + " , " + supply_pos.getY() + " ) ");
		return supply_pos;
	}
}
