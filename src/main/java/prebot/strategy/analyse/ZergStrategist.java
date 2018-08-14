package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.EnemyBuildTimer;

public class ZergStrategist extends Strategist {
	
	private int warithActivatedFrame = CommonCode.NONE;

	public ZergStrategist() {
		super(UnitType.Zerg_Lair);
	}
	
	@Override
	protected EnemyStrategy strategyPhase01() {
		if (hasInfo(ClueInfo.DOUBLE_HATCH_3HAT)) {
			return EnemyStrategy.ZERG_3HAT;
		}
		
		if (hasInfo(ClueInfo.DOUBLE_HATCH_12HAT)) {
			if (hasAnyInfo(ClueInfo.EXTRACTOR_LATE, ClueInfo.NO_EXTRACTOR) && !hasAnyInfo(ClueInfo.LAIR_2HAT_FAST, ClueInfo.LAIR_1HAT_FAST)) {
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
		if (minimumFastWraithIfStarportExist()) {
			StrategyIdea.wraithCount = 1;
		}
		
		if (hasAnyInfo(ClueInfo.LAIR_INCOMPLETE, ClueInfo.LAIR_COMPLETE)) {
			boolean spireTech = hasAnyInfo(ClueInfo.FAST_SPIRE, ClueInfo.SPIRE, ClueInfo.FAST_MUTAL);
			boolean hydraTech = hasAnyInfo(ClueInfo.HYDRADEN_BEFORE_LAIR_START, ClueInfo.HYDRADEN_BEFORE_LAIR_COMPLETE, ClueInfo.HYDRADEN, ClueInfo.FAST_HYDRA);
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
				return fastMutaliskByTime();
			} else {
				return fastMutaliskByTime();
			}
			
		} else if (hasAnyInfo(ClueInfo.NO_LAIR)) {
			if (hasType(ClueType.SPIRE)) {
				return fastMutaliskByTime(); // no lair, no mute
			} else if (hasAnyType(ClueType.HYDRADEN, ClueType.FAST_HYDRA)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			} else {
				return EnemyStrategy.ZERG_NO_LAIR_LING;
			}
		} else {
			if (hasType(ClueType.SPIRE)) {
				return fastMutaliskByTime(); // no lair, no mute
			} else if (hasAnyType(ClueType.HYDRADEN, ClueType.FAST_HYDRA)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			}
			return fastMutaliskByTime();
		}
	}
	
	private EnemyStrategy fastMutaliskByTime() {
//		System.out.println(EnemyBuildTimer.Instance().mutaliskInMyBaseFrame + " / " + TimeUtils.timeToFrames(5, 30));
		if (EnemyBuildTimer.Instance().mutaliskInMyBaseFrame == CommonCode.UNKNOWN) {
			System.out.println("mutaliskInMyBaseFrame is unknown.");
			return EnemyStrategy.ZERG_VERY_FAST_MUTAL;
		}
		
		if (EnemyBuildTimer.Instance().mutaliskInMyBaseFrame < TimeUtils.timeToFrames(6, 20)) {
			return EnemyStrategy.ZERG_VERY_FAST_MUTAL;
		} else {
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
		int goliathCount = UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Goliath);

		boolean hydraDiscovered = UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Hydralisk, UnitType.Zerg_Lurker, UnitType.Zerg_Hydralisk_Den);
		boolean spireExist = InfoUtils.enemyNumUnits(UnitType.Zerg_Spire, UnitType.Zerg_Greater_Spire) > 0;
		
		if (enemyGroundUnitPower > enemyAirUnitPower) {
			if (lateWraith()) {
				StrategyIdea.wraithCount = 2;
			}
			
			if (enemyAirUnitPower == 0 && !spireExist) {
				return EnemyStrategy.ZERG_GROUND3;
			} else if (enemyGroundUnitPower > enemyAirUnitPower * 1.5) {
				StrategyIdea.valkyrieCount = 0;
				return EnemyStrategy.ZERG_GROUND2;
			} else if (enemyGroundUnitPower > enemyAirUnitPower * 2) {
				StrategyIdea.valkyrieCount = 0;
				return EnemyStrategy.ZERG_GROUND1;
			} else {
				StrategyIdea.valkyrieCount = 0;
				return EnemyStrategy.ZERG_MIXED;
			}
		} else {
			if (enemyGroundUnitPower == 0 && !hydraDiscovered) {
				StrategyIdea.valkyrieCount = Math.min(goliathCount / 4, 6);
				return EnemyStrategy.ZERG_AIR2;
			} else {
				StrategyIdea.valkyrieCount = Math.min(goliathCount / 5, 4);
				return EnemyStrategy.ZERG_AIR1;
			}
		}
	}
	
	private boolean minimumFastWraithIfStarportExist() {
		if (TimeUtils.elapsedFrames(warithActivatedFrame) < 4 * TimeUtils.SECOND) {
			return false;
		}
		if (StrategyIdea.wraithCount > 0 || StrategyIdea.valkyrieCount > 0) {
			return false;
		}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) > 0) {
			return true;
		}
		warithActivatedFrame = TimeUtils.elapsedFrames();
		System.out.println("fast wraith activated");
		return true;
	}

	private boolean lateWraith() {
		if (TimeUtils.elapsedFrames(warithActivatedFrame) < 4 * TimeUtils.SECOND) {
			return false;
		}
		if (TimeUtils.beforeTime(10, 0)) {
			return false;
		}
		if (StrategyIdea.wraithCount > 0) {
			return false;
		}
		if (UnitUtils.activatedCommandCenterCount() < 2) {
			return false;
		}
		if (UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode) <= 5) {
			return false;
		}
		warithActivatedFrame = TimeUtils.elapsedFrames();
		System.out.println("late wraith activated");
		return true;
	}
}
