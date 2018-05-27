package prebot.build.provider.items;

import bwapi.UnitType;
import prebot.build.MetaType;
import prebot.build.provider.DefaultBuildableItem;

public class BuilderBarrack extends DefaultBuildableItem {

    public BuilderBarrack(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("barrack build condition check");
        return true;
    }


}
