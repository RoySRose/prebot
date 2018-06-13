package prebot.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.Region;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.PositionRegion;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.internal.IConditions.UnitCondition;
import prebot.common.util.internal.UnitCache;
import prebot.micro.WorkerManager;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyConfig;

public class UnitUtils {
	
	/** 유효한 유닛인지 검사 */
	public static boolean isValidUnit(Unit unit) {
		return unit != null && unit.isCompleted() && unit.getHitPoints() > 0 && unit.exists() && unit.getType() != UnitType.Unknown && unit.getPosition().isValid();
	}

	@Deprecated // TODO 삭제예정
	public static boolean isValidUnit(Unit unit, boolean excludeIncomplete, boolean excludeUndetected) {
		if (unit == null) {
			return false;
		}
		if (excludeIncomplete && !unit.isCompleted()) {
			return false;
		}
		if (excludeUndetected && !unit.isDetected()) {
			return false;
		}

		return unit.exists() && unit.getType() != UnitType.Unknown && unit.getPosition().isValid();
	}

	/** 시야에 있는 unitinfo이면 unit 정보 리턴 */
	public static Unit unitInSight(UnitInfo eui) {
		Unit enemyUnit = Prebot.Broodwar.getUnit(eui.getUnitID());
		if (UnitUtils.isValidUnit(enemyUnit)) {
			return enemyUnit;
		} else {
			return null;
		}
	}
	
	public static List<Unit> getUnitList(UnitType... unitTypes) {
		Set<Unit> unitSet = new HashSet<>();
		for (UnitType unitType : unitTypes) {
			unitSet.addAll(getUnitList(UnitFindRange.ALL, unitType));
		}
		return new ArrayList<>(unitSet);
	}

	public static List<Unit> getUnitList(UnitFindRange unitFindRange) {
		return getUnitList(unitFindRange, UnitType.AllUnits);
	}
	
	public static List<Unit> getUnitList(UnitFindRange unitFindRange, UnitType... unitTypes) {
		Set<Unit> unitSet = new HashSet<>();
		for (UnitType unitType : unitTypes) {
			unitSet.addAll(getUnitList(unitFindRange, unitType));
		}
		return new ArrayList<>(unitSet);
	}
	
	private static List<Unit> getUnitList(UnitFindRange unitFindRange, UnitType unitType) {
		switch (unitFindRange) {
		case COMPLETE:
			return UnitCache.getCurrentCache().completeUnits(unitType);
		case INCOMPLETE:
			return UnitCache.getCurrentCache().incompleteUnits(unitType);
		case CONSTRUCTION_QUEUE:
			return UnitCache.getCurrentCache().underConstructionUnits(unitType);
		case ALL: case ALL_AND_CONSTRUCTION_QUEUE: default:
			return UnitCache.getCurrentCache().allUnits(unitType);
		}
	}

	/** 유닛 수 (constructionQueue가 포함된 검색인 경우, getUnitList.size()와 수가 다를 수 있다.) */
	public static int getUnitCount(UnitType... unitTypes) {
		int unitCount = 0;
		for (UnitType unitType : unitTypes) {
			unitCount += getUnitCount(UnitFindRange.ALL, unitType);
		}
		return unitCount;
	}
	
	public static int getUnitCount(UnitFindRange unitFindRange, UnitType... unitTypes) {
		int unitCount = 0;
		for (UnitType unitType : unitTypes) {
			unitCount += getUnitCount(unitFindRange, unitType);
		}
		return unitCount;
	}
	
	private static int getUnitCount(UnitFindRange unitFindRange, UnitType unitType) {
		switch (unitFindRange) {
		case COMPLETE:
			return UnitCache.getCurrentCache().completeCount(unitType);
		case INCOMPLETE:
			return UnitCache.getCurrentCache().incompleteCount(unitType);
		case CONSTRUCTION_QUEUE:
			return UnitCache.getCurrentCache().underConstructionCount(unitType);
		case ALL:
			return UnitCache.getCurrentCache().allCount(unitType);
		case ALL_AND_CONSTRUCTION_QUEUE: default:
			if (unitType.isBuilding() && !unitType.isAddon()) {
				return UnitCache.getCurrentCache().underConstructionCount(unitType) + UnitCache.getCurrentCache().completeCount(unitType);
			} else {
				return UnitCache.getCurrentCache().incompleteCount(unitType) + UnitCache.getCurrentCache().completeCount(unitType);
			}
			
		}
	}
	
