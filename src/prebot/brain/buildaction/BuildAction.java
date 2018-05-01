package prebot.brain.buildaction;

import bwapi.UnitType;
import prebot.build.BuildOrderItem;

public abstract class BuildAction {
	public BuildOrderItem buildOrderItem;
	public BuildAction(BuildOrderItem buildOrderItem) {
		this.buildOrderItem = buildOrderItem;
	}
	public UnitType getBuildType() {
		return buildOrderItem.metaType.getUnitType();
	}
	public abstract boolean buildCondition();
}
