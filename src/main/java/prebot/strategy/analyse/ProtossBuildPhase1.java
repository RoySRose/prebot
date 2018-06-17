package prebot.strategy.analyse;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.util.TimeUtils;
import prebot.strategy.action.RaceAction;
import prebot.strategy.constant.EnemyStrategy;

public class ProtossBuildPhase1 extends RaceAction {

	private static final int PHASE1_END_SEC = 180 * TimeUtils.SECOND; // TODO 일반적으로 사이버네틱코어가 완성되는 시간

	private static final int FIRST_PYLON_SEC = 120 * TimeUtils.SECOND; // TODO 정상빌드 1파일런 건설시작 (초)
	private static final int FIRST_GATE_SEC = 120 * TimeUtils.SECOND; // TODO 정상빌드 1게이트 건설시작 (초)
	private static final int TWO_GATE_SECOND_GATE_SEC = 120 * TimeUtils.SECOND; // TODO 2게이트빌드 2게이트 건설시작 (초)

	private static final int ONE_GATE_NO_ZEALOT_CORE_SEC = 120 * TimeUtils.SECOND; // TODO 1게이트빌드 질럿생략 코어 건설시작 (초)
	private static final int ONE_GATE_ZEALOT_CORE_SEC = 120 * TimeUtils.SECOND; // TODO 1게이트빌드 질럿찍고 코어 건설시작 (초)

	private static final int ONE_GATE_GAS_SEC = 120 * TimeUtils.SECOND; // TODO 1게이트빌드 가스 건설시작 (초)
	private static final int TWO_GATE_GAS_SEC = 120 * TimeUtils.SECOND; // TODO 2게이트빌드 가스 건설시작 (초)

	private static final int ONE_GATE_FAST_DRAGOON_RANGE_UPGRADE_SEC = 120 * TimeUtils.SECOND; // TODO 1게이트빌드 패스트 드라군 사업 시작 (초)

	private static final int FORGE_FIRST_FORGE_SEC = 120 * TimeUtils.SECOND; // TODO 선포지빌드 포지 건설시작. 여유있게 생더블 포지시작시간 (초)
	private static final int FORGE_FIRST_CANNON_SEC = 120 * TimeUtils.SECOND; // TODO 선포지빌드 포지 건설시작. 여유있게 생더블 포지시작시간 (초)

	private static final int NATURAL_DOUBLE_NEXUS_SEC = 120 * TimeUtils.SECOND; // TODO 생 더블넥 넥서스건설 시작 (초)
	private static final int GATE_OR_FORGE_DOUBLE_NEXUS_SEC = 120 * TimeUtils.SECOND; // TODO 포지 더블넥 넥서스건설 시작 (초)
	private static final int NOT_DOUBLE_NEXUS_SEC = 120 * TimeUtils.SECOND; // TODO 포지 더블넥 넥서스건설 시작 (초)

	private boolean notDoubleNexsus = false;

