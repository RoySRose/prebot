package prebot.build.provider.items.building;

import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;

public class BuilderMachineShop extends DefaultBuildableItem {
	
	public static boolean machine_shop_chk = false;

    public BuilderMachineShop(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	
    	
        System.out.println("MachineShop build condition check");
        if(machine_shop_chk)        return true;
        
        machine_shop_chk = false;
        
        return false;
    }


}
