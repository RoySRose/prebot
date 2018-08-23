

import java.util.Collection;

import bwapi.Position;
import bwapi.Unit;

public class ValkyrieControl extends Control {
	
	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		
		MicroDecisionMaker decisionMaker = new MicroDecisionMaker(new DefaultTargetCalculator());
		
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, true, MicroConfig.Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, KitingOption.CoolTimeAttack.COOLTIME_ALWAYS);

		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			if (MicroUtils.isBeingHealed(unit)) {
				CommandUtils.holdPosition(unit);
				continue;
			}
			if (!StrategyIdea.mainSquadMode.isAttackMode) {
				if (dangerousOutOfMyRegion(unit)) {
					CommandUtils.move(unit, StrategyIdea.mainPosition);
					continue;
				}
			} else {
				if (StrategyIdea.mainSquadCenter.getDistance(unit) > StrategyIdea.mainSquadCoverRadius) {
					CommandUtils.move(unit, StrategyIdea.mainSquadCenter);
					continue;
				}
			}
			
			MicroDecision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == MicroDecision.MicroDecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT) {
				MicroUtils.kiting(unit, decision.eui, kOption);

			} else if (decision.type == MicroDecision.MicroDecisionType.ATTACK_POSITION) {
				if (MicroUtils.arrivedToPosition(unit, StrategyIdea.mainSquadCenter)) {
					if (MicroUtils.timeToRandomMove(unit)) {
						Position randomPosition = PositionUtils.randomPosition(unit.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
						CommandUtils.attackMove(unit, randomPosition);
					}
				} else {
					CommandUtils.attackMove(unit, StrategyIdea.mainSquadCenter);
				}
			}
		}
	}
}
