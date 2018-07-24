package prebot.micro.control.factory;

import java.util.List;

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

public class GoliathControl extends Control {

	// TODO 수리중인 골리앗 카이팅 하지 않기
	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, true, Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE);

		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
//			if (unit.isBeingHealed()) {
//				CommandUtils.holdPosition(unit);
//				continue;
//			}
			
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
