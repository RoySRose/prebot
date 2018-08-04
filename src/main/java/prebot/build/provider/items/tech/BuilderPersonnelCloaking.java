package prebot.build.provider.items.tech;

import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.common.MetaType;

public class BuilderPersonnelCloaking extends DefaultBuildableItem {
	// 고스트 클로킹
	ResearchSelector researchSelector;

	public BuilderPersonnelCloaking(MetaType metaType, ResearchSelector researchSelector) {
		super(metaType);
		this.researchSelector = researchSelector;
	}

	public final boolean buildCondition() {

		if (researchSelector.getSelected().equals(metaType)) {
			return true;
		} else {
			return false;
		}
	}
}
