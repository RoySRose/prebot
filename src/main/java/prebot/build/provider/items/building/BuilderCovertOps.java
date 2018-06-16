package prebot.build.provider.items.building;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderCovertOps extends DefaultBuildableItem {

    public BuilderCovertOps(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("CovertOps build condition check");
        return true;
    }


}
