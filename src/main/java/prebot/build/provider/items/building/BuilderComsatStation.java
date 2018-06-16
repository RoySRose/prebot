package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

public class BuilderComsatStation extends DefaultBuildableItem {

    public BuilderComsatStation(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("ComsatStation build condition check");
        
        
        if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Comsat_Station) && UnitUtils.myFactoryUnitSupplyCount() >= 32) {
			// 컴셋이 없다면
			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Academy)) {
				return false;
			} else {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) > 0) {
					// 아카데미가 완성되었고
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0) {
						// 빌드큐에 컴셋이 없는데, 아카데미가 완성되었다면빌드큐에 컴셋 입력
						if (Prebot.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
							/*BuildManager.Instance().buildQueue
									.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);*/
							return true;
						}
					}
				}
			}
		}
        return false;
    }


}
