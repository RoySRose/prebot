package prebot.strategy.analyse.deprecated;

public class ProtossBuildPhase2 {
//	extends RaceAction {
//
//	private enum CluePhase2 {
//		FAST_DRAGOON_RANGE_UP,
//		FAST_TEMPLAR_BUILD, TEMPLAR_BUILD,
//		FAST_ROBOTICS_BUILD, ROBOTICS_BUILD,
//		REAVER_BUILD,
//		OBSERVER_BUILD, FAST_OBSERVER_BUILD,
//	}
//	
//	private static final int PHASE2_LIMIT_TIME = TimeUtils.timeToFrames(59, 59); // ???
//	
//	public ProtossBuildPhase2() {
//		super(Race.Protoss, PHASE02, PHASE2_LIMIT_TIME);
//	}
//	
//	private int coreStartTime = CommonCode.INT_MAX;
//	private int adunStartTime = CommonCode.INT_MAX;
//	private int templarArchiveStartTime = CommonCode.INT_MAX;
//	
//	private int roboticsStartTime = CommonCode.INT_MAX;
//	private int roboSupportStartTime = CommonCode.INT_MAX;
//	private int observatoryStartTime = CommonCode.INT_MAX;
//	
////	private int stargateStartTime = CommonCode.INT_MAX;
////	private int fleetBeaconStartTime = CommonCode.INT_MAX;
//	
//	private int dropExpectedFrame = CommonCode.INT_MAX;
//	private int darkExpectedFrame = CommonCode.INT_MAX;
//	
//	
//	@Override
//	protected void analyse() {
//		if (unknownEnemyStrategy()) {
//			analyseByCore();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByAdun();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByRobotics();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseStargate();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseAdunUnits();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseRoboticsUnits();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseStargateUnits();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseThirdExpansion();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByClue();
//		}
//		
//		updateRushExpectedFrame();
//	}
//
//	private void updateRushExpectedFrame() {
//		if (containsAll(CluePhase2.ROBOTICS_BUILD)) {
//			dropExpectedFrame = roboSupportStartTime + UnitType.Protoss_Reaver.buildTime() +  baseToBaseFrame(UnitType.Protoss_Shuttle);
//		}
//		darkExpectedFrame = templarArchiveStartTime + UnitType.Protoss_Dark_Templar.buildTime() + baseToBaseFrame(UnitType.Protoss_Dark_Templar);
//		
//		int turretNeedFrame = Math.min(dropExpectedFrame, darkExpectedFrame);
//		
//		StrategyIdea.turretNeedFrame = turretNeedFrame;
//		StrategyIdea.turretBuildStartFrame = StrategyIdea.turretNeedFrame - UnitType.Terran_Missile_Turret.buildTime();
//		StrategyIdea.engineeringBayBuildStartFrame = StrategyIdea.turretBuildStartFrame - UnitType.Terran_Engineering_Bay.buildTime();
//	}
//
//	private void analyseByCore() {
//		// 코어 완료시간
//		FoundInfo coreFoundInfo = getFoundInfo(UnitType.Protoss_Cybernetics_Core);
//		int coreStartTime = buildStart(coreFoundInfo);
//		if (coreStartTime == CommonCode.UNKNOWN) {
//			coreStartTime = StrategyIdea.strategyHistory.get(PHASE01).defaultTimeMap.time(UnitType.Protoss_Cybernetics_Core);
//		}
//		this.coreStartTime = coreStartTime;
//		
//		// 빠른 드라군 사업 여부
//		if (StrategyIdea.strategyHistory.get(PHASE01).defaultTimeMap.time(UpgradeType.Singularity_Charge, 20) < TimeUtils.elapsedFrames()) {
//			List<UnitInfo> coreList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, UnitType.Protoss_Cybernetics_Core);
//			if (!coreList.isEmpty() && coreList.get(0).getUnit().isUpgrading()) {
//				addClue(CluePhase2.FAST_DRAGOON_RANGE_UP);
//			}
//		}
//	}
//
//	private void analyseByAdun() {
//		FoundInfo adunFoundInfo = getFoundInfo(UnitType.Protoss_Citadel_of_Adun);
//		int adunStartTime = buildStart(adunFoundInfo);
//		if (adunStartTime == CommonCode.UNKNOWN) {
//			adunStartTime = this.coreStartTime + UnitType.Protoss_Cybernetics_Core.buildTime();
//			
//			// 아둔 올라가는 것을 보지 못했을 때 다크가 올 수 있는 타이밍을 늦추는 로직
//			
//			// 드라군 사업을 빠르게 돌린 경우
//			if (containsAll(CluePhase2.FAST_DRAGOON_RANGE_UP)) {
//				adunStartTime += 20;
//			}
//			
//			// 게이트 웨이를 빠르게 늘린 경우
//			FoundInfo gateFoundInfo = getFoundInfo(UnitType.Protoss_Gateway);
//			if (gateFoundInfo.euiList.size() >= 2) {
//				// 게이트가 빨리 늘어났으면 아둔이 늦는다.
//				int firstGateStart = defaultBuildStartCompelteJustBefore(gateFoundInfo.euiList.get(0));
//				int secondGateStart = defaultBuildStartCompelteJustBefore(gateFoundInfo.euiList.get(1));
//				if (firstGateStart < EnemyStrategy.PROTOSS_FAST_DRAGOON.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 1, 20)
//						&& secondGateStart < EnemyStrategy.PROTOSS_FAST_DRAGOON.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 1, 20)) {
//					adunStartTime += 10;
//				}
//			}
//			
//			// 로보틱스퍼실리티를 빠르게 올린 경우
//			if (containsAll(CluePhase2.FAST_ROBOTICS_BUILD)) {
//				adunStartTime += 40;
//			}
//			
//		} else {
//			addClue(CluePhase2.TEMPLAR_BUILD);
//			if (adunStartTime < this.coreStartTime + UnitType.Protoss_Cybernetics_Core.buildTime() + 30) {
//				addClue(CluePhase2.FAST_TEMPLAR_BUILD);
//				if (adunStartTime < EnemyStrategy.PROTOSS_FAST_DARK.defaultTimeMap.time(UnitType.Protoss_Citadel_of_Adun, 20)) {
////					setEnemyStrategy(EnemyStrategy.PROTOSS_FAST_DARK, "fastest dark rush expected - adun");
//				}
//			}
//		}
//		this.adunStartTime = adunStartTime;
//
//		FoundInfo templarArchiveFoundInfo = getFoundInfo(UnitType.Protoss_Templar_Archives);
//		int templarArchiveStartTime = buildStart(templarArchiveFoundInfo);
//		if (templarArchiveStartTime == CommonCode.UNKNOWN) {
//			templarArchiveStartTime = this.adunStartTime + UnitType.Protoss_Citadel_of_Adun.buildTime();
//			
//		} else {
//			addClue(CluePhase2.TEMPLAR_BUILD);
//			if (templarArchiveStartTime < this.adunStartTime + UnitType.Protoss_Citadel_of_Adun.buildTime() + 30) {
//				addClue(CluePhase2.FAST_TEMPLAR_BUILD);
//				if (templarArchiveStartTime < EnemyStrategy.PROTOSS_FAST_DARK.defaultTimeMap.time(UnitType.Protoss_Templar_Archives, 20)) {
//					setEnemyStrategy(EnemyStrategy.PROTOSS_FAST_DARK, "fastest dark rush expected - adun");
//				}
//			}
//		}
//		this.templarArchiveStartTime = templarArchiveStartTime;
//	}
//	
//	private void analyseAdunUnits() {
//		FoundInfo darkFoundInfo = getFoundInfo(UnitType.Protoss_Dark_Templar);
//		if (!darkFoundInfo.euiList.isEmpty()) {
//			addClue(CluePhase2.TEMPLAR_BUILD);
//
//			int fastestDarkTemplarFrame = EnemyStrategy.PROTOSS_FAST_DARK.defaultTimeMap.time(UnitType.Protoss_Templar_Archives, 10)
//					+ UnitType.Protoss_Templar_Archives.buildTime() + UnitType.Protoss_Dark_Templar.buildTime();
//			int threeZealotInMyRegionFrame = fastestDarkTemplarFrame + baseToBaseFrame(UnitType.Protoss_Dark_Templar);
//			if (euiCountBeforeWhere(darkFoundInfo.euiList, threeZealotInMyRegionFrame, RegionType.ENEMY_FIRST_EXPANSION, RegionType.ETC, RegionType.MY_BASE) >= 1) {
//				setEnemyStrategy(EnemyStrategy.PROTOSS_FAST_DARK, "fastest dark rush expected - dark templar");
//			}
//		}
//		
//		FoundInfo highFoundInfo = getFoundInfo(UnitType.Protoss_High_Templar);
//		if (!highFoundInfo.euiList.isEmpty()) {
//			addClue(CluePhase2.TEMPLAR_BUILD);
//		}
//	}
//
//	private void analyseByRobotics() {
//		FoundInfo roboticsFoundInfo = getFoundInfo(UnitType.Protoss_Robotics_Facility);
//		int roboticsStartTime = buildStart(roboticsFoundInfo);
//		if (roboticsStartTime == CommonCode.UNKNOWN) {
//			roboticsStartTime = this.coreStartTime + UnitType.Protoss_Cybernetics_Core.buildTime();
//		} else {
//			addClue(CluePhase2.ROBOTICS_BUILD);
//			if (roboticsStartTime < this.coreStartTime + UnitType.Protoss_Cybernetics_Core.buildTime() + 30) {
//				addClue(CluePhase2.FAST_ROBOTICS_BUILD);
//			}
//		}
//		this.roboticsStartTime = roboticsStartTime;
//
//		FoundInfo roboSupportFoundInfo = getFoundInfo(UnitType.Protoss_Robotics_Support_Bay);
//		int roboSupportStartTime = buildStart(roboSupportFoundInfo);
//		if (roboSupportStartTime == CommonCode.UNKNOWN) {
//			roboSupportStartTime = this.roboticsStartTime + UnitType.Protoss_Robotics_Facility.buildTime();
//		} else {
//			addClue(CluePhase2.ROBOTICS_BUILD);
//			addClue(CluePhase2.REAVER_BUILD);
//			if (roboticsStartTime < this.roboticsStartTime + UnitType.Protoss_Robotics_Facility.buildTime() + 30) {
//				addClue(CluePhase2.FAST_ROBOTICS_BUILD);
//				setEnemyStrategy(EnemyStrategy.PROTOSS_ROBOTICS_REAVER, "fast robotics supportbay");
//			}
//		}
//		this.roboSupportStartTime = roboSupportStartTime;
//		
//		FoundInfo observatoryFoundInfo = getFoundInfo(UnitType.Protoss_Robotics_Support_Bay);
//		int observatoryStartTime = buildStart(observatoryFoundInfo);
//		if (observatoryStartTime == CommonCode.UNKNOWN) {
//			observatoryStartTime = this.roboticsStartTime + UnitType.Protoss_Observatory.buildTime();
//		} else {
//			addClue(CluePhase2.ROBOTICS_BUILD);
//			addClue(CluePhase2.OBSERVER_BUILD);
//			if (observatoryStartTime < this.roboticsStartTime + UnitType.Protoss_Robotics_Facility.buildTime() + 30) {
//				addClue(CluePhase2.FAST_ROBOTICS_BUILD);
//				addClue(CluePhase2.FAST_OBSERVER_BUILD);
//			}
//		}
//		this.roboSupportStartTime = roboSupportStartTime;
//	}
//	
//	private void analyseRoboticsUnits() {
//		FoundInfo shuttleFoundInfo = getFoundInfo(UnitType.Protoss_Shuttle);
//		if (!shuttleFoundInfo.euiList.isEmpty()) {
//			addClue(CluePhase2.ROBOTICS_BUILD);
//			int shuttleFrame = roboticsStartTime + UnitType.Protoss_Robotics_Facility.buildTime() + UnitType.Protoss_Shuttle.buildTime();
//			int shuttleInMyRegionFrame = shuttleFrame + baseToBaseFrame(UnitType.Protoss_Shuttle);
//			if (euiCountBefore(shuttleFoundInfo.euiList, shuttleInMyRegionFrame) >= 1) {
//				addClue(CluePhase2.FAST_ROBOTICS_BUILD);
//			}
//		}
//		
//		FoundInfo reaverFoundInfo = getFoundInfo(UnitType.Protoss_Reaver);
//		if (!reaverFoundInfo.euiList.isEmpty()) {
//			addClue(CluePhase2.ROBOTICS_BUILD);
//			addClue(CluePhase2.REAVER_BUILD);
//		}
//		
//		FoundInfo observerFoundInfo = getFoundInfo(UnitType.Protoss_Observer);
//		if (!observerFoundInfo.euiList.isEmpty()) {
//			int observerFrame = roboticsStartTime + UnitType.Protoss_Observatory.buildTime() + UnitType.Protoss_Observer.buildTime();
//			int observerInMyRegionFrame = observerFrame + baseToBaseFrame(UnitType.Protoss_Shuttle);
//			if (euiCountBefore(observerFoundInfo.euiList, observerInMyRegionFrame) >= 1) {
//				addClue(CluePhase2.FAST_OBSERVER_BUILD);
//			}
//			addClue(CluePhase2.ROBOTICS_BUILD);
//			addClue(CluePhase2.OBSERVER_BUILD);
//		}
//	}
//
//	private void analyseStargate() {
//		FoundInfo stargateFoundInfo = getFoundInfo(UnitType.Protoss_Stargate);
//		if (!stargateFoundInfo.euiList.isEmpty()) {
//			int stargateBuildStart = defaultBuildStartLastCheck(stargateFoundInfo, LastCheckType.BASE);
//			if (stargateBuildStart < this.coreStartTime + UnitType.Protoss_Stargate.buildTime() + 40) {
//				setEnemyStrategy(EnemyStrategy.PROTOSS_STARGATE, "fast stargate");
//				return;
//			}
//		}
//
//		FoundInfo fleetBeaconFoundInfo = getFoundInfo(UnitType.Protoss_Fleet_Beacon);
//		if (!fleetBeaconFoundInfo.euiList.isEmpty()) {
//			setEnemyStrategy(EnemyStrategy.PROTOSS_STARGATE, "carrier");
//			return;
//		}
//	}
//
//	private void analyseStargateUnits() {
//		
//	}
//	
//	private void analyseThirdExpansion() {
//	}
//	
//	private void analyseByClue() {
//		EnemyStrategy enemyStrategyExpect = StrategyIdea.strategyHistory.get(PHASE01);
//		if (containsAll(CluePhase2.FAST_TEMPLAR_BUILD)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_FAST_DARK;
//			
//		} else if (containsAll(CluePhase2.TEMPLAR_BUILD)) {
//			if (containsAny(CluePhase2.ROBOTICS_BUILD)) {
//				enemyStrategyExpect = EnemyStrategy.PROTOSS_DARK_DROP;
//			} else {
//				enemyStrategyExpect = EnemyStrategy.PROTOSS_FAST_DARK;
//			}
//			
//		} else if (containsAny(CluePhase2.REAVER_BUILD)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_ROBOTICS_REAVER;
//		}
//
//		setEnemyStrategyExpect(enemyStrategyExpect);
//	}
	
}
