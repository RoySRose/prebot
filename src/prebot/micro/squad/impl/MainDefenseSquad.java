package prebot.micro.squad.impl;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.Code.SquadName;
import prebot.micro.control.GoliathDefense;
import prebot.micro.control.MarineDefense;
import prebot.micro.control.TankDefense;
import prebot.micro.control.VultureDefense;
import prebot.micro.squad.Squad;

public class MainDefenseSquad extends Squad {

	private MarineDefense marineDefense = new MarineDefense();
	private VultureDefense vultureDefense = new VultureDefense();
	private TankDefense tankDefense = new TankDefense();
	private GoliathDefense goliathDefense = new GoliathDefense();

	public MainDefenseSquad(int priority, int squadRadius) {
		super(SquadName.MAIN_DEFENSE, priority, squadRadius);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Marine
				|| unit.getType() == UnitType.Terran_Vulture
				|| unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
				|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
				|| unit.getType() == UnitType.Terran_Goliath;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		List<Unit> marineList = new ArrayList<>();
		List<Unit> vultureList = new ArrayList<>();
		List<Unit> tankList = new ArrayList<>();
		List<Unit> goliathList = new ArrayList<>();
		for (Unit unit : unitList) {
			if (unit.getType() == UnitType.Terran_Marine) {
				marineList.add(unit);
			} else if (unit.getType() == UnitType.Terran_Vulture) {
				vultureList.add(unit);
			} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				tankList.add(unit);
			} else if (unit.getType() == UnitType.Terran_Goliath) {
				goliathList.add(unit);
			}
		}
		
		marineDefense.prepare(marineList, euiList);
		vultureDefense.prepare(vultureList, euiList);
		tankDefense.prepare(tankList, euiList);
		goliathDefense.prepare(goliathList, euiList);
		
		marineDefense.control();
		vultureDefense.control();
		tankDefense.control();
		goliathDefense.control();
		
	}
}