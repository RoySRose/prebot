package prebot.micro.control.factory;

import java.util.Collection;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMakerPrebot1;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class GoliathControl extends Control {
	
	private int saveUnitLevel;

	public void setSaveUnitLevel(int saveUnitLevel) {
		this.saveUnitLevel = saveUnitLevel;
	}

	// TODO 수리중인 골리앗 카이팅 하지 않기
	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
//		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
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
			if (dangerousOutOfMyRegion(unit)) {
				CommandUtils.move(unit, StrategyIdea.campPosition);
				continue;
			}
			
			Decision decision = DecisionMakerPrebot1.makeDecisionPrebot1(unit, euiList, null, saveUnitLevel);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (MicroUtils.isRemovableEnemySpiderMine(unit, decision.eui)) {
					MicroUtils.holdControlToRemoveMine(unit, decision.eui.getLastPosition(), fOption);
				} else {
					MicroUtils.kiting(unit, decision.eui, kOption);
				}

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
