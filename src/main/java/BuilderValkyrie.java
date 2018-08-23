

import bwapi.UnitType;

public class BuilderValkyrie extends DefaultBuildableItem {

	public BuilderValkyrie(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
			return false;
		}
		
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Control_Tower) == 0) {
			return false;
		}
		
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Armory) == 0) {
			return false;
		}
		
		int maxValkyrieCnt = StrategyIdea.valkyrieCount; 
		
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Valkyrie) < maxValkyrieCnt) {
			// if(Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith) < 5)
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Valkyrie, null) == 0) {
				if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Valkyrie)) {
					setHighPriority(true);
				}
				return true;
			}
		}
		
		return false;
		
		
	}
}

