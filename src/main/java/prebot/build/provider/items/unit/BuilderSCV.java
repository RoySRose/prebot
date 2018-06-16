package prebot.build.provider.items.unit;

import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;

public class BuilderSCV extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderSCV(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
            return false;
        }
    }
}
