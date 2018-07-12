package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;

public class BuilderArmory extends DefaultBuildableItem {

    public BuilderArmory(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        //System.out.println("Armory build condition check");
        
        
//      투아머리가 있을것 같지만 일단 체크
    	if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Armory) > 0) {
    		return false;
    	}
    
//    	전략적 판단에 대한 부분은 리더에게 오더 받는다. 변경예정. 긴급할 경우 자원 체크로직이 필요한가? 어차피 맨위 true로 올릴텐데?
        if(RespondToStrategy.Instance().enemy_scout 
    		|| RespondToStrategy.Instance().enemy_shuttle 
    		|| RespondToStrategy.Instance().enemy_wraith){
        	
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
//						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
//								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
				setBlocking(true);
				setHighPriority(true);
				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				return true;
			}
		}
    
        if(RespondToStrategy.Instance().enemy_arbiter){
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
//				if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
//						&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
				setBlocking(true);
				setHighPriority(true);
				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
				return true;
			}
		}

		
//		15000 프레임 이후
		if (Prebot.Broodwar.getFrameCount() > 15000) {
    		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
				return true;
			}
		}
		
		
//		활성화된 커맨드가 2개 이상일 경우
		int myCommand = 0;
		
		List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Armory);
		
		for(Unit unit : CommandCenter) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 8) {
				myCommand++;
			}
		}
		
    	if (myCommand >= 2) {
    		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0) {
				return true;
			}
		}

        return false;
    }


}
