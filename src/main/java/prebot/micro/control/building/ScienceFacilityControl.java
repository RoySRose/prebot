package prebot.micro.control.building;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.Control;
import prebot.strategy.UnitInfo;

public class ScienceFacilityControl extends BuildingFlyControl {

    public ScienceFacilityControl() {
        super(false, false);
    }

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
        executeFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {

    }
}
