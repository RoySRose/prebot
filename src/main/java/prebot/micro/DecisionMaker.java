package prebot.micro;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.strategy.UnitInfo;

public class DecisionMaker {

	private static final int BACKOFF_DIST = 64;
	private static final int TOO_TOO_FAR_DISTANCE = 450;
	private TargetScoreCalculator targetScoreCalculator;

	public DecisionMaker(TargetScoreCalculator targetScoreCalculator) {
		this.targetScoreCalculator = targetScoreCalculator;
	}

	public Decision makeDecisionForSiegeMode(Unit myUnit, List<UnitInfo> euiList) {
		UnitInfo bestTargetUnitInfo = null;
		int highestScore = 0;
		
		Unit undetectedDarkTemplar = null;
		Unit tooCloseTarget = null;
		Unit tooFarTarget = null;
		int closestTooFarTargetDistance = CommonCode.INT_MAX;
		boolean targetInRangeButOutOfSight = false;
		
		for (UnitInfo eui : euiList) {
			if (!MicroUtils.canAttack(myUnit, eui)) {
				continue;
			}
			Unit enemyUnit = UnitUtils.unitInSight(eui);
			if (enemyUnit == null) {
				if (bestTargetUnitInfo == null) {
					int distanceToTarget = myUnit.getDistance(eui.getLastPosition());
					if (distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE) {
						bestTargetUnitInfo = eui;
						targetInRangeButOutOfSight = true;
					}
				}
				continue;
			}
			if (isCloseUndetectedDarkTemplar(myUnit, eui)) {
				undetectedDarkTemplar = enemyUnit;
				continue;
			}
			
			int distanceToTarget = myUnit.getDistance(enemyUnit.getPosition());
			if (!myUnit.isInWeaponRange(enemyUnit)) { // 시즈 범위안에 타겟이 없을 경우 skip
				if (distanceToTarget < Tank.SIEGE_MODE_MIN_RANGE) {
					tooCloseTarget = enemyUnit;
		        } else if (distanceToTarget > Tank.SIEGE_MODE_MAX_RANGE) {
		        	if (tooFarTarget == null || distanceToTarget < closestTooFarTargetDistance) {
			        	tooFarTarget = enemyUnit;
		        		closestTooFarTargetDistance = distanceToTarget;
		        	}
		        }
	        	continue;
			}
			
			int score = targetScoreCalculator.calculate(myUnit, eui);
			if (score > highestScore) {
				bestTargetUnitInfo = eui;
				highestScore = score;
				targetInRangeButOutOfSight = false;
			}
		}

		if (bestTargetUnitInfo != null) {
			if (targetInRangeButOutOfSight || highestScore <= 0) {
				return Decision.stop();
			} else {
				return Decision.attackUnit(bestTargetUnitInfo);
			}
		} else {
			if (undetectedDarkTemplar != null || tooCloseTarget != null) {
				return Decision.change();
			} else if (tooFarTarget != null) {
				if (closestTooFarTargetDistance > TOO_TOO_FAR_DISTANCE) {
					return Decision.change();
				} else {
					return Decision.hold();
				}
			} else {
				return Decision.attackPosition();
			}
		}
	}

	public Decision makeDecision(Unit myUnit, List<UnitInfo> euiList) {
		UnitInfo bestTargetUnitInfo = null;
		int highestScore = 0;
		for (UnitInfo eui : euiList) {
			if (!MicroUtils.canAttack(myUnit, eui)) {
				continue;
			}
			if (isCloseDangerousTarget(myUnit, eui) || isCloseUndetectedDarkTemplar(myUnit, eui)) {
				return Decision.fleeFromUnit(eui);
			}
			int score = targetScoreCalculator.calculate(myUnit, eui);
			if (score > highestScore) {
				bestTargetUnitInfo = eui;
				highestScore = score;
			}
		}
		if (bestTargetUnitInfo != null) {
			return Decision.kitingUnit(bestTargetUnitInfo);
		} else {
			return Decision.attackPosition();
		}
	}

	private boolean isCloseDangerousTarget(Unit myUnit, UnitInfo eui) {
		boolean enemyIsComplete = eui.isCompleted();
		Position enemyPosition = eui.getLastPosition();
		UnitType enemyUnitType = eui.getType();
		
		Unit enemyUnit = UnitUtils.unitInSight(eui);
		if (UnitUtils.isValidUnit(enemyUnit)) {
			enemyIsComplete = enemyUnit.isCompleted();
			enemyPosition = enemyUnit.getPosition();
			enemyUnitType = enemyUnit.getType();
		}

		// 접근하면 안되는 유닛타입인지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
		if (!enemyIsComplete || !isDangerousType(myUnit, enemyUnitType, enemyUnit)) {
			return false;
		}

		// 접근하면 안되는 거리인지 있는지 판단
		int distanceToNearEnemy = myUnit.getDistance(enemyPosition);
		int enemyWeaponRange = 0;

		if (enemyUnitType == UnitType.Terran_Bunker) {
			enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
		} else {
			if (!myUnit.isFlying()) {
				enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(enemyUnitType.airWeapon());
			} else {
				enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(enemyUnitType.groundWeapon());
			}
		}
		return distanceToNearEnemy <= enemyWeaponRange + BACKOFF_DIST;
	}

	// 접근하면 안되는 적이 있는지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
	private boolean isDangerousType(Unit myUnit, UnitType enemyUnitType, Unit enemyUnit) {
		if (myUnit.isFlying()) {
			return enemyUnitType == UnitType.Zerg_Spore_Colony
					|| enemyUnitType == UnitType.Protoss_Photon_Cannon
					|| enemyUnitType == UnitType.Terran_Bunker
					|| enemyUnitType == UnitType.Terran_Missile_Turret;
			// || (marine, goliath, dragoon, archon, hydra..) {

		} else {
			return enemyUnitType == UnitType.Zerg_Sunken_Colony
					|| enemyUnitType == UnitType.Protoss_Photon_Cannon
					|| enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode
					|| enemyUnitType == UnitType.Terran_Bunker
					|| (enemyUnitType == UnitType.Zerg_Lurker && enemyUnit != null && enemyUnit.isBurrowed() && !enemyUnit.isDetected());
			// || (saveUnitLevel >= 2 && allRangeUnitType(PreBot.Broodwar.enemy(), enemyUnitType))) {
		}
	}
	
	private boolean isCloseUndetectedDarkTemplar(Unit myUnit, UnitInfo eui) {
		if (myUnit.isFlying()) {
			return false;
		}
		Unit enemyUnit = UnitUtils.unitInSight(eui);
		if (enemyUnit == null || enemyUnit.isDetected()) {
			return false;
		}
		if (enemyUnit.getType() == UnitType.Protoss_Dark_Templar) {
			return myUnit.getDistance(enemyUnit.getPosition()) < 150;
		}
		return false;
	}
}
