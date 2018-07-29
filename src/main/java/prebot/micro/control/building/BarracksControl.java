package prebot.micro.control.building;

import java.util.Collection;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.micro.control.FlyCondition;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class BarracksControl extends BuildingFlyControl {

    @Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
        //setFlyPosition(getFlyPosition0(unitList));
        for(Unit unit :  unitList){
            buildingFlyMap.put(unit, new FlyCondition(false, true, BlockingEntrance.Instance().barrack));
            processFly(unit);
        }
	}

    @Override
    public void checkFlyCondition(Unit unit) {

        FlyCondition flyCondition = buildingFlyMap.get(unit);

        if(flyCondition.getBuildingFly() == BuildingFly.DOWN && !marinInBuildManager()){

            if((StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.TWOGATE))
                    && UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Vulture) >= 3 ){
                flyCondition.setBuildingFly(BuildingFly.UP);
            }else if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK) {
            	CommonCode.RegionType regionType = PositionUtils.positionToRegionType(StrategyIdea.campPosition);
                if (regionType != CommonCode.RegionType.MY_BASE) {
                    flyCondition.setBuildingFly(BuildingFly.UP);
                }

            }else{
                if( UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Vulture) >= 1){
                    flyCondition.setBuildingFly(BuildingFly.UP);
                }
            }
        }
        //System.out.println("change fly: " + getBuildingFly());
    }
}
