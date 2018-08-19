package prebot.build.provider.items.building;

import java.util.HashSet;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.BaseLocationUtils;
import prebot.common.util.PlayerUtils;
import prebot.common.util.UnitUtils;

public class BuilderComsatStation extends DefaultBuildableItem {

    public BuilderComsatStation(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0) {
    		return false;
    	}

    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) != 0) {
			return false;
		}
    	
    	if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) > 4) {
    		return false;
    	}
    	
    	if (!PlayerUtils.enoughResource(UnitType.Terran_Comsat_Station)) {
    		return false;
    	}
    	
    	HashSet<TilePosition> baseLocationTiles = BaseLocationUtils.getBaseLocationTileHashSet();
		List<Unit> commandCenters = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		for (Unit commandCenter : commandCenters) {
			if (!commandCenter.canBuildAddon()) {
				continue;
			}
			if (baseLocationTiles.contains(commandCenter.getTilePosition())) {
				setBlocking(true);
				setHighPriority(true);
				return true;
			}
		}
		
        return false;
    }

}
