package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.strategy.StrategyIdea;

public class IdleSquad extends Squad {
	public IdleSquad() {
		super(SquadInfo.IDLE);
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
	public void setTargetPosition() {
	}

	@Override
	public void execute() {
	}
}