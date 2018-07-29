package prebot.micro.control.building;

import java.util.Collection;

import bwapi.Unit;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.FlyCondition;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public class EngineeringBayControl extends BuildingFlyControl {

 	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {

        for(Unit unit :  unitList){

            buildingFlyMap.put(unit, new FlyCondition(true, false, null));
            processFly(unit);
        }
	}

    @Override
    public void checkFlyCondition(Unit unit) {

    }
}
