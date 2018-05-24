package prebot.brain.knowledge.impl.inerrableaction;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.knowledge.Knowledge;
import prebot.brain.knowledge.impl.protoss.DarkTemplarSafeTime;
import prebot.brain.knowledge.impl.squad.ActivateEarlyDefenseSquad;
import prebot.brain.knowledge.impl.squad.ActivateScoutDefense;
import prebot.common.code.Code.InitialBuildType;
import prebot.common.main.Prebot;

/**
 * 종족별 초기 전략을 불러온다.
 */
public class FindInitialStrategy extends InerrableActionKnowledge {
	private boolean isRandom = false;

	@Override
	protected boolean doSomething() {
		if (Prebot.Game.enemy().getRace() == Race.Protoss) {
			Idea.of().initialBuildType = InitialBuildType.TWO_FACTORY_MULTI;
			if (!isRandom) {
				Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			}
			Idea.of().newKnowledgeList.add(new GasAdjustmentMechanic());

			Idea.of().newKnowledgeList.addAll(protossKnowledgeList());
			Idea.of().newKnowledgeList.addAll(getCommonKnowledgeList());
			return false;

		} else if (Prebot.Game.enemy().getRace() == Race.Zerg) {
			Idea.of().initialBuildType = InitialBuildType.FAST_BARRACKS_ONE_FACOTRY_MULTI;
			if (!isRandom) {
				Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
				Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
			}
			Idea.of().newKnowledgeList.add(new GasAdjustment8Barrack111());
			
			Idea.of().newKnowledgeList.addAll(zergKnowledgeList());
			Idea.of().newKnowledgeList.addAll(getCommonKnowledgeList());
			return false;

		} else if (Prebot.Game.enemy().getRace() == Race.Terran) {
			Idea.of().initialBuildType = InitialBuildType.TWO_FACTORY_MULTI;
			if (!isRandom) {
				Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, 0));
			}
			Idea.of().newKnowledgeList.add(new GasAdjustmentMechanic());
			
			Idea.of().newKnowledgeList.addAll(terranKnowledgeList());
			Idea.of().newKnowledgeList.addAll(getCommonKnowledgeList());
			return false;

		} else {
			isRandom = true;
			
			Idea.of().initialBuildType = InitialBuildType.FAST_BARRACKS_ONE_FACOTRY_MULTI;
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Supply_Depot, UnitType.Terran_Supply_Depot.buildTime()));
			Idea.of().newKnowledgeList.add(new ScvScoutAfterBuild(UnitType.Terran_Barracks, 0));
			return true;
		}
	}
	
	public List<Knowledge> getCommonKnowledgeList() {
		List<Knowledge> knowledgeList = new ArrayList<>();
		knowledgeList.add(new ActivateScoutDefense());
		knowledgeList.add(new ActivateEarlyDefenseSquad());
		knowledgeList.add(new UsePrebot1BuildStrategy());
		
		return knowledgeList;
	}
	
	public List<Knowledge> protossKnowledgeList() {
		List<Knowledge> knowledgeList = new ArrayList<>();
		knowledgeList.add(new DarkTemplarSafeTime());
		
		return knowledgeList;
	}
	
	public List<Knowledge> zergKnowledgeList() {
		List<Knowledge> knowledgeList = new ArrayList<>();
		// TODO
		
		return knowledgeList;
	}
	
	public List<Knowledge> terranKnowledgeList() {
		List<Knowledge> knowledgeList = new ArrayList<>();
		// TODO
		
		return knowledgeList;
	}
}