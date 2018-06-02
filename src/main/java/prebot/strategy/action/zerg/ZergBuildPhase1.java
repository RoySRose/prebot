package prebot.strategy.action.zerg;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.action.RaceAction;
import prebot.strategy.constant.StrategyConfig;
import prebot.strategy.constant.StrategyConfig.EnemyBuild;

public class ZergBuildPhase1 extends RaceAction {

	private static final int PHASE1_END_SEC = 180; // TODO 일반적인 3해처리가 건설 시작되는 시간 (초)
	private static final int ERROR_RANGE_SEC = 5; // TODO 오차범위(초)

	private static final int POOL_4DRONE_SEC = 120; // TODO 스포닝풀 건설시작 - 4드론 (초)
	private static final int POOL_5DRONE_SEC = 120; // TODO 스포닝풀 건설시작 - 5드론 (초)
	private static final int POOL_9DRONE_SEC = 120; // TODO 스포닝풀 건설시작 - 9드론 (초)
	private static final int POOL_12DRONE_SEC = 120; // TODO 스포닝풀 건설시작 - 12드론 스포닝풀(초)
	private static final int POOL_2HATCH_SEC = 120; // TODO 스포닝풀 건설시작 - 12드론 2해처리 (초)

	private static final int HATCH_2HATCH_SEC = 120; // TODO 투해처리 건설시작 (초)
	private static final int HATCH_3HATCH_SEC = 120; // TODO 쓰리해처리 건설시작 (초)

	private static final int GAS_9DRONE_SEC = 120; // TODO 가스기지 건설시작 - 9드론 (초)
	private static final int GAS_12DRONE_SEC = 120; // TODO 가스기지 건설시작 - 12드론 (초)
	private static final int GAS_2HATCH_SEC = 120; // TODO 가스기지 건설시작 - 12드론 2해처리 (초)
	private static final int GAS_3HATCH_SEC = 120; // TODO 가스기지 건설시작 - 12드론 3해처리 (초)

	private static final int OVERLOAD_SECOND_SEC = 120; // TODO 9드론후 2번째 오버로드를 찍었을 때 생산되는 시간 (초)
	private static final int OVERLOAD_SECOND_ON_EXPANSION_SEC = 120; // TODO 9드론후 2번째 오버로드가 앞마당에 도착하는 시작(초)

	private static final int ZERGLING_5DRONE_SEC = 120; // TODO 저글링 생산시간 - 5드론 (초)
	private static final int ZERGLING_5DRONE_ON_THE_WAY_SEC = 120; // TODO 저글링 앞마당 도착 시작 - 5드론 (초)
	private static final int ZERGLING_5DRONE_ON_MY_BASE_SEC = 120; // TODO 저글링 내 앞마당 도착 시작 - 5드론 (초)

	private static final int ZERGLING_9DRONE_SEC = 120; // TODO 저글링 생산시간 - 9드론 (초)
	private static final int ZERGLING_9DRONE_ON_THE_WAY_SEC = 120; // TODO 저글링 앞마당 도착 시작 - 9드론 (초)
	private static final int ZERGLING_9DRONE_ON_MY_BASE_SEC = 120; // TODO 저글링 내 앞마당 도착 시작 - 9드론 (초)

	private boolean is5DroneBuild = false;
	private boolean is9DroneBuild = false;
	private boolean is12DroneBuild = false;
	private boolean isOverloadFirstBuild = false;
	private boolean is2HatcheryBuild = false;

	private UnitInfo firstOverloadInfo = null;
	private UnitInfo secondOverloadInfo = null;
	private UnitInfo firstZerglingInfo = null;

	private int enemyGasFoundSec = CommonCode.UNKNOWN;
	private int enemyGasBuildSec = CommonCode.UNKNOWN;
	private int enemyGasNotFoundSec = CommonCode.UNKNOWN;

