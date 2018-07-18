package prebot.micro.control.building;

import bwapi.Unit;
import prebot.micro.control.BuildingFlyControl;
import prebot.strategy.UnitInfo;

import java.util.List;

public class EngineeringBayControl extends BuildingFlyControl {

    public EngineeringBayControl() {
        super(true, false);
    }

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
        processFly(unitList, euiList);
		// TODO Auto-generated method stub
	}

    @Override
    public void checkFlyCondition() {

    }
}
