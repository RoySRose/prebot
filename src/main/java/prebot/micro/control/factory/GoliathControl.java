package prebot.micro.control.factory;

import java.util.Collection;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
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

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (TimeUtils.before(StrategyIdea.findRatFinishFrame)) {
			findRat(unitList);
			return;
		}
		
//		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, true, Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE);

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
				if (unit.getDistance(StrategyIdea.mainSquadCenter) > StrategyIdea.mainSquadCoverRadius * 3 / 5) {
					CommandUtils.move(unit, StrategyIdea.mainSquadCenter);
					continue;
				}
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
					if (unit.getGroundWeaponCooldown() > 15) { // UnitType.Terran_Goliath.groundWeapon().damageCooldown() = 22
						unit.move(StrategyIdea.mainPosition);
					} else {
						CommandUtils.attackMove(unit, StrategyIdea.mainPosition);
					}
				}
			}
		}
	}
}
