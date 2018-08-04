package prebot.build.provider.items.upgrade;

import bwapi.UpgradeType;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.UpgradeSelector;
import prebot.common.MetaType;

public class BuilderTerranVehicleWeapons extends DefaultBuildableItem {

    //ResearchSelector researchSelector;
	UpgradeSelector upgradeSelector;

    public BuilderTerranVehicleWeapons(MetaType metaType, UpgradeSelector upgradeSelector){
        super(metaType);
        this.upgradeSelector = upgradeSelector;
    }

    public final boolean buildCondition(){
		if (upgradeSelector.getSelected().equals(metaType)) {
			BuildQueueProvider.Instance().startUpgrade(UpgradeType.Terran_Vehicle_Weapons);
			return true;
		} else {
			return false;
		}
    }
}
