package prebot.common.debug.chat.impl;

import bwapi.Race;
import prebot.common.debug.chat.ChatExecuter;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;

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
				StrategyIdea.enemyStrategy = enemyStrategy;
				return;
			}
		}
	}
};