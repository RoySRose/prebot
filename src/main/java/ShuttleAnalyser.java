

import java.util.List;

import bwapi.UnitType;

public class ShuttleAnalyser extends UnitAnalyser {

	public ShuttleAnalyser() {
		super(UnitType.Protoss_Shuttle);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_SHUTTLE)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int reaverFrame = EnemyStrategy.PROTOSS_ROBOTICS_REAVER.buildTimeMap.frame(UnitType.Protoss_Robotics_Facility, 30)
					+ UnitType.Protoss_Robotics_Facility.buildTime() + UnitType.Protoss_Shuttle.buildTime();
			int reaverInMyRegionFrame = reaverFrame + baseToBaseFrame(UnitType.Protoss_Shuttle);
			if (minimumUpdateFrame < reaverInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_SHUTTLE);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.SHUTTLE_FOUND);
			}
		}
	}
}
