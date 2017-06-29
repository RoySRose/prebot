package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

public class MicroGoliath extends MicroManager {

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> goliaths = getUnits();

		// figure out targets
		List<Unit> goliathTargets = new ArrayList<>();
		for (Unit target : targets) {
			if (target.isVisible() && !target.isStasised()) {
				goliathTargets.add(target);
			}
		}

		for (Unit goliath : goliaths) {
			Unit target = getTarget(goliath, goliathTargets);
			if (target != null) {
				MicroUtils.smartKiteTarget(goliath, target, order.getPosition(), true, true);
			} else {
				// if we're not near the order position, go there
				if (goliath.getDistance(order.getPosition()) > 100) {
					CommandUtil.attackMove(goliath, order.getPosition());
				}
			}
		}
		
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;
//		int hitPointScore = 0; // HP 점수

		for (Unit target : targets) {
			int priorityScore = getAttackPriority(rangedUnit, target);
			int distanceScore = 0 - rangedUnit.getDistance(target) / 50;           // 0..map size in pixels

			int score = priorityScore + distanceScore;

			if (score > bestTargetScore) {
				bestTargetScore = score;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
	
	private int getAttackPriority(Unit rangedUnit, Unit target) {
		return 0;
	}


}
