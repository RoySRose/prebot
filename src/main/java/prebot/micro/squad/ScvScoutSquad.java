package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.ScvScoutControl;
import prebot.strategy.StrategyIdea;

public class ScvScoutSquad extends Squad {

	private ScvScoutControl scvScoutControl = new ScvScoutControl();

	public ScvScoutSquad() {
		super(SquadInfo.SCV_SCOUT);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_SCV;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		List<Unit> recruitList = new ArrayList<>();
		if (StrategyIdea.assignScoutScv) {
			Chokepoint firstChoke = InfoUtils.myFirstChoke();
			Unit scvNearFirstChoke = UnitUtils.getClosestMineralWorkerToPosition(assignableUnitList, firstChoke.getCenter());
			if (scvNearFirstChoke != null) {
				recruitList.add(scvNearFirstChoke);
				//정찰가면서 미네랄 명령이랑 겹치는 현상 발생
				WorkerManager.Instance().setScoutWorker(scvNearFirstChoke);
				StrategyIdea.assignScoutScv = false;
			}
		}
		return recruitList;
	}

	@Override
	public void setTargetPosition() {
	}

	@Override
	public void execute() {
		scvScoutControl.control(unitList, euiList, null);
	}
}