package prebot.main.manager;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import prebot.brain.Idea;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.code.Code.WorkerJob;
import prebot.common.code.Config;
import prebot.common.util.CommandUtils;
import prebot.common.util.UnitUtils;
import prebot.information.WorkerData;
import prebot.information.WorkerMoveData;
import prebot.main.Prebot;

/// 일꾼 유닛들의 상태를 관리하고 컨트롤하는 class
public class WorkerManager extends GameManager {

	/// 각 Worker 에 대한 WorkerJob 상황을 저장하는 자료구조 객체
	private WorkerData workerData = new WorkerData();

	/// 일꾼 중 한명을 Repair Worker 로 정해서, 전체 수리 대상을 하나씩 순서대로 수리합니다
	private Unit currentRepairWorker = null;

	private static WorkerManager instance = new WorkerManager();

	/// static singleton 객체를 리턴합니다
	public static WorkerManager Instance() {
		return instance;
	}

	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트하고, 일꾼 유닛들이 자원 채취 등 임무 수행을 하도록 합니다
	public GameManager update() {
		// 1초에 1번만 실행한다
		// if (MyBotModule.Broodwar.getFrameCount() % 24 != 0)
		// return;

		updateWorkerStatus();
		handleGasWorkers();
		handleIdleWorkers();
		handleMoveWorkers();
//		handleRepairWorkers();
		
		return this;
	}

	public void updateWorkerStatus() {
		// Drone 은 건설을 위해 isConstructing = true 상태로 건설장소까지 이동한 후,
		// 잠깐 getBuildType() == none 가 되었다가, isConstructing = true, isMorphing = true 가 된 후, 건설을 시작한다

		// for each of our Workers
		for (Unit worker : workerData.getWorkers()) {
			if (!worker.isCompleted()) {
				continue;
			}

			// 게임상에서 worker가 isIdle 상태가 되었으면 (새로 탄생했거나, 그전 임무가 끝난 경우), WorkerData 도 Idle 로 맞춘 후, handleGasWorkers, handleIdleWorkers 등에서 새 임무를 지정한다
			if (worker.isIdle()) {
				// workerData 에서 Build / Move / Scout 로 임무지정한 경우, worker 는 즉 임무 수행 도중 (임무 완료 전) 에 일시적으로 isIdle 상태가 될 수 있다
				if (workerData.getWorkerJob(worker) != WorkerJob.BUILD
						&& workerData.getWorkerJob(worker) != WorkerJob.MOVE
						&& workerData.getWorkerJob(worker) != WorkerJob.COMBAT) {
					workerData.setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
				}
			}

			// if its job is gas
			if (workerData.getWorkerJob(worker) == WorkerJob.GAS) {
				Unit refinery = workerData.getWorkerResource(worker);

				// if the refinery doesn't exist anymore (파괴되었을 경우)
				if (refinery == null || !refinery.exists() || refinery.getHitPoints() <= 0) {
					workerData.setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
				}
			}

			// if its job is repair
			if (workerData.getWorkerJob(worker) == WorkerJob.REPAIR) {
				Unit repairTargetUnit = workerData.getWorkerRepairUnit(worker);

				// 대상이 파괴되었거나, 수리가 다 끝난 경우
				if (repairTargetUnit == null || !repairTargetUnit.exists() || repairTargetUnit.getHitPoints() <= 0
						|| repairTargetUnit.getHitPoints() == repairTargetUnit.getType().maxHitPoints()) {
					workerData.setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
				}
			}
		}
	}

