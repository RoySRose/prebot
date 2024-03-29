

import java.util.List;

import bwapi.UnitType;

public class RoboticsAnalyser extends UnitAnalyser {

	public RoboticsAnalyser() {
		super(UnitType.Protoss_Robotics_Facility);
	}

	@Override
	public void analyse() {
		fastRobo();
	}

	private void fastRobo() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_ROBO)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastRoboFrame = EnemyStrategy.PROTOSS_ROBOTICS_REAVER.buildTimeMap.frame(UnitType.Protoss_Robotics_Facility, 30);

			if (buildFrame < fastRoboFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.ROBO_FAST);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.ROBO_FOUND);
			}
		}
	}

}
