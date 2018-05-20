package prebot.micro;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.information.UnitInfo;
import prebot.common.code.ConfigForMicro.Flee;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;

public class DecisionMaker {
	
	private TargetScoreCalculator targetScoreCalculator;

	public DecisionMaker(TargetScoreCalculator targetScoreCalculator) {
		this.targetScoreCalculator = targetScoreCalculator;
	}

	public Decision makeDecision(Unit myUnit, List<UnitInfo> euiList) {

		UnitInfo bestTargetUnitInfo = null;
		int highestScore = 0;
		for (UnitInfo eui : euiList) {
			if (isDangerousTarget(myUnit, eui)) {
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
		}
		
		return Decision.attackPosition();
	}

	private boolean isDangerousTarget(Unit myUnit, UnitInfo eui) {
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
		if (!enemyIsComplete || !isDangerousTarget(myUnit, enemyUnitType, enemyUnit)) {
			return false;
		}

		// 접근하면 안되는 거리인지 있는지 판단
		int distanceToNearEnemy = myUnit.getDistance(enemyPosition);
		int enemyWeaponRange = 0;

		if (enemyUnitType == UnitType.Terran_Bunker) {
			enemyWeaponRange = Prebot.Game.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
		} else {
			if (!myUnit.isFlying()) {
				enemyWeaponRange = Prebot.Game.enemy().weaponMaxRange(enemyUnitType.airWeapon());
			} else {
				enemyWeaponRange = Prebot.Game.enemy().weaponMaxRange(enemyUnitType.groundWeapon());
			}
		}
		return distanceToNearEnemy <= enemyWeaponRange + Flee.BACKOFF_DIST;
	}

	// 접근하면 안되는 적이 있는지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
	private boolean isDangerousTarget(Unit myUnit, UnitType enemyUnitType, Unit enemyUnit) {
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
}