	/** 유닛 수 (constructionQueue가 포함된 검색인 경우, getUnitList.size()와 수가 다를 수 있다.) */
	public static int getUnitCount(UnitType unitType, UnitFindRange unitFindRange) {
		switch (unitFindRange) {
		case COMPLETE:
			return UnitCache.getCurrentCache().completeCount(unitType);
		case INCOMPLETE:
			return UnitCache.getCurrentCache().incompleteCount(unitType);
		case CONSTRUCTION_QUEUE:
			return UnitCache.getCurrentCache().underConstructionCount(unitType);
		case ALL:
			return UnitCache.getCurrentCache().allCount(unitType);
		case ALL_AND_CONSTRUCTION_QUEUE: default:
			if (unitType.isBuilding() && !unitType.isAddon()) {
				return UnitCache.getCurrentCache().underConstructionCount(unitType) + UnitCache.getCurrentCache().completeCount(unitType);
			} else {
				return UnitCache.getCurrentCache().incompleteCount(unitType) + UnitCache.getCurrentCache().completeCount(unitType);
			}
			
		}
	}
	
	/** 유닛 보유 여부 */
	@Deprecated
	public static boolean hasUnit(UnitFindRange unitFindRange, UnitType unitType) {
		return getUnitCount(unitFindRange, unitType) > 0;
	}
	
	/** 유닛 N개이상 보유 여부 */
	@Deprecated
	public static boolean hasUnit(UnitFindRange unitFindRange, int count, UnitType unitType) {
		return getUnitCount(unitFindRange, unitType) >= count;
	}

	/** 적 유닛 리스트 */
	public static List<UnitInfo> getEnemyUnitInfoList(EnemyUnitFindRange enemyUnitFindRange) {
		return getEnemyUnitInfoList(enemyUnitFindRange, UnitType.AllUnits);
	}
	
	public static List<UnitInfo> getEnemyUnitInfoList(EnemyUnitFindRange enemyUnitFindRange, UnitType... unitTypes) {
		Set<UnitInfo> unitSet = new HashSet<>();
		for (UnitType unitType : unitTypes) {
			unitSet.addAll(getEnemyUnitInfoList(enemyUnitFindRange, unitType));
		}
		return new ArrayList<>(unitSet);
	}
	
	private static List<UnitInfo> getEnemyUnitInfoList(EnemyUnitFindRange enemyUnitFindRange, UnitType unitType) {
		switch (enemyUnitFindRange) {
		case VISIBLE:
			return UnitCache.getCurrentCache().enemyVisibleUnitInfos(unitType);
		case INVISIBLE:
			return UnitCache.getCurrentCache().enemyInvisibleUnitInfos(unitType);
		case ALL: default:
			return UnitCache.getCurrentCache().enemyAllUnitInfos(unitType);
		}
	}

	/** 적 유닛  수 */
	public static int getEnemyUnitInfoCount(EnemyUnitFindRange enemyUnitFindRange, UnitType... unitTypes) {
		int unitCount = 0;
		for (UnitType unitType : unitTypes) {
			unitCount += getEnemyUnitInfoCount(enemyUnitFindRange, unitType);
		}
		return unitCount;
	}
	
	private static int getEnemyUnitInfoCount(EnemyUnitFindRange enemyUnitFindRange, UnitType unitType) {
		switch (enemyUnitFindRange) {
		case VISIBLE:
			return UnitCache.getCurrentCache().enemyVisibleCount(unitType);
		case INVISIBLE:
			return UnitCache.getCurrentCache().enemyInvisibleCount(unitType);
		case ALL: default:
			return UnitCache.getCurrentCache().enemyAllCount(unitType);
		}
	}
	
	/** position으로부터의 반경 radius이내에 있는 유닛정보를 enemyUnitInfoList에 세팅 */
	public static void addEnemyUnitInfosInRadius(Collection<UnitInfo> euiList, Position position, int radius) {
		for (UnitInfo eui : InfoUtils.enemyUnitInfoMap().values()) {
			if (euiList.contains(eui)) {
				continue;
			}
			//TODO unitinfo에 쓰레기 값 들어가는 오류 있음 information manager 확인필요 (아래는 임시 조치)
			if (eui.getUnitID() == 0 && eui.getType() == UnitType.None) {
				continue;
			}
			int weoponrange = 0;
			if (eui.getType().groundWeapon() != WeaponType.None) { // radius 안의 공격범위가 닿는 적까지 포함
				weoponrange = eui.getType().groundWeapon().maxRange() + 40;
			}
			if (eui.getLastPosition().getDistance(position) > radius + weoponrange) {
				continue;
			}
			if (ignorableEnemyUnitInfo(eui)) {
				continue;
			}
			euiList.add(eui);
		}
	}
	
