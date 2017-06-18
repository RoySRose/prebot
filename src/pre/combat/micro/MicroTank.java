package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.Chokepoint;
import pre.manager.InformationManager;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

public class MicroTank extends MicroManager {

	@Override
	protected void executeMicro(List<Unit> targets) {
		if (!order.isCombatOrder()) {
			return;
		}
		
		List<Unit> tanks = getUnits();

		// figure out targets
		List<Unit> tankTargets = new ArrayList<>();
		for (Unit target : targets) {
			if (target.isVisible() && !target.isFlying()) {
				tankTargets.add(target);
			}
		}
	    
	    int siegeTankRange = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() - 32;
	    boolean haveSiege = InformationManager.Instance().selfPlayer.hasResearched(TechType.Tank_Siege_Mode);

		for (Unit tank : tanks) {
	        boolean tankNearChokepoint = false; 
	        for (Chokepoint choke : BWTA.getChokepoints()) {
	            if (choke.getCenter().getDistance(tank.getPosition()) < 64) {
	                tankNearChokepoint = true;
	                break;
	            }
	        }
			
			if (!tankTargets.isEmpty()) {
				Unit target = getTarget(tank, tankTargets);

				boolean shouldSiege = !tankNearChokepoint;

				// Don't siege to fight buildings, unless they can shoot back.
				if (target != null &&
					target.getType().isBuilding() &&
					target.getType().groundWeapon() == WeaponType.None &&
					target.getType() != UnitType.Terran_Bunker) {
					shouldSiege = false;
				}
				// Also don't siege for spider mines.
				else if (target != null && target.getType() == UnitType.Terran_Vulture_Spider_Mine) {
					shouldSiege = false;
				}

				// Unsiege for a target which is too close.
				boolean shouldUnsiege = target != null && tank.getDistance(target) < 64;

				if (target != null &&
					tank.getDistance(target) < siegeTankRange &&
					shouldSiege && tank.canSiege()) {
                    tank.siege();
                } else if ((target == null || tank.getDistance(target) > siegeTankRange || shouldUnsiege) && tank.canUnsiege()) {
                    tank.unsiege();
                }

                if (tank.isSieged()) {
                	CommandUtil.attackUnit(tank, target);
                } else {
                	MicroUtils.smartKiteTarget(tank, target);
                }
			} else {
				// if we're not near the order position
				if (tank.getDistance(order.getPosition()) > 100) {
                    if (tank.canUnsiege()) {
                        tank.unsiege();
                    } else {
                    	CommandUtil.attackMove(tank, order.getPosition());
                    }
				}
			}
		}
	}
	
	private Unit getTarget(Unit tank, List<Unit> targets) {
	    int highPriority = 0;
		int closestDist = 99999;
		Unit closestTarget = null;

	    int siegeTankRange = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() - 32;
	    List<Unit> targetsInSiegeRange = new ArrayList<>();
	    for (Unit target : targets) {
	        if (target.getDistance(tank) < siegeTankRange && !target.isFlying()) {
	            targetsInSiegeRange.add(target);
	        }
	    }

		List<Unit> newTargets = targetsInSiegeRange.isEmpty() ? targets : targetsInSiegeRange;

	    // check first for units that are in range of our attack that can cause damage
	    // choose the highest priority one from them at the lowest health
	    for (Unit target : newTargets) {
	        if (target.isFlying()) {
	            continue;
	        }

	        int distance = tank.getDistance(target);
	        int priority = getAttackPriority(tank, target);

			if (closestTarget == null || (priority > highPriority) || (priority == highPriority && distance < closestDist)) {
				closestDist = distance;
				highPriority = priority;
				closestTarget = target;
			}       
	    }
	    return closestTarget;
	}
	
	private int getAttackPriority(Unit rangedUnit, Unit target) {
		UnitType targetType = target.getType();

	    if (target.getType() == UnitType.Zerg_Larva || target.getType() == UnitType.Zerg_Egg) {
	        return 0;
	    }

	    // if the target is building something near our base something is fishy
	    Position ourBasePosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
	    if (target.getType().isWorker() && (target.isConstructing() || target.isRepairing()) && target.getDistance(ourBasePosition) < 1200) {
	        return 12;
	    }

	    if (target.getType().isBuilding() && (target.isCompleted() || target.isBeingConstructed()) && target.getDistance(ourBasePosition) < 1200) {
	        return 12;
	    }

		boolean isThreat = MicroUtils.typeCanAttackGround(targetType);    // includes bunkers
		if (target.getType().isWorker()) {
			isThreat = false;
		}

		// The most dangerous enemy units.
		if (targetType == UnitType.Terran_Siege_Tank_Tank_Mode ||
			targetType == UnitType.Terran_Siege_Tank_Siege_Mode ||
			targetType == UnitType.Protoss_High_Templar ||
			targetType == UnitType.Protoss_Reaver ||
			targetType == UnitType.Zerg_Infested_Terran) {
			return 12;
		}
		// something that can attack us or aid in combat
	    if (isThreat) {
	        return 11;
	    }
		// next priority is any unit on the ground
		if (!targetType.isBuilding()) {
	  		return 9;
		}

	    // next is special buildings
		if (targetType == UnitType.Zerg_Nydus_Canal) {
			return 6;
		}
		if (targetType == UnitType.Zerg_Spawning_Pool) {
			return 5;
		}
		if (targetType == UnitType.Protoss_Pylon || targetType == UnitType.Protoss_Templar_Archives) {
			return 5;
		}

		// any buildings that cost gas
		if (targetType.gasPrice() > 0) {
			return 4;
		}
		if (targetType.mineralPrice() > 0) {
			return 3;
		}

		// then everything else
		return 1;
	}

}


