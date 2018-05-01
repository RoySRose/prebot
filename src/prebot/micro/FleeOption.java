package prebot.micro;

import bwapi.Position;
import prebot.common.code.ConfigForMicro.Angles;

public class FleeOption {

	public Position goalPosition;
	public boolean united;
	public int[] angles;

	public FleeOption(Position goalPosition, boolean united, int[] angles) {
		this.goalPosition = goalPosition;
		this.united = united;
		this.angles = angles;
	}
	
	public static FleeOption defaultOption() {
		return new FleeOption(Position.None, true, Angles.WIDE);
	}
}
