public class BuilderCaduceusReactor extends DefaultBuildableItem {

    //ResearchSelector researchSelector;
	UpgradeSelector upgradeSelector;

    public BuilderCaduceusReactor(MetaType metaType, UpgradeSelector upgradeSelector){
        super(metaType);
        this.upgradeSelector = upgradeSelector;
    }

    public final boolean buildCondition(){

        if(upgradeSelector.getSelected().equals(metaType)) {
            return true;
        }else{
            return false;
        }
    }
}
