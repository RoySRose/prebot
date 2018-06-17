package prebot.build.provider.items.upgrade;

import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.build.provider.UpgradeSelector;
import prebot.common.MetaType;

public class BuilderU238Shells extends DefaultBuildableItem {

    //ResearchSelector researchSelector;
	UpgradeSelector upgradeSelector;

    public BuilderU238Shells(MetaType metaType, UpgradeSelector upgradeSelector){
        super(metaType);
        this.upgradeSelector = upgradeSelector;
    }

    public final boolean buildCondition(){

        if(upgradeSelector.getSelected().equals(metaType)) {
            return true;
        }else{
            return false;
        }
    }
}
