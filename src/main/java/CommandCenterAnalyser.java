

import java.util.List;

import bwapi.UnitType;
import bwta.BaseLocation;

public class CommandCenterAnalyser extends UnitAnalyser {

	public CommandCenterAnalyser() {
		super(UnitType.Terran_Command_Center);
	}

	@Override
	public void analyse() {
		fastDoubleCommand();
	}

	private void fastDoubleCommand() {
		if (ClueManager.Instance().containsClueType(Clue.ClueType.FAST_COMMAND)) {
			return;
		}
		
		int doubleFrame = EnemyStrategy.TERRAN_NO_BARRACKS_DOUBLE.buildTimeMap.frame(UnitType.Terran_Command_Center, 20);
		int oneBarrackDoubleFrame = EnemyStrategy.TERRAN_1BARRACKS_DOUBLE.buildTimeMap.frame(UnitType.Terran_Command_Center, 20);
		int oneFacDoubleFrame = EnemyStrategy.TERRAN_1FAC_DOUBLE.buildTimeMap.frame(UnitType.Terran_Command_Center, 30);
		
		List<UnitInfo> found = found();
		if (found.isEmpty()) {
			return;
		}
		
		BaseLocation enemyBase = InfoUtils.enemyBase();
		if (enemyBase == null) {
			return;
		}
		
		if (found.size() == 1) {
			double distance = found.get(0).getLastPosition().getDistance(enemyBase.getPosition());
			if (distance < 100) {
				return;
			}
		}
			
		int buildFrame = buildStartFrameDefaultJustBefore(found.get(0));
		
		if (buildFrame < doubleFrame) { // 노배럭 더블
			ClueManager.Instance().addClueInfo(Clue.ClueInfo.COMMAND_FASTEST_DOUBLE);
			
		} else if (buildFrame < oneBarrackDoubleFrame) { // 원배럭 더블
			ClueManager.Instance().addClueInfo(Clue.ClueInfo.COMMAND_FAST_DOUBLE);
			
		} else if (buildFrame < oneFacDoubleFrame) { // 팩 더블
			ClueManager.Instance().addClueInfo(Clue.ClueInfo.COMMAND_FAC_DOUBLE);
		}
	}
}
