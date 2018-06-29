package prebot.strategy.analyse;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.EnemyBuildTimer;

public class ProtossStrategist extends Strategist {

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
			int buildTimeExpect = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Protoss_Cybernetics_Core);
			return buildTimeExpect == CommonCode.UNKNOWN || TimeUtils.before(buildTimeExpect);
		}
		return false;
	}

	private boolean phase02() {
		return TimeUtils.before(10 * TimeUtils.MINUTE); // TODO 조정
	}

	private EnemyStrategy strategyPhase01() {
		if (hasType(ClueType.FAST_NEXSUS)) {
			if (hasInfo(ClueInfo.NEXSUS_FASTEST_DOUBLE)) {
				return EnemyStrategy.PROTOSS_DOUBLE;
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

	private EnemyStrategy strategyPhase02() {
		if (hasAnyInfo(ClueInfo.TEMPLAR_ARCH_FAST, ClueInfo.ADUN_FAST)) {
			if (hasInfo(ClueInfo.ROBO_FAST)) {
				return EnemyStrategy.PROTOSS_DARK_DROP;
			} else {
				return EnemyStrategy.PROTOSS_FAST_DARK;
			}
		}
		
		if (hasAnyInfo(ClueInfo.ROBO_FAST)) {
			if (hasInfo(ClueInfo.ROBO_SUPPORT_FAST)) {
				return EnemyStrategy.PROTOSS_ROBOTICS_REAVER;
			} else if (hasInfo(ClueInfo.OBSERVER_FAST)) {
				return EnemyStrategy.PROTOSS_ROBOTICS_OB_DRAGOON;
			} else {
				return EnemyStrategy.PROTOSS_DARK_DROP;
			}
		}
		
		if (hasAnyInfo(ClueInfo.STARGATE_ONEGATE_FAST)) {
			return EnemyStrategy.PROTOSS_STARGATE;
		}
		
		if (hasAnyInfo(ClueInfo.DRAGOON_RANGE_FAST)) {
			return EnemyStrategy.PROTOSS_FAST_DRAGOON;
		}
		
		// if 더블인가
		if (StrategyIdea.startStrategy == EnemyStrategy.PROTOSS_DOUBLE
				|| StrategyIdea.startStrategy == EnemyStrategy.PROTOSS_FORGE_DOUBLE
				|| StrategyIdea.startStrategy == EnemyStrategy.PROTOSS_GATE_DOUBLE) {
			if (hasAnyInfo(ClueInfo.STARGATE_DOUBLE_FAST)) {
				return EnemyStrategy.PROTOSS_DOUBLE_CARRIER;
			} else {
				return EnemyStrategy.PROTOSS_DOUBLE_GROUND;
			}
		}
		
		if (StrategyIdea.startStrategy == EnemyStrategy.PROTOSS_2GATE
				|| StrategyIdea.startStrategy == EnemyStrategy.PROTOSS_2GATE_CENTER) {
			if (hasType(ClueType.FAST_CORE)) {
				return EnemyStrategy.PROTOSS_FAST_DRAGOON;
			} else if (hasInfo(ClueInfo.NO_ASSIMILATOR)) {
				return EnemyStrategy.PROTOSS_HARDCORE_ZEALOT;
			}
		}
		
		return EnemyStrategy.PROTOSS_FAST_DARK;
	}

	private EnemyStrategy strategyPhase03() {
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
