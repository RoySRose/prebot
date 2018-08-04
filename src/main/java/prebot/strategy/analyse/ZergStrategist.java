package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;

public class ZergStrategist extends Strategist {
	
	public ZergStrategist() {
		super(UnitType.Zerg_Lair);
	}
	
	@Override
	protected EnemyStrategy strategyPhase01() {
		if (hasInfo(ClueInfo.DOUBLE_HATCH_12HAT)) {
			if (hasAnyInfo(ClueInfo.EXTRACTOR_LATE, ClueInfo.NO_EXTRACTOR) && !hasInfo(ClueInfo.LAIR_2HAT_FAST)) {
				return EnemyStrategy.ZERG_3HAT;
			} else {
				return EnemyStrategy.ZERG_2HAT_GAS;
			}
		} else if (hasInfo(ClueInfo.TWIN_HATCH)) {
			return EnemyStrategy.ZERG_TWIN_HAT; // twin은 좋지 않은 빌드이다.
		}
		
		if (hasInfo(ClueInfo.POOL_5DRONE)) {
			return EnemyStrategy.ZERG_5DRONE;
			
		} else if (hasAnyInfo(ClueInfo.POOL_9DRONE, ClueInfo.POOL_9DRONE_UNDER)) {
			if (hasAnyInfo(ClueInfo.EXTRACTOR_9DRONE, ClueInfo.EXTRACTOR_OVERPOOL)) {
				return EnemyStrategy.ZERG_9DRONE_GAS;
			} else {
				return EnemyStrategy.ZERG_9DRONE;
			}
			
		} else if (hasAnyInfo(ClueInfo.POOL_OVERPOOL, ClueInfo.POOL_OVERPOOL_UNDER)) {
			if (hasAnyInfo(ClueInfo.EXTRACTOR_9DRONE, ClueInfo.EXTRACTOR_OVERPOOL)) {
				return EnemyStrategy.ZERG_OVERPOOL_GAS;
			} else {
				return EnemyStrategy.ZERG_OVERPOOL;
			}
			
		} else if (hasAnyInfo(ClueInfo.POOL_2HAT, ClueInfo.LATE_POOL)) {
			if (hasAnyInfo(ClueInfo.EXTRACTOR_LATE, ClueInfo.NO_EXTRACTOR)) {
				return EnemyStrategy.ZERG_3HAT;
			} else {
				return EnemyStrategy.ZERG_2HAT_GAS;
			}
		}
		
		if (hasType(ClueType.DOUBLE_HATCH)) {
			if (hasInfo(ClueInfo.DOUBLE_HATCH_OVERPOOL)) {
				return EnemyStrategy.ZERG_OVERPOOL;
			} else if (hasInfo(ClueInfo.DOUBLE_HATCH_9DRONE)) {
				return EnemyStrategy.ZERG_9DRONE;
			}
		}

		return EnemyStrategy.ZERG_INIT;
	}
	
	@Override
	protected EnemyStrategy strategyPhase02() {
		if (hasAnyInfo(ClueInfo.LAIR_INCOMPLETE, ClueInfo.LAIR_COMPLETE)) {
			boolean spireTech = hasAnyInfo(ClueInfo.FAST_SPIRE, ClueInfo.SPIRE, ClueInfo.FAST_MUTAL);
			boolean hydraTech = hasAnyInfo(ClueInfo.HYDRADEN_BEFORE_LAIR_START, ClueInfo.HYDRADEN_BEFORE_LAIR_COMPLETE, ClueInfo.FAST_HYDRA);
			boolean hydraThreeMany = hasInfo(ClueInfo.THREE_MANY_HYDRA);
			boolean hydraFiveMany = hasInfo(ClueInfo.FIVE_MANY_HYDRA);
			boolean lurkerFound = hasAnyInfo(ClueInfo.FAST_LURKER);
			
			if (spireTech && hydraTech) {
				return EnemyStrategy.ZERG_LAIR_MIXED;
			} else if (lurkerFound) {
				return EnemyStrategy.ZERG_FAST_LURKER;
			} else if (hydraThreeMany || hydraFiveMany) {
				return EnemyStrategy.ZERG_HYDRA_WAVE;
			} else if (hydraTech) {
				return EnemyStrategy.ZERG_FAST_LURKER;
			} else if (spireTech) {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			} else {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			}
			
		} else if (hasAnyInfo(ClueInfo.NO_LAIR)) {
			if (hasType(ClueType.SPIRE)) {
				return EnemyStrategy.ZERG_FAST_MUTAL; // no lair, no mute
			} else if (hasAnyType(ClueType.HYDRADEN, ClueType.FAST_HYDRA)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			} else {
				return EnemyStrategy.ZERG_NO_LAIR_LING;
			}
		} else {
			if (hasType(ClueType.SPIRE)) {
				return EnemyStrategy.ZERG_FAST_MUTAL; // no lair, no mute
			} else if (hasAnyType(ClueType.HYDRADEN, ClueType.FAST_HYDRA)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			}
			return EnemyStrategy.ZERG_FAST_MUTAL;
		}
	}
	
	@Override
	protected EnemyStrategy strategyPhase03() {
//		int hatchCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Hatchery);
//		int lairCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Lair);
//		int hiveCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Hive);
//		
//		int zerglingCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling);
		int enemyGroundUnitPower = UnitUtils.enemyGroundUnitPower();
		int enemyAirUnitPower = UnitUtils.enemyAirUnitPower();

		boolean hydraDiscovered = UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Hydralisk, UnitType.Zerg_Lurker, UnitType.Zerg_Hydralisk_Den);
		boolean spireExist = InfoUtils.enemyNumUnits(UnitType.Zerg_Spire, UnitType.Zerg_Greater_Spire) > 0;
		
		if (enemyGroundUnitPower > enemyAirUnitPower) {
			if (enemyAirUnitPower == 0 && !spireExist) {
				return EnemyStrategy.ZERG_GROUND3;
			} else if (enemyGroundUnitPower > enemyAirUnitPower * 1.5) {
				return EnemyStrategy.ZERG_GROUND2;
			} else if (enemyGroundUnitPower > enemyAirUnitPower * 2) {
				return EnemyStrategy.ZERG_GROUND1;
			} else {
				return EnemyStrategy.ZERG_MIXED;
			}
		} else {
			if (enemyGroundUnitPower == 0 && !hydraDiscovered) {
				return EnemyStrategy.ZERG_AIR2;
			} else {
				return EnemyStrategy.ZERG_AIR1;
			}
		}
	}
}
