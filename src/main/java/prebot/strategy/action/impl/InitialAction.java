package prebot.strategy.action.impl;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.main.Prebot;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.Action;
import prebot.strategy.constant.EnemyStrategy;

/**
 * 종족별 초기 전략을 불러온다.
 */
public class InitialAction extends Action {
	
	private boolean alreadyAssignFirstScout;
	
	public InitialAction(boolean alreadyAssignFirstScout) {
		this.alreadyAssignFirstScout = alreadyAssignFirstScout;
	}

	@Override
	public boolean exitCondition() {
		if (Prebot.Broodwar.enemy().getRace() != Race.Unknown) {
			initAction();
			addInitialAction();
			return true;
		}
		return false;
	}
	
	public void initAction() {
		if (Prebot.Broodwar.enemy().getRace() == Race.Protoss) {
			StrategyIdea.enemyStrategy = EnemyStrategy.PROTOSS_INIT;

		} else if (Prebot.Broodwar.enemy().getRace() == Race.Zerg) {
			StrategyIdea.enemyStrategy = EnemyStrategy.ZERG_INIT;

		} else if (Prebot.Broodwar.enemy().getRace() == Race.Terran) {
			StrategyIdea.enemyStrategy = EnemyStrategy.TERRAN_INIT;
		}
	}

	private void addInitialAction() {
		if (Prebot.Broodwar.enemy().getRace() == Race.Protoss) {
			nextActions.add(new GasAdjustmentMechanic());
			if (!alreadyAssignFirstScout) {
				nextActions.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			}

		} else if (Prebot.Broodwar.enemy().getRace() == Race.Zerg) {
			if (!alreadyAssignFirstScout) {
				nextActions.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
			}
			nextActions.add(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
			nextActions.add(new GasAdjustment8Barrack111());

		} else if (Prebot.Broodwar.enemy().getRace() == Race.Terran) {
			if (!alreadyAssignFirstScout) {
				nextActions.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			}
			nextActions.add(new GasAdjustmentMechanic());
		}
	}

	@Override
	public void action() {
	}
}
