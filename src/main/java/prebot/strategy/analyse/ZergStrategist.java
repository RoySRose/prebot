package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.EnemyBuildTimer;

public class ZergStrategist extends Strategist {
	
	@Override
	public EnemyStrategy strategyToApply() {
		if (phase01()) {
			return StrategyIdea.startStrategy = strategyPhase01();
		} else if (phase02()) {
			return strategyPhase02();
		} else {
			return strategyPhase03();
		}
	}

	private boolean phase01() {
		int buildTimeExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
		return TimeUtils.before(buildTimeExpect) || TimeUtils.before(TimeUtils.timeToFrames(4, 0));
	}

	private boolean phase02() {
		return TimeUtils.before(10 * TimeUtils.MINUTE); // TODO 조정
	}

	private EnemyStrategy strategyPhase01() {
		if (hasInfo(ClueInfo.DOUBLE_HATCH_12HAT)) {
			if (hasAnyInfo(ClueInfo.EXTRACTOR_LATE, ClueInfo.NO_EXTRACTOR) && !hasInfo(ClueInfo.LAIR_2HAT_FAST)) {
				return EnemyStrategy.ZERG_3HAT;
			} else {
				return EnemyStrategy.ZERG_2HAT_GAS;
			}
		} else if (hasInfo(ClueInfo.TWIN_HATCH)) {
			return EnemyStrategy.ZERG_TWIN_HAT; // twin은 좋지 않은 빌드이다.
		}
		
		if (hasType(ClueType.POOL)) {
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
	
	private EnemyStrategy strategyPhase02() {
		
		// TODO unit에 의한 판단 최상위
		
		if (hasAnyInfo(ClueInfo.LAIR_INCOMPLETE, ClueInfo.LAIR_COMPLETE)) {
			if (hasAllType(ClueType.SPIRE, ClueType.HYDRADEN)) {
				return EnemyStrategy.ZERG_LAIR_MIXED;
			} else if (hasType(ClueType.SPIRE)) {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			} else if (hasType(ClueType.HYDRADEN)) {
				return EnemyStrategy.ZERG_FAST_LURKER;
			} else {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			}
			
		} else if (hasAnyInfo(ClueInfo.NO_LAIR)) {
			if (hasType(ClueType.SPIRE)) {
				return EnemyStrategy.ZERG_FAST_MUTAL; // no lair, no mute
			} else if (hasType(ClueType.HYDRADEN)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			} else {
				return EnemyStrategy.ZERG_NO_LAIR_LING;
			}
		} else {
			if (hasType(ClueType.SPIRE)) {
				return EnemyStrategy.ZERG_FAST_MUTAL; // no lair, no mute
			} else if (hasType(ClueType.HYDRADEN)) {
				return EnemyStrategy.ZERG_NO_LAIR_HYDRA;
			} else {
				return EnemyStrategy.ZERG_FAST_MUTAL;
			}
		}
	}

	private EnemyStrategy strategyPhase03() {
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
}
