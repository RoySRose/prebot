package prebot.strategy.analyse.protoss.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class ShuttleAnalyser extends UnitAnalyser {

	public ShuttleAnalyser() {
		super(UnitType.Protoss_Shuttle);
	}

	@Override
	public void analyse() {
		if (ClueManager.Instance().containsClueType(ClueType.FAST_SHUTTLE)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int reaverFrame = EnemyStrategy.PROTOSS_ROBOTICS_REAVER.buildTimeMap.frame(UnitType.Protoss_Robotics_Facility, 30)
					+ UnitType.Protoss_Robotics_Facility.buildTime() + UnitType.Protoss_Shuttle.buildTime();
			int reaverInMyRegionFrame = reaverFrame + baseToBaseFrame(UnitType.Protoss_Shuttle);
			if (minimumUpdateFrame < reaverInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.FAST_SHUTTLE);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.SHUTTLE_FOUND);
			}
		}
	}
}
