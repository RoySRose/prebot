package prebot.strategy.analyse.zerg.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue;
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
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_LURKER);
			}
			
			if (TimeUtils.before(lurkerInMyRegionFrame) && UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Lurker_Egg)) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_LURKER);
			}
		}
	}
}
