package pre.combat;

import bwapi.Position;

public class SquadOrder {
	
	public enum SqaudOrderType {
		NONE, IDLE, ATTACK, DEFEND, HOLD
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
	public Position getPosition() {
		return position;
	}
	public int getRadius() {
		return radius;
	}
	public String getStatus() {
		return status;
	}
	
	public boolean isCombatOrder() {
		return type == SqaudOrderType.ATTACK ||
			   type == SqaudOrderType.DEFEND ||
			   type == SqaudOrderType.HOLD;
	}
	
}
