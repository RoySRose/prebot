

import java.util.Collection;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;

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
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, true, MicroConfig.Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, KitingOption.CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE);
		
		int coverRadius = StrategyIdea.mainSquadCoverRadius * 3 / 5;
		if (InfoUtils.enemyRace() == Race.Zerg) {
			coverRadius = StrategyIdea.mainSquadCoverRadius;
		}

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
				if (InfoUtils.enemyRace() != Race.Terran) {
					if (InfoUtils.euiListInBase().isEmpty() && InfoUtils.euiListInExpansion().isEmpty()) {
						if (unit.getDistance(StrategyIdea.mainSquadCenter) > coverRadius) {
							CommandUtils.move(unit, StrategyIdea.mainSquadCenter);
							continue;
						}
					}
				}
			}
			
			MicroDecision decision = MicroDecisionMakerPrebot1.makeDecisionPrebot1(unit, euiList, null, saveUnitLevel);
			if (decision.type == MicroDecision.MicroDecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT) {
				if (MicroUtils.isRemovableEnemySpiderMine(unit, decision.eui)) {
					MicroUtils.holdControlToRemoveMine(unit, decision.eui.getLastPosition(), fOption);
				} else {
					MicroUtils.kiting(unit, decision.eui, kOption);
				}

			} else if (decision.type == MicroDecision.MicroDecisionType.ATTACK_POSITION) {
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
