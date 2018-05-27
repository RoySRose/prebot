package prebot.build.provider;

import bwapi.TilePosition;
import prebot.build.prebot1.BuildOrderItem;

public class BuildCondition {

    public boolean blocking=false;
    public boolean highPriority=false;
    public BuildOrderItem.SeedPositionStrategy seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.NoLocation;
    public TilePosition tilePostition = TilePosition.None;

    public BuildCondition(){
    }

    public BuildCondition(boolean blocking, boolean highPriority, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, TilePosition tilePostition) {
        this.blocking = blocking;
        this.highPriority = highPriority;
        this.seedPositionStrategy = seedPositionStrategy;
        this.tilePostition = tilePostition;
    }


}
