package prebot.strategy.manage;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.strategy.action.impl.GasAdjustmentMechanic;
import prebot.strategy.action.impl.ScvScoutAfterBuild;

/**
 * 종족별 초기 전략을 불러온다.
 */
public class InitialAction {

	private static InitialAction instance = new InitialAction();

	public static InitialAction Instance() {
		return instance;
	}

	private boolean terminated = false;
	private boolean assignedFirstScout = false;

	private InitialAction() {
	}

	public void update() {
		if (terminated || InfoUtils.enemyRace() == null) {
			return;
		}

		if (InfoUtils.enemyRace() == Race.Protoss) {
			ActionManager.Instance().addAction(new GasAdjustmentMechanic());
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Barracks, UnitType.Terran_Barracks.buildTime()));
				assignedFirstScout = true;
			}
			StrategyAnalyseManager.Instance().setUp(Race.Protoss);
			terminated = true;
			
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (!assignedFirstScout) {
				//ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Bunker, 5 * TimeUtils.SECOND));
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Refinery, 10));
				assignedFirstScout = true;
			}
//			ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
//			ActionManager.Instance().addAction(new GasAdjustment2Star());
			StrategyAnalyseManager.Instance().setUp(Race.Zerg);
			terminated = true;
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
//			ActionManager.Instance().addAction(new GasAdjustment2Star());
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
				assignedFirstScout = true;
			}
			StrategyAnalyseManager.Instance().setUp(Race.Terran);
			terminated = true;
			
		} else {
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
				assignedFirstScout = true;
			}
		}
	}
}
