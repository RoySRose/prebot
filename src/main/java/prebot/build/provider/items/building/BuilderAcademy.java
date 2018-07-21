package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.StrategyIdea;

public class BuilderAcademy extends DefaultBuildableItem {

	public BuilderAcademy(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Academy)) {
			return false;
		}

		if (TimeUtils.after(StrategyIdea.academyFrame)) {
			setBlocking(true); setHighPriority(true);
			return true;
		}

		if (TimeUtils.afterTime(9, 0)) {
			return true;
		}

		// 활성화된 커맨드가 2개 이상일 경우
		int activatedCommandCount = 0;
		List<Unit> commandCenters = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);

		for (Unit commandCenter : commandCenters) {
			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(commandCenter) > 8) {
				activatedCommandCount++;
			}
			if (activatedCommandCount >= 2) {
				return true;
			}
		}

		return false;
	}

}
