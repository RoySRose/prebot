package prebot.build.provider.items.tech;

import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;

public class BuilderTankSiegeMode extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderTankSiegeMode(MetaType metaType, ResearchSelector researchSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {
    	
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderTankSiegeMode || buildCondition || researchSelector => " + researchSelector.getSelected().toString());
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderTankSiegeMode || buildCondition || metaType =>" + metaType.toString());
    	//FileUtils.appendTextToFile("log.txt", "\n BuilderTankSiegeMode || buildCondition => " + researchSelector.getSelected().getTechType() + " || metaType => " + metaType.getTechType());
	    	if(String.valueOf(researchSelector.getSelected()) != "null"){
		    	if(researchSelector.getSelected().isTech()) {
			    	if(researchSelector.getSelected().getTechType() == metaType.getTechType() && Prebot.Broodwar.self().isResearching(researchSelector.getSelected().getTechType()) == false) {
			    		if(BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) == 0) {
			    	//if(researchSelector.getSelected().equals(metaType)) {
			//        	FileUtils.appendTextToFile("log.txt", "\n BuilderTankSiegeMode || researchSelector => " + researchSelector.getSelected().getTechType() + " || metaType => " + metaType.getTechType());
			        	
				        	if (researchSelector.currentResearched <= 2) {
				        		setBlocking(true);
				        		setHighPriority(true);
				        	}
				            return true;
			    		}
			        }
		    	}
	    	}
    	}
    	return false;
    }
}
