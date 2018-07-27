package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions.AddOnOption;

public class BuilderVulture extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderVulture(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }

    public final boolean buildCondition(){
    	//FileUtils.appendTextToFile("log.txt", "\n BuilderVulture ==>>> " + metaType.getName());
    	
    	if(StrategyIdea.currentStrategy.addOnOption == AddOnOption.IMMEDIATELY && !UnitUtils.myUnitDiscovered(UnitType.Terran_Machine_Shop)){
			return false;
		}
    	

        if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
//       		if(InformationManager.Instance().enemyRace == Race.Zerg) {
//	        	if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_ONLYLING){
//	    			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().selfPlayer) < 5	){
//	    				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) < 1){
////	    						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//	    					setHighPriority(true);
//	    					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
//	    					return true;
//	    				}
//	    			}
//	    		}
//    		}
        	List<Unit> factory = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
        	
        	if(Prebot.Broodwar.self().minerals() > Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) * 100
        		&& factory.size() >= 4) {
        	
	      		int notConstFac = 0;
	      		
//	      		List<Unit> factory = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
	      		for(Unit unit : factory) {
	      			if(!unit.isConstructing()) {
	      				notConstFac++;
	      			}
	      		}
	      		
	      		//쉬고있는 팩토리가 전체 팩의 절반이상일경우 그냥 벌쳐. 빌드큐에 있으면 넣지 않는다.
	      		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) / 2 <= notConstFac) {
	      			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) == 0){
	      				return true;
	      			}
	      		}
	            return false;
        	}
        	return false;
        }
    }
}
