package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.squad.Squad;

public class Strategy {
	protected List<InerrableActionKnowledge> actionList = new ArrayList<>();
	protected List<Squad> squadList = new ArrayList<>();
	
	public String strategyName() {
		return getClass().getSimpleName();
	}
}
