package prebot.build.initialProvider.buildSets;


import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.strategy.InformationManager;

/// 봇 프로그램 설정
public class VsZerg extends BaseBuild{


    public VsZerg(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos, TilePosition bunkerPos) {

        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(true,UnitType.Terran_Barracks, barrackPos);
        queueBuild(true,UnitType.Terran_Supply_Depot, firstSupplyPos);
        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV); //TODO 짧은 맵은 이거 한마리 날리면 산다.
        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
        ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
        queueBuild(true,UnitType.Terran_Bunker, bunkerPos);
        queueBuild(false, UnitType.Terran_SCV);
        queueBuild(true, UnitType.Terran_Marine);
        queueBuild(true, UnitType.Terran_Refinery);
        queueBuild(false, UnitType.Terran_Marine);
        queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
        queueBuild(true,UnitType.Terran_Factory, factoryPos);
        queueBuild(false,UnitType.Terran_Factory, factoryPos);
        queueBuild(true, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_Machine_Shop);
        //queueBuild(true, UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
        queueBuild(false, UnitType.Terran_Vulture);
        queueBuild(false, UnitType.Terran_Vulture);
        //queueBuild(true, UnitType.Terran_Factory);
        queueBuild(true, UnitType.Terran_Armory);
    }
}