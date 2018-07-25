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
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

import java.util.ArrayList;
import java.util.List;

public class CommandCenterControl extends BuildingFlyControl {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
        if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2
                && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) == 2) {
            List<Unit> unitTempList = new ArrayList<>();
            unitTempList.add(getSecondCommandCenter());
            processFly(unitTempList, euiList);
        }
	}

    @Override
    public void checkFlyCondition() {

        if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {

            Unit checkCC = getSecondCommandCenter();

            if (checkCC != null) {
                BaseLocation correctLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);

                if (checkCC.isLifted() == false) {
                    if (checkCC.getTilePosition().getX() != correctLocation.getTilePosition().getX() || checkCC.getTilePosition().getY() != correctLocation.getTilePosition().getY()) {
                        setBuildingFly(BuildingFly.UP);
                    }
                } else {
                    setLandPosition(new TilePosition(correctLocation.getTilePosition().getX(), correctLocation.getTilePosition().getY()));
                    setBuildingFly(BuildingFly.DOWN);
                }
            }
        }
    }

    public Unit getSecondCommandCenter(){

        for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center)) {
            if (commandCenter.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX() && commandCenter.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
                continue;
            } else {
                return commandCenter;
            }
        }
        return null;
    }

}
