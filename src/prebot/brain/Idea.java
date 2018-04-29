package prebot.brain;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.squad.Squad;
import prebot.main.manager.StrategyManager;

public class Idea {
	public static Idea of() {
		return StrategyManager.Instance().getIdea();
	}

	// strategy
	public String strategyName;
	public List<BuildAction> buildActionList = new ArrayList<>();
	public List<Squad> squadList = new ArrayList<>();
	public List<InerrableActionKnowledge> actionList = new ArrayList<>();
	
	public boolean gasAdjustment = false; // true인 경우 gasAdjustmentWorkerCount에 따른 가스조절 
	public int gasAdjustmentWorkerCount = 0;
	
	public int checkerMaxCount = 0;
	public int scvScoutMaxCount = 0;
	
	public Position campPosition;
	public Position attackPosition;
	public BaseLocation expansionBase;

	public BaseLocation enemeyExpectedBase;
}
