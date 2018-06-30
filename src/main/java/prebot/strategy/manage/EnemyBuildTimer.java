package prebot.strategy.manage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.StrategyAnalyseManager.LastCheckLocation;

public class EnemyBuildTimer {

	// lair, hydraden, spire, core, adun, templar arch, robotics, robo support, factory, starport
	public Map<UnitType, Integer> buildTimeExpectMap = new HashMap<>();
	public Map<UnitType, Integer> buildTimeMinimumMap = new HashMap<>();
	public Set<UnitType> buildTimeCertain = new HashSet<>();

	public int darkTemplarInMyBaseFrame = CommonCode.UNKNOWN;
	public int reaverInMyBaseFrame = CommonCode.UNKNOWN;
	public int mutaliskInMyBaseFrame = CommonCode.UNKNOWN;
	public int lurkerInMyBaseFrame = CommonCode.UNKNOWN;

	public int getBuildStartFrameExpect(UnitType buildingType) {
		Integer expectFrame = buildTimeExpectMap.get(buildingType);
		return expectFrame == null ? CommonCode.UNKNOWN : expectFrame;
	}

	private static EnemyBuildTimer instance = new EnemyBuildTimer();

	public static EnemyBuildTimer Instance() {
		return instance;
	}

	// [[건물 예측시간 기준]]
	//
	// [확정 케이스 - 발견]
	// 1. 빌드 중인 건물을 본 경우 - 정확한 시간
	// - 빌드 중인 건물의 에너지를 기반으로 정확한 빌드시작시간 예측 가능(레어, 하이브 제외)
	// 2. 완성된 건물을 본 경우 - 부정확한 시간
	// - 전략별 시간, 발견시간에 막 완성 중 작은값.
	// - 전략별 시간이 논리적인 최소시간보다 작을 경우 논리적 최소시간을 사용한다.
	// - 후자가 빠르다면 전략예측이 빗나간 케이스일 확률이 높음
	//
	// [미확정 케이스 - 미발견]
	// 1. 건물을 발견하지 못함 - 부정확한 시간
	// - 전략별 시간
	//
	// * 논리적 최소시간
	// 1. 연계건물
	// ex) 아둔은 코어빌드시작프레임 + 코어빌드타임 이전에 지어질 수 없다.
	//     템플러 아카이브는 아둔빌드시작프레임 + 아둔빌드타임 이전에 지어질 수 없다.
	// 2. 정찰이 모두 끝난 상태(본진, 가스, 앞마당)의 프레임에 이전에 건물이 있을 수 없다.
	// - 단, 다크의 경우 숨김건물이 있을 수 있으므로 예외로직 필요
	public void update() {
		Race enemyRace = InfoUtils.enemyRace();
		if (enemyRace == Race.Protoss) {
			updateCoreExpectTime();
			updateAdunExpectTime();
			updateTemplarArchExpectTime();
			updateRoboticsExpectTime();
			updateRoboticsSupportExpectTime();
			updateObservatoryExpectTime();

		} else if (enemyRace == Race.Zerg) {
			updateLairExpectTime();
			updateHydradenExpectTime();
			updateSpireExpectTime();

		} else if (enemyRace == Race.Terran) {
			// updateFactoryExpectTime();
			// updateStarportExpectTime();
			// updateEngineeringExpectTime();
			// updateTurretExpectTime();
		}
		
		setImportantTime();
	}

	private void setImportantTime() {
		Race enemyRace = InfoUtils.enemyRace();
		if (enemyRace == Race.Protoss) {
			if (!UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Dark_Templar)) {
				int templarArchFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Protoss_Templar_Archives);
				if (templarArchFrame != CommonCode.UNKNOWN) {
					darkTemplarInMyBaseFrame = templarArchFrame + UnitType.Protoss_Templar_Archives.buildTime() + UnitType.Protoss_Dark_Templar.buildTime()
									+ InformationManager.Instance().baseToBaseFrame(UnitType.Protoss_Dark_Templar);
				}
			} else {
				
			}
			
