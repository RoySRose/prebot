package prebot.build.provider.items.building;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderBarracks extends DefaultBuildableItem {

    public BuilderBarracks(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        //System.out.println("barracks build condition check");
        return true;
    }


}
