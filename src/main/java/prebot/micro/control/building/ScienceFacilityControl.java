package prebot.micro.control.building;

import java.util.Collection;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import prebot.common.main.MyBotModule;
import prebot.micro.constant.MicroConfig;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.FlyCondition;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public class ScienceFacilityControl extends BuildingFlyControl {

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {

        for(Unit unit :  unitList){
        	if (skipControl(unit)) {
        		continue;
        	}

            buildingFlyMap.put(unit, new FlyCondition(false, false, null));

            if (MyBotModule.Broodwar.getFrameCount() > 12000) {
                buildingFlyMap.get(unit).setFlyPosition(getFlyPosition0(unit));
            }

            processFly(unit);
        }
	}

    @Override
    public void checkFlyCondition(Unit unit) {
//        if (InformationManager.Instance().enemyRace == Race.Zerg) {
//            if (MicroConfig.Upgrade.hasResearched(TechType.Irradiate)) {
//                buildingFlyMap.get(unit).setBuildingFly(BuildingFly.UP);
//            }
//        } else {
            buildingFlyMap.get(unit).setBuildingFly(BuildingFly.UP);
//        }
    }
}
