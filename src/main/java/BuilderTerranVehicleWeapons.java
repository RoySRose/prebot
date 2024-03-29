

import bwapi.UnitType;
import bwapi.UpgradeType;

public class BuilderTerranVehicleWeapons extends DefaultBuildableItem {

    //ResearchSelector researchSelector;
	UpgradeSelector upgradeSelector;

    public BuilderTerranVehicleWeapons(MetaType metaType, UpgradeSelector upgradeSelector){
        super(metaType);
        this.upgradeSelector = upgradeSelector;
    }

    public final boolean buildCondition(){
    	
    	if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Armory) == 0) {
    		return false;
    	}
		if (upgradeSelector.getSelected() == null) {
			return false;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(upgradeSelector.getSelected(), null) != 0) {
			return false;
		}
		if (!upgradeSelector.getSelected().isUpgrade()) {
			return false;
		}
		if (upgradeSelector.getSelected().getUpgradeType() != metaType.getUpgradeType()) {
			return false;
		}
		if (MyBotModule.Broodwar.self().isUpgrading(upgradeSelector.getSelected().getUpgradeType())) {
			return false;
		}
    	
		if (upgradeSelector.getSelected().equals(metaType)) {
			BuildQueueProvider.Instance().startUpgrade(UpgradeType.Terran_Vehicle_Weapons);
			return true;
		} else {
			return false;
		}
    }
}
