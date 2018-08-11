package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class RoboticsAnalyser extends UnitAnalyser {

	public RoboticsAnalyser() {
		super(UnitType.Protoss_Robotics_Facility);
	}

	@Override
	public void analyse() {
		fastRobo();
	}

	private void fastRobo() {
		if (ClueManager.Instance().containsClueType(ClueType.FAST_ROBO)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastRoboFrame = EnemyStrategy.PROTOSS_ROBOTICS_REAVER.buildTimeMap.frame(UnitType.Protoss_Robotics_Facility, 30);

			if (buildFrame < fastRoboFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.ROBO_FAST);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.ROBO_FOUND);
			}
		}
	}

}
