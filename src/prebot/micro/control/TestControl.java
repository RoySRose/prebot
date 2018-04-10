package prebot.micro.control;

import bwapi.Unit;
import prebot.common.util.CommandUtils;

public class TestControl extends MicroControl {

	@Override
	public void control(Unit controlUnit) {
		CommandUtils.move(controlUnit, goalPosition);
	}

}