	public ZergBuildPhase1() {
		super(Race.Zerg, PHASE1_END_SEC);
		foundUnitTypes.add(UnitType.Zerg_Spawning_Pool);
		foundUnitTypes.add(UnitType.Zerg_Hatchery); // ignore enemybase hatchery
		foundUnitTypes.add(UnitType.Zerg_Extractor);
		foundUnitTypes.add(UnitType.Zerg_Overlord); // ignore first overload
		foundUnitTypes.add(UnitType.Zerg_Zergling);
	}

	/// 유닛 발견 맵을 분석한 정보
	@Override
	protected boolean analyse() {
		return analyseByPool() // 스포닝풀 발견에 따른 정보획득
				|| analyseBySecondHatch() // 2번째 해처리 발견에 따른 정보획득
				|| analyseBySecondOverload() // 2번째 오버로드 발견에 따른 정보획득
				|| analyseByZergling(); // 저글링 발견에 따른 정보획득
	}

	@Override
	protected void expectBuild() {
		if (is5DroneBuild) {
			enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_4OR5DRONE;
		} else if (is9DroneBuild) {
			if (enemyGasFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasFoundSec < GAS_9DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_9DRONE_GAS;
				}
			} else if (enemyGasNotFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasNotFoundSec > GAS_9DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_9DRONE_NO_GAS;
				}
			}
		} else if (is12DroneBuild) {
			if (enemyGasFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasFoundSec < GAS_12DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_OVER_POOL_GAS;
				}
			} else if (enemyGasNotFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasNotFoundSec > GAS_12DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_OVER_POOL_NO_GAS;
				}
			}
		} else if (is2HatcheryBuild) {
			if (enemyGasFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasFoundSec < GAS_2HATCH_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_12DRONE_MULTI_GAS;
				}
			} else if (enemyGasNotFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasNotFoundSec > GAS_2HATCH_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildOfPhase = EnemyBuild.ZERG_PHASE1_12DRONE_MULTI_NO_GAS;
				}
			}
		}
	}

	@Override
	protected EnemyBuild finalExpect() {
		if (is9DroneBuild) {
			return EnemyBuild.ZERG_PHASE1_9DRONE_GAS;
		} else if (is12DroneBuild) {
			return EnemyBuild.ZERG_PHASE1_OVER_POOL_GAS;
		} else if (is2HatcheryBuild) {
			return EnemyBuild.ZERG_PHASE1_12DRONE_MULTI_GAS;
		} else {
			return EnemyBuild.FAIL_TO_RECOGNIZE;
		}
	}

	private boolean analyseByPool() {
		List<UnitInfo> poolList = enemyUnitInfoMap.getOrDefault(UnitType.Zerg_Spawning_Pool, new ArrayList<UnitInfo>());
		if (poolList.isEmpty()) {
			return false;
		}

		UnitInfo firstPool = poolList.get(0);
		int firstPoolBuildSec = buildStartSecMap.get(firstPool.getUnitID());
		if (firstPoolBuildSec != CommonCode.UNKNOWN) {
			// 건설 중인 스포닝풀 발견
			if (firstPoolBuildSec < POOL_5DRONE_SEC) {
				is5DroneBuild = true;
			} else if (firstPoolBuildSec < POOL_9DRONE_SEC) {
				is9DroneBuild = true;
			} else if (firstPoolBuildSec < POOL_12DRONE_SEC) {
				is12DroneBuild = true;
			} else if (firstPoolBuildSec < POOL_2HATCH_SEC) {
				is2HatcheryBuild = true;
			} else {
				is2HatcheryBuild = true; // too late spawning
			}
			return true;
		} else {
			// 완성된 스포닝풀 발견
			return false;
		}
	}

	private boolean analyseBySecondHatch() {
		List<UnitInfo> hatchList = enemyUnitInfoMap.getOrDefault(UnitType.Zerg_Hatchery, new ArrayList<UnitInfo>());
		if (hatchList.isEmpty()) {
			return false;
		}

		UnitInfo secondHatchInfo = null;
		for (UnitInfo hatchInfo : hatchList) {
			if (!PositionUtils.isStartingPosition(hatchInfo.getLastPosition())) {
				secondHatchInfo = hatchInfo;
				break;
			}
		}
		if (secondHatchInfo == null) {
			return false;
		}

		int secondHatchBuildSec = buildStartSecMap.get(secondHatchInfo.getUnitID());
		if (secondHatchBuildSec != CommonCode.UNKNOWN) {
			// 건설중인 2번째 해처리 발견했는데, 2해처리 빌드이다.
			if (secondHatchBuildSec < HATCH_2HATCH_SEC) {
				is2HatcheryBuild = true;
				return true;
			}
		} else {
			// 완성된 2번째 해처리 발견했는데, 2해처리 빌드이다.
			int secondHatchFoundSec = TimeUtils.framesToSeconds(secondHatchInfo.getUpdateFrame());
			if (secondHatchFoundSec < HATCH_2HATCH_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Hatchery)) {
				is2HatcheryBuild = true;
				return true;
			}
		}
		return false;
	}

	private boolean analyseBySecondOverload() {
		List<UnitInfo> overloadList = enemyUnitInfoMap.getOrDefault(UnitType.Zerg_Overlord, new ArrayList<UnitInfo>());
		if (overloadList.size() <= 1) {
			return false;
		}

		int secondOverloadFoundSec = TimeUtils.framesToSeconds(overloadList.get(1).getUpdateFrame());
		if (secondOverloadFoundSec < OVERLOAD_SECOND_SEC) {
			// 2번째 오버로드 발견
			isOverloadFirstBuild = true;
		} else if (secondOverloadFoundSec < OVERLOAD_SECOND_ON_EXPANSION_SEC) {
			// 적 본진 바깥지역에서 2번째 오버로드 발견
			BaseLocation enemyBase = InfoUtils.enemyBase();
			if (enemyBase == null || !Prebot.Broodwar.isExplored(enemyBase.getTilePosition())) {
				isOverloadFirstBuild = true;
			}
		} else {
			// 더 늦게 발견한 오버로드는 의미 없다.
		}
		return false;
	}

	private boolean analyseByZergling() {
		List<UnitInfo> zerglingList = enemyUnitInfoMap.getOrDefault(UnitType.Zerg_Zergling, new ArrayList<UnitInfo>());
		if (zerglingList.isEmpty()) {
			return false;
		}

		int zerglingFoundSec = TimeUtils.framesToSeconds(zerglingList.get(0).getUpdateFrame());
		Position zerglingPosition = firstZerglingInfo.getLastPosition();
		Position basePosition = InfoUtils.myBase().getPosition();

		int distanceOfZergling = zerglingPosition.getApproxDistance(basePosition);

		if (zerglingFoundSec < ZERGLING_5DRONE_SEC) {
			is5DroneBuild = true;
			return true;
		}

		if (distanceOfZergling > StrategyConfig.APPROX_DISTANCE_1280) {
			if (zerglingFoundSec < ZERGLING_9DRONE_SEC) {
				is9DroneBuild = true;
				return true;
			}
		} else if (distanceOfZergling > StrategyConfig.APPROX_DISTANCE_640 // 필요시 세분화하여 분리
				|| distanceOfZergling > StrategyConfig.APPROX_DISTANCE_320) {
			if (zerglingFoundSec < ZERGLING_5DRONE_ON_THE_WAY_SEC) {
				is5DroneBuild = true;
				return true;
			} else if (zerglingFoundSec < ZERGLING_9DRONE_ON_THE_WAY_SEC) {
				is9DroneBuild = true;
				return true;
			}

		} else {
			if (zerglingFoundSec < ZERGLING_5DRONE_ON_MY_BASE_SEC) {
				is5DroneBuild = true;
				return true;
			} else if (zerglingFoundSec < ZERGLING_9DRONE_ON_MY_BASE_SEC) {
				is9DroneBuild = true;
				return true;
			}
		}
		return false;
	}
}
