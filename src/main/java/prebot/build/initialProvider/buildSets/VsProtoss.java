package prebot.build.initialProvider.buildSets;


import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.prebot1.BuildOrderItem;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation.GameMap;

/// 봇 프로그램 설정
public class VsProtoss extends BaseBuild{


    public VsProtoss(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos, TilePosition bunkerPos) {
    	
    	if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.UNKNOWN) {

	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Supply_Depot, firstSupplyPos);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Barracks, barrackPos);
	        queueBuild(true, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Refinery);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Marine);
	        queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
	        queueBuild(true, UnitType.Terran_Factory, factoryPos);
	        queueBuild(true, UnitType.Terran_Factory);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Vulture);
	        queueBuild(true, UnitType.Terran_Machine_Shop);
	        queueBuild(true, UnitType.Terran_SCV);
	        
    	}else {
    		queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Supply_Depot);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Barracks);
	        queueBuild(true, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Refinery);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Marine);
	        queueBuild(true, UnitType.Terran_Supply_Depot);
	        queueBuild(true, UnitType.Terran_Factory);
	        queueBuild(true, UnitType.Terran_Factory);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Vulture);
	        queueBuild(true, UnitType.Terran_Machine_Shop);
	        queueBuild(true, UnitType.Terran_SCV);
    	}
    }
  
}