package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.WraithControl;
import prebot.strategy.UnitInfo;

public class SpecialSquad extends Squad {

	private WraithControl wraithControl = new WraithControl();

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
				|| unit.getType() == UnitType.Terran_Science_Facility
				|| unit.getType() == UnitType.Terran_Comsat_Station;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		
		List<Unit> wraithList = unitListMap.getOrDefault(UnitType.Terran_Wraith, new ArrayList<Unit>());
		List<UnitInfo> wraithEuiList = findEnemies(wraithList);
		wraithControl.control(wraithList, wraithEuiList);
		
		List<Unit> dropshipList = unitListMap.getOrDefault(UnitType.Terran_Dropship, new ArrayList<Unit>());
		List<UnitInfo> dropshipEuiList = findEnemies(dropshipList);
		wraithControl.control(dropshipList, dropshipEuiList);
		
		List<Unit> scienceVesselList = unitListMap.getOrDefault(UnitType.Terran_Science_Vessel, new ArrayList<Unit>());
		List<UnitInfo> scienceVesselEuiList = findEnemies(scienceVesselList);
		wraithControl.control(scienceVesselList, scienceVesselEuiList);
		
		List<Unit> barracksList = unitListMap.getOrDefault(UnitType.Terran_Barracks, new ArrayList<Unit>());
		List<UnitInfo> barracksEuiList = findEnemies(barracksList);
		wraithControl.control(barracksList, barracksEuiList);
		
		List<Unit> engineeringBayList = unitListMap.getOrDefault(UnitType.Terran_Engineering_Bay, new ArrayList<Unit>());
		List<UnitInfo> engineeringBayEuiList = findEnemies(engineeringBayList);
		wraithControl.control(engineeringBayList, engineeringBayEuiList);
		
		List<Unit> commandCenterList = unitListMap.getOrDefault(UnitType.Terran_Command_Center, new ArrayList<Unit>());
		List<UnitInfo> commandCenterEuiList = findEnemies(commandCenterList);
		wraithControl.control(commandCenterList, commandCenterEuiList);
		
		List<Unit> scienceFacilityList = unitListMap.getOrDefault(UnitType.Terran_Science_Facility, new ArrayList<Unit>());
		List<UnitInfo> scienceFacilityEuiList = findEnemies(scienceFacilityList);
		wraithControl.control(scienceFacilityList, scienceFacilityEuiList);
		
		List<Unit> combatList = unitListMap.getOrDefault(UnitType.Terran_Comsat_Station, new ArrayList<Unit>());
		List<UnitInfo> comsatEuiList = findEnemies(combatList);
		wraithControl.control(combatList, comsatEuiList);
	}
	
	public List<UnitInfo> findEnemies(List<Unit> unitList) {
		List<UnitInfo> euiList = new ArrayList<>();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadius(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.SPECIAL.squadRadius);
		}
		return euiList;
	}
	
	@Override
	public void findEnemies() {
		// nothing
	}
}