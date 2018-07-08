package prebot.micro.control.building;

import java.util.List;

import bwapi.Unit;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.Control;
import prebot.strategy.UnitInfo;

public class EngineeringBayControl extends BuildingFlyControl {

    public EngineeringBayControl() {
        super(true, false);
    }

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
        executeFly(unitList, euiList);
		// TODO Auto-generated method stub
	}

    @Override
    public void checkFlyCondition() {

    }
}
