public class BuilderIrradiate extends DefaultBuildableItem {

	ResearchSelector researchSelector;

	public BuilderIrradiate(MetaType metaType, ResearchSelector researchSelector) {
		super(metaType);
		this.researchSelector = researchSelector;
	}

	public final boolean buildCondition() {

		if (researchSelector.getSelected().equals(metaType)) {
			return true;
		} else {
//			if (BuildQueueProvider.Instance().respondSet) {
//				if (StrategyIdea.currentStrategy == EnemyStrategy.ZERG_FAST_MUTAL || StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_PROTOSS_AIR3) {
//					if (Prebot.Broodwar.self().hasResearched(TechType.Irradiate) == false && Prebot.Broodwar.self().isResearching(TechType.Irradiate) == false) {
//						if (BuildManager.Instance().buildQueue.getItemCount(TechType.Irradiate) < 1) {
//							// BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate);
//							return true;
//						}
//					}
//				}
//			}
			return false;
		}
	}
}
