package prebot.micro.control;

import java.util.Collection;

import bwapi.Unit;
import prebot.common.LagObserver;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;

public abstract class Control {
	
	// TODO 추후 모든 컨트롤 적용 필요
	public void controlIfUnitExist(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (!unitList.isEmpty()) {
			control(unitList, euiList);
		}
	}

	public void controlIfUnitMoreThanTwo(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (unitList.size() > 1) {
			control(unitList, euiList);
		}
	}
		
	public abstract void control(Collection<Unit> unitList, Collection<UnitInfo> euiList);
	
	protected boolean skipControl(Unit unit) {
		return !TimeUtils.executeUnitRotation(unit, LagObserver.groupsize());
	}
}
