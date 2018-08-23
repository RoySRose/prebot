public class BuilderTankSiegeMode extends DefaultBuildableItem {

	ResearchSelector researchSelector;

	public BuilderTankSiegeMode(MetaType metaType, ResearchSelector researchSelector) {
		super(metaType);
		this.researchSelector = researchSelector;
	}

	public final boolean buildCondition() {
		
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
		if (MyBotModule.Broodwar.self().isResearching(researchSelector.getSelected().getTechType())) {
			return false;
		}

		if (researchSelector.currentResearched <= 2) {
			setBlocking(true);
			setHighPriority(true);
		}
		return true;
	}
}
