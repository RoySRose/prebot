package prebot.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.brain.action.Action;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.history.History;
import prebot.brain.history.HistoryFrame;
import prebot.brain.strategy.GeneralStrategies;
import prebot.brain.strategy.GeneralStrategy;
import prebot.brain.strategy.InitialStrategies;
import prebot.brain.strategy.InitialStrategy;
import prebot.brain.strategy.Strategy;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.UnitCache;
import prebot.main.PreBot;
import prebot.main.manager.InformationManager;

/// 시간별 있었던 일들을 토대로 적의 전략을 예측하고, 행동을 구성한다.
public class Brain {

	public History historyData = new History();
	
	public Idea think(Idea previousIdea) {
		// history를 쌓는다.
		this.stackHistory();
		
		 // history를 분석하여 idea구성
		Idea idea = new Idea();
		idea.strategy = this.thinkStrategy(previousIdea);
		
		this.setDummyData(idea);
		return idea;
	}

	/** for test */
	private void setDummyData(Idea idea) {
		BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(PreBot.Broodwar.self());
		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(PreBot.Broodwar.enemy());
		
		if (myBase != null) {
			idea.campPosition = myBase.getPosition();
		}
		if (enemyBase != null) {
			idea.attackPosition = enemyBase.getPosition();
		}
	}

	private void stackHistory() {
		int seconds = TimeUtils.elapsedSeconds();
		UnitCache unitCache = UnitCache.getCurrentCache();

		// TODO unit 정보를 담는다. 판단에 어떤 정보가 더 필요할지에 따라 정보가 추가되어야 한다. 
		HistoryFrame historyFrame = new HistoryFrame(seconds, unitCache);
		historyData.add(historyFrame);
	}

	private Strategy thinkStrategy(Idea previousIdea) {
		if (previousIdea == null) {
			return InitialStrategies.initialStrategy(PreBot.Broodwar.enemy().getRace());
		}
		
		// TOOD 결과로 나온 STRATEGY를 새로운(new) 객체로 리턴
		// 분석결과(analysed data)
		Strategy analysedStrategy = analysedStrategy(previousIdea.strategy);
		boolean strategyChanged = !analysedStrategy.getClass().equals(previousIdea.getClass());
		
		// 1. 빌드액션
		// - 초반빌드큐는 항상 모든 빌드아이템을 리턴하고, 처리된 항목은 제외한다.
		if (analysedStrategy instanceof InitialStrategy) {
			analysedStrategy.buildActionList = this.finishedBuildActionRemoved(analysedStrategy.buildActionList);
			if (analysedStrategy.buildActionList.isEmpty()) {
				return GeneralStrategies.generalStrategy(PreBot.Broodwar.enemy().getRace());
			}
			
		} else if (analysedStrategy instanceof GeneralStrategy) {
			// nothing to do
		}
		
		// 2. 스쿼드리스트
		// TODO 스쿼드 구성
		
		
		// 3. 액션리스트
		// - 전략이 변경되지 않았으면 이전 액션리스트로 유지되며 관리한다.
		if (!strategyChanged) {
			analysedStrategy.actionList = this.finishedActionRemoved(previousIdea.strategy.actionList);
		}
		
		return analysedStrategy;
	}

	private Strategy analysedStrategy(Strategy previousStrategy) {
		if (previousStrategy instanceof GeneralStrategy) {
			return GeneralStrategies.generalStrategy(PreBot.Broodwar.enemy().getRace());
		} else {
			return InitialStrategies.initialStrategy(PreBot.Broodwar.enemy().getRace());
		}
	}

	private List<BuildAction> finishedBuildActionRemoved(List<BuildAction> buildActionList) {
		List<BuildAction> resultBuildActionList = new ArrayList<>();

		Map<UnitType, Integer> buildUnitCountMap = new HashMap<>();
		for (BuildAction buildAction : buildActionList) {
			UnitType unitType = buildAction.getBuildType();
			Integer count = buildUnitCountMap.get(unitType);
			count = count == null ? 1 : count + 1;
			buildUnitCountMap.put(unitType, count);
			if (UnitUtils.hasUnit(unitType, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, count)) {
				continue;
			}
			resultBuildActionList.add(buildAction);
		}
		
		return resultBuildActionList;
	}

	private List<Action> finishedActionRemoved(List<Action> actionList) {
		List<Action> removeActionList = new ArrayList<>();
		List<Action> addActionList = new ArrayList<>();
		for (Action action : actionList) {
			if (action.exitCondition()) {
				removeActionList.add(action);
				if (action.next != null) {
					addActionList.add(action.next);
				}
			}
		}
		
		List<Action> resultActionList = new ArrayList<>(actionList);
		resultActionList.removeAll(removeActionList); // 종료된 action 제거
		resultActionList.addAll(addActionList); // 연결된 다음 action 추가
		
		return resultActionList;
	}

}
