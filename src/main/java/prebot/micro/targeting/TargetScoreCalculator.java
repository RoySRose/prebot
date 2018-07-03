package prebot.micro.targeting;

import bwapi.Unit;
import prebot.strategy.UnitInfo;

public abstract class TargetScoreCalculator {
	public abstract int calculate(Unit unit, UnitInfo eui);
}
