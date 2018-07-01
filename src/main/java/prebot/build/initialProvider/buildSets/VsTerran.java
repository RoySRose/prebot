package prebot.build.initialProvider.buildSets;


import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;

/// 봇 프로그램 설정
public class VsTerran extends BaseBuild{


    public VsTerran(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos, TilePosition bunkerPos) {
    	
    	/*2스타 레이스*/
    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
    	queueBuild(true, UnitType.Terran_Supply_Depot, firstSupplyPos);
    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
    	queueBuild(true, UnitType.Terran_Barracks, barrackPos);
    	queueBuild(true, UnitType.Terran_Refinery);
    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
    	queueBuild(true, UnitType.Terran_Factory, factoryPos);
    	queueBuild(true, UnitType.Terran_Marine);
    	queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
    	queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
    	queueBuild(true, UnitType.Terran_Vulture);
    	queueBuild(true, UnitType.Terran_Starport);
    	queueBuild(true, UnitType.Terran_Starport);
    	
    	
        /*queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Supply_Depot, firstSupplyPos);
        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Barracks, barrackPos);
        queueBuild(true, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Refinery);
        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(false, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Marine);
        queueBuild(true, UnitType.Terran_Factory, factoryPos);
        queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_SCV);
        queueBuild(false, UnitType.Terran_SCV);
        queueBuild(false, UnitType.Terran_Machine_Shop);
        queueBuild(true, UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
        queueBuild(false, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_Vulture);
        queueBuild(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
        queueBuild(true, UnitType.Terran_Starport, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);*/

    }


}