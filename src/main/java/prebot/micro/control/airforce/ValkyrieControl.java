package prebot.micro.control.airforce;

import java.util.Collection;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.micro.targeting.DefaultTargetCalculator;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class ValkyrieControl extends Control {
	
	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		
		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
		
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, true, Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS);

		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			if (MicroUtils.isBeingHealed(unit)) {
				CommandUtils.holdPosition(unit);
				continue;
			}
			if (dangerousOutOfMyRegion(unit)) {
				CommandUtils.move(unit, StrategyIdea.campPosition);
				continue;
			}
			
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == DecisionType.KITING_UNIT) {
				MicroUtils.kiting(unit, decision.eui, kOption);

			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				if (MicroUtils.arrivedToPosition(unit, StrategyIdea.mainPosition)) {
					if (MicroUtils.timeToRandomMove(unit)) {
						Position randomPosition = PositionUtils.randomPosition(unit.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
						CommandUtils.attackMove(unit, randomPosition);
					}
				} else {
					CommandUtils.attackMove(unit, StrategyIdea.mainPosition);
				}
			}
		}
	}
}
