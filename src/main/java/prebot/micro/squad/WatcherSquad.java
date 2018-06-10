package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.micro.VultureCombatPredictor;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.WatcherControl;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.StrategyCode.VultureCombatResult;

public class WatcherSquad extends Squad {

	private static final int WATCHER_FLEE_SECONDS = 7;
	private VultureCombatResult vultureCombatResult = VultureCombatResult.ATTACK;
	private int watcherFleeStartFrame = 0;
	
	public VultureCombatResult getVultureCombatResult() {
		return vultureCombatResult;
	}

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
	public void setTargetPosition() {
		targetPosition = StrategyIdea.attackPosition;
	}

	@Override
	public void execute() {
		if (StrategyIdea.initiated) {
			vultureCombatResult = VultureCombatResult.ATTACK;
		} else {
			if (TimeUtils.elapsedSeconds(watcherFleeStartFrame) <= WATCHER_FLEE_SECONDS) {
				vultureCombatResult = VultureCombatResult.BACK;
			} else {
				vultureCombatResult = VultureCombatPredictor.watcherPredictByUnitInfo(unitList, euiList);
				if (vultureCombatResult == VultureCombatResult.BACK) {
					watcherFleeStartFrame = TimeUtils.elapsedFrames();
				}
			}
		}
		
		if (!unitList.isEmpty()) {
			vultureWatcher.setVultureCombatResult(vultureCombatResult);
			vultureWatcher.control(unitList, euiList, targetPosition);
		}
	}
}