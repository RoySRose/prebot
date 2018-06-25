package prebot.strategy.analyse.protoss;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.StrategyAnalyseManager;
import prebot.strategy.manage.StrategyAnalyseManager.LastCheckLocation;

public class GateAnalyser extends UnitAnalyser {

	public GateAnalyser() {
		super(UnitType.Protoss_Gateway);
	}

	@Override
	public void analyse() {
		List<UnitInfo> found = found();
		if (!found.isEmpty()) {
			if (found.size() >= 2) { // 게이트 웨이 2개 이상
				int firstBuildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				int secondBuildFrame = buildStartFrameDefaultJustBefore(found.get(1));
				int twoGateSecondGateFrame = EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 1, 20);
				
				if (firstBuildFrame < twoGateSecondGateFrame && secondBuildFrame < twoGateSecondGateFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.GATE_FAST_TWO);
				} else {
					ClueManager.Instance().addClueInfo(ClueInfo.GATE_TWO);
				}
			
			} else if (found.size() == 1) { // 게이트 웨이 1개
				int firstBuildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				int twoGateFirstGateFrame = EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.time(UnitType.Protoss_Gateway, 15);
				if (firstBuildFrame < twoGateFirstGateFrame) {
					ClueManager.Instance().addClueInfo(ClueInfo.GATE_FAST_ONE);
				} else {
					ClueManager.Instance().addClueInfo(ClueInfo.GATE_ONE);
				}
			}
		} else {
			int twoGateFirstGateFrame = EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.time(UnitType.Protoss_Gateway, 20);
			int baseLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.BASE);
			if (baseLastCheckFrame > twoGateFirstGateFrame) {
				ClueManager.Instance().addClueInfo(ClueInfo.GATE_NOT_FOUND);
			}
		}
	}

}
