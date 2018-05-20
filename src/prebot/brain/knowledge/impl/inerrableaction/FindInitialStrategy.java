package prebot.brain.knowledge.impl.inerrableaction;

import bwapi.Race;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.knowledge.impl.protoss.DarkTemplarSafeTime;
import prebot.common.code.Code.InitialBuildType;
import prebot.main.Prebot;

/**
 * 종족별 초기 전략을 불러온다.
 */
public class FindInitialStrategy extends InerrableActionKnowledge {
	@Override
	protected boolean doSomething() {
		if (Prebot.Game.enemy().getRace() == Race.Protoss) {
			Idea.of().initialBuildType = InitialBuildType.TWO_FACTORY_MULTI;
			Idea.of().newKnowledgeList.add(new GasAdjustmentMechanic());
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			Idea.of().newKnowledgeList.add(new DarkTemplarSafeTime());
			return false;

		} else if (Prebot.Game.enemy().getRace() == Race.Zerg) {
			Idea.of().initialBuildType = InitialBuildType.FAST_BARRACKS_ONE_FACOTRY_MULTI;
			Idea.of().newKnowledgeList.add(new GasAdjustment8Barrack111());
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
			
			return false;

		} else if (Prebot.Game.enemy().getRace() == Race.Terran) {
			Idea.of().initialBuildType = InitialBuildType.TWO_FACTORY_MULTI;
			Idea.of().newKnowledgeList.add(new GasAdjustmentMechanic());
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			return false;

		} else {
			Idea.of().initialBuildType = InitialBuildType.TWO_FACTORY_MULTI;
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			return true;
		}
	}
}