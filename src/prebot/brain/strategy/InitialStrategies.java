package prebot.brain.strategy;

import bwapi.Race;
import bwapi.UnitType;
import prebot.brain.action.Actions;

public class InitialStrategies {

	public static final class InitialStrategyForProtoss extends InitialStrategy {
		public InitialStrategyForProtoss() {
			setUpDefaultInitialStrategy();
			
			buildActionList = new InitialBuildBuilder().
					add(UnitType.Terran_SCV, false).times(8).
					add(UnitType.Terran_Supply_Depot, true).
					add(UnitType.Terran_SCV, false).times(2).
					add(UnitType.Terran_Barracks, true).
					add(UnitType.Terran_SCV, false).times(1).
					add(UnitType.Terran_Refinery, true).
					add(UnitType.Terran_SCV, false).times(3).
					add(UnitType.Terran_Marine, true).
					add(UnitType.Terran_Supply_Depot, true).
					add(UnitType.Terran_Factory, true).
					add(UnitType.Terran_SCV, true).times(3).
					add(UnitType.Terran_Vulture, true).
					add(UnitType.Terran_Machine_Shop, true).
					build();
			
			actionList.add(new Actions.MechanicTerranGasAdjustment());
			actionList.add(new Actions.WorkerScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
		}
	}
	
	public static final class InitialStrategyForZerg extends InitialStrategy {
		public InitialStrategyForZerg() {
			setUpDefaultInitialStrategy();
			
			// TODO initial build for zerg
		}
	}
	
	public static final class InitialStrategyForTerran extends InitialStrategy {
		public InitialStrategyForTerran() {
			setUpDefaultInitialStrategy();

			// TODO initial build for terran
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
