package prebot.strategy.analyse.zerg.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.EnemyBuildTimer;

public class MutaliskAnalyser extends UnitAnalyser {

	public MutaliskAnalyser() {
		super(UnitType.Zerg_Mutalisk);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}

		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int spireBuildExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Spire);
			if (spireBuildExpect == CommonCode.UNKNOWN) {
				return;
			}
			
			int mutalInMyRegionFrame = spireBuildExpect + UnitType.Zerg_Spire.buildTime()
					+ UnitType.Zerg_Mutalisk.buildTime() + baseToBaseFrame(UnitType.Zerg_Mutalisk) + 15 * TimeUtils.SECOND;
			if (minimumUpdateFrame < mutalInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_MUTAL);
			}
		}
	}
}
