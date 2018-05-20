package prebot.brain.information;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/// 해당 Unit의 ID, UnitType, 소속 Player, HitPoint, lastPosition, completed(건물이 완성된 것인지) 등을 저장해두는 자료구조<br>
/// 적군 유닛의 경우 아군 시야 내에 있지 않아 invisible 상태가 되었을 때 정보를 조회할수도 없어지고 파악했던 정보도 유실되기 때문에 별도 자료구조가 필요합니다
public class UnitInfo {

	private Unit unit;
	private int unitID;
	private Player player;
	private UnitType type;
	private Position lastPosition;
	private boolean completed;
	private int lastHitPoints;
	private int lastShields;
	private int hitPointsReduced;
	private int shieldsReduced;
	private int updateFrame;

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

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public int getUnitID() {
		return unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public UnitType getType() {
		return type;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public Position getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(Position lastPosition) {
		this.lastPosition = lastPosition;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getLastHitPoints() {
		return lastHitPoints;
	}

	public void setLastHitPoints(int lastHitPoints) {
		this.lastHitPoints = lastHitPoints;
	}

	public int getLastShields() {
		return lastShields;
	}

	public void setLastShields(int lastShields) {
		this.lastShields = lastShields;
	}

	public int getHitPointsReduced() {
		return hitPointsReduced;
	}

	public void setHitPointsReduced(int hitPointsReduced) {
		this.hitPointsReduced = hitPointsReduced;
	}

	public int getShieldsReduced() {
		return shieldsReduced;
	}

	public void setShieldsReduced(int shieldsReduced) {
		this.shieldsReduced = shieldsReduced;
	}

	public int getUpdateFrame() {
		return updateFrame;
	}

	public void setUpdateFrame(int updateFrame) {
		this.updateFrame = updateFrame;
	}

	@Override
	public String toString() {
		return "UnitInfo [unit=" + unit + ", unitID=" + unitID + ", player=" + player + ", type=" + type + ", lastPosition=" + lastPosition + ", completed=" + completed
				+ ", lastHitPoints=" + lastHitPoints + ", lastShields=" + lastShields + ", hitPointsReduced=" + hitPointsReduced + ", shieldsReduced=" + shieldsReduced
				+ ", updateFrame=" + updateFrame + "]";
	}
};