package prebot.micro.control.building;

import java.util.Collection;

import bwapi.Unit;
import prebot.micro.control.BuildingFlyControl;
import prebot.strategy.UnitInfo;

public class EngineeringBayControl extends BuildingFlyControl {

    public EngineeringBayControl() {
        super(true, false, null);
    }

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
  
        processFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {

    }
}
