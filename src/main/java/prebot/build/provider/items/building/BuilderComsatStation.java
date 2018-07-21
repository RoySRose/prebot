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

public class BuilderComsatStation extends DefaultBuildableItem {

    public BuilderComsatStation(MetaType metaType){
        super(metaType);
    }
    
    public boolean EXOK = false;

    public final boolean buildCondition(){
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0) {
    		return false;
    	}

    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) != 0) {
			return false;
		}
    	
    	List<Unit> commandCenters = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
    	for (Unit commandCenter : commandCenters) {
			if (commandCenter.canBuildAddon() && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4) {
				if (Prebot.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
						&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
					setBlocking(true);
					setHighPriority(true);
					return true;
				}
			}
		}
        
        return false;
    }

}
