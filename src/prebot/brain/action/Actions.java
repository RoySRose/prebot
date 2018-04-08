package prebot.brain.action;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.code.GameConstant;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.PreBot;
import prebot.manager.InformationManager;
import prebot.manager.WorkerManager;

public class Actions {

	/**
	 * 기본 가스일꾼 조절 : 최소 미네랄일꾼 7기 기준
	 * [가스통 1개]
	 * 일꾼 00-07기 : 가스일꾼 0기
	 * 일꾼 08-09기 : 가스일꾼 1기
	 * 일꾼 10-11기 : 가스일꾼 2기
	 * 일꾼 12-00기 : 가스일꾼 3기
	 * 
	 * [가스통 x2 기준]
	 * 일꾼 00-09기, 가스일꾼 0기
	 * 일꾼 10-13기, 가스일꾼 1기
	 * 일꾼 14-17기, 가스일꾼 2기
	 * 일꾼 18-00기, 가스일꾼 3기
	 */
	public static final class DefaultGasAdjustment extends Action {

		private int minimumMineralWorkerCount;
		
		public DefaultGasAdjustment(int minimumMineralWorkerCount) {
			this.minimumMineralWorkerCount = minimumMineralWorkerCount;
		}
		
		@Override
		public void doAction() {
			ActionVariables.gasAdjustment = true;
			int workerCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.COMPLETE);
			if (workerCount <= minimumMineralWorkerCount) {
				ActionVariables.gasAdjustmentWorkerCount = 0;
				
			} else {
				int refineryCount = UnitUtils.getUnitCount(UnitType.Terran_Refinery, UnitFindRange.COMPLETE);
				int workersPerRefinery = (int) (workerCount - minimumMineralWorkerCount + 1) / (refineryCount * 2);
				
				if (workersPerRefinery > GameConstant.WORKERS_PER_REFINERY) {
					ActionVariables.gasAdjustmentWorkerCount = GameConstant.WORKERS_PER_REFINERY;
				} else {
					ActionVariables.gasAdjustmentWorkerCount = workersPerRefinery;
				}
			}
		}

		@Override
		public void finalize() {
			ActionVariables.gasAdjustment = false;
			ActionVariables.gasAdjustmentWorkerCount = 0;
		}

		@Override
		public String toString() {
			return "DefaultGasAdjustment [minimumMineralWorkerCount=" + minimumMineralWorkerCount + "]";
		}
	}		
	

	/**
	 * 메카닉 테란 가스 조절
	 * 기본 가스조절로직을 무시하고, gasAmount만큼 가스 채취.
	 */
	public static final class MechanicTerranGasAdjustment extends Action {
		@Override
		public void doAction() {
			ActionVariables.gasAdjustment = true;
			int workerCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.COMPLETE);
			if (workerCount <= 5) {
				ActionVariables.gasAdjustmentWorkerCount = 0;
			} else {
				ActionVariables.gasAdjustmentWorkerCount = 3;
			}
		}
		
		@Override
		public boolean exitCondition() {
			return UnitUtils.hasUnit(UnitType.Terran_Factory, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE) || PreBot.Broodwar.self().gas() >= 100;
		}

		@Override
		public void finalize() {
			ActionVariables.gasAdjustment = false;
			ActionVariables.gasAdjustmentWorkerCount = 0;
		}

		@Override
		public String toString() {
			return "MechanicTerranGasAdjustment []";
		}
	}

	/**
	 * ScoutManager 기본소스에서 가져와서 수정함
	 * unitType의 빌드완료시간이 remainingSeconds만큼 남았으면 정찰시작 (remainingSeconds이 0이면 완료시 정찰)
	 */
	public static final class WorkerScoutAfterBuild extends Action {
		private final UnitType buildingType;
		private final int remainingSeconds;

		public WorkerScoutAfterBuild(UnitType buildingType, int remainingSeconds) {
			this.buildingType = buildingType;
			this.remainingSeconds = remainingSeconds;
		}

		@Override
		public void doAction() {
			if (InformationManager.Instance().getMainBaseLocation(PreBot.Broodwar.enemy()) != null
					|| WorkerManager.Instance().getScoutWorker() != null) {
				return;
			}
			
			for (Unit building : UnitUtils.getUnitList(buildingType, UnitFindRange.ALL)) {
				if (building.getType() == buildingType && TimeUtils.framesToSeconds(building.getRemainingBuildTime()) <= remainingSeconds) {
					Chokepoint firstChoke = InformationManager.Instance().getFirstChokePoint(PreBot.Broodwar.self());
					Unit scoutWorker = UnitUtils.getClosestMineralWorkerToPosition(WorkerManager.Instance().getWorkerData().getWorkers(), firstChoke.getCenter());
					if (scoutWorker != null) {
						WorkerManager.Instance().setScoutWorker(scoutWorker);
					}
					break;
				}
			}
			// 참고로, 일꾼의 정찰 임무를 해제하려면, 다음과 같이 하면 된다
			// WorkerManager::Instance().setIdleWorker(currentScoutUnit);
		}

		@Override
		public boolean exitCondition() {
			return InformationManager.Instance().getMainBaseLocation(PreBot.Broodwar.enemy()) != null;
		}

		@Override
		public void finalize() {
		}

		@Override
		public String toString() {
			return "WorkerScoutAfterBuild [remainingSeconds=" + remainingSeconds + ", buildingType=" + buildingType + "]";
		}
	}
}
