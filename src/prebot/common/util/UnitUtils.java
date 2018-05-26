package prebot.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.internal.IConditions.UnitCondition;
import prebot.common.util.internal.UnitCache;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitData;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode;

public class UnitUtils {
	
	/** 유효한 유닛인지 검사 */
	public static boolean isValidUnit(Unit unit) {
		return unit != null && unit.isCompleted() && unit.getHitPoints() > 0 && unit.exists() && unit.getType() != UnitType.Unknown && unit.getPosition().isValid();
	}

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

	/** 유닛 리스트 */
	public static List<Unit> getUnitList(UnitType unitType, UnitFindRange unitFindRange) {
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
	public static boolean hasUnit(UnitType unitType, UnitFindRange unitFindRange) {
		return getUnitCount(unitType, unitFindRange) > 0;
	}
	
	/** 유닛 N개이상 보유 여부 */
	public static boolean hasUnit(UnitType unitType, UnitFindRange unitFindRange, int count) {
		return getUnitCount(unitType, unitFindRange) >= count;
	}

	/** 적 유닛 리스트 */
	public static List<UnitInfo> getEnemyUnitInfoList(UnitType unitType, EnemyUnitFindRange enemyUnitFindRange) {
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
	public static int getEnemyUnitInfoCount(UnitType unitType, EnemyUnitFindRange enemyUnitFindRange) {
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
		UnitData enemyUnitData = InformationManager.Instance().getUnitData(Prebot.Broodwar.enemy());
		for (UnitInfo eui : enemyUnitData.getUnitAndUnitInfoMap().values()) {
			if (euiList.contains(eui)) {
				continue;
			}
			if (eui.getLastPosition().getDistance(position) > radius) {
				continue;
			}
			if (ignorableEnemyUnitInfo(eui)) {
				continue;
			}
			euiList.add(eui);
		}
	}
	
	/** position 근처의 유닛리스트를 리턴 */
	public static List<Unit> getUnitsInRadius(Position position, int radius, PlayerRange playerRange) {
		return getUnitsInRadius(position, radius, playerRange, null);
	}
	
	/** position 근처의 유닛리스트를 리턴 */
	public static List<Unit> getUnitsInRadius(Position position, int radius, PlayerRange playerRange, UnitType unitType) {
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
			if (unitType != null && unit.getType() != unitType) {
				continue;
			}
			unitsInRadius.add(unit);
		}
		return unitsInRadius;
	}
	
	/** position 근처의 유닛리스트를 리턴 */
	public static void addUnitsInRadius(Collection<Unit> units, Position position, int radius, PlayerRange playerRange) {
		Player player = PlayerUtil.getPlayerByRange(playerRange);
		for (Unit unit : Prebot.Broodwar.getUnitsInRadius(position, radius)) {
			if (player != null && player != unit.getPlayer()) {
				continue;
			}
			units.add(unit);
		}
	}
	
	/** 시야에서 사라진지 N초가 경과하여 무시할 수 있다고 판단되면 true 리턴 */
	public static boolean ignorableEnemyUnitInfo(UnitInfo eui) {
		return TimeUtils.elapsedSeconds(eui.getUpdateFrame()) >= StrategyCode.IGNORE_ENEMY_UNITINFO_SECONDS;
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

	public static int remainingBuildTimeByHitPoint(Unit building) {
		double rate = (double) (building.getType().maxHitPoints() - building.getHitPoints()) / building.getType().maxHitPoints();
		return (int) (building.getType().buildTime() * rate);
	}

	public static boolean canAttack(Unit attacker, Unit target) {
		return getWeapon(attacker, target) != WeaponType.None;
	}

	public static boolean canAttackAir(Unit unit) {
		return unit.getType().airWeapon() != WeaponType.None;
	}

	public static boolean canAttackGround(Unit unit) {
		return unit.getType().groundWeapon() != WeaponType.None;
	}

	public static double calculateLTD(Unit attacker, Unit target) {
		WeaponType weapon = getWeapon(attacker, target);
		if (weapon == WeaponType.None) {
			return 0;
		}

		return 0; // C++ : static_cast<double>(weapon.damageAmount()) / weapon.damageCooldown();
	}

	public static WeaponType getWeapon(Unit attacker, Unit target) {
		return target.isFlying() ? attacker.getType().airWeapon() : attacker.getType().groundWeapon();
	}

	public static WeaponType getWeapon(UnitType attacker, UnitType target) {
		return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
	}

	public static int getAttackRange(Unit attacker, Unit target) {
		WeaponType weapon = getWeapon(attacker, target);

		if (weapon == WeaponType.None) {
			return 0;
		}

		int range = weapon.maxRange();

		if ((attacker.getType() == UnitType.Protoss_Dragoon) && (attacker.getPlayer() == Prebot.Broodwar.self())
				&& Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
			range = 6 * 32;
		}

		return range;
	}

	public static int getAttackRange(UnitType attacker, UnitType target) {
		WeaponType weapon = getWeapon(attacker, target);

		if (weapon == WeaponType.None) {
			return 0;
		}

		return weapon.maxRange();
	}

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

}