package prebot.build.initialProvider.buildSets;


import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.strategy.InformationManager;

/// 봇 프로그램 설정
public class VsProtoss extends BaseBuild{


    public VsProtoss(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos, TilePosition bunkerPos) {

        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Supply_Depot, firstSupplyPos);
        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Barracks, barrackPos);
        queueBuild(true, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Refinery);
        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(false, UnitType.Terran_SCV);
        queueBuild(false, UnitType.Terran_Marine);
        queueBuild(false, UnitType.Terran_Supply_Depot, secondSupplyPos);
        queueBuild(false, UnitType.Terran_Factory, factoryPos);
        queueBuild(false, UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_Machine_Shop);
        queueBuild(false, UnitType.Terran_SCV);
    }

}