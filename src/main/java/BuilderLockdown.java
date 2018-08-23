public class BuilderLockdown extends DefaultBuildableItem {

	ResearchSelector researchSelector;

	public BuilderLockdown(MetaType metaType, ResearchSelector researchSelector) {
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
