package prebot.common.util.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionTask;
import prebot.common.main.Prebot;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitData;
import prebot.strategy.UnitInfo;

/// 유닛 리스트를 부하가 걸리지 않도록 관리
public class UnitCache {
	
	private static UnitCache currentCache = new UnitCache();
	
	public static UnitCache getCurrentCache() {
		return currentCache;
	}

	// 아군 UnitType 발견 여부
	private Map<UnitType, Boolean> selfUnitDiscoveredMap = new HashMap<>();
	private Map<UnitType, Boolean> selfCompleteUnitDiscoveredMap = new HashMap<>();

	// 아군 모든 UnitType 리스트
	private List<Unit> selfAllUnitList = new ArrayList<>(); /// 아군 모든 유닛 리스트
	private List<Unit> selfCompleteUnitList = new ArrayList<>(); /// 아군 완성 유닛 리스트
	private List<Unit> selfIncompleteUnitList = new ArrayList<>(); /// 아군 미완성 유닛 리스트
	private List<ConstructionTask> selfConstructionTaskList = new ArrayList<>(); /// 아군 건설큐의 건물 리스트

	// 아군 UnitType별 맵
	private Map<UnitType, List<Unit>> selfAllUnitMap = new HashMap<>(); /// 아군 모든 유닛
	private Map<UnitType, List<Unit>> selfCompleteUnitMap = new HashMap<>(); /// 아군 완성 유닛
	private Map<UnitType, List<Unit>> selfIncompleteUnitMap = new HashMap<>(); /// 아군 미완성 유닛
	private Map<UnitType, List<ConstructionTask>> selfConstructionTaskMap = new HashMap<>(); /// 아군 건설큐의 건물
	

	// 적군  UnitType 발견 여부
	private Map<UnitType, Boolean> enemyUnitDiscoveredMap = new HashMap<>();
	private Map<UnitType, Boolean> enemyCompleteUnitDiscoveredMap = new HashMap<>();

	// 적군 모든 UnitType 리스트
	private List<UnitInfo> enemyAllUnitInfoList = new ArrayList<>(); /// 적군 모든 유닛정보 리스트
	private List<UnitInfo> enemyVisibileUnitInfoList = new ArrayList<>(); /// 보이는 적군 유닛정보 리스트
	private List<UnitInfo> enemyInvisibleUnitInfoList = new ArrayList<>(); /// 안보이는 적군 유닛정보 리스트
	
	// 적군 UnitType별 맵
	private Map<UnitType, List<UnitInfo>> enemyAllUnitInfoMap = new HashMap<>(); /// 적군 모든 유닛정보
	private Map<UnitType, List<UnitInfo>> enemyVisibleUnitInfoMap = new HashMap<>(); /// 보이는 적군 유닛정보
	private Map<UnitType, List<UnitInfo>> enemyInvisibleUnitInfoMap = new HashMap<>(); /// 안보이는 적군 유닛정보

	////////////////////// 아군 유닛 리스트 관련

	public Map<UnitType, Boolean> getSelfUnitDiscoveredMap() {
		return selfUnitDiscoveredMap;
	}
	
	public Map<UnitType, Boolean> getSelfCompleteUnitDiscoveredMap() {
		return selfCompleteUnitDiscoveredMap;
	}

