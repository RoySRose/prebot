package prebot.strategy.analyse;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.util.TimeUtils;
import prebot.strategy.action.RaceAction;
import prebot.strategy.constant.EnemyStrategy;

public class ProtossBuildPhase1 extends RaceAction {
	
	private enum Clue {
		NO_GAS, LATE_GAS, FAST_GAS, NOT_DOUBLE_NEXSUS, FAST_NEXSUS,
		FAST_FORGE_IN_BASE, FAST_FORGE_IN_EXPANSION,
		NO_GATE, TWO_GATE, ONE_GATE, GATE_IN_BASE,
		NO_FORGE, FAST_CANNON_IN_BASE, FAST_CANNON_IN_EXPANSION, FAST_CANNON_WHERE
	}

	private static final int PHASE1_END_TIME = TimeUtils.timeToFrames(03, 30); // TODO 일반적으로 사이버네틱코어가 완성되는 시간

//	private static final int FIRST_PYLON_SEC = getTimeUtils.timeToFrames(01, 05);
	private static final int FIRST_GATE_TIME = TimeUtils.timeToFrames(01, 40);
	private static final int TWO_GATE_SECOND_GATE_TIME = TimeUtils.timeToFrames(02, 00);

	private static final int ONE_GATE_NO_ZEALOT_CORE_TIME = TimeUtils.timeToFrames(2, 00); // TODO 1게이트빌드 질럿생략 코어 건설시작 (초)
	private static final int ONE_GATE_ZEALOT_CORE_TIME = TimeUtils.timeToFrames(2, 20); // TODO 1게이트빌드 질럿찍고 코어 건설시작 (초)

	private static final int ONE_GATE_GAS_TIME = TimeUtils.timeToFrames(1, 55); // TODO 1게이트빌드 가스 건설시작 (초)
	private static final int TWO_GATE_GAS_TIME = TimeUtils.timeToFrames(3, 00); // TODO 2게이트빌드 가스 건설시작 (초)

	private static final int ONE_GATE_FAST_DRAGOON_RANGE_UPGRADE_TIME = TimeUtils.timeToFrames(3, 00); // TODO 1게이트빌드 패스트 드라군 사업 시작 (초)

	private static final int FORGE_FIRST_FORGE_TIME = TimeUtils.timeToFrames(01, 50); // TODO 선포지빌드 포지 건설시작. 여유있게 생더블 포지시작시간 (초)
	private static final int FORGE_FIRST_CANNON_TIME = TimeUtils.timeToFrames(02, 25); // TODO 선포지빌드 포지 건설시작. 여유있게 생더블 포지시작시간 (초)

	private static final int NATURAL_DOUBLE_NEXUS_TIME = TimeUtils.timeToFrames(2, 10); // TODO 생 더블넥 넥서스건설 시작 (초)
	private static final int GATE_OR_FORGE_DOUBLE_NEXUS_TIME = TimeUtils.timeToFrames(03, 00); // TODO 포지 더블넥 넥서스건설 시작 (초)

	public ProtossBuildPhase1() {
		super(Race.Protoss, PHASE1_END_TIME);
	}

	/// 유닛 발견 맵을 분석한 정보
	@Override
	protected void analyse() {
		if (unknownEnemyStrategy()) {
			analyseByNexus();
		}
		if (unknownEnemyStrategy()) {
			analyseByCore();
		}
		if (unknownEnemyStrategy()) {
			analyseByGate();
		}
		if (unknownEnemyStrategy()) {
			analyseByForge();
		}
		if (unknownEnemyStrategy()) {
			analyseByAssimilator();
		}
		if (unknownEnemyStrategy()) {
			analyseByCannon();
		}
		if (unknownEnemyStrategy()) {
			analyseByZealot();
		}
		if (unknownEnemyStrategy()) {
			analyseByNoBuilding();
		}
	}

