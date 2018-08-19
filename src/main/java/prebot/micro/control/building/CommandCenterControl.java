package prebot.micro.control.building;

import java.util.Collection;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.FlyCondition;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder;

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
            	if (StrategyIdea.campType != PositionFinder.CampType.INSIDE && StrategyIdea.campType != PositionFinder.CampType.FIRST_CHOKE) {
            		if (checkCC.getTilePosition().getX() != correctLocation.getTilePosition().getX() || checkCC.getTilePosition().getY() != correctLocation.getTilePosition().getY()) {
                        buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.UP);
                    }
            	}
                
            } else {
                buildingFlyMap.get(checkCC).setLandPosition(new TilePosition(correctLocation.getTilePosition().getX(), correctLocation.getTilePosition().getY()));
                buildingFlyMap.get(checkCC).setBuildingFly(BuildingFly.DOWN);
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

}
