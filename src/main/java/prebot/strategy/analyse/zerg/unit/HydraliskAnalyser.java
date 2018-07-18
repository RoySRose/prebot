package prebot.strategy.analyse.zerg.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.EnemyBuildTimer;

public class HydraliskAnalyser extends UnitAnalyser {

	public HydraliskAnalyser() {
		super(UnitType.Zerg_Hydralisk);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}

		if (!found.isEmpty()) {
			int hydradenExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Hydralisk_Den);
			if (hydradenExpect == CommonCode.UNKNOWN) {
				return;
			}
			
			if (found.size() >= 3) {
				int lastUnitFoundFrame = lastUnitFoundFrame(found, 3);
				int threeHydraliskInMyRegionFrame = hydradenExpect + UnitType.Zerg_Hydralisk_Den.buildTime()
					+ UnitType.Zerg_Hydralisk.buildTime() + baseToBaseFrame(UnitType.Zerg_Hydralisk) + 30 * TimeUtils.SECOND;
				if (lastUnitFoundFrame < threeHydraliskInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.FAST_MANY_HYDRA);
				}
			} else {
				int lastUnitFoundFrame = lastUnitFoundFrame(found, found.size());
				int hydraliskInMyRegionFrame = hydradenExpect + UnitType.Zerg_Hydralisk_Den.buildTime()
					+ UnitType.Zerg_Hydralisk.buildTime() + baseToBaseFrame(UnitType.Zerg_Hydralisk) + 15 * TimeUtils.SECOND;
				if (lastUnitFoundFrame < hydraliskInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.FAST_HYDRA);
				}
			}
		}
	}
}
