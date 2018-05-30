package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.strategy.UnitInfo;

public abstract class Control {
	public abstract void control(List<Unit> unitList, List<UnitInfo> euiList);
}
