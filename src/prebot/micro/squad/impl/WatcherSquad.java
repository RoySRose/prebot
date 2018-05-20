package prebot.micro.squad.impl;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.control.VultureWatcher;
import prebot.micro.squad.Squad;

public class WatcherSquad extends Squad {
	private VultureWatcher vultureWatcher = new VultureWatcher();
	
	public WatcherSquad() {
		super(2, 100);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Vulture;
	}

	@Override
	public void execute() {
		if (!unitList.isEmpty()) {
			vultureWatcher.prepare(unitList, euiList);
			vultureWatcher.control();
		}
	}
}