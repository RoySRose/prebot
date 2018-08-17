package prebot.build.provider.items.tech;

import prebot.build.prebot1.BuildManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;

public class BuilderSpiderMines extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderSpiderMines(MetaType metaType, ResearchSelector researchSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){
    	
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderSpiderMines || buildCondition || researchSelector => " + researchSelector.getSelected().toString());
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderSpiderMines || buildCondition || metaType =>" + metaType.toString());

    	
    	
		if (researchSelector.getSelected() == null) {
			return false;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) != 0) {
			return false;
		}
		if (!researchSelector.getSelected().isTech()) {
			return false;
		}
		if (researchSelector.getSelected().getTechType() != metaType.getTechType()) {
			return false;
		}
		if (Prebot.Broodwar.self().isResearching(researchSelector.getSelected().getTechType())) {
			return false;
		}

//    	if(researchSelector.getSelected() != null){
//	    	if(researchSelector.getSelected().isTech()) {
//		    	if(researchSelector.getSelected().getTechType() == metaType.getTechType()) {
//		    		if(BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) == 0) {
//		    	//if(researchSelector.getSelected().equals(metaType)) {
//		//        	FileUtils.appendTextToFile("log.txt", "\n BuilderSpiderMines || researchSelector => " + researchSelector.getSelected().getTechType() + " || metaType => " + metaType.getTechType());
//			        	if (researchSelector.currentResearched <= 2) {
//			//        		FileUtils.appendTextToFile("log.txt", "\n BuilderSpiderMines || block & high");
//			        		setBlocking(true);
//			        		setHighPriority(true);
//			        	}
//			            return true;
//		    		}
//		        }
//	    	}
//    	}
    	if (researchSelector.currentResearched <= 2) {
			setBlocking(true);
			setHighPriority(true);
		}
		return true;
    }
}
