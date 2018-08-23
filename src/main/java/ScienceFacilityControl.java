

import java.util.Collection;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;

public class ScienceFacilityControl extends BuildingFlyControl {

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {

        for(Unit unit :  unitList){
        	if (skipControl(unit)) {
        		continue;
        	}

            buildingFlyMap.put(unit, new FlyCondition(false, false, null));

            if (MyBotModule.Broodwar.getFrameCount() > 10000) {
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
