package prebot.build.initialProvider.BlockingEntrance;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation;
import prebot.strategy.constant.StrategyCode;


import java.util.HashMap;

//
public class BlockingEntrance {
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
    public TilePosition barrack = TilePosition.None;;
    public TilePosition factory = TilePosition.None;
    public TilePosition bunker = TilePosition.None;
    public TilePosition entrance_turret = TilePosition.None;

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
    
    private static int fix_supplyX[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int fix_supplyY[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};

    private HashMap<Integer, TilePosition> postitionStorage = new HashMap<>();

    private final int combine(Map map, Location location, Building building) {
        return map.getValue() * 100 + location.getValue() * 10 + building.getValue();
    }

    public void SetBlockingTilePosition() {

        //starting position..... needed?
        for (Unit unit : Prebot.Broodwar.self().getUnits()) {
            if (unit.getType() == UnitType.Terran_Command_Center) {

                //System.out.println("unit.getTilePosition().getX() ==>> " + unit.getTilePosition().getX() + "  //  unit.getTilePosition().getY() ==>> " +unit.getTilePosition().getY());
                starting = new TilePosition(unit.getTilePosition().getX(),unit.getTilePosition().getY());
            }
        }



        //TODO MAP, 지도의 ABCD 이름에 맞춰 바꾸면 될듯
        Map map = Map.OVERWATCH;
        if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.CIRCUITBREAKER) {
            map = Map.CIRCUITBREAKER;
        }
        System.out.println("this map ==>> " + map.toString());

        Location loc = Location.START;

        if(starting.getX() < SMALL
                && starting.getY() < SMALL){
            loc = Location.Eleven;
        }
        /*if(SMALL < starting.getX()  && starting.getX() < BIG
                && starting.getY() < SMALL){
            loc = Location.Twelve;
        }*/
        if(BIG < starting.getX()
                && starting.getY() < SMALL){
            loc = Location.One;
        }
        /*if(starting.getX() < SMALL
                && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Nine;
        }*/
        //center
//        if(SMALL < starting.getX()  && starting.getX() < BIG
//                 && SMALL < starting.getY()  && starting.getY() < BIG){
//            loc = Location.Twelve;
//        }
        /*if(BIG < starting.getX()
                && SMALL < starting.getY()  && starting.getY() < BIG){
            loc = Location.Three;
        }*/
        if(starting.getX() < SMALL
                && starting.getY() > SMALL){
            loc = Location.Seven;
        }
        /*if(SMALL < starting.getX()  && starting.getX() < BIG
                && starting.getY() > SMALL){
            loc = Location.Six;
        }*/
        if(BIG < starting.getX()
                && starting.getY() > SMALL){
            loc = Location.Five;
        }

//            if(starting.getX() < topLeft.getX() && starting.getY() < topLeft.getY()){
//                loc = Location.Eleven;
//            }
//            if(starting.getX() < topLeft.getX() && starting.getY() < topLeft.getY()){
//                loc = Location.Eleven;
//            }
//            if(starting.getX() < topLeft.getX() && starting.getY() < topLeft.getY()){
//                loc = Location.Eleven;
//            }
//            if(starting.getX() < topLeft.getX() && starting.getY() < topLeft.getY()){
//                loc = Location.Eleven;
//            }
//            if(starting.getX() < topLeft.getX() && starting.getY() < topLeft.getY()){
//                loc = Location.Eleven;
//            }
//            if(starting.getX() < topLeft.getX() && starting.getY() < topLeft.getY()){
//                loc = Location.Eleven;
//            }

//        if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.THE_HUNTERS) {
//
//            //else if(startingX == 70 && startingY == 8){
////				starting_int = 1;
////			}else if(startingX == 113 && startingY == 8){
////				starting_int = 2;
////			}else if(startingX == 114 && startingY == 80){
////				starting_int = 3;
////			}else if(startingX == 114 && startingY == 116){
////				starting_int = 4;
////			}else if(startingX == 63 && startingY == 117){
////				starting_int = 5;
////			}else if(startingX == 10 && startingY == 115){
////				starting_int = 6;
////			}else if(startingX == 8 && startingY == 47){
////				starting_int = 7;
////			}
//
//        }

        first_supple = postitionStorage.get(combine(map, loc, Building.FIRST_SUPPLY));
        second_supple = postitionStorage.get(combine(map, loc, Building.SECOND_SUPPLY));
        barrack = postitionStorage.get(combine(map, loc, Building.BARRACK));
        factory = postitionStorage.get(combine(map, loc, Building.FACTORY));
        bunker = postitionStorage.get(combine(map, loc, Building.BUNKER));
        entrance_turret = postitionStorage.get(combine(map, loc, Building.ENTRANCE_TURRET));
        
//      avoid supply 설정
        ConstructionPlaceFinder.Instance().setTilesToAvoidSupply();
        
        loc_t = loc;
        
        System.out.println("loc_t of getValue ==>> " + loc_t.getValue());

    }
    
    
    public final TilePosition getSupplyPosition(TilePosition tilepos)
	{
		
		TilePosition supply_pos= new TilePosition(fix_supplyX[loc_t.getValue()], fix_supplyY[loc_t.getValue()]);
		return supply_pos;

	}
	
