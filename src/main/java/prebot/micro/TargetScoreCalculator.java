package prebot.micro;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.strategy.UnitInfo;

public class TargetScoreCalculator {

	private int unitWeight;
	private int splashWeight;
	private int distanceWeight;
	private int hitpointWeight;

	public TargetScoreCalculator(int unitWeight, int splashWeight, int distanceWeight, int hitpointWeight) {
		this.unitWeight = unitWeight;
		this.splashWeight = splashWeight;
		this.distanceWeight = distanceWeight;
		this.hitpointWeight = hitpointWeight;
	}

	public int calculate(Unit unit, UnitInfo eui) {
		boolean enemyIsComplete = eui.isCompleted();
		Position enemyPosition = eui.getLastPosition();
		UnitType enemyUnitType = eui.getType();
		
		Unit enemyUnit = UnitUtils.unitInSight(eui);
		if (UnitUtils.isValidUnit(enemyUnit)) {
			enemyIsComplete = enemyUnit.isCompleted();
			enemyPosition = enemyUnit.getPosition();
			enemyUnitType = enemyUnit.getType();
		}
		
		
		// 우선순위 점수 : 유닛 우선순위 맵
		int priorityScore = TargetPriority.getPriority(unit.getType(), enemyUnitType);
		int distanceScore = 0; // 거리 점수 : 최고점수 100점. 멀수록 점수 높음.
		int hitPointScore = 0; // HP 점수 : 최고점수 50점. HP가 많을 수록 점수 낮음.
		int specialScore = 0; // 특별 점수 : 탱크앞에 붙어있는 밀리유닛 +100점

		if (enemyUnit != null) {
			if (unit.isInWeaponRange(enemyUnit)) {
				distanceScore = 100;
			}
			distanceScore -= unit.getDistance(enemyUnit.getPosition()) / 5;
			hitPointScore = 50 - enemyUnit.getHitPoints() / 10;

			// 시즈에 붙어있는 밀리유닛 : +200점
			if (enemyUnitType.groundWeapon().maxRange() <= Tank.SIEGE_MODE_MIN_RANGE) {
				List<Unit> nearSiegeMode = UnitUtils.getUnitsInRadius(PlayerRange.ENEMY, enemyPosition, Tank.SIEGE_MODE_MIN_RANGE, UnitType.Terran_Siege_Tank_Siege_Mode);
				if (!nearSiegeMode.isEmpty()) {
					specialScore += 100;
				}
			}
			// 클로킹 유닛 : -1000점
			if (!enemyUnit.isDetected() && enemyUnitType == UnitType.Protoss_Dark_Templar) {
				specialScore -= 1000;
			}
			if (enemyUnitType == UnitType.Protoss_Interceptor) {
				specialScore -= 1000;
			}

		} else {
			distanceScore -= unit.getDistance(eui.getLastPosition()) / 5;
			hitPointScore = 50 - eui.getLastHealth() / 10;
			specialScore = -100;
			if (eui.getType().isCloakable()) {
				specialScore -= 2000;
			}
		}
		
		return priorityScore + distanceScore + hitPointScore + specialScore;
	}

}
