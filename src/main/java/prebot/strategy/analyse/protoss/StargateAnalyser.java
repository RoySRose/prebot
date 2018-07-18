package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class StargateAnalyser extends UnitAnalyser {

	public StargateAnalyser() {
		super(UnitType.Protoss_Stargate);
	}

	@Override
	public void analyse() {
		fastStargate();
	}

	private void fastStargate() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int fastStargateFrame = EnemyStrategy.PROTOSS_STARGATE.buildTimeMap.frame(UnitType.Protoss_Stargate, 30);
			int doubleStargateFrame = EnemyStrategy.PROTOSS_DOUBLE_CARRIER.buildTimeMap.frame(UnitType.Protoss_Stargate, 30);

			if (buildFrame < fastStargateFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.STARGATE_ONEGATE_FAST);
			} else if (buildFrame < doubleStargateFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.STARGATE_DOUBLE_FAST);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.STARGATE_FOUND);
			}
		}
	}

}
