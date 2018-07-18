package prebot.strategy.analyse.terran.unit;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class VultureAnalyser extends UnitAnalyser {

	public VultureAnalyser() {
		super(UnitType.Terran_Vulture);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			if (found.size() >= 6) {
				int lastFoundFrame = lastUnitFoundFrame(found, 6);
				int vultureInMyRegionFrame = vultureInMyRegionFrame(6);
				if (lastFoundFrame < vultureInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.FAST_SIX_VULTURE);
				}
			}
			if (found.size() >= 4) {
				int lastFoundFrame = lastUnitFoundFrame(found, 4);
				int vultureInMyRegionFrame = vultureInMyRegionFrame(4);
				if (lastFoundFrame < vultureInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.FAST_FOUR_VULTURE);
				}
			} else if (found.size() >= 2) {
				int lastFoundFrame = lastUnitFoundFrame(found, 2);
				int vultureInMyRegionFrame = vultureInMyRegionFrame(2);
				if (lastFoundFrame < vultureInMyRegionFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.FAST_TWO_VULTURE);
				}
			}
		}
	}

	private int vultureInMyRegionFrame(int enemyCount) {
		int factoryFrame = EnemyStrategy.TERRAN_2FAC.buildTimeMap.frame(UnitType.Terran_Factory, 10) + UnitType.Terran_Factory.buildTime();
		int nVultureCompleteFrame = factoryFrame + UnitType.Terran_Vulture.buildTime() * (enemyCount / 2 + 1);
		return nVultureCompleteFrame + baseToBaseFrame(UnitType.Terran_Vulture);
	}
}
