package prebot.brain.knowledge.impl.inerrableaction;

import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;

/**
 * 메카닉 테란 가스 조절 기본 가스조절로직을 무시하고, gasAmount만큼 가스 채취.
 */
public class GasAdjustmentMechanic extends InerrableActionKnowledge {
	@Override
	protected boolean doSomething() {
		if (TimeUtils.elapsedSeconds() > 210 || UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.ALL) >= 2) {
			Idea.of().gasAdjustment = false;
			Idea.of().gasAdjustmentWorkerCount = 0;
			return false;

		} else {
			Idea.of().gasAdjustment = true;
			int workerCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.COMPLETE);
			if (workerCount < 8) {
				Idea.of().gasAdjustmentWorkerCount = 0;
			} else {
				if (UnitUtils.hasUnit(UnitType.Terran_Factory, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE) || Prebot.Game.self().gas() >= 100) {
					Idea.of().gasAdjustmentWorkerCount = 1;
				} else {
					Idea.of().gasAdjustmentWorkerCount = 3;
				}
			}
			return true;
		}
	}
}
