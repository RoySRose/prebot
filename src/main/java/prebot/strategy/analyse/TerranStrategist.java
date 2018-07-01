package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.EnemyBuildTimer;

public class TerranStrategist extends Strategist {

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
		if (TimeUtils.before(TimeUtils.timeToFrames(4, 0))) {
			int buildTimeExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Terran_Factory);
			return buildTimeExpect == CommonCode.UNKNOWN || TimeUtils.before(buildTimeExpect);
		}
		return false;
	}

	private boolean phase02() {
		return TimeUtils.before(10 * TimeUtils.MINUTE); // TODO 조정
	}
	
	private EnemyStrategy strategyPhase01() {
		if (hasInfo(ClueInfo.COMMAND_FASTEST_DOUBLE)) {
			return EnemyStrategy.TERRAN_NO_BARRACKS_DOUBLE;
		} else if (hasInfo(ClueInfo.COMMAND_FAST_DOUBLE)) {
			return EnemyStrategy.TERRAN_1BARRACKS_DOUBLE;
		}

		if (hasInfo(ClueInfo.BARRACK_FASTEST_TWO)) {
			return EnemyStrategy.TERRAN_BBS;
			
		} else if (hasInfo(ClueInfo.BARRACK_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
			
		} else if (hasAnyInfo(ClueInfo.BARRACK_ONE, ClueInfo.BARRACK_FAST_ONE)) {
			if (hasAnyInfo(ClueInfo.REFINERY_FAST, ClueInfo.REFINERY_LATE)) {
				return EnemyStrategy.TERRAN_MECHANIC; 
			} else if (hasInfo(ClueInfo.NO_REFINERY)) {
				return EnemyStrategy.TERRAN_1BARRACKS_DOUBLE;
			}
		} else if (hasAllInfo(ClueInfo.BARRACK_NOT_FOUND, ClueInfo.NO_REFINERY)) {
			return EnemyStrategy.TERRAN_BBS;
		}
		
		if (hasInfo(ClueInfo.REFINERY_FAST)) {
			return EnemyStrategy.TERRAN_MECHANIC; 
		}
		
		if (hasInfo(ClueInfo.ACADEMY_FAST)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(ClueInfo.BARRACK_TWO)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
		}
		
		return EnemyStrategy.TERRAN_INIT;
	}

	private EnemyStrategy strategyPhase02() {
		if (hasInfo(ClueInfo.STARPORT_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2STAR;
		} else if (hasInfo(ClueInfo.STARPORT_FAST_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_1STAR;
		}

		if (hasInfo(ClueInfo.FACTORY_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2FAC;
		}
		
		if (hasInfo(ClueInfo.COMMAND_FAC_DOUBLE)) {
//			return EnemyStrategy.TERRAN_1FAC_DOUBLE_1STAR;
//			return EnemyStrategy.TERRAN_1FAC_DOUBLE_ARMORY;
			return EnemyStrategy.TERRAN_1FAC_DOUBLE;
		}
		
		if (StrategyIdea.startStrategy == EnemyStrategy.TERRAN_NO_BARRACKS_DOUBLE
				|| StrategyIdea.startStrategy == EnemyStrategy.TERRAN_1BARRACKS_DOUBLE) {
			if (hasInfo(ClueInfo.FACTORY_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
			} else if (hasInfo(ClueInfo.BARRACK_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_BIONIC;
			} else {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
			}
		}

		if (StrategyIdea.startStrategy == EnemyStrategy.TERRAN_2BARRACKS
				|| StrategyIdea.startStrategy == EnemyStrategy.TERRAN_BBS) {
			return EnemyStrategy.TERRAN_BIONIC;
		} else {
			return EnemyStrategy.TERRAN_2FAC;
		}
	}

	private EnemyStrategy strategyPhase03() {
		int marineCount = InfoUtils.enemyNumUnits(UnitType.Terran_Marine);
		int vultureCount = InfoUtils.enemyNumUnits(UnitType.Terran_Vulture);
		int goliathCount = InfoUtils.enemyNumUnits(UnitType.Terran_Goliath);
//		int tankCount = InfoUtils.enemyNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
		
		int wraithCount = InfoUtils.enemyNumUnits(UnitType.Terran_Wraith);
//		int valkyrieCount = InfoUtils.enemyNumUnits(UnitType.Terran_Valkyrie);
//		int dropshipCount = InfoUtils.enemyNumUnits(UnitType.Terran_Dropship);
		int battleCount = InfoUtils.enemyNumUnits(UnitType.Terran_Wraith);
		
		int airUnitPoint = wraithCount + battleCount;
		if (airUnitPoint ==  0) {
			boolean isBionic = false;
			if (marineCount > vultureCount + goliathCount) {
				isBionic = true;
			}
			if (isBionic) {
				return EnemyStrategy.TERRAN_MECHANIC_VULTURE_TANK;
			} else {
				if (goliathCount == 0) {
					return EnemyStrategy.TERRAN_MECHANIC_WRAITH_TANK;
				} else {
					return EnemyStrategy.TERRAN_MECHANIC_VULTURE_TANK;
				}
			}
		} else {
			return EnemyStrategy.TERRAN_MECHANIC_GOLIATH_TANK;
		}
	}
}
