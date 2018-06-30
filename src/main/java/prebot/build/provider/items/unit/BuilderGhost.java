package prebot.build.provider.items.unit;

import prebot.build.provider.BarrackUnitSelector;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;

public class BuilderGhost extends DefaultBuildableItem {

	BarrackUnitSelector barrackUnitSelector;

    public BuilderGhost(MetaType metaType, BarrackUnitSelector barrackUnitSelector){
        super(metaType);
        this.barrackUnitSelector = barrackUnitSelector;
    }

    public final boolean buildCondition(){

        if(barrackUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
        	if(BuildQueueProvider.Instance().respondSet) {
        		return false;
        	}else {
	            return false;
	        }
    	}
    }
}
