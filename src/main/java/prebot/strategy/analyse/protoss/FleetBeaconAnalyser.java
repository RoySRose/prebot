package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class FleetBeaconAnalyser extends UnitAnalyser {

	public FleetBeaconAnalyser() {
		super(UnitType.Protoss_Fleet_Beacon);
	}

	@Override
	public void analyse() {
		fastFleetBeacon();
	}

	private void fastFleetBeacon() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int buildStartFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int forgeDoubleForgeFrame = EnemyStrategy.PROTOSS_DOUBLE_CARRIER.buildTimeMap.frame(UnitType.Protoss_Fleet_Beacon, 90);
			if (buildStartFrame < forgeDoubleForgeFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_FLEET_BEACON);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FLEET_BEACON_FOUND);
			}
		}
	}

}
