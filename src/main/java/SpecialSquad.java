

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;

public class SpecialSquad extends Squad {

	private DropshipControl dropshipControl = new DropshipControl();
	private VesselControl vesselControl = new VesselControl();

	public SpecialSquad() {
		super(MicroConfig.SquadInfo.SPECIAL);
		setUnitType(UnitType.Terran_Science_Vessel, UnitType.Terran_Dropship);
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
		
		List<Unit> dropshipList = unitListMap.getOrDefault(UnitType.Terran_Dropship, new ArrayList<Unit>());
		List<UnitInfo> dropshipEuiList = findEnemies(dropshipList);
		dropshipControl.controlIfUnitExist(dropshipList, dropshipEuiList);
		
		List<Unit> scienceVesselList = unitListMap.getOrDefault(UnitType.Terran_Science_Vessel, new ArrayList<Unit>());
		List<UnitInfo> scienceVesselEuiList = findEnemies(scienceVesselList);
		vesselControl.controlIfUnitExist(scienceVesselList, scienceVesselEuiList);
	}
	
	public List<UnitInfo> findEnemies(List<Unit> unitList) {
		List<UnitInfo> euiList = new ArrayList<>();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForAir(euiList, unit.getPosition(), unit.getType().sightRange() + MicroConfig.COMMON_ADD_RADIUS);
		}
		return euiList;
	}
	
	@Override
	public void findEnemies() {
		// nothing
	}
}