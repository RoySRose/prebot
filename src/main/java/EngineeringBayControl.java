

import java.util.Collection;

import bwapi.Unit;

public class EngineeringBayControl extends BuildingFlyControl {

 	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {

        for(Unit unit :  unitList){
        	if (skipControl(unit)) {
        		continue;
        	}

            buildingFlyMap.put(unit, new FlyCondition(true, false, null));

            if (MyBotModule.Broodwar.getFrameCount() > 10000) {
                buildingFlyMap.get(unit).setFlyPosition(getFlyPosition0(unit));
            }

            processFly(unit);
        }
	}

    @Override
    public void checkFlyCondition(Unit unit) {

    }
}
