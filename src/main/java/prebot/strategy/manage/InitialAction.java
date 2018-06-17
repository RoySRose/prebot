package prebot.strategy.manage;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.strategy.action.impl.GasAdjustment8Barrack111;
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
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
				assignedFirstScout = true;
			}
//			RaceActionManager.Instance().setAction(new ProtossBuildPhase1());
			terminated = true;
			
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
				assignedFirstScout = true;
			}
			ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
			ActionManager.Instance().addAction(new GasAdjustment8Barrack111());
//			RaceActionManager.Instance().setAction(new ZergBuildPhase1());
			terminated = true;
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			ActionManager.Instance().addAction(new GasAdjustmentMechanic());
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
				assignedFirstScout = true;
			}
//			RaceActionManager.Instance().setAction(new TerranBuildPhase1());
			terminated = true;
			
		} else {
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
				assignedFirstScout = true;
			}
		}
	}
}