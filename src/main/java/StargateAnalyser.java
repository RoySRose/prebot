

import java.util.List;

import bwapi.UnitType;

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
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARGATE_ONEGATE_FAST);
			} else if (buildFrame < doubleStargateFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARGATE_DOUBLE_FAST);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.STARGATE_FOUND);
			}
		}
	}

}
