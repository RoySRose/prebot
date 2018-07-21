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

public class BuilderMachineShop extends DefaultBuildableItem {
	
	public static boolean machine_shop_chk = false;

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
		
		int factoryCountForMachineShop = 0;
		int machineShopCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop);
		List<Unit> factories = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
		
		for (Unit factory : factories) {
			factoryCountForMachineShop++;
			if (TilePositionUtils.addOnBuildable(factory.getTilePosition())) {
				factoryCountForMachineShop--;
			}
		}

		int vultureCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Vulture);
		int tankCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
				+ Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
		int goliathCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Goliath);
		
		for (Unit factory : factories) {
			if (factory.getAddon() != null || !factory.canBuildAddon()) {
				continue;
			}
			
			int addition = 3;
			if (Prebot.Broodwar.self().gas() > 300) {
				UnitType selected = FactoryUnitSelector.chooseunit(
						StrategyIdea.factoryRatio.vulture, StrategyIdea.factoryRatio.tank, StrategyIdea.factoryRatio.goliath, 1,
						vultureCount, tankCount, goliathCount);

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

}
