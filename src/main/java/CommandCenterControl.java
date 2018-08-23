

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

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
//				System.out.println(" this command is wrong position :: " + wrongPositionCommand.getTilePosition() + " :: has turret");
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
        			if(checkCC.getDistance(targetBaseLocation.getPosition())<200) {
        				correctLocation = targetBaseLocation.getTilePosition();
//        				System.out.println(" this is correctLocation");
        				break;
        			}
        		}
        		
        		if (TilePositionUtils.isValidTilePosition(correctLocation)) {
//        			System.out.println(" command center go to correctLocation :: " + correctLocation.getX() + " :: " + correctLocation.getY());
	        		int tot_ontile_unit = 0;
	        		
	        		for(int x = 0; x < 4 ; x++) {
	        			for(int y = 0; y < 3 ; y++) {
	        				List<Unit> unitOnBaseTile = MyBotModule.Broodwar.getUnitsOnTile(correctLocation.getX()+x, correctLocation.getY()+y);
	        				for(Unit unit : unitOnBaseTile) {
//	        					System.out.println(unit.getType() + " :: " + unit.getTilePosition() + " is at " + (correctLocation.getX()+x) + " / " + (correctLocation.getY()+y));
	        					if(unit.getType() != UnitType.Terran_Command_Center 
	        							&& unit.getType() != UnitType.Terran_Missile_Turret
	        							&& unit.getType() != UnitType.Terran_SCV) {
	        						tot_ontile_unit ++;
	        					}
	        				}
	        			}
	        		}
	            	
	            	if(tot_ontile_unit == 0) {
//	            		System.out.println(" command center gogogogogogogo!!!!!!!!!!!!!!!!!!!!!!");
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
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) < 3) {
			return null;
		}
    	
    	HashSet<TilePosition> baseTileSet = BaseLocationUtils.getBaseLocationTileHashSet();
        for(Unit commandCenter : UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
			// 20180821. hkk. 커맨드 센터와 일치하는 베이스 로케이션이 있으면 다음 커맨드센터 확인.
			// 20180822. ojw. 커맨드 센터가 올라가 있을 때 base와 tile이 일치하는 상황 커버
        	if (!commandCenter.isLifted() && baseTileSet.contains(commandCenter.getTilePosition())) {
				continue;
        	}
        	
//        	System.out.println(" this command is wrong position :: " + commandCenter.getTilePosition());
        	List<Unit> nearTurret = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.SELF, commandCenter.getPosition(), 250, UnitType.Terran_Missile_Turret);
			if (nearTurret.size() > 0) {
//				System.out.println(" this command is wrong position :: " + commandCenter.getTilePosition() + " :: has turret");
				return commandCenter;
			}
			
			
//        	List<Unit> unitOnBaseTile = MyBotModule.Broodwar.getUnitsOnTile(commandCenter.getTilePosition());
//        	if(unitOnBaseTile.size() != 0) {
//        		continue;
//        	}
        	
        }
    	return null;
    }

}
