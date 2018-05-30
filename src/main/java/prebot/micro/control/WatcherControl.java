package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.WatcherPredictor;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.WatcherPredictResult;

/// MainSquad <-> 적 기지 or 주력병력 주둔지 이동하여 마인 매설 
public class WatcherControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		WatcherPredictResult result = WatcherPredictor.predictByUnitInfo(unitList, euiList);
		
		if (result == WatcherPredictResult.ATTACK) {

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
			
		} else {
			
			Unit leader = UnitUtils.getClosestUnitToPosition(unitList, StrategyIdea.attackPosition);
			for (Unit unit : unitList) {
				if (unit.getID() == leader.getID() || unit.getDistance(leader) <= 300) {
					CommandUtils.move(unit, StrategyIdea.campPosition);
				} else {
					CommandUtils.move(unit, leader.getPosition());
				}
			}
		}
	}
}