	public void handleGasWorkers() {
		// for each unit we have
		for (Unit unit : Prebot.Game.self().getUnits()) {
			if (unit.getType().isWorker() && unit.isGatheringGas() && !unit.isCarryingGas()) {
				if (workerData.getWorkerJob(unit) != WorkerJob.GAS) {
					workerData.setWorkerJob(unit, WorkerJob.IDLE, (Unit) null);
				}
			}
			
			// refinery 가 건설 completed 되었으면,
			if (unit.getType().isRefinery() && unit.isCompleted()) {
				Unit refinery = unit;
				// get the number of workers currently assigned to it
				int numAssigned = workerData.getNumAssignedWorkers(refinery);

				// if it's less than we want it to be, fill 'er up
				// 단점 : 미네랄 일꾼은 적은데 가스 일꾼은 무조건 3~4명인 경우 발생.
				WorkerJob preJob = WorkerJob.MINERALS;
				WorkerJob postJob = WorkerJob.GAS;
				int numInsufficient = getAdjustedWorkersPerRefinery() - numAssigned;
				if (numInsufficient < 0) { // 가스조절이 필요
					numInsufficient *= -1;
					preJob = WorkerJob.GAS;
					postJob = WorkerJob.MINERALS;
				}
				for (int i = 0; i < numInsufficient; ++i) {
					Unit jobChangeWorker = chooseWorkerToChangeGatherJob(preJob, refinery); // mineral to gas
					if (jobChangeWorker != null) {
						if (postJob == WorkerJob.GAS) {
							workerData.setWorkerJob(jobChangeWorker, postJob, refinery);
						} else if (postJob == WorkerJob.MINERALS) {
							Unit resourceDepot = getClosestResourceDepotFromWorker(jobChangeWorker);
							workerData.setWorkerJob(jobChangeWorker, postJob, resourceDepot);
						}

					}
				}
			}
		}
	}

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
	private int getAdjustedWorkersPerRefinery() {
		if (Idea.of().gasAdjustment) {
			// specific count adjusted by action
			return Idea.of().gasAdjustmentWorkerCount;
			
		} else {
			int workerCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.COMPLETE);
			if (workerCount <= 7) {
				return 0;
				
			} else {
				int refineryCount = UnitUtils.getUnitCount(UnitType.Terran_Refinery, UnitFindRange.COMPLETE);
				int workersPerRefinery = (int) (workerCount - 7 + 1) / (refineryCount * 2);
				
				if (workersPerRefinery > Config.WORKERS_PER_REFINERY) {
					return Config.WORKERS_PER_REFINERY;
				} else {
					return workersPerRefinery;
				}
			}
		}
	}

	/// Idle 일꾼을 Mineral 일꾼으로 만듭니다
	public void handleIdleWorkers() {
		// for each of our workers
		for (Unit worker : workerData.getWorkers()) {
			if (worker == null)
				continue;

			// if worker's job is idle
			if (workerData.getWorkerJob(worker) == WorkerJob.IDLE || workerData.getWorkerJob(worker) == WorkerJob.DEFAULT) {
				// send it to the nearest mineral patch
				setMineralWorker(worker);
			}
		}
	}

	public void handleMoveWorkers() {
		// for each of our workers
		for (Unit worker : workerData.getWorkers()) {
			if (worker == null)
				continue;

			// if it is a move worker
			if (workerData.getWorkerJob(worker) == WorkerJob.MOVE) {
				WorkerMoveData data = workerData.getWorkerMoveData(worker);

				// 목적지에 도착한 경우 이동 명령을 해제한다
				if (worker.getPosition().getDistance(data.getPosition()) < 4) {
					setIdleWorker(worker);
				} else {
					CommandUtils.move(worker, data.getPosition());
				}
			}
		}
	}

	public void handleRepairWorkers() {
		if (Prebot.Game.self().getRace() != Race.Terran) {
			return;
		}

		for (Unit unit : Prebot.Game.self().getUnits()) {
			// 건물의 경우 아무리 멀어도 무조건 수리. 일꾼 한명이 순서대로 수리
			if (unit.getType().isBuilding() && unit.isCompleted() && unit.getHitPoints() < unit.getType().maxHitPoints()) {
				Unit repairWorker = chooseRepairWorkerClosestTo(unit.getPosition(), 0);
				setRepairWorker(repairWorker, unit);
				break;
			}
			// 메카닉 유닛 (SCV, 시즈탱크, 레이쓰 등)의 경우 근처에 SCV가 있는 경우 수리. 일꾼 한명이 순서대로 수리
			else if (unit.getType().isMechanical() && unit.isCompleted() && unit.getHitPoints() < unit.getType().maxHitPoints()) {
				// SCV 는 수리 대상에서 제외. 전투 유닛만 수리하도록 한다
				if (unit.getType() != UnitType.Terran_SCV) {
					Unit repairWorker = chooseRepairWorkerClosestTo(unit.getPosition(), 10 * Config.TILE_SIZE);
					setRepairWorker(repairWorker, unit);
					break;
				}
			}

		}
	}

	/// position 에서 가장 가까운 Mineral 혹은 Idle 혹은 Move 일꾼 유닛들 중에서 Repair 임무를 수행할 일꾼 유닛을 정해서 리턴합니다
	public Unit chooseRepairWorkerClosestTo(Position p, int maxRange) {
		if (!p.isValid())
			return null;

		Unit closestWorker = null;

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 변수 기본값 수정

		double closestDist = 1000000000;

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		if (currentRepairWorker != null && currentRepairWorker.exists() && currentRepairWorker.getHitPoints() > 0) {
			return currentRepairWorker;
		}

		// for each of our workers
		for (Unit worker : workerData.getWorkers()) {
			if (worker == null) {
				continue;
			}

			if (worker.isCompleted() && (workerData.getWorkerJob(worker) == WorkerJob.MINERALS || workerData.getWorkerJob(worker) == WorkerJob.IDLE
					|| workerData.getWorkerJob(worker) == WorkerJob.MOVE)) {
				double dist = worker.getDistance(p);

				if (closestWorker == null || (dist < closestDist && worker.isCarryingMinerals() == false && worker.isCarryingGas() == false)) {
					closestWorker = worker;
					dist = closestDist;
				}
			}
		}

		if (currentRepairWorker == null || currentRepairWorker.exists() == false || currentRepairWorker.getHitPoints() <= 0) {
			currentRepairWorker = closestWorker;
		}

		return closestWorker;
	}

	/// 해당 일꾼 유닛 unit 의 WorkerJob 값를 Mineral 로 변경합니다
	public void setMineralWorker(Unit unit) {
		if (unit == null)
			return;

		// check if there is a mineral available to send the worker to
		Unit depot = getClosestResourceDepotFromWorker(unit);

		// if there is a valid ResourceDepot (Command Center, Nexus, Hatchery)
		if (depot != null) {
			// update workerData with the new job
			workerData.setWorkerJob(unit, WorkerJob.MINERALS, depot);
		}
	}

	/// 해당 일꾼 유닛 unit 으로부터 가장 가까운 ResourceDepot 건물을 리턴합니다
	public Unit getClosestResourceDepotFromWorker(Unit worker) {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 멀티 기지간 일꾼 숫자 리밸런싱이 잘 일어나도록 버그 수정

		if (worker == null)
			return null;

		Unit closestDepot = null;
		double closestDistance = 1000000000;

		// 완성된, 공중에 떠있지 않고 땅에 정착해있는, ResourceDepot 혹은 Lair 나 Hive로 변형중인 Hatchery 중에서
		// 첫째로 미네랄 일꾼수가 꽉 차지않은 곳
		// 둘째로 가까운 곳을 찾는다
		for (Unit unit : Prebot.Game.self().getUnits()) {
			if (unit == null)
				continue;

			if (unit.getType().isResourceDepot() && (unit.isCompleted() || unit.getType() == UnitType.Zerg_Lair || unit.getType() == UnitType.Zerg_Hive)
					&& unit.isLifted() == false) {
				if (workerData.depotHasEnoughMineralWorkers(unit) == false) {
					double distance = unit.getDistance(worker);
					if (closestDistance > distance) {
						closestDepot = unit;
						closestDistance = distance;
					}
				}
			}
		}

		// 모든 ResourceDepot 이 다 일꾼수가 꽉 차있거나, 완성된 ResourceDepot 이 하나도 없고 건설중이라면,
		// ResourceDepot 주위에 미네랄이 남아있는 곳 중에서 가까운 곳이 선택되도록 한다
		if (closestDepot == null) {
			for (Unit unit : Prebot.Game.self().getUnits()) {
				if (unit == null)
					continue;

				if (unit.getType().isResourceDepot()) {
					if (UnitUtils.getNearMineralsCount(unit) > 0) {
						double distance = unit.getDistance(worker);
						if (closestDistance > distance) {
							closestDepot = unit;
							closestDistance = distance;
						}
					}
				}
			}

		}

		// 모든 ResourceDepot 주위에 미네랄이 하나도 없다면, 일꾼에게 가장 가까운 곳을 선택한다
		if (closestDepot == null) {
			for (Unit unit : Prebot.Game.self().getUnits()) {
				if (unit == null)
					continue;

				if (unit.getType().isResourceDepot()) {
					double distance = unit.getDistance(worker);
					if (closestDistance > distance) {
						closestDepot = unit;
						closestDistance = distance;
					}
				}
			}
		}

		return closestDepot;

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	}

	/// 해당 일꾼 유닛 unit 의 WorkerJob 값를 Idle 로 변경합니다
	public void setIdleWorker(Unit unit) {
		if (unit == null)
			return;

		workerData.setWorkerJob(unit, WorkerJob.IDLE, (Unit) null);
	}

	/// Mineral 일꾼 유닛들 중에서 Gas 임무를 수행할 일꾼 유닛을 정해서 리턴합니다<br>
	/// Idle 일꾼은 Build, Repair, Scout 등 다른 임무에 먼저 투입되어야 하기 때문에 Mineral 일꾼 중에서만 정합니다
	private Unit chooseWorkerToChangeGatherJob(WorkerJob workerJob, Unit refinery) {
		if (refinery == null)
			return null;

		Unit closestWorker = null;
		double closestDistance = 1000000000;

		for (Unit unit : workerData.getWorkers()) {
			if (!UnitUtils.isValidUnit(unit) || workerData.getWorkerJob(unit) != workerJob
					|| !unit.canMove() || !unit.isMoving()) { // 가스통 안에 있는 일꾼, 또는 자원을 채취하기 직전인 일꾼을 사용하지 않기 위함
				continue;
			}

			if (unit.isCarryingMinerals() || unit.isCarryingGas()) {
				continue;
			}

			double distance = unit.getDistance(refinery);
			if (closestWorker == null || distance < closestDistance) {
				closestWorker = unit;
				closestDistance = distance;
			}
		}

		return closestWorker;
	}

	public void setConstructionWorker(Unit worker, UnitType buildingType) {
		if (worker == null)
			return;

		workerData.setWorkerJob(worker, WorkerJob.BUILD, buildingType);
	}

	/// buildingPosition 에서 가장 가까운 Move 혹은 Idle 혹은 Mineral 일꾼 유닛들 중에서 Construction 임무를 수행할 일꾼 유닛을 정해서 리턴합니다<br>
	/// Move / Idle Worker 중에서 먼저 선정하고, 없으면 Mineral Worker 중에서 선정합니다<br>
	/// 일꾼 유닛이 2개 이상이면, avoidWorkerID 에 해당하는 worker 는 선정하지 않도록 합니다<br>
	/// if setJobAsConstructionWorker is true (default), it will be flagged as a builder unit<br>
	/// if setJobAsConstructionWorker is false, we just want to see which worker will build a building
	public Unit chooseConstuctionWorkerClosestTo(UnitType buildingType, TilePosition buildingPosition, boolean setJobAsConstructionWorker, int avoidWorkerID) {
		// variables to hold the closest worker of each type to the building
		Unit closestMovingWorker = null;
		Unit closestMiningWorker = null;

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 변수 기본값 수정

		double closestMovingWorkerDistance = 1000000000;
		double closestMiningWorkerDistance = 1000000000;

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		// look through each worker that had moved there first
		for (Unit unit : workerData.getWorkers()) {
			if (unit == null)
				continue;

			// worker 가 2개 이상이면, avoidWorkerID 는 피한다
			if (workerData.getWorkers().size() >= 2 && avoidWorkerID != 0 && unit.getID() == avoidWorkerID)
				continue;

			// Move / Idle Worker
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerJob.MOVE || workerData.getWorkerJob(unit) == WorkerJob.IDLE)) {
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(buildingPosition.toPosition());
				if (closestMovingWorker == null || (distance < closestMovingWorkerDistance && unit.isCarryingMinerals() == false && unit.isCarryingGas() == false)) {
					if (BWTA.isConnected(unit.getTilePosition(), buildingPosition)) {
						closestMovingWorker = unit;
						closestMovingWorkerDistance = distance;
					}
				}
			}

			// Move / Idle Worker 가 없을때, 다른 Worker 중에서 차출한다
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) != WorkerJob.MOVE && workerData.getWorkerJob(unit) != WorkerJob.IDLE
					&& workerData.getWorkerJob(unit) != WorkerJob.BUILD)) {
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(buildingPosition.toPosition());
				if (closestMiningWorker == null || (distance < closestMiningWorkerDistance && unit.isCarryingMinerals() == false && unit.isCarryingGas() == false)) {
					if (BWTA.isConnected(unit.getTilePosition(), buildingPosition)) {
						closestMiningWorker = unit;
						closestMiningWorkerDistance = distance;
					}
				}
			}
		}

		Unit chosenWorker = closestMovingWorker != null ? closestMovingWorker : closestMiningWorker;

		// if the worker exists (one may not have been found in rare cases)
		if (chosenWorker != null && setJobAsConstructionWorker) {
			workerData.setWorkerJob(chosenWorker, WorkerJob.BUILD, buildingType);
		}

		return chosenWorker;
	}

	// get a worker which will move to a current location
	public Unit chooseMoveWorkerClosestTo(Position p) {
		// set up the pointer
		Unit closestWorker = null;

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 변수 기본값 수정

		double closestDistance = 1000000000;

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		// for each worker we currently have
		for (Unit unit : workerData.getWorkers()) {
			if (unit == null)
				continue;

			// only consider it if it's a mineral worker
			if (unit.isCompleted() && workerData.getWorkerJob(unit) == WorkerJob.MINERALS) {
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(p);
				if (closestWorker == null || (distance < closestDistance && unit.isCarryingMinerals() == false && unit.isCarryingGas() == false)) {
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		// return the worker
		return closestWorker;
	}

	/// position 에서 가장 가까운 Mineral 혹은 Idle 일꾼 유닛들 중에서 Move 임무를 수행할 일꾼 유닛을 정해서 리턴합니다
	public void setMoveWorker(Unit worker, int mineralsNeeded, int gasNeeded, Position p) {
		// set up the pointer
		Unit closestWorker = null;

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 변수 기본값 수정

		double closestDistance = 1000000000;

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		// for each worker we currently have
		for (Unit unit : workerData.getWorkers()) {
			if (unit == null)
				continue;

			// only consider it if it's a mineral worker or idle worker
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerJob.MINERALS || workerData.getWorkerJob(unit) == WorkerJob.IDLE)) {
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(p);
				if (closestWorker == null || distance < closestDistance) {
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		if (closestWorker != null) {
			workerData.setWorkerJob(closestWorker, WorkerJob.MOVE, new WorkerMoveData(mineralsNeeded, gasNeeded, p));
		} else {
			// MyBotModule.Broodwar.printf("Error, no worker found");
		}
	}

	/// 해당 일꾼 유닛에게 Combat 임무를 부여합니다
	public void setCombatWorker(Unit worker) {
		if (worker == null)
			return;

		workerData.setWorkerJob(worker, WorkerJob.COMBAT, (Unit) null);
	}

	public void setRepairWorker(Unit worker, Unit unitToRepair) {
		workerData.setWorkerJob(worker, WorkerJob.REPAIR, unitToRepair);
	}

	public void stopRepairing(Unit worker) {
		workerData.setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
	}

	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트합니다
	public void onUnitMorph(Unit unit) {
		if (unit == null)
			return;

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 일꾼 탄생/파괴 등에 대한 업데이트 로직 버그 수정

		// onUnitComplete 에서 처리하도록 수정
		// if something morphs into a worker, add it
		// if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getHitPoints() >= 0)
		// {
		// workerData.addWorker(unit);
		// }

		// if something morphs into a building, it was a worker (Zerg Drone)
		if (unit.getType().isBuilding() && unit.getPlayer() == Prebot.Game.self() && unit.getPlayer().getRace() == Race.Zerg) {
			// 해당 worker 를 workerData 에서 삭제한다
			workerData.workerDestroyed(unit);
			rebalanceWorkers();
		}

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	}

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 일꾼 탄생/파괴 등에 대한 업데이트 로직 버그 수정 : onUnitShow 가 아니라 onUnitComplete 에서 처리하도록 수정

	// onUnitShow 메소드 제거
	/*
	 * // 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트합니다 public void onUnitShow(Unit unit) { if (unit == null) return;
	 * 
	 * // add the depot if it exists if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self()) { workerData.addDepot(unit); }
	 * 
	 * // add the worker if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getHitPoints() >= 0) { workerData.addWorker(unit); }
	 * 
	 * if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self()) { rebalanceWorkers(); }
	 * 
	 * }
	 */

	// onUnitComplete 메소드 추가

	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트합니다
	/// Terran_SCV, Protoss_Probe 유닛 훈련이 끝나서 탄생할 경우,
	/// Zerg_Drone 유닛이 탄생하는 경우,
	/// Zerg_Drone 유닛이 건물로 Morph 가 끝나서 건물이 완성되는 경우,
	/// Zerg_Drone 유닛의 Zerg_Extractor 건물로의 Morph 를 취소시켜서 Zerg_Drone 유닛이 새롭게 탄생하는 경우
	/// 호출됩니다
	public void onUnitComplete(Unit unit) {
		if (unit == null)
			return;

		// ResourceDepot 건물이 신규 생성되면, 자료구조 추가 처리를 한 후, rebalanceWorkers 를 한다
		if (unit.getType().isResourceDepot() && unit.getPlayer() == Prebot.Game.self()) {
			workerData.addDepot(unit);
			rebalanceWorkers();
		}

		// 일꾼이 신규 생성되면, 자료구조 추가 처리를 한다.
		if (unit.getType().isWorker() && unit.getPlayer() == Prebot.Game.self() && unit.getHitPoints() >= 0) {
			workerData.addWorker(unit);
			rebalanceWorkers();
		}
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	// 일하고있는 resource depot 에 충분한 수의 mineral worker 들이 지정되어 있다면, idle 상태로 만든다
	// idle worker 에게 mineral job 을 부여할 때, mineral worker 가 부족한 resource depot 으로 이동하게 된다
	public void rebalanceWorkers() {
		for (Unit worker : workerData.getWorkers()) {
			if (workerData.getWorkerJob(worker) != WorkerJob.MINERALS) {
				continue;
			}

			Unit depot = workerData.getWorkerDepot(worker);

			if (depot != null && workerData.depotHasEnoughMineralWorkers(depot)) {
				workerData.setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
			} else if (depot == null) {
				workerData.setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
			}
		}
	}

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 일꾼 탄생/파괴 등에 대한 업데이트 로직 버그 수정 및 멀티 기지간 일꾼 숫자 리밸런싱이 잘 일어나도록 수정

	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트합니다
	public void onUnitDestroy(Unit unit) {
		if (unit == null)
			return;

		// ResourceDepot 건물이 파괴되면, 자료구조 삭제 처리를 한 후, 일꾼들을 Idle 상태로 만들어 rebalanceWorkers 한 효과가 나게 한다
		if (unit.getType().isResourceDepot() && unit.getPlayer() == Prebot.Game.self()) {
			workerData.removeDepot(unit);
		}

		// 일꾼이 죽으면, 자료구조 삭제 처리를 한 후, rebalanceWorkers 를 한다
		if (unit.getType().isWorker() && unit.getPlayer() == Prebot.Game.self()) {
			workerData.workerDestroyed(unit);
			rebalanceWorkers();
		}

		// 미네랄을 다 채취하면 rebalanceWorkers를 한다
		if (unit.getType() == UnitType.Resource_Mineral_Field) {
			rebalanceWorkers();
		}
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	public boolean isMineralWorker(Unit worker) {
		if (worker == null)
			return false;

		return workerData.getWorkerJob(worker) == WorkerJob.MINERALS || workerData.getWorkerJob(worker) == WorkerJob.IDLE;
	}

	public boolean isConstructionWorker(Unit worker) {
		if (worker == null)
			return false;

		return (workerData.getWorkerJob(worker) == WorkerJob.BUILD);
	}

	public int getNumMineralWorkers() {
		return workerData.getNumMineralWorkers();
	}

	/// idle 상태인 일꾼 유닛 unit 의 숫자를 리턴합니다
	public int getNumIdleWorkers() {
		return workerData.getNumIdleWorkers();
	}

	public int getNumGasWorkers() {
		return workerData.getNumGasWorkers();
	}

	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 리턴합니다
	public WorkerData getWorkerData() {
		return workerData;
	}
}