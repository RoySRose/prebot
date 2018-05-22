package prebot.micro.squad.impl;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.brain.Idea;
import prebot.brain.manager.InformationManager;
import prebot.common.code.Code.SquadName;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.control.ScvScout;
import prebot.micro.squad.Squad;

public class ScvScoutSquad extends Squad {

	private ScvScout scvScout = new ScvScout();

	public ScvScoutSquad(int priority, int squadRadius) {
		super(SquadName.SCV_SCOUT, priority, squadRadius);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_SCV;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {

		List<Unit> recruitList = new ArrayList<>();
		if (Idea.of().assignScvScout) {
			Chokepoint firstChoke = InformationManager.Instance().getFirstChokePoint(Prebot.Game.self());
			Unit scvNearFirstChoke = UnitUtils.getClosestMineralWorkerToPosition(assignableUnitList, firstChoke.getCenter());
			if (scvNearFirstChoke != null) {
				recruitList.add(scvNearFirstChoke);
			}
		}
		return recruitList;
	}

	@Override
	public void execute() {
		scvScout.prepare(unitList, euiList);
		scvScout.control();
	}
}