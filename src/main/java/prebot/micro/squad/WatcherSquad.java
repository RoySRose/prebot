package prebot.micro.squad;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.WatcherControl;
import prebot.micro.predictor.VultureFightPredictor;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;

public class WatcherSquad extends Squad {

//	private static final int MAX_REGROUP_POSITION_SIZE = 3;
	private static final int WATCHER_FLEE_SECONDS = 8;
	private SmallFightPredict smallFightPredict = SmallFightPredict.ATTACK;
	private int watcherFleeStartFrame = 0;

//	private Position otherWatcherPosition;
//	private Position[] regroupPositions = new Position[MAX_REGROUP_POSITION_SIZE];
//	private int regroupPositionIndex = 0;
//	private int otherWatcherPositionIndex = 0;
	
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
	public void execute() {
		if (unitList.isEmpty()) {
			return;
		}

		updateWatcherPosition();
		
		Unit regroupLeader = null;
		if (StrategyIdea.initiated) {
			smallFightPredict = SmallFightPredict.ATTACK;
		} else {
			if (TimeUtils.elapsedSeconds(watcherFleeStartFrame) <= WATCHER_FLEE_SECONDS) {
				smallFightPredict = SmallFightPredict.BACK;
			} else {
				smallFightPredict = VultureFightPredictor.watcherPredictByUnitInfo(unitList, euiList);
				if (smallFightPredict == SmallFightPredict.BACK) {
					watcherFleeStartFrame = TimeUtils.elapsedFrames();
					regroupLeader = UnitUtils.getClosestUnitToPosition(unitList, StrategyIdea.watcherPosition);
//					otherWatcherPosition = getOtherWatcherPosition(regroupLeader.getPosition());
				}
			}
		}
		if (smallFightPredict == SmallFightPredict.BACK && regroupLeader == null) {
			regroupLeader = UnitUtils.getClosestUnitToPosition(unitList, StrategyIdea.watcherPosition);
		}
		
		vultureWatcher.setSmallFightPredict(smallFightPredict);
		vultureWatcher.setRegroupLeader(regroupLeader);
		
		vultureWatcher.control(unitList, euiList);
	}

	private void updateWatcherPosition() {
		Position watcherPosition;
		if (PositionUtils.isValidGroundPosition(StrategyIdea.enemyGroundSquadPosition)) {
			watcherPosition = StrategyIdea.enemyGroundSquadPosition;
			
		} else if (InfoUtils.enemyBase() != null) {
			// if (otherWatcherPosition != null) {watcherPosition = otherWatcherPosition;} else {}
			watcherPosition = InfoUtils.enemyBase().getPosition();
		} else {
			watcherPosition = InfoUtils.mySecondChoke().getCenter();
		}
		StrategyIdea.watcherPosition = watcherPosition;
	}

//	private Position getOtherWatcherPosition(Position regroupPosition) {
//		if (InfoUtils.enemyBase() == null) {
//			return null;
//		}
//		
//		regroupPositions[regroupPositionIndex] = regroupPosition;
//		regroupPositionIndex = (regroupPositionIndex + 1) % MAX_REGROUP_POSITION_SIZE;
//		
//		Position firstExpansionPosition = InfoUtils.myFirstExpansion().getPosition();
//		double beforeDistance = 0;
//		double distance = 0;
//		for (int i = 0; i < MAX_REGROUP_POSITION_SIZE; i++) {
//			Position position = regroupPositions[(regroupPositionIndex + i) % MAX_REGROUP_POSITION_SIZE];
//			if (position == null) {
//				return null;
//			}
//			distance = position.getDistance(firstExpansionPosition);
//			if (distance + 200 < beforeDistance) { // 적이 다가오고 있다고 판단되면
//				return null;
//			}
//		}
//		
//		List<BaseLocation> enemyOtherExpansions = InfoUtils.enemyOtherExpansionsSorted();
//		BaseLocation baseLocation = enemyOtherExpansions.get(otherWatcherPositionIndex);
//		if (baseLocation != null) {
//			otherWatcherPositionIndex = (otherWatcherPositionIndex + 1) % enemyOtherExpansions.size();
//			return baseLocation.getPosition();
//		} else {
//			return null;
//		}
//	}
}