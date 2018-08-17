package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions.AddOnOption;

public class BuilderMachineShop extends DefaultBuildableItem {
	
//	public static boolean machine_shop_chk = false;

    public BuilderMachineShop(MetaType metaType){
        super(metaType);
    }
    
    public final boolean buildCondition(){
		if (Prebot.Broodwar.self().minerals() < 50 || Prebot.Broodwar.self().gas() < 50) {
			return false;
		}
		
		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null);
		if (buildQueueCount > 0) {
			return false;
		}
		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null);
		if (constructionCount > 0) {
			return false;
		}
		
		// 커맨드센터 개수에 따른 머신샵 max 설정
		int maxCountByCommandCenter = 0;
		int commandCenterCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		if (commandCenterCount == 1) {
			maxCountByCommandCenter = 1;
		} else if (commandCenterCount == 2) {
			maxCountByCommandCenter = 2;
		} else if (commandCenterCount == 3) {
			maxCountByCommandCenter = 3;
		}
		
		if (Prebot.Broodwar.self().gas() > 300) {
			maxCountByCommandCenter++;
		}
		
		int machineShopCountIncludeQueue = UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Machine_Shop);
		if (machineShopCountIncludeQueue >= maxCountByCommandCenter) {
			return false;
		}
		
		List<Unit> factories = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
		
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop) == 0) {
			if(StrategyIdea.addOnOption == AddOnOption.VULTURE_FIRST) {
				if (UnitUtils.myUnitDiscovered(UnitType.Terran_Vulture)) {
					for (Unit factory : factories) {
						if (factory.getAddon() != null || !factory.canBuildAddon()) {
							continue;
						}
						return true;
					}
				}
			} else {
				return true;
			}
		}
			
			
		
		int factoryCountForMachineShop = 0;
		int machineShopCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop);
		
		
		for (Unit factory : factories) {
			factoryCountForMachineShop++;
			if (!TilePositionUtils.addOnBuildable(factory.getTilePosition())) {
				factoryCountForMachineShop--;
			}
		}
		
		

//		int vultureCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Vulture);
//		int tankCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
//				+ Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
//		int goliathCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Goliath);
		
		int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
		int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
		int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
		
		int vultureratio = StrategyIdea.factoryRatio.vulture;
		int tankratio = StrategyIdea.factoryRatio.tank;
		int goliathratio = StrategyIdea.factoryRatio.goliath;
		int wgt = StrategyIdea.factoryRatio.weight;
		
		for (Unit factory : factories) {
			if (factory.getAddon() != null || !factory.canBuildAddon()) {
				continue;
			}
			
			int addition = 3;
			if (Prebot.Broodwar.self().gas() > 300) {
				UnitType selected = FactoryUnitSelector.chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
				if (selected == UnitType.Terran_Siege_Tank_Tank_Mode) {
					addition = 0;
				}
			}
			if (machineShopCount + addition < factoryCountForMachineShop) {
				return true;
			}
		}

        
        return false;
    }
    
    public int GetCurrentTot(UnitType checkunit) {
		return BuildManager.Instance().buildQueue.getItemCount(checkunit) + Prebot.Broodwar.self().allUnitCount(checkunit);
	}
    
    @Override
    public boolean checkInitialBuild(){
		return true;
    }

}
