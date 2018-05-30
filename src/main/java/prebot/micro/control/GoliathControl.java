package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class GoliathControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		for (Unit unit : unitList) {
			unit.attack(StrategyIdea.campPosition);
		}
	}
}
