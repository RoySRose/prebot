package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class ForgeAnalyser extends UnitAnalyser {

	public ForgeAnalyser() {
		super(UnitType.Protoss_Forge);
	}

	@Override
	public void analyse() {
		fastForge();
	}

	private void fastForge() {
		List<UnitInfo> found = found(RegionType.ENEMY_FIRST_EXPANSION);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int forgeDoubleForgeFrame = EnemyStrategy.PROTOSS_FORGE_DOUBLE.buildTimeMap.frame(UnitType.Protoss_Forge, 15);
			if (buildFrame < forgeDoubleForgeFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.FORGE_FAST_IN_EXPANSION);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.FORGE_FOUND);
			}
			
		} else {
			found = found();
			if (!found.isEmpty()) {
				int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				int forgeDoubleForgeFrame = EnemyStrategy.PROTOSS_FORGE_DOUBLE.buildTimeMap.frame(UnitType.Protoss_Forge, 15);
				if (buildFrame < forgeDoubleForgeFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.FORGE_FAST_IN_BASE);
				} else {
					ClueManager.Instance().addClueInfo(ClueInfo.FORGE_FOUND);
				}
			}
		}
	}

}
