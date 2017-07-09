package pre.combat.micro;

import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import pre.MapGrid;
import pre.combat.SpiderMineManger;
import pre.combat.SquadOrder.SquadOrderType;
import pre.util.CommandUtil;
import pre.util.KitingOption;
import pre.util.MicroSet.FleeAngle;
import pre.util.MicroUtils;
import pre.util.TargetPriority;

public class MicroVulture extends MicroManager {
	
	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> vultures = getUnits();
		List<Unit> vultureTargets = MicroUtils.filterTargets(targets, false);
		
		KitingOption kitingOption = new KitingOption();
		kitingOption.setGoalPosition(order.getPosition());
		
		if (order.getType() == SquadOrderType.ATTACK || order.getType() == SquadOrderType.BATTLE
				|| order.getType() == SquadOrderType.CHECK_INACTIVE) { // 한타 설정
			kitingOption.setCooltimeAlwaysAttack(true);
			kitingOption.setUnitedKiting(true);
			kitingOption.setFleeAngle(FleeAngle.NARROW_ANGLE);
			
		} else if (order.getType() == SquadOrderType.WATCH) { // 회피가 아주 좋은 설정(상대병력 감시용 - watcher)
			kitingOption.setCooltimeAlwaysAttack(false);
			kitingOption.setUnitedKiting(false);
			kitingOption.setFleeAngle(FleeAngle.WIDE_ANGLE);
			kitingOption.setSpecialSquadType(1);
			
		} else if (order.getType() == SquadOrderType.CHECK_ACTIVE) { // 회피가 아주 좋은 설정(정찰견제용 - checker)
			kitingOption.setCooltimeAlwaysAttack(false);
			kitingOption.setUnitedKiting(false);
			kitingOption.setFleeAngle(FleeAngle.WIDE_ANGLE);
			kitingOption.setSpecialSquadType(2);
			
		} else { // 회피가 좋은 설정
			kitingOption.setCooltimeAlwaysAttack(false);
			kitingOption.setUnitedKiting(false);
			kitingOption.setFleeAngle(FleeAngle.WIDE_ANGLE);
		}

//		Unit leader = MicroUtils.leaderOfUnit(vultures, order.getPosition());
//		if (order.getType() == SquadOrderType.CHECK_ACTIVE) {
//			for (Unit vulture : vultures) {
//			}
//		}

		for (Unit vulture : vultures) {
			if (order.getType() == SquadOrderType.BATTLE && awayFromChokePoint(vulture)) {
				continue;
			}
			if (order.getType() == SquadOrderType.ATTACK && inUnityThereIsStrength(vulture)) {
				continue;
			}
			Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
			if (positionToMine != null) { // 심기로 한 마인은 심어야지
				CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
				continue;
			}
			
			Unit target = getTarget(vulture, vultureTargets);
			if (target != null) {
				MicroUtils.preciseKiting(vulture, target, kitingOption);
				
			} else {
				if (vulture.getDistance(order.getPosition()) > squadRange) {
					positionToMine = null;
					if (order.getType() == SquadOrderType.WATCH || order.getType() == SquadOrderType.CHECK_ACTIVE) {
						positionToMine = SpiderMineManger.Instance().getPositionToMine(vulture);
					}
					if (positionToMine != null) {
						CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
					} else {
						CommandUtil.attackMove(vulture, order.getPosition());
					}
				} else {
					if (vulture.isIdle()) {
						Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), squadRange);
						CommandUtil.attackMove(vulture, randomPosition);
					}
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
				List<Unit> nearUnits = MapGrid.Instance().getUnitsNear(target.getPosition(), siegeMinRange, true, false, null);
				for (Unit unit : nearUnits) {
					if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						dangerousScore += 100; // 시즈에 붙어있는 적은 죽여야 한다.
					} else {
						dangerousScore += 10;
					}
				}
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
	        hitPointScore -= target.getHitPoints() / 10;
			
	        int totalScore = 0;
	        if (order.getType() == SquadOrderType.WATCH) {
	        	totalScore = distanceScore;
	        } else {
	        	totalScore = priorityScore + distanceScore + hitPointScore + dangerousScore;
	        }
	        
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
	
//	private Position findPositionToMine(Unit vulture) {
//		if (vulture.getSpiderMineCount() <= 0) {
//			return null;
//		}
//		
//		Position positionToMine = null;
//
//		BaseLocation nearestBase = BWTA.getNearestBaseLocation(vulture.getPosition());
//		if (vulture.getDistance(nearestBase.getPosition()) < 500) {
//			boolean nearestBaseOccupied = false;	
//			for (BaseLocation base : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
//				if (base.equals(nearestBase)) {
//					nearestBaseOccupied = true;
//					break;
//				}
//			}
//			
//			if (!nearestBaseOccupied) {
//				int x = nearestBase.getPosition().getX() + (int)(Math.random() % 20);
//				int y = nearestBase.getPosition().getY() + (int)(Math.random() % 20);
//				positionToMine = new Position(x, y);
//				
//				List<Unit> mines = MapGrid.Instance().getUnitsNear(positionToMine, 50, true, false, UnitType.Terran_Vulture_Spider_Mine);
//				if (mines.size() < 3) {
//					return positionToMine;
//				}
//			}
//		}
//		
//		
//		int x = vulture.getX() / 10 * 10;
//		int y = vulture.getY() / 10 * 10;
//		positionToMine = new Position (x, y);
//		return positionToMine;
//	}
}
