package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class BuilderControlTower extends DefaultBuildableItem {

    public BuilderControlTower(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
    		return false;
    	}

		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Control_Tower)) {
			return false;
		}
    	
		boolean addControlTower = false;
			
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Science_Facility) >= 1) {
			addControlTower = true;
		} else if (StrategyIdea.valkyrieCount > 0) {
			addControlTower = true;
		}
		
		// 레이스 4기 생산후 건설 or 사이언스 퍼실리티 있으면 건설
		// 레이쓰가 성공적일 경우에만 개발할 필요가 있음.
//		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith) >= 4 && StrategyIdea.wraithCount >= 6 && PlayerUtils.enoughResource(200, 250)) {
//			addControlTower = true;
//		}
		
		if (addControlTower) {
			List<Unit> starport = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Starport);
			for (Unit unit : starport) {
				if (unit.canBuildAddon()) {
					return true;
				}
			}
    	}
		
        return false;
    }

}
