package prebot.micro.squad.impl;

import java.util.List;

import bwapi.Unit;
import prebot.common.code.Code.SquadName;
import prebot.micro.squad.Squad;

public class IdleSquad extends Squad {
	public IdleSquad(int priority, int squadRadius) {
		super(SquadName.IDLE, priority, squadRadius);
	}

	@Override
	public boolean want(Unit unit) {
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
	}
}