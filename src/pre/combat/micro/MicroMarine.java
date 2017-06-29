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
						MicroUtils.smartKiteTarget(rangedUnit, target, order.getPosition(), false, true);
					} else {
						MicroUtils.smartKiteTarget(rangedUnit, target, order.getPosition(), true, true);
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

//		UnitType rangedType = rangedUnit.getType();
//		UnitType targetType = target.getType();
//		
//		int priority = 0;
//		if (rangedType == UnitType.Terran_Marine) {
//			priority = getMarinePriority();
//		}

//		// An addon other than a completed comsat is boring.
//		// TODO should also check that it is attached
//		if (targetType.isAddon() && !(targetType == BWAPI::UnitTypes::Terran_Comsat_Station && target->isCompleted()))
//		{
//			return 1;
//		}
		
		
//	    // if the target is building something near our base something is fishy
//	    BWAPI::Position ourBasePosition = BWAPI::Position(InformationManager::Instance().getMyMainBaseLocation()->getPosition());
//		if (target->getDistance(ourBasePosition) < 1200) {
//			if (target->getType().isWorker() && (target->isConstructing() || target->isRepairing()))
//			{
//				return 12;
//			}
//			if (target->getType().isBuilding())
//			{
//				return 12;
//			}
//		}
//	    
//		if (rangedType.isFlyer()) {
//			// Exceptions if we're a flyer (other than scourge, which is handled above).
//			if (targetType == BWAPI::UnitTypes::Zerg_Scourge)
//			{
//				return 12;
//			}
//		}
//		else
//		{
//			// Exceptions if we're a ground unit.
//			if (targetType == BWAPI::UnitTypes::Terran_Vulture_Spider_Mine && !target->isBurrowed() ||
//				targetType == BWAPI::UnitTypes::Zerg_Infested_Terran)
//			{
//				return 12;
//			}
//		}
//
//		if (targetType == BWAPI::UnitTypes::Protoss_High_Templar)
//		{
//			return 12;
//		}
//
//		// Short circuit: Give bunkers a lower priority to reduce bunker obsession.
//		if (targetType == BWAPI::UnitTypes::Terran_Bunker)
//		{
//			return 9;
//		}
//
//		// Threats can attack us. Exception: Workers are not threats.
//		if (UnitUtil::CanAttack(targetType, rangedType) && !targetType.isWorker())
//		{
//			// Enemy unit which is far enough outside its range is lower priority than a worker.
//			if (rangedUnit->getDistance(target) > 64 + UnitUtil::GetAttackRange(target, rangedUnit))
//			{
//				return 8;
//			}
//			return 10;
//		}
//		// Droppers are as bad as threats. They may be loaded and are often isolated and safer to attack.
//		if (targetType == BWAPI::UnitTypes::Terran_Dropship ||
//			targetType == BWAPI::UnitTypes::Protoss_Shuttle)
//		{
//			return 10;
//		}
//		// Also as bad are other dangerous things.
//		if (targetType == BWAPI::UnitTypes::Terran_Science_Vessel ||
//			targetType == BWAPI::UnitTypes::Zerg_Scourge)
//		{
//			return 10;
//		}
//		// Next are workers.
//		if (targetType.isWorker()) 
//		{
//	        if (rangedUnit->getType() == BWAPI::UnitTypes::Terran_Vulture)
//	        {
//	            return 11;
//	        }
//			// Repairing or blocking a choke makes you critical.
//			if (target->isRepairing() || unitNearChokepoint(target))
//			{
//				return 11;
//			}
//			// SCVs constructing are also important.
//			if (target->isConstructing())
//			{
//				return 10;
//			}
//
//	  		return 9;
//		}
//		// Important combat units that we may not have targeted above (esp. if we're a flyer).
//		if (targetType == BWAPI::UnitTypes::Protoss_Carrier ||
//			targetType == BWAPI::UnitTypes::Protoss_Reaver || 
//			targetType == BWAPI::UnitTypes::Terran_Siege_Tank_Tank_Mode ||
//			targetType == BWAPI::UnitTypes::Terran_Siege_Tank_Siege_Mode)
//		{
//			return 8;
//		}
//		// Nydus canal is the most important building to kill.
//		if (targetType == BWAPI::UnitTypes::Zerg_Nydus_Canal)
//		{
//			return 8;
//		}
//		// Spellcasters are as important as key buildings.
//		// Also remember to target other non-threat combat units.
//		if (targetType.isSpellcaster() ||
//			targetType.groundWeapon() != BWAPI::WeaponTypes::None ||
//			targetType.airWeapon() != BWAPI::WeaponTypes::None)
//		{
//			return 7;
//		}
//		// Templar tech and spawning pool are more important.
//		if (targetType == BWAPI::UnitTypes::Protoss_Templar_Archives)
//		{
//			return 7;
//		}
//		if (targetType == BWAPI::UnitTypes::Zerg_Spawning_Pool)
//		{
//			return 7;
//		}
//		// Don't forget the nexus/cc/hatchery.
//		if (targetType.isResourceDepot())
//		{
//			return 6;
//		}
//		if (targetType == BWAPI::UnitTypes::Protoss_Pylon)
//		{
//			return 5;
//		}
//		if (targetType == BWAPI::UnitTypes::Terran_Factory || targetType == BWAPI::UnitTypes::Terran_Armory)
//		{
//			return 5;
//		}
//		// Downgrade unfinished/unpowered buildings, with exceptions.
//		if (targetType.isBuilding() &&
//			(!target->isCompleted() || !target->isPowered()) &&
//			!(	targetType.isResourceDepot() ||
//				targetType.groundWeapon() != BWAPI::WeaponTypes::None ||
//				targetType.airWeapon() != BWAPI::WeaponTypes::None ||
//				targetType == BWAPI::UnitTypes::Terran_Bunker))
//		{
//			return 2;
//		}
//		if (targetType.gasPrice() > 0)
//		{
//			return 4;
//		}
//		if (targetType.mineralPrice() > 0)
//		{
//			return 3;
//		}
//		// Finally everything else.
//		return 1;
	}

}
