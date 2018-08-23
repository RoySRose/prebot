

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;

public class ScvScoutSquad extends Squad {

	private ScvScoutControl scvScoutControl = new ScvScoutControl();

	public ScvScoutSquad() {
		super(MicroConfig.SquadInfo.SCV_SCOUT);
		setUnitType(UnitType.Terran_SCV);
	}

	@Override
	public boolean want(Unit unit) {
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		List<Unit> recruitList = new ArrayList<>();
		if (StrategyIdea.assignScoutScv) {
			Chokepoint firstChoke = InfoUtils.myFirstChoke();
			Unit scvNearFirstChoke = UnitUtils.getClosestMineralWorkerToPosition(assignableUnitList, firstChoke.getCenter());
			if (scvNearFirstChoke != null) {
				recruitList.add(scvNearFirstChoke);
				StrategyIdea.assignScoutScv = false;
			}
		}
		return recruitList;
	}

	@Override
	public void execute() {
		scvScoutControl.controlIfUnitExist(unitList, euiList);
	}
}