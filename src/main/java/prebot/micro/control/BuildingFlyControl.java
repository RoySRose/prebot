package prebot.micro.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public abstract class BuildingFlyControl extends Control{

    public Map<Unit, FlyCondition> buildingFlyMap;

//    public BuildingFlyControl(boolean flyAlways, boolean isGateway, TilePosition tilePosition) {
//        this.flyAlways = flyAlways;
//        this.isGateway = isGateway;
//        if(isGateway){
//            if(tilePosition == null){
//                System.out.println("TilePosition must not be null");
//            }
//            this.landPosition = tilePosition;
//        }else {
//            this.landPosition = TilePosition.None;
//        }
//        this.flyPosition = Position.None;
//    }
//
    public BuildingFlyControl() {
    	buildingFlyMap = new HashMap<>();
    }

//    public final void setLandPosition(TilePosition landPosition){
//        this.landPosition = landPosition;
//    }
//    public BuildingFly getBuildingFly() {
//        return buildingFly;
//    }
//    public void setBuildingFly(BuildingFly buildingFly) {
//        this.buildingFly = buildingFly;
//    }
//    public void setFlyPosition(Position flyPosition) {
//        this.flyPosition = flyPosition;
//    }


    public final void processFly(Unit unit){

        setDefaultBuildingFly(unit);

        checkFlyCondition(unit);

        executeFly(unit);

    }

    private void executeFly(Unit unit){

        FlyCondition flyCondition = buildingFlyMap.get(unit);

        if(!unit.isFlying() && flyCondition.getBuildingFly() == BuildingFly.UP){
            CommandUtils.lift(unit);
        }else if(unit.isFlying() && flyCondition.getBuildingFly() == BuildingFly.DOWN){
            //System.out.print("inside excute to down: " + landPosition);
            if(flyCondition.getLandPosition() != TilePosition.None) {
                CommandUtils.land(unit, flyCondition.getLandPosition());
                //System.out.println("send land command");
            }
        }else if(unit.isFlying() && flyCondition.getBuildingFly() == BuildingFly.UP){
            if(flyCondition.getFlyPosition() != Position.None) {
                CommandUtils.move(unit, flyCondition.getFlyPosition());
            }
        }
    }

    public abstract void checkFlyCondition(Unit unit);

    public final void setDefaultBuildingFly(Unit unit) {

        FlyCondition flyCondition = buildingFlyMap.get(unit);

        if(flyCondition.isFlyAlways()){
            flyCondition.setBuildingFly(BuildingFly.UP);
        }else {
            if (flyCondition.isGateway()) {
                if (Prebot.Broodwar.enemy().getRace() == Race.Zerg) {
                    flyCondition.setBuildingFly(BuildingFly.DOWN);
                }else{
                    if (InformationManager.Instance().firstBarrack != null && InformationManager.Instance().barrackStart + 24*3 > Prebot.Broodwar.getFrameCount()) {
                        flyCondition.setBuildingFly(BuildingFly.UP);
                        //System.out.println("wait!");
                    } else {
                        flyCondition.setBuildingFly(BuildingFly.DOWN);
                    }
                }
            } else {
                flyCondition.setBuildingFly(BuildingFly.DOWN);
            }
        }
        //System.out.println("default: " + getBuildingFly());
    }

    private final boolean checkEnemyNearBy(Unit unit){
        if(!UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, unit.getPosition(),500).isEmpty() ){
            return true;
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
