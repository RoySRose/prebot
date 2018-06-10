package prebot.micro;

import bwapi.Position;

public class PositionReserveInfo {
	public int unitId;
	public Position position;
	public int reservedFrame;

	public PositionReserveInfo(int unitId, Position position, int reservedFrame) {
		this.unitId = unitId;
		this.position = position;
		this.reservedFrame = reservedFrame;
	}
}
