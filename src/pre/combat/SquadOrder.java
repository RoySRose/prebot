package pre.combat;

import bwapi.Position;

public class SquadOrder {
	
	// ATTACK <-> BATTLE 스위칭 관계, 전투중인 경우 상태가 BATTLE로 바뀐다.
	public enum SqaudOrderType {
		NONE, IDLE, ATTACK, BATTLE, DEFEND, HOLD
	}
	
	private SqaudOrderType type;
	private Position position;
	private int radius;
	private String status;
	
	public SquadOrder(SqaudOrderType type, Position position, int radius, String status) {
		this.type = type;
		this.position = position;
		this.radius = radius;
		this.status = status;
	}
	
	public SqaudOrderType getType() {
		return type;
	}
	public void setType(SqaudOrderType type) {
		this.type = type;
	}
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isCombatOrder() {
		return type == SqaudOrderType.ATTACK ||
			   type == SqaudOrderType.BATTLE ||
			   type == SqaudOrderType.DEFEND ||
			   type == SqaudOrderType.HOLD;
	}

	@Override
	public String toString() {
		return "SquadOrder [type=" + type + ", position=" + position + ", radius=" + radius + ", status=" + status
				+ "]";
	}
}