	public final TilePosition getSupplyPosition()
	{
			TilePosition supply_pos= new TilePosition(fix_supplyX[loc_t.getValue()], fix_supplyY[loc_t.getValue()]);
			return supply_pos;
	}

    public BlockingEntrance() {
//    	기존 프리봇1 에서 입구 터렛 위치 추가
    	
    	
//    	맵 : Over_wath

    	if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.OVERWATCH) {
    		int[] fix_supplyXX = { 0, 115, 94, 0, 21 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 28, 121, 95, 0 };
			fix_supplyY = fix_supplyYY;
    		
    		if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FIRST_SUPPLY)   , new TilePosition(110, 12));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BARRACK)   		  , new TilePosition(104, 15));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.SECOND_SUPPLY)  , new TilePosition(108, 14));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FACTORY)   		  , new TilePosition(115, 21));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BUNKER)   		  , new TilePosition(108, 16));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.ENTRANCE_TURRET), new TilePosition(111, 14));
		
		
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FIRST_SUPPLY)   , new TilePosition(107, 105));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BARRACK)   		 , new TilePosition(109, 107));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.SECOND_SUPPLY)  , new TilePosition(113, 107));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FACTORY)   		 , new TilePosition(107, 115));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BUNKER)   		   , new TilePosition(113, 109));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.ENTRANCE_TURRET), new TilePosition(116, 108));
		
		
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FIRST_SUPPLY)   , new TilePosition(23, 110));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BARRACK)   		  , new TilePosition(16, 112));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.SECOND_SUPPLY)  , new TilePosition(20, 110));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FACTORY)   		  , new TilePosition(7 , 105));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BUNKER)   		  , new TilePosition(20, 108));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.ENTRANCE_TURRET), new TilePosition(18, 110));
		
		
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FIRST_SUPPLY)   , new TilePosition(18, 22));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BARRACK)   		 , new TilePosition(12, 18));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.SECOND_SUPPLY)  , new TilePosition(16, 20));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FACTORY)   		 , new TilePosition(17, 10));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BUNKER)   		   , new TilePosition(16, 18));
		    	postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.ENTRANCE_TURRET), new TilePosition(19, 20));
    		}else {
    			
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FIRST_SUPPLY)   , new TilePosition(120, 44));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BARRACK)   		  , new TilePosition(116, 15));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.SECOND_SUPPLY)  , new TilePosition(114, 13));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.FACTORY)   		  , new TilePosition(120, 16));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BUNKER)   		  , new TilePosition(117, 13));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.ENTRANCE_TURRET), null);


    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FIRST_SUPPLY)   , new TilePosition(113, 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BARRACK)   		 , new TilePosition(113, 112));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.SECOND_SUPPLY)  , new TilePosition(121, 113));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FACTORY)   		 , new TilePosition(117, 110));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BUNKER)   		   , new TilePosition(116, 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.ENTRANCE_TURRET), null);


    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FIRST_SUPPLY)   , new TilePosition(4 , 113));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BARRACK)   		  , new TilePosition(11, 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.SECOND_SUPPLY)  , new TilePosition(11, 113));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FACTORY)   		  , new TilePosition(7 , 110));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BUNKER)   		  , new TilePosition(6 , 115));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.ENTRANCE_TURRET), null);


    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FIRST_SUPPLY)   , new TilePosition(7 , 15));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BARRACK)   		 , new TilePosition(10, 14));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.SECOND_SUPPLY)  , new TilePosition(0 , 15));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FACTORY)   		 , new TilePosition(3 , 16));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BUNKER)   		   , new TilePosition(7 , 13));
    			postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.ENTRANCE_TURRET), null);

    			
    		}
    	}
    	
    	else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.CIRCUITBREAKER) {
    		int[] fix_supplyXX = { 0, 101, 101, 15, 15 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 0, 121, 121, 0 };
			fix_supplyY = fix_supplyYY;
    	
//    	맵 : CIRCUITBREAKER
    		if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FIRST_SUPPLY)   , new TilePosition(125, 25));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BARRACK)   		 , new TilePosition(118, 23));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.SECOND_SUPPLY)  , new TilePosition(122, 24));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FACTORY)   		 , new TilePosition(113, 17));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BUNKER)   		   , new TilePosition(122, 22));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.ENTRANCE_TURRET), new TilePosition(125, 23));
		
		
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FIRST_SUPPLY)   , new TilePosition(125, 100));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BARRACK)   		  , new TilePosition(118, 102));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.SECOND_SUPPLY)  , new TilePosition(122, 101));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FACTORY)   		  , new TilePosition(118, 109));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BUNKER)   		  , new TilePosition(122, 103));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.ENTRANCE_TURRET), new TilePosition(125, 102));
		
		
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FIRST_SUPPLY)   , new TilePosition(7 , 102));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BARRACK)   		 , new TilePosition(0 , 100));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.SECOND_SUPPLY)  , new TilePosition(4 , 101));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FACTORY)   		 , new TilePosition(14, 110));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BUNKER)   		   , new TilePosition(4 , 103));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.ENTRANCE_TURRET), new TilePosition(7 , 104));
		
		
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FIRST_SUPPLY)   , new TilePosition(8 , 23));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BARRACK)   		  , new TilePosition(1 , 24));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.SECOND_SUPPLY)  , new TilePosition(5 , 23));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FACTORY)   		  , new TilePosition(12, 17));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BUNKER)   		  , new TilePosition(5 , 21));
		    	postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.ENTRANCE_TURRET), new TilePosition(8 , 21));
		    	
    		}else {
    			
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FIRST_SUPPLY)   , new TilePosition(120, 44));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BARRACK)   		 , new TilePosition(116, 15));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.SECOND_SUPPLY)  , new TilePosition(114, 13));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.FACTORY)   		 , new TilePosition(120, 16));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.BUNKER)   		   , new TilePosition(117, 13));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.One, Building.ENTRANCE_TURRET), null);

    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FIRST_SUPPLY)   , new TilePosition(113, 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BARRACK)   		  , new TilePosition(113, 112));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.SECOND_SUPPLY)  , new TilePosition(121, 113));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.FACTORY)   		  , new TilePosition(117, 110));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.BUNKER)   		  , new TilePosition(116, 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Five, Building.ENTRANCE_TURRET), null);


    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FIRST_SUPPLY)   , new TilePosition(4 , 113));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BARRACK)   		 , new TilePosition(11, 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.SECOND_SUPPLY)  , new TilePosition(11, 113));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.FACTORY)   		 , new TilePosition(7 , 110));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.BUNKER)   		   , new TilePosition(6 , 115));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Seven, Building.ENTRANCE_TURRET), null);


    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FIRST_SUPPLY)   , new TilePosition(7 , 15));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BARRACK)   		  , new TilePosition(10, 14));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.SECOND_SUPPLY)  , new TilePosition(0 , 15));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.FACTORY)   		  , new TilePosition(3 , 16));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.BUNKER)   		  , new TilePosition(7 , 13));
    			postitionStorage.put(combine(Map.CIRCUITBREAKER, Location.Eleven, Building.ENTRANCE_TURRET), null);


    			
    		}
    	}




        /*postitionStorage.put(combine(Map.OVERWATCH, Location.Three, Building.FIRST_SUPPLY) , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.FIRST_SUPPLY)  , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Six, Building.FIRST_SUPPLY)   , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.FIRST_SUPPLY) , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Nine, Building.FIRST_SUPPLY)  , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.FIRST_SUPPLY), new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Twelve, Building.FIRST_SUPPLY), new TilePosition(1, 1));

        postitionStorage.put(combine(Map.OVERWATCH, Location.One, Building.BARRACK)   , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Three, Building.BARRACK) , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Five, Building.BARRACK)  , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Six, Building.BARRACK)   , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Seven, Building.BARRACK) , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Nine, Building.BARRACK)  , new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Eleven, Building.BARRACK), new TilePosition(1, 1));
        postitionStorage.put(combine(Map.OVERWATCH, Location.Twelve, Building.BARRACK), new TilePosition(1, 1));*/

        //...
        //...
        //...
        //...
        //...


        SetBlockingTilePosition();
    }
}


		//헌터
