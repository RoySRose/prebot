package prebot.build.provider.items.building;

import bwapi.UnitType;
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
		
		// 완성된 팩토리 6개 이상 or 활성화된 커맨드가 3개 이상일 경우
		return UnitUtils.activatedCommandCenterCount() >= 3
				|| Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 6;
	}

}
