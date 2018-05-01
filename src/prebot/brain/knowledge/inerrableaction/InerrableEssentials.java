package prebot.brain.knowledge.inerrableaction;

import bwapi.Race;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.strategy.InitialStrategies;
import prebot.brain.strategy.StrategyInjecter;
import prebot.main.Prebot;

public class InerrableEssentials {
	
	/**
	 * 종족별 초기 전략을 불러온다.
	 */
	public static final class FindInitialStrategy extends InerrableActionKnowledge {
		@Override
		protected boolean doSomething() {
			if (Prebot.Game.enemy().getRace() == Race.Protoss) {
				StrategyInjecter.inject(new InitialStrategies.InitialStrategyForProtoss());
				return false;
				
			} else if (Prebot.Game.enemy().getRace() == Race.Zerg) {
				StrategyInjecter.inject(new InitialStrategies.InitialStrategyForZerg());
				return false;
				
			} else if (Prebot.Game.enemy().getRace() == Race.Terran) {
				StrategyInjecter.inject(new InitialStrategies.InitialStrategyForTerran());
				return false;
				
			} else {
				StrategyInjecter.inject(new InitialStrategies.InitialStrategyForRandom());
				return true;
			}
		}
	}
}
