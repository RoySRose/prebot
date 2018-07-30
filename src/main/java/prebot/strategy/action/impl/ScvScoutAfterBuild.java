package prebot.strategy.action.impl;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.Action;

/**
 * ScoutManager 기본소스에서 가져와서 수정함 unitType의 빌드완료시간이 remainingSeconds만큼 남았으면 정찰시작 (remainingSeconds이 0이면 완료시 정찰)
 */
public class ScvScoutAfterBuild extends Action {

	private final UnitType buildingType;
	private final int remainingFrames;

	private int marginFrames = 10;
	private boolean assigned = false;

	public ScvScoutAfterBuild(UnitType buildingType, int remainingFrames) {
		this.buildingType = buildingType;
		this.remainingFrames = remainingFrames;
	}

	@Override
	public boolean exitCondition() {
		if (assigned) {
			if (marginFrames > 0) {
				marginFrames--;
				return false;
			} else {
				if (InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.enemy()) == null) {
					StrategyIdea.assignScoutScv = true;
				}
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public void action() {
		if (assigned) {
			return;
		}
		for (Unit building : UnitUtils.getUnitList(UnitFindRange.ALL, buildingType)) {
			if (building.getType() == buildingType && building.getRemainingBuildTime() <= remainingFrames) {
				assigned = true;
				break;
			}
		}
	}
}