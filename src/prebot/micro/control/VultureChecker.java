package prebot.micro.control;

import bwapi.Unit;

public class VultureChecker extends Control {

	@Override
	public void control() {
		for (Unit unit : sqaudUnitList) {
			unit.attack(idea.campPosition);
		}
	}
}
