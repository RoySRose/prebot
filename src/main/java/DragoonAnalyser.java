

import java.util.List;

import bwapi.UnitType;

public class DragoonAnalyser extends UnitAnalyser {

	public DragoonAnalyser() {
		super(UnitType.Protoss_Dragoon);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int minimumUpdateFrame = found.get(0).getUpdateFrame();
			int dragoonFrame = EnemyStrategy.PROTOSS_1GATE_CORE.buildTimeMap.frame(UnitType.Protoss_Cybernetics_Core, 10) + UnitType.Protoss_Dragoon.buildTime();
			int dragoonFrameInMyRegionFrame = dragoonFrame + baseToBaseFrame(UnitType.Protoss_Dragoon);
			if (minimumUpdateFrame < dragoonFrameInMyRegionFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_DRAGOON);
			}
		}
	}
}
