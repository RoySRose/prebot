package pre;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Unitset;
import bwta.BaseLocation;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.CommandUtil;

public class WorkerData {

	
	/// 일꾼 유닛에게 지정하는 임무의 종류
	public  enum WorkerJob { 
		Minerals, 		///< 미네랄 채취 
		Gas,			///< 가스 채취
		Build,			///< 건물 건설
		Combat, 		///< 전투
		Idle,			///< 하는 일 없음. 대기 상태. 
		Repair,			///< 수리. Terran_SCV 만 가능
		Move,			///< 이동
		Scout, 			///< 정찰. Move와 다름. Mineral / Gas / Build 등의 다른 임무로 차출되지 않게 됨. 
		Default, 		///< 기본. 미설정 상태.
		NongBongM,	///< 농봉 미네랄 붙어있는 상태
		NongBongG,	///< 농봉 가스 붙어있는 상태
		NongBongGS,	///< 농봉 가스(shift) 붙어있는 상태
		NongBongAT	///< 농봉 공격중 상태
	};
	
	/// 일꾼 목록
	private List<Unit> workers = new ArrayList<Unit>();
	/// ResourceDepot 목록
	private List<Unit> depots = new ArrayList<Unit>();
	
	//일꾼 유닛과 임무 관계
	private Map<Integer, WorkerJob> workerJobMap = new HashMap<Integer, WorkerJob>();
	//일꾼과 건설일꾼 배정
	private Map<Integer, UnitType> workerBuildingTypeMap = new HashMap<Integer, UnitType>();
	//CC에 배정된 일꾼 수
	public Map<Integer, Integer> depotWorkerCount = new HashMap<Integer, Integer>();
	//Gas 에 배정된 일꾼 수
	public Map<Integer, Integer> refineryWorkerCount = new HashMap<Integer, Integer>();
	//작업중인 광물 ????
	private Map<Integer, Unit> workerDepotMap = new HashMap<Integer, Unit>();
	//이동중인 일꾼과 목적지
	private Map<Integer, WorkerMoveData> workerMoveMap = new HashMap<Integer, WorkerMoveData>();
	//일꾼과 미네랄 간의 배정 관계
	public Map<Integer, Unit> workerMineralAssignment = new HashMap<Integer, Unit>();
	//미네랄에 배정된 일꾼의 수
	public Map<Integer, Integer> workersOnMineralPatch = new HashMap<Integer, Integer>();
	//미네랄 일꾼
	private Map<Integer, Unit> workerMineralMap = new HashMap<Integer, Unit>();
	//Gas 일꾼 
	private Map<Integer, Unit> workerRefineryMap = new HashMap<Integer, Unit>();
	//수리중인 일꾼 
	private Map<Integer, Unit> workerRepairMap = new HashMap<Integer, Unit>();
	
	private CommandUtil commandUtil = new CommandUtil();
	
