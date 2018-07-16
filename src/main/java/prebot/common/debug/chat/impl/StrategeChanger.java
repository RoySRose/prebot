package prebot.common.debug.chat.impl;

import bwapi.Race;
import prebot.common.debug.chat.ChatExecuter;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

public class StrategeChanger extends ChatExecuter {
	public StrategeChanger(char type) {
		super(type);
	}

	@Override
	public void execute(String option) {
		Race enemyRace = InformationManager.Instance().enemyRace;
		String inputStrategyName = (enemyRace.toString() + "_" + option).replaceAll(" ", "_").toUpperCase();

		for (EnemyStrategy enemyStrategy : EnemyStrategy.values()) {
			if (enemyStrategy.name().toUpperCase().equals(inputStrategyName)) {
				StrategyIdea.currentStrategy = enemyStrategy;
				applyDetailValue(enemyStrategy);
				return;
			}
		}
	}

	// StrategyAnalyseManager에서 copy
	private void applyDetailValue(EnemyStrategy currentStrategy) {
		StrategyIdea.factoryRatio = currentStrategy.factoryRatio;
		StrategyIdea.upgrade = currentStrategy.upgrade;
		StrategyIdea.marineCount = currentStrategy.marineCount;
		
		if (currentStrategy.addOnOption != null) {
			StrategyIdea.addOnOption = currentStrategy.addOnOption;
		}
		if (currentStrategy.expansionOption != null) {
			StrategyIdea.expansionOption = currentStrategy.expansionOption;
			if (currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT) {
				StrategyIdea.wraithCount = 4; // default wraith count
			} else {
				StrategyIdea.wraithCount = 0;
			}
		}
		if (currentStrategy.buildTimeMap != null) {
			StrategyIdea.buildTimeMap = currentStrategy.buildTimeMap;
		}
	}
};