package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class BuilderEngineeringBay extends DefaultBuildableItem {

	public BuilderEngineeringBay(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Engineering_Bay)) {
			return false;
		}
		
		if (TimeUtils.after(StrategyIdea.engineeringBayBuildStartFrame)) {
			setBlocking(true);
			setHighPriority(true);
			return true;
		} else {
			return false;
		}
	}
}
