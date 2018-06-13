package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.WatcherControl;
import prebot.micro.predictor.VultureFightPredictor;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;

public class WatcherSquad extends Squad {

	private static final int WATCHER_FLEE_SECONDS = 8;
	private SmallFightPredict smallFightPredict = SmallFightPredict.ATTACK;
	private int watcherFleeStartFrame = 0;
	
	public SmallFightPredict getSmallFightPredict() {
		return smallFightPredict;
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
			smallFightPredict = SmallFightPredict.ATTACK;
		} else {
			if (TimeUtils.elapsedSeconds(watcherFleeStartFrame) <= WATCHER_FLEE_SECONDS) {
				smallFightPredict = SmallFightPredict.BACK;
			} else {
				smallFightPredict = VultureFightPredictor.watcherPredictByUnitInfo(unitList, euiList);
				if (smallFightPredict == SmallFightPredict.BACK) {
					watcherFleeStartFrame = TimeUtils.elapsedFrames();
				}
			}
		}
		
		if (!unitList.isEmpty()) {
			vultureWatcher.setSmallFightPredict(smallFightPredict);
			vultureWatcher.control(unitList, euiList, targetPosition);
		}
	}
}