package pre.combat.micro;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import pre.MapGrid;
import pre.combat.SquadOrder.SqaudOrderType;
import pre.util.CommandUtil;
import pre.util.MicroSet.FleeAngle;
import pre.util.MicroUtils;
import pre.util.TargetPriority;

public class MicroVulture extends MicroManager {

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> vultures = getUnits();
		List<Unit> vultureTargets = MicroUtils.filterTargets(targets, false);
		
		boolean cooltimeAlwaysAttack = false;
		boolean unitedKiting = false;
		Integer[] fleeAngle = null;
		
		if (order.getType() == SqaudOrderType.ATTACK || order.getType() == SqaudOrderType.BATTLE) { // 한타 설정
			cooltimeAlwaysAttack = true;
			unitedKiting = true;
			fleeAngle = FleeAngle.NARROW_ANGLE;
			
		} else { // 회피가 좋은 설정
			cooltimeAlwaysAttack = false;
			unitedKiting = false;
			fleeAngle = FleeAngle.WIDE_ANGLE;
		}

		for (Unit vulture : vultures) {
			if (awayFromChokePoint(vulture)) {
				continue;
			} else if (inUnityThereIsStrength(vulture)) {
				continue;
			}
			
			Unit target = getTarget(vulture, vultureTargets);
			if (target != null) {
//				MicroUtils.preciseKiting(vulture, target, false, false, order.getPosition());
				MicroUtils.preciseKiting(vulture, target, cooltimeAlwaysAttack, unitedKiting, order.getPosition(), fleeAngle);
			} else {
				// if we're not near the order position, go there
				if (vulture.getDistance(order.getPosition()) > squadRange) {
					CommandUtil.attackMove(vulture, order.getPosition());
				}
			}
		}
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (Unit target : targets) {
			int priorityScore = TargetPriority.getPriority(rangedUnit, target); // 우선순위 점수
			
			int distanceScore = 0; // 거리 점수
			int hitPointScore = 0; // HP 점수
			int dangerousScore = 0; // 위험한 새끼 점수
			
			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}
			
			int siegeMinRange = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange();
			if (target.getType().groundWeapon().maxRange() <= siegeMinRange) {
				List<Unit> nearUnits = MapGrid.Instance().getUnitsNear(target.getPosition(), siegeMinRange, true, false);
				for (Unit unit : nearUnits) {
					if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						dangerousScore += 100; // 시즈에 붙어 있으면 위험하다.
					} else {
						dangerousScore += 10;
					}
				}
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
	        hitPointScore -= target.getHitPoints() / 10;
			
			int totalScore = priorityScore + distanceScore + hitPointScore + dangerousScore;
			if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
}
