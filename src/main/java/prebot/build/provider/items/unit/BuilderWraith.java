package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.build.provider.StarportUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

public class BuilderWraith extends DefaultBuildableItem {

	StarportUnitSelector starportUnitSelector;

    public BuilderWraith(MetaType metaType, StarportUnitSelector starportUnitSelector){
        super(metaType);
        this.starportUnitSelector = starportUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
    		return false;
    	}

    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith) < 11) {
    		if(Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith) < 5)
    		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Wraith, null) == 0) {
    			return true;
    		}
    	}

    	
    	
//        if(starportUnitSelector.getSelected().equals(metaType.getUnitType())) {
//            return true;
//        }else{
//		List<Unit> starport = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Starport);
//		if (BuildConditionChecker.Instance().starportUnit.isTraining() == false && (Prebot.Broodwar.getFrameCount() - BuildConditionChecker.Instance().WraithTime > 2400 || BuildConditionChecker.Instance().wraithcnt < 1)) { // TODO && wraithcnt <= needwraith){
//			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Wraith, null) == 0) {
//				BuildConditionChecker.Instance().WraithTime = Prebot.Broodwar.getFrameCount();
//				BuildConditionChecker.Instance().nomorewraithcnt++;
//				return true;
////						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Wraith, false);
//				
//			}
//		}

    	return false;

    }
}
