package prebot.build.provider.items.tech;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;

public class BuilderIonThrusters extends DefaultBuildableItem {

    ResearchSelector researchSelector;

    public BuilderIonThrusters(MetaType metaType, ResearchSelector researchSelector){
        super(metaType);
        this.researchSelector = researchSelector;
    }

    public final boolean buildCondition(){
    	
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderIonThrusters || buildCondition || researchSelector => " + researchSelector.getSelected().toString());
//    	FileUtils.appendTextToFile("log.txt", "\n BuilderIonThrusters || buildCondition || metaType =>" + metaType.toString());

        //if(researchSelector.getSelected().getUpgradeType().equals(metaType.getUpgradeType())) {
    	
		if (researchSelector.getSelected() == null) {
			return false;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) != 0) {
			return false;
		}
		if (!researchSelector.getSelected().isUpgrade()) {
			return false;
		}
		if (researchSelector.getSelected().getUpgradeType() != metaType.getUpgradeType()) {
			return false;
		}
		if (Prebot.Broodwar.self().isUpgrading(researchSelector.getSelected().getUpgradeType())) {
			return false;
		}
    	
//    	if(researchSelector.getSelected() != null){
//	    	if(researchSelector.getSelected().isUpgrade()) {
//		    	if(researchSelector.getSelected().getUpgradeType() == metaType.getUpgradeType()) {
//		    		if(BuildManager.Instance().buildQueue.getItemCount(researchSelector.getSelected(), null) == 0) {
//		    	//if(researchSelector.getSelected().equals(metaType)) {
//		//        	FileUtils.appendTextToFile("log.txt", "\n BuilderIonThrusters || researchSelector => " + researchSelector.getSelected().getUpgradeType() + " || metaType => " + metaType.getUpgradeType());
//			        	if (researchSelector.currentResearched <= 2) {
//			//        		FileUtils.appendTextToFile("log.txt", "\n BuilderIonThrusters || block & high");
//			        		setBlocking(true);
//			        		setHighPriority(true);
//			        	}
//			        	System.out.println(" research return true ==> ion_thrusters");
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
