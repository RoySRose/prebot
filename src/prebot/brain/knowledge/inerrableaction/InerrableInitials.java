package prebot.brain.knowledge.inerrableaction;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.brain.strategy.GeneralStrategies;
import prebot.brain.strategy.StrategyInjecter;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;
import prebot.main.manager.BuildManager;
import prebot.main.manager.InformationManager;

public class InerrableInitials {
	
	/**
	 * 메카닉 테란 가스 조절 기본 가스조절로직을 무시하고, gasAmount만큼 가스 채취.
	 */
	public static final class MechanicGasAdjustment extends InerrableActionKnowledge {
		@Override
		protected boolean doSomething() {
			if (TimeUtils.elapsedSeconds() > 200 || UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.ALL) >= 2) {
				Idea.of().gasAdjustment = false;
				Idea.of().gasAdjustmentWorkerCount = 0;
				return false;

			} else {
				Idea.of().gasAdjustment = true;
				int workerCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.COMPLETE);
				if (workerCount < 8) {
					Idea.of().gasAdjustmentWorkerCount = 0;
				} else {
					if (UnitUtils.hasUnit(UnitType.Terran_Factory, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE) || Prebot.Game.self().gas() >= 100) {
						Idea.of().gasAdjustmentWorkerCount = 1;
					} else {
						Idea.of().gasAdjustmentWorkerCount = 3;
					}
				}
				return true;
			}
		}
	}

	/**
	 * ScoutManager 기본소스에서 가져와서 수정함 unitType의 빌드완료시간이 remainingSeconds만큼 남았으면 정찰시작 (remainingSeconds이 0이면 완료시 정찰)
	 */
	public static final class ScvScoutAfterBuild extends InerrableActionKnowledge {
		private final UnitType buildingType;
		private final int remainingSeconds;
		private final int scvScoutMaxCount;

		public ScvScoutAfterBuild(UnitType buildingType, int remainingSeconds, int scvScoutMaxCount) {
			this.buildingType = buildingType;
			this.remainingSeconds = remainingSeconds;
			this.scvScoutMaxCount = scvScoutMaxCount;
		}

		@Override
		protected boolean doSomething() {
			if (InformationManager.Instance().getMainBaseLocation(Prebot.Game.enemy()) != null) {
				Idea.of().scvScoutMaxCount = 0;
				return false;
			} else {
				for (Unit building : UnitUtils.getUnitList(buildingType, UnitFindRange.ALL)) {
					if (building.getType() == buildingType && TimeUtils.framesToSeconds(building.getRemainingBuildTime()) <= remainingSeconds) {
						Idea.of().scvScoutMaxCount = scvScoutMaxCount;
						break;
					}
				}
				return true;
			}
		}
	}
	
	/** 
	 * build큐가 empty가 되었을때 general strategy로 변경
	 *  */
	public static final class UsePrebot1BuildStrategy extends InerrableActionKnowledge {
		@Override
		protected boolean doSomething() {
			if (BuildManager.Instance().buildQueue.isEmpty()) {
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
