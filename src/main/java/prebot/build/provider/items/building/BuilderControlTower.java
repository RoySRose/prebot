package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.RespondToStrategy;

public class BuilderControlTower extends DefaultBuildableItem {

    public BuilderControlTower(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("ControlTower build condition check");
        int cc = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center);
        
        if ((RespondToStrategy.Instance().need_vessel == true && cc >= 2) || cc >= 3) {
			
			if (UnitUtils.myUnitDiscovered(UnitType.Terran_Starport)) {
				// 컨트롤 타워가 없다면
				if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Control_Tower)) {
					if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower, null)
								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
							//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower, true);
							return true;
						}
					}
				}
			}
		}
        
        return true;
    }


}
