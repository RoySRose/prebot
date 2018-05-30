package prebot.strategy.action.impl;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.main.Prebot;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.Action;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

/**
 * 종족별 초기 전략을 불러온다.
 */
public class InitialAction extends Action {

	@Override
	public boolean exitCondition() {
		if (Prebot.Broodwar.enemy().getRace() != Race.Unknown) {
			return true;
		}
		return false;
	}

	@Override
	public void action() {
		if (Prebot.Broodwar.enemy().getRace() == Race.Protoss) {
			StrategyIdea.enemyStrategy = EnemyStrategy.PROTOSSBASIC;
			StrategyIdea.enemyStrategyException = EnemyStrategyException.INIT;
			
			StrategyIdea.newActionList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			StrategyIdea.newActionList.add(new GasAdjustmentMechanic());

			StrategyIdea.newActionList.addAll(protossActionList());
			StrategyIdea.newActionList.addAll(getCommonActionList());

		} else if (Prebot.Broodwar.enemy().getRace() == Race.Zerg) {
			StrategyIdea.enemyStrategy = EnemyStrategy.ZERGBASIC;
			StrategyIdea.enemyStrategyException = EnemyStrategyException.INIT;
			
			StrategyIdea.newActionList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
			StrategyIdea.newActionList.add(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
			StrategyIdea.newActionList.add(new GasAdjustment8Barrack111());
			
			StrategyIdea.newActionList.addAll(zergActionList());
			StrategyIdea.newActionList.addAll(getCommonActionList());

		} else if (Prebot.Broodwar.enemy().getRace() == Race.Terran) {
			StrategyIdea.enemyStrategy = EnemyStrategy.TERRANBASIC;
			StrategyIdea.enemyStrategyException = EnemyStrategyException.INIT;
			
			StrategyIdea.newActionList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			StrategyIdea.newActionList.add(new GasAdjustmentMechanic());
			
			StrategyIdea.newActionList.addAll(terranActionList());
			StrategyIdea.newActionList.addAll(getCommonActionList());

		} else {
			StrategyIdea.enemyStrategy = EnemyStrategy.ZERGBASIC;
			StrategyIdea.enemyStrategyException = EnemyStrategyException.INIT;
			
			StrategyIdea.newActionList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
		}
	}
	
	public List<Action> getCommonActionList() {
		List<Action> actionList = new ArrayList<>();
		return actionList;
	}
	
	private List<Action> protossActionList() {
		List<Action> actionList = new ArrayList<>();
		return actionList;
	}
	
	private List<Action> zergActionList() {
		List<Action> actionList = new ArrayList<>();
		return actionList;
	}
	
	private List<Action> terranActionList() {
		List<Action> actionList = new ArrayList<>();
		return actionList;
	}
}