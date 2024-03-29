

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

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
	
	public enum SCVSTATE{
		MovingToMineral,
		GatheringMineral,
		ReturningMineral,
		ExtraMoveToMineral, //used for the path trick
		ExtraMoveToPos,
		NoTrick //used for the path trick
	};
	
	/// 미네랄 숫자 대비 미네랄 일꾼 숫자의 적정 비율
	double mineralAndMineralWorkerRatio;						
	
	/// 일꾼 목록
	public ArrayList<Unit> workers = new ArrayList<Unit>();
	/// ResourceDepot 목록
	private ArrayList<Unit> depots = new ArrayList<Unit>();
	
	//일꾼 유닛과 임무 관계
	private Map<Integer, WorkerJob> workerJobMap = new HashMap<Integer, WorkerJob>();
	//일꾼과 건설일꾼 배정
	private Map<Integer, UnitType> workerBuildingTypeMap = new HashMap<Integer, UnitType>();
	//CC에 배정된 일꾼 수
	public static Map<Integer, Integer> depotWorkerCount = new HashMap<Integer, Integer>();
	//Gas 에 배정된 일꾼 수
	public Map<Integer, Integer> refineryWorkerCount = new HashMap<Integer, Integer>();
	//작업중인 광물 ????
	private Map<Integer, Unit> workerDepotMap = new HashMap<Integer, Unit>();
	//이동중인 일꾼과 목적지
	private Map<Integer, WorkerMoveData> workerMoveMap = new HashMap<Integer, WorkerMoveData>();
	//일꾼과 미네랄 간의 배정 관계
	public Map<Integer, Minerals> workerMineralAssignment = new HashMap<Integer, Minerals>();
	//미네랄에 배정된 일꾼의 수
	public Map<Integer, Integer> workersOnMineralPatch = new HashMap<Integer, Integer>();
	//미네랄 일꾼
	private Map<Integer, Unit> workerMineralMap = new HashMap<Integer, Unit>();
	//Gas 일꾼 
	public Map<Integer, Unit> workerRefineryMap = new HashMap<Integer, Unit>();
	//수리중인 일꾼 
	public Map<Integer, Unit> workerRepairMap = new HashMap<Integer, Unit>();
	public Map<Integer, Unit> workerWraithRepairMap = new HashMap<Integer, Unit>();
	//수리중인 일꾼 
	//CC에 배정된 미네랄 리스트(미네랄 트릭 위해)
	public static Map<Unit, List<Minerals>> depotMineral = new HashMap<Unit, List<Minerals>>();
	
		
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
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////

		// workers, depotWorkerCount, refineryWorkerCount 등 자료구조에서 사망한 일꾼 정보를 제거합니다
		for (Iterator<Unit> it = workers.iterator(); it.hasNext(); ) {
			Unit worker = it.next();

			if (worker == null ) {			
				workers.remove(worker);
			}
			else {
				if (worker.getType().isWorker() == false || worker.getPlayer() != MyBotModule.Broodwar.self() 
						|| worker.exists() == false || worker.getHitPoints() == 0) {
									
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
					if (workerWraithRepairMap.containsKey(worker.getID())) {
						workerWraithRepairMap.remove(worker.getID());
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
						for (Map.Entry<Integer, Unit> entry : workerDepotMap.entrySet())
						{
							if (entry.getValue().getID() == depot.getID() ) {
								count++;
							}
						}
						depotWorkerCount.put(depot.getID(), count);
					}

					// refineryWorkerCount 를 다시 셉니다
					for (Map.Entry<Integer, Integer> entry1 : refineryWorkerCount.entrySet()) {
						int refineryID = entry1.getKey();
						int count = 0;
						for (Map.Entry<Integer, Unit> entry2 : workerRefineryMap.entrySet())
						{
							if (refineryID == entry2.getValue().getID() ) {
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
		
		if (unit == null) { return; }

		clearPreviousJob(unit);
		workers.remove(unit); // C++ : workers.erase(unit);*/
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

	public void addToMineralPatch(Minerals mineral, int num) {
		if (mineral == null) {
			return;
		}

		if (!workersOnMineralPatch.containsKey(mineral.unitId)) {
			workersOnMineralPatch.put(mineral.unitId, num);
		} else {
			workersOnMineralPatch.put(mineral.unitId, workersOnMineralPatch.get(mineral.unitId) + num);
		}
	}

	public void setWorkerJob(Unit unit, WorkerJob job, Unit jobUnit)
	{
		if (unit == null) { return; }
		
		clearPreviousJob(unit);
		workerJobMap.put(unit.getID(), job);
		if (job == WorkerJob.Minerals)
		{
			/*if(unit.isGatheringMinerals()){
				return;
			}*/
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

			Minerals closMin = getMineralToMine(unit);
			if (closMin != null) {
		        if(closMin.mineralTrick != null ){
		        	unit.gather(closMin.mineralTrick);
		        	workerMineralAssignment.put(unit.getID(), closMin);
		        	addToMineralPatch(closMin, 1);
				 } else if( closMin.posTrick != bwapi.Position.None ){
					 CommandUtils.rightClick(unit, closMin.posTrick );
		           	 workerMineralAssignment.put(unit.getID(), closMin);
					 addToMineralPatch(closMin, 1);

				 } else {
					//CommandUtils.rightClick(unit, closMin.miner);
					unit.gather(closMin.mineralUnit);
					workerMineralAssignment.put(unit.getID(), closMin);
					addToMineralPatch(closMin, 1);
				 }
			}

			//Mineral.Instance().Minerals.get(closMin).SCVcount++;
	        //CommandUtils.rightClick(unit, mineralToMine);
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
			CommandUtils.rightClick(unit, jobUnit);
		}
	    else if (job == WorkerJob.Repair)
	    {
	        // only SCV can repair
			if (unit.getType() == UnitType.Terran_SCV) {
				// set the unit the worker is to repair
				// 기존이 이미 수리를 하고 있으면 계속 기존 것을 수리한다
				if (!unit.isRepairing())
				{
					if(jobUnit.getType() == UnitType.Terran_Wraith){
						workerWraithRepairMap.put(unit.getID(), jobUnit);
					}else{
						workerRepairMap.put(unit.getID(), jobUnit);
					}
					
					CommandUtils.repair(unit, jobUnit);
				}
				
				// start repairing if it is not repairing 
				
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

	public void clearPreviousJob(Unit unit) { 
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
			if(workerRepairMap.containsKey(unit.getID())){
				workerRepairMap.remove(unit.getID()); // C++ : workerRepairMap.erase(unit);
			}
			if(workerWraithRepairMap.containsKey(unit.getID())){
				workerWraithRepairMap.remove(unit.getID()); // C++ : workerRepairMap.erase(unit);
			}
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
	
	//워커는 Build 명령 취소되었지만 ConstructionManager 에서 build 명령취소 되지 않은것 비교하기 위한 함수
	public boolean getWorkerId(Unit unit)
	{
		if (unit == null)
		{
			return false;
		}
		if(workerJobMap.get(unit.getID()) !=  WorkerData.WorkerJob.Build)
		{
			return true;
		}
		return false;
	}
	//1.1 추가 함수
	
	public boolean depotHasEnoughMineralWorkers(Unit depot)
	{
		if (depot == null) { return false; }

		int assignedWorkers = getNumAssignedWorkers(depot);
		int mineralsNearDepot = getMineralsNearDepot(depot);

		// 충분한 수의 미네랄 일꾼 수를 얼마로 정할 것인가 : 
		// (근처 미네랄 수) * 1.5배 ~ 2배 정도가 적당
		// 근처 미네랄 수가 8개 라면, 일꾼 8마리여도 좋지만, 12마리면 조금 더 채취가 빠르다. 16마리면 충분하다. 24마리면 너무 많은 숫자이다.
		// 근처 미네랄 수가 0개 인 ResourceDepot은, 이미 충분한 수의 미네랄 일꾼이 꽉 차있는 것이다
		Position basePosition = InfoUtils.myBase().getPosition();
		Region baseRegion = BWTA.getRegion(basePosition);
		Region depotRegion = BWTA.getRegion(depot.getPosition());
		// 멀티 기지간 일꾼 숫자 리밸런싱 조건값 수정 : 미네랄 갯수 * 2 배 초과일 경우 리밸런싱
		if (depotRegion == baseRegion) {
			mineralAndMineralWorkerRatio = 1.5;
		}else{
			mineralAndMineralWorkerRatio = 2;
		}
		if (assignedWorkers >= (int)(mineralsNearDepot * mineralAndMineralWorkerRatio))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public List<Minerals> getMineralPatchesNearDepot(Unit depot)
	{
	    // if there are minerals near the depot, add them to the set
		//List<Unit> mineralsNearDepot = new ArrayList<Unit>();
		
		/*
		 * 1.3 초기 일꾼 한마리 노는거 방지 TF 가 알려준 소스 반영 getAllUnits -> getMinerals
		 * */
	    int radius = 320;
	    int c = 0;
	    ArrayList<Minerals> mineralList = new ArrayList<Minerals>();
	    for (Unit unit : MyBotModule.Broodwar.getMinerals())
		{
			if (unit.getType() == UnitType.Resource_Mineral_Field && unit.getDistance(depot) < radius)
			{
	            //mineralsNearDepot.add(unit);
				Minerals newMineral = new Minerals();
				newMineral.unitId = unit.getID();
				newMineral.mineralUnit = unit;
				mineralList.add(newMineral);
				c++;
			}
		}

	    if (mineralList.size() == 0)
	    {
	    	mineralList = new ArrayList<Minerals>();
	    	for (Unit unit : MyBotModule.Broodwar.getMinerals()) {
	        	/*if(unit.getDistance(enemyBaseLocation) < radius)
	        		continue;*/
			    if ((unit.getType() == UnitType.Resource_Mineral_Field))
			    {
			    	Minerals newMineral = new Minerals();
					newMineral.unitId = unit.getID();
					newMineral.mineralUnit = unit;
					mineralList.add(newMineral);
			    }
		    }
	    	//depotMineral.put(depot, mineralList);
	    }
	    return mineralList;
	}

	/// ResourceDepot 반경 200 point 이내의 미네랄 덩이 수를 반환합니다
	public int getMineralsNearDepot(Unit depot)
	{
		if (depot == null) { return 0; }

		int mineralsNearDepot = 0;

		for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 320)
			{
				mineralsNearDepot++;
			}
		}

		return mineralsNearDepot;
	}
	
	/// ResourceDepot 반경 200 point 이내의 미네랄 덩이 수를 반환합니다
	public int getMineralsSumNearDepot(Unit depot)
	{
		if (depot == null) { return 0; }

		int mineralsNearDepot = 0;

		for (Unit unit : MyBotModule.Broodwar.getMinerals())
		{
			if (unit.getDistance(depot) < 320)
			{
				mineralsNearDepot += unit.getResources();
			}
		}

		return mineralsNearDepot;
	}
	
	public Unit getGasNearDepot(BaseLocation base) {
		if (base == null) {
			return null;
		}

		for (Unit geyser : MyBotModule.Broodwar.getGeysers()) {
			if (geyser.getDistance(base) < 320) {
				return geyser;
			}
		}
		return null;
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


	public Minerals getMineralToMine(Unit worker)
	{
		// get the depot associated with this unit
		Unit depot = getWorkerDepot(worker);
		if (depot == null) {
			return null;
		}
		Minerals bestMineral = null;
		double bestDist = 100000000;
	    double bestNumAssigned = 10000000;
	    int workerCnt = depotWorkerCount.get(depot.getID());
	    //아직 cc와 미네랄간의 배정이 안끝났을경우 ex) 본진에서 cc짓고 멀티 보낸다던지 null값 나오므로 
	    if(depotMineral.get(depot) == null){
	    	return null;
	    }
	    
	    int minCnt = depotMineral.get(depot).size();
        if( workerCnt > minCnt){
        	bestDist = 0;
        }
		
        for (Minerals minr : depotMineral.get(depot)) {
			double dist = minr.mineralUnit.getDistance(depot);
			double numAssigned = workersOnMineralPatch.get(minr.unitId);

			if (workerCnt <= minCnt) {
				if (numAssigned < bestNumAssigned) {
					bestMineral = minr;
					bestDist = dist;
					bestNumAssigned = numAssigned;
				} else if (numAssigned == bestNumAssigned) {
					if (dist < bestDist) {
						bestMineral = minr;
						bestDist = dist;
						bestNumAssigned = numAssigned;
					}
				}
			} else {
				if (numAssigned < bestNumAssigned) {
					bestMineral = minr;
					bestDist = dist;
					bestNumAssigned = numAssigned;
				} else if (numAssigned == bestNumAssigned) {
					if (dist > bestDist) {
						bestMineral = minr;
						bestDist = dist;
						bestNumAssigned = numAssigned;
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
		}else if(workerWraithRepairMap.containsKey(unit.getID())){
			return workerWraithRepairMap.get(unit.getID());
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