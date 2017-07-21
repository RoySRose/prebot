package pre.combat;

import bwapi.Position;

public class SquadOrder {
	
	// ATTACK <-> BATTLE 스위칭 관계, 전투중인 경우 상태가 BATTLE로 바뀐다.
	public enum SquadOrderType {
		NONE, IDLE, ATTACK, DEFEND, HOLD, WATCH, CHECK
	}
	
	private SquadOrderType type;
	private Position position;
	private int radius;
	private String status;
	
	public SquadOrder(SquadOrderType type, Position position, int radius, String status) {
		this.type = type;
		this.position = position;
		this.radius = radius;
		this.status = status;
	}
	
	public SquadOrderType getType() {
		return type;
	}
	public void setType(SquadOrderType type) {
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
		return type != SquadOrderType.NONE && type != SquadOrderType.IDLE;
	}

	@Override
	public String toString() {
		return "SquadOrder [type=" + type + ", position=" + position + ", radius=" + radius + ", status=" + status + "]";
	}
}
