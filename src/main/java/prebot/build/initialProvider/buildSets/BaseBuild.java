package prebot.build.initialProvider.buildSets;


import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;

public class BaseBuild {

    public static void queueBuild(boolean blocking, UnitType... types) {
        BuildOrderQueue bq = BuildManager.Instance().buildQueue;
        BuildOrderItem.SeedPositionStrategy defaultSeedPosition = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
        for (UnitType type : types) {
            bq.queueAsLowestPriority(type, defaultSeedPosition, blocking);
        }
    }

    public void queueBuild(boolean blocking, UnitType type, BuildOrderItem.SeedPositionStrategy seedPosition) {
        BuildOrderQueue bq = BuildManager.Instance().buildQueue;
        bq.queueAsLowestPriority(type, seedPosition, blocking);
    }

    public void queueBuild(boolean blocking, UnitType type, TilePosition tilePosition) {
        BuildOrderQueue bq = BuildManager.Instance().buildQueue;
        bq.queueAsLowestPriority(type, tilePosition, blocking);
    }

    public void queueBuild(TechType type) {
        BuildOrderQueue bq = BuildManager.Instance().buildQueue;
        bq.queueAsLowestPriority(type);
    }

    public void queueBuild(UpgradeType type) {
        BuildOrderQueue bq = BuildManager.Instance().buildQueue;
        bq.queueAsLowestPriority(type);
    }
}
