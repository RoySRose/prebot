package pre.combat.micro;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import pre.main.MyBotModule;
import pre.util.CommandUtil;
import pre.util.KitingOption;
import pre.util.MicroSet;
import pre.util.MicroSet.FleeAngle;
import pre.util.MicroUtils;

public class MicroMarine extends MicroManager {

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> marines = getUnits();
	    Unit bunker = null; 
	    Unit CC = null;
	    
	    if(targets.size() == 0){
			return;
		}
	    	
//		List<Unit> targets = new ArrayList<>();
//		for (Unit target : targets) {
//			if (target.isVisible() && target.isDetected() &&
//					target.getType() != UnitType.Zerg_Larva &&
//					target.getType() != UnitType.Zerg_Egg &&
//					!target.isStasised()) {
//				targets.add(target);
//			}
//		}
		
//		if(targets.size() == 0){
//			return;
//		}
	    	
		boolean DontGoFar = true;
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
			if(unit.getType() == UnitType.Terran_Bunker && unit.isCompleted()){
				bunker = unit;
			}
			if(unit.isUnderAttack()){
				DontGoFar = false;
			}
			if(unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted()){
				CC = unit;
			}
		}
		
		
		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setCooltimeAlwaysAttack(false);
		kitingOption.setUnitedKiting(true); //TODO 같이 가 좋으까?
		kitingOption.setGoalPosition(order.getPosition());
		kitingOption.setFleeAngle(FleeAngle.NARROW_ANGLE);
		kitingOption.setHaveToFlee(false);

		boolean kiteWithmarines = true;
		
	
		//벙커가 없는 경우 행동 
		if(bunker == null){
			Unit mineral = getClosestMineral(CC);			
			kitingOption.setGoalPosition(mineral.getPosition());
			//kitingOption.setSaveUnit(false);
			kitingOption.setCooltimeAlwaysAttack(true);
			
			for (Unit marine : marines) {
				
				Unit target = getTarget(marine, targets);
				if(target != null){
					
					if(marine.getDistance(target) < 50){
						DontGoFar= false;
					}
					
					if(DontGoFar){
						if(marine.getDistance(mineral) > 80){
							marine.move(mineral.getPosition());
						}
					}else{
						if (target != null) {
							if (kiteWithmarines) {
								MicroUtils.preciseKiting(marine, target, kitingOption, false);
							} 
						}
					}
				}else{
					// if we're not near the order position, go there
					if (marine.getDistance(order.getPosition()) > 80) {
						CommandUtil.move(marine, order.getPosition());
					}
				}
			}
		}else{//벙커가 있으면
			kitingOption.setGoalPosition(bunker.getPosition());
			for (Unit marine : marines) {
				Unit target = getTarget(marine, targets);
				if(target != null){
					
					if(marine.getType().maxHitPoints() < 11){
						kitingOption.setHaveToFlee(true);
					}else{
						kitingOption.setHaveToFlee(false);
					}
				
	//				if(marine.getDistance(target) < marine.getType().groundWeapon().maxRange()/2){
						//kitingOption.setCooltimeAlwaysAttack(false);
	//				}else{
	//					kitingOption.setCooltimeAlwaysAttack(true);
	//				}
					
							
		//			System.out.println("====================================");
	//				System.out.println("marine attackrange : " + marine.getType().groundWeapon().maxRange());
	//				System.out.println("marine attackrange : " + MyBotModule.Broodwar.self().weaponMaxRange(WeaponType.Gauss_Rifle));
	//				System.out.println("bunker attackrange : " + bunker.getType().groundWeapon().maxRange());
	//				System.out.println("marine " + marine.getID() + " far from bunker : " + marine.getDistance(bunker) + " and far from target : " + marine.getDistance(target));
		//			System.out.println("target " + target.getID() + " is far from bunker : " + target.getDistance(bunker));
		//			System.out.println("enemy in range of bunker :" + bunker.isInWeaponRange(target));
					
					
						if(marine.getHitPoints() < 6){
							bunker.load(marine);
							continue;
						}
						
						
	//					if(marine.getDistance(bunker) < marine.getType().groundWeapon().maxRange()*1/2){
	//						kitingOption.setCooltimeAlwaysAttack(true);
	//					}
						if(DontGoFar){
							if(marine.getDistance(bunker) > 120){
								marine.move(bunker.getPosition());
							}
							if(marine.getHitPoints() < 11){
								if(marine.getDistance(bunker) > 50){
									marine.move(bunker.getPosition());
								}
							}
						}else{
							if(marine.getDistance(target) <= marine.getType().groundWeapon().maxRange()*3/4 && bunker.getDistance(target) <= 160){
								
								if(marine.getDistance(bunker) < 30 ){
									bunker.load(marine);
								}else{
		//							System.out.println("marine moving");
									marine.move(bunker.getPosition());
								}
								//marine.move(bunker.getPosition());
							}else{
								if (target != null) {
									if (kiteWithmarines) {
										MicroUtils.preciseKiting(marine, target, kitingOption, true);
									} 
								}
							}
						}
				}else{
					// if we're not near the order position, go there
					if (marine.getDistance(order.getPosition()) > 40) {
						CommandUtil.move(marine, order.getPosition());
					}
				}
			}
		}
	}
	
	
	public Unit getClosestMineral(Unit depot)
	{
		double bestDist = 99999;
		Unit bestMineral = null;
		
		for (Unit mineral : MyBotModule.Broodwar.getAllUnits()){
			if ((mineral.getType() == UnitType.Resource_Mineral_Field) && mineral.getDistance(depot) < 320){
				double dist = mineral.getDistance(depot);
				if (dist < bestDist){
                    bestMineral = mineral;
                    bestDist = dist;
                }
			}
		}
	    return bestMineral;
	}
	
	private Unit getTarget(Unit marine, List<Unit> targets) {
		int bestScore = -999999;
		Unit bestTarget = null;

		for (Unit target : targets) {
			if (!target.isDetected()) continue;
			
			//int priority = 0;   // 0..12
			int range    = marine.getDistance(target);           // 0..map size in pixels
			int toGoal   = target.getDistance(order.getPosition());  // 0..map size in pixels

			// Let's say that 1 priority step is worth 160 pixels (5 tiles).
			// We care about unit-target range and target-order position distance.
			//int score = 5 * 32 * priority - range - toGoal/2;
			int score =  - range - toGoal/2;

			// Adjust for special features.
			// This could adjust for relative speed and direction, so that we don't chase what we can't catch.
			if (marine.isInWeaponRange(target)) {
				score += 4 * 32;
			} else if (!target.isMoving()) {
				score += 24;
			} else if (target.isBraking()) {
				score += 16;
			} else if (target.getType().topSpeed() >= marine.getType().topSpeed()) {
				score -= 5 * 32;
			}
			
			// Prefer targets that are already hurt.
//			if (target.getType().getRace() == Race.Protoss && target.getShields() == 0) {
//				score += 32;
//			} else if (target.getHitPoints() < target.getType().maxHitPoints()) {
//				score += 24;
//			}
			
			score += (target.getType().maxHitPoints() + target.getType().maxShields() - target.getHitPoints() + target.getShields())
					/ target.getType().maxHitPoints() + target.getType().maxShields() * 32;  
			
			if (score > bestScore) {
				bestScore = score;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
	
	public static Position getFleePosition(Unit rangedUnit, Unit target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle, Boolean bunker) {
		int reverseX = rangedUnit.getPosition().getX() - target.getPosition().getX(); // 타겟과 반대로 가는 x양
		int reverseY = rangedUnit.getPosition().getY() - target.getPosition().getY(); // 타겟과 반대로 가는 y양
	    final double fleeRadian = Math.atan2(reverseY, reverseX); // 회피 각도
	    
		Position safePosition = null; // 0.0 means the unit is facing east.
		int minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;
		double maximumdistanceToTarget = 0;

//		Integer[] FLEE_ANGLE = MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType()); // MicroData.FleeAngle에 저장된 유닛타입에 따른 회피 각 범위(골리앗 새끼들은 뚱뚱해서 각이 넓으면 지들끼리 낑김)
		Integer[] FLEE_ANGLE = fleeAngle != null ? fleeAngle : MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType());
		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = moveDistPerSec; // 이동 회피지점의 거리 = 유닛의 초당이동거리
		if(!bunker){
			moveCalcSize = moveDistPerSec/4;
		}
		while (safePosition == null && moveCalcSize > 10) {
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
			    Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(), rangedUnit.getPosition().getY() + fleeVector.getY()); // 회피지점
				Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2, rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // 회피중간지점
				
				int risk = MicroUtils.riskOfFleePosition(rangedUnit.getType(), movePosition, moveCalcSize, unitedKiting); // 회피지점에서의 예상위험도
				int distanceToGoal = movePosition.getApproxDistance(goalPosition); // 위험도가 같을 경우 2번째 고려사항: 목표지점까지의 거리
				double distanceToTarget = movePosition.getDistance(target.getPosition());
				
				if(bunker){
					risk += distanceToGoal;
					// 회피지점은 유효하고, 걸어다닐 수 있어야 하고, 안전해야 하고 등등
					if (MicroUtils.isValidGroundPosition(movePosition) && middlePosition.isValid() && BWTA.getRegion(middlePosition) != null
							&& (risk < minimumRisk || (risk == minimumRisk && distanceToGoal < minimumDistanceToGoal))) {
						
						safePosition =  movePosition;
						minimumRisk = risk;
						minimumDistanceToGoal = distanceToGoal;
					}
				}else{
					if (MicroUtils.isValidGroundPosition(movePosition) && middlePosition.isValid() && BWTA.getRegion(middlePosition) != null
							&& (risk < minimumRisk || (risk == minimumRisk && distanceToTarget > maximumdistanceToTarget))) {
						
						safePosition =  movePosition;
						minimumRisk = risk;
						maximumdistanceToTarget = distanceToTarget;
					}
				}
				fleeRadianAdjust = MicroUtils.rotate(fleeRadian, FLEE_ANGLE[i]); // 각도변경
		    }
			if (safePosition == null) { // 회피지역이 없을 경우 1) 회피거리 짧게 잡고 다시 조회
//		    	MyBotModule.Broodwar.sendText("safe is null : " + moveCalcSize);
		    	moveCalcSize = moveCalcSize * 2;
		    	
		    	if (moveCalcSize <= 10 && FLEE_ANGLE.equals(FleeAngle.NARROW_ANGLE)) { // 회피지역이 없을 경우 2) 각 범위를 넓힘
					FLEE_ANGLE = FleeAngle.WIDE_ANGLE;
					unitedKiting = false;
					moveCalcSize = moveDistPerSec;
				}
			}
		}
		if (safePosition == null) { // 회피지역이 없을 경우 3) 목표지점으로 간다. 이 경우는 거의 없다.
			safePosition = goalPosition;
		}
		
	    return safePosition;
	}
	
}
