package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class TemplarArchAnalyser extends UnitAnalyser {

	public TemplarArchAnalyser() {
		super(UnitType.Protoss_Templar_Archives);
	}

	@Override
	public void analyse() {
		fastTemplarArch();
	}

	private void fastTemplarArch() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastTemplarArchFrame = EnemyStrategy.PROTOSS_FAST_DARK.defaultTimeMap.time(UnitType.Protoss_Templar_Archives, 30);

			if (buildFrame < fastTemplarArchFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.TEMPLAR_ARCH_FAST);
			}
		}
	}

}
