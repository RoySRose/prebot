

import java.util.HashSet;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class BuilderComsatStation extends DefaultBuildableItem {

    public BuilderComsatStation(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0) {
    		return false;
    	}

    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) != 0) {
			return false;
		}
    	
    	if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) > 4) {
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
