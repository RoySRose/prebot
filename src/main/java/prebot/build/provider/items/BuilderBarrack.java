package prebot.build.provider.items;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderBarrack extends DefaultBuildableItem {

    public BuilderBarrack(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("barrack build condition check");
        return true;
    }


}
