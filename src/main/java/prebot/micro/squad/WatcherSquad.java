package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.WatcherControl;

public class WatcherSquad extends Squad {
	private WatcherControl vultureWatcher = new WatcherControl();
	
	public WatcherSquad() {
		super(SquadInfo.WATCHER);
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
			vultureWatcher.control(unitList, euiList);
		}
	}
}