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

    	if (researchSelector.currentResearched <= 2) {
			setBlocking(true);
			setHighPriority(true);
		}
		return true;
    }
}
