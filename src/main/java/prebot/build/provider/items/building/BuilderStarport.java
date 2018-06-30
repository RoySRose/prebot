package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.RespondToStrategy;

public class BuilderStarport extends DefaultBuildableItem {

    public BuilderStarport(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("Starport build condition check");
        int wraithcnt = 0;
    	int nomorewraithcnt = 0;
    	int cc = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center);
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {
	        if (RespondToStrategy.Instance().max_wraith > wraithcnt && nomorewraithcnt <= RespondToStrategy.Instance().max_wraith) {
	
				if (cc >= 2) {
					if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Starport)) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
	//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
							return true;
						}
					}
				}
				
			}
	        
	        if ((RespondToStrategy.Instance().need_vessel == true && BuildConditionChecker.Instance().CC >= 2) || BuildConditionChecker.Instance().CC >= 3) {
				if (BuildConditionChecker.Instance().star == false) {
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null) == 0) {
	//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, false);
						return true;
					}
				}
			}
    	}
        
        
        return false;
    }


}
