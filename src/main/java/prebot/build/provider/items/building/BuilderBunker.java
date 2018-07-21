package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;

public class BuilderBunker extends DefaultBuildableItem {

    public BuilderBunker(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
		// TODO
    	// inital에 들어가야 하고 들어가야 한다.
    	// 입구가 막히지 않았을 때 건설. 케이스별 생각해볼게 있음
    	
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 3) {
    		return false;
    	}
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Bunker)) {
			return false;
		}
    	
		boolean entranceBlocked = InformationManager.Instance().isBlockingEnterance();
		if (!entranceBlocked) {
			setBlocking(true);
			setHighPriority(true);
			setTilePosition(BlockingEntrance.Instance().bunker);
			return true;
		}
		
		return false;
    }
}
