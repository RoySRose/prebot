

import java.util.List;

import bwapi.UnitType;

public class ArmoryAnalyser extends UnitAnalyser {

	public ArmoryAnalyser() {
		super(UnitType.Terran_Armory);
	}

	@Override
	public void analyse() {
		fastArmory();
	}

	private void fastArmory() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_ARMORY)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}
		
		int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
		int armoryFrame = EnemyStrategy.TERRAN_1FAC_DOUBLE_ARMORY.buildTimeMap.frame(UnitType.Terran_Armory, 10);
		
		if (buildFrame < armoryFrame) {
			ClueManager.Instance().addClueInfo(Clue.ClueInfo.ACADEMY_FAST);
		} else {
			ClueManager.Instance().addClueInfo(Clue.ClueInfo.ACADEMY_FOUND);
		}
	}
}
