package prebot.common.util.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.ConstructionTask;
import prebot.information.UnitData;
import prebot.information.UnitInfo;
import prebot.main.Prebot;
import prebot.main.manager.ConstructionManager;
import prebot.main.manager.InformationManager;

/// 유닛 리스트를 부하가 걸리지 않도록 관리
public class UnitCache {
	
	private static UnitCache currentCache = new UnitCache();
	
	public static UnitCache getCurrentCache() {
		return currentCache;
	}

	private Map<UnitType, List<Unit>> selfAllUnitMap = new HashMap<>(); /// 아군 모든 유닛
	private Map<UnitType, List<Unit>> selfCompleteUnitMap = new HashMap<>(); /// 아군 완성 유닛
	private Map<UnitType, List<Unit>> selfIncompleteUnitMap = new HashMap<>(); /// 아군 미완성 유닛
	private Map<UnitType, List<ConstructionTask>> selfConstructionTaskMap = new HashMap<>(); /// 아군 건설큐의 건물

	private Map<UnitType, List<UnitInfo>> enemyAllUnitInfoMap = new HashMap<>(); /// 적군 모든 유닛정보
	private Map<UnitType, List<UnitInfo>> enemyVisibleUnitInfoMap = new HashMap<>(); /// 보이는 적군 유닛정보
	private Map<UnitType, List<UnitInfo>> enemyInvisibleUnitInfoMap = new HashMap<>(); /// 안보이는 적군 유닛정보

	////////////////////// 아군 유닛 리스트 관련

	public int allCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfAllUnitMap, unitType).size();
	}

	public List<Unit> allUnits(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfAllUnitMap, unitType);
	}

	public int completeCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfCompleteUnitMap, unitType).size();
	}

	public List<Unit> completeUnits(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfCompleteUnitMap, unitType);
	}

	public int incompleteCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfIncompleteUnitMap, unitType).size();
	}

	public List<Unit> incompleteUnits(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfIncompleteUnitMap, unitType);
	}

	public int underConstructionCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(selfConstructionTaskMap, unitType).size();
	}

	// 실제 construction queue의 사이즈보다 유닛리스트의 수가 적을 수 있다.(건설시작 전인 빌딩이 있을수 있으므로)
	public List<Unit> underConstructionUnits(UnitType unitType) {
		List<Unit> unitList = new ArrayList<>();
		List<ConstructionTask> tasks = selfConstructionTaskMap.get(unitType);
		for (ConstructionTask task : tasks) {
			if (task.getBuildingUnit() != null) {
				unitList.add(task.getBuildingUnit());
			}
		}
		return unitList;
	}

	////////////////////// 적군 유닛정보 리스트 관련

	public int enemyAllCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(enemyAllUnitInfoMap, unitType).size();
	}

	public List<UnitInfo> enemyAllUnitInfos(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(enemyAllUnitInfoMap, unitType);
	}

	public int enemyVisibleCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(enemyVisibleUnitInfoMap, unitType).size();
	}

	public List<UnitInfo> enemyVisibleUnitInfos(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(enemyVisibleUnitInfoMap, unitType);
	}

	public int enemyInvisibleCount(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(enemyInvisibleUnitInfoMap, unitType).size();
	}

	public List<UnitInfo> enemyInvisibleUnitInfos(UnitType unitType) {
		return getUnitListReturnEmptyListIfNull(enemyInvisibleUnitInfoMap, unitType);
	}

	////////////////////// 캐시 업데이트

	public void updateCache() {
		claerAll();
		putSelfData();
		putEnemyData();
	}

	private void claerAll() {
		selfAllUnitMap.clear();
		selfCompleteUnitMap.clear();
		selfIncompleteUnitMap.clear();
		selfConstructionTaskMap.clear();

		enemyAllUnitInfoMap.clear();
		enemyVisibleUnitInfoMap.clear();
		enemyInvisibleUnitInfoMap.clear();
	}

	private void putSelfData() {
		for (Unit unit : Prebot.Game.self().getUnits()) {
			if (unit == null) {
				continue;
			}
			List<Unit> allUnitList = getUnitListReturnEmptyListIfNull(selfAllUnitMap, unit.getType());
			List<Unit> completeUnitList = getUnitListReturnEmptyListIfNull(selfCompleteUnitMap, unit.getType());
			List<Unit> incompleteUnitList = getUnitListReturnEmptyListIfNull(selfIncompleteUnitMap, unit.getType());

			allUnitList.add(unit);
			if (unit.isCompleted()) {
				completeUnitList.add(unit);
			} else {
				incompleteUnitList.add(unit);
			}

			selfAllUnitMap.put(unit.getType(), allUnitList);
			selfCompleteUnitMap.put(unit.getType(), completeUnitList);
			selfIncompleteUnitMap.put(unit.getType(), incompleteUnitList);
		}

		Vector<ConstructionTask> constructionTaskvector = ConstructionManager.Instance().getConstructionQueue();
		for (ConstructionTask constructionTask : constructionTaskvector) {
			List<ConstructionTask> constructionTaskList = getUnitListReturnEmptyListIfNull(selfConstructionTaskMap, constructionTask.getType());
			constructionTaskList.add(constructionTask);
			selfConstructionTaskMap.put(constructionTask.getType(), constructionTaskList);
		}
	}

	private void putEnemyData() {
		UnitData enemyUnitData = InformationManager.Instance().getUnitData(Prebot.Game.enemy());
		for (int unitId : enemyUnitData.getUnitAndUnitInfoMap().keySet()) {
			UnitInfo unitInfo = enemyUnitData.getUnitAndUnitInfoMap().get(unitId);

			List<UnitInfo> allUnitList = getUnitListReturnEmptyListIfNull(enemyAllUnitInfoMap, unitInfo.getType());
			List<UnitInfo> visibleUnitList = getUnitListReturnEmptyListIfNull(enemyVisibleUnitInfoMap, unitInfo.getType());
			List<UnitInfo> invisibleUnitList = getUnitListReturnEmptyListIfNull(enemyInvisibleUnitInfoMap, unitInfo.getType());

			allUnitList.add(unitInfo);
			Unit enemy = Prebot.Game.getUnit(unitInfo.getUnitID());
			if (enemy != null && enemy.getType() != UnitType.Unknown) {
				visibleUnitList.add(unitInfo);
			} else {
				invisibleUnitList.add(unitInfo);
			}

			enemyAllUnitInfoMap.put(unitInfo.getType(), allUnitList);
			enemyVisibleUnitInfoMap.put(unitInfo.getType(), visibleUnitList);
			enemyInvisibleUnitInfoMap.put(unitInfo.getType(), invisibleUnitList);
		}
	}

	private <T> List<T> getUnitListReturnEmptyListIfNull(Map<UnitType, List<T>> unitMap, UnitType unitType) {
		List<T> unitList = unitMap.get(unitType);
		if (unitList == null) {
			return new ArrayList<T>();
		} else {
			return unitList;
		}
	}
}