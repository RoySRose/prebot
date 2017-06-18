package pre.combat;

import java.util.HashMap;
import java.util.Map;

import bwapi.Unit;
import pre.main.MyBotModule;
import pre.util.MicroUtils;

public class SquadData {
	
	private Map<String, Squad> squads = new HashMap<>();
	
	public void update() {
		for (String squadName : squads.keySet()) {
			squads.get(squadName).update();
		}
	}
	
	public void putSquad(Squad squad) {
		squads.put(squad.getName(), squad);
	}

	public Squad getSquad(String squadName) {
		return squads.get(squadName);
	}

	public Squad getUnitSquad(Unit unit) {
		for (String sqaudName : squads.keySet()) {
			Squad squad = squads.get(sqaudName);
			if (MicroUtils.isUnitContainedInUnitSet(unit, squad.getUnitSet())) {
				return squad;
			}
	    }
		return null;
	}
	
	public boolean canAssignUnitToSquad(Unit unit, Squad squad) {
	    Squad unitSqaud = getUnitSquad(unit);
	    return unitSqaud == null || unitSqaud.getPriority() < squad.getPriority();
	}
	
	public void assignUnitToSquad(Unit unit, Squad squad) {
		if (!canAssignUnitToSquad(unit, squad)) {
			MyBotModule.Broodwar.sendText("We shouldn't be re-assigning this unit!");
			return;
		}
		
		Squad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
		
	}

	@Override
	public String toString() {
		String info = "";
		for (String squadName : squads.keySet()) {
			Squad squad = squads.get(squadName);
			info += squad.getName() + " / " + squad.getOrder() + " / " +  squad.getPriority() + " / size: " + squad.getUnitSet().size() + "\n";
		}
		
		return "SquadData [" + info + "]";
		
	}
}
