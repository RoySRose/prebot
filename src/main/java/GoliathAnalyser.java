

import java.util.List;

import bwapi.UnitType;

public class GoliathAnalyser extends UnitAnalyser {

	public GoliathAnalyser() {
		super(UnitType.Terran_Goliath);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			if (found.size() >= 3) {
				int lastFoundFrame = lastUnitFoundFrame(found, 3);
				int goliathInMyRegionFrame = goliathInMyRegionFrame(3);
				if (lastFoundFrame < goliathInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_THREE_GOLIATH);
				}
			}
			if (found.size() >= 2) {
				int lastFoundFrame = lastUnitFoundFrame(found, 2);
				int goliathInMyRegionFrame = goliathInMyRegionFrame(2);
				if (lastFoundFrame < goliathInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_TWO_GOLIATH);
				}
			} else {
				int lastFoundFrame = lastUnitFoundFrame(found, 1);
				int goliathInMyRegionFrame = goliathInMyRegionFrame(1);
				if (lastFoundFrame < goliathInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_ONE_GOLIATH);
				}
			}
			
			if (!ClueManager.Instance().containsClueType(Clue.ClueType.FAST_GOLIATH)) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.GOLIATH_FOUND);
			}
		}
	}

	private int goliathInMyRegionFrame(int enemyCount) {
		int factoryCompleteFrame = EnemyStrategy.TERRAN_2FAC.buildTimeMap.frame(UnitType.Terran_Factory, 10) + UnitType.Terran_Factory.buildTime();
		int nGoliathCompleteFrame = factoryCompleteFrame + UnitType.Terran_Goliath.buildTime() * (enemyCount / 2 + 1);
		return nGoliathCompleteFrame + baseToBaseFrame(UnitType.Terran_Goliath);
	}
}
