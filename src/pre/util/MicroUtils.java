package pre.util;

import java.util.ArrayList;
import java.util.List;

import bwapi.DamageType;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.Region;
import pre.MapGrid;
import pre.main.MyBotModule;
import pre.manager.InformationManager;

public class MicroUtils {
	
//	private static final int[] FLEE_ANGLE = { -15, 15, -30, 30, -45, 45, -60, 60 };
	private static final int[] FLEE_ANGLE = { -10, +10, -20, +20 -30, +30, -40, +40, -50, +50
			, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
    
	private static final int LATENCY = MyBotModule.Broodwar.getLatency();
	
	private static boolean vultureSpeedUpgrade = false;
	
	public static boolean smartScan(Position targetPosition) {
		if (targetPosition.isValid()) {
			MyBotModule.Broodwar.sendText("SmartScan : bad position");
			return false;
		}
		if (MapGrid.Instance().scanIsActiveAt(targetPosition)) {
			return false;
		}

		// Choose the comsat with the highest energy.
		// If we're not terran, we're unlikely to have any comsats....
		int maxEnergy = 49;      // anything greater is enough energy for a scan
		Unit comsat = null;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Comsat_Station &&
				unit.getEnergy() > maxEnergy &&
				unit.canUseTech(TechType.Scanner_Sweep, targetPosition)) {
				maxEnergy = unit.getEnergy();
				comsat = unit;
			}
		}

		if (comsat != null) {
			MapGrid.Instance().scanAtPosition(targetPosition);
			return comsat.useTech(TechType.Scanner_Sweep, targetPosition);
		}

