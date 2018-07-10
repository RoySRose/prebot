package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.strategy.UnitInfo;

public class AirForceTeam {

	private static final int MAX_TARGET_TRY_COUNT = 2; // 타깃포지션별 재공격 시도 횟수
	private static final int RETREAT_TIME = 3 * TimeUtils.SECOND; // 타깃포지션별 재공격 시도 횟수
	private static final int WRAITH_EFFECTIVE_FRAME_SIZE = 25 * TimeUtils.SECOND;

	public Unit leaderUnit;
	public List<Unit> memberList = new ArrayList<>();
	public int currentTargetIndex = 0;
	public int retreatFrame = 0;
	public int targetTryCount = 0;
	public boolean directionReservse;
	public Position leaderOrderPosition;
	public int[] driveAngle;

	// for achievement
	public int damaged = 0;
	public int killed = 0;
	
	public Map<Integer, Integer> preKillCounts = new HashMap<>();
	public Map<Integer, Integer> preHitPoints = new HashMap<>();
	public int[] damagedMemory = new int[WRAITH_EFFECTIVE_FRAME_SIZE];
	public int[] killedMemory = new int[WRAITH_EFFECTIVE_FRAME_SIZE];

	public AirForceTeam(Unit leaderUnit) {
		this.leaderUnit = leaderUnit; // leader wraith unit ID
		this.currentTargetIndex = 0; // 현재 타겟 index
		this.retreatFrame = 0;
		this.targetTryCount = 0; // 공격 재시도 count
		this.directionReservse = false; // 타깃포지션 변경 역방향 여부
		this.leaderOrderPosition = null;
		this.driveAngle = Angles.AIR_FORCE_DRIVE_LEFT;
	}
	
	public int achievement() {
		int reducedHitPoints = 0;
		int killCounts = 0;
		for (Unit wraith : memberList) {
			Integer preHitPoint = preHitPoints.get(wraith.getID());
			Integer preKillCount = preKillCounts.get(wraith.getID());
			if (preHitPoint == null) {
				continue;
			}
			if (wraith.getHitPoints() < preHitPoint) {
				int reduced = preHitPoint - wraith.getHitPoints();
				reducedHitPoints += reduced;
			}
			if (wraith.getKillCount() > preKillCount) {
				int killed = wraith.getKillCount() - preKillCount;
				killCounts += killed;
			}
		}
		
		int index = TimeUtils.elapsedFrames() % WRAITH_EFFECTIVE_FRAME_SIZE;
		damaged = damaged + reducedHitPoints - damagedMemory[index];
		killed = killed + killCounts - killedMemory[index];
		
		damagedMemory[index] = reducedHitPoints;
		killedMemory[index] = killCounts;
		
		preHitPoints.clear();
		preKillCounts.clear();
		for (Unit wraith : memberList) {
			preHitPoints.put(wraith.getID(), wraith.getHitPoints());
			preKillCounts.put(wraith.getID(), wraith.getKillCount());
		}
		
		return killed * 100 - damaged;
	}

	public Position getTargetPosition() {
		List<Position> targetPositions = AirForceManager.Instance().getTargetPositions();
		if (targetPositions.isEmpty()) {
			return null;
		}
		return targetPositions.get(currentTargetIndex);
	}

	public void switchDriveAngle() {
		if (driveAngle == Angles.AIR_FORCE_DRIVE_LEFT) {
			driveAngle = Angles.AIR_FORCE_DRIVE_RIGHT;
		} else {
			driveAngle = Angles.AIR_FORCE_DRIVE_LEFT;
		}
	}

	public boolean retreating() {
		return TimeUtils.elapsedFrames(retreatFrame) < RETREAT_TIME;
	}
	
	public void retreat() {
		if (retreating()) {
			return;
		}
		retreatFrame = TimeUtils.elapsedFrames();
		targetTryCount++;
		if (targetTryCount == MAX_TARGET_TRY_COUNT) {
			changeTargetIndex();
		}
	}
	
	public boolean isInAirForceWeaponRange(Unit enemyUnit) {
		for (Unit member : memberList) {
			if (!member.isInWeaponRange(enemyUnit)) {
				return false;
			}
		}
		return true;
	}

	public void changeTargetIndex() {
		targetTryCount = 0;
		int foundCount = AirForceManager.airForceTargetPositionSize;
		while (foundCount > 0) {
			currentTargetIndex = directionReservse ? currentTargetIndex - 1 : currentTargetIndex + 1;

			if (currentTargetIndex < 0) {
				currentTargetIndex = 1;
				directionReservse = false;

			} else if (currentTargetIndex >= AirForceManager.airForceTargetPositionSize) {
				currentTargetIndex = AirForceManager.airForceTargetPositionSize - 2;
				directionReservse = true;
			}

			Position nextPosition = AirForceManager.Instance().getTargetPositions().get(currentTargetIndex);
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(nextPosition, 20, UnitUtils.enemyAirDefenseUnitType());
			foundCount = !enemyDefTowerList.isEmpty() ? foundCount - 1 : 0;
		}
	}

	@Override
	public String toString() {
		return "Team" + leaderUnit.getID() + " size=" + memberList.size() + ", achieve=" + killed + "/" + damaged;
	}

}
