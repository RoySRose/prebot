package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import prebot.brain.action.Action;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.squad.Squad;

public class Strategy {
	public List<BuildAction> buildActionList = new ArrayList<>();
	public List<Squad> squadList = new ArrayList<>();
	public List<Action> actionList = new ArrayList<>();
}
