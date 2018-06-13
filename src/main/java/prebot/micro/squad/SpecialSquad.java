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
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class SpecialSquad extends Squad {

	private WraithControl wraithControl = new WraithControl();
	private DropshipControl dropshipControl = new DropshipControl();
	private VesselControl vesselControl = new VesselControl();

	public SpecialSquad() {
		super(SquadInfo.SPECIAL);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Wraith
				|| unit.getType() == UnitType.Terran_Dropship
				|| unit.getType() == UnitType.Terran_Science_Vessel;
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
	}
	
	public List<UnitInfo> findEnemies(List<Unit> unitList) {
		List<UnitInfo> euiList = new ArrayList<>();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForAir(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.SPECIAL.squadRadius);
		}
		return euiList;
	}
	
	@Override
	public void findEnemies() {
		// nothing
	}
}