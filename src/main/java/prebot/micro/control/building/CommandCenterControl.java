package prebot.micro.control.building;

import java.util.Collection;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.MyBotModule;
import prebot.common.util.InfoUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.FlyCondition;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder;

public class CommandCenterControl extends BuildingFlyControl {
	
	public int setInt = 0;

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
        	if (skipControl(unit)) {
        		continue;
        	}
        	
        	setInt = 0;

            buildingFlyMap.put(unit, new FlyCondition());

			Unit firstCC = InformationManager.Instance().getFirstCC();
			if (firstCC != null && unit.getID() == firstCC.getID()) {
				continue;
			}

			Unit secondCommandCenter = getSecondCommandCenter();
			if (secondCommandCenter != null && unit.getID() == secondCommandCenter.getID()) {
				setInt = 1;
				processFly(unit);
			}

			Unit wrongPositionCommand = getWrongPositionCommand();
			if (wrongPositionCommand != null && unit.getID() == wrongPositionCommand.getID()) {
				setInt = 2;
				processFly(unit);
			}
        }
	}

    @Override
    public void checkFlyCondition(Unit checkCC) {

        if (checkCC != null) {
        	if(setInt == 1) {
	            BaseLocation correctLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
	
	            if (checkCC.isLifted() == false) {
	            	if (StrategyIdea.campType != PositionFinder.CampType.INSIDE && StrategyIdea.campType != PositionFinder.CampType.FIRST_CHOKE) {
	            		if (checkCC.getTilePosition().getX() != correctLocation.getTilePosition().getX() || checkCC.getTilePosition().getY() != correctLocation.getTilePosition().getY()) {
	                        buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.UP);
	                    }
	            	}
	                
	            } else {
	                buildingFlyMap.get(checkCC).setLandPosition(new TilePosition(correctLocation.getTilePosition().getX(), correctLocation.getTilePosition().getY()));
	                buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.DOWN);
	            }
        	}else if(setInt == 2) {
        		
        		TilePosition correctLocation = TilePosition.None;
        		for (BaseLocation targetBaseLocation : BWTA.getBaseLocations()) {
        			if(checkCC.getDistance(targetBaseLocation.getPosition())<100) {
        				correctLocation = targetBaseLocation.getTilePosition();
        				break;
        			}
        		}
        		
        		if (TilePositionUtils.isValidTilePosition(correctLocation)) {
	        		int tot_ontile_unit = 0;
	        		
	        		for(int x = 0; x < 4 ; x++) {
	        			for(int y = 0; y < 3 ; y++) {
	        				List<Unit> unitOnBaseTile = MyBotModule.Broodwar.getUnitsOnTile(correctLocation.getX()+x, correctLocation.getY()+y);
	        				for(Unit unit : unitOnBaseTile) {
	        					if(unit.getType() != UnitType.Terran_Command_Center) {
	        						tot_ontile_unit ++;
	        					}
	        				}
	        			}
	        		}
	            	
	            	if(tot_ontile_unit == 0) {
	            		if (checkCC.isLifted() == false) {
	                        buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.UP);
	    	            } else {
	    	                buildingFlyMap.get(checkCC).setLandPosition(correctLocation);
	    	                buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.DOWN);
	    	            }
	            	}
        		}
        		
        	}
        	
        }

    }

    public Unit getSecondCommandCenter(){
    	
    	for (Unit commandCenter : UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
    		if (commandCenter.getTilePosition().equals(BlockingEntrance.Instance().starting)) {
    			continue;
    		}
    		Region centerRegion = BWTA.getRegion(commandCenter.getPosition());
    		Region baseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
    		if (centerRegion == baseRegion) {
    			return commandCenter;
    		}
    		Region expansionRegion = BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
    		if (centerRegion == expansionRegion || centerRegion == InfoUtils.myThirdRegion()) {
    			return commandCenter;
    		}
    	}
    	return null;
    }
    
    public Unit getWrongPositionCommand(){
    	
    	if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) < 3) {
    		return null;
    	}
    	
    	boolean nextCommand = false;
        for(Unit commandCenter : UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
        	nextCommand = false;
        	for (BaseLocation targetBaseLocation : BWTA.getBaseLocations()) {
//        		20180821. hkk. 커맨드 센터와 일치하는 베이스 로케이션이 있으면 다음 커맨드센터 확인.
    			if(TilePositionUtils.equals(commandCenter.getTilePosition(), targetBaseLocation.getTilePosition())) {
    				nextCommand = true;
    				break;
    			}
    			 
    		}
        	if(nextCommand) continue;
        	
        	List<Unit> nearTurret = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.SELF, commandCenter.getPosition(), 250, UnitType.Terran_Missile_Turret);
        	if(nearTurret.size() > 0) {
        		return commandCenter;
        	}
        	
        	

//        	
//        	List<Unit> unitOnBaseTile = MyBotModule.Broodwar.getUnitsOnTile(commandCenter.getTilePosition());
//        	if(unitOnBaseTile.size() != 0) {
//        		continue;
//        	}
        	
        }
    	return null;
    }

}
