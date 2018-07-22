package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.Mission;
import prebot.strategy.constant.EnemyStrategyOptions.Mission.MissionType;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.EnemyBuildTimer;

public abstract class Strategist {
	private int phase = 1;
	private UnitType phase01keyUnitType;
	
	public Strategist(UnitType phase01keyUnitType) {
		this.phase01keyUnitType = phase01keyUnitType;
	}

	public int getPhase() {
		return phase;
	}
	
	public EnemyStrategy strategyToApply() {
		switch(phaseSwitch()) {
		case 1: return StrategyIdea.startStrategy = strategyPhase01();
		case 2: return strategyPhase02();
		default: return strategyPhase03();
		}
	}

	protected abstract EnemyStrategy strategyPhase01();
	protected abstract EnemyStrategy strategyPhase02();
	protected abstract EnemyStrategy strategyPhase03();

	protected int phaseSwitch() {
		if (phase == 1 && phase01End()) {
			phase++;
		} else if (phase == 2 && phase02End()) {
			phase++;
		}
		return phase;
	}

	private boolean phase01End() {
		if (TimeUtils.after(TimeUtils.timeToFrames(4, 0))) {
			return true;
		}
		int buildTimeExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(phase01keyUnitType);
		return buildTimeExpect != CommonCode.UNKNOWN && TimeUtils.after(buildTimeExpect);
	}

	private boolean phase02End() {
		if (TimeUtils.after(10 * TimeUtils.MINUTE)) {
			System.out.println("TIME IS UP");
			return true;
		}

		// default mission
		if (UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center).size() < 2) {
			return false;
		}
		
		if (!StrategyIdea.currentStrategy.missionTypeList.isEmpty()) {
			for (MissionType missionType : StrategyIdea.currentStrategy.missionTypeList) {
				if (!Mission.complete(missionType)) {
					return false;
				}
			}
			System.out.println("MISSION COMPLETE");
			return true;
			
		} else {
			
			if (UnitUtils.myFactoryUnitSupplyCount() + UnitUtils.myWraithUnitSupplyCount() < 32) {
				return false;
			}
			System.out.println("DEFAULT MISSION COMPLETE");
			return true;
			
		}
	}

	public boolean hasInfo(ClueInfo info) {
		return ClueManager.Instance().containsClueInfo(info);
	}
	
	public boolean hasType(ClueType type) {
		return ClueManager.Instance().containsClueType(type);
	}
	
	public boolean hasAnyInfo(ClueInfo... infos) {
		for (ClueInfo info : infos) {
			if (hasInfo(info)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAllInfo(ClueInfo... infos) {
		for (ClueInfo info : infos) {
			if (!hasInfo(info)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean hasAnyType(ClueType... types) {
		for (ClueType type : types) {
			if (hasType(type)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAllType(ClueType... types) {
		for (ClueType type : types) {
			if (!hasType(type)) {
				return false;
			}
		}
		return true;
	}
}
