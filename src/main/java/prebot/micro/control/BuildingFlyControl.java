package prebot.micro.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.build.prebot1.BuildManager;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

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
                	 if (InformationManager.Instance().firstBarrack != null && InformationManager.Instance().barrackStart + 24*3 > Prebot.Broodwar.getFrameCount() && checkEnemyNearBy(unit) == false) {
                         flyCondition.setBuildingFly(BuildingFly.UP);
                         //System.out.println("wait!");
                     } else {
						if (WorkerManager.Instance().scvIsOutOfBase() && checkEnemyNearBy(unit) == false) {
							flyCondition.setBuildingFly(BuildingFly.UP);
                    	}else {
                    		flyCondition.setBuildingFly(BuildingFly.DOWN);
                    	}
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

        List<Unit> attackUnit = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Siege_Mode);
        attackUnit.addAll(UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode));
        attackUnit.addAll(UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Goliath));
        List<UnitInfo> enemyUnit = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.VISIBLE,
        		UnitType.Terran_Marine, UnitType.Terran_Goliath, UnitType.Terran_Wraith, UnitType.Terran_Valkyrie, UnitType.Terran_Battlecruiser, UnitType.Terran_Missile_Turret,
        		UnitType.Protoss_Dragoon, UnitType.Protoss_Archon, UnitType.Protoss_Corsair, UnitType.Protoss_Scout, UnitType.Protoss_Carrier, UnitType.Protoss_Photon_Cannon,
        		UnitType.Zerg_Hydralisk, UnitType.Zerg_Mutalisk, UnitType.Zerg_Devourer, UnitType.Zerg_Spore_Colony);
        
        Unit leader = UnitUtils.leaderOfUnit(attackUnit);

        Position goalPos = null;

        UnitInfo mostDangerousUnit = getMostDangerTarget(enemyUnit, unit);
        UnitInfo mostDangerousBuilding = getMostDangerBulding(enemyUnit, unit);

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

    	Position leaderPos = null;
    	if(leader != null) {
    		leaderPos = leader.getPosition();
    	}
        Position goalPos = null;
        Position orderPos = StrategyIdea.mainPosition;

        Position halfway;
        Chokepoint SC = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
        if(InformationManager.Instance().enemyRace == Race.Terran) {
            halfway = new Position((SC.getX()*3+2048)/4, (SC.getY()*3+2048)/4);
        }else{
            halfway = new Position((SC.getX()*5+2048)/6, (SC.getY()*5+2048)/6);
        }
    
        if (InfoUtils.enemyBase() == null) {
        	return orderPos;
        }
        
        boolean expansionOccupied = false;    
        List<BaseLocation> enemyBases = InfoUtils.enemyOccupiedBases();
		for (BaseLocation enemyBase : enemyBases) {
			if (enemyBase.equals(InfoUtils.enemyFirstExpansion())) {
				expansionOccupied = true;
				break;
			}
		}

		BaseLocation attackBase = expansionOccupied ? InfoUtils.enemyFirstExpansion() : InfoUtils.enemyBase();

        if(fleeing){
        	if(leader == null) {
        		goalPos = SC.getPoint();
        	}else {
        		goalPos = leaderPos;
        	}
        }else{

        	if (orderPos.getDistance(attackBase) > SC.getDistance(attackBase)) {
                goalPos = halfway;
            }else {
        	
	            if(mostDangerousBuilding != null){
	                if(mostDangerousBuilding.getUnit().isVisible() == false){
	                    goalPos = mostDangerousBuilding.getLastPosition();
	                }
	            }
	           
	            if(leader == null) {
	                goalPos = halfway;
	            }else {
	            	
	            	if(buildingUnit.getDistance(leader) > 350){
	                    goalPos = leaderPos;
	                }
	
	                if(mostDangerousUnit != null){
	                	Position enemy = mostDangerousUnit.getLastPosition().getPoint();
	                	int myDist = buildingUnit.getDistance(leader);
	                	if(enemy.getDistance(buildingUnit) + myDist <= 135) {
		                    if(myDist > 128){
		                        goalPos = buildingUnit.getPosition();
		                    }
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
