package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import bwapi.UnitType;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.squad.Squads;
import prebot.build.BuildOrderItem;
import prebot.build.MetaType;
import prebot.build.BuildOrderItem.SeedPositionStrategy;

public class InitialStrategy extends Strategy {

	public class InitialBuildBuilder {
		
		private List<BuildAction> initialBuildActionList = new ArrayList<>();
		
		public InitialBuildBuilder add(UnitType unitType, boolean blocking) {
			return add(unitType, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, blocking);
		}

		public InitialBuildBuilder add(UnitType unitType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy, boolean blocking) {
			initialBuildActionList.add(newBuildAction(new MetaType(unitType), seedPositionStrategy, blocking));
			return this;
		}
		
		public InitialBuildBuilder times(int times) {
			BuildOrderItem lastBuildOrderItem = initialBuildActionList.get(initialBuildActionList.size() - 1).buildOrderItem;
			for (int i = 0; i < times - 1; i++) {
				initialBuildActionList.add(newBuildAction(lastBuildOrderItem.metaType, lastBuildOrderItem.seedLocationStrategy, lastBuildOrderItem.blocking));
			}
			return this;
		}
		
		public List<BuildAction> build() {
			return initialBuildActionList;
		}
		
		private BuildAction newBuildAction(MetaType metaType, SeedPositionStrategy seedLocationStrategy, boolean blocking) {
			BuildOrderItem buildOrderItem = new BuildOrderItem(metaType, seedLocationStrategy, 0, blocking, -1);
			return new BuildAction(buildOrderItem) {
				@Override
				public boolean buildCondition() {
					return true;
				}
			};
		}
	}
	
	public void setUpDefaultInitialStrategy() {
		squadList.add(new Squads.IdleSquad());
		squadList.add(new Squads.MainSquad());
	}
	
}
