

import bwapi.UnitType;

public class BuilderWraith extends DefaultBuildableItem {

	StarportUnitSelector starportUnitSelector;

    public BuilderWraith(MetaType metaType){
        super(metaType);
//        this.starportUnitSelector = starportUnitSelector;
    }

    public final boolean buildCondition(){
    	
    	int maxWraithCnt = StrategyIdea.wraithCount;
    	
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
			return false;
		}

    	
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith) < maxWraithCnt) {
			// if(Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith) < 5)
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Wraith, null) == 0) {
				if (!UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Wraith)) {
					setHighPriority(true);
				}
				return true;
			}
		}

    	
    	
//        if(starportUnitSelector.getSelected().equals(metaType.getUnitType())) {
//            return true;
//        }else{
//		List<Unit> starport = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Starport);
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
