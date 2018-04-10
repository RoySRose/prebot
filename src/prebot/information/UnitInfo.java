package prebot.information;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/// 해당 Unit의 ID, UnitType, 소속 Player, HitPoint, lastPosition, completed(건물이 완성된 것인지) 등을 저장해두는 자료구조<br>
/// 적군 유닛의 경우 아군 시야 내에 있지 않아 invisible 상태가 되었을 때 정보를 조회할수도 없어지고 파악했던 정보도 유실되기 때문에 별도 자료구조가 필요합니다
public class UnitInfo {

	public Unit unit;
	public int unitID;
	public Player player;
	public UnitType type;
	public Position lastPosition;
	public boolean completed;
	public int lastHitPoints;
	public int lastShields;
	public int hitPointsReduced;
	public int shieldsReduced;
	public int updateFrame;

	public UnitInfo() {
		unitID = 0;
		unit = null;
		player = null;
		type = UnitType.None;
		lastPosition = Position.None;
		completed = false;
		lastHitPoints = 0;
		lastShields = 0;
		hitPointsReduced = 0;
		shieldsReduced = 0;
		updateFrame = 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UnitInfo)) {
			return false;
		}

		return this.unitID == ((UnitInfo) o).unitID;
	}

	@Override
	public String toString() {
		return "UnitInfo [unit=" + unit + ", unitID=" + unitID + ", player=" + player + ", type=" + type + ", lastPosition=" + lastPosition + "]";
	}
};