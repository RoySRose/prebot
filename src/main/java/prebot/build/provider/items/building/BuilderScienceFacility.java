package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;

public class BuilderScienceFacility extends DefaultBuildableItem {

	public BuilderScienceFacility(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
			return false;
		}
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Science_Facility) > 0) {
			return false;
		}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 6) {
			return true;
		}
		
		// 활성화된 커맨드가 3개 이상일 경우
		int activatedCommandCount = 0;
		List<Unit> commandCenters = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);

		for (Unit commandCenter : commandCenters) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(commandCenter) > 8) {
				activatedCommandCount++;
			}
			if (activatedCommandCount >= 3) {
				return true;
			}
		}

		return false;
	}

}
