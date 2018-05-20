package prebot.micro;

import java.util.HashMap;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.manager.WorkerManager;
import prebot.micro.squad.Squad;

public class SquadData {

	public Map<String, Squad> squadMap = new HashMap<>(); // 이름별 squad 저장

	public void addOrUpdateSquad(Squad squad) {
		if (squadMap.get(squad.getName()) != null) {
			Squad existSquad = squadMap.get(squad.getName());
			existSquad.priority = squad.priority;
			existSquad.squadRadius = squad.squadRadius;
			squadMap.put(squad.getName(), existSquad);
		} else {
			squadMap.put(squad.getName(), squad);
		}
	}
	
	public void removeSquad(Squad squad) {
		for (Unit unit : squad.unitList) {
			if (unit.getType() == UnitType.Terran_SCV) {
				WorkerManager.Instance().setIdleWorker(unit); // SCV를 WorkerManager에서 관리
			}
		}
		squadMap.remove(squad.getName());
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
		
		if (unit.getType() == UnitType.Terran_SCV) {
			WorkerManager.Instance().setCombatWorker(unit); // SCV를 CombatManager에서 관리
		}
	}

}
