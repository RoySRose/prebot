package prebot.micro;

import java.util.HashMap;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.manager.WorkerManager;
import prebot.micro.squad.Squad;

public class SquadData {

	public Map<String, Squad> squadMap = new HashMap<>(); // 이름별 squad 저장

	public void addSquad(Squad squad) {
		squadMap.put(squad.getName(), squad);
	}
	
	public void activateSquad(Squad squad) {
		squad.setActivated(true);
	}
	
	public void deactivateSquad(Squad squad) {
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
