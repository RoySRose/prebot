package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

public class BuilderFactory extends DefaultBuildableItem {

    public BuilderFactory(MetaType metaType){
        super(metaType);
    }
    
    public final boolean buildCondition(){
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
			return false;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) > 0) {
			return false;
		}
		
		// 배럭이 있고, 가스는 100 이상인데 팩토리가 없을경우. 첫팩은 무조건 있어야 하므로
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) == 0 && Prebot.Broodwar.self().gas() > 100) {
    		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory);
    		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);
    		if (buildQueueCount + constructionCount == 0) {
    			return true;    			
    		}
    	}
    	
		if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT) {
			List<Unit> commandCenterList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Factory);
			if (commandCenterList.size() < 2) {
				return false;
			}
    	} else if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_FACTORY) {
    		List<Unit> facotryList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Factory);
			List<Unit> commandCenterList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Command_Center);
			if (facotryList.size() >= 2 && commandCenterList.size() < 2) {
				return false;
			}
    	} else if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.ONE_FACTORY) {
    		List<Unit> facotryList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Factory);
			List<Unit> commandCenterList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Command_Center);
			if (facotryList.size() >= 1 && commandCenterList.size() < 2) {
				return false;
			}
    	}
		
		boolean factoryFullOperating = true;
		List<Unit> factoryList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Factory);
		for (Unit factory : factoryList) {
			if (!factory.isCompleted() || !factory.isTraining()) {
				factoryFullOperating = false;
				break;
			}
		}

		int completeFactoryCount = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory).size();
		int maxFactoryCount = 0;
		if (completeFactoryCount <= 1) {
			maxFactoryCount = 3;
		} else if (completeFactoryCount == 2) {
			maxFactoryCount = 6;
		} else if (completeFactoryCount >= 3) {
			maxFactoryCount = 9;
		}

		
		completeFactoryCount += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);

		int additonalmin = 0;
		int additonalgas = 0;
		if (!factoryFullOperating) {
			additonalmin = (completeFactoryCount - 1) * 40;
			additonalgas = (completeFactoryCount - 1) * 20;
		}

		if (completeFactoryCount == 0) {
			setBlocking(true);
			setHighPriority(true);
			return true;

		} else if (completeFactoryCount < maxFactoryCount) {
			if (Prebot.Broodwar.self().minerals() > 200 + additonalmin && Prebot.Broodwar.self().gas() > 100 + additonalgas) {
				if (completeFactoryCount == 2) {
					if (UnitUtils.myFactoryUnitSupplyCount() > 24 && factoryFullOperating) {
						return true;
					}
				} else if (completeFactoryCount <= 6) {
					return true;
				} else {
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.LastBuilingPoint);
					return true;
				}
			}
		}

        return false;
    }
    
    

}
