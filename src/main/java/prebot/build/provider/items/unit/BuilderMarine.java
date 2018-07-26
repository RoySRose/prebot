package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

public class BuilderMarine extends DefaultBuildableItem {

    public BuilderMarine(MetaType metaType){
        super(metaType);
    }
    
    public boolean liftChecker = false;

    public final boolean buildCondition(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
    	
    	List<Unit> barracks = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Barracks);
		for(Unit unit : barracks) {
			if(!unit.isLifted()) {
				liftChecker = true;
			}
		}

    	if(liftChecker) {
    		return false;
    	}
//    	liftChecker = false;
    	
    	

       
    	return false;

    }
}
