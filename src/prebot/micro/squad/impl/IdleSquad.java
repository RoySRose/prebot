package prebot.micro.squad.impl;

import bwapi.Unit;
import prebot.micro.squad.Squad;

public class IdleSquad extends Squad {
	public IdleSquad() {
		super(0, 0);
	}

	@Override
	public boolean want(Unit unit) {
		return true;
	}

	@Override
	public void execute() {
	}
}