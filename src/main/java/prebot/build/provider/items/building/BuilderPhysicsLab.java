package prebot.build.provider.items.building;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderPhysicsLab extends DefaultBuildableItem {

    public BuilderPhysicsLab(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//        System.out.println("PhysicsLab build condition check");
        return false;
    }


}
