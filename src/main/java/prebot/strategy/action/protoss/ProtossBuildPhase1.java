package prebot.strategy.action.protoss;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PositionRegion;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.action.RaceAction;
import prebot.strategy.constant.EnemyStrategy.Strategy;

public class ProtossBuildPhase1 extends RaceAction {

	private static final int PHASE1_END_SEC = 180; // TODO 일반적으로 사이버네틱코어가 완성되는 시간
	private static final int ERROR_RANGE_SEC = 5; // TODO 오차범위(초)

	private static final int FIRST_PYLON_SEC = 120; // TODO 정상빌드 1파일런 건설시작 (초)
	private static final int FIRST_GATE_SEC = 120; // TODO 정상빌드 1게이트 건설시작 (초)
	private static final int TWO_GATE_SECOND_GATE_SEC = 120; // TODO 2게이트빌드 2게이트 건설시작 (초)

	private static final int ONE_GATE_ZEALOT_CORE_SEC = 120; // TODO 1게이트빌드 질럿찍고 코어 건설시작 (초)
	private static final int ONE_GATE_NO_ZEALOT_CORE_SEC = 120; // TODO 1게이트빌드 질럿생략 코어 건설시작 (초)

	private static final int ONE_GATE_GAS_SEC = 120; // TODO 1게이트빌드 가스 건설시작 (초)
	private static final int TWO_GATE_GAS_SEC = 120; // TODO 2게이트빌드 가스 건설시작 (초)

	private static final int ONE_GATE_FAST_DRAGOON_RANGE_UPGRADE_SEC = 120; // TODO 1게이트빌드 패스트 드라군 사업 시작 (초)

	private static final int FORGE_FIRST_FORGE_SEC = 120; // TODO 선포지빌드 포지 건설시작. 여유있게 생더블 포지시작시간 (초)

	private static final int NATURAL_DOUBLE_NEXUS_SEC = 120; // TODO 생 더블넥 넥서스건설 시작 (초)
	private static final int FORGE_DOUBLE_NEXUS_SEC = 120; // TODO 포지 더블넥 넥서스건설 시작 (초)

	private boolean isOneGateBuild = false;
	private boolean isTwoGateBuild = false;
	private boolean isForgeFirstBuild = false;
	private boolean isDoubleNexus = false;
	private boolean isPlyonOnExpansion = false;

	private boolean nothingInEnemyBase = false;

	public ProtossBuildPhase1() {
		super(Race.Protoss, PHASE1_END_SEC);
		foundUnitTypes.add(UnitType.Protoss_Probe);
		foundUnitTypes.add(UnitType.Protoss_Pylon);
		foundUnitTypes.add(UnitType.Protoss_Zealot);
		foundUnitTypes.add(UnitType.Protoss_Nexus);
		foundUnitTypes.add(UnitType.Protoss_Gateway);
		foundUnitTypes.add(UnitType.Protoss_Forge);
		foundUnitTypes.add(UnitType.Protoss_Photon_Cannon);
		foundUnitTypes.add(UnitType.Protoss_Assimilator);
		foundUnitTypes.add(UnitType.Protoss_Cybernetics_Core);
	}

	/// 유닛 발견 맵을 분석한 정보
	@Override
	protected boolean analyse() {
		return analyseByNexus()
				|| analyseVisibleBuilding()
				|| analyseByPlyon();
		// analyseByCore(); // 2번째 오버로드 발견에 따른 정보획득
		// analyseByForge(); //
		// analyseByNexus();
	}

	@Override
	protected void expectBuild() {
		if (nothingInEnemyBase) {
			enemyBuildOfPhase = Strategy.PROTOSS_1GATE_CORE;
		} else if (isTwoGateBuild) {
			enemyBuildOfPhase = Strategy.PROTOSS_2GATE;
		} else if (isDoubleNexus) {
			enemyBuildOfPhase = Strategy.PROTOSS_DOUBLE;
		}
	}

	@Override
	protected Strategy finalExpect() {
		if (isPlyonOnExpansion && isForgeFirstBuild) {
			return Strategy.PROTOSS_DOUBLE;
		} else if (nothingInEnemyBase) {
			return Strategy.PROTOSS_2GATE;
		} else {
			return Strategy.PROTOSS_1GATE_CORE;
		}
	}

	private boolean analyseByNexus() {
		List<UnitInfo> nexusList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Nexus, new ArrayList<UnitInfo>());
		if (TimeUtils.elapsedFrames() < FORGE_DOUBLE_NEXUS_SEC) {
			if (nexusList.size() >= 1) {
				for (UnitInfo nexusInfo : nexusList) {
					PositionRegion nexusRegion = PositionUtils.positionToPositionRegion(nexusInfo.getLastPosition());
					if (nexusRegion == PositionRegion.ENEMY_FIRST_EXPANSION) {
						isDoubleNexus = true;
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean analyseVisibleBuilding() {
		if (InfoUtils.enemyBase() == null) {
			return false;
		}
		if (!Prebot.Broodwar.isVisible(InfoUtils.enemyBase().getTilePosition()) || !Prebot.Broodwar.isVisible(InfoUtils.enemyFirstExpansion().getTilePosition())) {
			return false;
		}

		List<UnitInfo> pylonList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Pylon, new ArrayList<UnitInfo>());
		List<UnitInfo> gateList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Gateway, new ArrayList<UnitInfo>());
		List<UnitInfo> forgeList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Forge, new ArrayList<UnitInfo>());
		List<UnitInfo> gasList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Assimilator, new ArrayList<UnitInfo>());
		List<UnitInfo> coreList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Cybernetics_Core, new ArrayList<UnitInfo>());

		if (TimeUtils.elapsedFrames() < TWO_GATE_SECOND_GATE_SEC) { // 투게이트 2번째 게이트 올라가는 시간
			if (coreList.size() >= 1 || gasList.size() >= 1) {
				isOneGateBuild = true;
				return true;
			} else if (gateList.size() >= 2) {
				isTwoGateBuild = true;
				return true;
			} else if (forgeList.size() >= 1) {
				isForgeFirstBuild = true;
			} else if (pylonList.size() == 0) {
				nothingInEnemyBase = true;
			}
		}
		return false;
	}

	private boolean analyseByPlyon() {
		List<UnitInfo> pylonList = enemyUnitInfoMap.getOrDefault(UnitType.Protoss_Pylon, new ArrayList<UnitInfo>());
		if (pylonList.isEmpty()) {
			return false;
		}

		UnitInfo firstSearchedPylon = enemyUnitInfoMap.get(UnitType.Protoss_Pylon).get(0);
		PositionRegion positionRegion = PositionUtils.positionToPositionRegion(firstSearchedPylon.getLastPosition());
		if (positionRegion == PositionRegion.ENEMY_BASE) {
			isPlyonOnExpansion = false;
		} else if (positionRegion == PositionRegion.ENEMY_BASE) {
			isPlyonOnExpansion = true;
		}
		return false;
	}

}
