package prebot.brain.strategy;

import bwapi.UnitType;
import prebot.brain.knowledge.inerrable.InerrableEssentials;
import prebot.brain.knowledge.inerrable.InerrableInitials;
import prebot.brain.squad.DefaultSquads;
import prebot.brain.squad.NewSquads;

public class FixedStrategies {

	public static final class InitialStrategyForProtoss extends FixedStrategy {
		public InitialStrategyForProtoss() {
			buildItemList = new FixedBuildBuilder().
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
			
			actionList.add(new InerrableInitials.MechanicGasAdjustment());
			actionList.add(new InerrableInitials.ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			actionList.add(new InerrableEssentials.UsePrebot1BuildStrategy());
			
			squadList.add(new DefaultSquads.IdleSquad());
			squadList.add(new DefaultSquads.MainDefenseSquad());
			squadList.add(new DefaultSquads.WatcherSquad());
			squadList.add(new DefaultSquads.CheckerSquad());
			squadList.add(new NewSquads.ScvScoutSquad());
		}
	}
	
	public static final class InitialStrategyForZerg extends FixedStrategy {
		public InitialStrategyForZerg() {
			// TODO initial build for zerg
		}
	}
	
	public static final class InitialStrategyForTerran extends FixedStrategy {
		public InitialStrategyForTerran() {
			// TODO initial build for terran
		}
	}
	
	public static final class InitialStrategyForRandom extends FixedStrategy {
		public InitialStrategyForRandom() {
			buildItemList = new FixedBuildBuilder().
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
			
			actionList.add(new InerrableInitials.MechanicGasAdjustment());
			actionList.add(new InerrableInitials.ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			actionList.add(new InerrableEssentials.UsePrebot1BuildStrategy());
			
			squadList.add(new DefaultSquads.IdleSquad());
			squadList.add(new DefaultSquads.MainDefenseSquad());
			squadList.add(new DefaultSquads.WatcherSquad());
			squadList.add(new DefaultSquads.CheckerSquad());
		}
	}
}
