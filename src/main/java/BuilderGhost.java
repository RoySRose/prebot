public class BuilderGhost extends DefaultBuildableItem {

	BarrackUnitSelector barrackUnitSelector;

	public BuilderGhost(MetaType metaType, BarrackUnitSelector barrackUnitSelector) {
		super(metaType);
		this.barrackUnitSelector = barrackUnitSelector;
	}

	public final boolean buildCondition() {

		if (barrackUnitSelector.getSelected().equals(metaType.getUnitType())) {
			return true;
		} else {
			return false;
		}
	}
}
