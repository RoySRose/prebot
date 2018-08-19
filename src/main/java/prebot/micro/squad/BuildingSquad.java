package prebot.micro.squad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
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
		setUnitType(UnitType.Terran_Barracks
				, UnitType.Terran_Engineering_Bay
				, UnitType.Terran_Command_Center
				, UnitType.Terran_Science_Facility
				, UnitType.Terran_Comsat_Station);
	}

	@Override
	public boolean want(Unit unit) {
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		
		List<Unit> barracksList = unitListMap.getOrDefault(UnitType.Terran_Barracks, new ArrayList<Unit>());
		// List<UnitInfo> barracksEuiList = findEnemies(barracksList);
		barracksControl.controlIfUnitExist(barracksList, Collections.emptySet());
		
		List<Unit> engineeringBayList = unitListMap.getOrDefault(UnitType.Terran_Engineering_Bay, new ArrayList<Unit>());
		// List<UnitInfo> engineeringBayEuiList = findEnemies(engineeringBayList);
		engineeringBayControl.controlIfUnitExist(engineeringBayList, Collections.emptySet());
		
		List<Unit> commandCenterList = unitListMap.getOrDefault(UnitType.Terran_Command_Center, new ArrayList<Unit>());
		// List<UnitInfo> commandCenterEuiList = findEnemies(commandCenterList);
		commandCenterControl.controlIfUnitMoreThanTwo(commandCenterList, Collections.emptySet());
		
		List<Unit> scienceFacilityList = unitListMap.getOrDefault(UnitType.Terran_Science_Facility, new ArrayList<Unit>());
		// List<UnitInfo> scienceFacilityEuiList = findEnemies(scienceFacilityList);
		scienceFacilityControl.controlIfUnitExist(scienceFacilityList, Collections.emptySet());
		
		List<Unit> combatList = unitListMap.getOrDefault(UnitType.Terran_Comsat_Station, new ArrayList<Unit>());
		List<UnitInfo> comsatEuiList = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL);
		comsatControl.controlIfUnitExist(combatList, comsatEuiList);
	}
	
//	public List<UnitInfo> findEnemies(List<Unit> unitList) {
//		List<UnitInfo> euiList = new ArrayList<>();
//		for (Unit unit : unitList) {
//			UnitUtils.addEnemyUnitInfosInRadiusForAir(euiList, unit.getPosition(), unit.getType().sightRange() + MicroConfig.LARGE_ADD_RADIUS);
//		}
//		return euiList;
//	}
	
	@Override
	public void findEnemies() {
		// nothing
	}
}