package prebot.micro.control;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode;
import prebot.common.main.MyBotModule;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.CombatManager;
import prebot.micro.control.factory.GoliathControl;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder;

import java.util.ArrayList;
import java.util.List;

public abstract class BuildingFlyControl extends Control{

    private TilePosition landPosition;
    private Position flyPosition;
    private BuildingFly buildingFly;
    boolean flyAlways;
    boolean isGateway;

    public BuildingFlyControl(boolean flyAlways, boolean isGateway, TilePosition tilePosition) {
        this.flyAlways = flyAlways;
        this.isGateway = isGateway;
        if(isGateway){
            if(tilePosition == null){
                System.out.println("TilePosition must not be null");
            }
            this.landPosition = tilePosition;
        }else {
            this.landPosition = TilePosition.None;
        }
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


    public final void processFly(List<Unit> unitList, List<UnitInfo> euiList){

        setDefaultBuildingFly(unitList);
        checkFlyCondition();

        executeFly(unitList, euiList);

    }

    public void executeFly(List<Unit> unitList, List<UnitInfo> euiList){
        for(Unit unit : unitList){

            if(!unit.isFlying() && getBuildingFly() == BuildingFly.UP){
                CommandUtils.lift(unit);
            }else if(unit.isFlying() && getBuildingFly() == BuildingFly.DOWN){
            	System.out.print("inside excute to down: " + landPosition);
                if(landPosition != TilePosition.None) {
                    CommandUtils.land(unit, landPosition);
                    System.out.println("send land command");
                }
            }else if(unit.isFlying() && getBuildingFly() == BuildingFly.UP){
                if(flyPosition != Position.None) {
                    CommandUtils.move(unit, flyPosition);
                }
            }
        }
    }

    public abstract void checkFlyCondition();

    public final void setDefaultBuildingFly(List<Unit> unitList) {

        if(flyAlways){
            this.buildingFly = BuildingFly.UP;
        }else {
            if (isGateway) {
                if (Prebot.Broodwar.self().getRace() == Race.Zerg) {
                    buildingFly = BuildingFly.DOWN;
                }else{
                    if (InformationManager.Instance().firstBarrack != null && InformationManager.Instance().barrackStart + 24*3 > Prebot.Broodwar.getFrameCount()) {
                        buildingFly = BuildingFly.UP;
                        System.out.println("wait!");
                    } else {
                        buildingFly = BuildingFly.DOWN;
                        
                    }
                }
            } else {
                buildingFly = BuildingFly.DOWN;
            }
        }
        System.out.println("default: " + getBuildingFly());
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
