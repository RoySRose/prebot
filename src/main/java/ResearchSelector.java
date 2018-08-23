


import java.util.List;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

//연구해야될 UpgradeType 만
public class ResearchSelector implements Selector<MetaType>{
	BuildCondition buildCondition;
	MetaType metaType;
//    MetaType techType;
//    MetaType upgradeType;
    //BuildCondition buildCondition;

    public final MetaType getSelected(){
        return metaType;
    }

    public final void select(){
    	//metaType = new MetaType(UpgradeType.None);
   		//metaType = new MetaType(UpgradeType.None);
    	
	    	//if (Prebot.Broodwar.getFrameCount() % 43 == 0) {
    	metaType = new MetaType();
    	
		executeResearchChk();
			//}

    	

        //buildCondition = new BuildCondition();
        
    }
    
    public int currentResearched;
    
    public void executeResearchChk() {

		boolean VS = (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1 ? true : false)
				|| (MyBotModule.Broodwar.self().isUpgrading(UpgradeType.Ion_Thrusters) ? true : false);
		boolean VM = (MyBotModule.Broodwar.self().hasResearched(TechType.Spider_Mines)) || (MyBotModule.Broodwar.self().isResearching(TechType.Spider_Mines));
		boolean TS = (MyBotModule.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)) || (MyBotModule.Broodwar.self().isResearching(TechType.Tank_Siege_Mode));
		boolean GR = (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false)
				|| (MyBotModule.Broodwar.self().isUpgrading(UpgradeType.Charon_Boosters) ? true : false);

		
		
		if (VS && VM && TS && GR)
			return; // 4개 모두 완료이면

		currentResearched = 0;
		if (VS) {
			currentResearched++;
		}
		if (VM) {
			currentResearched++;
		}
		if (TS) {
			currentResearched++;
		}
		if (GR) {
			currentResearched++;
		}
		
//		EnemyStrategyOptions.UpgradeOrder order = StrategyIdea.currentStrategy.upgrade;
		
		List<Unit> canMachineShop = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Machine_Shop);
		
		boolean canResearch = false;
		
		int canMachineShopCnt = 0;
		
//		현재 큐에 들어있는 개발건수
		int QueueResearch = BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Ion_Thrusters)
							+ BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines)
							+ BuildManager.Instance().buildQueue.getItemCount(TechType.Tank_Siege_Mode)
							+ BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Charon_Boosters);
		
		for(Unit unit : canMachineShop) {
			if(unit.canUpgrade()) {
				canMachineShopCnt++;
				
			}
		}
		
//		비어있는 머신샵이 최소 큐에 들어있는 개발건 보다 많아야 선택
		if(canMachineShopCnt != 0 && QueueResearch < canMachineShopCnt) {
			canResearch = true;
		}
		
		
		
		
//		canResearch = true;
		
		if(canResearch == true) {
		
			List<MetaType> upgradeOrder = StrategyIdea.upgrade;
			
			
			for(MetaType e : upgradeOrder) {
				if(BuildManager.Instance().buildQueue.getItemCount(e) != 0){
					continue;
				}
				if(e.isUpgrade()) {
					if(MyBotModule.Broodwar.self().getUpgradeLevel(e.getUpgradeType()) == 1 
							|| MyBotModule.Broodwar.self().isUpgrading(e.getUpgradeType())
							|| BuildManager.Instance().buildQueue.getItemCount(e.getUpgradeType()) != 0
							) {
						continue;
					}else {
						metaType = e;
						break;
					}
				}
				if(e.isTech()) {
					if(MyBotModule.Broodwar.self().hasResearched(e.getTechType()) 
							|| MyBotModule.Broodwar.self().isResearching(e.getTechType())
							|| BuildManager.Instance().buildQueue.getItemCount(e.getTechType()) != 0
							){
						continue;
					}else {
						metaType = e;
						break;
					}
				}
				
			}
			
		}
	}
}
