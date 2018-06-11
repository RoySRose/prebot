package prebot.strategy.manage;

import prebot.strategy.action.Action;

public class RaceActionManager {

	private static RaceActionManager instance = new RaceActionManager();
	public static RaceActionManager Instance() {
		return instance;
	}
	
	private Action action = null;
	public void setAction(Action action) {
		this.action = action;
	}

	public void update() {
		if (action != null) {
			if (action.exitCondition()) {
				action = action.getNextAction();
			} else {
				action.action();
			}
		}
	}
}
