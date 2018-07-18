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

public class LurkerAnalyser extends UnitAnalyser {

	public LurkerAnalyser() {
		super(UnitType.Zerg_Lurker);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}

		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int lairBuildExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
			if (lairBuildExpect == CommonCode.UNKNOWN) {
				return;
			}
			
			int lurkerInMyRegionFrame = lairBuildExpect + UnitType.Zerg_Lair.buildTime()
				+ UnitType.Zerg_Lurker.buildTime() + baseToBaseFrame(UnitType.Zerg_Lurker) + 15 * TimeUtils.SECOND;
			if (minimumUpdateFrame < lurkerInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.FAST_LURKER);
			}
		}
	}
}
