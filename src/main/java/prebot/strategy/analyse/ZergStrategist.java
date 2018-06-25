package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.RaceActionManager;
import prebot.strategy.manage.RaceActionManager.LastCheckLocation;

public class ZergStrategist extends Strategist {

	private static final int PHASE1 = 1;
	private static final int PHASE2 = 2;
	private static final int PHASE3 = 3;
	
	private EnemyStrategy strategyPhase1 = EnemyStrategy.UNKNOWN;
	private EnemyStrategy strategyPhase2 = EnemyStrategy.UNKNOWN;
	private int phase = PHASE1;
	
	@Override
	public EnemyStrategy strategyToApply() {
		EnemyStrategy enemyStrategy = EnemyStrategy.ZERG_INIT;
		if (phase == PHASE1) {
			enemyStrategy = strategyPhase1();
			if (phase1EndCondition()) {
				phase = PHASE2;
				strategyPhase1 = enemyStrategy;
			}
			
		} else if (phase == PHASE2) {
			enemyStrategy = strategyPhase2();
			if (phase2EndCondition()) {
				phase = PHASE3;
				strategyPhase2 = enemyStrategy;
			}
			
		} else {
			enemyStrategy = strategyPhase3();
		}
		
		updateExpectFrame(enemyStrategy);
		
		return enemyStrategy;
	}

	private boolean phase1EndCondition() {
		int expectLairFrame = ClueManager.Instance().getExpectBuildFrame(UnitType.Zerg_Lair);
		boolean lairBuildStarted = TimeUtils.after(expectLairFrame + 5 * TimeUtils.SECOND);
		return lairBuildStarted;
	}

	private boolean phase2EndCondition() {
		return TimeUtils.after(10 * TimeUtils.MINUTE); // TODO 조정
	}

	private EnemyStrategy strategyPhase1() {
		if (hasType(ClueType.DOUBLE_HATCH)) {
			if (hasInfo(ClueInfo.DOUBLE_HATCH_12HAT)) {
				if (hasAnyInfo(ClueInfo.EXTRACTOR_LATE, ClueInfo.NO_EXTRACTOR)) {
					return EnemyStrategy.ZERG_3HAT;
				} else {
					return EnemyStrategy.ZERG_2HAT_GAS;
				}
			} else if (hasInfo(ClueInfo.TWIN_HATCH)) {
				return EnemyStrategy.ZERG_TWIN_HAT; // twin은 좋지 않은 빌드이다.
			}
		}
		
		if (hasType(ClueType.POOL)) {
			if (hasInfo(ClueInfo.POOL_5DRONE)) {
				return EnemyStrategy.ZERG_5DRONE;
				
			} else if (hasInfo(ClueInfo.POOL_9DRONE)) {
				if (hasAnyInfo(ClueInfo.EXTRACTOR_9DRONE, ClueInfo.EXTRACTOR_OVERPOOL)) {
					return EnemyStrategy.ZERG_9DRONE_GAS;
				} else {
					return EnemyStrategy.ZERG_9DRONE;
				}
				
			} else if (hasInfo(ClueInfo.POOL_OVERPOOL)) {
				if (hasAnyInfo(ClueInfo.EXTRACTOR_9DRONE, ClueInfo.EXTRACTOR_OVERPOOL)) {
					return EnemyStrategy.ZERG_OVERPOOL_GAS;
				} else {
					return EnemyStrategy.ZERG_OVERPOOL;
				}
				
			} else if (hasAnyInfo(ClueInfo.POOL_2HAT, ClueInfo.NO_POOL)) {
				if (hasAnyInfo(ClueInfo.EXTRACTOR_LATE, ClueInfo.NO_EXTRACTOR)) {
					return EnemyStrategy.ZERG_3HAT;
				} else {
					return EnemyStrategy.ZERG_2HAT_GAS;
				}
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
	
	private EnemyStrategy strategyPhase2() {
		if (strategyPhase1 == EnemyStrategy.ZERG_9DRONE_GAS || strategyPhase1 == EnemyStrategy.ZERG_OVERPOOL_GAS) {
			if (hasAnyInfo(ClueInfo.LAIR_INCOMPLETE, ClueInfo.LAIR_COMPLETE)) {
				if (hasInfo(ClueInfo.HYDRADEN_FASTEST)) {
					return EnemyStrategy.ZERG_1HAT_FAST_LURKER;
				} else if (hasInfo(ClueInfo.SPIRE_1HAT)) {
					return EnemyStrategy.ZERG_1HAT_FAST_MUTAL;
				}
			}
			
		} else if (strategyPhase1 == EnemyStrategy.ZERG_2HAT_GAS) {
			if (hasAnyInfo(ClueInfo.LAIR_INCOMPLETE, ClueInfo.LAIR_COMPLETE)) {
				if (hasType(ClueType.SPIRE)) {
					return EnemyStrategy.ZERG_2HAT_SPIRE;
				} else if (hasType(ClueType.HYDRADEN)) {
					return EnemyStrategy.ZERG_LAIR_HYDRA_TECH;
				}
			}
			
		} else if (strategyPhase1 == EnemyStrategy.ZERG_3HAT) {
			if (hasAnyInfo(ClueInfo.LAIR_INCOMPLETE, ClueInfo.LAIR_COMPLETE)) {
				if (hasType(ClueType.SPIRE)) {
					return EnemyStrategy.ZERG_3HAT_SPIRE;
				} else if (hasType(ClueType.HYDRADEN)) {
					return EnemyStrategy.ZERG_LAIR_HYDRA_TECH;
				}
			}
		}
		
		if (hasAnyInfo(ClueInfo.NO_LAIR)) {
			if (hasType(ClueType.HYDRADEN)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			} else {
				return EnemyStrategy.ZERG_NO_LAIR_LING;
			}
		}
		
		if (hasType(ClueType.SPIRE)) {
			return EnemyStrategy.ZERG_2HAT_SPIRE;
		} else if (hasType(ClueType.HYDRADEN)) {
			return EnemyStrategy.ZERG_LAIR_HYDRA_TECH;
		}
		
		return EnemyStrategy.ZERG_2HAT_SPIRE;
	}

	private EnemyStrategy strategyPhase3() {
//		int hatchCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Hatchery);
//		int lairCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Lair);
//		int hiveCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Hive);
//		
//		int zerglingCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling);
		int hydraCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk);
		int lurkerCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Lurker, UnitType.Zerg_Lurker_Egg);
		int ultraCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Ultralisk);
		int defilerCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Defiler);
		
