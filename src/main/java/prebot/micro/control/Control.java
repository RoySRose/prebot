package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.common.LagObserver;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;

public abstract class Control {
	
	// TODO 추후 모든 컨트롤 적용 필요
	public void controlIfUnitExist(List<Unit> unitList, List<UnitInfo> euiList) {
		if (!unitList.isEmpty()) {
			control(unitList, euiList);
		}
	}

	public void controlIfUnitMoreThanTwo(List<Unit> unitList, List<UnitInfo> euiList) {
		if (unitList.size() > 1) {
			control(unitList, euiList);
		}
	}
		
	public abstract void control(List<Unit> unitList, List<UnitInfo> euiList);
	
	protected boolean skipControl(Unit unit) {
		return !TimeUtils.executeUnitRotation(unit, LagObserver.groupsize());
	}
}
