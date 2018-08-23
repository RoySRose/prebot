

import java.util.Collection;

import bwapi.Unit;
import bwapi.UnitType;

public class BarracksControl extends BuildingFlyControl {

    @Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {


        for(Unit unit :  unitList){
        	if (skipControl(unit)) {
        		continue;
        	}
            buildingFlyMap.put(unit, new FlyCondition(false, true, BlockingEntrance.Instance().barrack));

            if (MyBotModule.Broodwar.getFrameCount() > 10000) {
                buildingFlyMap.get(unit).setFlyPosition(getFlyPosition0(unit));
            }

            processFly(unit);
        }
	}

    @Override
    public void checkFlyCondition(Unit unit) {

        FlyCondition flyCondition = buildingFlyMap.get(unit);

        if(flyCondition.getBuildingFly() == BuildingFly.DOWN && !marinInBuildManager()){

			// 앞마당으로 바뀐 경우 안전한 상황이라고 가정한다. 공격 또는 커맨드 건설 등을 위해 배럭을 띄운다.
            if (StrategyIdea.campType != PositionFinder.CampType.INSIDE && StrategyIdea.campType != PositionFinder.CampType.FIRST_CHOKE) {
                flyCondition.setBuildingFly(BuildingFly.UP);
                
            } else {
            	if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.TWOGATE)) {
            		if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Vulture) >= 3) {
    					flyCondition.setBuildingFly(BuildingFly.UP);
    				}
                    
    			} else if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK) {
    				if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Vulture) >= 3) {
    					flyCondition.setBuildingFly(BuildingFly.UP);
    				}

    			} else {
    				if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Vulture) >= 1) {
    					flyCondition.setBuildingFly(BuildingFly.UP);
    				}
                }	
            }
            
        }
        //System.out.println("change fly: " + getBuildingFly());
    }
}
