

import bwapi.UnitType;

public class ZergStrategist extends Strategist {
	
	private int warithActivatedFrame = CommonCode.NONE;

	public ZergStrategist() {
		super(UnitType.Zerg_Lair);
	}
	
	@Override
	protected EnemyStrategy strategyPhase01() {
		if (hasAnyType(Clue.ClueType.HYDRADEN, Clue.ClueType.FAST_HYDRA) || hasInfo(Clue.ClueInfo.THREE_MANY_HYDRA)) {
			if (hasAnyInfo(Clue.ClueInfo.LAIR_1HAT_FAST, Clue.ClueInfo.LAIR_COMPLETE)) {
				return EnemyStrategy.ZERG_LURKER_ALL_IN;
			} else if (hasInfo(Clue.ClueInfo.DOUBLE_HATCH_LATE) && hasAnyInfo(Clue.ClueInfo.LAIR_COMPLETE, Clue.ClueInfo.LAIR_INCOMPLETE)) {
				return EnemyStrategy.ZERG_LURKER_ALL_IN;
			}
			return EnemyStrategy.ZERG_HYDRA_ALL_IN;
		}
		
		if (hasInfo(Clue.ClueInfo.DOUBLE_HATCH_3HAT)) {
			return EnemyStrategy.ZERG_3HAT;
		}
		
		if (hasInfo(Clue.ClueInfo.DOUBLE_HATCH_12HAT)) {
			if (hasAnyInfo(Clue.ClueInfo.NO_EXTRACTOR) && !hasAnyInfo(Clue.ClueInfo.LAIR_2HAT_FAST, Clue.ClueInfo.LAIR_1HAT_FAST)) {
				return EnemyStrategy.ZERG_3HAT;
			} else {
				return EnemyStrategy.ZERG_2HAT_GAS;
			}
		} else if (hasInfo(Clue.ClueInfo.TWIN_HATCH)) {
			return EnemyStrategy.ZERG_TWIN_HAT; // twin은 좋지 않은 빌드이다.
		}
		
		if (hasInfo(Clue.ClueInfo.POOL_5DRONE)) {
			return EnemyStrategy.ZERG_5DRONE;
			
		} else if (hasAnyInfo(Clue.ClueInfo.POOL_9DRONE, Clue.ClueInfo.POOL_9DRONE_UNDER)) {
			if (hasAnyInfo(Clue.ClueInfo.EXTRACTOR_9DRONE, Clue.ClueInfo.EXTRACTOR_OVERPOOL)) {
				return EnemyStrategy.ZERG_9DRONE_GAS;
			} else {
				return EnemyStrategy.ZERG_9DRONE;
			}
			
		} else if (hasAnyInfo(Clue.ClueInfo.POOL_OVERPOOL, Clue.ClueInfo.POOL_OVERPOOL_UNDER)) {
			if (hasAnyInfo(Clue.ClueInfo.EXTRACTOR_9DRONE, Clue.ClueInfo.EXTRACTOR_OVERPOOL)) {
				return EnemyStrategy.ZERG_OVERPOOL_GAS;
			} else {
				return EnemyStrategy.ZERG_OVERPOOL;
			}
			
		} else if (hasAnyInfo(Clue.ClueInfo.POOL_2HAT, Clue.ClueInfo.LATE_POOL)) {
			if (hasAnyInfo(Clue.ClueInfo.EXTRACTOR_LATE, Clue.ClueInfo.NO_EXTRACTOR)) {
				return EnemyStrategy.ZERG_3HAT;
			} else {
				return EnemyStrategy.ZERG_2HAT_GAS;
			}
		}
		
		if (hasType(Clue.ClueType.DOUBLE_HATCH)) {
			if (hasInfo(Clue.ClueInfo.DOUBLE_HATCH_OVERPOOL)) {
				return EnemyStrategy.ZERG_OVERPOOL;
			} else if (hasInfo(Clue.ClueInfo.DOUBLE_HATCH_9DRONE)) {
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
		

		boolean spireTech = hasAnyInfo(Clue.ClueInfo.FAST_SPIRE, Clue.ClueInfo.SPIRE, Clue.ClueInfo.FAST_MUTAL);
		boolean hydraTech = hasAnyInfo(Clue.ClueInfo.HYDRADEN_BEFORE_LAIR_START, Clue.ClueInfo.HYDRADEN_BEFORE_LAIR_COMPLETE, Clue.ClueInfo.HYDRADEN, Clue.ClueInfo.FAST_HYDRA);
		boolean hydraFiveMany = hasInfo(Clue.ClueInfo.FIVE_MANY_HYDRA);
		boolean lurkerFound = hasAnyInfo(Clue.ClueInfo.FAST_LURKER);
		
		if (hasAnyInfo(Clue.ClueInfo.LAIR_INCOMPLETE, Clue.ClueInfo.LAIR_COMPLETE)) {
			if (spireTech && hydraTech) {
				return EnemyStrategy.ZERG_LAIR_MIXED;
			} else if (lurkerFound) {
				if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.ZERG_FAST_ALL_IN)
						|| hasAnyInfo(Clue.ClueInfo.DOUBLE_HATCH_LATE)) {
					return EnemyStrategy.ZERG_FAST_1HAT_LURKER;
				} else {
					return EnemyStrategy.ZERG_FAST_LURKER;
				}
				
			} else if (hydraFiveMany) {
				return EnemyStrategy.ZERG_HYDRA_WAVE;
			} else if (hydraTech) {
				if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.ZERG_FAST_ALL_IN)
						|| hasAnyInfo(Clue.ClueInfo.DOUBLE_HATCH_LATE)) {
					return EnemyStrategy.ZERG_FAST_1HAT_LURKER;
				} else {
					return EnemyStrategy.ZERG_FAST_LURKER;
				}
			} else if (spireTech) {
				return fastMutaliskByTime();
			} else {
				return fastMutaliskByTime();
			}
			
		} else if (hasAnyInfo(Clue.ClueInfo.NO_LAIR)) {
			if (spireTech) {
				return fastMutaliskByTime(); // no lair, no mute
			} else if (hasAnyType(Clue.ClueType.HYDRADEN, Clue.ClueType.FAST_HYDRA) || hasInfo(Clue.ClueInfo.THREE_MANY_HYDRA)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			} else {
				return EnemyStrategy.ZERG_NO_LAIR_LING;
			}
		} else {
			if (spireTech) {
				return fastMutaliskByTime(); // no lair, no mute
			} else if (hasAnyType(Clue.ClueType.HYDRADEN, Clue.ClueType.FAST_HYDRA)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			}
			return fastMutaliskByTime();
		}
	}
	
	private EnemyStrategy fastMutaliskByTime() {
		if (EnemyBuildTimer.Instance().mutaliskInMyBaseFrame == CommonCode.UNKNOWN) {
			if (UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Lair)) {
				return EnemyStrategy.ZERG_VERY_FAST_MUTAL;
			} else {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			}
		}

		if (EnemyBuildTimer.Instance().mutaliskInMyBaseFrame < TimeUtils.timeToFrames(6, 20)) {
			if (UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Lair)) {
				return EnemyStrategy.ZERG_VERY_FAST_MUTAL;
			} else {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			}
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
		int goliathCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Goliath);

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
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) > 0) {
			return true;
		}
		warithActivatedFrame = TimeUtils.elapsedFrames();
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
		if (UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode) <= 5) {
			return false;
		}
		warithActivatedFrame = TimeUtils.elapsedFrames();
		return true;
	}
}
