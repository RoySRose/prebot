package prebot.micro.control.building;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
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

import java.util.List;

public class BarracksControl extends BuildingFlyControl {

    public BarracksControl() {
        super(false, true);
    }

    @Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
        processFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {

        if(getBuildingFly() == BuildingFly.DOWN && !marinInBuildManager()){

            if((StrategyIdea.currentStrategy.buildTimeMap.isTwoGate())
                    && UnitUtils.getUnitCount(UnitType.Terran_Vulture) >= 3 ){
                setBuildingFly(BuildingFly.UP);
            }else if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK) {
                BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
                if(StrategyIdea.campPosition == firstExpansion.getPosition()){
                    setBuildingFly(BuildingFly.UP);
                }
                BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
                CommonCode.RegionType regionType = PositionUtils.positionToRegionType(myFirstExpansion.getPosition());
                if (regionType != CommonCode.RegionType.MY_BASE) {
                    setBuildingFly(BuildingFly.UP);
                }

            }else{
                if(UnitUtils.getUnitCount(UnitType.Terran_Vulture) >= 1){
                    setBuildingFly(BuildingFly.UP);
                }
            }
        }
    }
}
