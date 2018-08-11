package prebot.build.provider.items.building;

import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

public class BuilderScienceFacility extends DefaultBuildableItem {

	public BuilderScienceFacility(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
			return false;
		}
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Science_Facility)) {
			return false;
		}
		
		if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) >= 1 || Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) >= 1)
		{
			return true;
		}
		
//		20180809. hkk.
//		조건중 현재 업그레이드 중 이라는 조건은 사이언스 퍼실리티가 없다는 전제하에 들어가지만 hasUnitOrWillBe 에서 전제되어있다.
		if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) >= 1 || Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) >= 1
			||Prebot.Broodwar.self().isUpgrading(UpgradeType.Terran_Vehicle_Weapons) || Prebot.Broodwar.self().isUpgrading(UpgradeType.Terran_Vehicle_Plating))
		{
			return true;
		}
		
		
		// 완성된 팩토리 6개 이상 or 활성화된 커맨드가 3개 이상일 경우
		return UnitUtils.activatedCommandCenterCount() >= 3
				|| Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 6;
	}

}
