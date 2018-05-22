package prebot.micro.squad.impl;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.Code.SquadName;
import prebot.micro.control.VultureWatcher;
import prebot.micro.squad.Squad;

public class WatcherSquad extends Squad {
	private VultureWatcher vultureWatcher = new VultureWatcher();
	
	public WatcherSquad(int priority, int squadRadius) {
		super(SquadName.WATCHER, priority, squadRadius);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Vulture;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		if (!unitList.isEmpty()) {
			vultureWatcher.prepare(unitList, euiList);
			vultureWatcher.control();
		}
	}
}