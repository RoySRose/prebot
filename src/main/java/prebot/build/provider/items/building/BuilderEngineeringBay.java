package prebot.build.provider.items.building;

import bwapi.Race;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;

public class BuilderEngineeringBay extends DefaultBuildableItem {

    public BuilderEngineeringBay(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("EngineeringBay build condition check");
        
        if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay) && (InformationManager.Instance().enemyRace != Race.Protoss || Prebot.Broodwar.getFrameCount() > 5000)){
			
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
				//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				return true;
			}
		}
        
        if(RespondToStrategy.Instance().enemy_dark_templar 
        		|| RespondToStrategy.Instance().enemy_wraith 
        		|| RespondToStrategy.Instance().enemy_lurker 
        		|| RespondToStrategy.Instance().enemy_shuttle){
			return true;
		}
        
        
        return false;
    }


}
