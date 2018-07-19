package prebot.strategy.analyse.terran.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class MedicAnalyser extends UnitAnalyser {

	public MedicAnalyser() {
		super(UnitType.Terran_Medic);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			int medicFrame = EnemyStrategy.TERRAN_BIONIC.buildTimeMap.frame(UnitType.Terran_Academy) + UnitType.Terran_Medic.buildTime() + 20 * TimeUtils.SECOND;
			if (found.get(0).getUpdateFrame() < medicFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.FAST_MEDIC);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.MEDIC_FOUND);
			}
		}
	}
}