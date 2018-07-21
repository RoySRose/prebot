package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.building.BarracksControl;
import prebot.micro.control.building.CommandCenterControl;
import prebot.micro.control.building.ComsatControl;
import prebot.micro.control.building.EngineeringBayControl;
import prebot.micro.control.building.ScienceFacilityControl;
import prebot.strategy.UnitInfo;

public class BuildingSquad extends Squad {

	private BarracksControl barracksControl = new BarracksControl();
	private EngineeringBayControl engineeringBayControl = new EngineeringBayControl();
	private CommandCenterControl commandCenterControl = new CommandCenterControl();
	private ScienceFacilityControl scienceFacilityControl = new ScienceFacilityControl();
	private ComsatControl comsatControl = new ComsatControl();

	public BuildingSquad() {
		super(SquadInfo.BUILDING);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Barracks
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
		
		List<Unit> barracksList = unitListMap.getOrDefault(UnitType.Terran_Barracks, new ArrayList<Unit>());
		List<UnitInfo> barracksEuiList = findEnemies(barracksList);
		barracksControl.controlIfUnitExist(barracksList, barracksEuiList);
		
		List<Unit> engineeringBayList = unitListMap.getOrDefault(UnitType.Terran_Engineering_Bay, new ArrayList<Unit>());
		List<UnitInfo> engineeringBayEuiList = findEnemies(engineeringBayList);
		engineeringBayControl.controlIfUnitExist(engineeringBayList, engineeringBayEuiList);
		
		List<Unit> commandCenterList = unitListMap.getOrDefault(UnitType.Terran_Command_Center, new ArrayList<Unit>());
		List<UnitInfo> commandCenterEuiList = findEnemies(commandCenterList);
		commandCenterControl.controlIfUnitMoreThanTwo(commandCenterList, commandCenterEuiList);
		
		List<Unit> scienceFacilityList = unitListMap.getOrDefault(UnitType.Terran_Science_Facility, new ArrayList<Unit>());
		List<UnitInfo> scienceFacilityEuiList = findEnemies(scienceFacilityList);
		scienceFacilityControl.controlIfUnitExist(scienceFacilityList, scienceFacilityEuiList);
		
		List<Unit> combatList = unitListMap.getOrDefault(UnitType.Terran_Comsat_Station, new ArrayList<Unit>());
		List<UnitInfo> comsatEuiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE);
		comsatControl.controlIfUnitExist(combatList, comsatEuiList);
	}
	
	public List<UnitInfo> findEnemies(List<Unit> unitList) {
		List<UnitInfo> euiList = new ArrayList<>();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForAir(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.BUILDING.squadRadius);
		}
		return euiList;
	}
	
	@Override
	public void findEnemies() {
		// nothing
	}
}