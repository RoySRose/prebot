package prebot.build.provider.items.tech;

import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.build.provider.UpgradeSelector;
import prebot.common.MetaType;
import prebot.common.util.FileUtils;

public class BuilderCharonBoosters extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderCharonBoosters(MetaType metaType, ResearchSelector researchSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){
    	
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderCharonBoosters || buildCondition || researchSelector => " + researchSelector.getSelected().toString());
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderCharonBoosters || buildCondition || metaType =>" + metaType.toString());

    	//if(researchSelector.getSelected().getUpgradeType().equals(metaType.getUpgradeType())) {
    	if(BuildQueueProvider.Instance().respondSet) {
    		return false;
    	}else {
	    	if(String.valueOf(researchSelector.getSelected()) != "null"){
		    	if(researchSelector.getSelected().isUpgrade()) {
			    	if(researchSelector.getSelected().getUpgradeType() == metaType.getUpgradeType()) {
			    		if(BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) == 0) {
				    	//if(researchSelector.getSelected().equals(metaType)) {
				        	//FileUtils.appendTextToFile("log.txt", "\n BuilderCharonBoosters || researchSelector => " + researchSelector.getSelected().getUpgradeType() + " || metaType => " + metaType.getUpgradeType());
				        	if (researchSelector.currentResearched <= 2) {
				        		//FileUtils.appendTextToFile("log.txt", "\n BuilderCharonBoosters || block & high");
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
