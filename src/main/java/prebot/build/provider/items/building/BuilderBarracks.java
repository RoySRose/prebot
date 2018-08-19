package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

public class BuilderBarracks extends DefaultBuildableItem {

    public BuilderBarracks(MetaType metaType){
        super(metaType);
    }
    
    public int setRecoverBarrack = 0;
    
    public int bfBarrack = 0;
    public int nowBarrack = 0;
    
    public final boolean buildCondition(){
    	
    	nowBarrack = Prebot.Broodwar.self().incompleteUnitCount(UnitType.Terran_Barracks) + Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks);
    	
    	if(nowBarrack > 0) {
    		bfBarrack = nowBarrack;
    	}
//    	빌드큐에서 빠져서 만들어지기 시작하거나 완성이 되면, 배럭은 그 수만큼 복구 대상이다.
//    	if(nowBarrack > 0 
//    		&& setRecoverBarrack != nowBarrack) {
//    		setRecoverBarrack = nowBarrack;
//    		System.out.println("barrack set Recover Item ==> " + UnitUtils.hasUnitOrWillBeCount(UnitType.Terran_Barracks));
//    		
//    	}
    	if(bfBarrack > 0 && bfBarrack > nowBarrack) {
    		setRecoverItemCount(setRecoverBarrack);
    		setHighPriority(true);
    	}
    	
    	int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks);
    	if (buildQueueCount > 0) {
    		return false;
    	}
    	int constructionQueueCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null);
    	if (constructionQueueCount > 0) {
			return false;
		}
    	
    	
		
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks) == 0) {
			return true;

		} else if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Barracks) == 1) {
			List<Unit> barracksList = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Barracks);

			for (Unit unit : barracksList) {
				if (unit.getHitPoints() < UnitType.Terran_Barracks.maxHitPoints() / 3) {
					return true;
				}
			}
		}

		return false;
    }
    
    @Override
    public boolean checkInitialBuild(){
  	
		return true;
    }

}
