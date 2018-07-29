package prebot.micro.control.building;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.FlyCondition;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.Collection;

public class CommandCenterControl extends BuildingFlyControl {

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {

		// TODO Auto-generated method stub
//        if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2
//                && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) == 2) {
//            List<Unit> unitTempList = new ArrayList<>();
//            unitTempList.add(getSecondCommandCenter());
//            processFly(unitTempList, euiList);
//        }

        for(Unit unit :  unitList){

            buildingFlyMap.put(unit, new FlyCondition());

            if(unit.equals(InformationManager.Instance().getFirstCC())){
                continue;
            }
            if(unit.equals(getSecondCommandCenter())) {
                processFly(unit);
            }
        }
	}

    @Override
    public void checkFlyCondition(Unit checkCC) {

        if (checkCC != null) {
            BaseLocation correctLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);

            if (checkCC.isLifted() == false) {
                if (checkCC.getTilePosition().getX() != correctLocation.getTilePosition().getX() || checkCC.getTilePosition().getY() != correctLocation.getTilePosition().getY()) {
                    buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.UP);
                }
            } else {
                buildingFlyMap.get(checkCC).setLandPosition(new TilePosition(correctLocation.getTilePosition().getX(), correctLocation.getTilePosition().getY()));
                buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.DOWN);
            }
        }

    }

    public Unit getSecondCommandCenter(){

        if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2
                && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) == 2) {

            for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center)) {
                if (commandCenter.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX() && commandCenter.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
                    continue;
                } else {
                    return commandCenter;
                }
            }
        }
        return null;
    }

}
