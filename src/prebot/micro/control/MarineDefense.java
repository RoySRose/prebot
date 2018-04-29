package prebot.micro.control;

import bwapi.Unit;
import prebot.brain.Idea;

public class MarineDefense extends Control {

	@Override
	public void control() {
		for (Unit unit : unitList) {
			unit.attack(Idea.of().campPosition);
		}
	}
}
