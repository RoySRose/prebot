package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class TerranStrategist extends Strategist {
	
	public TerranStrategist() {
		super(UnitType.Terran_Factory);
	}

	@Override
	protected EnemyStrategy strategyPhase01() {
		if (hasInfo(Clue.ClueInfo.COMMAND_FASTEST_DOUBLE)) {
			return EnemyStrategy.TERRAN_NO_BARRACKS_DOUBLE;
		} else if (hasInfo(Clue.ClueInfo.COMMAND_FAST_DOUBLE)) {
			return EnemyStrategy.TERRAN_1BARRACKS_DOUBLE;
		}

		if (hasInfo(Clue.ClueInfo.BARRACK_FASTEST_TWO)) {
			return EnemyStrategy.TERRAN_BBS;
			
		} else if (hasInfo(Clue.ClueInfo.BARRACK_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
			
		} else if (hasAllInfo(Clue.ClueInfo.BARRACK_NOT_FOUND, Clue.ClueInfo.NO_REFINERY)) {
			return EnemyStrategy.TERRAN_BBS;
		}
		
		if (hasInfo(Clue.ClueInfo.ACADEMY_FAST)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasAnyType(Clue.ClueType.MEDIC, Clue.ClueType.FIREBAT)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		if (hasType(Clue.ClueType.FAST_MARINE)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
		}
		
		if (hasAnyInfo(Clue.ClueInfo.REFINERY_FAST, Clue.ClueInfo.REFINERY_LATE)) {
			if (hasAnyInfo(Clue.ClueInfo.FACTORY_NOT_FOUND)) {
				return EnemyStrategy.TERRAN_BIONIC;
			} else {
				return EnemyStrategy.TERRAN_MECHANIC; 
			}
		} else if (hasInfo(Clue.ClueInfo.NO_REFINERY)) {
			return EnemyStrategy.TERRAN_1BARRACKS_DOUBLE;
		}

		if (hasAnyInfo(Clue.ClueInfo.FAST_SIX_MARINE, Clue.ClueInfo.FAST_FOUR_MARINE)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
		}
		
		if (hasInfo(Clue.ClueInfo.BARRACK_TWO)) {
			return EnemyStrategy.TERRAN_2BARRACKS;
		}
		
		return EnemyStrategy.TERRAN_INIT;
	}

	@Override
	protected EnemyStrategy strategyPhase02() {
		if (hasInfo(Clue.ClueInfo.STARPORT_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2STAR;
		} else if (hasInfo(Clue.ClueInfo.STARPORT_FAST_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_1STAR;
		}
		
		if (hasAnyInfo(Clue.ClueInfo.FAST_THREE_WRAITH, Clue.ClueInfo.FAST_TWO_WRAITH)) {
			return EnemyStrategy.TERRAN_2STAR;
		} else if (hasAnyInfo(Clue.ClueInfo.FAST_ONE_WRAITH)) {
			if (hasInfo(Clue.ClueInfo.STARPORT_TWO)) {
				return EnemyStrategy.TERRAN_2STAR;
			} else {
				return EnemyStrategy.TERRAN_1FAC_1STAR;
			}
		}
		
		if (hasInfo(Clue.ClueInfo.FAST_THREE_TANK)) {
			return EnemyStrategy.TERRAN_2FAC; // TODO 탱크를 이용한 빠른 지상병력 공격 예상
		} else if (hasAnyInfo(Clue.ClueInfo.FAST_TWO_TANK, Clue.ClueInfo.FAST_ONE_TANK)) {
			if (hasType(Clue.ClueType.FAST_VULTURE)) {
				return EnemyStrategy.TERRAN_2FAC; // TODO 탱크를 이용한 빠른 지상병력 공격 예상
			}
		}
		
		if (hasInfo(Clue.ClueInfo.FACTORY_FAST_TWO)) {
			return EnemyStrategy.TERRAN_2FAC;
		}
		
		if (hasInfo(Clue.ClueInfo.COMMAND_FAC_DOUBLE)) {
			if (hasAnyType(Clue.ClueType.FAST_STARPORT, Clue.ClueType.FAST_WRAITH)) {
				return EnemyStrategy.TERRAN_1FAC_DOUBLE_1STAR;	
			} else if (hasAnyType(Clue.ClueType.FAST_ARMORY, Clue.ClueType.FAST_GOLIATH)) {
				return EnemyStrategy.TERRAN_1FAC_DOUBLE_ARMORY;
			} else {
				return EnemyStrategy.TERRAN_1FAC_DOUBLE;
			}
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(Feature.DOUBLE)) {
			if (hasInfo(Clue.ClueInfo.FACTORY_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
				
			} else if (hasInfo(Clue.ClueInfo.BARRACK_TWO)) {
				return EnemyStrategy.TERRAN_DOUBLE_BIONIC;
				
			} else {
				return EnemyStrategy.TERRAN_DOUBLE_MECHANIC;
			}
		}
		
		if (hasInfo(Clue.ClueInfo.STARPORT_TWO)) {
			return EnemyStrategy.TERRAN_2STAR;
		}
		
		if (hasInfo(Clue.ClueInfo.ACADEMY_FAST)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(Clue.ClueInfo.FACTORY_FAST_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_DOUBLE;
		}
		
		if (hasInfo(Clue.ClueInfo.STARPORT_ONE)) {
			return EnemyStrategy.TERRAN_1FAC_1STAR;
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(Feature.BIONIC)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}

		if (hasAnyInfo(Clue.ClueInfo.FAST_FOUR_MARINE, Clue.ClueInfo.FAST_SIX_MARINE)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasInfo(Clue.ClueInfo.FACTORY_NOT_FOUND)) { // *
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		if (hasAnyInfo(Clue.ClueInfo.BARRACK_FASTEST_TWO, Clue.ClueInfo.BARRACK_FAST_TWO)) {
			return EnemyStrategy.TERRAN_BIONIC;
		}
		
		return EnemyStrategy.TERRAN_2FAC;
	}

	@Override
	protected EnemyStrategy strategyPhase03() {
		int marineCount = InfoUtils.enemyNumUnits(UnitType.Terran_Marine);
		int vultureCount = InfoUtils.enemyNumUnits(UnitType.Terran_Vulture);
		int goliathCount = InfoUtils.enemyNumUnits(UnitType.Terran_Goliath);
		int tankCount = InfoUtils.enemyNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
		
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
//						AirForceManager.Instance().setAirForceWaiting();
						StrategyIdea.wraithCount = 5;
					}
				}
			}
			return EnemyStrategy.TERRAN_MECHANIC_VULTURE_TANK;
			
		} else {
			int groundPoint = marineCount + vultureCount + goliathCount + tankCount;
			if (airUnitPoint > groundPoint) {
				return EnemyStrategy.TERRAN_MECHANIC_GOL_GOL_TANK;
			} else {
				return EnemyStrategy.TERRAN_MECHANIC_GOLIATH_TANK;
			}
		}
	}
}