	public int allCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfAllUnitList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(selfAllUnitMap, unitType).size();
		}
	}

	public List<Unit> allUnits(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfAllUnitList;
		} else {
			return getUnitListReturnEmptyListIfNull(selfAllUnitMap, unitType);
		}
	}

	public int completeCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfCompleteUnitList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(selfCompleteUnitMap, unitType).size();
		}
	}

	public List<Unit> completeUnits(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfCompleteUnitList;
		} else {
			return getUnitListReturnEmptyListIfNull(selfCompleteUnitMap, unitType);
		}
	}

	public int incompleteCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfIncompleteUnitList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(selfIncompleteUnitMap, unitType).size();
		}
	}

	public List<Unit> incompleteUnits(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfIncompleteUnitList;
		} else {
			return getUnitListReturnEmptyListIfNull(selfIncompleteUnitMap, unitType);
		}
	}

	public int underConstructionCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return selfConstructionTaskList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(selfConstructionTaskMap, unitType).size();
		}
	}

	// 실제 construction queue의 사이즈보다 유닛리스트의 수가 적을 수 있다.(건설시작 전인 빌딩이 있을수 있으므로)
	public List<Unit> underConstructionUnits(UnitType unitType) {
		List<Unit> unitList = new ArrayList<>();
		List<ConstructionTask> tasks = new ArrayList<>();
		if (unitType == UnitType.AllUnits) {
			tasks = selfConstructionTaskList;
		} else {
			tasks = selfConstructionTaskMap.get(unitType);
		}
		
		for (ConstructionTask task : tasks) {
			if (task.getBuildingUnit() != null) {
				unitList.add(task.getBuildingUnit());
			}
		}
		return unitList;
	}

	////////////////////// 적군 유닛정보 리스트 관련

	public Map<UnitType, Boolean> getEnemyUnitDiscoveredMap() {
		return enemyUnitDiscoveredMap;
	}
	
	public Map<UnitType, Boolean> getEnemyCompleteUnitDiscoveredMap() {
		return enemyCompleteUnitDiscoveredMap;
	}

	public int enemyAllCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return enemyAllUnitInfoList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(enemyAllUnitInfoMap, unitType).size();
		}
	}

	public List<UnitInfo> enemyAllUnitInfos(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return enemyAllUnitInfoList;
		} else {
			return getUnitListReturnEmptyListIfNull(enemyAllUnitInfoMap, unitType);
		}
	}

	public int enemyVisibleCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return enemyVisibileUnitInfoList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(enemyVisibleUnitInfoMap, unitType).size();
		}
	}

	public List<UnitInfo> enemyVisibleUnitInfos(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return enemyVisibileUnitInfoList;
		} else {
			return getUnitListReturnEmptyListIfNull(enemyVisibleUnitInfoMap, unitType);
		}
	}

	public int enemyInvisibleCount(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return enemyInvisibleUnitInfoList.size();
		} else {
			return getUnitListReturnEmptyListIfNull(enemyInvisibleUnitInfoMap, unitType).size();
		}
	}

	public List<UnitInfo> enemyInvisibleUnitInfos(UnitType unitType) {
		if (unitType == UnitType.AllUnits) {
			return enemyInvisibleUnitInfoList;
		} else {
			return getUnitListReturnEmptyListIfNull(enemyInvisibleUnitInfoMap, unitType);
		}
	}

	////////////////////// 캐시 업데이트

	public void updateCache() {
		claerAll();
		putSelfData();
		putEnemyData();
		updateDiscoveredMap();
	}

	private void claerAll() {
		selfAllUnitList.clear();
		selfCompleteUnitList.clear();
		selfIncompleteUnitList.clear();
		selfConstructionTaskList.clear();
		
		selfAllUnitMap.clear();
		selfCompleteUnitMap.clear();
		selfIncompleteUnitMap.clear();
		selfConstructionTaskMap.clear();

		enemyAllUnitInfoList.clear();
		enemyVisibileUnitInfoList.clear();
		enemyInvisibleUnitInfoList.clear();

		enemyAllUnitInfoMap.clear();
		enemyVisibleUnitInfoMap.clear();
		enemyInvisibleUnitInfoMap.clear();
	}

	private void putSelfData() {
		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
			if (unit == null) {
				continue;
			}
			
			List<Unit> allUnitList = getUnitListReturnEmptyListIfNull(selfAllUnitMap, unit.getType());
			List<Unit> completeUnitList = getUnitListReturnEmptyListIfNull(selfCompleteUnitMap, unit.getType());
			List<Unit> incompleteUnitList = getUnitListReturnEmptyListIfNull(selfIncompleteUnitMap, unit.getType());

			allUnitList.add(unit);
			selfAllUnitList.add(unit);
			if (unit.isCompleted()) {
				completeUnitList.add(unit);
				selfCompleteUnitList.add(unit);
			} else {
				incompleteUnitList.add(unit);
				selfIncompleteUnitList.add(unit);
			}
			
			selfAllUnitMap.put(unit.getType(), allUnitList);
			selfCompleteUnitMap.put(unit.getType(), completeUnitList);
			selfIncompleteUnitMap.put(unit.getType(), incompleteUnitList);
		}

		Vector<ConstructionTask> constructionTaskvector = ConstructionManager.Instance().getConstructionQueue();
		for (ConstructionTask constructionTask : constructionTaskvector) {
			List<ConstructionTask> constructionTaskList = getUnitListReturnEmptyListIfNull(selfConstructionTaskMap, constructionTask.getType());
			selfConstructionTaskList.add(constructionTask);
			constructionTaskList.add(constructionTask);
			selfConstructionTaskMap.put(constructionTask.getType(), constructionTaskList);
		}
	}

	private void putEnemyData() {
		UnitData enemyUnitData = InformationManager.Instance().getUnitData(Prebot.Broodwar.enemy());
		for (int unitId : enemyUnitData.getUnitAndUnitInfoMap().keySet()) {
			UnitInfo unitInfo = enemyUnitData.getUnitAndUnitInfoMap().get(unitId);

			List<UnitInfo> allUnitList = getUnitListReturnEmptyListIfNull(enemyAllUnitInfoMap, unitInfo.getType());
			List<UnitInfo> visibleUnitList = getUnitListReturnEmptyListIfNull(enemyVisibleUnitInfoMap, unitInfo.getType());
			List<UnitInfo> invisibleUnitList = getUnitListReturnEmptyListIfNull(enemyInvisibleUnitInfoMap, unitInfo.getType());

			allUnitList.add(unitInfo);
			enemyAllUnitInfoList.add(unitInfo);
			Unit enemy = Prebot.Broodwar.getUnit(unitInfo.getUnitID());
			if (enemy != null && enemy.getType() != UnitType.Unknown) {
				visibleUnitList.add(unitInfo);
				enemyVisibileUnitInfoList.add(unitInfo);
			} else {
				invisibleUnitList.add(unitInfo);
				enemyInvisibleUnitInfoList.add(unitInfo);
			}

			enemyAllUnitInfoMap.put(unitInfo.getType(), allUnitList);
			enemyVisibleUnitInfoMap.put(unitInfo.getType(), visibleUnitList);
			enemyInvisibleUnitInfoMap.put(unitInfo.getType(), invisibleUnitList);
		}
	}

	private void updateDiscoveredMap() {
		for (Unit selfUnit : Prebot.Broodwar.self().getUnits()) {
			if (selfUnit == null) {
				continue;
			}
			UnitType unitType = selfUnit.getType();
			Boolean discovered = selfUnitDiscoveredMap.get(unitType);
			if (discovered == null || discovered == Boolean.FALSE) {
				selfUnitDiscoveredMap.put(unitType, Boolean.TRUE);
			}

			if (selfUnit.isCompleted()) {
				Boolean discoveredComplete = selfCompleteUnitDiscoveredMap.get(unitType);
				if (discoveredComplete == null || discoveredComplete == Boolean.FALSE) {
					selfCompleteUnitDiscoveredMap.put(unitType, Boolean.TRUE);
				}
			}
		}
		
		for (Unit enemyUnit : Prebot.Broodwar.enemy().getUnits()) {
			if (enemyUnit == null) {
				continue;
			}
			UnitType unitType = enemyUnit.getType();
			Boolean discovered = enemyUnitDiscoveredMap.get(unitType);
			if (discovered == null || discovered == Boolean.FALSE) {
				enemyUnitDiscoveredMap.put(unitType, Boolean.TRUE);
			}

			if (enemyUnit.isCompleted()) {
				Boolean discoveredComplete = enemyCompleteUnitDiscoveredMap.get(unitType);
				if (discoveredComplete == null || discoveredComplete == Boolean.FALSE) {
					enemyCompleteUnitDiscoveredMap.put(unitType, Boolean.TRUE);
				}
			}
		}
	}

	private <T> List<T> getUnitListReturnEmptyListIfNull(Map<UnitType, List<T>> unitMap, UnitType unitType) {
		return unitMap.getOrDefault(unitType, new ArrayList<T>());
//		List<T> unitList = unitMap.get(unitType);
//		if (unitList == null) {
//			return new ArrayList<T>();
//		} else {
//			return unitList;
//		}
	}
}