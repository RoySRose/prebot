package prebot.strategy;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.main.Prebot;
import prebot.strategy.action.Action;
import prebot.strategy.action.impl.EnemyBaseFinder;
import prebot.strategy.action.impl.InitialAction;
import prebot.strategy.action.impl.ScvScoutAfterBuild;

public class ActionManager {
	
	private static List<Action> actionList = new ArrayList<>();

	public static void init() {
		boolean isRandom = isRandom();

		actionList = new ArrayList<>();
		if (isRandom) {
			actionList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
		}
		actionList.add(new InitialAction(isRandom));
		actionList.add(new EnemyBaseFinder());
	}
	
	public static void updateActions() {
		List<Action> removeActionList = new ArrayList<>();
		List<Action> addActionList = new ArrayList<>();
		
		for (Action action : actionList) {
			if (action.exitCondition()) {
				removeActionList.add(action);
				if (action.getNextActions() != null && !action.getNextActions().isEmpty()) {
					addActionList.addAll(action.getNextActions());
				}
			} else {
				action.action();
			}
		}
		
		removeActionList(removeActionList);
		addActionList(addActionList);
	}

	private static void removeActionList(List<Action> removeActionList) {
		actionList.removeAll(removeActionList);
	}

	private static void addActionList(List<Action> addActionList) {
		actionList.addAll(addActionList);
	}

	private static boolean isRandom() {
		return Prebot.Broodwar.enemy().getRace() != Race.Protoss
				&& Prebot.Broodwar.enemy().getRace() != Race.Zerg
				&& Prebot.Broodwar.enemy().getRace() != Race.Terran;
	}
	
}
