package prebot.build.provider;


import bwapi.UpgradeType;
import prebot.common.MetaType;

public class UpgradeSelector implements Selector<MetaType>{

    MetaType metaType;
    //BuildCondition buildCondition;

    public final MetaType getSelected(){
        return metaType;
    }

    public final void select(){

        //buildCondition = new BuildCondition();
        metaType = new MetaType(UpgradeType.Terran_Infantry_Armor);

    }
    
    

  }