	private void analyseByNexus() {
		FoundInfo nexusInExpansion = getFoundInfo(UnitType.Protoss_Nexus, RegionType.ENEMY_FIRST_EXPANSION);
		FoundInfo forgeAnywhere = getFoundInfo(UnitType.Protoss_Forge);
		FoundInfo gateAnywhere = getFoundInfo(UnitType.Protoss_Gateway);
		
		if (!nexusInExpansion.euiList.isEmpty()) {
			int nexsusStartFrame = defaultBuildStartLastCheck(nexusInExpansion, LastCheckType.FIRST_EXPANSION);
			if (nexsusStartFrame < NATURAL_DOUBLE_NEXUS_TIME) { // 생더블 확정
				setEnemyStrategy(EnemyStrategy.PROTOSS_DOUBLE, "very fast nexsus");
				
			} else if (nexsusStartFrame < GATE_OR_FORGE_DOUBLE_NEXUS_TIME) {
				if (!forgeAnywhere.euiList.isEmpty()) {
					setEnemyStrategy(EnemyStrategy.PROTOSS_FORGE_DOUBLE, "fast nexsus, forge");
				} else {
					if (!gateAnywhere.euiList.isEmpty()) {
						setEnemyStrategy(EnemyStrategy.PROTOSS_GATE_DOUBLE, "fast nexsus, gate");
					} else {
						addClue(Clue.FAST_NEXSUS);
					}
				}
			}
		} else {
			if (lastCheckFrame(LastCheckType.FIRST_EXPANSION) > GATE_OR_FORGE_DOUBLE_NEXUS_TIME) {
				addClue(Clue.NOT_DOUBLE_NEXSUS);
			}
		}
	}

