

import java.util.ArrayList;
import java.util.List;

public class ActionManager {

	private static ActionManager instance = new ActionManager();

	public static ActionManager Instance() {
		return instance;
	}
	
	private List<Action> actionList = new ArrayList<>();

	public void update() {
		List<Action> removeActionList = new ArrayList<>();
		List<Action> addActionList = new ArrayList<>();
		
		for (Action action : actionList) {
			if (action.exitCondition()) {
				removeActionList.add(action);
				if (action.getNextAction() != null) {
					addActionList.add(action.getNextAction());
				}
			} else {
				action.action();
			}
		}
		
		actionList.removeAll(removeActionList);
		actionList.addAll(addActionList);
	}
	
	public void addAction(Action action) {
		actionList.add(action);
	}
}
