package prebot.micro.squad;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.VultureControl;

public class GuerillaSquad extends Squad {
	private Position targetPosition;
	private VultureControl vultureControl = new VultureControl();
	
	public Position getTargetPosition() {
		return targetPosition;
	}

	public GuerillaSquad(Position position) {
		super(SquadInfo.GUERILLA_, position);
		this.targetPosition = position;
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Vulture;
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