package prebot.micro;

import java.util.HashMap;
import java.util.Map;

import bwapi.Unit;
import prebot.brain.squad.Squad;

public class SquadData {

	public Map<String, Squad> squadMap = new HashMap<>(); // 이름별 squad 저장

	public void addSquad(Squad squad) {
		squadMap.put(squad.squadName, squad);
	}
	
	public void clearSquadMap() {
		squadMap.clear();
	}

	public Squad getUnitSquad(Unit unit) {
		for (String sqaudName : squadMap.keySet()) {
			Squad squad = squadMap.get(sqaudName);
			if (squad.hasUnit(unit)) {
				return squad;
			}
		}
		return null;
	}

	public boolean canAssignUnitToSquad(Unit unit, Squad squad) {
		Squad unitSqaud = getUnitSquad(unit);
		return unitSqaud == null || unitSqaud.priority < squad.priority;
	}

	public void assignUnitToSquad(Unit unit, Squad squad) {
		if (!canAssignUnitToSquad(unit, squad)) {
			return;
		}

		Squad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
	}

}
