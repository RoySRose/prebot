package prebot.micro.targeting;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.common.constant.CommonCode;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceManager.StrikeLevel;

public class WraithTargetCalculator extends TargetScoreCalculator {

	@Override
	public int calculate(Unit unit, UnitInfo eui) {
		Unit unitInSight = UnitUtils.unitInSight(eui);
		if (unitInSight == null) {
			return CommonCode.NONE;
		}

		if (unitInSight.getType().airWeapon() != WeaponType.None || unitInSight.getType() == UnitType.Protoss_Carrier) {
			return caculateForEnemy(unit, unitInSight);
		} else {
			return caculateForFeed(unit, unitInSight);
		}
	}

	private int caculateForEnemy(Unit unit, Unit enemyUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

	private int caculateForFeed(Unit unit, Unit enemyUnit) {
		int strikeLevel = AirForceManager.Instance().getStrikeLevel();
		int score = criticalHighestScore(enemyUnit);
		if (score == CommonCode.NONE && strikeLevel < StrikeLevel.CRITICAL_SPOT) {
			score = soreHighestScore(enemyUnit);
			if (score == CommonCode.NONE && strikeLevel < StrikeLevel.SORE_SPOT) {
				score = possibleHighestScore(enemyUnit);
			}
		}
		return score;
	}

	// CRITICAL_SPOT = 3; // 때리면 죽는 곳 공격 (터렛건설중인 SCV, 아모리 건설중인 SCV, 엔지니어링베이 건설중인 SCV)
	private int criticalHighestScore(Unit enemyUnit) {
		if (enemyUnit.getType().isWorker()) {
			if (enemyUnit.isConstructing()) {
				UnitType constructingBuilding = enemyUnit.getOrderTarget().getType();
				if (constructingBuilding == UnitType.Terran_Missile_Turret) {
					return 30;
				} else if (constructingBuilding == UnitType.Terran_Armory) {
					return 29;
				} else if (constructingBuilding == UnitType.Terran_Engineering_Bay) {
					return 28;
				}
			}
		}
		return CommonCode.NONE;
	}

	// SORE_SPOT = 2; // 때리면 아픈 곳 공격 (커맨드센터건설중인 SCV, 팩토리 건설중인 SCV, 뭔가 건설중인 SCV, 체력이 적은 SCV, 가까운 SCV, 탱크)
	private int soreHighestScore(Unit enemyUnit) {
		if (enemyUnit.getType().isWorker()) {
			if (enemyUnit.isConstructing()) {
				UnitType constructingBuilding = enemyUnit.getOrderTarget().getType();
				if (constructingBuilding == UnitType.Terran_Command_Center) {
					return 27;
				} else if (constructingBuilding == UnitType.Terran_Factory) {
					return 26;
				} else {
					return 25;
				}
			}
			return 24;
		}
		if (enemyUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
			return 23;
		}
		if (enemyUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
			return 22;
		}
		return CommonCode.NONE;
	}

	// POSSIBLE_SPOT = 1; // 때릴 수 있는 곳 공격 (벌처, 건물 등 잡히는 대로)
	private int possibleHighestScore(Unit enemyUnit) {
		if (enemyUnit.getType() == UnitType.Terran_Supply_Depot) {
			return 21;
		} else {
			return 20;
		}
	}

	// enemyUnit.isCloaked()
	// enemyUnit.isDefenseMatrixed()
}
