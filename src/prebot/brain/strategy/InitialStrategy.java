package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import bwapi.UnitType;
import prebot.brain.buildaction.BuildAction;
import prebot.manager.build.BuildOrderItem;
import prebot.manager.build.MetaType;
import prebot.manager.build.BuildOrderItem.SeedPositionStrategy;

public class InitialStrategy extends Strategy {
	
	public class InitialBuildBuilder {
		
		private List<BuildAction> initialBuildActionList = new ArrayList<>();
		
		public InitialBuildBuilder add(UnitType unitType) {
			return add(unitType, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
		}

		public InitialBuildBuilder add(UnitType unitType, BuildOrderItem.SeedPositionStrategy seedPositionStrategy) {
			return add(unitType, seedPositionStrategy, true);
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

	
}
