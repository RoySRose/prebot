

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class MicroWraith extends MicroManager {

	// 유닛별 실행시 orderMap에서 unitId를 key로 하여 각 unit별 order를 사용한다.
//	private static Map<Integer, SquadOrder> orderMap = new HashMap<>();
//	
//	// CombatManager.updateVesselSquad에서 명령을 지정한다. by insaneojw
//	public static void setUnitOrder(Integer unitId, SquadOrder unitOrder) {
//		orderMap.put(unitId, unitOrder);
//	}
//	public static void getUnitOrder(Integer unitId) {
//		orderMap.get(unitId);
//	}
//	
//	public static void removeInvalidUnitOrder() {
//		List<Integer> removeKeys = new ArrayList<>();
//		for (Integer unitId : orderMap.keySet()) {
//			if (!CommandUtil.IsValidUnit(MyBotModule.Broodwar.getUnit(unitId))) {
//				removeKeys.add(unitId);
//			}
//		}
//		for (Integer unitId : removeKeys) {
//			orderMap.remove(unitId);
//		}
//	}
	
	private final static double wraithCheckSpeed = UnitType.Terran_Wraith.topSpeed()*8;
	private final static int wraithCheckRadius = UnitType.Terran_Wraith.sightRange()+50;
	private static final Integer[] WRAITH_ANGLE_LEFT = { -80, -70, -60, -50, -40, -30, -20, -10, +10, +20, +30, +40, +50, +60, +70, +80 };
	private static final Integer[] WRAITH_ANGLE_RIGHT = { +80, +70, +60, +50, +40, +30, +20, +10, -10, -20, -30, -40, -50, -60, -70, -80 };
	
	private List<UnitInfo> wraithTargets = new ArrayList<>();
//	private int attackFrame = 0;

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> wraiths = getUnits();
//		List<Unit> wraithTargets = MicroUtils.filterTargets(targets, true);
		
		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setCooltimeAlwaysAttack(false);
		kitingOption.setUnitedKiting(true);
		
		if (order.getPosition().equals(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer))) {
			kitingOption.setFleeAngle(WRAITH_ANGLE_LEFT);
		} else if (order.getPosition().equals(InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer))) {
			kitingOption.setFleeAngle(WRAITH_ANGLE_RIGHT);
		} else {
			kitingOption.setFleeAngle(MicroSet.FleeAngle.WIDE_ANGLE);
		}
		kitingOption.setGoalPosition(order.getPosition());
		
		wraithTargets.clear();
		for (Unit wraith : wraiths) {
			InformationManager.Instance().getNearbyForce(wraithTargets, wraith.getPosition(), InformationManager.Instance().enemyPlayer, UnitType.Terran_Wraith.sightRange() + 500);
		}
		
		int goliathCount = 0;
		for (UnitInfo target : wraithTargets) {
			if (target.getType() == UnitType.Terran_Goliath) {
				goliathCount++;
				if (goliathCount >= 2) {
					CombatManager.Instance().changeWraithOrderState();
					return;
				}
			}
		}
		
				
		for (Unit wraith : wraiths) {
			if (!CommonUtils.executeUnitRotation(wraith, LagObserver.groupsize())) {
				continue;
			}

			UnitInfo mostDangerousBuildingTarget = null;
			double mostDangerBuildingCheck = -99999;
			
			UnitInfo mostDangerousUnitTarget = null;
			double mostDangerUnitCheck = -99999;
			
			for (UnitInfo target : wraithTargets) {
				if (target.getType() == UnitType.Terran_Missile_Turret) {
					double temp = MyBotModule.Broodwar.enemy().weaponMaxRange(target.getType().airWeapon()) - target.getLastPosition().getDistance(wraith.getPosition()); 
					if (temp > mostDangerBuildingCheck) {
						mostDangerousBuildingTarget = target;
						mostDangerBuildingCheck = temp;
					}
				} else if (target.getType() == UnitType.Terran_Goliath) {
					double temp = MyBotModule.Broodwar.enemy().weaponMaxRange(target.getType().airWeapon()) - target.getLastPosition().getDistance(wraith.getPosition()); 
					if (temp > mostDangerUnitCheck) {
						mostDangerousUnitTarget = target;
						mostDangerUnitCheck = temp;
					}
				}
			}
			
			boolean unitedKiting = kitingOption.isUnitedKiting();
			Position goalPosition = kitingOption.getGoalPosition();
			Integer[] fleeAngle = kitingOption.getFleeAngle();
			
			boolean fleeing = false;
			if (mostDangerousBuildingTarget != null){
				if (wraith.getDistance(mostDangerousBuildingTarget.getLastPosition()) < 350) {
					Position fleePosition = getFleePosition(wraith, mostDangerousBuildingTarget.getLastPosition(), (int) wraithCheckSpeed, unitedKiting, goalPosition, fleeAngle);
					wraith.move(fleePosition);
					fleeing = true;
				}
			}
			
			if (!fleeing) {
				if(mostDangerousUnitTarget != null){
					Position fleePosition = getFleePosition(wraith, mostDangerousUnitTarget.getLastPosition(), (int) wraithCheckSpeed, unitedKiting, goalPosition, fleeAngle);
					wraith.move(fleePosition);
					fleeing = true;
				}
			}
			
			if (!fleeing) {
				Unit target = getTarget(wraith);
				if (target != null && !fleeing) {
					if (target.getType().airWeapon().damageAmount() == 0) {
						CommandUtil.attackUnit(wraith, target);
					} else {
						MicroUtils.preciseKiting(wraith, target, kitingOption);
					}
				}else {
					if (wraith.getDistance(order.getPosition()) > order.getRadius()) {
						CommandUtil.attackMove(wraith, order.getPosition());
					} else {
						if (wraith.isIdle()) {
							CommandUtil.attackMove(wraith, order.getPosition());
						}
					}
				}
			} 
		}
		
	}
	
	private Unit getTarget(Unit rangedUnit) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (UnitInfo targetInfo : wraithTargets) {
			Unit target = MicroUtils.getUnitIfVisible(targetInfo);
			if (target == null) {
				continue;
			}
			if (target.getType().isBuilding() && !target.isFlying()) {
				continue;
			}
			
			if (target.getType() == UnitType.Terran_Missile_Turret
					|| target.getType() == UnitType.Terran_Goliath
					|| target.getType() == UnitType.Terran_Bunker) {
				continue;
			}
			
			int priorityScore = 0;
			int distanceScore = 0;
			
			priorityScore = TargetPriority.getPriority(rangedUnit, target); // 우선순위 점수
			distanceScore = 0; // 거리 점수
			
			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
			
	        int totalScore =  priorityScore + distanceScore;
	        
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
	
	
	public Position getFleePosition(Unit rangedUnit, Unit target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle) {
		return getFleePosition(rangedUnit, target.getPosition(), moveDistPerSec, unitedKiting, goalPosition, fleeAngle, true);
	}
	
	public Position getFleePosition(Unit rangedUnit, Position target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle) {
		return getFleePosition(rangedUnit, target, moveDistPerSec, unitedKiting, goalPosition, fleeAngle, false);
	}
	public Position getFleePosition(Unit rangedUnit, Position target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle, boolean fromUnit) {
		int reverseX = rangedUnit.getPosition().getX() - target.getX(); // 타겟과 반대로 가는 x양
		int reverseY = rangedUnit.getPosition().getY() - target.getY(); // 타겟과 반대로 가는 y양
	    final double fleeRadian = Math.atan2(reverseY, reverseX); // 회피 각도
	    
		Position safePosition = null;
		double minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;

//		Integer[] FLEE_ANGLE = MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType()); // MicroData.FleeAngle에 저장된 유닛타입에 따른 회피 각 범위(골리앗 새끼들은 뚱뚱해서 각이 넓으면 지들끼리 낑김)
		Integer[] FLEE_ANGLE = fleeAngle != null ? fleeAngle : MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType());
		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = moveDistPerSec; // 이동 회피지점의 거리 = 유닛의 초당이동거리
		
		while (safePosition == null && moveCalcSize < 1200) {//TODO 이거 회피지역 안나오면 무한 Loop 아닐까??? moveCalcsize 때문에 무한은 안 돌테지만... 사실상 moveCalcsize 는 늘어나야 찾을수 있을텐데
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
				
			    Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(), rangedUnit.getPosition().getY() + fleeVector.getY()); // 회피지점
				Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2, rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // 회피중간지점
				
				double risk = 0;
				risk = riskOfFleePositionAir(rangedUnit, movePosition, moveCalcSize, unitedKiting, fromUnit); // 회피지점에서의 예상위험도
				
				int distanceToGoal = movePosition.getApproxDistance(goalPosition); // 위험도가 같을 경우 2번째 고려사항: 목표지점까지의 거리
				// 회피지점은 유효하고, 걸어다닐 수 있어야 하고, 안전해야 하고 등등
				// 변경사항. 기존과 같이 전체를 확인하다가 만족스러운 곳이 있으면 회피 지점이 아닌 전체를 모두 확인하고 가장 좋은 자리를 가지고 간다.
				if (risk < 100 	&& (risk < minimumRisk || ((risk == minimumRisk) && distanceToGoal < minimumDistanceToGoal))) {
				
					//System.out.print("selected: " + i + "  ");
					safePosition =  movePosition;
					//System.out.println("setting position : " +safePosition.getX() + ","+safePosition.getY());
					minimumRisk = risk;
					minimumDistanceToGoal = distanceToGoal;
				}
				fleeRadianAdjust = MicroUtils.rotate(fleeRadian, FLEE_ANGLE[i]); // 각도변경
		    }
			if (safePosition == null) { // 회피지역이 없을 경우 1) 회피거리 짧게 잡고 다시 조회 
		    	moveCalcSize = moveCalcSize * 2; //TODO 보정 로직이 moveCalcsize 늘려야 하는거 같은데..또한 늘렸을시에 무한 loop 조심
			}
		}
		if (safePosition == null) { // 회피지역이 없을 경우 3) 목표지점으로 간다. 이 경우는 거의 없다.
			safePosition = goalPosition;
		}
		
	    return safePosition;
	}

	public double riskOfFleePositionAir(Unit myunit, Position position, int radius, boolean united, boolean fromUnit) {
		double risk = 0;

		for (UnitInfo targetInfo : wraithTargets) {
			Unit target = MicroUtils.getUnitIfVisible(targetInfo);
			if (target == null) {
				continue;
			}
			
			double inrange =  MyBotModule.Broodwar.enemy().weaponMaxRange(target.getType().airWeapon());
			double additionalrange = wraithCheckSpeed;		
			double distance = target.getDistance(position);
			//if(enemyunits.getDistance(unit) <= inrange + additionalrange){
			if(target.getType() == UnitType.Terran_Missile_Turret
					|| target.getType() == UnitType.Terran_Goliath
					|| target.getType() == UnitType.Terran_Marine
					|| target.getType() == UnitType.Terran_Wraith
					|| target.getType() == UnitType.Terran_Marine
					|| (target.getType() == UnitType.Terran_Bunker && target.isLoaded())
					 ){
				if (distance < inrange) {
					risk += 100;
				} else {
//					risk = (additionalrange - (distance - inrange))/additionalrange * 100;
				}
			}
			
		}
		
		return risk;
	}
}
