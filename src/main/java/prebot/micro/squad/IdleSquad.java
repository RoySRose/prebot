package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import prebot.micro.constant.MicroConfig;

public class IdleSquad extends Squad {
	public IdleSquad() {
		super(MicroConfig.SquadInfo.IDLE);
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