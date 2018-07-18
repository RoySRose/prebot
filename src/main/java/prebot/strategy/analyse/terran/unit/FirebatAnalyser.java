package prebot.strategy.analyse.terran.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

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
				ClueManager.Instance().addClueInfo(ClueInfo.FAST_FIREBAT);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.FIREBAT_FOUND);
			}
		}
	}
}
