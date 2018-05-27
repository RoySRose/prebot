package prebot.build.provider.items;

import bwapi.UnitType;
import prebot.build.MetaType;
import prebot.build.provider.DefaultBuildableItem;

public class BuilderExample extends DefaultBuildableItem {

    public BuilderExample(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("example build condition check");
        return true;
    }


}
