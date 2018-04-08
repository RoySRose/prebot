package prebot.brain.strategy;

import java.util.ArrayList;
import java.util.List;

import prebot.brain.action.Action;
import prebot.brain.action.Actions;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.buildaction.BuildActions;
import prebot.brain.buildaction.TrainActions;
import prebot.brain.squad.Squad;
import prebot.brain.squad.Squads;

public class Strategy {
	public List<BuildAction> buildActionList = new ArrayList<>();
	public List<Squad> squadList = new ArrayList<>();
	public List<Action> actionList = new ArrayList<>();
	
	public static List<BuildAction> getDefaultBuildActionList() {
		List<BuildAction> buildActionList = new ArrayList<>();
		buildActionList.add(new TrainActions.TrainSCV());
		buildActionList.add(new BuildActions.BuildSupplyDepot());
		return buildActionList;
	}
	
	public static List<Squad> getDefaultSquadList() {
		List<Squad> squadList = new ArrayList<>();
		squadList.add(new Squads.IdleSquad());
		squadList.add(new Squads.MainSquad());
		return squadList;
	}
	
	public static List<Action> getDefaultActionList() {
		List<Action> actionList = new ArrayList<>();
		actionList.add(new Actions.MechanicTerranGasAdjustment());
		return actionList;
	}
}
