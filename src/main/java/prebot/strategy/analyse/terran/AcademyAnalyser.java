package prebot.strategy.analyse.terran;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class AcademyAnalyser extends UnitAnalyser {

	public AcademyAnalyser() {
		super(UnitType.Terran_Academy);
	}

	@Override
	public void analyse() {
		fastAcademy();
	}

	private void fastAcademy() {
		if (ClueManager.Instance().containsClueType(ClueType.FAST_ACADEMY)) {
			return;
		}
		
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}
		
		int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
		int academyFrame = EnemyStrategy.TERRAN_BIONIC.buildTimeMap.frame(UnitType.Terran_Academy, 30);
		
		if (academyFrame < buildFrame) {
			ClueManager.Instance().addClueInfo(ClueInfo.ACADEMY_FAST);
		} else {
			ClueManager.Instance().addClueInfo(ClueInfo.ACADEMY_FOUND);
		}
	}
}
