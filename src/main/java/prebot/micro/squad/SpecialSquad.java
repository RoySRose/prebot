package prebot.micro.squad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.VultureControl;
import prebot.strategy.StrategyIdea;

public class SpecialSquad extends Squad {
	private VultureControl vultureControl = new VultureControl();

	public SpecialSquad() {
		super(SquadInfo.SPECIAL);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Wraith
				|| unit.getType() == UnitType.Terran_Dropship
				|| unit.getType() == UnitType.Terran_Science_Vessel
				|| unit.getType() == UnitType.Terran_Barracks
				|| unit.getType() == UnitType.Terran_Engineering_Bay
				|| unit.getType() == UnitType.Terran_Command_Center
				|| unit.getType() == UnitType.Terran_Science_Facility;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		vultureControl.control(unitList, euiList);
	}
}