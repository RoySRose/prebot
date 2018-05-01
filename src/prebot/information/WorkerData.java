package prebot.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.Code.WorkerJob;
import prebot.common.util.CommandUtils;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;

public class WorkerData {

	private Map<Integer, Integer> workersOnMineralPatch = new HashMap<Integer, Integer>();

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 멀티 기지간 일꾼 숫자 리밸런싱 조건값 수정 : 미네랄 갯수 * 2 배 초과일 경우 리밸런싱

	/// 미네랄 숫자 대비 미네랄 일꾼 숫자의 적정 비율
	double mineralAndMineralWorkerRatio;

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	/// 일꾼 목록
	private ArrayList<Unit> workers = new ArrayList<Unit>();
	/// ResourceDepot 목록
	private ArrayList<Unit> depots = new ArrayList<Unit>();

	private Map<Integer, WorkerJob> workerJobMap = new HashMap<Integer, WorkerJob>();
	private Map<Integer, UnitType> workerBuildingTypeMap = new HashMap<Integer, UnitType>();
	private Map<Integer, Integer> depotWorkerCount = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> refineryWorkerCount = new HashMap<Integer, Integer>();
	private Map<Integer, Unit> workerDepotMap = new HashMap<Integer, Unit>();
	private Map<Integer, WorkerMoveData> workerMoveMap = new HashMap<Integer, WorkerMoveData>();
	private Map<Integer, Unit> workerMineralAssignment = new HashMap<Integer, Unit>();
	private Map<Integer, Unit> workerMineralMap = new HashMap<Integer, Unit>();
	private Map<Integer, Unit> workerRefineryMap = new HashMap<Integer, Unit>();
	private Map<Integer, Unit> workerRepairMap = new HashMap<Integer, Unit>();

	public WorkerData() {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 멀티 기지간 일꾼 숫자 리밸런싱 조건값 수정 : 미네랄 갯수 * 2 배 초과일 경우 리밸런싱

		mineralAndMineralWorkerRatio = 2;

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		for (Unit unit : Prebot.Game.getAllUnits()) {
			if ((unit.getType() == UnitType.Resource_Mineral_Field)) {
				workersOnMineralPatch.put(unit.getID(), 0);
			}
		}
	}

	public final List<Unit> getWorkers() {
		return workers;
	}

	public void workerDestroyed(Unit unit) {

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////

		// workers, depotWorkerCount, refineryWorkerCount 등 자료구조에서 사망한 일꾼 정보를 제거합니다
		for (Iterator<Unit> it = workers.iterator(); it.hasNext();) {
			Unit worker = it.next();

			if (worker == null) {
				workers.remove(worker);
			} else {
				if (worker.getType().isWorker() == false || worker.getPlayer() != Prebot.Game.self() || worker.exists() == false
						|| worker.getHitPoints() == 0) {

					clearPreviousJob(worker);

					// worker의 previousJob 이 잘못 설정되어있는 경우에 대해 정리합니다
					if (workerMineralMap.containsKey(worker.getID())) {
						workerMineralMap.remove(worker.getID());
					}
					if (workerDepotMap.containsKey(worker.getID())) {
						workerDepotMap.remove(worker.getID());
					}
					if (workerRefineryMap.containsKey(worker.getID())) {
						workerRefineryMap.remove(worker.getID());
					}
					if (workerRepairMap.containsKey(worker.getID())) {
						workerRepairMap.remove(worker.getID());
					}
					if (workerMoveMap.containsKey(worker.getID())) {
						workerMoveMap.remove(worker.getID());
					}
					if (workerBuildingTypeMap.containsKey(worker.getID())) {
						workerBuildingTypeMap.remove(worker.getID());
					}
					if (workerMineralAssignment.containsKey(worker.getID())) {
						workerMineralAssignment.remove(worker.getID());
					}
					if (workerJobMap.containsKey(worker.getID())) {
						workerJobMap.remove(worker.getID());
					}

					// depotWorkerCount 를 다시 셉니다
					for (Unit depot : depots) {
						int count = 0;
						for (Map.Entry<Integer, Unit> entry : workerDepotMap.entrySet()) {
							if (entry.getValue().getID() == depot.getID()) {
								count++;
							}
						}
						depotWorkerCount.put(depot.getID(), count);
					}

					// refineryWorkerCount 를 다시 셉니다
					for (Map.Entry<Integer, Integer> entry1 : refineryWorkerCount.entrySet()) {
						int refineryID = entry1.getKey();
						int count = 0;
						for (Map.Entry<Integer, Unit> entry2 : workerRefineryMap.entrySet()) {
							if (refineryID == entry2.getValue().getID()) {
								count++;
							}
						}
						refineryWorkerCount.put(refineryID, count);
					}

					workers.remove(worker);
				}
			}
		}

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		if (unit == null) {
			return;
		}

		clearPreviousJob(unit);
		workers.remove(unit); // C++ : workers.erase(unit);
	}