	public ProtossBuildPhase1() {
		super(Race.Protoss, PHASE1_END_SEC);
		setUnitTypes(UnitType.Protoss_Probe, UnitType.Protoss_Pylon, UnitType.Protoss_Zealot, UnitType.Protoss_Nexus, UnitType.Protoss_Gateway, UnitType.Protoss_Forge,
				UnitType.Protoss_Photon_Cannon, UnitType.Protoss_Assimilator, UnitType.Protoss_Cybernetics_Core);
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
			analyseByGateAndForge();
		}
		if (unknownEnemyStrategy()) {
			analyseByCannon();
		}
		if (unknownEnemyStrategy()) {
			analyseByZealot();
		}
	}

	private void analyseByNexus() {
		if (notDoubleNexsus) {
			return;
		}
		
		FoundInfo nexusFoundInfo = getFoundInfo(UnitType.Protoss_Nexus, RegionType.ENEMY_FIRST_EXPANSION);
		if (!nexusFoundInfo.euiList.isEmpty()) {
			int nexsusStartFrame = defaultBuildStartLastCheck(nexusFoundInfo, LastCheckType.FIRST_EXPANSION);
			
			if (nexsusStartFrame < NATURAL_DOUBLE_NEXUS_SEC) { // 생더블 확정
				setEnemyStrategy(EnemyStrategy.PROTOSS_DOUBLE, "생더블 시간 이내 넥서스 건설");
				
			} else if (nexsusStartFrame < GATE_OR_FORGE_DOUBLE_NEXUS_SEC) {
				FoundInfo forgeFoundInfo = getFoundInfo(UnitType.Protoss_Forge);
				if (!forgeFoundInfo.euiList.isEmpty()) {
					setEnemyStrategy(EnemyStrategy.PROTOSS_FORGE_DOUBLE, "더블 시간 이내 넥서스 건설 / 포지발견");
				} else {
					FoundInfo gateFoundInfo = getFoundInfo(UnitType.Protoss_Gateway);
					if (!gateFoundInfo.euiList.isEmpty()) {
						setEnemyStrategy(EnemyStrategy.PROTOSS_GATE_DOUBLE, "더블 시간 이내 넥서스 건설 / 게이트발견");
					} else {
						setEnemyStrategyExpect(EnemyStrategy.PROTOSS_GATE_DOUBLE);
					}
				}
			}
			
		} else {
			if (lastCheckFrameFirstExpansion < NOT_DOUBLE_NEXUS_SEC) {
				notDoubleNexsus = true;
			}
		}
	}

	private void analyseByCore() {
		FoundInfo coreFoundInfo = getFoundInfo(UnitType.Protoss_Cybernetics_Core);
		if (!coreFoundInfo.euiList.isEmpty()) {
			int coreStartFrame = defaultBuildStartLastCheck(coreFoundInfo, LastCheckType.BASE);
			if (coreStartFrame < ONE_GATE_NO_ZEALOT_CORE_SEC) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_1GATE_CORE, "코어 시간 이내 코어 건설");
			} else if (coreStartFrame < ONE_GATE_ZEALOT_CORE_SEC) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_1GATE_CORE, "1질럿 코어 시간 이내 코어 건설");
			}
		}
	}

	private void analyseByGateAndForge() {
		FoundInfo gateFoundInfo = getFoundInfo(UnitType.Protoss_Gateway);
		if (!gateFoundInfo.euiList.isEmpty()) {
			// 게이트 웨이 2개 이상
			if (gateFoundInfo.euiList.size() >= 2) {
				// 2게이트 빌드시작 시간
				int firstGateStart = defaultBuildStartCompelteJustBefore(gateFoundInfo.euiList.get(0));
				int secondGateStart = defaultBuildStartCompelteJustBefore(gateFoundInfo.euiList.get(1));
				
				if (firstGateStart < TWO_GATE_SECOND_GATE_SEC && secondGateStart < TWO_GATE_SECOND_GATE_SEC) {
					setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "2게이트 빌드시작 시간"
							, "gateway1=" + TimeUtils.framesToSeconds(firstGateStart) + "초"
							, "gateway2=" + TimeUtils.framesToSeconds(secondGateStart) + "초");
				} else {
					// 2게이트웨이 가스통 여부
					FoundInfo gasFoundInfo = getFoundInfo(UnitType.Protoss_Assimilator);
					if (!gasFoundInfo.euiList.isEmpty()) {
						int gasStartFrame = defaultBuildStartCompelteJustBefore(gasFoundInfo);
						if (gasStartFrame > ONE_GATE_GAS_SEC) {
							setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "2게이트 / 늦은가스 (가스빌드시간이 1게이트 가스시간 초과)");
						}
					} else {
						if (lastCheckFrameGas < ONE_GATE_GAS_SEC) {
							setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE , "2게이트 / 가스없음 (정찰시간이 1게이트 가스시간 초과)");
						}
					}
				}
			
			// 게이트 웨이 1개
			} else if (gateFoundInfo.euiList.size() == 1) {
				FoundInfo gasFoundInfo = getFoundInfo(UnitType.Protoss_Assimilator);
				if (!gasFoundInfo.euiList.isEmpty()) {
					int gasStartFrame = defaultBuildStartCompelteJustBefore(gasFoundInfo);
					if (gasStartFrame < ONE_GATE_GAS_SEC) {
						setEnemyStrategyExpect(EnemyStrategy.PROTOSS_1GATE_CORE, "1게이트 / 빠른가스 (1게이트 가스시간 이내)");
					}
				} else {
					if (lastCheckFrameGas > ONE_GATE_GAS_SEC) {
						setEnemyStrategyExpect(EnemyStrategy.PROTOSS_2GATE , "1게이트 / 가스가 너무 늦음 (1게이트 가스시간 초과)");
					}
				}
			}
			
		} else {
			FoundInfo forgeFoundInfo = getFoundInfo(UnitType.Protoss_Forge, RegionType.ENEMY_FIRST_EXPANSION);
			if (!forgeFoundInfo.euiList.isEmpty()) {
				int forgeStartFrame = defaultBuildStartCompelteJustBefore(forgeFoundInfo);
				if (forgeStartFrame < FORGE_FIRST_FORGE_SEC) {
					setEnemyStrategyExpect(EnemyStrategy.PROTOSS_FORGE_DOUBLE , "빠른 앞마당 포지");
				}
				
			} else {
				forgeFoundInfo = getFoundInfo(UnitType.Protoss_Forge);
				if (!forgeFoundInfo.euiList.isEmpty()) {
					int forgeStartFrame = defaultBuildStartCompelteJustBefore(forgeFoundInfo);
					if (forgeStartFrame < FORGE_FIRST_FORGE_SEC) {
						setEnemyStrategyExpect(EnemyStrategy.PROTOSS_FORGE_DEFENSE , "포지 방어");
					}
				} else {
					if (lastCheckFrameBase > FIRST_GATE_SEC) {
						setEnemyStrategyExpect(EnemyStrategy.PROTOSS_2GATE_CENTER , "본진 게이트없음");
					}
				}
			}
		}
	}

	private void analyseByCannon() {
		FoundInfo cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon, RegionType.ENEMY_FIRST_EXPANSION);
		if (!cannonFoundInfo.euiList.isEmpty()) {
			int forgeStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
			if (forgeStartFrame < FORGE_FIRST_CANNON_SEC) {
				setEnemyStrategyExpect(EnemyStrategy.PROTOSS_FORGE_DOUBLE , "선포지 앞마당 캐논");
			}
		} else {
			cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon, RegionType.ENEMY_BASE);
			if (!cannonFoundInfo.euiList.isEmpty()) {
				int forgeStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
				if (forgeStartFrame < FORGE_FIRST_CANNON_SEC) {
					setEnemyStrategyExpect(EnemyStrategy.PROTOSS_FORGE_DEFENSE , "캐논본진 방어");
				}
			} else {
				cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon);
				if (!cannonFoundInfo.euiList.isEmpty()) {
					int forgeStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
					if (forgeStartFrame < FORGE_FIRST_CANNON_SEC) {
						setEnemyStrategyExpect(EnemyStrategy.PROTOSS_FORGE_CANNON_RUSH, "캐논을 어디에 짓는거지");
					}
				}
			}
		}
	}

	private void analyseByZealot() {
		FoundInfo zealotFoundInfo = getFoundInfo(UnitType.Protoss_Zealot);
		if (!zealotFoundInfo.euiList.isEmpty()) {
			int threeZealotFrame = TWO_GATE_SECOND_GATE_SEC + UnitType.Protoss_Zealot.buildTime();
			int threeZealotInMyRegionFrame = threeZealotFrame + baseToBaseFrame(UnitType.Protoss_Zealot);
			if (euiCountBefore(zealotFoundInfo.euiList, threeZealotFrame) >= 3) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "빠른 3질럿 (적진에서 발견)");
			} else if (euiCountBeforeWhere(zealotFoundInfo.euiList, threeZealotInMyRegionFrame, RegionType.ENEMY_FIRST_EXPANSION, RegionType.ETC, RegionType.MY_BASE) >= 3) {
				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "빠른 3질럿 (바깥에서 발견)");
			}
		}
	}
}
