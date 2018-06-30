package prebot.build.provider.items.building;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;

public class BuilderSupplyDepot extends DefaultBuildableItem {

    public BuilderSupplyDepot(MetaType metaType){
        super(metaType);
    }

    @Override
    public boolean checkInitialBuild(){
        return true;
    }

    public final boolean buildCondition(){
    	
    	BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem checkItem = null;
    	
    	
//    	frame으로 처리되어 있는 이유. initial이 끝난 후로 하면 되지 않나?
//    	일단 기존 조건대로 처리. 셀렉터는 이니셜 빌드 이후에 도므로 아래 조건의 필요 유무 판단
    	
    	if (!(Prebot.Broodwar.getFrameCount() % 29 == 0 && Prebot.Broodwar.getFrameCount() > 4500)) {
    		
    		return false;
    	}

        if (Prebot.Broodwar.self().supplyTotal() >= 400) {
            return false;
        }
        
        
        if (!tempbuildQueue.isEmpty()) {
			checkItem = tempbuildQueue.getHighestPriorityItem();
			while (true) {
				if (checkItem.blocking == true) {
					break;
				}
				// if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
				// return;
				// }
				if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret) {
					return false;
				}
				if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
					return false;
				}
				if (tempbuildQueue.canSkipCurrentItem() == true) {
					tempbuildQueue.skipCurrentItem();
				} else {
					break;
				}
				checkItem = tempbuildQueue.getItem();
			}
			if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
				return false;
			}
		}
        
        

        // 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
        // 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
        int supplyMargin = 4;
        boolean barrackflag = false;
        boolean factoryflag = false;

        if(factoryflag==false){
            for (Unit unit : Prebot.Broodwar.self().getUnits()) {
                if (unit.getType() == UnitType.Terran_Factory  && unit.isCompleted()) {
                    factoryflag = true;
                }
                if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
                    barrackflag = true;
                }
            }
        }

        int Faccnt=0;
        int CCcnt=0;
        int facFullOperating =0;
        for (Unit unit : Prebot.Broodwar.self().getUnits())
        {
            if (unit == null) continue;
            if (unit.getType().isResourceDepot() && unit.isCompleted()){
                CCcnt++;
            }
            if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
                Faccnt ++;
                if(unit.isTraining() == true){
                    facFullOperating++;
                }
            }
        }

        if(CCcnt == 1){//TODO 이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨
            if(factoryflag==false && barrackflag==true){
                supplyMargin = 5;
            }else if(factoryflag==true){
                supplyMargin = 6+4*Faccnt+facFullOperating*2;
            }
        }else{ //if((MyBotModule.Broodwar.getFrameCount()>=6000 && MyBotModule.Broodwar.getFrameCount()<10000) || (Faccnt > 3 && CCcnt == 2)){
            supplyMargin = 11+4*Faccnt+facFullOperating*2;
        }

        // currentSupplyShortage 를 계산한다
        int currentSupplyShortage = Prebot.Broodwar.self().supplyUsed() + supplyMargin + 1 - Prebot.Broodwar.self().supplyTotal();

        if (currentSupplyShortage > 0) {
            // 생산/건설 중인 Supply를 센다
            int onBuildingSupplyCount = 0;
            // 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
            onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
                    UnitType.Terran_Supply_Depot, null)
                    * UnitType.Terran_Supply_Depot.supplyProvided();

            if (currentSupplyShortage > onBuildingSupplyCount) {
                setHighPriority(true);
                setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextSupplePoint);
                //this.setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextSupplePoint);
                //System.out.println("return supply true");
                //FileUtils.appendTextToFile("log.txt", "\n return supply true ==>>> ");
                return true;
            }
        }

        return false;
    }
}
