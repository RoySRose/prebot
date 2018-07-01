package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyManager;
import prebot.strategy.TempBuildSourceCode;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuilderBunker extends DefaultBuildableItem {

    public BuilderBunker(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	if(BuildQueueProvider.Instance().respondSet) {
    		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH){
    			if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) >= 1){
    				if(RespondToStrategy.Instance().center_gateway){
    					if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
    						if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
    								&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
    								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){
    							
    							setBlocking(true);
    							setHighPriority(true);
    							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
    							return true;

    							// BlockingEntrance Prebot2버전을 사용하도록 하면서 주석처리
//    							TilePosition bunkerPos = new TilePosition(BlockingEntrance.Instance().bunkerX,BlockingEntrance.Instance().bunkerY);
//    							ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
//    							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker, bunkerPos,true);

    						}
    					}
    				}else{
    					if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) >= 1) {
    						if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
    							if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
    									&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
    									&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){
//    								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
    								setBlocking(true);
        							setHighPriority(true);
        							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
        							return true;
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	return false;
    }


}
