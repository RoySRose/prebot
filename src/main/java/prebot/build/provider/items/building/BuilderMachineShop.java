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
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class BuilderMachineShop extends DefaultBuildableItem {
	
	public static boolean machine_shop_chk = false;

    public BuilderMachineShop(MetaType metaType){
        super(metaType);
    }
    
    /*private int vultureratio = 0;
    private int tankratio = 0;
    private int goliathratio = 0;
    private int wgt = 1;*/

    public final boolean buildCondition(){
    	
    	
    	
		int FaccntForMachineShop = 0;
		int MachineShopcnt = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop);
		
		List<Unit> factory = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
		
		for (Unit unit : factory) {
//			if (unit == null)
//				continue;
//			if (unit.getType().isResourceDepot() && unit.isCompleted()) {
//				CCcnt++;
//			}

//			Faccnt++;
			FaccntForMachineShop++;
			if (BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 4, unit.getTilePosition().getY() + 1) == false
					|| BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 5, unit.getTilePosition().getY() + 1) == false
					|| BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 4, unit.getTilePosition().getY() + 2) == false
					|| BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX() + 5, unit.getTilePosition().getY() + 2) == false) {
				FaccntForMachineShop--;
			}
		}
		
		
    	
//    if (MachineShopcnt < FaccntForMachineShop) {
		for (Unit unit : factory) {
	//			if (unit == null)
	//				continue;
			if (unit.getAddon() == null && unit.canBuildAddon()) {
				int addition = 3;
				if (Prebot.Broodwar.self().gas() > 300) {
	
					int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
					int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
					int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
//					setCombatUnitRatio();
//					UnitType selected = FactoryUnitSelector.chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
					UnitType selected = FactoryUnitSelector.chooseunit(StrategyIdea.currentStrategy.ratio.vulture, StrategyIdea.currentStrategy.ratio.tank, StrategyIdea.currentStrategy.ratio.goliath, 1, tot_vulture, tot_tank, tot_goliath);
	
					if (selected == UnitType.Terran_Siege_Tank_Tank_Mode) {
						addition = 0;
					}
				}
	
				if (MachineShopcnt + addition < FaccntForMachineShop) {
					if (Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50) {
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null) == 0
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) == 0) {
	//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop, true);
	//							break;
//							BuilderMachineShop.machine_shop_chk = true;
							return true;
						}
					}
				}
			}
		}
        
        
        
        return false;
    }
    
    private int GetCurrentTotBlocked(UnitType checkunit) {
		int cnt = Prebot.Broodwar.self().allUnitCount(checkunit);

		return cnt;
	}
   

}
