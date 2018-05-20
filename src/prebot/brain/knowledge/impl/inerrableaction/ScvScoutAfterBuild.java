package prebot.brain.knowledge.impl.inerrableaction;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.manager.InformationManager;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;

/**
 * ScoutManager 기본소스에서 가져와서 수정함 unitType의 빌드완료시간이 remainingSeconds만큼 남았으면 정찰시작 (remainingSeconds이 0이면 완료시 정찰)
 */
public class ScvScoutAfterBuild extends InerrableActionKnowledge {
	private final UnitType buildingType;
	private final int remainingSeconds;

	public ScvScoutAfterBuild(UnitType buildingType, int remainingSeconds) {
		this.buildingType = buildingType;
		this.remainingSeconds = remainingSeconds;
	}

	@Override
	protected boolean doSomething() {
		if (InformationManager.Instance().getMainBaseLocation(Prebot.Game.enemy()) != null) {
			Idea.of().assignScvScout = false;
			return false;
			
		} else {
			for (Unit building : UnitUtils.getUnitList(buildingType, UnitFindRange.ALL)) {
				if (building.getType() == buildingType && TimeUtils.framesToSeconds(building.getRemainingBuildTime()) <= remainingSeconds) {
					Idea.of().assignScvScout = true;
					return false;
				}
			}
			return true;
		}
	}
}