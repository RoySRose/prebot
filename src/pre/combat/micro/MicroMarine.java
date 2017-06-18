package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Order;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

public class MicroMarine extends MicroManager {

	@Override
	protected void executeMicro(List<Unit> targets) {
		assignTargets(targets);
	}
	
	private void assignTargets(List<Unit> targets) {
		if (!order.isCombatOrder()) {
			return;
		}
		
	    List<Unit> rangedUnits = getUnits();

		// The set of potential targets.
		List<Unit> rangedUnitTargets = new ArrayList<>();
		for (Unit target : targets) {
			if (target.isVisible() && target.isDetected() &&
					target.getType() != UnitType.Zerg_Larva &&
					target.getType() != UnitType.Zerg_Egg &&
					!target.isStasised()) {
				rangedUnitTargets.add(target);
			}
		}

		boolean kiteWithRangedUnits = true;
		for (Unit rangedUnit : rangedUnits) {
			Unit target = getTarget(rangedUnit, rangedUnitTargets);
			if (target != null) {
				if (kiteWithRangedUnits) {
					if (rangedUnit.getType() == UnitType.Terran_Vulture) {
						MicroUtils.vultureDanceTarget(rangedUnit, target);
					} else {
						MicroUtils.smartKiteTarget(rangedUnit, target);
					}
				} else {
					CommandUtil.attackUnit(rangedUnit, target);
				}
			} else {
				// if we're not near the order position, go there
				if (rangedUnit.getDistance(order.getPosition()) > 100) {
					CommandUtil.attackMove(rangedUnit, order.getPosition());
				}
			}
		}
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		int bestScore = -999999;
		Unit bestTarget = null;

		for (Unit target : targets) {
			int priority = getAttackPriority(rangedUnit, target);     // 0..12
			int range    = rangedUnit.getDistance(target);           // 0..map size in pixels
			int toGoal   = target.getDistance(order.getPosition());  // 0..map size in pixels

			// Let's say that 1 priority step is worth 160 pixels (5 tiles).
			// We care about unit-target range and target-order position distance.
			int score = 5 * 32 * priority - range - toGoal/2;

			// Adjust for special features.
			// This could adjust for relative speed and direction, so that we don't chase what we can't catch.
			if (rangedUnit.isInWeaponRange(target)) {
				score += 4 * 32;
			} else if (!target.isMoving()) {
				if (target.isSieged() || target.getOrder() == Order.Sieging || target.getOrder() == Order.Unsieging) {
					score += 48;
				} else {
					score += 24;
				}
			} else if (target.isBraking()) {
				score += 16;
			} else if (target.getType().topSpeed() >= rangedUnit.getType().topSpeed()) {
				score -= 5 * 32;
			}
			
			// Prefer targets that are already hurt.
			if (target.getType().getRace() == Race.Protoss && target.getShields() == 0) {
				score += 32;
			} else if (target.getHitPoints() < target.getType().maxHitPoints()) {
				score += 24;
			}

			if (score > bestScore) {
				bestScore = score;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
	

	//TODO 이거
	private int getAttackPriority(Unit rangedUnit, Unit target) {
		return 0;
	}

}
