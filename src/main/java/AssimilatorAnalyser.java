

import java.util.List;

import bwapi.UnitType;

public class AssimilatorAnalyser extends UnitAnalyser {

	public AssimilatorAnalyser() {
		super(UnitType.Protoss_Assimilator);
	}

	@Override
	public void analyse() {
		fastAssimilator();
	}

	private void fastAssimilator() {
		List<UnitInfo> found = found(CommonCode.RegionType.ENEMY_BASE);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			int oneGateCoreGasFrame = EnemyStrategy.PROTOSS_1GATE_CORE.buildTimeMap.frame(UnitType.Protoss_Assimilator, 25);
			if (buildFrame < oneGateCoreGasFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.ASSIMILATOR_FAST);
			} else {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.ASSIMILATOR_LATE);
			}
		} else {
			int gasLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(StrategyAnalyseManager.LastCheckLocation.GAS);
			int oneGateCoreGasFrame = EnemyStrategy.PROTOSS_1GATE_CORE.buildTimeMap.frame(UnitType.Protoss_Assimilator, 25);
			if (gasLastCheckFrame > oneGateCoreGasFrame) {
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.NO_ASSIMILATOR);
			}
		}
	}

}