	public WorkerData() 
	{
	     for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field))
			{
	            workersOnMineralPatch.put(unit.getID(), 0);
			}
		}
	}

	public final List<Unit> getWorkers()
	{
		return workers;
	}
	
	public void workerDestroyed(Unit unit)
	{
		if (unit == null) { return; }

		clearPreviousJob(unit);
		workers.remove(unit); // C++ : workers.erase(unit);
	}

	// WorkerJob::Idle 로 일단 추가한다
	public void addWorker(Unit unit)
	{
		if (unit ==  null) { return; }

		workers.add(unit); // C++ : workers.insert(unit);
		workerJobMap.put(unit.getID(), WorkerJob.Idle);
	}

	public void addWorker(Unit unit, WorkerJob job, Unit jobUnit)
	{
		if (unit == null || jobUnit == null) { return; }

//		assert(workers.find(unit) == workers.end());

		workers.add(unit); // C++ : workers.insert(unit);
		setWorkerJob(unit, job, jobUnit);
	}

	public void addWorker(Unit unit, WorkerJob job, UnitType jobUnitType)
	{
		if (unit == null) { return; }

//		assert(workers.find(unit) == workers.end());
		workers.add(unit); // C++ : workers.insert(unit);
		setWorkerJob(unit, job, jobUnitType);
	}

	public void addDepot(Unit unit)
	{
		if (unit == null) { return; }

		boolean flag = true;
		for(int i = 0 ; i < depots.size() ; i++)
		{
			if(depots.get(i).getID() == unit.getID())
			{
				flag = false;
			}
		}
		if(flag)
		{
			depots.add(unit);
			depotWorkerCount.put(unit.getID(), 0);
		}
		// assert(depots.find(unit) == depots.end());
		// C+ : depots.insert(unit);
	}

	public void removeDepot(Unit unit)
	{	
		if (unit == null) { return; }

		depots.remove(unit); // C++ :  depots.erase(unit);
		depotWorkerCount.remove(unit.getID());

		// re-balance workers in here
		for (Unit worker : workers)
		{
			// if a worker was working at this depot
			if (workerDepotMap.get(worker.getID()) == unit)
			{
				setWorkerJob(worker, WorkerJob.Idle, (Unit)null);
			}
		}
	}

	public List<Unit> getDepots()
	{
		return depots;
	}

	public void addToMineralPatch(Unit unit, int num)
	{
		if (unit == null) { return; }

		if (!workersOnMineralPatch.containsKey(unit.getID()))
	    {
	        workersOnMineralPatch.put(unit.getID(), num);
	    }
	    else
	    {
	        workersOnMineralPatch.put(unit.getID(), workersOnMineralPatch.get(unit.getID()) + num);
	    }
	}

	public void setWorkerJob(Unit unit, WorkerJob job, Unit jobUnit)
	{
		if (unit == null) { return; }
		
		/*
		if (job == Idle)
		{
			std::cout << "set worker " << unit.getID() << " job " << workerJobMap[unit] << " . 4 (idle) " << std::endl;
		}
		*/
		
		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);
		if (job == WorkerJob.Minerals)
		{
			// 커멘드 센터에 연결된 일꾼 수 증가
			if(depotWorkerCount.get(jobUnit.getID()) == null)
			{
				depotWorkerCount.put(jobUnit.getID(), 1);
			}
			else
			{
				depotWorkerCount.put(jobUnit.getID(), depotWorkerCount.get(jobUnit.getID()) + 1);
			}

			// set the mineral the worker is working on
			workerDepotMap.put(unit.getID(), jobUnit);

	        Unit mineralToMine = getMineralToMine(unit);
	        workerMineralAssignment.put(unit.getID(), mineralToMine);
	        addToMineralPatch(mineralToMine, 1);
	        
	        commandUtil.rightClick(unit, mineralToMine);
		}
		else if (job == WorkerJob.Gas)
		{
			// increase the count of workers assigned to this refinery
			if(refineryWorkerCount.get(jobUnit.getID()) == null)
			{
				refineryWorkerCount.put(jobUnit.getID(), 1);					
			}
			else 
			{
				refineryWorkerCount.put(jobUnit.getID(), refineryWorkerCount.get(jobUnit.getID()) + 1);			
			}

			// set the refinery the worker is working on
			workerRefineryMap.put(unit.getID(), jobUnit);

			// right click the refinery to start harvesting
			commandUtil.rightClick(unit, jobUnit);
		}
	    else if (job == WorkerJob.Repair)
	    {
	        // only SCV can repair
			if (unit.getType() == UnitType.Terran_SCV) {

				// set the unit the worker is to repair
				workerRepairMap.put(unit.getID(), jobUnit);

				// start repairing if it is not repairing 
				// 기존이 이미 수리를 하고 있으면 계속 기존 것을 수리한다
				if (!unit.isRepairing())
				{
					commandUtil.repair(unit, jobUnit);
				}
			}
	    }
		else if (job == WorkerJob.Scout)
		{
		}
	    else if (job == WorkerJob.Build)
	    {
	    }else if (job == WorkerJob.Move)
	    {
	    	
	    }
	}

	public void setWorkerJob(Unit unit, WorkerJob job, UnitType jobUnitType)
	{
		if (unit == null) { return; }

		//System.out.println("setWorkerJob "+ unit.getID() + " from " + workerJobMap.get(unit.getID()).ordinal() + " to " + job.ordinal());
		
		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);

		if (job == WorkerJob.Build)
		{
			workerBuildingTypeMap.put(unit.getID(), jobUnitType);
		}
	}

	public void setWorkerJob(Unit unit, WorkerJob job, WorkerMoveData wmd)
	{
		if (unit == null) { return; }

		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);

		if (job == WorkerJob.Move)
		{
			workerMoveMap.put(unit.getID(), wmd);
		}

		if (workerJobMap.get(unit.getID()) != WorkerJob.Move)
		{
			//BWAPI::Broodwar->printf("Something went horribly wrong");
		}
	}

	public void clearPreviousJob(Unit unit)
	{
		if (unit == null) { return; }

		WorkerJob previousJob = getWorkerJob(unit);

		if (previousJob == WorkerJob.Minerals)
		{
			//System.out.println("workerDepotMap.get(unit.getID()) : " + workerDepotMap.get(unit.getID()));
			if(workerDepotMap.get(unit.getID()) != null)
			{
				if (depotWorkerCount.get(workerDepotMap.get(unit.getID()).getID()) != null) {
					depotWorkerCount.put(workerDepotMap.get(unit.getID()).getID(), depotWorkerCount.get(workerDepotMap.get(unit.getID()).getID()) - 1);
				}
			}

			workerDepotMap.remove(unit.getID()); // C++ : workerDepotMap.erase(unit);

	        // remove a worker from this unit's assigned mineral patch
	        addToMineralPatch(workerMineralAssignment.get(unit.getID()), -1);

	        // erase the association from the map
	        workerMineralAssignment.remove(unit.getID()); // C++ : workerMineralAssignment.erase(unit);
		}
		else if (previousJob == WorkerJob.Gas)
		{
			refineryWorkerCount.put(workerRefineryMap.get(unit.getID()).getID(), refineryWorkerCount.get(workerRefineryMap.get(unit.getID()).getID()) - 1);
			workerRefineryMap.remove(unit.getID()); // C++ : workerRefineryMap.erase(unit);
		}
		else if (previousJob == WorkerJob.Build)
		{
			workerBuildingTypeMap.remove(unit.getID()); // C++ : workerBuildingTypeMap.erase(unit);
		}
		else if (previousJob == WorkerJob.Repair)
		{
			workerRepairMap.remove(unit.getID()); // C++ : workerRepairMap.erase(unit);
		}
		else if (previousJob == WorkerJob.Move)
		{
		
		}
	}

	public final int getNumWorkers()
	{
		return workers.size();
	}

	public final int getNumMineralWorkers()
	{
		int num = 0;
		for (Unit unit : workers)
		{
			if (workerJobMap.get(unit.getID()) == WorkerData.WorkerJob.Minerals)
			{
				num++;
			}
		}
		return num;
	}
	
	public final int getNumNongBongMWorkers()
	{
		int num = 0;
		for (Unit unit : workers)
		{
			if (workerJobMap.get(unit.getID()) == WorkerData.WorkerJob.NongBongM)
			{
				num++;
			}
		}
		return num;
	}
	
	public final int getNumNongBongGWorkers()
	{
		int num = 0;
		for (Unit unit : workers)
		{
			if (workerJobMap.get(unit.getID()) == WorkerData.WorkerJob.NongBongG)
			{
				num++;
			}
		}
		return num;
	}
	
	public final int getNumNongBongGSWorkers()
	{
		int num = 0;
		for (Unit unit : workers)
		{
			if (workerJobMap.get(unit.getID()) == WorkerData.WorkerJob.NongBongGS)
			{
				num++;
			}
		}
		return num;
	}

	public final int getNumGasWorkers()
	{
		int num = 0;
		for (Unit unit : workers)
		{
			if (workerJobMap.get(unit.getID()) == WorkerData.WorkerJob.Gas)
			{
				num++;
			}
		}
		return num;
	}

	public final int getNumIdleWorkers()
	{
		int num = 0;
		for (Unit unit : workers)
		{
			if (workerJobMap.get(unit.getID()) == WorkerData.WorkerJob.Idle)
			{
				num++;
			}
		}
		return num;
	}

	public WorkerData.WorkerJob getWorkerJob(Unit unit)
	{
		if (unit == null)
		{
			return WorkerJob.Default;
		}
		
		if(workerJobMap.containsKey(unit.getID()))
		{
			return workerJobMap.get(unit.getID());
		}
//		Iterator<Integer> it = workerJobMap.keySet().iterator();
//		while(it.hasNext())
//		{
//			Integer tempUnit = it.next(); 
//			if(tempUnit.getID() == unit.getID())
//			{
//					
//			}
//		}
		return WorkerJob.Default;
	}

	public boolean depotHasEnoughMineralWorkers(Unit depot)
	{
		if (depot == null) { return false; }

		int assignedWorkers = getNumAssignedWorkers(depot);
		int mineralsNearDepot = getMineralsNearDepot(depot);

		if (assignedWorkers >= mineralsNearDepot * 3)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public List<Unit> getMineralPatchesNearDepot(Unit depot)
	{
	    // if there are minerals near the depot, add them to the set
		List<Unit> mineralsNearDepot = new ArrayList<Unit>();
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
	    int radius = 320;
	    for (Unit unit : MyBotModule.Broodwar.getAllUnits())
//	    for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < radius)
			{
	            mineralsNearDepot.add(unit);
			}
		}

	    // if we didn't find any, use the whole map
	    if (mineralsNearDepot.isEmpty())
	    {
	        for (Unit unit : MyBotModule.Broodwar.getAllUnits())
//	    	for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		    {
	        	/*if(unit.getDistance(enemyBaseLocation) < radius)
	        		continue;*/
			    if ((unit.getType() == UnitType.Resource_Mineral_Field))
			    {
			    	if(unit.getDistance(depot) < unit.getDistance(depot))
			    		mineralsNearDepot.add(unit);
			    }
		    }
	    }

	    return mineralsNearDepot;
	}
	

	public int getMineralsNearDepot(Unit depot)
	{
		if (depot == null) { return 0; }

		int mineralsNearDepot = 0;

		for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 200)
			{
				mineralsNearDepot++;
			}
		}

		return mineralsNearDepot;
	}

	public Unit getWorkerResource(Unit unit)
	{
		if (unit == null) { return null; }
		
		// if the worker is mining, set the iterator to the mineral map
		if (getWorkerJob(unit) == WorkerData.WorkerJob.Minerals)
		{
			if (workerMineralMap.containsKey(unit.getID()))
			{
				return workerMineralMap.get(unit.getID());
			}	
		}
		else if (getWorkerJob(unit) == WorkerData.WorkerJob.Gas)
		{
			if (workerRefineryMap.containsKey(unit.getID()))
			{
				return workerRefineryMap.get(unit.getID());
			}	
		}

		return null;
	}


	public Unit getMineralToMine(Unit worker)
	{
		if (worker == null) { return null; }

		// get the depot associated with this unit
		Unit depot = getWorkerDepot(worker);
		Unit bestMineral = null;
		double bestDist = 100000000;
	    double bestNumAssigned = 10000000;
	    int lowSCV = 10;
	    int workerCnt = depotWorkerCount.get(depot.getID());
	    int minCnt = 0;
	    
		if (depot != null)
		{
			//se-min.park
	        List<Unit> mineralPatches = getMineralPatchesNearDepot(depot);
	        minCnt = mineralPatches.size();
	        if( workerCnt > minCnt){
	        	bestDist = 0;
	        }
	        for (Unit mineral : mineralPatches){
	        	lowSCV = workersOnMineralPatch.get(mineral.getID());
	        	//System.out.println("mineralPatches : " + mineralPatches.size());
	        }
			for (Unit mineral : mineralPatches)
			{
					double dist = mineral.getDistance(depot);
	                double numAssigned = workersOnMineralPatch.get(mineral.getID());
	                if( workerCnt <= minCnt){
	                	if (numAssigned < bestNumAssigned)
		                {
		                    bestMineral = mineral;
		                    bestDist = dist;
		                    bestNumAssigned = numAssigned;
		                }
						else if (numAssigned == bestNumAssigned)
						{
							if (dist < bestDist)
		                    {
		                        bestMineral = mineral;
		                        bestDist = dist;
		                        bestNumAssigned = numAssigned;
		                    }
						}
	                }else{
	                	if (numAssigned < bestNumAssigned)
		                {
		                    bestMineral = mineral;
		                    bestDist = dist;
		                    bestNumAssigned = numAssigned;
		                }
						else if (numAssigned == bestNumAssigned)
						{
							if (dist > bestDist)
		                    {
		                        bestMineral = mineral;
		                        bestDist = dist;
		                        bestNumAssigned = numAssigned;
		                    }
						}
	                }
	                
	                
			
			}
		}

		return bestMineral;
	}
	
	/*
	BWAPI::Unit WorkerData::getMineralToMine(BWAPI::Unit worker)
	{
		if (!worker) { return null; }

		// get the depot associated with this unit
		BWAPI::Unit depot = getWorkerDepot(worker);
		BWAPI::Unit mineral = null;
		double closestDist = 10000;

		if (depot)
		{
			BOOST_FOREACH (BWAPI::Unit unit, MyBotModule.Broodwar.getAllUnits())
			{
				if (unit.getType() == BWAPI::UnitTypes::Resource_Mineral_Field && unit.getResources() > 0)
				{
					double dist = unit.getDistance(depot);

					if (!mineral || dist < closestDist)
					{
						mineral = unit;
						closestDist = dist;
					}
				}
			}
		}

		return mineral;
	}*/

	public Unit getWorkerRepairUnit(Unit unit)
	{
		if (unit == null) { return null; }
		
		if(workerRepairMap.containsKey(unit.getID())){
			return workerRepairMap.get(unit.getID());
		}

		return null;
	}

	public Unit getWorkerDepot(Unit unit)
	{
		if (unit == null) { return null; }

		if(workerDepotMap.containsKey(unit.getID())){
			return workerDepotMap.get(unit.getID());
		}
		
		return null;
	}

	public UnitType	getWorkerBuildingType(Unit unit)
	{
		if (unit == null) { return UnitType.None; }

		if(workerBuildingTypeMap.containsKey(unit.getID())){
			return workerBuildingTypeMap.get(unit.getID());
		}

		return UnitType.None;
	}

	public WorkerMoveData getWorkerMoveData(Unit unit)
	{
//		assert(it != workerMoveMap.end());

		return workerMoveMap.get(unit.getID());
	}

	public int getNumAssignedWorkers(Unit unit)
	{
		if (unit == null) { return 0; }
		
		// if the worker is mining, set the iterator to the mineral map
		if (unit.getType().isResourceDepot())
		{
			// if there is an entry, return it
			if (depotWorkerCount.containsKey(unit.getID()))
			{
				return depotWorkerCount.get(unit.getID());
			}
		}
		else if (unit.getType().isRefinery())
		{
			// if there is an entry, return it
			if (refineryWorkerCount.containsKey(unit.getID()))
			{
				return refineryWorkerCount.get(unit.getID());
			}
			// otherwise, we are only calling this on completed refineries, so set it
			else
			{
				refineryWorkerCount.put(unit.getID(), 0);
			}
		}

		// when all else fails, return 0
		return 0;
	}

	public char getJobCode(Unit unit)
	{
		if (unit == null) { return 'X'; }

		WorkerData.WorkerJob j = getWorkerJob(unit);

		if (j == WorkerData.WorkerJob.Build) return 'B';
		if (j == WorkerData.WorkerJob.Combat) return 'C';
		if (j == WorkerData.WorkerJob.Default) return 'D';
		if (j == WorkerData.WorkerJob.Gas) return 'G';
		if (j == WorkerData.WorkerJob.Idle) return 'I';
		if (j == WorkerData.WorkerJob.Minerals) return 'M';
		if (j == WorkerData.WorkerJob.Repair) return 'R';
		if (j == WorkerData.WorkerJob.Move) return 'O';
		if (j == WorkerData.WorkerJob.Scout) return 'S';
		return 'X';
	}
}