//		if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.THE_HUNTERS) {
//			// ConstructionPlaceFinder.Instance().maxSupplyCntX = 3;
//			int[] fix_supplyXX = { 0, 52, 118, 118, 118, 46, 0, 0 };
//			fix_supplyX = fix_supplyXX;
//			int[] fix_supplyYY = { 10, 0, 17, 66, 105, 110, 102, 54 };
//			fix_supplyY = fix_supplyYY;
//			// 입막용 11시 부터 시계방향으로 세팅
//			if (InformationManager.Instance().enemyRace == Race.Protoss
//					|| InformationManager.Instance().enemyRace == Race.Terran) {
//				int[] first_suppleXX_array = { 29, 55, 100, 102, 99, 55, 12, 24 };// private
//																					// static
//																					// intnew
//																					// int
//																					// []
//				first_suppleX_array = first_suppleXX_array;
//				int[] first_suppleYY_array = { 19, 26, 23, 61, 100, 94, 97, 55 };
//				first_suppleY_array = first_suppleYY_array;
//				int[] second_suppleXX_array = { 3, 71, 113, 114, 107, 56, 3, 9 };
//				second_suppleX_array = second_suppleXX_array;
//				int[] second_suppleYY_array = { 9, 11, 11, 83, 115, 116, 113, 50 };
//				second_suppleY_array = second_suppleYY_array;
//				int[] barrackXX_array = { 25, 51, 96, 104, 95, 52, 14, 20 };
//				barrackX_array = barrackXX_array;
//				int[] barrackYY_array = { 20, 24, 21, 63, 101, 96, 99, 56 };
//				barrackY_array = barrackYY_array;
//				int[] factoryXX_array = { 20, 53, 100, 119, 108, 64, 0, 0 };
//				factoryX_array = factoryXX_array;
//				int[] factoryYY_array = { 9, 11, 5, 59, 107, 109, 106, 55 };
//				factoryY_array = factoryYY_array;
//				int[] bunkerXX_array = { 26, 55, 99, 105, 99, 56, 11, 21 };
//			    bunkerX_array = bunkerXX_array;
//			    int[] bunkerYY_array = { 18, 24, 21, 61, 102, 96, 99, 54 };
//			    bunkerY_array = bunkerYY_array;
//			} else {
//				int[] first_suppleXX_array = { 10, 71, 113, 114, 115, 63, 10, 8 };
//				first_suppleX_array = first_suppleXX_array;
//				int[] first_suppleYY_array = { 11, 13, 13, 85, 112, 113, 111, 52 };
//				first_suppleY_array = first_suppleYY_array;
//				int[] second_suppleXX_array = { 3, 76, 120, 120, 107, 72, 3, 14 };
//				second_suppleX_array = second_suppleXX_array;
//				int[] second_suppleYY_array = { 9, 9, 14, 87, 115, 121, 113, 48 };
//				second_suppleY_array = second_suppleYY_array;
//				int[] barrackXX_array = { 13, 67, 109, 110, 111, 60, 13, 11 };
//				barrackX_array = barrackXX_array;
//				int[] barrackYY_array = { 9, 11, 10, 82, 113, 114, 112, 50 };
//				barrackY_array = barrackYY_array;
//				int[] factoryXX_array = { 14, 63, 109, 108, 109, 69, 14, 12 };
//				factoryX_array = factoryXX_array;
//				int[] factoryYY_array = { 12, 11, 14, 79, 110, 114, 109, 53 };
//				factoryY_array = factoryYY_array;
//				int[] bunkerXX_array = { 10, 71, 113, 114, 115, 63, 10, 8 };
//				bunkerX_array = bunkerXX_array;
//				int[] bunkerYY_array = { 9, 11, 11, 83, 114, 115, 113, 50 };
//				bunkerY_array = bunkerYY_array;
//			}
//		} else if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.LOST_TEMPLE) {
//			int[] fix_supplyXX = { 66, 118, 40, 0 };
//			fix_supplyX = fix_supplyXX;
//			int[] fix_supplyYY = { 0, 37, 118, 74 };
//			fix_supplyY = fix_supplyYY;
//			if (InformationManager.Instance().enemyRace == Race.Protoss
//					|| InformationManager.Instance().enemyRace == Race.Terran) {
//				int[] first_suppleXX_array = { 77, 117, 53, 13 };// private
//																	// static
//																	// intnew
//																	// int []
//				first_suppleX_array = first_suppleXX_array;
//				int[] first_suppleYY_array = { 6, 51, 106, 61 };
//				first_suppleY_array = first_suppleYY_array;
//				int[] second_suppleXX_array = { 80, 119, 56, 10 };
//				second_suppleX_array = second_suppleXX_array;
//				int[] second_suppleYY_array = { 5, 53, 105, 60 };
//				second_suppleY_array = second_suppleYY_array;
//				int[] barrackXX_array = { 76, 113, 52, 14 };
//				barrackX_array = barrackXX_array;
//				int[] barrackYY_array = { 8, 50, 108, 63 };
//				barrackY_array = barrackYY_array;
//				int[] factoryXX_array = { 67, 107, 41, 20 };
//				factoryX_array = factoryXX_array;
//				int[] factoryYY_array = { 12, 38, 109, 82 };
//				factoryY_array = factoryYY_array;
//				int[] bunkerXX_array = { 77, 110, 53, 16 };
//			    bunkerX_array = bunkerXX_array;
//			    int[] bunkerYY_array = { 4, 51, 104, 61 };
//			    bunkerY_array = bunkerYY_array;
//			} else {
//				int[] first_suppleXX_array = { 57, 117, 27, 7 };// private
//																// static intnew
//																// int []
//				first_suppleX_array = first_suppleXX_array;
//				int[] first_suppleYY_array = { 11, 32, 114, 92 };
//				first_suppleY_array = first_suppleYY_array;
//				int[] second_suppleXX_array = { 50, 114, 20, 11 };
//				second_suppleX_array = second_suppleXX_array;
//				int[] second_suppleYY_array = { 8, 27, 116, 86 };
//				second_suppleY_array = second_suppleYY_array;
//				int[] barrackXX_array = { 60, 113, 30, 10 };
//				barrackX_array = barrackXX_array;
//				int[] barrackYY_array = { 9, 29, 115, 90 };
//				barrackY_array = barrackYY_array;
//				int[] factoryXX_array = { 60, 113, 30, 10 };
//				factoryX_array = factoryXX_array;
//				int[] factoryYY_array = { 12, 33, 112, 93 };
//				factoryY_array = factoryYY_array;
//				int[] bunkerXX_array = { 57, 117, 27, 7 };
//				bunkerX_array = bunkerXX_array;
//				int[] bunkerYY_array = { 9, 30, 116, 90 };
//				bunkerY_array = bunkerYY_array;
//			}
//		} else if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.FIGHTING_SPRIRITS) {
//			/*
//			 * ConstructionPlaceFinder.Instance().maxSupplyCntX = 3;
//			 * ConstructionPlaceFinder.Instance().maxSupplyCntY = 4;
//			 */
//
//			int[] fix_supplyXX = { 0, 118, 104, 0 };
//			fix_supplyX = fix_supplyXX;
//			int[] fix_supplyYY = { 15, 17, 118, 102 };
//			fix_supplyY = fix_supplyYY;
//			if (InformationManager.Instance().enemyRace == Race.Protoss
//					|| InformationManager.Instance().enemyRace == Race.Terran) {
//				int[] first_suppleXX_array = { 7, 100, 118, 25 };// private
//																	// static
//																	// intnew
//																	// int []
//				first_suppleX_array = first_suppleXX_array;
//				int[] first_suppleYY_array = { 26, 7, 100, 120 };
//				first_suppleY_array = first_suppleYY_array;
//				int[] second_suppleXX_array = { 10, 97, 120, 28 };
//				second_suppleX_array = second_suppleXX_array;
//				int[] second_suppleYY_array = { 26, 5, 98, 121 };
//				second_suppleY_array = second_suppleYY_array;
//				int[] barrackXX_array = { 4, 102, 114, 21 };
//				barrackX_array = barrackXX_array;
//				int[] barrackYY_array = { 28, 9, 101, 118 };
//				barrackY_array = barrackYY_array;
//				int[] factoryXX_array = { 11, 104, 110, 15 };
//				factoryX_array = factoryXX_array;
//				int[] factoryYY_array = { 16, 0, 107, 111 };
//				factoryY_array = factoryYY_array;
//				int[] bunkerXX_array = { 13, 100, 111, 25 };
//			    bunkerX_array = bunkerXX_array;
//			    int[] bunkerYY_array = { 26, 5, 101, 122 };
//			    bunkerY_array = bunkerYY_array;
//			} else {
//				int[] first_suppleXX_array = { 8, 117, 117, 8 };// private
//																// static intnew
//																// int []
//				first_suppleX_array = first_suppleXX_array;
//				int[] first_suppleYY_array = { 11, 12, 122, 121 };
//				first_suppleY_array = first_suppleYY_array;
//				int[] second_suppleXX_array = { 11, 114, 114, 11 };
//				second_suppleX_array = second_suppleXX_array;
//				int[] second_suppleYY_array = { 5, 7, 117, 115 };
//				second_suppleY_array = second_suppleYY_array;
//				int[] barrackXX_array = { 11, 113, 113, 11 };
//				barrackX_array = barrackXX_array;
//				int[] barrackYY_array = { 9, 9, 119, 119 };
//				barrackY_array = barrackYY_array;
//				int[] factoryXX_array = { 11, 113, 113, 11 };
//				factoryX_array = factoryXX_array;
//				int[] factoryYY_array = { 12, 13, 123, 122 };
//				factoryY_array = factoryYY_array;
//				int[] bunkerXX_array = { 8, 117, 117, 8 };
//				bunkerX_array = bunkerXX_array;
//				int[] bunkerYY_array = { 9, 10, 120, 119 };
//				bunkerY_array = bunkerYY_array;
//			}
//		}
		
		
		
		
		