		int mutalCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Mutalisk);
		int guardianCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Guardian);

		boolean hydradenExist = InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk_Den) > 0;
		boolean spireExist = InfoUtils.enemyNumUnits(UnitType.Zerg_Spire, UnitType.Zerg_Greater_Spire) > 0;
		
		int groundUnitPoint = (hydraCount * 1) + (lurkerCount * 2) + (ultraCount * 5) + (defilerCount * 3);
		int airUnitPoint = (mutalCount * 1) + (guardianCount * 4);
		
		if (groundUnitPoint > airUnitPoint) {
			if (airUnitPoint == 0 && !spireExist) {
				return EnemyStrategy.ZERG_GROUND3;
			} else if (groundUnitPoint > airUnitPoint * 1.5) {
				return EnemyStrategy.ZERG_GROUND2;
			} else if (groundUnitPoint > airUnitPoint * 2) {
				return EnemyStrategy.ZERG_GROUND1;
			} else {
				return EnemyStrategy.ZERG_MIXED;
			}
		} else {
			if (groundUnitPoint == 0 && !hydradenExist) {
				return EnemyStrategy.ZERG_AIR2;
			} else {
				return EnemyStrategy.ZERG_AIR1;
			}
		}
	}

	private void updateExpectFrame(EnemyStrategy enemyStrategy) {
		if (hasInfo(ClueInfo.NO_LAIR)) {
			int baseLastCheckFrame = RaceActionManager.Instance().lastCheckFrame(LastCheckLocation.BASE) + 5 * TimeUtils.SECOND;
			ClueManager.Instance().updateExpectBuildIfBigger(UnitType.Zerg_Lair, baseLastCheckFrame);
		} else {
			int lairTimeByStrategy = enemyStrategy.defaultTimeMap.time(UnitType.Zerg_Lair);
			ClueManager.Instance().updateExpectBuildIfBigger(UnitType.Zerg_Lair, lairTimeByStrategy);
		}
		
		if (!hasInfo(ClueInfo.SPIRE)) {
			int expectLairFrame = ClueManager.Instance().getExpectBuildFrame(UnitType.Zerg_Lair);
			ClueManager.Instance().updateExpectBuildFrame(UnitType.Zerg_Spire, expectLairFrame + UnitType.Zerg_Lair.buildTime());
		}
	}
}
