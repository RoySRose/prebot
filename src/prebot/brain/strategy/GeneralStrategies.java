package prebot.brain.strategy;

import bwapi.Race;

public class GeneralStrategies {
	
	public static final class GeneralStrategyForProtoss extends GeneralStrategy {
		public GeneralStrategyForProtoss() {
			buildActionList = getDefaultBuildActionList();
			squadList = getDefaultSquadList();
			actionList = getDefaultActionList();
			
			// TODO add protoss specific options
		}
	}
	
	public static final class GeneralStrategyForZerg extends GeneralStrategy {
		public GeneralStrategyForZerg() {
			buildActionList = getDefaultBuildActionList();
			squadList = getDefaultSquadList();
			actionList = getDefaultActionList();
		}
	}
	
	public static final class GeneralStrategyForTerran extends GeneralStrategy {
		public GeneralStrategyForTerran() {
			buildActionList = getDefaultBuildActionList();
			squadList = getDefaultSquadList();
			actionList = getDefaultActionList();
		}
	}
	
	public static Strategy generalStrategy(Race race) {
		if (race == Race.Protoss) {
			return new GeneralStrategyForProtoss();
		} else if (race == Race.Zerg) {
			return new GeneralStrategyForZerg();
		} else if (race == Race.Terran) {
			return new GeneralStrategyForTerran();
		} else {
			return new GeneralStrategyForZerg();
		}
	}

}
