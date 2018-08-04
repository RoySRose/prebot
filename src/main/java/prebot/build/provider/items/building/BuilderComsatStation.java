package prebot.build.provider.items.building;

import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
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
    	
    	
    	
    	List<Unit> commandCenters = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
    	
    	for(BaseLocation baseLocation : BWTA.getBaseLocations())
		{
    		for (Unit commandCenter : commandCenters) {
    			if (commandCenter.canBuildAddon() && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4
    				&& 
    				(	baseLocation.getTilePosition().getX() == commandCenter.getTilePosition().getX()
    					&& baseLocation.getTilePosition().getY() == commandCenter.getTilePosition().getY()
    				)
				) {
    				FileUtils.appendTextToFile("log.txt", "\n baseLocation ==> " + baseLocation.getTilePosition());
    				FileUtils.appendTextToFile("log.txt", "\n commandCenter ==> " + commandCenter.getTilePosition());
    				if(baseLocation.getTilePosition() == commandCenter.getTilePosition()) {
    					FileUtils.appendTextToFile("log.txt", "\n TilePosition 만으로 비교해도 위치 일치함");
    				}

    				if (Prebot.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
    						&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
    					setBlocking(true);
    					setHighPriority(true);
    					return true;
    				}
    			}
    		}
		}
		
    	
        
        return false;
    }

}
