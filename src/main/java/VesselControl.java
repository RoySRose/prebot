

import java.util.Collection;
import java.util.List;

import bwapi.Order;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;

public class VesselControl extends Control {

	private final static double VESSEL_CHECK_SPEED = UnitType.Terran_Science_Vessel.topSpeed() * 8;
	private final static int VESSEL_SIGHT = UnitType.Terran_Science_Vessel.sightRange();

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		// TODO Auto-generated method stub
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, false, MicroConfig.Angles.WIDE);

		for (Unit vessel : unitList) {
			if (skipControl(vessel)) {
				continue;
			}

			// 싸베 이리디 쓰기
			if (StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR1
					|| StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR2) {
				if (vessel.getEnergy() >= 75) {
					List<Unit> irradiateTargets = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, vessel.getPosition(), VESSEL_SIGHT + 300, UnitType.Zerg_Mutalisk, UnitType.Zerg_Guardian); 
					for (Unit target : irradiateTargets) {
						if (target.getHitPoints() > target.getType().maxHitPoints() * 0.9) {
							CommandUtils.useTechTarget(vessel, TechType.Irradiate, target);
							return;
						}
					}
				}
			}
			// 싸베 디펜시브 매트릭스 쓰기
			else if (vessel.getEnergy() >= 100) {
				List<Unit> matrixTargets = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.SELF, vessel.getPosition(), VESSEL_SIGHT + 300); 
				for (Unit target : matrixTargets) {
					if (target.getType() == UnitType.Terran_SCV
							|| target.getType() == UnitType.Terran_Vulture_Spider_Mine
							|| target.getType() == UnitType.Terran_Wraith) {
						continue;
					}
					if (target.isUnderAttack() && !target.isDefenseMatrixed()
							&& target.getHitPoints() > target.getType().maxHitPoints() * 0.15
							&& target.getHitPoints() < target.getType().maxHitPoints() * 0.7) {
						CommandUtils.useTechTarget(vessel, TechType.Defensive_Matrix, target);
						return;
					}
				}
			}

			Unit invisibleEnemyUnit = null;
			int closestDistToVessel = 100000;
			for (UnitInfo unitInfo : euiList) {
				Unit unit = unitInfo.getUnit();
				if (unit.isVisible() && unit.isDetected() && unit.getPosition().isValid()) {
					if (unit.getType() == UnitType.Protoss_Dark_Templar || unit.getType() == UnitType.Zerg_Lurker
							|| unit.getType() == UnitType.Terran_Wraith || unit.getType() == UnitType.Terran_Ghost
							|| unit.getType() == UnitType.Protoss_Arbiter) {
						int tempdist = unit.getDistance(vessel);

						if (tempdist < closestDistToVessel) {
							invisibleEnemyUnit = unit;
							closestDistToVessel = tempdist;
						}
					}
				}
			}

			Position orderPosition = PositionUtils.randomPosition(StrategyIdea.mainSquadLeaderPosition, 500);
			
			if (invisibleEnemyUnit == null) {
				closestDistToVessel = 100000;
				for (UnitInfo unitInfo : euiList) {
					Unit unit = unitInfo.getUnit();
					if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)) {
						int tempdist = unit.getDistance(vessel);
						if (tempdist < closestDistToVessel) {
							invisibleEnemyUnit = unit;
							closestDistToVessel = tempdist;
						}
					}
				}
			}
			
			if (invisibleEnemyUnit != null) {
				List<Unit> nearallies = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.SELF, vessel.getPosition(), VESSEL_SIGHT);
				if (nearallies.size() > 2) {
					if (invisibleEnemyUnit.getDistance(vessel) <= UnitType.Terran_Science_Vessel.sightRange() * 2 / 3) {
						orderPosition = invisibleEnemyUnit.getPosition();// 움직이시오.
					}
				}
			}

			Unit mostDangerousTarget = null;
			double mostDangercheck = -99999;

			List<Unit> dangerous_targets = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, vessel.getPosition(), VESSEL_SIGHT); 
			for (Unit target : dangerous_targets) {
				double temp = target.getType().airWeapon().maxRange() - target.getPosition().getDistance(vessel.getPosition());
				if (temp > mostDangercheck) {
					mostDangerousTarget = target;
					mostDangercheck = temp;
				}
			}


			boolean fleeing = false;
			if (mostDangerousTarget != null) {
				double temp = 0;
				if (!mostDangerousTarget.getType().isBuilding()) {
					temp = VESSEL_CHECK_SPEED * 3;
				}
				if (mostDangerousTarget.isInWeaponRange(vessel)
						|| (vessel.getDistance(mostDangerousTarget) <= MyBotModule.Broodwar.enemy().weaponMaxRange(
								mostDangerousTarget.getType().airWeapon()) + VESSEL_CHECK_SPEED + temp)) {
					Position fleePosition = getFleePosition(vessel, mostDangerousTarget.getPosition(), (int) VESSEL_CHECK_SPEED,fOption);
					vessel.move(fleePosition);
					fleeing = true;
				}
			}

			if (!fleeing) {
				if (!MicroUtils.isBeingHealed(vessel)) {
					CommandUtils.move(vessel, orderPosition);
				}
			}
		}
	}
	
	public static Position getFleePosition(Unit rangedUnit, Position target,int moveDistPerSec, FleeOption fleeOption) {
		int reverseX = rangedUnit.getPosition().getX() - target.getX(); // 타겟과 반대로 가는 x양
		int reverseY = rangedUnit.getPosition().getY() - target.getY(); // 타겟과 반대로 가는 y양
	    final double fleeRadian = Math.atan2(reverseY, reverseX); // 회피 각도
	    
		Position safePosition = null;
		double minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;

		int[] FLEE_ANGLE = (int[]) (fleeOption.angles != null ? fleeOption.angles : MicroConfig.FleeAngle.getFleeAngle(rangedUnit.getType()));
		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = moveDistPerSec; // 이동 회피지점의 거리 = 유닛의 초당이동거리
		
		while (safePosition == null && moveCalcSize < 1200) {//TODO 이거 회피지역 안나오면 무한 Loop 아닐까??? moveCalcsize 때문에 무한은 안 돌테지만... 사실상 moveCalcsize 는 늘어나야 찾을수 있을텐데
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
				
			    Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(), rangedUnit.getPosition().getY() + fleeVector.getY()); // 회피지점
				//Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2, rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // 회피중간지점
				
				double risk = 0;
				risk = riskOfFleePositionAir(rangedUnit, movePosition, moveCalcSize, fleeOption.united); // 회피지점에서의 예상위험도
				
				int distanceToGoal = movePosition.getApproxDistance(fleeOption.goalPosition); // 위험도가 같을 경우 2번째 고려사항: 목표지점까지의 거리

				if (risk < 100 	&& (risk < minimumRisk || ((risk == minimumRisk) && distanceToGoal < minimumDistanceToGoal))) {
				
					safePosition =  movePosition;
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
			safePosition = fleeOption.goalPosition;
		}
		
	    return safePosition;
	}
	
	
	/**
	 * 회피지점의 위험도
	 * 
	 * @param unitType
	 * @param position
	 * @param radius
	 * @param united
	 * @return
	 */

	public static double riskOfFleePositionAir(Unit myunit, Position position, int radius, boolean united) {
		double risk = 0;
		
		List<Unit> dangerous_targets = null;
		dangerous_targets = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, position, VESSEL_SIGHT, UnitType.Terran_Missile_Turret); 
		dangerous_targets.addAll(UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, position, VESSEL_SIGHT, UnitType.Terran_Goliath));
			for (Unit enemyunits : dangerous_targets) {
				
				double inrange =  MyBotModule.Broodwar.enemy().weaponMaxRange(enemyunits.getType().airWeapon());
				double additionalrange = VESSEL_CHECK_SPEED;		
				double distance = enemyunits.getDistance(position);
				if(enemyunits.getType().airWeapon() != WeaponType.None){
					if(distance<inrange){
						risk += 100;
					}else{
						risk = (additionalrange - (distance - inrange))/additionalrange * 100;
					}
				}
			}
		return risk;
	}

}
