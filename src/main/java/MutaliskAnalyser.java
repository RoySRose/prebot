

import java.util.List;

import bwapi.UnitType;

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
