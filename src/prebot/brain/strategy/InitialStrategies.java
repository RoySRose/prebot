package prebot.brain.strategy;

import bwapi.Race;
import bwapi.UnitType;

public class InitialStrategies extends InitialStrategy {

	public static final class InitialStrategyForProtoss extends InitialStrategy {
		public InitialStrategyForProtoss() {
			buildActionList = new InitialBuildBuilder().
					add(UnitType.Terran_SCV).times(6).
					add(UnitType.Terran_Supply_Depot).
					add(UnitType.Terran_SCV).
					add(UnitType.Terran_Barracks).
					add(UnitType.Terran_SCV).times(2).
					add(UnitType.Terran_Marine).times(2).build();
			
			squadList = getDefaultSquadList();
			actionList = getDefaultActionList();
		}
	}
	
	public static final class InitialStrategyForZerg extends InitialStrategy {
		public InitialStrategyForZerg() {
			// TODO initial build for zerg
			
			squadList = getDefaultSquadList();
			actionList = getDefaultActionList();
		}
	}
	
	public static final class InitialStrategyForTerran extends InitialStrategy {
		public InitialStrategyForTerran() {
			// TODO initial build for terran
			
			squadList = getDefaultSquadList();
			actionList = getDefaultActionList();
		}
	}
	
	public static Strategy initialStrategy(Race race) {
		if (race == Race.Protoss) {
			return new InitialStrategyForProtoss();
		} else if (race == Race.Zerg) {
			return new InitialStrategyForZerg();
		} else if (race == Race.Terran) {
			return new InitialStrategyForTerran();
		} else {
			return new InitialStrategyForZerg();
		}
	}
	
}
