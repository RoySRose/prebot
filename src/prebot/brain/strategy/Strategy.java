package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import prebot.brain.buildaction.BuildAction;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.squad.Squad;

public class Strategy {
	protected List<BuildAction> buildActionList = new ArrayList<>();
	protected List<InerrableActionKnowledge> actionList = new ArrayList<>();
	protected List<Squad> squadList = new ArrayList<>();
	
	public String strategyName() {
		return getClass().getSimpleName();
	}

	public List<BuildAction> getBuildActionList() {
		return buildActionList;
	}

	public void setBuildActionList(List<BuildAction> buildActionList) {
		this.buildActionList = buildActionList;
	}

	public List<InerrableActionKnowledge> getActionList() {
		return actionList;
	}

	public void setActionList(List<InerrableActionKnowledge> actionList) {
		this.actionList = actionList;
	}

	public List<Squad> getSquadList() {
		return squadList;
	}

	public void setSquadList(List<Squad> squadList) {
		this.squadList = squadList;
	}
}
