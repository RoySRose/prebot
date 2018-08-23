

import java.util.List;

import bwapi.UnitType;

public class FirebatAnalyser extends UnitAnalyser {

	public FirebatAnalyser() {
		super(UnitType.Terran_Firebat);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int medicFrame = EnemyStrategy.TERRAN_BIONIC.buildTimeMap.frame(UnitType.Terran_Academy) + UnitType.Terran_Firebat.buildTime() + 20 * TimeUtils.SECOND;
			if (found.get(0).getUpdateFrame() < medicFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FAST_FIREBAT);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.FIREBAT_FOUND);
			}
		}
	}
}
