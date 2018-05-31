package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.VultureControl;

public class GuerillaSquad extends Squad {
	private VultureControl vultureControl = new VultureControl();

	public GuerillaSquad() {
		super(SquadInfo.GUERILLA);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Vulture;
	}

	/// checker squad는 매 frame 1회 용감한 checker부대원을 모집한다.
	/// 스파이더마인을 많이 보유한 벌처의 우선순위가 높다.
	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		vultureControl.control(unitList, euiList);
	}
}