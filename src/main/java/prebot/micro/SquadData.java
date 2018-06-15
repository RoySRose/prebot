package prebot.micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.squad.Squad;

public class SquadData {

	private Map<String, Squad> squadMap = new HashMap<>(); // 이름별 squad 저장
	
	public Map<String, Squad> getSquadMap() {
		return squadMap;
	}

	public Squad getSquad(String squadName) {
		return squadMap.get(squadName);
	}

	public List<Squad> getSquadList(String squadName) {
		List<Squad> squadList = new ArrayList<>();
		for (Squad squad : squadMap.values()) {
			if (squad.getSquadName().startsWith(squadName)) {
				squadList.add(squad);
			}
		}
		return squadList;
	}

	public void addSquad(Squad squad) {
		squadMap.put(squad.getSquadName(), squad);
	}
	
	public Squad removeSquad(String squadName) {
		return squadMap.remove(squadName);
	}
	
	public void activateSquad(String squadName) {
		Squad squad = squadMap.get(squadName);
		squad.setActivated(true);
	}
	
	public void deactivateSquad(String squadName) {
		Squad squad = squadMap.get(squadName);
		for (Unit unit : squad.unitList) {
			if (unit.getType() == UnitType.Terran_SCV) {
				WorkerManager.Instance().setIdleWorker(unit); // SCV를 WorkerManager에서 관리
			}
		}
		squad.unitList.clear();
		squad.setActivated(false);
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
		return unitSqaud == null || unitSqaud.getPriority() < squad.getPriority();
	}

	public void assignUnitToSquad(Unit unit, Squad squad) {
		Squad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
		
		if (unit.getType() == UnitType.Terran_SCV) {
			WorkerManager.Instance().setCombatWorker(unit); // SCV를 CombatManager에서 관리
		}
	}
	
	public void excludeUnitFromSquad(Unit unit) {
		Squad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		if (unit.getType() == UnitType.Terran_SCV) {
			WorkerManager.Instance().setIdleWorker(unit); // SCV를 WorkerManager에서 관리
		}
	}

}
