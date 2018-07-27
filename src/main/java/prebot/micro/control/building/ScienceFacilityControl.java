package prebot.micro.control.building;

import java.util.Collection;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import prebot.micro.constant.MicroConfig;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public class ScienceFacilityControl extends BuildingFlyControl {

    public ScienceFacilityControl() {
        super(false, false, null);
    }

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {

        processFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {
        if (InformationManager.Instance().enemyRace == Race.Zerg) {
            if (MicroConfig.Upgrade.hasResearched(TechType.Irradiate)) {
                setBuildingFly(BuildingFly.UP);
            }
        } else {
            setBuildingFly(BuildingFly.UP);
        }
    }
}
