package prebot.micro.control.building;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import prebot.build.initialProvider.buildSets.VsProtoss;
import prebot.build.initialProvider.buildSets.VsTerran;
import prebot.build.initialProvider.buildSets.VsZerg;
import prebot.common.MetaType;
import prebot.micro.constant.MicroConfig;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.List;

public class ScienceFacilityControl extends BuildingFlyControl {

    public ScienceFacilityControl() {
        super(false, false);
    }

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
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
