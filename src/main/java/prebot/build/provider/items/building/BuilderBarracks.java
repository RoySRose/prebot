package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

public class BuilderBarracks extends DefaultBuildableItem {

    public BuilderBarracks(MetaType metaType){
        super(metaType);
    }
    
    public final boolean buildCondition(){
    	int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks);
    	if (buildQueueCount > 0) {
    		return false;
    	}
    	int constructionQueueCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null);
    	if (constructionQueueCount > 0) {
			return false;
		}
    	
    	setRecoverItemCount(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks));
		
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks) == 0) {
			return true;

		} else if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks) == 1) {
			List<Unit> barracksList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Barracks);

			for (Unit unit : barracksList) {
				if (unit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints() / 3) {
					return true;
				}
			}
		}

		return false;
    }

}
