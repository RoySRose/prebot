package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class CoreAnalyser extends UnitAnalyser {

	public CoreAnalyser() {
		super(UnitType.Protoss_Cybernetics_Core);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int normalCoreFrame = EnemyStrategy.PROTOSS_1GATE_CORE.defaultTimeMap.time(UnitType.Protoss_Cybernetics_Core, 20);
			
			if (buildFrame < normalCoreFrame) { // 생더블 확정
				ClueManager.Instance().addClueInfo(ClueInfo.CORE_FAST);
			}
		}
	}

}
