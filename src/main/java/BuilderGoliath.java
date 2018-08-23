

import bwapi.Race;
import bwapi.UnitType;

public class BuilderGoliath extends DefaultBuildableItem {

    FactoryUnitSelector factoryUnitSelector;

    public BuilderGoliath(MetaType metaType, FactoryUnitSelector factoryUnitSelector){
        super(metaType);
        this.factoryUnitSelector = factoryUnitSelector;
    }
    
    public final boolean buildCondition(){
    	if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Armory) == 0) {
    		return false;
    	}
    	
    	if(factoryUnitSelector.getSelected().equals(metaType.getUnitType())) {
    		return true;
    	}

    	// 최소 0기일 때 1기 뽑는 조건
    	if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) == 0 && MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Goliath) == 0) {
    		// 드랍십 등이 발견 되었으면 최소 1기는 있어야 한다.
    		if (UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Shuttle, UnitType.Terran_Dropship, UnitType.Terran_Wraith, UnitType.Zerg_Mutalisk)) {
	    		if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) + MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 2) {
	    			return true;
	    		}
	    	}
    		
    		// 병력이 거의 꽉찼을 때 골리앗 생산
    		int goliathSupply = 370;
    		if (InfoUtils.enemyRace() == Race.Zerg) {
    			goliathSupply = 350;
    		}
    		if (MyBotModule.Broodwar.self().supplyUsed() >= goliathSupply) {
    			return true;
    		}
    	}

    	return false;

    }
}