	// WorkerJob::Idle 로 일단 추가한다
	public void addWorker(Unit unit) {
		if (unit == null) {
			return;
		}

		workers.add(unit); // C++ : workers.insert(unit);
		workerJobMap.put(unit.getID(), WorkerJob.IDLE);
	}

	public void addWorker(Unit unit, WorkerJob job, Unit jobUnit) {
		if (unit == null || jobUnit == null) {
			return;
		}

		// assert(workers.find(unit) == workers.end());

		workers.add(unit); // C++ : workers.insert(unit);
		setWorkerJob(unit, job, jobUnit);
	}

	public void addWorker(Unit unit, WorkerJob job, UnitType jobUnitType) {
		if (unit == null) {
			return;
		}

		// assert(workers.find(unit) == workers.end());
		workers.add(unit); // C++ : workers.insert(unit);
		setWorkerJob(unit, job, jobUnitType);
	}

	public void addDepot(Unit unit) {
		if (unit == null) {
			return;
		}

		boolean flag = true;
		for (int i = 0; i < depots.size(); i++) {
			if (depots.get(i).getID() == unit.getID()) {
				flag = false;
			}
		}
		if (flag) {
			depots.add(unit);
			depotWorkerCount.put(unit.getID(), 0);
		}
		// assert(depots.find(unit) == depots.end());
		// C+ : depots.insert(unit);
	}

	public void removeDepot(Unit unit) {
		if (unit == null) {
			return;
		}

		depots.remove(unit); // C++ : depots.erase(unit);
		depotWorkerCount.remove(unit.getID());

		// re-balance workers in here
		for (Unit worker : workers) {
			// if a worker was working at this depot
			if (workerDepotMap.get(worker.getID()) == unit) {
				setWorkerJob(worker, WorkerJob.IDLE, (Unit) null);
			}
		}
	}

	public List<Unit> getDepots() {
		return depots;
	}

	public void addToMineralPatch(Unit unit, int num) {
		if (unit == null) {
			return;
		}

		if (!workersOnMineralPatch.containsKey(unit.getID())) {
			workersOnMineralPatch.put(unit.getID(), num);
		} else {
			workersOnMineralPatch.put(unit.getID(), workersOnMineralPatch.get(unit.getID()) + num);
		}
	}

	public void setWorkerJob(Unit unit, WorkerJob job, Unit jobUnit) {
		if (unit == null) {
			return;
		}

		/*
		 * if (job == Idle) { std::cout << "set worker " << unit.getID() << " job " << workerJobMap[unit] << " . 4 (idle) " << std::endl; }
		 */

		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);

