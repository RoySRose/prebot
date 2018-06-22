package prebot.build.provider;

import bwapi.TilePosition;
import prebot.build.prebot1.BuildOrderItem;
import prebot.common.util.FileUtils;
//import prebot.common.util.*;

public class BuildCondition {

    public boolean blocking=false;
    public boolean highPriority=false;
    public BuildOrderItem.SeedPositionStrategy seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.NoLocation;
    public TilePosition tilePosition = TilePosition.None;
    
    

    public BuildCondition(){
    	/*this.blocking = false;
        this.highPriority = false;
        this.seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.NoLocation;
        this.tilePosition = TilePosition.None;*/
        //FileUtils.appendTextToFile("log.txt", "\n test log of BuildCondition ==>>> " + tilePosition.toString());
    }

    public BuildCondition(boolean blocking, boolean highPriority, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, TilePosition tilePostition) {
        this.blocking = blocking;
        this.highPriority = highPriority;
        this.seedPositionStrategy = seedPositionStrategy;
        this.tilePosition = tilePostition;
    }


}
