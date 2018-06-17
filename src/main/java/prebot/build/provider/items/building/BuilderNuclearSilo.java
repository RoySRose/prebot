package prebot.build.provider.items.building;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderNuclearSilo extends DefaultBuildableItem {

    public BuilderNuclearSilo(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("NuclearSilo build condition check");
        return true;
    }


}
