package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.RaceActionManager;
import prebot.strategy.manage.RaceActionManager.LastCheckLocation;

public class AssimilatorAnalyser extends UnitAnalyser {

	public AssimilatorAnalyser() {
		super(UnitType.Protoss_Assimilator);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found(RegionType.ENEMY_BASE);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int oneGateCoreGasFrame = EnemyStrategy.PROTOSS_1GATE_CORE.defaultTimeMap.time(UnitType.Protoss_Assimilator, 25);
			if (buildFrame < oneGateCoreGasFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.ASSIMILATOR_FAST);
			} else {
				ClueManager.Instance().addClueInfo(ClueInfo.ASSIMILATOR_LATE);
			}
		} else {
			int gasLastCheckFrame = RaceActionManager.Instance().lastCheckFrame(LastCheckLocation.GAS);
			int oneGateCoreGasFrame = EnemyStrategy.PROTOSS_1GATE_CORE.defaultTimeMap.time(UnitType.Protoss_Assimilator, 25);
			if (gasLastCheckFrame > oneGateCoreGasFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.NO_ASSIMILATOR);
			}
		}
	}

}
