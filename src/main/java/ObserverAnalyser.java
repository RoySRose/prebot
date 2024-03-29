

import java.util.List;

import bwapi.UnitType;

public class ObserverAnalyser extends UnitAnalyser {

	public ObserverAnalyser() {
		super(UnitType.Protoss_Observer);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int observerFrame = EnemyStrategy.PROTOSS_ROBOTICS_OB_DRAGOON.buildTimeMap.frame(UnitType.Protoss_Observatory, 10)
					+ UnitType.Protoss_Observatory.buildTime() + UnitType.Protoss_Observer.buildTime();
			int observerInMyRegionFrame = observerFrame + baseToBaseFrame(UnitType.Protoss_Observer);
			if (minimumUpdateFrame < observerInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.OBSERVERTORY_FAST);
			}
		}
	}
}
