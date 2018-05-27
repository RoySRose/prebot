package prebot.build.provider;


import bwapi.TechType;
import prebot.common.MetaType;

public class ResearchSelector implements Selector<MetaType>{

    MetaType metaType;
    //BuildCondition buildCondition;

    public final MetaType getSelected(){
        return metaType;
    }

    public final void select(){

        //buildCondition = new BuildCondition();
        metaType = new MetaType(TechType.Tank_Siege_Mode);
    }

  }
