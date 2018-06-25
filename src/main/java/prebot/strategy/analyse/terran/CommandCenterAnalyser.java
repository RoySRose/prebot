package prebot.strategy.analyse.terran;

import java.util.List;

import bwapi.UnitType;
import prebot.strategy.UnitInfo;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public class CommandCenterAnalyser extends UnitAnalyser {

	public CommandCenterAnalyser() {
		super(UnitType.Terran_Command_Center);
	}

	@Override
	public void analyse() {
		if (!ClueManager.Instance().containsClueType(ClueType.FAST_COMMAND)) {
			int doubleFrame = EnemyStrategy.TERRAN_NO_BARRACKS_DOUBLE.defaultTimeMap.time(UnitType.Terran_Command_Center, 20);
			int oneBarrackDoubleFrame = EnemyStrategy.TERRAN_1BARRACKS_DOUBLE.defaultTimeMap.time(UnitType.Terran_Command_Center, 20);
			
			List<UnitInfo> found = found();
			if (!found.isEmpty()) {
				int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
				
				if (buildFrame < doubleFrame) { // 생더블 확정
					ClueManager.Instance().addClueInfo(ClueInfo.COMMAND_FASTEST_DOUBLE);
					
				} else if (buildFrame < oneBarrackDoubleFrame) { // 원배럭 더블
					ClueManager.Instance().addClueInfo(ClueInfo.COMMAND_FAST_DOUBLE);
				}
			}
		}
		
	}
}
