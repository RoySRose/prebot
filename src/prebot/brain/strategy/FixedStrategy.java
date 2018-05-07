package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.BuildOrderItem;
import prebot.build.BuildOrderItem.SeedPositionStrategy;
import prebot.build.MetaType;

public class FixedStrategy extends Strategy {
	
	protected List<BuildOrderItem> buildItemList = new ArrayList<>();
	
	protected class FixedBuildBuilder {
		protected FixedBuildBuilder add(UnitType unitType, boolean blocking) {
			return add(new BuildOrderItem(new MetaType(unitType), SeedPositionStrategy.MainBaseLocation, 0, blocking, -1));
		}
		
		protected FixedBuildBuilder add(UnitType unitType, boolean blocking, TilePosition tilePosition) {
			if (tilePosition == null) {
				return add(unitType, blocking);
			} else {
				return add(new BuildOrderItem(new MetaType(unitType), tilePosition, 0, blocking, -1));
			}
		}
		
		protected FixedBuildBuilder add(TechType techType, boolean blocking) {
			add(new BuildOrderItem(new MetaType(techType), SeedPositionStrategy.MainBaseLocation, 0, blocking, -1));
			return this;
		}

		protected FixedBuildBuilder add(BuildOrderItem buildOrderItem) {
			buildItemList.add(buildOrderItem);
			return this;
		}
		
		protected FixedBuildBuilder times(int times) {
			BuildOrderItem lastBuildOrderItem = buildItemList.get(buildItemList.size() - 1);
			for (int i = 0; i < times - 1; i++) {
				buildItemList.add(new BuildOrderItem(lastBuildOrderItem.metaType, lastBuildOrderItem.seedLocationStrategy, lastBuildOrderItem.priority, lastBuildOrderItem.blocking, -1));
			}
			return this;
		}
		
		protected List<BuildOrderItem> build() {
			return buildItemList;
		}
	}
}
