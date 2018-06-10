package prebot.strategy.action.impl;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.Action;

/**
 * ScoutManager 기본소스에서 가져와서 수정함 unitType의 빌드완료시간이 remainingSeconds만큼 남았으면 정찰시작 (remainingSeconds이 0이면 완료시 정찰)
 */
public class ScvScoutAfterBuild extends Action {

	private final UnitType buildingType;
	private final int remainingSeconds;

	private boolean assigned = false;

	public ScvScoutAfterBuild(UnitType buildingType, int remainingSeconds) {
		this.buildingType = buildingType;
		this.remainingSeconds = remainingSeconds;
	}

	@Override
	public boolean exitCondition() {
		if (assigned) {
			if (InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.enemy()) == null) {
				StrategyIdea.assignScoutScv = true;
			}
			return true;
		}
		
		return false;
	}

	@Override
	public void action() {
		if (!assigned) {
			for (Unit building : UnitUtils.getUnitList(UnitFindRange.ALL, buildingType)) {
				if (building.getType() == buildingType && TimeUtils.framesToSeconds(building.getRemainingBuildTime()) <= remainingSeconds) {
					assigned = true;
					break;
				}
			}
		}
	}
}