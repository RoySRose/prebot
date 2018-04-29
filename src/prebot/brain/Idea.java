package prebot.brain;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.knowledge.Knowledge;
import prebot.brain.squad.Squad;
import prebot.main.manager.StrategyManager;

public class Idea {
	public static Idea of() {
		return StrategyManager.Instance().getIdea();
	}
	
	private List<Knowledge> newKnowledgeList = new ArrayList<>();
	public List<Knowledge> knowledgeList = new ArrayList<>();
	public List<BuildAction> buildActionList = new ArrayList<>();
	public List<Squad> squadList = new ArrayList<>();
	
	public String strategyName;
	public boolean gasAdjustment = false; // true인 경우 gasAdjustmentWorkerCount에 따른 가스조절 
	public int gasAdjustmentWorkerCount = 0;
	
	public int checkerMaxCount = 0;
	public int scvScoutMaxCount = 0;
	
	public Position campPosition;
	public Position attackPosition;
	public BaseLocation expansionBase;

	public BaseLocation enemeyExpectedBase;
	
	public void addKnowledge(Knowledge knowledge) {
		newKnowledgeList.add(knowledge);
	}
	
	public void addKnowledgeList(List<Knowledge> knowledgeList) {
		newKnowledgeList.addAll(knowledgeList);
	}
	
	public void arrangeKnowledgeList() {
		for (Knowledge newKnowledge : newKnowledgeList) {
			boolean hasKnowledge = false;
			for (Knowledge oldKnowledge : knowledgeList) {
				if (oldKnowledge.knowledgeName().equals(newKnowledge.knowledgeName())) {
					hasKnowledge = true;
					break;
				}
			}
			if (!hasKnowledge) {
				knowledgeList.add(newKnowledge);
			}
		}
	}
}
