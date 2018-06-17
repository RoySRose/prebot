package prebot.build.provider.items.upgrade;

import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.build.provider.UpgradeSelector;
import prebot.common.MetaType;

public class BuilderIonThrusters extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderIonThrusters(MetaType metaType, UpgradeSelector upgradeSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){

        if(researchSelector.getSelected().equals(metaType)) {
            return true;
        }else{
            return false;
        }
    }
}