//		if(InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.THE_HUNTERS){
//			if(startingX == 10 && startingY == 6){
//				//11시부터 시계방향
//				starting_int = 0;
//			}else if(startingX == 70 && startingY == 8){
//				starting_int = 1;
//			}else if(startingX == 113 && startingY == 8){
//				starting_int = 2;
//			}else if(startingX == 114 && startingY == 80){
//				starting_int = 3;
//			}else if(startingX == 114 && startingY == 116){
//				starting_int = 4;
//			}else if(startingX == 63 && startingY == 117){
//				starting_int = 5;
//			}else if(startingX == 10 && startingY == 115){
//				starting_int = 6;
//			}else if(startingX == 8 && startingY == 47){
//				starting_int = 7;
//			}
//		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.LOST_TEMPLE){
//			if(startingX == 57 && startingY == 6){
//				//11시부터 시계방향
//				starting_int = 0;
//			}else if(startingX == 117 && startingY == 27){
//				starting_int = 1;
//			}else if(startingX == 27 && startingY == 118){
//				starting_int = 2;
//			}else if(startingX == 7&& startingY == 87){
//				starting_int = 3;
//			}
//		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.FIGHTING_SPRIRITS){
//			if(startingX == 7 && startingY == 6){
//				//11시부터 시계방향
//				starting_int = 0;
//			}else if(startingX == 117 && startingY == 7){
//				starting_int = 1;
//			}else if(startingX == 117 && startingY == 117){
//				starting_int = 2;
//			}else if(startingX == 7 && startingY == 116){
//				starting_int = 3;
//			}
//		}
//		first_suppleX = first_suppleX_array[starting_int];
//		first_suppleY = first_suppleY_array[starting_int];
//		second_suppleX = second_suppleX_array[starting_int];
//		second_suppleY = second_suppleY_array[starting_int];
//		//suppleX = 102;
//		//suppleY = 61;
//		barrackX = barrackX_array[starting_int];
//		barrackY = barrackY_array[starting_int];
//		factoryX = factoryX_array[starting_int];
//		factoryY = factoryY_array[starting_int];
//		bunkerX = bunkerX_array[starting_int];
//		bunkerY = bunkerY_array[starting_int];
//
//		//avoid supply 설정
//		ConstructionPlaceFinder.Instance().setTilesToAvoidSupply();
//
//	}
//
//	public void CheckBlockingPosition() {
//		if(InformationManager.Instance().enemyRace == Race.Protoss || InformationManager.Instance().enemyRace == Race.Terran){
//			for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//				if(unit.getType() == UnitType.Terran_Supply_Depot){
//					build_first_suppleX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
//					build_first_suppleY = unit.getTilePosition().getY();
//					if(first_suppleX != build_first_suppleX || first_suppleY != build_first_suppleY){
//						blockingEntranceNow = false;
//					}
//				}
//				if(unit.getType() == UnitType.Terran_Barracks){
//					build_barrackX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
//					build_barrackY = unit.getTilePosition().getY();
//					if(barrackX != build_barrackX || barrackY != build_barrackY){
//						blockingEntranceNow = false;
//					}
//				}
//			}
//		}
//	}
//
//
//	public final TilePosition getSupplyPosition(TilePosition tilepos)
//	{
//		int startingX = tilepos.getX();
//		int startingY = tilepos.getY();
//		int starting_int =10000;
//		if(InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.THE_HUNTERS){
//			if(startingX == 10 && startingY == 6){
//				//11시부터 시계방향
//				starting_int = 0;
//			}else if(startingX == 70 && startingY == 8){
//				starting_int = 1;
//			}else if(startingX == 113 && startingY == 8){
//				starting_int = 2;
//			}else if(startingX == 114 && startingY == 80){
//				starting_int = 3;
//			}else if(startingX == 114 && startingY == 116){
//				starting_int = 4;
//			}else if(startingX == 63 && startingY == 117){
//				starting_int = 5;
//			}else if(startingX == 10 && startingY == 115){
//				starting_int = 6;
//			}else if(startingX == 8 && startingY == 47){
//				starting_int = 7;
//			}
//		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.LOST_TEMPLE){
//			if(startingX == 57 && startingY == 6){
//				//11시부터 시계방향
//				starting_int = 0;
//			}else if(startingX == 117 && startingY == 27){
//				starting_int = 1;
//			}else if(startingX == 27 && startingY == 118){
//				starting_int = 2;
//			}else if(startingX == 7&& startingY == 87){
//				starting_int = 3;
//			}
//		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.FIGHTING_SPRIRITS){
//			if(startingX == 7 && startingY == 6){
//				//11시부터 시계방향
//				starting_int = 0;
//			}else if(startingX == 117 && startingY == 7){
//				starting_int = 1;
//			}else if(startingX == 117 && startingY == 117){
//				starting_int = 2;
//			}else if(startingX == 7 && startingY == 116){
//				starting_int = 3;
//			}
//		}
//
//		if(starting_int==10000){
//			return null;
//		}else{
//		//다음 포지션으ㅢ
//			TilePosition supply_pos= new TilePosition(fix_supplyX[starting_int], fix_supplyY[starting_int]);
//			return supply_pos;
//		}
//	}
//
//	public final TilePosition getSupplyPosition()
//	{
//			TilePosition supply_pos= new TilePosition(fix_supplyX[starting_int], fix_supplyY[starting_int]);
//			return supply_pos;
//	}
//
////	public void ReturnBuildSpacing() {
////		Config.BuildingSpacing = BuildingSpacingOld;
////		Config.BuildingResourceDepotSpacing = BuildingResourceDepotSpacingOld;
////
////	}
//
//	public int getStartingInt(){
//		return starting_int;
//	}