		return false;
	}
	
	public static void groupKite() {}
	
	public static void smartKiteTarget(Unit rangedUnit, Unit target, Position goalPosition, boolean cooltimeAlwaysAttack, boolean united) {
		if (rangedUnit.getPlayer() != MyBotModule.Broodwar.self() ||
				!CommandUtil.IsValidUnit(rangedUnit) ||
				!CommandUtil.IsValidUnit(target)) {
			MyBotModule.Broodwar.sendText("smartKiteTarget : bad arg");
			return;
		}

		WeaponType rangedUnitWeapon = target.isFlying() ? rangedUnit.getType().airWeapon() : rangedUnit.getType().groundWeapon();
		WeaponType targetWeapon = rangedUnit.isFlying() ? target.getType().airWeapon() : target.getType().groundWeapon();
		
		if (rangedUnitWeapon.maxRange() <= targetWeapon.maxRange()) {
			CommandUtil.attackUnit(rangedUnit, target);
		} else {
			double distanceToAttack = rangedUnit.getDistance(target) - rangedUnitWeapon.maxRange(); // 거리(pixel)
			int currentCooldown = rangedUnit.isStartingAttack() ? rangedUnitWeapon.damageCooldown() // // 쿨타임시간(frame)
					: (target.isFlying() ? rangedUnit.getAirWeaponCooldown() : rangedUnit.getGroundWeaponCooldown());
			int timeToCatch = (int) (distanceToAttack / rangedUnit.getType().topSpeed()); // 상대를 잡기위해 걸리는 시간 (frame)
			
			// 공격하기 위해 전진해야 하는 경우, 명령에 대한 지연시간(latency)을 더한다.
			// 적과 너무 가까워 후퇴해야 하는 경우, 지연시간을 더하면 도망을 더 늦게갈 수도 있다. 
//			if (distanceToAttack > 0) // TODO 조절가능
			timeToCatch += LATENCY * 2; // 명령에 대한 지연시간(latency)을 더한다.

			boolean survivalInstinct = !killedByNShot(rangedUnit, target, 1) && killedByNShot(target, rangedUnit, 2);
			if ((!survivalInstinct && cooltimeAlwaysAttack && currentCooldown == 0) || currentCooldown <= timeToCatch) { // || target.getType().isBuilding()) { TODO 빌딩이면 때리는 로직 사용?
				
				System.out.println("survivalInstinct:" + survivalInstinct);
				System.out.println("currentCooldown & timeToCatch : " + currentCooldown + ", " + timeToCatch);
				
				// TODO P컨 잘안되서 일단 주석
//			    double rad = Math.atan2(vulture.getPosition().getY() - target.getPosition().getY()
//			    		, vulture.getPosition().getX() - target.getPosition().getX());
//			    Position enemyFrontVec = new Position((int)(5 * Math.cos(rad)), (int)(5 * Math.sin(rad)));
//				int pConX = target.getPosition().getX() + enemyFrontVec.getX();
//				int pConY = target.getPosition().getY() + enemyFrontVec.getY();
//				Position pConPos = new Position(pConX, pConY);
//				CommandUtil.patrolMove(vulture, pConPos);
				CommandUtil.attackUnit(rangedUnit, target);
			} else {
				double rangedUnitSpeed = rangedUnit.getType().topSpeed() * 24.0; // 1초(24frame)에 몇 pixel가는지
				if (vultureSpeedUpgrade && rangedUnit.getType() == UnitType.Terran_Vulture) {
					rangedUnitSpeed += 3.0 * 24.0; // 벌처 업그레이드 된 스피드 안나온다. 걍 frame당 3pixel 더 간다고 치자.
				} else {
					if (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) > 0) {
						vultureSpeedUpgrade = true;
						MyBotModule.Broodwar.sendText("Ion Thrusters Upgraded!");
					}
				}
				
				Position fleePosition = getFleePosition(target, rangedUnit, (int) rangedUnitSpeed, goalPosition, survivalInstinct? false : united);
				CommandUtil.rightClick(rangedUnit, fleePosition);
			}
		}
	}
	
	private static Position getFleePosition(Unit target, Unit unit, int moveDistPerSec, Position goalPosition, boolean united) {
		int reverseX = unit.getPosition().getX() - target.getPosition().getX(); // 타겟과 반대로 가는 x양
		int reverseY = unit.getPosition().getY() - target.getPosition().getY(); // 타겟과 반대로 가는 y양
	    double fleeRadian = Math.atan2(reverseY, reverseX);
	    
		Position safePosition = null;
		int minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;
		
		int moveCalcSize = moveDistPerSec;
		while (safePosition == null && moveCalcSize > 0) {
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
			    Position fleeVector = new Position((int)(200 * Math.cos(fleeRadian)), (int)(moveCalcSize * Math.sin(fleeRadian)));
				Position movePosition = new Position(unit.getPosition().getX() + fleeVector.getX(), unit.getPosition().getY() + fleeVector.getY()); // 이동지점
				Position middlePosition = new Position(unit.getPosition().getX() + fleeVector.getX() / 2, unit.getPosition().getY() + fleeVector.getY() / 2); // 중간지점
				
//				int enemyCount = MapGrid.Instance().getUnitsNear(movePosition, moveCalcSize, false, true).size();
				int risk = riskOfFleePosition(unit.getType(), movePosition, moveCalcSize, united);
				int distanceToGoal = movePosition.getApproxDistance(goalPosition);

				if (movePosition.isValid() && BWTA.getRegion(movePosition) != null
						&& middlePosition.isValid() && BWTA.getRegion(middlePosition) != null
						&& MyBotModule.Broodwar.isWalkable(movePosition.getX() / 8, movePosition.getY() / 8)
						&& (risk < minimumRisk || (risk == minimumRisk && distanceToGoal < minimumDistanceToGoal))) {
					
					safePosition =  movePosition;
					minimumRisk = risk;
					minimumDistanceToGoal = distanceToGoal;
				} else {
			    	fleeRadian = rotate(fleeRadian, FLEE_ANGLE[i]);
				}
		    }
			if (safePosition == null) {
		    	MyBotModule.Broodwar.sendText("safe is null : " + moveCalcSize);
		    	moveCalcSize -= 50;
			}
		}
		if (safePosition == null) {
			safePosition = goalPosition;
		}
		
	    return safePosition;
	}
	
	public static int riskOfFleePosition(UnitType unitType, Position position, int radius, boolean united) {
		int risk = 0;
		List<Unit> unitsInRadius = MyBotModule.Broodwar.getUnitsInRadius(position, radius);
		for (Unit u : unitsInRadius) {
			if (u.getPlayer() == InformationManager.Instance().enemyPlayer) { // 적군인 경우
				if (MyBotModule.Broodwar.getDamageFrom(u.getType(), unitType) > 0) { // 적군이 공격할 수 있을 때
					if (u.getType().isWorker()) {
						risk += 1;
					} else if (u.getType().isBuilding()) {
						risk += 15;
					} else if (!u.getType().isFlyer()) {
						risk += 10;
					} else {
						risk += 5;
					}
				} else { // 적군이 공격할 수 없을 때
					if (u.getType().isBuilding()) {
						risk += 5;
					} else if (!u.getType().isFlyer()) {
						risk += 3;
					} else {
						risk += 1;
					}
				}
			} else if (u.getPlayer() == InformationManager.Instance().selfPlayer) { // 아군인 경우
				
				if (!u.getType().isFlyer()) {
					risk += united ? -3 : 3;
				} else {
					risk += united ? -1 : 1;
				}
			} else { // 중립(미네랄, 가스 등)
				risk += 1;
			}
		}
		return risk;
	}

	// * 참조사이트: http://yc0345.tistory.com/45
	// 공식: radian = (π / 180) * 각도 
	// -> 각도 = (radian * 180) / π
	// -> 회원 radian = (π / 180) * ((radian * 180) / π + 회전각)
	private static double rotate(double radian, int angle) { 
		double rotatedRadian = (Math.PI / 180) * ((radian * 180 / Math.PI) + angle);
		return rotatedRadian;
	}
	
	public static boolean isUnitContainedInUnitSet(Unit unit, List<Unit> unitSet) {
		for (Unit u : unitSet) {
			if (u.getID() == unit.getID())
				return true;
		}
		return false;
	}
	
	public static boolean addUnitToUnitSet(Unit unit, List<Unit> unitSet) {
		if (!isUnitContainedInUnitSet(unit, unitSet)) {
			return unitSet.add(unit);
		}
		return false;
	}
	
	public static List<Unit> getUnitsInRegion(Region region, Player player) {
		List<Unit> units = new ArrayList<>();
	    for (Unit unit : player.getUnits()) {
	        if (region == BWTA.getRegion(unit.getPosition())) {
	            units.add(unit);
	        }
	    }
		return units;
	}
	
	public static boolean typeCanAttackGround(UnitType attacker) {
		return attacker.groundWeapon() != WeaponType.None ||
				attacker == UnitType.Terran_Bunker ||
				attacker == UnitType.Protoss_Carrier ||
				attacker == UnitType.Protoss_Reaver;
	}
	
	public static boolean killedByNShot(Unit attacker, Unit target, int shot) {
		UnitType attackerType = attacker.getType();
		UnitType targetType = target.getType();
		
		int multiply = shot;
		if (attackerType == UnitType.Protoss_Zealot
				|| attackerType == UnitType.Terran_Goliath && targetType.isFlyer()) {
			multiply *= 2;
		}
		
		int damageExpected = MyBotModule.Broodwar.getDamageFrom(attackerType, targetType, attacker.getPlayer(), target.getPlayer()) * multiply;
		
		int targetHitPoints = target.getHitPoints();
		
		if (targetType.maxShields() == 0 && !targetType.regeneratesHP()) {
			if (damageExpected >= targetHitPoints) {
				return true;
			}
		}
		
		int spareDamage = damageExpected - targetHitPoints;
		if (spareDamage > 0) {
			int targetShields = target.getShields();
			if (targetShields == 0) {
				return true;
			}
			
			DamageType explosionType = getDamageType(attacker, target);
			UnitSizeType targetUnitSize = getUnitSize(targetType);
			
			if (explosionType == DamageType.Explosive) {
				if (targetUnitSize == UnitSizeType.Small) {
					spareDamage *= 2;
				} else if (targetUnitSize == UnitSizeType.Medium) {
					spareDamage *= 4/3;
				}
			} else if (explosionType == DamageType.Concussive) {
				if (targetUnitSize == UnitSizeType.Medium) {
					spareDamage *= 2;
				} else if (targetUnitSize == UnitSizeType.Large) {
					spareDamage *= 4;
				}
			}
			
			if (spareDamage > targetShields + target.getPlayer().getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields)) {
				return true;
			}
		}
		
		return false;
	}
	
	// weapon.damageType() bwapi 오류발생하여 구현함.
	private static DamageType getDamageType(Unit attacker, Unit target) {
		UnitType attackerType = attacker.getType();
		WeaponType weapon = target.isFlying()? attackerType.airWeapon() : attackerType.groundWeapon();
		if (weapon == null || weapon == WeaponType.Unknown) {
			MyBotModule.Broodwar.sendText("no weapon. no war.");
			return DamageType.None;
		}
		
		if (attackerType == UnitType.Terran_Siege_Tank_Tank_Mode
			|| attackerType == UnitType.Terran_Siege_Tank_Siege_Mode) {
			return DamageType.Explosive;
		}
		if (attackerType == UnitType.Terran_Vulture
				|| attackerType == UnitType.Terran_Firebat
				|| attackerType == UnitType.Terran_Goliath) {
			return DamageType.Concussive;
		}
		if (attackerType == UnitType.Terran_Goliath
				|| attackerType == UnitType.Terran_Wraith
				|| attackerType == UnitType.Terran_Valkyrie) {
			if (weapon.targetsAir()) {
				return DamageType.Explosive;
			} 
		}
		return DamageType.Normal;
	}
	
	// unitType.size() bwapi 오류발생하여 구현함.
	private static UnitSizeType getUnitSize(UnitType unitType) {
		if (unitType.isBuilding()) {
			return UnitSizeType.Large;
		} else if (unitType.isWorker()
				|| unitType == UnitType.Terran_Marine
				|| unitType == UnitType.Terran_Firebat
				|| unitType == UnitType.Terran_Ghost
				|| unitType == UnitType.Terran_Medic
				|| unitType == UnitType.Protoss_Zealot
				|| unitType == UnitType.Protoss_High_Templar
				|| unitType == UnitType.Hero_Dark_Templar
				|| unitType == UnitType.Protoss_Observer
				|| unitType == UnitType.Zerg_Larva
				|| unitType == UnitType.Zerg_Zergling
				|| unitType == UnitType.Zerg_Infested_Terran
				|| unitType == UnitType.Zerg_Broodling
				|| unitType == UnitType.Zerg_Scourge
				|| unitType == UnitType.Zerg_Mutalisk
				) {
			return UnitSizeType.Small;
		} else if (unitType == UnitType.Terran_Vulture
				|| unitType == UnitType.Protoss_Corsair
				|| unitType == UnitType.Zerg_Hydralisk
				|| unitType == UnitType.Zerg_Defiler
				|| unitType == UnitType.Zerg_Queen
				|| unitType == UnitType.Zerg_Lurker
				) {
			return UnitSizeType.Medium;
		} else if (unitType == UnitType.Terran_Siege_Tank_Tank_Mode
				|| unitType == UnitType.Terran_Siege_Tank_Siege_Mode
				|| unitType == UnitType.Terran_Goliath
				|| unitType == UnitType.Terran_Wraith
				|| unitType == UnitType.Terran_Dropship
				|| unitType == UnitType.Terran_Science_Vessel
				|| unitType == UnitType.Terran_Battlecruiser
				|| unitType == UnitType.Terran_Valkyrie
				|| unitType == UnitType.Protoss_Dragoon
				|| unitType == UnitType.Protoss_Archon
				|| unitType == UnitType.Protoss_Reaver
				|| unitType == UnitType.Protoss_Shuttle
				|| unitType == UnitType.Protoss_Scout
				|| unitType == UnitType.Protoss_Carrier
				|| unitType == UnitType.Protoss_Arbiter
				|| unitType == UnitType.Zerg_Overlord
				|| unitType == UnitType.Zerg_Guardian
				|| unitType == UnitType.Zerg_Devourer
				) {
			return UnitSizeType.Large;
		}
		
		return UnitSizeType.Small;
	}
}
