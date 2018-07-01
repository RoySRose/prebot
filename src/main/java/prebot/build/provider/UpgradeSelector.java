package prebot.build.provider;


import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.constant.EnemyStrategyOptions.UpgradeOrder;
import prebot.strategy.manage.AttackExpansionManager;
import prebot.strategy.constant.EnemyStrategy.*;

public class UpgradeSelector implements Selector<MetaType>{

    MetaType metaType;
    //BuildCondition buildCondition;

    public final MetaType getSelected(){
        return metaType;
    }

    public final void select(){
    	// 업그레이드
    	if(BuildQueueProvider.Instance().respondSet) {
    		//unitType = UnitType.None;
    	}else {
			if (Prebot.Broodwar.getFrameCount() % 53 == 0) {
				executeUpgrade();
			}
    	}

        //buildCondition = new BuildCondition();
        //metaType = new MetaType(UpgradeType.Terran_Infantry_Armor);
        
        

    }
    
    
    
    //UpgradeOrder.VM_TS_VS;
    
    
    private void executeUpgrade() {

		Unit armory = null;
		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Armory) {
				armory = unit;
			}
		}
		if (armory == null) {
			return;
		}

		boolean standard = false;
		int unitPoint = AttackExpansionManager.Instance().UnitPoint;

		int myFactoryUnitSupplyCount = UnitUtils.myFactoryUnitSupplyCount();
		if (myFactoryUnitSupplyCount > 42 || (unitPoint > 10 && myFactoryUnitSupplyCount > 30)) {
			standard = true;
		}
		if (unitPoint < -10 && myFactoryUnitSupplyCount < 60) {
			standard = false;
		}
		// Fac Unit 18 마리 이상 되면 1단계 업그레이드 시도
		if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 0 && standard && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)
				&& Prebot.Broodwar.self().minerals() > 100 && Prebot.Broodwar.self().gas() > 100) {
			if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
				//BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
				metaType =  new MetaType(UpgradeType.Terran_Vehicle_Weapons);
			}
		} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 0 && standard && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)
				&& Prebot.Broodwar.self().minerals() > 100 && Prebot.Broodwar.self().gas() > 100) {
			if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
				//BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
				metaType =  new MetaType(UpgradeType.Terran_Vehicle_Plating);
			}
		}
		// Fac Unit 30 마리 이상, 일정 이상의 자원 2단계
		else if (Prebot.Broodwar.self().minerals() > 250 && Prebot.Broodwar.self().gas() > 225) {
			if ((Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 2 && myFactoryUnitSupplyCount > 140)
					|| (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) >= 3 && myFactoryUnitSupplyCount > 80)) {

				if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 1 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)) {
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
						metaType =  new MetaType(UpgradeType.Terran_Vehicle_Weapons);
					}
				} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 1 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)) {
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
						metaType =  new MetaType(UpgradeType.Terran_Vehicle_Plating);
					}
				} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 2 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)) {// 3단계
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
						metaType =  new MetaType(UpgradeType.Terran_Vehicle_Weapons);
					}
				} else if (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 2 && armory.canUpgrade(UpgradeType.Terran_Vehicle_Plating)) {
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
						metaType =  new MetaType(UpgradeType.Terran_Vehicle_Plating);
					}
				}
			}
		}
	}

}
