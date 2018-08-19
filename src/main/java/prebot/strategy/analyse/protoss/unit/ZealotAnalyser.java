package prebot.strategy.analyse.protoss.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class ZealotAnalyser extends UnitAnalyser {

	public ZealotAnalyser() {
		super(UnitType.Protoss_Zealot);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			if (found.size() >= 3) {
				int lastUnitFoundFrame = lastUnitFoundFrame(found, 3);
				
				int threeZealotFrame = EnemyStrategy.PROTOSS_2GATE.buildTimeMap.frameOfIndex(UnitType.Protoss_Gateway, 1, 30) + UnitType.Protoss_Zealot.buildTime();
				int threeZealotInMyRegionFrame = threeZealotFrame + baseToBaseFrame(UnitType.Protoss_Zealot);
				if (lastUnitFoundFrame < threeZealotInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_THREE_ZEALOT);
				}
			} else if (found.size() >= 1) {
				int lastUnitFoundFrame = found.get(0).getUpdateFrame();
				
				int oneZealotFrame = EnemyStrategy.PROTOSS_2GATE.buildTimeMap.frameOfIndex(UnitType.Protoss_Gateway, 0, 5) + UnitType.Protoss_Zealot.buildTime();
				int oneZealotInMyRegionFrame = oneZealotFrame + baseToBaseFrame(UnitType.Protoss_Zealot);
				if (lastUnitFoundFrame < oneZealotInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_ONE_ZEALOT);
				}
			}
		}
	}
}
