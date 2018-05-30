package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.constant.MicroCode.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.GoliathControl;
import prebot.micro.control.MarineControl;
import prebot.micro.control.TankControl;
import prebot.micro.control.VultureControl;
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
		
		marineControl.control(marineList, euiList);
		vultureControl.control(vultureList, euiList);
		tankControl.control(tankList, euiList);
		goliathControl.control(goliathList, euiList);
	}
}