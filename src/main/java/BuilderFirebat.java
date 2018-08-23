public class BuilderFirebat extends DefaultBuildableItem {

    BarrackUnitSelector barrackUnitSelector;

    public BuilderFirebat(MetaType metaType, BarrackUnitSelector barrackUnitSelector){
        super(metaType);
        this.barrackUnitSelector = barrackUnitSelector;
    }

    public final boolean buildCondition(){

        if(barrackUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
            return false;
        }
    }
}
