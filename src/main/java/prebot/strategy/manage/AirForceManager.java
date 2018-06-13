package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Position;
import bwapi.Unit;
import bwta.BaseLocation;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.strategy.UnitInfo;

public class AirForceManager {

	public static class AirForceUnit {
		public Unit leaderUnit;
		public int currentTargetIndex = 0;
		public int targetTryCount = 0;
		public boolean directionReservse;
		public int[] retreatAngle;
		public Position leaderOrderPosition;
		
		public AirForceUnit(Unit leaderUnit) {
			this.leaderUnit = leaderUnit; // leader wraith unit ID
			this.currentTargetIndex = 0; // 현재 타겟 index
			this.targetTryCount = 0; // 공격 재시도 count
			this.directionReservse = false; // 타깃포지션 변경 역방향 여부
			this.retreatAngle = Angles.AIR_FORCE_RETREAT_LEFT;
			this.leaderOrderPosition = null;
		}

		public Position getTargetPosition() {
			List<Position> targetPositions = AirForceManager.instance.targetPositions;
			if (targetPositions.isEmpty()) {
				return null;
			} else {
				return targetPositions.get(currentTargetIndex);
			}
		}
	}

	private static AirForceManager instance = new AirForceManager();

	/// static singleton 객체를 리턴합니다
	public static AirForceManager Instance() {
		return instance;
	}

	private static final int TARGET_POSITIONS_SIZE = 4; // 최소값=3 (enemyBase, enemyExpansionBase, middlePosition1)
	private static final int MAX_TARGET_TRY_COUNT = 2; // 타깃포지션별 재공격 시도 횟수

	private List<Position> targetPositions = new ArrayList<>(); // 타깃포지션

	private BaseLocation firstBase = null; // 시작 공격 베이스
	private BaseLocation secondBase = null; // 다음 공격 베이스


	private static final int AIR_FORCE_UNIT_MERGE_DISTANCE = 150;
	private Map<Integer, AirForceUnit> airForceUnitMap = new HashMap<>(); // key : group leader unit ID
	
//	public Position getCurrentTargetPosition(int leaderUnitID) {
//		if (targetPositions.isEmpty()) {
//			return null;
//		}
//		AirForceSquadGroup airForceSquadGroup = airForceGroupMap.get(leaderUnitID);
//		return targetPositions.get(airForceSquadGroup.currentTargetIndex);
//	}

	public void retreat(int leaderUnitID) {
		AirForceUnit airForceSquadGroup = airForceUnitMap.get(leaderUnitID);

		if (airForceSquadGroup != null) {
			airForceSquadGroup.targetTryCount++;
			if (airForceSquadGroup.targetTryCount == MAX_TARGET_TRY_COUNT) {
				changeTargetIndex(airForceSquadGroup);
			}
		}
	}

	private void changeTargetIndex(AirForceUnit airForceSquadGroup) {
		airForceSquadGroup.targetTryCount = 0;
		int foundCount = TARGET_POSITIONS_SIZE;
		while (foundCount > 0) {
			airForceSquadGroup.currentTargetIndex = airForceSquadGroup.directionReservse ? airForceSquadGroup.currentTargetIndex - 1 : airForceSquadGroup.currentTargetIndex + 1;

			if (airForceSquadGroup.currentTargetIndex < 0) {
				airForceSquadGroup.currentTargetIndex = 1;
				airForceSquadGroup.directionReservse = false;

			} else if (airForceSquadGroup.currentTargetIndex >= TARGET_POSITIONS_SIZE) {
				airForceSquadGroup.currentTargetIndex = TARGET_POSITIONS_SIZE - 2;
				airForceSquadGroup.directionReservse = true;
			}

			Position nextPosition = targetPositions.get(airForceSquadGroup.currentTargetIndex);
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(nextPosition, 0, UnitUtils.enemyAirDefenseUnitType());

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
	
	public boolean isLeader(int wraithID) {
		return airForceUnitMap.get(wraithID).leaderUnit.getID() == wraithID;
	}

	/// welcome to our air force unit
	public void assignAirForceUnit(Unit wraith) {
		AirForceUnit airForceUnit = airForceUnitMap.get(wraith.getID());
		if (airForceUnit == null) {
			airForceUnit = new AirForceUnit(wraith);
			airForceUnitMap.put(wraith.getID(), airForceUnit);
		}
	}

	/// merge air force unit
	public void mergeAirForceUnit(List<Unit> wraithList) {
		// 리더의 위치를 비교하여 합칠 그룹인지 체크한다.
		Map<Integer, Integer> airForceUnitMergeMap = new HashMap<>(); // key:merge될 그룹 leaderID, value:merge할 그룹 leaderID
		Set<AirForceUnit> airForceUnitSet = new HashSet<>(airForceUnitMap.values());
		for (AirForceUnit airForceUnit : airForceUnitSet) {
			for (AirForceUnit compareForceUnit : airForceUnitSet) {
				Unit airForceLeader = airForceUnit.leaderUnit;
				Unit compareForceLeader = compareForceUnit.leaderUnit;
				if (airForceLeader.getDistance(compareForceLeader) <= AIR_FORCE_UNIT_MERGE_DISTANCE) {
					airForceUnitMergeMap.put(compareForceLeader.getID(), airForceLeader.getID());
				}
			}
		}

		// merge당하는 애들의 airForceUnit을 재설정한다.
		for (Unit wraith : wraithList) {
			Integer fromForceUnitLeaderId = airForceUnitMap.get(wraith.getID()).leaderUnit.getID();
			Integer toForceUnitLeaderId = airForceUnitMergeMap.get(fromForceUnitLeaderId);
			if (toForceUnitLeaderId != null) {
				airForceUnitMap.put(wraith.getID(), airForceUnitMap.get(toForceUnitLeaderId));
			}
		}
	}

	public AirForceUnit getAirForceUnit(int wraithID) {
		return airForceUnitMap.get(wraithID);
	}
}
