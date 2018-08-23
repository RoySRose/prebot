public class BuilderBattlecruiser extends DefaultBuildableItem {

	StarportUnitSelector starportUnitSelector;

	public BuilderBattlecruiser(MetaType metaType, StarportUnitSelector starportUnitSelector) {
		super(metaType);
		this.starportUnitSelector = starportUnitSelector;
	}

	public final boolean buildCondition() {

		if (starportUnitSelector.getSelected().equals(metaType.getUnitType())) {
			return true;
		} else {
			return false;
		}

	}
}
