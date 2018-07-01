package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.strategy.UnitInfo;

public class AirForceTeam {

	private static final int MAX_TARGET_TRY_COUNT = 2; // 타깃포지션별 재공격 시도 횟수

	public Unit leaderUnit;
	public List<Unit> memberList = new ArrayList<>();
	public int currentTargetIndex = 0;
	public int targetTryCount = 0;
	public boolean directionReservse;
	public int[] driveAngle;
	public Position leaderOrderPosition;

	public AirForceTeam(Unit leaderUnit) {
		this.leaderUnit = leaderUnit; // leader wraith unit ID
		this.currentTargetIndex = 0; // 현재 타겟 index
		this.targetTryCount = 0; // 공격 재시도 count
		this.directionReservse = false; // 타깃포지션 변경 역방향 여부
		this.driveAngle = Angles.AIR_FORCE_DRIVE_LEFT;
		this.leaderOrderPosition = null;
	}
	
	public void setMemberList(List<Unit> memberList) {
		this.memberList = memberList;
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

	public void retreat() {
		targetTryCount++;
		if (targetTryCount == MAX_TARGET_TRY_COUNT) {
			changeTargetIndex();
		}
	}

	public void changeTargetIndex() {
		targetTryCount = 0;
		int foundCount = AirForceManager.TARGET_POSITIONS_SIZE;
		while (foundCount > 0) {
			currentTargetIndex = directionReservse ? currentTargetIndex - 1 : currentTargetIndex + 1;

			if (currentTargetIndex < 0) {
				currentTargetIndex = 1;
				directionReservse = false;

			} else if (currentTargetIndex >= AirForceManager.TARGET_POSITIONS_SIZE) {
				currentTargetIndex = AirForceManager.TARGET_POSITIONS_SIZE - 2;
				directionReservse = true;
			}

			Position nextPosition = AirForceManager.Instance().getTargetPositions().get(currentTargetIndex);
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(nextPosition, 20, UnitUtils.enemyAirDefenseUnitType());
			foundCount = !enemyDefTowerList.isEmpty() ? foundCount - 1 : 0;
		}
	}

	@Override
	public String toString() {
		return "Team" + leaderUnit.getID() + " size=" + memberList.size();
	}

}
