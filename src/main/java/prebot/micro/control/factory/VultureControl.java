package prebot.micro.control.factory;

import java.util.List;

import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class VultureControl extends Control {


	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);
		FleeOption fOption = new FleeOption(StrategyIdea.campPosition, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, false);
		
		for (Unit unit : unitList) {
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				
				
				MicroUtils.kiting(unit, decision.eui, kOption);
				
			} else {
				CommandUtils.attackMove(unit, StrategyIdea.attackPosition);
			}
			
		}
	}
}
