package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class RoboticsSupportAnalyser extends UnitAnalyser {

	public RoboticsSupportAnalyser() {
		super(UnitType.Protoss_Robotics_Support_Bay);
	}

	@Override
	public void analyse() {
		fastRoboSupport();
	}

	private void fastRoboSupport() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastRoboSupportFrame = EnemyStrategy.PROTOSS_ROBOTICS_REAVER.defaultTimeMap.frame(UnitType.Protoss_Robotics_Support_Bay, 30);

			if (buildFrame < fastRoboSupportFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.ROBO_SUPPORT_FAST);
			}
		}
	}

}
