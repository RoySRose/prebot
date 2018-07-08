package prebot.micro.control;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.common.constant.CommonCode;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;

import java.util.List;

public abstract class BuildingFlyControl extends Control{

    private TilePosition landPosition;
    private Position flyPosition;
    private BuildingFly buildingFly;
    boolean flyAlways;
    boolean isGateway;

    public BuildingFlyControl(boolean flyAlways, boolean isGateway) {
        this.flyAlways = flyAlways;
        this.isGateway = isGateway;
        this.landPosition = TilePosition.None;
        this.flyPosition = Position.None;
    }

    public BuildingFlyControl() {
        this.flyAlways = false;
        this.isGateway = false;
        this.landPosition = TilePosition.None;
        this.flyPosition = Position.None;
    }

    public final void setLandPosition(TilePosition landPosition){
        this.landPosition = landPosition;
    }
    public BuildingFly getBuildingFly() {
        return buildingFly;
    }
    public void setBuildingFly(BuildingFly buildingFly) {
        this.buildingFly = buildingFly;
    }
    public void setFlyPosition(Position flyPosition) {
        this.flyPosition = flyPosition;
    }


    public void executeFly(List<Unit> unitList, List<UnitInfo> euiList){
        if(flyAlways){
            this.buildingFly = BuildingFly.UP;
            return;
        }

        setDefaultBuildingFly(unitList);
        checkFlyCondition();

        for(Unit units : unitList){

            if(!units.isFlying() && getBuildingFly() == BuildingFly.UP){
                units.lift();
            }else if(units.isFlying() && getBuildingFly() == BuildingFly.DOWN){
                if(landPosition != TilePosition.None) {
                    units.land(landPosition);
                }
            }else if(units.isFlying() && getBuildingFly() == BuildingFly.UP){
                if(flyPosition != Position.None) {
                    units.move(flyPosition);
                }
            }
        }
    }

    public abstract void checkFlyCondition();

    private final void setDefaultBuildingFly(List<Unit> unitList) {
        if(isGateway) {
            if (checkEnemyNearBy(unitList) || marinInBuildManager()) {
                buildingFly = BuildingFly.DOWN;
            } else {
                buildingFly = BuildingFly.UP;
            }
        }else{
            buildingFly = BuildingFly.DOWN;
        }
    }

    private final boolean checkEnemyNearBy(List<Unit> unitList){
        for(Unit units : unitList){
            if(!UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, units.getPosition(),500).isEmpty() ){
                return true;
            }
        }
        return false;
    }

    public final boolean marinInBuildManager(){
        if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) >= 1){
            return true;
        }
        return false;
    }
}
