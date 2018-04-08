package prebot.common.util;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import prebot.common.code.GameConstant;
import prebot.common.code.Code.CommonCode;
import prebot.common.code.Code.EnemyUnitFindRange;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.internal.UnitCache;
import prebot.main.PreBot;
import prebot.manager.InformationManager;
import prebot.manager.WorkerManager;
import prebot.manager.information.UnitData;
import prebot.manager.information.UnitInfo;

/**
 * 유닛 유틸
 * 
 * @author insaneojw
 *
 */
public class UnitUtils {

	/// 유닛 리스트
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
	
	/// 유닛 수
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
			return UnitCache.getCurrentCache().underConstructionCount(unitType) + UnitCache.getCurrentCache().completeCount(unitType);
		}
	}
	
	/// 유닛 보유 여부
	public static boolean hasUnit(UnitType unitType, UnitFindRange unitFindRange) {
		return getUnitCount(unitType, unitFindRange) > 0;
	}
	
	/// 유닛 N개이상 보유 여부
	public static boolean hasUnit(UnitType unitType, UnitFindRange unitFindRange, int count) {
		return getUnitCount(unitType, unitFindRange) >= count;
	}

	/// 적 유닛 리스트
	public static List<UnitInfo> enemyUnitInfoList(UnitType unitType, EnemyUnitFindRange enemyUnitFindRange) {
		switch (enemyUnitFindRange) {
		case VISIBLE:
			return UnitCache.getCurrentCache().enemyVisibleUnitInfos(unitType);
		case INVISIBLE:
			return UnitCache.getCurrentCache().enemyInvisibleUnitInfos(unitType);
		case ALL: default:
			return UnitCache.getCurrentCache().enemyAllUnitInfos(unitType);
		}
	}
	
	/// 현재 생산할 수 있는 상태인지 리턴
	public final static boolean isProduceableImmediately(UnitType unitType) {
		// 생산할 수 있는 자원이 있어야 한다.
		if (PreBot.Broodwar.self().minerals() < unitType.mineralPrice()
				&& PreBot.Broodwar.self().gas() < unitType.gasPrice()) {
			return false;
		}
		
		// 서플라이가 있어야 한다.
		if (unitType.supplyRequired() > 0) {
			int supplySpace = PreBot.Broodwar.self().supplyTotal() - PreBot.Broodwar.self().supplyUsed();
			if (supplySpace < unitType.supplyRequired()) {
				return false;
			}
		}
		
		return PreBot.Broodwar.canMake(unitType);
	}
	
	

	public static Unit getClosestUnitToPosition(final List<Unit> units, Position closestTo) {
		return getClosestUnitToPosition(units, closestTo, null);
	}
	
	
	public static void getEuiList(List<UnitInfo> euiList, Position fromPosition, int radius) {
		UnitData enemyUnitData = InformationManager.Instance().getUnitData(PreBot.Broodwar.enemy());
		for (UnitInfo eui : enemyUnitData.getUnitAndUnitInfoMap().values()) {
			if (euiList.contains(eui)) {
				continue;
			}
			if (eui.lastPosition.getDistance(fromPosition) > (radius)) {
				continue;
			}
			if (isVeryOldUnitInfo(eui)) {
				continue;
			}
			euiList.add(eui);
		}
	}
	
	public static List<UnitInfo> getEuiList(Position fromPosition, int radius) {
		List<UnitInfo> euiList = new ArrayList<>();
		getEuiList(euiList, fromPosition, radius);
		return euiList;
	}
	
	public static boolean isVeryOldUnitInfo(UnitInfo ui) {
		return TimeUtils.seconds(ui.updateFrame) >= GameConstant.IGNORE_ENEMY_UNITINFO_SECONDS;
	}

	public static boolean isValidUnit(Unit unit) {
		return unit != null && unit.isCompleted() && unit.getHitPoints() > 0 && unit.exists() && unit.getType() != UnitType.Unknown && unit.getPosition().isValid();
	}

	// 미사용
	// public double GetDistanceBetweenTwoRectangles(Rect rect1, Rect rect2)
	// {
	// Rect & mostLeft = rect1.x < rect2.x ? rect1 : rect2;
	// Rect & mostRight = rect2.x < rect1.x ? rect1 : rect2;
	// Rect & upper = rect1.y < rect2.y ? rect1 : rect2;
	// Rect & lower = rect2.y < rect1.y ? rect1 : rect2;
	//
	// int diffX = std::max(0, mostLeft.x == mostRight.x ? 0 : mostRight.x - (mostLeft.x + mostLeft.width));
	// int diffY = std::max(0, upper.y == lower.y ? 0 : lower.y - (upper.y + upper.height));
	//
	// return std::sqrtf(static_cast<float>(diffX*diffX + diffY*diffY));
	// }

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

		if ((attacker.getType() == UnitType.Protoss_Dragoon) && (attacker.getPlayer() == PreBot.Broodwar.self())
				&& PreBot.Broodwar.self().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
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

	// TODO 전체 순차탐색을 하기 때문에 느리다
	public static Unit getClosestUnitToPosition(final List<Unit> units, Position closestTo, UnitType unitType) {
		if (units.size() == 0) {
			return null;
		}

		// if we don't care where the unit is return the first one we have
		if (!PositionUtils.isValidPosition(closestTo)) {
			return units.get(0); // C++ : return units.begin();
		}

		if (unitType != null && unitType.isWorker()) {
			return getClosestMineralWorkerToPosition(units, closestTo);
		}

		Unit closestUnit = null;
		double closestDist = CommonCode.DOUBLE_MAX;

		for (Unit unit : units) {
			if (!UnitUtils.isValidUnit(unit)) {
				continue;
			}

			if (unitType != null && unit.getType() != unitType) {
				continue;
			}

			double dist = unit.getDistance(closestTo);
			if (closestUnit == null || dist < closestDist) {
				closestUnit = unit;
				closestDist = dist;
			}
		}

		return closestUnit;
	}

	/// target 으로부터 가장 가까운 Mineral 일꾼 유닛을 리턴합니다
	public static Unit getClosestMineralWorkerToPosition(final List<Unit> workers, Position closestTo) {
		Unit closestUnit = null;
		double closestDist = CommonCode.DOUBLE_MAX;
		boolean closestUnitIsCarrying = false;

		for (Unit worker : workers) {
			if (!UnitUtils.isValidUnit(worker) || !worker.getType().isWorker() || !WorkerManager.Instance().isMineralWorker(worker)) {
				continue;
			}

			double dist = worker.getDistance(closestTo);
			if (closestUnit == null) {
				closestUnit = worker;
				closestDist = dist;
				if (worker.isCarryingMinerals() || worker.isCarryingGas()) {
					closestUnitIsCarrying = true;
				}

			} else if (dist < closestDist) {
				if (closestUnitIsCarrying) {
					if (!worker.isCarryingMinerals() && !worker.isCarryingGas()) {
						closestUnitIsCarrying = false;
					}
					closestUnit = worker;
					closestDist = dist;
				} else {
					closestUnit = worker;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}

	// TODO all unit 루프를 돌아 메서드 수정 필요
	public static int getNearMineralsCount(Unit pointUnit) {
		if (pointUnit == null) {
			return 0;
		}
		int mineralsNearDepot = 0;
		for (Unit mineral : PreBot.Broodwar.getAllUnits()) {
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