		if (job == WorkerJob.MINERALS) {
			// increase the number of workers assigned to this nexus
			if (depotWorkerCount.get(jobUnit.getID()) == null) {
				depotWorkerCount.put(jobUnit.getID(), 1);
			} else {
				depotWorkerCount.put(jobUnit.getID(), depotWorkerCount.get(jobUnit.getID()) + 1);
			}

			// set the mineral the worker is working on
			workerDepotMap.put(unit.getID(), jobUnit);

			Unit mineralToMine = getMineralToMine(unit);
			workerMineralAssignment.put(unit.getID(), mineralToMine);
			addToMineralPatch(mineralToMine, 1);

			// right click the mineral to start mining
			CommandUtils.rightClick(unit, mineralToMine);
		} else if (job == WorkerJob.GAS) {
			// increase the count of workers assigned to this refinery
			if (refineryWorkerCount.get(jobUnit.getID()) == null) {
				refineryWorkerCount.put(jobUnit.getID(), 1);
			} else {
				refineryWorkerCount.put(jobUnit.getID(), refineryWorkerCount.get(jobUnit.getID()) + 1);
			}

			// set the refinery the worker is working on
			workerRefineryMap.put(unit.getID(), jobUnit);

			// right click the refinery to start harvesting
			CommandUtils.rightClick(unit, jobUnit);
		} else if (job == WorkerJob.REPAIR) {
			// only SCV can repair
			if (unit.getType() == UnitType.Terran_SCV) {

				// set the unit the worker is to repair
				workerRepairMap.put(unit.getID(), jobUnit);

				// start repairing if it is not repairing
				// 기존이 이미 수리를 하고 있으면 계속 기존 것을 수리한다
				if (!unit.isRepairing()) {
					CommandUtils.repair(unit, jobUnit);
				}
			}
		} else if (job == WorkerJob.COMBAT) {

		} else if (job == WorkerJob.BUILD) {
		}
	}

	public void setWorkerJob(Unit unit, WorkerJob job, UnitType jobUnitType) {
		if (unit == null) {
			return;
		}

		// System.out.println("setWorkerJob "+ unit.getID() + " from " + workerJobMap.get(unit.getID()).ordinal() + " to " + job.ordinal());

		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);

		if (job == WorkerJob.BUILD) {
			workerBuildingTypeMap.put(unit.getID(), jobUnitType);
		}
	}

	public void setWorkerJob(Unit unit, WorkerJob job, WorkerMoveData wmd) {
		if (unit == null) {
			return;
		}

		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);

		if (job == WorkerJob.MOVE) {
			workerMoveMap.put(unit.getID(), wmd);
		}

		if (workerJobMap.get(unit.getID()) != WorkerJob.MOVE) {
			// BWAPI::Broodwar->printf("Something went horribly wrong");
		}
	}

	public void clearPreviousJob(Unit unit) {
		if (unit == null) {
			return;
		}

		WorkerJob previousJob = getWorkerJob(unit);

		if (previousJob == WorkerJob.MINERALS) {
			if (workerDepotMap.get(unit.getID()) != null) {
				if (depotWorkerCount.get(workerDepotMap.get(unit.getID()).getID()) != null) {
					depotWorkerCount.put(workerDepotMap.get(unit.getID()).getID(),
							depotWorkerCount.get(workerDepotMap.get(unit.getID()).getID()) - 1);
				}
			}

			workerDepotMap.remove(unit.getID()); // C++ : workerDepotMap.erase(unit);

			// remove a worker from this unit's assigned mineral patch
			addToMineralPatch(workerMineralAssignment.get(unit.getID()), -1);

			// erase the association from the map
			workerMineralAssignment.remove(unit.getID()); // C++ : workerMineralAssignment.erase(unit);
		} else if (previousJob == WorkerJob.GAS) {
			refineryWorkerCount.put(workerRefineryMap.get(unit.getID()).getID(),
					refineryWorkerCount.get(workerRefineryMap.get(unit.getID()).getID()) - 1);
			workerRefineryMap.remove(unit.getID()); // C++ : workerRefineryMap.erase(unit);
		} else if (previousJob == WorkerJob.BUILD) {
			workerBuildingTypeMap.remove(unit.getID()); // C++ : workerBuildingTypeMap.erase(unit);
		} else if (previousJob == WorkerJob.REPAIR) {
			workerRepairMap.remove(unit.getID()); // C++ : workerRepairMap.erase(unit);
		} else if (previousJob == WorkerJob.MOVE) {
			workerMoveMap.remove(unit.getID()); // C++ : workerMoveMap.erase(unit);
		}

		workerJobMap.remove(unit.getID()); // C++ : workerJobMap.erase(unit);
	}

	public final int getNumWorkers() {
		return workers.size();
	}

	public final int getNumMineralWorkers() {
		int num = 0;
		for (Unit unit : workers) {
			if (workerJobMap.get(unit.getID()) == WorkerJob.MINERALS) {
				num++;
			}
		}
		return num;
	}

	public final int getNumGasWorkers() {
		int num = 0;
		for (Unit unit : workers) {
			if (workerJobMap.get(unit.getID()) == WorkerJob.GAS) {
				num++;
			}
		}
		return num;
	}

	public final int getNumIdleWorkers() {
		int num = 0;
		for (Unit unit : workers) {
			if (workerJobMap.get(unit.getID()) == WorkerJob.IDLE) {
				num++;
			}
		}
		return num;
	}

	public WorkerJob getWorkerJob(Unit unit) {
		if (unit == null) {
			return WorkerJob.DEFAULT;
		}

		if (workerJobMap.containsKey(unit.getID())) {
			return workerJobMap.get(unit.getID());
		}
		// Iterator<Integer> it = workerJobMap.keySet().iterator();
		// while(it.hasNext())
		// {
		// Integer tempUnit = it.next();
		// if(tempUnit.getID() == unit.getID())
		// {
		//
		// }
		// }
		return WorkerJob.DEFAULT;
	}

	/// ResourceDepot 에 충분한 수(미네랄 덩이 수 * mineralAndMineralWorkerRatio ) 의 미네랄 일꾼이 배정되어있는가
	public boolean depotHasEnoughMineralWorkers(Unit depot) {
		if (depot == null) {
			return false;
		}

		int assignedWorkers = getNumAssignedWorkers(depot);
		int mineralsNearDepot = UnitUtils.getNearMineralsCount(depot);

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////

		// 충분한 수의 미네랄 일꾼 수를 얼마로 정할 것인가 :
		// (근처 미네랄 수) * 1.5배 ~ 2배 정도가 적당
		// 근처 미네랄 수가 8개 라면, 일꾼 8마리여도 좋지만, 12마리면 조금 더 채취가 빠르다. 16마리면 충분하다. 24마리면 너무 많은 숫자이다.
		// 근처 미네랄 수가 0개 인 ResourceDepot은, 이미 충분한 수의 미네랄 일꾼이 꽉 차있는 것이다
		if (assignedWorkers >= (int) (mineralsNearDepot * mineralAndMineralWorkerRatio)) {
			return true;
		} else {
			return false;
		}

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	}

	public List<Unit> getMineralPatchesNearDepot(Unit depot) {
		// if there are minerals near the depot, add them to the set
		List<Unit> mineralsNearDepot = new ArrayList<Unit>();

		int radius = 320;

		for (Unit unit : Prebot.Game.getAllUnits()) {
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < radius) {
				mineralsNearDepot.add(unit);
			}
		}

		// if we didn't find any, use the whole map
		if (mineralsNearDepot.isEmpty()) {
			for (Unit unit : Prebot.Game.getAllUnits()) {
				if ((unit.getType() == UnitType.Resource_Mineral_Field)) {
					mineralsNearDepot.add(unit);
				}
			}
		}

		return mineralsNearDepot;
	}

	public Unit getWorkerResource(Unit unit) {
		if (unit == null) {
			return null;
		}

		// if the worker is mining, set the iterator to the mineral map
		if (getWorkerJob(unit) == WorkerJob.MINERALS) {
			if (workerMineralMap.containsKey(unit.getID())) {
				return workerMineralMap.get(unit.getID());
			}
		} else if (getWorkerJob(unit) == WorkerJob.GAS) {
			if (workerRefineryMap.containsKey(unit.getID())) {
				return workerRefineryMap.get(unit.getID());
			}
		}

		return null;
	}

	public Unit getMineralToMine(Unit worker) {
		if (worker == null) {
			return null;
		}

		// get the depot associated with this unit
		Unit depot = getWorkerDepot(worker);
		Unit bestMineral = null;
		double bestDist = 100000000;
		double bestNumAssigned = 10000000;

		if (depot != null) {
			List<Unit> mineralPatches = getMineralPatchesNearDepot(depot);

			for (Unit mineral : mineralPatches) {
				double dist = mineral.getDistance(depot);
				double numAssigned = workersOnMineralPatch.get(mineral.getID());

				if (numAssigned < bestNumAssigned) {
					bestMineral = mineral;
					bestDist = dist;
					bestNumAssigned = numAssigned;
				} else if (numAssigned == bestNumAssigned) {
					if (dist < bestDist) {
						bestMineral = mineral;
						bestDist = dist;
						bestNumAssigned = numAssigned;
					}
				}

			}
		}

		return bestMineral;
	}

	/*
	 * BWAPI::Unit WorkerData::getMineralToMine(BWAPI::Unit worker) { if (!worker) { return null; }
	 * 
	 * // get the depot associated with this unit BWAPI::Unit depot = getWorkerDepot(worker); BWAPI::Unit mineral = null; double closestDist = 10000;
	 * 
	 * if (depot) { BOOST_FOREACH (BWAPI::Unit unit, MyBotModule.Broodwar.getAllUnits()) { if (unit.getType() ==
	 * BWAPI::UnitTypes::Resource_Mineral_Field && unit.getResources() > 0) { double dist = unit.getDistance(depot);
	 * 
	 * if (!mineral || dist < closestDist) { mineral = unit; closestDist = dist; } } } }
	 * 
	 * return mineral; }
	 */

	public Unit getWorkerRepairUnit(Unit unit) {
		if (unit == null) {
			return null;
		}

		if (workerRepairMap.containsKey(unit.getID())) {
			return workerRepairMap.get(unit.getID());
		}

		return null;
	}

	public Unit getWorkerDepot(Unit unit) {
		if (unit == null) {
			return null;
		}

		if (workerDepotMap.containsKey(unit.getID())) {
			return workerDepotMap.get(unit.getID());
		}

		return null;
	}

	public UnitType getWorkerBuildingType(Unit unit) {
		if (unit == null) {
			return UnitType.None;
		}

		if (workerBuildingTypeMap.containsKey(unit.getID())) {
			return workerBuildingTypeMap.get(unit.getID());
		}

		return UnitType.None;
	}

	public WorkerMoveData getWorkerMoveData(Unit unit) {
		// assert(it != workerMoveMap.end());

		return workerMoveMap.get(unit.getID());
	}

	public int getNumAssignedWorkers(Unit unit) {
		if (unit == null) {
			return 0;
		}

		// if the worker is mining, set the iterator to the mineral map
		if (unit.getType().isResourceDepot()) {
			// if there is an entry, return it
			if (depotWorkerCount.containsKey(unit.getID())) {
				return depotWorkerCount.get(unit.getID());
			}
		} else if (unit.getType().isRefinery()) {
			// if there is an entry, return it
			if (refineryWorkerCount.containsKey(unit.getID())) {
				return refineryWorkerCount.get(unit.getID());
			}
			// otherwise, we are only calling this on completed refineries, so set it
			else {
				refineryWorkerCount.put(unit.getID(), 0);
			}
		}

		// when all else fails, return 0
		return 0;
	}

	public char getJobCode(Unit unit) {
		if (unit == null) {
			return 'X';
		}

		WorkerJob j = getWorkerJob(unit);

		if (j == WorkerJob.BUILD)
			return 'B';
		if (j == WorkerJob.COMBAT)
			return 'C';
		if (j == WorkerJob.DEFAULT)
			return 'D';
		if (j == WorkerJob.GAS)
			return 'G';
		if (j == WorkerJob.IDLE)
			return 'I';
		if (j == WorkerJob.MINERALS)
			return 'M';
		if (j == WorkerJob.REPAIR)
			return 'R';
		if (j == WorkerJob.MOVE)
			return 'O';
		return 'X';
	}
}