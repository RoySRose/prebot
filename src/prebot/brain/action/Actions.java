package prebot.brain.action;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.PreBot;
import prebot.manager.InformationManager;
import prebot.manager.WorkerManager;

public class Actions {

	/// 메카닉 테란 가스 조절
	public static final class MechanicTerranGasAdjustment extends Action {
		@Override
		public void doAction() {
			ActionVariables.gasAdjustment = false;
			ActionVariables.gasAdjustmentWorkerCount = 0;
		}

		@Override
		public void finalize() {
			super.finalize();
			ActionVariables.gasAdjustment = false;
			ActionVariables.gasAdjustmentWorkerCount = 0;
		}

		@Override
		public String toString() {
			return "MechanicTerranGasAdjustment []";
		}
	}

	/// 기본 가스조절로직을 무시하고, gasAmount만큼 가스 채취.
	public static final class ThreeGasWorkerUntil extends Action {
		private final int gasAmount;

		public ThreeGasWorkerUntil(int gasAmount) {
			this.gasAmount = gasAmount;
		}
		
		@Override
		public void doAction() {
			ActionVariables.gasAdjustment = true;
			ActionVariables.gasAdjustmentWorkerCount = 3;
		}

		@Override
		public boolean exitCondition() {
			return PreBot.Broodwar.self().gas() >= gasAmount;
		}

		@Override
		public void finalize() {
			super.finalize();
			ActionVariables.gasAdjustment = false;
			ActionVariables.gasAdjustmentWorkerCount = 0;
		}

		@Override
		public String toString() {
			return "ThreeGasWorkerUntil [gasAmount=" + gasAmount + "]";
		}
	}

	/// 530 뮤탈 전략용 가스조절 (초반빌드에서 가스가 남는 현상을 막는다.)
	public class GasAdjustFor530Mute extends Action {
		@Override
		public void doAction() {
			ActionVariables.gasAdjustment = true;
			int droneCount = PreBot.Broodwar.self().completedUnitCount(UnitType.Zerg_Drone);
			int extractorCount = PreBot.Broodwar.self().completedUnitCount(UnitType.Zerg_Extractor);
			if (droneCount >= 11 + extractorCount * 2) {
				ActionVariables.gasAdjustmentWorkerCount = 3;
			} else if (droneCount >= 10 + extractorCount * 2) {
				ActionVariables.gasAdjustmentWorkerCount = 2;
			} else if (droneCount >= 8 + extractorCount * 2) {
				ActionVariables.gasAdjustmentWorkerCount = 1;
			}
		}

		@Override
		public boolean exitCondition() {
			return PreBot.Broodwar.getFrameCount() >= (13 * TimeUtils.MINUTE); /// 종료시간 13분
		}

		@Override
		public void finalize() {
			super.finalize();
			ActionVariables.gasAdjustment = false;
			ActionVariables.gasAdjustmentWorkerCount = 0;
		}

		@Override
		public String toString() {
			return "GasAdjustFor530Mute []";
		}
	}

	/// ScoutManager 기본소스에서 가져와서 수정함
	/// unitType의 빌드완료시간이 remainingFrames만큼 남았으면 정찰 (remainingFrames이 0이면 완료시 정찰)
	public class WorkerScoutAfterBuild extends Action {
		private final int remainingFrames;
		private final UnitType unitType;

		public WorkerScoutAfterBuild(int remainingFrames, UnitType unitType) {
			this.remainingFrames = remainingFrames;
			this.unitType = unitType;
		}

		@Override
		public void doAction() {
			for (Unit unit : PreBot.Broodwar.self().getUnits()) {
				if (unit.getType() == unitType && unit.getRemainingBuildTime() <= remainingFrames) {
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
		public String toString() {
			return "WorkerScoutAfterBuild [remainingFrames=" + remainingFrames + ", unitType=" + unitType + "]";
		}
	}
}
