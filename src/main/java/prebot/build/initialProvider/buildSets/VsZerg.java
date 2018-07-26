package prebot.build.initialProvider.buildSets;


import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation.GameMap;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/// 봇 프로그램 설정
public class VsZerg extends BaseBuild{


    public VsZerg(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos, TilePosition bunkerPos) {
  	
    	if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.UNKNOWN) {
    	
	    	/*2스타 레이스*/
	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	    	queueBuild(true, UnitType.Terran_Barracks, barrackPos);
	    	queueBuild(true, UnitType.Terran_Supply_Depot, firstSupplyPos);
	    	queueBuild(true, UnitType.Terran_SCV);
	    	queueBuild(true, UnitType.Terran_Refinery);
	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
	    	ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
	    	queueBuild(true, UnitType.Terran_Bunker, bunkerPos);
	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);

	    	queueBuild(true, UnitType.Terran_Factory, factoryPos);
//	    	queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
//	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//	    	queueBuild(true, UnitType.Terran_Vulture);
//	    	queueBuild(true, UnitType.Terran_Starport);
//	    	queueBuild(true, UnitType.Terran_Starport);

    	}else {
    		
    		/*2스타 레이스*/
    		queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	    	queueBuild(true, UnitType.Terran_Barracks, barrackPos);
	    	queueBuild(true, UnitType.Terran_Supply_Depot, firstSupplyPos);
	    	queueBuild(true, UnitType.Terran_SCV);
	    	queueBuild(true, UnitType.Terran_Refinery);
	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//	    	queueBuild(true, UnitType.Terran_Marine);
//	    	queueBuild(true, UnitType.Terran_SCV);
	    	ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
	    	queueBuild(true, UnitType.Terran_Bunker, bunkerPos);
	    	queueBuild(true, UnitType.Terran_Factory, factoryPos);
//	    	queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
//	    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
//	    	queueBuild(true, UnitType.Terran_Vulture);
//	    	queueBuild(true, UnitType.Terran_Starport);
//	    	queueBuild(true, UnitType.Terran_Starport);
    		
    	}
    }
    
}