	public static List<UnitInfo> getEnemyUnitInfosInRadius(Position position, int radius) {
		List<UnitInfo> euiList = new ArrayList<>();
		addEnemyUnitInfosInRadius(euiList, position, radius);
		return euiList;
	}
	
	/** position 근처의 유닛리스트를 리턴 */
	public static List<Unit> getUnitsInRadius(PlayerRange playerRange, Position position, int radius) {
		return getUnitsInRadius(playerRange, position, radius, UnitType.AllUnits);
	}
	
	/** position 근처의 유닛리스트를 리턴 */
	public static List<Unit> getUnitsInRadius(PlayerRange playerRange, Position position, int radius, UnitType... unitTypes) {
		Player player = null;
		if (playerRange == PlayerRange.SELF) {
			player = Prebot.Broodwar.self();
		} else if (playerRange == PlayerRange.ENEMY) {
			player = Prebot.Broodwar.enemy();
		} else if (playerRange == PlayerRange.NEUTRAL) {
			player = Prebot.Broodwar.neutral();
		}
		
		List<Unit> unitsInRadius = new ArrayList<>();
		for (Unit unit : Prebot.Broodwar.getUnitsInRadius(position, radius)) {
			if (player != null && player != unit.getPlayer()) {
				continue;
			}
			boolean isSearchingUnitType = false;
			for (UnitType unitType : unitTypes) {
				if (unitType == UnitType.AllUnits || unit.getType() == unitType) {
					isSearchingUnitType = true;
					break;
				}
			}
			if (isSearchingUnitType) {
				unitsInRadius.add(unit);
			}
		}
		return unitsInRadius;
	}
	
	/** position 근처의 유닛리스트를 리턴 */
	public static void addUnitsInRadius(Collection<Unit> units, PlayerRange playerRange, Position position, int radius) {
		Player player = PlayerUtil.getPlayerByRange(playerRange);
		for (Unit unit : Prebot.Broodwar.getUnitsInRadius(position, radius)) {
			if (player != null && player != unit.getPlayer()) {
				continue;
			}
			units.add(unit);
		}
	}
	
	public static List<Unit> getUnitsInRegion(PositionRegion positionRegion, PlayerRange playerRange) {
		List<Unit> totalUnits = null;
		if (playerRange == PlayerRange.SELF) {
			totalUnits = Prebot.Broodwar.self().getUnits();
		} else if (playerRange == PlayerRange.ENEMY) {
			totalUnits = Prebot.Broodwar.enemy().getUnits();
		} else if (playerRange == PlayerRange.NEUTRAL) {
			totalUnits = Prebot.Broodwar.neutral().getUnits();
		} else {
			totalUnits = Prebot.Broodwar.getAllUnits();
		}
		
		List<Unit> unitsInRegion = new ArrayList<>();
		Region region = PositionUtils.positionRegionToRegion(positionRegion);
	    for (Unit unit : totalUnits) {
	    	if (UnitUtils.isValidUnit(unit)) {
	    		if (region == BWTA.getRegion(unit.getPosition())) {
		            unitsInRegion.add(unit);
		        }
	    	}
	    }
		return unitsInRegion;
	}
	
	/// 유닛타입의 아군 유닛이 하나 생산되었는지 여부
	public static boolean myUnitDiscovered(UnitType... unitTypes) {
		Map<UnitType, Boolean> selfUnitDiscoveredMap = UnitCache.getCurrentCache().getSelfUnitDiscoveredMap();
		return unitDiscovered(selfUnitDiscoveredMap, unitTypes);
	}
	
	/// 유닛타입의 적 유닛이 하나 생산되었는지 여부
	public static boolean enemyUnitDiscovered(UnitType... unitTypes) {
		Map<UnitType, Boolean> enemyUnitDiscoveredMap = UnitCache.getCurrentCache().getEnemyUnitDiscoveredMap();
		return unitDiscovered(enemyUnitDiscoveredMap, unitTypes);
	}
	
	private static boolean unitDiscovered(Map<UnitType, Boolean> unitDiscoveredMap, UnitType... unitTypes) {
		for (UnitType unitType : unitTypes) {
			Boolean discovered = unitDiscoveredMap.get(unitType);
			if (discovered != null && discovered == Boolean.TRUE) {
				return true;
			}
		}
		return false;
	}
	
	/** 시야에서 사라진지 N초가 경과하여 무시할 수 있다고 판단되면 true 리턴 */
	public static boolean ignorableEnemyUnitInfo(UnitInfo eui) {
		return TimeUtils.elapsedSeconds(eui.getUpdateFrame()) >= StrategyConfig.IGNORE_ENEMY_UNITINFO_SECONDS;
	}
	
