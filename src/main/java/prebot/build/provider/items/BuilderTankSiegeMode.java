package prebot.build.provider.items;

import prebot.build.MetaType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;

public class BuilderTankSiegeMode extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderTankSiegeMode(MetaType metaType, ResearchSelector researchSelector){
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
