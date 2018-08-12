package prebot.strategy.manage;

import bwapi.Race;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.util.InfoUtils;
import prebot.strategy.InformationManager;
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
	private boolean assignedSecondScout = false;

	private InitialAction() {
	}

	public void update() {
		BaseLocation enemyBaseLocation = InfoUtils.enemyBase();
		if(enemyBaseLocation == null && !InformationManager.Instance().isFirstScoutAlive()){
			if (!assignedSecondScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
				assignedSecondScout = true;
				terminated = true;
			}
		}
		
		
		if (terminated || InfoUtils.enemyRace() == null) {
			return;
		}
		
		
		
		if (InfoUtils.enemyRace() == Race.Protoss) {
			ActionManager.Instance().addAction(new GasAdjustmentMechanic());
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0)); // 서플 완성후 출발
				assignedFirstScout = true;
			}
			StrategyAnalyseManager.Instance().setUp(Race.Protoss);
			terminated = true;
			
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			ActionManager.Instance().addAction(new GasAdjustmentMechanic());
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime())); // 서플 시작후 출발
				assignedFirstScout = true;
			}
			StrategyAnalyseManager.Instance().setUp(Race.Zerg);
			terminated = true;
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			ActionManager.Instance().addAction(new GasAdjustmentMechanic());
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0)); // 서플 완성후 출발
				assignedFirstScout = true;
			}
			StrategyAnalyseManager.Instance().setUp(Race.Terran);
			terminated = true;
			
		} else {
			if (!assignedFirstScout) {
				ActionManager.Instance().addAction(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0)); // 서플 완성후 출발
				assignedFirstScout = true;
			}
		}
	}
}
