package prebot.strategy.action;

import java.util.ArrayList;
import java.util.List;

public abstract class Action {
	protected List<Action> nextActions = new ArrayList<>();
	
	public List<Action> getNextActions() {
		return nextActions;
	}
	public void getNextActions(List<Action> nextActions) {
		this.nextActions = nextActions;
	}
	
	public abstract boolean exitCondition();
	public abstract void action();

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass());
	}
}
