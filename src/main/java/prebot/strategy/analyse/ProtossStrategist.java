package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class ProtossStrategist extends Strategist {
	
	public ProtossStrategist() {
		super(UnitType.Protoss_Cybernetics_Core);
	}

	@Override
	protected EnemyStrategy strategyPhase01() {
		if (hasType(ClueType.FAST_NEXSUS)) {
			if (hasInfo(ClueInfo.NEXSUS_FASTEST_DOUBLE)) {
				if (hasAnyType(ClueType.FAST_FORGE, ClueType.FAST_CANNON)) {
					return EnemyStrategy.PROTOSS_FORGE_DOUBLE;
				} else {
					return EnemyStrategy.PROTOSS_DOUBLE;
				}
				
			} else if (hasInfo(ClueInfo.NEXSUS_FAST_DOUBLE)) {
				if (hasAnyType(ClueType.FAST_FORGE, ClueType.FAST_CANNON)) {
					return EnemyStrategy.PROTOSS_FORGE_DOUBLE;
				} else {
					return EnemyStrategy.PROTOSS_GATE_DOUBLE;
				}
			}
		}
		
		if (hasInfo(ClueInfo.CORE_FAST)) {
			return EnemyStrategy.PROTOSS_1GATE_CORE;
		}
		
		if (hasInfo(ClueInfo.GATE_FAST_TWO)) {
			return EnemyStrategy.PROTOSS_2GATE;
		}
		
		// 2게이트 예측
		if (hasInfo(ClueInfo.GATE_TWO) && hasAnyInfo(ClueInfo.ASSIMILATOR_LATE, ClueInfo.NO_ASSIMILATOR)) {
			return EnemyStrategy.PROTOSS_2GATE;
		}
		if (hasInfo(ClueInfo.GATE_FAST_ONE) && hasAnyInfo(ClueInfo.ASSIMILATOR_LATE, ClueInfo.NO_ASSIMILATOR)) {
			return EnemyStrategy.PROTOSS_2GATE;
		}
		if (hasAllInfo(ClueInfo.GATE_NOT_FOUND, ClueInfo.NO_ASSIMILATOR)) {
			return EnemyStrategy.PROTOSS_2GATE_CENTER;
		}

		// 포지 더블 예측
		if (hasAllInfo(ClueInfo.FORGE_FAST_IN_EXPANSION, ClueInfo.NO_ASSIMILATOR)) {
			return EnemyStrategy.PROTOSS_FORGE_DOUBLE;
		}
		
		// 본진 캐논으로 시작하는 초보 프로토스
		if (hasInfo(ClueInfo.FORGE_FAST_IN_BASE)) {
			return EnemyStrategy.PROTOSS_FORGE_DEFENSE;
		}
		
		// 날빌 예상
		if (hasInfo(ClueInfo.CANNON_FAST_SOMEWHERE)) {
			return EnemyStrategy.PROTOSS_FORGE_CANNON_RUSH;
		}
		
		return EnemyStrategy.PROTOSS_INIT;
	}
	
	@Override
	protected EnemyStrategy strategyPhase02() {
		if (hasAnyInfo(ClueInfo.TEMPLAR_ARCH_FAST, ClueInfo.ADUN_FAST, ClueInfo.FAST_DARK)) {
			if (hasAnyInfo(ClueInfo.ROBO_FAST, ClueInfo.FAST_SHUTTLE)) {
				return EnemyStrategy.PROTOSS_DARK_DROP;
			} else {
				return EnemyStrategy.PROTOSS_FAST_DARK;
			}
		}
		
		if (hasAnyInfo(ClueInfo.ROBO_FAST, ClueInfo.FAST_SHUTTLE)) {
			if (hasAnyInfo(ClueInfo.ROBO_SUPPORT_FAST, ClueInfo.FAST_REAVER)) {
				return EnemyStrategy.PROTOSS_ROBOTICS_REAVER;
			} else if (hasAnyInfo(ClueInfo.OBSERVERTORY_FAST, ClueInfo.FAST_OBSERVER)) {
				return EnemyStrategy.PROTOSS_ROBOTICS_OB_DRAGOON;
			} else {
				return EnemyStrategy.PROTOSS_DARK_DROP;
			}
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(Feature.DOUBLE) || hasAnyInfo(ClueInfo.NEXSUS_FAST_DOUBLE, ClueInfo.NEXSUS_FASTEST_DOUBLE)) {
			if (hasInfo(ClueInfo.FAST_FLEET_BEACON)) {
				return EnemyStrategy.PROTOSS_DOUBLE_CARRIER;
			} else if (hasAnyInfo(ClueInfo.STARGATE_DOUBLE_FAST)) {
				return EnemyStrategy.PROTOSS_DOUBLE_CARRIER;
			} else if (hasAnyType(ClueType.FAST_FORGE, ClueType.FAST_CANNON)) {
				return EnemyStrategy.PROTOSS_DOUBLE_CARRIER;
			} else {
				return EnemyStrategy.PROTOSS_DOUBLE_GROUND;
			}
		}
		
		if (StrategyIdea.startStrategy.buildTimeMap.featureEnabled(Feature.TWOGATE)) {
			if (hasType(ClueType.FAST_CORE)) {
				return EnemyStrategy.PROTOSS_FAST_DRAGOON;
			} else {
				return EnemyStrategy.PROTOSS_HARDCORE_ZEALOT;
			}
		}
		
		if (hasAnyInfo(ClueInfo.STARGATE_ONEGATE_FAST, ClueInfo.FAST_FLEET_BEACON)) {
			return EnemyStrategy.PROTOSS_STARGATE;
		}
		
		if (hasAnyInfo(ClueInfo.DRAGOON_RANGE_FAST)) {
			return EnemyStrategy.PROTOSS_FAST_DRAGOON;
		}
		
		if (hasAnyInfo(ClueInfo.NO_ASSIMILATOR, ClueInfo.ASSIMILATOR_LATE)) {
			if (hasInfo(ClueInfo.NEXSUS_NOT_DOUBLE)) {
				if (hasInfo(ClueInfo.ASSIMILATOR_LATE)) {
					return EnemyStrategy.PROTOSS_FAST_DRAGOON;
				} else if (hasInfo(ClueInfo.NO_ASSIMILATOR)) {
					return EnemyStrategy.PROTOSS_HARDCORE_ZEALOT;
				}
			} else if (hasType(ClueType.FAST_CORE)) {
				return EnemyStrategy.PROTOSS_FAST_DRAGOON;
			} else if (hasAnyInfo(ClueInfo.GATE_FAST_TWO, ClueInfo.GATE_TWO)) {
				return EnemyStrategy.PROTOSS_HARDCORE_ZEALOT;
			} else {
				return EnemyStrategy.PROTOSS_DOUBLE_GROUND;
			}
		}
		
		if (hasInfo(ClueInfo.FAST_THREE_ZEALOT)) {
			return EnemyStrategy.PROTOSS_HARDCORE_ZEALOT;	
		}
		
		if (hasAnyInfo(ClueInfo.GATE_FAST_TWO)) {
			return EnemyStrategy.PROTOSS_FAST_DRAGOON;
		}
		
		return EnemyStrategy.PROTOSS_FAST_DARK;
	}
	
	@Override
	protected EnemyStrategy strategyPhase03() {
		int zealotCount = InfoUtils.enemyNumUnits(UnitType.Protoss_Zealot);
		int dragoonCount = InfoUtils.enemyNumUnits(UnitType.Protoss_Dragoon);
		int darkCount = InfoUtils.enemyNumUnits(UnitType.Protoss_Dark_Templar);
		int highCount = InfoUtils.enemyNumUnits(UnitType.Protoss_High_Templar);
		int archonCount = InfoUtils.enemyNumUnits(UnitType.Protoss_Archon);

		boolean carrierExist = UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Fleet_Beacon, UnitType.Protoss_Carrier);
		if (carrierExist) {
			int groundUnitPoint = zealotCount + dragoonCount + darkCount + highCount + archonCount;
			if (groundUnitPoint < 10) {
				return EnemyStrategy.PROTOSS_PROTOSS_AIR3;
			} else {
				return EnemyStrategy.PROTOSS_PROTOSS_AIR2;
			}
		}
		
		boolean airUnitExsit = UnitUtils.enemyUnitDiscovered(
				UnitType.Protoss_Arbiter_Tribunal, UnitType.Protoss_Stargate, UnitType.Protoss_Arbiter, UnitType.Protoss_Scout);
		
		if (airUnitExsit) {
			return EnemyStrategy.PROTOSS_PROTOSS_AIR1;
		} else {
			return EnemyStrategy.PROTOSS_GROUND;
		}
	}
}
