package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;
import prebot.strategy.manage.AirForceManager;

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
		
		if (hasAnyInfo(ClueInfo.FAST_THREE_WRAITH, ClueInfo.FAST_TWO_WRAITH)) {
			return EnemyStrategy.TERRAN_2STAR;
		} else if (hasAnyInfo(ClueInfo.FAST_ONE_WRAITH)) {
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
		
		if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(Feature.DOUBLE)) {
			if (hasInfo(ClueInfo.FACTORY_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
				
			} else if (hasInfo(ClueInfo.BARRACK_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_BIONIC;
				
			} else {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
			}
		}
		
		if (hasInfo(ClueInfo.STARPORT_TWO)) {
			return EnemyStrategy.TERRAN_2STAR;
		}
		
		if (hasInfo(ClueInfo.ACADEMY_FAST)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(ClueInfo.FACTORY_FAST_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_DOUBLE;
		}
		
		if (hasInfo(ClueInfo.STARPORT_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_1STAR;
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(Feature.BIONIC)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(ClueInfo.FACTORY_NOT_FOUND)) { // *
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasAnyInfo(ClueInfo.BARRACK_FASTEST_TWO, ClueInfo.BARRACK_FAST_TWO)) {
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
		
		int airUnitPoint = UnitUtils.enemyAirUnitPower();
		if (airUnitPoint ==  0) {
			boolean isBionic = false;
			if (marineCount > vultureCount + goliathCount) {
				isBionic = true;
			}
			if (!isBionic) {
				// 내 레이쓰 수가 0개 유지 상태, 적 골리앗이 1기 이하, 내 탱크가 10개 이상인 경우 역레이쓰 
				if (goliathCount <= 2 && StrategyIdea.wraithCount == 0) {
					int myTankCount = InfoUtils.myNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Tank_Mode);
					if (myTankCount >= 10) {
						AirForceManager.Instance().setAirForceWaiting();
					}
				}
			}
			return EnemyStrategy.TERRAN_MECHANIC_VULTURE_TANK;
			
		} else {
			return EnemyStrategy.TERRAN_MECHANIC_GOLIATH_TANK;
		}
	}
}
