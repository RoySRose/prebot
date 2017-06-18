package pre.util;

import java.util.ArrayList;
import java.util.List;

import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.Region;
import pre.MapGrid;
import pre.main.MyBotModule;

public class MicroUtils {
	
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

	public static void vultureDanceTarget(Unit vulture, Unit target){
		
		if (vulture.getType() != UnitType.Terran_Vulture ||
				vulture.getPlayer() != MyBotModule.Broodwar.self() ||
				!CommandUtil.IsValidUnit(vulture) ||
				!CommandUtil.IsValidUnit(target)) {
			MyBotModule.Broodwar.sendText("vultureDanceTarget : bad arg");
			return;
		}
		
	    int cooldown                  = UnitType.Terran_Vulture.groundWeapon().damageCooldown();
	    int latency                   = MyBotModule.Broodwar.getLatency();
	    double speed                  = UnitType.Terran_Vulture.topSpeed();
	    double range                  = UnitType.Terran_Vulture.groundWeapon().maxRange();
	    double distanceToTarget       = vulture.getDistance(target);
		double distanceToFiringRange  = Math.max(distanceToTarget - range, 0.0);
		double timeToEnterFiringRange = distanceToFiringRange / speed;
		int framesToAttack            = (int) (timeToEnterFiringRange) + 2*latency;

		// How many frames are left before we can attack?
		int currentCooldown = vulture.isStartingAttack() ? cooldown : vulture.getGroundWeaponCooldown();

		// If we can attack by the time we reach our firing range
		if (currentCooldown <= framesToAttack) {
			vulture.attack(target);
		} else { // Otherwise we cannot attack and should temporarily back off
			Position fleeVector = getKiteVector(target, vulture);
			int moveX = vulture.getPosition().getX() + fleeVector.getX();
			int moveY = vulture.getPosition().getY() + fleeVector.getY();
			Position moveToPosition = new Position(moveX, moveY);
			if (moveToPosition.isValid()) {
				vulture.rightClick(moveToPosition);
			}
		}
	}
	
	private static Position getKiteVector(Unit unit, Unit target) {
		int moveX = target.getPosition().getX() - unit.getPosition().getX();
		int moveY = target.getPosition().getY() - unit.getPosition().getY();
	    double fleeAngle = Math.atan2(moveY, moveX);
	    Position fleeVec = new Position((int)(64 * Math.cos(fleeAngle)), (int)(64 * Math.cos(fleeAngle)));
	    return fleeVec;
	}
	
	public static void smartKiteTarget(Unit rangedUnit, Unit target) {
		if (rangedUnit.getPlayer() != MyBotModule.Broodwar.self() ||
				!CommandUtil.IsValidUnit(rangedUnit) ||
				!CommandUtil.IsValidUnit(target)) {
			MyBotModule.Broodwar.sendText("smartKiteTarget : bad arg");
			return;
		}

		boolean kite = true;

		double range = rangedUnit.getType().groundWeapon().maxRange();
		if (range <= target.getType().groundWeapon().maxRange()) {
			kite = false;
		}

		// Kite if we're not ready yet: Wait for the weapon.
		double dist = rangedUnit.getDistance(target);
		double speed = rangedUnit.getType().topSpeed();
		double timeToEnter = 0.0;                      // time to reach firing range
		if (speed > .00001) {                          // don't even visit the same city as division by zero
			timeToEnter = Math.max(0.0, dist - range) / speed;
		}
		if (timeToEnter >= rangedUnit.getGroundWeaponCooldown() || target.getType().isBuilding()) {
			kite = false;
		}

		if (kite) {
			// Run away.
			int moveX = rangedUnit.getPosition().getX() - (target.getPosition().getX() - rangedUnit.getPosition().getX());
			int moveY = rangedUnit.getPosition().getY() - (target.getPosition().getY() - rangedUnit.getPosition().getY());
			Position fleePosition = new Position(moveX, moveY);
			CommandUtil.move(rangedUnit, fleePosition);
		} else {
			// Shoot.
			CommandUtil.attackUnit(rangedUnit, target);
		}
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
}
