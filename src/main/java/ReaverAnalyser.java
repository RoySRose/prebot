

import java.util.List;

import bwapi.UnitType;

public class ReaverAnalyser extends UnitAnalyser {

	public ReaverAnalyser() {
		super(UnitType.Protoss_Reaver);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int reaverFrame = EnemyStrategy.PROTOSS_ROBOTICS_REAVER.buildTimeMap.frame(UnitType.Protoss_Robotics_Support_Bay, 40)
					+ UnitType.Protoss_Robotics_Support_Bay.buildTime() + UnitType.Protoss_Reaver.buildTime();
			int reaverInMyRegionFrame = reaverFrame + baseToBaseFrame(UnitType.Protoss_Shuttle);
			if (minimumUpdateFrame < reaverInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_REAVER);
			}
		}
	}
}
