package prebot.micro.control;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.build.prebot1.BuildManager;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BuildingFlyControl extends Control{

    public Map<Unit, FlyCondition> buildingFlyMap;

    public BuildingFlyControl() {
    	buildingFlyMap = new HashMap<>();
    }

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

    public Position getFlyPosition0(Unit unit){

        List<Unit> attackUnit = UnitUtils.getUnitList(UnitType.Terran_Vulture, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Goliath);
        List<UnitInfo> enemyUnit = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL);
        List<UnitInfo> dangerous_targets = new ArrayList<>();

        for(UnitInfo dangerous_target : enemyUnit){
            if(dangerous_target.getType().airWeapon() != null){
                dangerous_targets.add(dangerous_target);
            }
        }

        Unit leader = UnitUtils.leaderOfUnit(attackUnit);

        Position goalPos = null;

        UnitInfo mostDangerousUnit = getMostDangerTarget(dangerous_targets, unit);
        UnitInfo mostDangerousBuilding = getMostDangerBulding(dangerous_targets, unit);

        boolean fleeing = calculateFlee(leader, mostDangerousUnit, mostDangerousBuilding, unit);
        goalPos = calculatePosition(fleeing, leader, mostDangerousUnit, mostDangerousBuilding, unit);

        return goalPos;
    }

    private boolean calculateFlee(Unit leader, UnitInfo mostDangerousUnit, UnitInfo mostDangerousBuilding, Unit buildingUnit) {

        boolean fleeing = false;

        if(mostDangerousUnit != null){
            if(mostDangerousUnit.getUnit().isInWeaponRange(buildingUnit)){
                if(leader != null && leader.isInWeaponRange(mostDangerousUnit.getUnit())){
                    fleeing = false;
                }else{
                    fleeing = true;
                }
            }
        }

        if(mostDangerousBuilding != null){
            if(mostDangerousBuilding.getUnit().isInWeaponRange(buildingUnit)){
                fleeing = true;
            }
        }

        return fleeing;
    }

    private Position calculatePosition(boolean fleeing, Unit leader, UnitInfo mostDangerousUnit, UnitInfo mostDangerousBuilding, Unit buildingUnit) {

        Position leaderPos = leader.getPosition();
        Position goalPos = null;
        Position orderPos = StrategyIdea.mainPosition;

        Position halfway;
        Chokepoint SC = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
        if(InformationManager.Instance().enemyRace == Race.Terran) {
            halfway = new Position((SC.getX()*3+2048)/4, (SC.getY()*3+2048)/4);
        }else{
            halfway = new Position((SC.getX()*5+2048)/6, (SC.getY()*5+2048)/6);
        }


        if(fleeing){
            goalPos = leaderPos;
        }else{

            if(mostDangerousBuilding != null){
                if(mostDangerousBuilding.getUnit().isVisible() == false){
                    goalPos = mostDangerousBuilding.getLastPosition();
                }
            }
            if(buildingUnit.getDistance(leader) > 350){
                goalPos = leaderPos;
            }

            if(leader == null) {
                goalPos = halfway;
            }else {

                if(mostDangerousUnit != null){
                    if(buildingUnit.getDistance(leader) > 128){
                        goalPos = buildingUnit.getPosition();
                    }
                }

                if (leaderPos.getDistance(orderPos) > SC.getDistance(orderPos)) {
                    goalPos = halfway;
                }

                if (leaderPos.getDistance(orderPos) < buildingUnit.getDistance(orderPos)) {
                    goalPos = orderPos;
                }
            }
        }

        return goalPos;
    }

    private UnitInfo getMostDangerTarget(List<UnitInfo> dangerous_targets, Unit buildingUnit){

        UnitInfo dangerUnit = null;
        double mostDangercheck = -99999;

        for (UnitInfo target : dangerous_targets) {
            if(target.getType().isBuilding() == true) {
                continue;
            }
            double temp = target.getType().airWeapon().maxRange() - target.getLastPosition().getDistance(buildingUnit.getPosition());
            if(temp > mostDangercheck){
                dangerUnit = target;
                mostDangercheck = temp;
            }
        }
        return dangerUnit;
    }

    private UnitInfo getMostDangerBulding(List<UnitInfo> dangerous_targets, Unit buildingUnit){

        UnitInfo dangerUnit = null;
        double mostDangercheck = -99999;

        //TODO should we add sunken and bunker to the list? to get sight?
        for (UnitInfo target : dangerous_targets) {
            if(target.getType().isBuilding() == false) {
                continue;
            }
            double temp = target.getType().airWeapon().maxRange() - target.getLastPosition().getDistance(buildingUnit.getPosition());
            if(temp > mostDangercheck){
                dangerUnit = target;
                mostDangercheck = temp;
            }
        }
        return dangerUnit;
    }

}