	private void analyseByCore() {
		FoundInfo coreFoundInfo = getFoundInfo(UnitType.Protoss_Cybernetics_Core);
		if (!coreFoundInfo.euiList.isEmpty()) {
			int coreStartFrame = defaultBuildStartLastCheck(coreFoundInfo, LastCheckType.BASE);
			if (coreStartFrame < ONE_GATE_NO_ZEALOT_CORE_TIME) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_1GATE_CORE, "very fast core");
			} else if (coreStartFrame < ONE_GATE_ZEALOT_CORE_TIME) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_1GATE_CORE, "fast core");
			}
		}
	}

	private void analyseByGate() {
		FoundInfo gate = getFoundInfo(UnitType.Protoss_Gateway);
		// 2게이트웨이 가스통 여부
		
		if (!gate.euiList.isEmpty()) {
			if (gate.euiList.size() >= 2) { // 게이트 웨이 2개 이상
				removeClue(Clue.NO_GATE);
				removeClue(Clue.ONE_GATE);
				addClue(Clue.TWO_GATE);
				// 2게이트 빌드시작 시간
				int firstGateStart = defaultBuildStartCompelteJustBefore(gate.euiList.get(0));
				int secondGateStart = defaultBuildStartCompelteJustBefore(gate.euiList.get(1));
				
				if (firstGateStart < TWO_GATE_SECOND_GATE_TIME && secondGateStart < TWO_GATE_SECOND_GATE_TIME) {
					setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "fast 2 gateway");
				} else {
					if (containsAll(Clue.LATE_GAS)) {
						setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "2 gateway, late gas");
						
					} else if (containsAll(Clue.NO_GAS)) {
						setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "2 gateway, no gas");
					}
				}
			
			} else if (gate.euiList.size() == 1) { // 게이트 웨이 1개
				removeClue(Clue.NO_GATE);
				addClue(Clue.ONE_GATE);
			}
		} else {
			if (lastCheckFrame(LastCheckType.BASE) > FIRST_GATE_TIME) {
				addClue(Clue.NO_GATE);
			}
		}
	}
	
	private void analyseByForge() {
		FoundInfo forgeInExpansion = getFoundInfo(UnitType.Protoss_Forge, RegionType.ENEMY_FIRST_EXPANSION);
		if (!forgeInExpansion.euiList.isEmpty()) {
			int forgeStartFrame = defaultBuildStartCompelteJustBefore(forgeInExpansion);
			if (forgeStartFrame < FORGE_FIRST_FORGE_TIME) {
				addClue(Clue.FAST_FORGE_IN_EXPANSION);
			}
		} else {
			FoundInfo forge = getFoundInfo(UnitType.Protoss_Forge);
			if (!forge.euiList.isEmpty()) {
				int forgeStartFrame = defaultBuildStartCompelteJustBefore(forge);
				if (forgeStartFrame < FORGE_FIRST_FORGE_TIME) {
					addClue(Clue.FAST_FORGE_IN_BASE);
				}
			} else {
				if (lastCheckFrame(LastCheckType.GAS) < FORGE_FIRST_FORGE_TIME) {
					addClue(Clue.NO_FORGE);
				}
			}
		}
	}
	
	private void analyseByAssimilator() {
		FoundInfo gas = getFoundInfo(UnitType.Protoss_Assimilator);
		if (!gas.euiList.isEmpty()) {
			int gasStartFrame = defaultBuildStartCompelteJustBefore(gas);
			if (gasStartFrame < ONE_GATE_GAS_TIME) {
				removeClue(Clue.NO_GAS);
			} else {
				addClue(Clue.LATE_GAS);
			}
		} else {
			if (lastCheckFrame(LastCheckType.GAS) < ONE_GATE_GAS_TIME) {
				addClue(Clue.NO_GAS);
			}
		}
	}

	private void analyseByCannon() {
		FoundInfo cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon, RegionType.ENEMY_FIRST_EXPANSION);
		if (!cannonFoundInfo.euiList.isEmpty()) {
			int cannonStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
			if (cannonStartFrame < FORGE_FIRST_CANNON_TIME) {
				addClue(Clue.FAST_CANNON_IN_EXPANSION);
			}
		} else {
			cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon, RegionType.ENEMY_BASE);
			if (!cannonFoundInfo.euiList.isEmpty()) {
				int cannonStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
				if (cannonStartFrame < FORGE_FIRST_CANNON_TIME) {
					addClue(Clue.FAST_CANNON_IN_BASE);
				}
			} else {
				cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon);
				if (!cannonFoundInfo.euiList.isEmpty()) {
					int cannonStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
					if (cannonStartFrame < FORGE_FIRST_CANNON_TIME) {
						addClue(Clue.FAST_CANNON_WHERE);
					}
				}
			}
		}
	}

	private void analyseByZealot() {
		FoundInfo zealotFoundInfo = getFoundInfo(UnitType.Protoss_Zealot);
		if (!zealotFoundInfo.euiList.isEmpty()) {
			int threeZealotFrame = TWO_GATE_SECOND_GATE_TIME + UnitType.Protoss_Zealot.buildTime();
			int threeZealotInMyRegionFrame = threeZealotFrame + baseToBaseFrame(UnitType.Protoss_Zealot);
			if (euiCountBefore(zealotFoundInfo.euiList, threeZealotFrame) >= 3) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "3 zealots in enemy base");
			} else if (euiCountBeforeWhere(zealotFoundInfo.euiList, threeZealotInMyRegionFrame, RegionType.ENEMY_FIRST_EXPANSION, RegionType.ETC, RegionType.MY_BASE) >= 3) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "3 zealots");
			}
		}
		
		FoundInfo zealotInBase = getFoundInfo(UnitType.Protoss_Zealot, RegionType.ENEMY_BASE);
		if (!zealotFoundInfo.euiList.isEmpty()) {
			int oneZealotFrame = FIRST_GATE_TIME + UnitType.Protoss_Zealot.buildTime();
			if (euiCountBefore(zealotInBase.euiList, oneZealotFrame) >= 1) {
				addClue(Clue.GATE_IN_BASE);
			}
		}
	}
	
	private void analyseByNoBuilding() {
		
		EnemyStrategy enemyStrategyExpect = EnemyStrategy.UNKNOWN;
		if (containsAll(Clue.FAST_NEXSUS) && !containsAny(Clue.FAST_GAS)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_GATE_DOUBLE;

		} else if (containsAll(Clue.ONE_GATE, Clue.LATE_GAS) && !containsAny(Clue.FAST_GAS)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_2GATE;

		} else if (containsAll(Clue.ONE_GATE, Clue.NO_GAS) && !containsAny(Clue.FAST_GAS)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_2GATE;

		} else if (containsAll(Clue.NO_GATE, Clue.NO_FORGE, Clue.NO_GAS) && !containsAny(Clue.GATE_IN_BASE, Clue.FAST_GAS)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_2GATE_CENTER;

		} else if (containsAll(Clue.FAST_FORGE_IN_EXPANSION, Clue.NO_GAS) && !containsAny(Clue.NOT_DOUBLE_NEXSUS, Clue.FAST_GAS)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DOUBLE;

		} else if (containsAll(Clue.FAST_FORGE_IN_BASE)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DEFENSE;
			
		} else if (containsAll(Clue.FAST_CANNON_IN_EXPANSION, Clue.NO_GAS) && !containsAny(Clue.NOT_DOUBLE_NEXSUS, Clue.FAST_GAS)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DOUBLE;
			
		} else if (containsAll(Clue.FAST_CANNON_IN_BASE)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DEFENSE;
			
		} else if (containsAll(Clue.FAST_CANNON_WHERE)) {
			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_CANNON_RUSH;
		}
		
		setEnemyStrategyExpect(enemyStrategyExpect);
	}
	
}
