package prebot.strategy.action.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.action.Action;
import prebot.strategy.constant.StrategyConfig;
import prebot.strategy.constant.StrategyConfig.EnemyBuild;

public class ZergBuildPhase1 extends Action {
	
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

	private EnemyBuild enemyBuildPhase1 = EnemyBuild.UNKNOWN; // TODO diplay
	private List<UnitType> foundUnitTypes = new ArrayList<>(); // TODO diplay
	private Map<UnitType, Integer> unitFoundSecMap = new HashMap<>(); // TODO display
	private Map<UnitType, Integer> unitBuildStartSecMap = new HashMap<>(); // TODO display
	
	private boolean buildFoundFlag = false;
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
		foundUnitTypes.add(UnitType.Zerg_Spawning_Pool);
		foundUnitTypes.add(UnitType.Zerg_Hatchery); // ignore enemybase hatchery
		foundUnitTypes.add(UnitType.Zerg_Extractor);
		foundUnitTypes.add(UnitType.Zerg_Overlord); // ignore first overload
		foundUnitTypes.add(UnitType.Zerg_Zergling);
	}

	@Override
	public boolean exitCondition() {
		// 전략이 파악되었다.
		if (enemyBuildPhase1 != EnemyBuild.UNKNOWN) {
			StrategyIdea.enemyBuildPhase1 = enemyBuildPhase1;
			return true;
		}
		
		// 일정시간이 지날 때까지 전략을 알지 못하였다.
		if (TimeUtils.isPassedFrame(PHASE1_END_SEC)) {
			StrategyIdea.enemyBuildPhase1 = EnemyBuild.FAIL_TO_RECOGNIZE;
			return true;
		}
		
		return false;
	}
	
	@Override
	public void action() {
		updateUnitFoundMap();
		analyse();
		
		if (zerg4or5Drone()) return;
		if (zerg9Drone()) return;
		if (zerg12Drone()) return;
		if (zerg12DroneMulti()) return;
	}

	/// 유닛 발견 맵을 업데이트한다.
	private void updateUnitFoundMap() {
		for (UnitType unitType : foundUnitTypes) {
			Integer foundSec = unitFoundSecMap.get(unitType);
			if (foundSec != null) { // 이미 찾은 유닛타입
				continue;
			}
			
			Unit enemyUnit = null;
			List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, unitType);
			if (!euiList.isEmpty()) {
				enemyUnit = UnitUtils.unitInSight(euiList.get(0));
			}
			if (enemyUnit == null) { // 찾지 못하였다.
				continue;
			}
			
			if (unitType == UnitType.Zerg_Hatchery) { // 본진해처리는 무시한다.
				BaseLocation enemyBase = InfoUtils.enemyBase();
				if (enemyBase != null && enemyBase.getPosition().equals(enemyUnit.getPosition())) {
					continue;
				}
			} else if (unitType == UnitType.Zerg_Overlord) { // 첫번째 오버로드는 무시한다.
				if (firstOverloadInfo == null || firstOverloadInfo.getUnitID() == enemyUnit.getID()) {
					firstOverloadInfo = (UnitInfo) euiList.get(0);
					continue;
				}
				secondOverloadInfo = (UnitInfo) euiList.get(0);
				
			} else if (unitType == UnitType.Zerg_Zergling) {
				firstZerglingInfo = (UnitInfo) euiList.get(0);
			}
			
			unitFoundSecMap.put(unitType, TimeUtils.elapsedSeconds()); // 발견한 시간 (초)
			unitBuildStartSecMap.put(unitType, TimeUtils.buildStartSeconds(enemyUnit)); // 빌드시작한 시잔 (초). 이미 완성되어 알 수 없다면 UNKNOWN
		}
	}

	/// 유닛 발견 맵을 분석한 정보
	private void analyse() {
		analyseByGas(); // 가스통 발견에 따른 정보획득
		if (!buildFoundFlag) {
			analyseByPool(); // 스포닝풀 발견에 따른 정보획득
			analyseBySecondHatch(); // 2번째 해처리 발견에 따른 정보획득
			analyseBySecondOverload(); // 2번째 오버로드 발견에 따른 정보획득
			analyseByZergling(); // 저글링 발견에 따른 정보획득
		}
	}

	private void analyseByGas() {
		if (unitFoundSecMap.get(UnitType.Zerg_Extractor) != null) {
			enemyGasFoundSec = unitFoundSecMap.get(UnitType.Zerg_Extractor);
			enemyGasBuildSec = unitBuildStartSecMap.get(UnitType.Zerg_Extractor);
		} else {
			BaseLocation enemyBase = InfoUtils.enemyBase();
			if (enemyBase != null) {
				List<Unit> geysers = enemyBase.getGeysers();
				if (!geysers.isEmpty()) {
					Unit enemyGas = geysers.get(0);
					if (Prebot.Broodwar.isVisible(enemyGas.getTilePosition())) {
						enemyGasNotFoundSec = TimeUtils.elapsedSeconds();
					}
				}
			}
		}
	}

	private void analyseByPool() {
		if (unitFoundSecMap.get(UnitType.Zerg_Spawning_Pool) != null) {
			int firstPoolBuildSec = unitBuildStartSecMap.get(UnitType.Zerg_Spawning_Pool);
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
				buildFoundFlag =  true;
			} else {
				// 완성된 스포닝풀 발견
			}
		}
	}
	
	private void analyseBySecondHatch() {
		if (unitFoundSecMap.get(UnitType.Zerg_Hatchery) != null) {
			int secondHatchBuildSec = unitBuildStartSecMap.get(UnitType.Zerg_Hatchery);
			if (secondHatchBuildSec != CommonCode.UNKNOWN) {
				// 건설중인 2번째 해처리 발견
				if (secondHatchBuildSec < HATCH_2HATCH_SEC) {
					is2HatcheryBuild = true;
					buildFoundFlag =  true;
				}
			} else {
				// 완성된 2번째 해처리 발견
				int secondHatchFoundSec = unitFoundSecMap.get(UnitType.Zerg_Hatchery);
				if (secondHatchFoundSec < HATCH_2HATCH_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Hatchery)) {
					is2HatcheryBuild = true;
					buildFoundFlag =  true;
				}
			}
		}
	}

	private void analyseBySecondOverload() {
		if (unitFoundSecMap.get(UnitType.Zerg_Overlord) != null) {
			int secondOverloadFoundSec = unitFoundSecMap.get(UnitType.Zerg_Overlord);
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
		}
	}

	private void analyseByZergling() {
		if (unitFoundSecMap.get(UnitType.Zerg_Zergling) != null) {
			int zerglingFoundSec = unitFoundSecMap.get(UnitType.Zerg_Zergling);
			Position zerglingPosition = firstZerglingInfo.getLastPosition();
			Position basePosition = InfoUtils.myBase().getPosition();

			int distanceOfZergling = zerglingPosition.getApproxDistance(basePosition);
			
			if (zerglingFoundSec < ZERGLING_5DRONE_SEC) {
				is5DroneBuild = true;
				buildFoundFlag =  true;
			}
			
			if (distanceOfZergling > StrategyConfig.APPROX_DISTANCE_1280) {
				if (zerglingFoundSec < ZERGLING_9DRONE_SEC) {
					is9DroneBuild = true;
					buildFoundFlag =  true;
				}
			} else if (distanceOfZergling > StrategyConfig.APPROX_DISTANCE_640 // 필요시 세분화하여 분리
					|| distanceOfZergling > StrategyConfig.APPROX_DISTANCE_320) {
				if (zerglingFoundSec < ZERGLING_5DRONE_ON_THE_WAY_SEC) {
					is5DroneBuild = true;
					buildFoundFlag =  true;
				} else if (zerglingFoundSec < ZERGLING_9DRONE_ON_THE_WAY_SEC) {
					is9DroneBuild = true;
					buildFoundFlag =  true;
				}
				
			} else {
				if (zerglingFoundSec < ZERGLING_5DRONE_ON_MY_BASE_SEC) {
					is5DroneBuild = true;
					buildFoundFlag =  true;
				} else if (zerglingFoundSec < ZERGLING_9DRONE_ON_MY_BASE_SEC) {
					is9DroneBuild = true;
					buildFoundFlag =  true;
				}
			}
		}
	}

	// false 조건
	// 1. 2번째 해처리 발견
	// 1. 조건 xxx 프레임 이하에 enemyBase region이 아닌 곳에서 저글링을 발견했다.
	// 2. enemy region에 도착. $9DRONE_POOL_SEC 이하에 스포닝풀 발견. 프레임 대비, 스포닝풀 드론수가 6마리 이하
	private boolean zerg4or5Drone() {
		if (is5DroneBuild) {
			enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_4OR5DRONE;
			return true;
		}
		return false;
	}

	private boolean zerg9Drone() {
		if (is9DroneBuild) {
			if (enemyGasFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasFoundSec < GAS_9DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_9DRONE_GAS;
					return true;
				}
			} else if (enemyGasNotFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasNotFoundSec > GAS_9DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_9DRONE_NO_GAS;
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean zerg12Drone() {
		if (is12DroneBuild) {
			if (enemyGasFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasFoundSec < GAS_12DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_OVER_POOL_GAS;
					return true;
				}
			} else if (enemyGasNotFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasNotFoundSec > GAS_12DRONE_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_OVER_POOL_NO_GAS;
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean zerg12DroneMulti() {
		if (is2HatcheryBuild) {
			if (enemyGasFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasFoundSec < GAS_2HATCH_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_12DRONE_MULTI_GAS;
					return true;
				}
			} else if (enemyGasNotFoundSec != CommonCode.UNKNOWN) {
				if (enemyGasNotFoundSec > GAS_2HATCH_SEC + TimeUtils.buildSeconds(UnitType.Zerg_Extractor)) {
					enemyBuildPhase1 = EnemyBuild.ZERG_PHASE1_12DRONE_MULTI_NO_GAS;
					return true;
				}
			}
		}
		return false;
	}

}
