package prebot.micro.old;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import prebot.common.util.MicroUtils;

public class OldSquadData {
	
	private Map<String, OldSquad> squads = new HashMap<>();
	
	public void update() {
//		LagTest lag = LagTest.startTest();
//		lag.setDuration(10);
		for (String squadName : squads.keySet()) {
			squads.get(squadName).update();
//			lag.estimate(squadName + ", " + squads.get(squadName).getUnitSet().size());
		}
		
//		if (CommonUtils.executeRotation(0, 168)){
//			System.out.println("[SQUAD INFO]");
//			for (String squadName : squads.keySet()) {
//				Squad sq = squads.get(squadName);
//				if (sq.getUnitSet().size() > 0) {
//					System.out.println(sq + " -> v: " + sq.microVulture.getUnits().size() + ", t: " + sq.microTank.getUnits().size() + ", g: " + sq.microGoliath.getUnits().size() + ", m: " + sq.microMarine.getUnits().size());
//				}
//			}
//		}
	}
	
	public void putSquad(OldSquad squad) {
		squads.put(squad.getName(), squad);
	}

	public OldSquad getSquad(String squadName) {
		return squads.get(squadName);
	}

	public OldSquad removeSquad(String squadName) {
		return squads.remove(squadName);
	}

	public List<OldSquad> getSquadList(String squadName) {
		List<OldSquad> squadList = new ArrayList<>();
		for (OldSquad squad : squads.values()) {
			if (squad.getName().startsWith(squadName)) {
				squadList.add(squad);
			}
		}
		return squadList;
	}

	public OldSquad getUnitSquad(Unit unit) {
		for (String sqaudName : squads.keySet()) {
			OldSquad squad = squads.get(sqaudName);
			if (MicroUtils.isUnitContainedInUnitSet(unit, squad.getUnitSet())) {
				return squad;
			}
	    }
		return null;
	}
	
	public boolean canAssignUnitToSquad(Unit unit, OldSquad squad) {
	    OldSquad unitSqaud = getUnitSquad(unit);
	    return unitSqaud == null || unitSqaud.getPriority() < squad.getPriority();
	}
	public void assignWorkerToSquad(Unit unit, OldSquad squad) {
		OldSquad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
		
	}
	
	public void assignUnitToSquad(Unit unit, OldSquad squad) {
		if (!canAssignUnitToSquad(unit, squad)) {
//			MyBotModule.Broodwar.sendText("We shouldn't be re-assigning this unit!");
			return;
		}
		
		OldSquad previousSquad = getUnitSquad(unit);
		if (previousSquad != null) {
			previousSquad.removeUnit(unit);
		}
		squad.addUnit(unit);
		
	}

//	public void printSquadInfo() {
//		for (String squadName : squads.keySet()) {
//			Squad squad = squads.get(squadName);
//			System.out.println("[" + squad.getName() + "] SIZE: " + squad.getUnitSet().size() + "\nORDER: " + squad.getOrder());
//		}
//		System.out.println();
//	}

	@Override
	public String toString() {
		return "SquadData [squads=" + squads + "]";
	}
	
}
