package prebot.brain.knowledge.inerrable;

import bwapi.Race;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.strategy.FixedStrategies;
import prebot.brain.strategy.GeneralStrategies;
import prebot.brain.strategy.StrategyInjecter;
import prebot.main.Prebot;
import prebot.main.manager.BuildManager;

public class InerrableEssentials {
	
	/**
	 * 메카닉 테란 가스 조절 기본 가스조절로직을 무시하고, gasAmount만큼 가스 채취.
	 */
	public static final class FindInitialStrategy extends InerrableActionKnowledge {
		@Override
		protected boolean doSomething() {
			if (Prebot.Game.enemy().getRace() == Race.Protoss) {
				StrategyInjecter.inject(new FixedStrategies.InitialStrategyForProtoss());
				return false;
				
			} else if (Prebot.Game.enemy().getRace() == Race.Zerg) {
				StrategyInjecter.inject(new FixedStrategies.InitialStrategyForZerg());
				return false;
				
			} else if (Prebot.Game.enemy().getRace() == Race.Terran) {
				StrategyInjecter.inject(new FixedStrategies.InitialStrategyForTerran());
				return false;
				
			} else {
				StrategyInjecter.inject(new FixedStrategies.InitialStrategyForRandom());
				return true;
			}
		}
	}
	
	public static final class UsePrebot1BuildStrategy extends InerrableActionKnowledge {
		@Override
		protected boolean doSomething() {
			if (Idea.of().buildActionList.isEmpty() && BuildManager.Instance().buildQueue.isEmpty()) {
				if (Prebot.Game.enemy().getRace() == Race.Protoss) {
					StrategyInjecter.inject(new GeneralStrategies.GeneralStrategyForProtoss());
					
				} else if (Prebot.Game.enemy().getRace() == Race.Zerg) {
					StrategyInjecter.inject(new GeneralStrategies.GeneralStrategyForZerg());
					
				} else if (Prebot.Game.enemy().getRace() == Race.Terran) {
					StrategyInjecter.inject(new GeneralStrategies.GeneralStrategyForTerran());
					
				} else {
					StrategyInjecter.inject(new GeneralStrategies.GeneralStrategyForProtoss());
				}
				return false;
				
			} else {
				return true;
			}
		}
	}
}
