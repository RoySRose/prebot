public class BuilderCloakingField extends DefaultBuildableItem {
	//레이스 클로킹

    public BuilderCloakingField(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
//    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
//			return false;
//		}
//    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Control_Tower) == 0) {
//			return false;
//		}
//		if (BuildManager.Instance().buildQueue.getItemCount(TechType.Cloaking_Field) > 0) {
//			return false;
//		}
//		if (Prebot.Broodwar.self().hasResearched(TechType.Cloaking_Field) || Prebot.Broodwar.self().isResearching(TechType.Cloaking_Field)) {
//			return false;
//		}
//    	
//		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Wraith) >= 4 && StrategyIdea.wraithCount >= 6) {
//			return true;
//		}
//    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith) + Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith) > 4) {
//    		return true;
//    	}
    	
    	return false;

    }
}
