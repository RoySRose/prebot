package prebot.build.provider.items.tech;

import bwapi.TechType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.strategy.StrategyManager;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;

public class BuilderIrradiate extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderIrradiate(MetaType metaType, ResearchSelector researchSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){
    	
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {

	        if(researchSelector.getSelected().equals(metaType)) {
	            return true;
	        }else{
	        	if(BuildQueueProvider.Instance().respondSet) {
	        		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_MUTALMANY){
	        			if(Prebot.Broodwar.self().hasResearched(TechType.Irradiate) ==false &&Prebot.Broodwar.self().isResearching(TechType.Irradiate) ==false){
	        				if(BuildManager.Instance().buildQueue.getItemCount(TechType.Irradiate) < 1){
	//        					BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate);
	        					return true;
	        				}
	        			}
	        		}
	        	}
	            return false;
	        }
    	}
    }
}
