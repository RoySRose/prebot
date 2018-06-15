package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.common.LagObserver;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;

public abstract class Control {
	public abstract void control(List<Unit> unitList, List<UnitInfo> euiList);
	
	protected boolean skipControl(Unit unit) {
		return !TimeUtils.executeUnitRotation(unit, LagObserver.groupsize());
	}
}
