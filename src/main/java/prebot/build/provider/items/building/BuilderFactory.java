package prebot.build.provider.items.building;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.strategy.InformationManager;

public class BuilderFactory extends DefaultBuildableItem {

    public BuilderFactory(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("Factory build condition check");
        
        
        
        
        int CCcnt = 0;
		int maxFaccnt = 0;
		int Faccnt = 0;
		int FaccntForMachineShop = 0;
		int MachineShopcnt = 0;
		boolean facFullOperating = true;

		for (Unit unit : Prebot.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType().isResourceDepot() && unit.isCompleted()){
				CCcnt++;
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
				Faccnt ++;
				FaccntForMachineShop++;
				if (BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+1) == false
						||BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+1) == false
						||BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+2) == false
						||BuildManager.Instance().isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+2) == false)
				{
					FaccntForMachineShop--;
				}
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
				if(unit.isTraining() == false){
					facFullOperating = false;
				}
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() == false){
				facFullOperating = false;
			}
			if (unit.getType() == UnitType.Terran_Machine_Shop){
				MachineShopcnt ++;
			}
 		}
		
		if(CCcnt <= 1){
			/*if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				maxFaccnt = 4;
			}else{*/
				maxFaccnt = 3;
			//}
		}else if(CCcnt == 2){ 
			/*if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				maxFaccnt = 6;
			}else{*/
				maxFaccnt = 6;
			//}
		}else if(CCcnt >= 3){
			maxFaccnt = 9;
		}
		
		Faccnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);
				
		
		int additonalmin = 0;
		int additonalgas = 0;
		if(facFullOperating == false){
			additonalmin = (Faccnt-1)*40;
			additonalgas = (Faccnt-1)*20;
		}
		
		//하단 내용은 BuilderMachineShop 에서 사용
		if(MachineShopcnt < FaccntForMachineShop){
			for (Unit unit : Prebot.Broodwar.self().getUnits())
			{
				if (unit == null) continue;
				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()&& unit.isCompleted() &&unit.getAddon() ==null &&unit.canBuildAddon() ){
					int addition = 3;
					/*if( Prebot.Broodwar.self().gas() > 300){
						
						int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
						int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
						int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
						UnitType selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
						
						if(selected == UnitType.Terran_Siege_Tank_Tank_Mode){
							addition =0;
						}
					}*/
					
					if(MachineShopcnt + addition < FaccntForMachineShop){
						if(Prebot.Broodwar.self().minerals() > 50 && Prebot.Broodwar.self().gas() > 50){
							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null) == 0
									&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) == 0) {
								//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop, true);
								BuilderMachineShop.machine_shop_chk = true;
								//return true;
								//break;
							}
						}
					}
				}
			}
		}
		
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) == 0 ) {
			if(Faccnt == 0){
				//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				return true;
			}else if(Faccnt < maxFaccnt){
				if(Prebot.Broodwar.self().minerals() > 200 + additonalmin && Prebot.Broodwar.self().gas() > 100 + additonalgas){
					if(Faccnt == 2){
						if(getFacUnits()>24 && facFullOperating){
							//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							return true;
						}
					}else if(Faccnt <= 6){
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return true;
					}else{
						//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.LastBuilingPoint, false);
						return true;
					}
				}
			}
		}
		
		
        
        
        return false;
    }
    
    
    public int getFacUnits(){
		
		int tot=0;
		for (Unit unit : Prebot.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Vulture && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Goliath && unit.isCompleted() ){
				tot++;
			}
 		}
		return tot*4; //인구수 기준이므로
	}
    
    private int GetCurrentTotBlocked(UnitType checkunit) {
		int cnt = Prebot.Broodwar.self().allUnitCount(checkunit);

		return cnt;
	}


}
