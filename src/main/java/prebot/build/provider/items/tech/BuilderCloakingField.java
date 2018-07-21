package prebot.build.provider.items.tech;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;

public class BuilderCloakingField extends DefaultBuildableItem {
	//레이스 클로킹

    public BuilderCloakingField(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	if(BuildManager.Instance().buildQueue.getItemCount(TechType.Cloaking_Field) == 0) {
      		return false;
      	}
    	
    	if(Prebot.Broodwar.self().hasResearched(TechType.Cloaking_Field) || Prebot.Broodwar.self().isResearching(TechType.Cloaking_Field)){
    		return false;
    	}
    	
    	if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Wraith) > 4) {
    		return true;
    	}
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith) + Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Wraith) > 4) {
    	
    		return true;
    	}
    	
    	
    	
    	return false;

    }
}
