package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;

public class AirForceManager {

	private static AirForceManager instance = new AirForceManager();

	/// static singleton 객체를 리턴합니다
	public static AirForceManager Instance() {
		return instance;
	}

	private static final int MAX_TARGET_TRY_COUNT = 2; // 타깃포지션별 재공격 시도 횟수
	private int targetTryCount = 0;
	private int currentTargetIndex = 0;
	private boolean directionReservse = false; // 타깃포지션 변경 역방향 여부

	private static final int TARGET_POSITIONS_SIZE = 4; // 최소값=3 (enemyBase, enemyExpansionBase, middlePosition1)
	private List<Position> targetPositions = new ArrayList<>(); // 타깃포지션

	private BaseLocation firstBase = null; // 시작 공격 베이스
	private BaseLocation secondBase = null; // 다음 공격 베이스

	public Position getCurrentTargetPosition() {
		if (targetPositions.isEmpty()) {
			return null;
		}
		return targetPositions.get(currentTargetIndex);
	}
	
	public void retreat() {
		targetTryCount++;

		if (targetTryCount == MAX_TARGET_TRY_COUNT) {
			changeTargetIndex();
		}
	}

	public void changeTargetIndex() {
		targetTryCount = 0;
		int foundCount = TARGET_POSITIONS_SIZE;
		while (foundCount > 0) {
			currentTargetIndex = directionReservse ? currentTargetIndex - 1 : currentTargetIndex + 1;

			if (currentTargetIndex < 0) {
				currentTargetIndex = 1;
				directionReservse = false;
				
			} else if (currentTargetIndex >= TARGET_POSITIONS_SIZE) {
				currentTargetIndex = TARGET_POSITIONS_SIZE - 2;
				directionReservse = true;
			}
			
			Position nextPosition = targetPositions.get(currentTargetIndex);
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(nextPosition, 0,
					UnitType.Terran_Missile_Turret, UnitType.Protoss_Photon_Cannon, UnitType.Zerg_Spore_Colony);
			
			foundCount = !enemyDefTowerList.isEmpty() ? foundCount - 1 : 0;
		}
	}

	public void update() {
		BaseLocation enemyBase = InfoUtils.enemyBase();
		BaseLocation enemyFirstExpansion = InfoUtils.enemyFirstExpansion();
		if (enemyBase == null || enemyFirstExpansion == null) {
			return;
		}

		if (positionReset()) {
			firstBase = secondBase = null;
			targetPositions.clear();
			setTargetPositions();
		}
	}

	private boolean positionReset() {
		if (firstBase == null || secondBase == null || targetPositions.isEmpty()) {
			return true;
		}
		boolean enemyBaseFirstCase = InfoUtils.enemyBase().equals(firstBase) && InfoUtils.enemyFirstExpansion().equals(secondBase);
		boolean enemyExpansionFirstCase = InfoUtils.enemyFirstExpansion().equals(firstBase) && InfoUtils.enemyBase().equals(secondBase);
		return !enemyBaseFirstCase && !enemyExpansionFirstCase;

	}

	/// 첫번째 공격 base, 마지막 공격 base를 지정하고, 중간 포지션을 설정한다.
	private void setTargetPositions() {
		
		double airDistanceToBase = InfoUtils.myBase().getAirDistance(InfoUtils.enemyBase());
		double airDistanceToExpansion = InfoUtils.myBase().getAirDistance(InfoUtils.enemyFirstExpansion());

		if (airDistanceToBase < airDistanceToExpansion) {
			firstBase = InfoUtils.enemyBase();
			secondBase = InfoUtils.enemyFirstExpansion();
		} else {
			firstBase = InfoUtils.enemyFirstExpansion();
			secondBase = InfoUtils.enemyBase();
		}

		int vectorX = secondBase.getPosition().getX() - firstBase.getPosition().getX();
		int vectorY = secondBase.getPosition().getY() - firstBase.getPosition().getY();

		double vectorXSegment = vectorX / (TARGET_POSITIONS_SIZE - 1);
		double vectorYSegment = vectorY / (TARGET_POSITIONS_SIZE - 1);

		targetPositions.add(0, firstBase.getPosition());
		for (int index = 1; index < TARGET_POSITIONS_SIZE - 1; index++) {
			int resultX = firstBase.getPosition().getX() + (int) (vectorXSegment * (index));
			int resultY = firstBase.getPosition().getY() + (int) (vectorYSegment * (index));

			targetPositions.add(index, new Position(resultX, resultY));
		}
		targetPositions.add(TARGET_POSITIONS_SIZE - 1, secondBase.getPosition());
	}
}
