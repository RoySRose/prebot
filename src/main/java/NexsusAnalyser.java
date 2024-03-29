

import java.util.List;

import bwapi.UnitType;

public class NexsusAnalyser extends UnitAnalyser {

	public NexsusAnalyser() {
		super(UnitType.Protoss_Nexus);
	}

	@Override
	public void analyse() {
		fastNexsus();
	}

	private void fastNexsus() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_NEXSUS)) {
			return;
		}
		
		int doubleFrame = EnemyStrategy.PROTOSS_DOUBLE.buildTimeMap.frame(UnitType.Protoss_Nexus, 20);
		int forgeDoubleFrame = EnemyStrategy.PROTOSS_FORGE_DOUBLE.buildTimeMap.frame(UnitType.Protoss_Nexus, 20);
		
		List<UnitInfo> found = found(CommonCode.RegionType.ENEMY_FIRST_EXPANSION);
		if (!found.isEmpty()) {
			int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
			
			if (buildFrame < doubleFrame) { // 생더블 확정
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.NEXSUS_FASTEST_DOUBLE);
				
			} else if (buildFrame < forgeDoubleFrame) { // 포지더블, 게이트더블
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.NEXSUS_FAST_DOUBLE);
			}
		} else {
			int expansionLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(StrategyAnalyseManager.LastCheckLocation.FIRST_EXPANSION);
			if (expansionLastCheckFrame > forgeDoubleFrame) { // 더블 타이밍 지남
				ClueManager.Instance().addClueInfo(Clue.ClueInfo.NEXSUS_NOT_DOUBLE);
			}
		}
	}

}
