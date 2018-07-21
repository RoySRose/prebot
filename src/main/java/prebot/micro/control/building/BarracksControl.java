package prebot.micro.control.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class BarracksControl extends BuildingFlyControl {

    public BarracksControl() {
        super(false, true, BlockingEntrance.Instance().barrack);
    }

    @Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
        //setFlyPosition(getFlyPosition0(unitList));
    	System.out.println("inside barrack");
        processFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {

        if(getBuildingFly() == BuildingFly.DOWN && !marinInBuildManager()){
        	
            if((StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.TWOGATE))
                    && UnitUtils.getUnitCount(UnitType.Terran_Vulture) >= 3 ){
                setBuildingFly(BuildingFly.UP);
            }else if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK) {
            	CommonCode.RegionType regionType = PositionUtils.positionToRegionType(StrategyIdea.campPosition);
                if (regionType != CommonCode.RegionType.MY_BASE) {
                    setBuildingFly(BuildingFly.UP);
                }

            }else{
                if(UnitUtils.getUnitCount(UnitType.Terran_Vulture) >= 1){
                    setBuildingFly(BuildingFly.UP);
                }
            }
        }
        System.out.println("change fly: " + getBuildingFly());
    }
}
