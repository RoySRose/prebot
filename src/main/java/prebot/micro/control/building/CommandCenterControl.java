package prebot.micro.control.building;

import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.Control;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public class CommandCenterControl extends BuildingFlyControl {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
        executeFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {

        if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {

            Unit checkCC = getSecondCommandCenter();
            BaseLocation correctLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);

            if (checkCC != null) {
                if (checkCC.isLifted() == false) {
                    if (checkCC.getTilePosition().getX() != correctLocation.getTilePosition().getX() || checkCC.getTilePosition().getY() != correctLocation.getTilePosition().getY()) {
                        setBuildingFly(BuildingFly.UP);
                    }
                } else {
                    setLandPosition(new TilePosition(correctLocation.getTilePosition().getX(), correctLocation.getTilePosition().getY()));
                    setBuildingFly(BuildingFly.DOWN);
                }
                if (checkCC.isLifted() == false && checkCC.getTilePosition().getX() == correctLocation.getTilePosition().getX()
                        && checkCC.getTilePosition().getY() == correctLocation.getTilePosition().getY()) {
                    EXOK = true;
                }
            }
        }
    }

    private final Unit getSecondCommandCenter(){

        for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center)) {
            if (commandCenter.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX() && commandCenter.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
                continue;
            } else {
                return commandCenter;
            }
        }
    }

}