	/** 즉시 생산할 수 있는 상태인지 판단 */
	public static boolean isProduceableImmediately(UnitType unitType) {
		// 생산할 수 있는 자원이 있어야 한다.
		if (Prebot.Broodwar.self().minerals() < unitType.mineralPrice()
				&& Prebot.Broodwar.self().gas() < unitType.gasPrice()) {
			return false;
		}
		
		// 서플라이가 있어야 한다.
		if (unitType.supplyRequired() > 0) {
			int supplySpace = Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed();
			if (supplySpace < unitType.supplyRequired()) {
				return false;
			}
		}
		
		return Prebot.Broodwar.canMake(unitType);
	}

	/** unitList 중 position에 가장 가까운 유닛 리턴 */
	public static Unit getClosestUnitToPosition(List<Unit> unitList, Position position) {
		return getClosestUnitToPosition(unitList, position, new UnitCondition() {
			@Override
			public boolean correspond(Unit unit) {
				return true;
			}
		});
	}

	/** unitList 중 position에 가장 가까운 유닛타입 유닛 리턴 */
	public static Unit getClosestUnitToPosition(List<Unit> unitList, Position position, final UnitType unitType) {
		return getClosestUnitToPosition(unitList, position, new UnitCondition() {
			@Override
			public boolean correspond(Unit unit) {
				return unit.getType() == unitType;
			}
		});
	}
	
	/** unitList 중 position에 가장 가까운 미네랄 일꾼 리턴 */
	public static Unit getClosestMineralWorkerToPosition(List<Unit> unitList, Position position) {
		return getClosestUnitToPosition(unitList, position, new UnitCondition() {
			@Override
			public boolean correspond(Unit unit) {
				return unit.getType().isWorker() && WorkerManager.Instance().isMineralWorker(unit) && !unit.isCarryingMinerals();
			}
		});
	}

	/** unitList 중 position에 조건(unitCondition)에 부합하는 가장 가까운 유닛 리턴 */
	private static Unit getClosestUnitToPosition(List<Unit> unitList, Position position, UnitCondition unitCondition) {
		if (unitList.size() == 0) {
			return null;
		}
		if (!PositionUtils.isValidPosition(position)) {
			return unitList.get(0);
		}

		Unit closestUnit = null;
		double closestDist = CommonCode.DOUBLE_MAX;

		for (Unit unit : unitList) {
			if (!UnitUtils.isValidUnit(unit) || !unitCondition.correspond(unit)) {
				continue;
			}
			double dist = unit.getDistance(position);
			if (closestUnit == null || dist < closestDist) {
				closestUnit = unit;
				closestDist = dist;
			}
		}
		return closestUnit;
	}

	public static Map<UnitType, List<Unit>> makeUnitListMap(List<Unit> sourceUnitList) {
		Map<UnitType, List<Unit>> unitListMap = new HashMap<>();
		for (Unit unit : sourceUnitList) {
			List<Unit> unitList = unitListMap.get(unit.getType());
			if (unitList == null) {
				unitList = new ArrayList<>();
			}
			unitList.add(unit);
			unitListMap.put(unit.getType(), unitList);
		}
		return unitListMap;
	}
	
	/////////////////////////////////////////// 기본 제공 메서드 ////////////////////////////////////////////////////////////

	// TODO all unit 루프를 돌아 메서드 수정 필요
	public static int getNearMineralsCount(Unit pointUnit) {
		if (pointUnit == null) {
			return 0;
		}
		int mineralsNearDepot = 0;
		for (Unit mineral : Prebot.Broodwar.getAllUnits()) {
			if (mineral.getType().isMineralField() && pointUnit.getDistance(mineral) < 200) {
				mineralsNearDepot++;
			}
		}
		return mineralsNearDepot;
	}
	
	/// 해당 UnitType 이 전투유닛이면 리턴
	public final static boolean isUnitOrCombatBuilding(UnitType type) {
		if (type.isBuilding()) { // 공격불가능한 건물
			return type.canAttack();
		} else {
			return true;
		}
	}
	
	public static int myFactoryUnitSupplyCount() {
		int totalSupplyCount = 0;
		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
			if (!UnitUtils.isValidUnit(unit)) {
				continue;
			}
			if (unit.getType() == UnitType.Terran_Vulture
					|| unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
					|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
					|| unit.getType() == UnitType.Terran_Goliath) {
				totalSupplyCount++;
			}
		}
		return totalSupplyCount * 4; // 인구수 기준이므로
	}

}