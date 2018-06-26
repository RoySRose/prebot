package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class CannonAnalyser extends UnitAnalyser {

	public CannonAnalyser() {
		super(UnitType.Protoss_Photon_Cannon);
	}

	@Override
	public void analyse() {
		fastCannon();
	}

	private void fastCannon() {
		int forgeDoulbeCannonFrame = EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.time(UnitType.Protoss_Photon_Cannon, 15);
		List<UnitInfo> found = found(RegionType.ENEMY_FIRST_EXPANSION);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			if (buildFrame < forgeDoulbeCannonFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.CANNON_FAST_IN_EXPANSION);
			}
		} else {
			found = found(RegionType.ENEMY_BASE);
			if (!found.isEmpty()) {
				int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				if (buildFrame < forgeDoulbeCannonFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.CANNON_FAST_IN_BASE);
				}
			} else {
				found = found();
				if (!found.isEmpty()) {
					int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
					if (buildFrame < forgeDoulbeCannonFrame) {
						ClueManager.Instance().addClueInfo(ClueInfo.CANNON_FAST_SOMEWHERE);
					}
				}
			}
		}
	}

}
