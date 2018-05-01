package prebot.micro.control;

import bwapi.Unit;
import prebot.brain.Idea;
import prebot.brain.WatcherPredictor;
import prebot.common.code.Code.WatcherPredictResult;
import prebot.common.code.ConfigForMicro.Angles;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;

public class VultureWatcher extends Control {

	@Override
	public void control() {
		WatcherPredictResult result = WatcherPredictor.predictByUnitInfo(unitList, euiList);
		
		if (result == WatcherPredictResult.ATTACK) {

			DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);
			FleeOption fOption = new FleeOption(Idea.of().campPosition, false, Angles.WIDE);
			KitingOption kOption = new KitingOption(fOption, false);
			
			for (Unit unit : unitList) {
				Decision decision = decisionMaker.makeDecision(unit, euiList);
				if (decision.type == DecisionType.FLEE_FROM_UNIT) {
					MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
					
				} else if (decision.type == DecisionType.KITING_UNIT) {
					MicroUtils.kiting(unit, decision.eui, kOption);
					
				} else {
					CommandUtils.attackMove(unit, Idea.of().attackPosition);
				}
			}
			
		} else {
			
			Unit leader = UnitUtils.getClosestUnitToPosition(unitList, Idea.of().attackPosition);
			for (Unit unit : unitList) {
				if (unit.getID() == leader.getID() || unit.getDistance(leader) <= 300) {
					CommandUtils.move(unit, Idea.of().campPosition);
				} else {
					CommandUtils.move(unit, leader.getPosition());
				}
			}
		}
	}
}
