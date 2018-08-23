

import bwapi.UnitType;

public class BarrackUnitSelector implements Selector<UnitType> {

	UnitType unitType;
	// BuildCondition buildCondition;

	public final UnitType getSelected() {
		return unitType;
	}

	public boolean block;
	public boolean highpriority;
	public boolean tileposition;
	public boolean seedposition;

	public final void select() {
		unitType = UnitType.None;
	}
}
