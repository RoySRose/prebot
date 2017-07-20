package pre.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	public Squad removeSquad(String squadName) {
		return squads.remove(squadName);
	}

	public List<Squad> getSquadList(String squadName) {
		List<Squad> squadList = new ArrayList<>();
		for (Squad squad : squads.values()) {
			if (squad.getName().startsWith(squadName)) {
				squadList.add(squad);
			}
		}
		return squadList;
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

	public void printSquadInfo() {
		for (String squadName : squads.keySet()) {
			Squad squad = squads.get(squadName);
			System.out.println("[" + squad.getName() + "] SIZE: " + squad.getUnitSet().size() + "\nORDER: " + squad.getOrder());
		}
		System.out.println();
	}

	@Override
	public String toString() {
		return "SquadData [squads=" + squads + "]";
	}
	
}
