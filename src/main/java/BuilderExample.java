public class BuilderExample extends DefaultBuildableItem {

    public BuilderExample(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        return true;
    }


}