			if (!UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Reaver)) {
				int roboSupportFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Protoss_Robotics_Support_Bay);
				if (roboSupportFrame != CommonCode.UNKNOWN) {
					reaverInMyBaseFrame = roboSupportFrame + UnitType.Protoss_Robotics_Support_Bay.buildTime() + UnitType.Protoss_Reaver.buildTime()
									+ InformationManager.Instance().baseToBaseFrame(UnitType.Protoss_Shuttle);
				}
			} else {
				
			}
			
			if (darkTemplarInMyBaseFrame != CommonCode.UNKNOWN) {
				if (reaverInMyBaseFrame == CommonCode.UNKNOWN || darkTemplarInMyBaseFrame < reaverInMyBaseFrame) {
					StrategyIdea.turretNeedFrame = darkTemplarInMyBaseFrame - 5 * TimeUtils.SECOND;
					StrategyIdea.turretBuildStartFrame = StrategyIdea.turretNeedFrame - UnitType.Terran_Missile_Turret.buildTime() - 5 * TimeUtils.SECOND;
					StrategyIdea.engineeringBayBuildStartFrame = StrategyIdea.turretBuildStartFrame - UnitType.Terran_Engineering_Bay.buildTime() - 5 * TimeUtils.SECOND;
				} else {
					StrategyIdea.turretNeedFrame = reaverInMyBaseFrame;
					StrategyIdea.turretBuildStartFrame = reaverInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
					StrategyIdea.engineeringBayBuildStartFrame = reaverInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
				}
			} else if (reaverInMyBaseFrame != CommonCode.UNKNOWN) {
				StrategyIdea.turretNeedFrame = reaverInMyBaseFrame;
				StrategyIdea.turretBuildStartFrame = reaverInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
				StrategyIdea.engineeringBayBuildStartFrame = reaverInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
			}
			
		} else if (enemyRace == Race.Zerg) {
			if (!UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Mutalisk)) {
				int spireFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Spire);
				if (spireFrame != CommonCode.UNKNOWN) {
					mutaliskInMyBaseFrame = spireFrame + UnitType.Zerg_Spire.buildTime() + UnitType.Zerg_Mutalisk.buildTime()
									+ InformationManager.Instance().baseToBaseFrame(UnitType.Zerg_Mutalisk);
				}
			}
			
			if (!UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Lurker)) {
				int hydradenFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Hydralisk_Den);
				int lairFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
				
				if (hydradenFrame != CommonCode.UNKNOWN && lairFrame != CommonCode.UNKNOWN) {
					lurkerInMyBaseFrame = Math.max(hydradenFrame, lairFrame) + TechType.Lurker_Aspect.researchTime() + UnitType.Zerg_Lurker.buildTime()
									+ InformationManager.Instance().baseToBaseFrame(UnitType.Zerg_Lurker);
				}
			}
			
			if (mutaliskInMyBaseFrame != CommonCode.UNKNOWN) {
				if (lurkerInMyBaseFrame == CommonCode.UNKNOWN || mutaliskInMyBaseFrame < lurkerInMyBaseFrame) {
					StrategyIdea.turretNeedFrame = mutaliskInMyBaseFrame;
					StrategyIdea.turretBuildStartFrame = mutaliskInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
					StrategyIdea.engineeringBayBuildStartFrame = mutaliskInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
				} else {
					StrategyIdea.turretNeedFrame = lurkerInMyBaseFrame;
					StrategyIdea.turretBuildStartFrame = lurkerInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
					StrategyIdea.engineeringBayBuildStartFrame = lurkerInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
				}
			} else if (lurkerInMyBaseFrame != CommonCode.UNKNOWN) {
				StrategyIdea.turretNeedFrame = lurkerInMyBaseFrame;
				StrategyIdea.turretBuildStartFrame = lurkerInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
				StrategyIdea.engineeringBayBuildStartFrame = lurkerInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
			}
			
		} else {
			
		}
		
	}

	private void updateCoreExpectTime() {
		updateByBuilding(UnitType.Protoss_Cybernetics_Core, UnitType.Protoss_Citadel_of_Adun, UnitType.Protoss_Robotics_Facility);
		updateByFlagUnit(UnitType.Protoss_Cybernetics_Core, UnitType.Protoss_Dragoon);
	}

	private void updateAdunExpectTime() {
		updateByBuilding(UnitType.Protoss_Citadel_of_Adun, UnitType.Protoss_Templar_Archives);
		updateByFlagUnit(UnitType.Protoss_Citadel_of_Adun, UnitType.Protoss_Dark_Templar);
		updateByFlagUnit(UnitType.Protoss_Citadel_of_Adun, UnitType.Protoss_High_Templar);
	}

	private void updateTemplarArchExpectTime() {
		updateByBuilding(UnitType.Protoss_Templar_Archives);
		updateByFlagUnit(UnitType.Protoss_Templar_Archives, UnitType.Protoss_Dark_Templar);
		updateByFlagUnit(UnitType.Protoss_Templar_Archives, UnitType.Protoss_High_Templar);
	}

	private void updateRoboticsExpectTime() {
		updateByBuilding(UnitType.Protoss_Robotics_Facility, UnitType.Protoss_Robotics_Support_Bay, UnitType.Protoss_Observatory);
		updateByFlagUnit(UnitType.Protoss_Robotics_Facility, UnitType.Protoss_Shuttle);
		updateByFlagUnit(UnitType.Protoss_Robotics_Facility, UnitType.Protoss_Reaver);
		updateByFlagUnit(UnitType.Protoss_Robotics_Facility, UnitType.Protoss_Observer);
	}

	private void updateRoboticsSupportExpectTime() {
		updateByBuilding(UnitType.Protoss_Robotics_Support_Bay);
		updateByFlagUnit(UnitType.Protoss_Robotics_Support_Bay, UnitType.Protoss_Reaver);
	}

	private void updateObservatoryExpectTime() {
		updateByBuilding(UnitType.Protoss_Observatory);
		updateByFlagUnit(UnitType.Protoss_Observatory, UnitType.Protoss_Observer);
	}

	private void updateLairExpectTime() {
		updateByBuilding(UnitType.Zerg_Lair, UnitType.Zerg_Spire);
		updateByFlagUnit(UnitType.Zerg_Lair, UnitType.Zerg_Lurker);
		updateByFlagUnit(UnitType.Zerg_Lair, UnitType.Zerg_Mutalisk);
	}

	private void updateHydradenExpectTime() {
		int lairInProgressExpect = getBuildStartFrameExpect(UnitType.Zerg_Lair) + (int) (UnitType.Zerg_Lair.buildTime() * 0.4);
		updateByBuilding(lairInProgressExpect, UnitType.Zerg_Hydralisk_Den);
		updateByFlagUnit(UnitType.Zerg_Hydralisk_Den, UnitType.Zerg_Hydralisk);
	}

	private void updateSpireExpectTime() {
		int lairCompleteExpect = getBuildStartFrameExpect(UnitType.Zerg_Lair) + UnitType.Zerg_Lair.buildTime();
		updateByBuilding(lairCompleteExpect, UnitType.Zerg_Spire);
		updateByFlagUnit(UnitType.Zerg_Spire, UnitType.Zerg_Mutalisk);
	}
	
	private void updateByBuilding(UnitType buildingType, UnitType... nextBuildingTypes) {
		int buildFrameByStrategy = StrategyIdea.currentStrategy.defaultTimeMap.frame(buildingType);
		if (buildFrameByStrategy == CommonCode.UNKNOWN) {
			buildFrameByStrategy = StrategyIdea.startStrategy.defaultTimeMap.frame(buildingType);
		}
		updateByBuilding(buildFrameByStrategy, buildingType, nextBuildingTypes);
	}
	
	private void updateByBuilding(int buildFrameByStrategy, UnitType buildingType, UnitType... nextBuildingTypes) {
		if (isCertainBuildTime(buildingType)) {
			return;
		}
		List<UnitInfo> buildingInfos = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, buildingType);
		if (!buildingInfos.isEmpty()) {
			if (buildingInfos.get(0).isCompleted()) {
				// 완성된 건물 발견. 막 완성되었다고 가정한 시간과 전략별 시간 중 빠른 값 선택
				int expectBuildFrame = buildingInfos.get(0).getUpdateFrame() - buildingType.buildTime();
				if (buildFrameByStrategy != CommonCode.UNKNOWN && buildFrameByStrategy < expectBuildFrame) {
					expectBuildFrame = buildFrameByStrategy;
				}
				updateBuildTimeExpect(buildingType, expectBuildFrame);
				addCertainBuildTime(buildingType);
				for (UnitType nextBuildingType : nextBuildingTypes) {
					updateBuildTimeMinimum(nextBuildingType, expectBuildFrame + buildingType.buildTime());
				}

			} else {
				// 미완성 건물 발견. 에너지로 빌드시간 추측 가능
				int expectBuildFrame = TimeUtils.buildStartFrames(buildingInfos.get(0).getUnit());
				if (expectBuildFrame == CommonCode.UNKNOWN) {
					expectBuildFrame = buildFrameByStrategy;
				}
				if (expectBuildFrame == CommonCode.UNKNOWN) {
					System.out.println("################ ERROR - updateDefaultExpectTime" + buildFrameByStrategy + " / " + buildingType);
				}
				updateBuildTimeExpect(buildingType, expectBuildFrame);
				addCertainBuildTime(buildingType);
				for (UnitType nextBuildingType : nextBuildingTypes) {
					updateBuildTimeMinimum(nextBuildingType, expectBuildFrame + buildingType.buildTime());
				}
			}

		} else {
			// 미발견. 전략별 시간 선택. 전략별 시간 이후 적 본진, 앞마당, 가스가 모두 정찰 되었다면 최후 정찰시간을 이후로 최소값을 증가시킨다.
			if (buildFrameByStrategy != CommonCode.UNKNOWN) {
				int baseLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.BASE);
				int gasLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.GAS);
				int expansionLastCheckFrame = StrategyAnalyseManager.Instance().lastCheckFrame(LastCheckLocation.FIRST_EXPANSION);
				if (baseLastCheckFrame > buildFrameByStrategy && gasLastCheckFrame > buildFrameByStrategy && expansionLastCheckFrame > buildFrameByStrategy) {
					int minimumFrame = baseLastCheckFrame < gasLastCheckFrame ? baseLastCheckFrame : gasLastCheckFrame;
					minimumFrame = minimumFrame < expansionLastCheckFrame ? minimumFrame : expansionLastCheckFrame;
					updateBuildTimeMinimum(buildingType, minimumFrame);
				}
				if (buildingType.gasPrice() > 0 && InfoUtils.enemyBaseGas() != null) {
					if (InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor, UnitType.Terran_Refinery) == 0) {
						updateBuildTimeMinimum(buildingType, gasLastCheckFrame + UnitType.Terran_Refinery.buildTime() + 10 * TimeUtils.SECOND); // 모든 가스기지 빌드타임 동일
					}
				}
				
				updateBuildTimeExpect(buildingType, buildFrameByStrategy);
			}
		}
	}

	private void updateByFlagUnit(UnitType buildingType, UnitType unitType) {
		if (isCertainBuildTime(buildingType)) {
			return;
		}
		if (UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, unitType).isEmpty()) {
			return;
		}

		int expectBuildTime = TimeUtils.elapsedFrames() - unitType.buildTime() - buildingType.buildTime();

		if (buildingType == UnitType.Protoss_Citadel_of_Adun) {
			if (unitType == UnitType.Protoss_Dark_Templar || unitType == UnitType.Protoss_High_Templar) {
				expectBuildTime -= UnitType.Protoss_Templar_Archives.buildTime();
			}
		} else if (buildingType == UnitType.Protoss_Robotics_Facility) {
			if (unitType == UnitType.Protoss_Reaver) {
				expectBuildTime -= UnitType.Protoss_Robotics_Support_Bay.buildTime();
			} else if (unitType == UnitType.Protoss_Observer) {
				expectBuildTime -= UnitType.Protoss_Observatory.buildTime();
			}
		} else if (buildingType == UnitType.Zerg_Lair) {
			if (unitType == UnitType.Zerg_Lurker) {
				expectBuildTime -= TechType.Lurker_Aspect.researchTime();
			} else if (unitType == UnitType.Zerg_Mutalisk) {
				expectBuildTime -= UnitType.Zerg_Spire.buildTime();
			}
		}

		int buildFrameByStrategy = StrategyIdea.currentStrategy.defaultTimeMap.frame(buildingType);
		if (buildFrameByStrategy != CommonCode.UNKNOWN && buildFrameByStrategy < expectBuildTime) {
			expectBuildTime = buildFrameByStrategy;
		} else {
			buildFrameByStrategy = StrategyIdea.startStrategy.defaultTimeMap.frame(buildingType);
			if (buildFrameByStrategy != CommonCode.UNKNOWN && buildFrameByStrategy < expectBuildTime) {
				expectBuildTime = buildFrameByStrategy;
			}
		}
		updateBuildTimeExpect(buildingType, expectBuildTime);
		addCertainBuildTime(buildingType);
	}

	private boolean isCertainBuildTime(UnitType buildingType) {
		return buildTimeCertain.contains(buildingType);
	}

	private void updateBuildTimeExpect(UnitType buildingType, int frame) {
		if (frame == CommonCode.UNKNOWN) {
			return;
		}
		Integer buildTimeMinimum = buildTimeMinimumMap.get(buildingType);
		if (buildTimeMinimum != null && frame < buildTimeMinimum) {
			buildTimeExpectMap.put(buildingType, buildTimeMinimum);
		} else {
			buildTimeExpectMap.put(buildingType, frame);
		}
	}

	private void updateBuildTimeMinimum(UnitType buildingType, int frame) {
		if (frame == CommonCode.UNKNOWN) {
			return;
		}
		Integer buildTimeMinimum = buildTimeMinimumMap.get(buildingType);
		if (buildTimeMinimum == null || frame > buildTimeMinimum) {
			buildTimeMinimumMap.put(buildingType, frame);
		}
	}

	private void addCertainBuildTime(UnitType buildingType) {
		buildTimeCertain.add(buildingType);
	}
}
