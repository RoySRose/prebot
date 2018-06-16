package prebot.build.provider.items.building;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderBunker extends DefaultBuildableItem {

    public BuilderBunker(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("Bunker build condition check");
        return true;
    }


}
