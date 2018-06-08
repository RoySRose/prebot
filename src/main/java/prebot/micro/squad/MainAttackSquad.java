package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.MarineControl;
import prebot.micro.control.factory.GoliathControl;
import prebot.micro.control.factory.TankControl;
import prebot.micro.control.factory.VultureControl;
import prebot.strategy.StrategyIdea;

public class MainAttackSquad extends Squad {
	
	private MarineControl marineControl = new MarineControl();
	private VultureControl vultureControl = new VultureControl();
	private TankControl tankControl = new TankControl();
	private GoliathControl goliathControl = new GoliathControl();
	
	public MainAttackSquad() {
		super(SquadInfo.MAIN_ATTACK);
	}

	@Override
	public boolean want(Unit unit) {
		if (StrategyIdea.mainSquadMode == MainSquadMode.FD_PRESS) {
			if (unit.getType() == UnitType.Terran_Marine) {
				return true;
			}
		}
		
		return unit.getType() == UnitType.Terran_Vulture
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
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);

		List<Unit> marineList = unitListMap.getOrDefault(UnitType.Terran_Marine, new ArrayList<Unit>());
		marineControl.control(marineList, euiList);
		
		List<Unit> vultureList = unitListMap.getOrDefault(UnitType.Terran_Vulture, new ArrayList<Unit>());
		vultureControl.control(vultureList, euiList);
		
		List<Unit> tankList = new ArrayList<>();
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Tank_Mode, new ArrayList<Unit>()));
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Siege_Mode, new ArrayList<Unit>()));
		tankControl.control(tankList, euiList);
		
		List<Unit> goliathList = unitListMap.getOrDefault(UnitType.Terran_Goliath, new ArrayList<Unit>());
		goliathControl.control(goliathList, euiList);
	}
}