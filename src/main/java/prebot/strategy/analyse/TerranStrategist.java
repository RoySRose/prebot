package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;

public class TerranStrategist extends Strategist {
	
	public TerranStrategist() {
		super(UnitType.Terran_Factory);
	}

	@Override
	protected EnemyStrategy strategyPhase01() {
		if (hasInfo(ClueInfo.COMMAND_FASTEST_DOUBLE)) {
			return EnemyStrategy.TERRAN_NO_BARRACKS_DOUBLE;
		} else if (hasInfo(ClueInfo.COMMAND_FAST_DOUBLE)) {
			return EnemyStrategy.TERRAN_1BARRACKS_DOUBLE;
		}

		if (hasInfo(ClueInfo.BARRACK_FASTEST_TWO)) {
			return EnemyStrategy.TERRAN_BBS;
			
		} else if (hasInfo(ClueInfo.BARRACK_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
			
		} else if (hasAllInfo(ClueInfo.BARRACK_NOT_FOUND, ClueInfo.NO_REFINERY)) {
			return EnemyStrategy.TERRAN_BBS;
		}
		
		if (hasInfo(ClueInfo.ACADEMY_FAST)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasAnyType(ClueType.MEDIC, ClueType.FIREBAT)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasAnyInfo(ClueInfo.REFINERY_FAST, ClueInfo.REFINERY_LATE)) {
			if (hasAnyInfo(ClueInfo.FACTORY_NOT_FOUND)) {
				return EnemyStrategy.TERRAN_BIONIC;
			} else {
				return EnemyStrategy.TERRAN_MECHANIC; 
			}
		} else if (hasInfo(ClueInfo.NO_REFINERY)) {
			return EnemyStrategy.TERRAN_1BARRACKS_DOUBLE;
		}

		if (hasAnyInfo(ClueInfo.FAST_SIX_MARINE, ClueInfo.FAST_FOUR_MARINE)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
		}
		
		if (hasInfo(ClueInfo.BARRACK_TWO)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
		}
		
		return EnemyStrategy.TERRAN_INIT;
	}

	@Override
	protected EnemyStrategy strategyPhase02() {
		if (hasInfo(ClueInfo.STARPORT_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2STAR;
		} else if (hasInfo(ClueInfo.STARPORT_FAST_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_1STAR;
		}
		
		if (hasInfo(ClueInfo.FAST_THREE_WRAITH)) {
			return EnemyStrategy.TERRAN_2STAR;
		} else if (hasAnyInfo(ClueInfo.FAST_TWO_WRAITH, ClueInfo.FAST_ONE_WRAITH)) {
			if (hasInfo(ClueInfo.STARPORT_TWO)) {
				return EnemyStrategy.TERRAN_2STAR;
			} else {
				return EnemyStrategy.TERRAN_1FAC_1STAR;
			}
		}
		
		if (hasInfo(ClueInfo.FAST_THREE_TANK)) {
			return EnemyStrategy.TERRAN_2FAC; // TODO 탱크를 이용한 빠른 지상병력 공격 예상
		} else if (hasAnyInfo(ClueInfo.FAST_TWO_TANK, ClueInfo.FAST_ONE_TANK)) {
			if (hasType(ClueType.FAST_VULTURE)) {
				return EnemyStrategy.TERRAN_2FAC; // TODO 탱크를 이용한 빠른 지상병력 공격 예상
			}
		}
		
		if (hasInfo(ClueInfo.FACTORY_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2FAC;
		}
		
		if (hasInfo(ClueInfo.COMMAND_FAC_DOUBLE)) {
			if (hasAnyType(ClueType.FAST_STARPORT, ClueType.FAST_WRAITH)) {
				return EnemyStrategy.TERRAN_1FAC_DOUBLE_1STAR;	
			} else if (hasAnyType(ClueType.FAST_ARMORY, ClueType.FAST_GOLIATH)) {
				return EnemyStrategy.TERRAN_1FAC_DOUBLE_ARMORY;
			} else {
				return EnemyStrategy.TERRAN_1FAC_DOUBLE;
			}
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.isDouble()) {
			if (hasInfo(ClueInfo.FACTORY_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
				
			} else if (hasInfo(ClueInfo.BARRACK_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_BIONIC;
				
			} else {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
			}
		}
		
		if (hasInfo(ClueInfo.FACTORY_FAST_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_DOUBLE;
		}
		
		if (hasInfo(ClueInfo.ACADEMY_FAST)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(ClueInfo.STARPORT_TWO)) {
			return EnemyStrategy.TERRAN_2STAR;
		}
		
		if (hasInfo(ClueInfo.STARPORT_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_1STAR;
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.isBionic()) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(ClueInfo.FACTORY_NOT_FOUND)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		return EnemyStrategy.TERRAN_2FAC;
	}

	@Override
	protected EnemyStrategy strategyPhase03() {
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
