package prebot.build.provider.items.tech;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;
import prebot.common.main.MyBotModule;

public class BuilderIonThrusters extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderIonThrusters(MetaType metaType, ResearchSelector researchSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){
    	
        //if(researchSelector.getSelected().getUpgradeType().equals(metaType.getUpgradeType())) {
    	
		if (researchSelector.getSelected() == null) {
			return false;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) != 0) {
			return false;
		}
		if (!researchSelector.getSelected().isUpgrade()) {
			return false;
		}
		if (researchSelector.getSelected().getUpgradeType() != metaType.getUpgradeType()) {
			return false;
		}
		if (MyBotModule.Broodwar.self().isUpgrading(researchSelector.getSelected().getUpgradeType())) {
			return false;
		}

    	if (researchSelector.currentResearched <= 2) {
    		setBlocking(true);
    		setHighPriority(true);
    	}
        return true;
    }
}
