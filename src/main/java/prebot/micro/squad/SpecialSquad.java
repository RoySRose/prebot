package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.DropshipControl;
import prebot.micro.control.airforce.VesselControl;
import prebot.micro.control.airforce.WraithControl;
import prebot.micro.control.building.BarracksControl;
import prebot.micro.control.building.CommandCenterControl;
import prebot.micro.control.building.ComsatControl;
import prebot.micro.control.building.EngineeringBayControl;
import prebot.micro.control.building.ScienceFacilityControl;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class SpecialSquad extends Squad {

	private WraithControl wraithControl = new WraithControl();
	private DropshipControl dropshipControl = new DropshipControl();
	private VesselControl vesselControl = new VesselControl();
	private BarracksControl barracksControl = new BarracksControl();
	private EngineeringBayControl engineeringBayControl = new EngineeringBayControl();
	private CommandCenterControl commandCenterControl = new CommandCenterControl();
	private ScienceFacilityControl scienceFacilityControl = new ScienceFacilityControl();
	private ComsatControl comsatControl = new ComsatControl();

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
	public void setTargetPosition() {
		targetPosition = StrategyIdea.mainSquadPosition;
	}

	@Override
	public void execute() {
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		
		List<Unit> wraithList = unitListMap.getOrDefault(UnitType.Terran_Wraith, new ArrayList<Unit>());
		List<UnitInfo> wraithEuiList = findEnemies(wraithList);
		wraithControl.control(wraithList, wraithEuiList, null);
		
		List<Unit> dropshipList = unitListMap.getOrDefault(UnitType.Terran_Dropship, new ArrayList<Unit>());
		List<UnitInfo> dropshipEuiList = findEnemies(dropshipList);
		dropshipControl.control(dropshipList, dropshipEuiList, null);
		
		List<Unit> scienceVesselList = unitListMap.getOrDefault(UnitType.Terran_Science_Vessel, new ArrayList<Unit>());
		List<UnitInfo> scienceVesselEuiList = findEnemies(scienceVesselList);
		vesselControl.control(scienceVesselList, scienceVesselEuiList, null);
		
		List<Unit> barracksList = unitListMap.getOrDefault(UnitType.Terran_Barracks, new ArrayList<Unit>());
		List<UnitInfo> barracksEuiList = findEnemies(barracksList);
		barracksControl.control(barracksList, barracksEuiList, null);
		
		List<Unit> engineeringBayList = unitListMap.getOrDefault(UnitType.Terran_Engineering_Bay, new ArrayList<Unit>());
		List<UnitInfo> engineeringBayEuiList = findEnemies(engineeringBayList);
		engineeringBayControl.control(engineeringBayList, engineeringBayEuiList, null);
		
		List<Unit> commandCenterList = unitListMap.getOrDefault(UnitType.Terran_Command_Center, new ArrayList<Unit>());
		List<UnitInfo> commandCenterEuiList = findEnemies(commandCenterList);
		commandCenterControl.control(commandCenterList, commandCenterEuiList, null);
		
		List<Unit> scienceFacilityList = unitListMap.getOrDefault(UnitType.Terran_Science_Facility, new ArrayList<Unit>());
		List<UnitInfo> scienceFacilityEuiList = findEnemies(scienceFacilityList);
		scienceFacilityControl.control(scienceFacilityList, scienceFacilityEuiList, null);
		
		List<Unit> combatList = unitListMap.getOrDefault(UnitType.Terran_Comsat_Station, new ArrayList<Unit>());
		List<UnitInfo> comsatEuiList = findEnemies(combatList);
		comsatControl.control(combatList, comsatEuiList, null);
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