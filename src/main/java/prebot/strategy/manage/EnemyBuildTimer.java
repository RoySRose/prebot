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
	public int cloakingWraithFrame = CommonCode.UNKNOWN;

	public int getBuildStartFrameExpect(UnitType buildingType) {
		Integer expectFrame = buildTimeExpectMap.get(buildingType);
		return expectFrame == null ? CommonCode.UNKNOWN : expectFrame;
	}

	private static EnemyBuildTimer instance = new EnemyBuildTimer();

	public static EnemyBuildTimer Instance() {
		return instance;
	}

	private void setImportantTime() {
		Race enemyRace = InfoUtils.enemyRace();
		
		if (enemyRace == Race.Protoss) {
			int templarArchFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Protoss_Templar_Archives);
			if (templarArchFrame != CommonCode.UNKNOWN) {
				darkTemplarInMyBaseFrame = templarArchFrame + UnitType.Protoss_Templar_Archives.buildTime() + UnitType.Protoss_Dark_Templar.buildTime() + InformationManager.Instance().baseToBaseFrame(UnitType.Protoss_Dark_Templar);
			}
			
			int roboSupportFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Protoss_Robotics_Support_Bay);
			if (roboSupportFrame != CommonCode.UNKNOWN) {
				reaverInMyBaseFrame = roboSupportFrame + UnitType.Protoss_Robotics_Support_Bay.buildTime() + UnitType.Protoss_Reaver.buildTime() + InformationManager.Instance().baseToBaseFrame(UnitType.Protoss_Shuttle);
			}
			
			boolean darktemplarTime = darkTemplarInMyBaseFrame != CommonCode.UNKNOWN && (reaverInMyBaseFrame == CommonCode.UNKNOWN || darkTemplarInMyBaseFrame < reaverInMyBaseFrame);
			boolean reaverTime = reaverInMyBaseFrame != CommonCode.UNKNOWN && (darkTemplarInMyBaseFrame == CommonCode.UNKNOWN || reaverInMyBaseFrame < darkTemplarInMyBaseFrame);

			if (darktemplarTime) {
				StrategyIdea.turretNeedFrame = darkTemplarInMyBaseFrame;
				StrategyIdea.turretBuildStartFrame = StrategyIdea.turretNeedFrame - UnitType.Terran_Missile_Turret.buildTime();
				StrategyIdea.engineeringBayBuildStartFrame = StrategyIdea.turretBuildStartFrame - UnitType.Terran_Engineering_Bay.buildTime();
				
			} else if (reaverTime) {
				StrategyIdea.turretNeedFrame = reaverInMyBaseFrame;
				StrategyIdea.turretBuildStartFrame = reaverInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
				StrategyIdea.engineeringBayBuildStartFrame = reaverInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
			}
			
			if (darkTemplarInMyBaseFrame != CommonCode.UNKNOWN) {
				StrategyIdea.academyFrame = darkTemplarInMyBaseFrame - UnitType.Terran_Academy.buildTime() - UnitType.Terran_Comsat_Station.buildTime();
			}
			
			// 프로토스 전은 안전하게 10초나 땡겨서 시간을 준다.
			StrategyIdea.turretNeedFrame -= 15 * TimeUtils.SECOND;
			StrategyIdea.turretBuildStartFrame -= 15 * TimeUtils.SECOND;
			StrategyIdea.engineeringBayBuildStartFrame -= 15 * TimeUtils.SECOND;
			StrategyIdea.academyFrame -= 10 * TimeUtils.SECOND;
			
		} else if (enemyRace == Race.Zerg) {
			int spireFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Spire);
			if (spireFrame != CommonCode.UNKNOWN) {
				mutaliskInMyBaseFrame = spireFrame + UnitType.Zerg_Spire.buildTime() + UnitType.Zerg_Mutalisk.buildTime() + InformationManager.Instance().baseToBaseFrame(UnitType.Zerg_Mutalisk);
			}
			
			int hydradenFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Hydralisk_Den);
			int lairFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Zerg_Lair);
			if (hydradenFrame != CommonCode.UNKNOWN && lairFrame != CommonCode.UNKNOWN) {
				lurkerInMyBaseFrame = Math.max(hydradenFrame, lairFrame) + TechType.Lurker_Aspect.researchTime() + UnitType.Zerg_Lurker.buildTime() + InformationManager.Instance().baseToBaseFrame(UnitType.Zerg_Lurker);
			}

			boolean muteTime = mutaliskInMyBaseFrame != CommonCode.UNKNOWN && (lurkerInMyBaseFrame == CommonCode.UNKNOWN || mutaliskInMyBaseFrame < lurkerInMyBaseFrame);
			boolean lurkTime = lurkerInMyBaseFrame != CommonCode.UNKNOWN && (mutaliskInMyBaseFrame == CommonCode.UNKNOWN || lurkerInMyBaseFrame < mutaliskInMyBaseFrame);

			if (muteTime) {
				StrategyIdea.turretNeedFrame = mutaliskInMyBaseFrame;
				StrategyIdea.turretBuildStartFrame = mutaliskInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
				StrategyIdea.engineeringBayBuildStartFrame = mutaliskInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
			} else if (lurkTime) {
				StrategyIdea.turretNeedFrame = lurkerInMyBaseFrame;
				StrategyIdea.turretBuildStartFrame = lurkerInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime();
				StrategyIdea.engineeringBayBuildStartFrame = lurkerInMyBaseFrame - UnitType.Terran_Missile_Turret.buildTime() - UnitType.Terran_Engineering_Bay.buildTime();
			}
			StrategyIdea.academyFrame = lurkerInMyBaseFrame - UnitType.Terran_Academy.buildTime() - UnitType.Terran_Comsat_Station.buildTime();
			
		} else {
			int starportFrame = EnemyBuildTimer.Instance().getBuildStartFrameExpect(UnitType.Terran_Starport);
			if (starportFrame != CommonCode.UNKNOWN) {
				cloakingWraithFrame = starportFrame + UnitType.Terran_Starport.buildTime() + UnitType.Terran_Control_Tower.buildTime() + TechType.Cloaking_Field.researchTime();
				StrategyIdea.academyFrame = cloakingWraithFrame - UnitType.Terran_Academy.buildTime() - UnitType.Terran_Comsat_Station.buildTime();
			}
		}
		
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
			updateFactoryExpectTime();
			updateStarportExpectTime();
			// updateEngineeringExpectTime();
			// updateTurretExpectTime();
			// updateArmoryExpectTime();
		}
		
		setImportantTime();
	}

	private void updateFactoryExpectTime() {
		updateByBuilding(UnitType.Terran_Factory, UnitType.Terran_Starport);
		updateByFlagUnit(UnitType.Terran_Starport, UnitType.Terran_Vulture);
	}

	private void updateStarportExpectTime() {
		updateByBuilding(UnitType.Terran_Starport);
		updateByFlagUnit(UnitType.Terran_Starport, UnitType.Terran_Wraith);
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
	
	/// 건물에 따른 시간 예측 (또는 확정)
	private void updateByBuilding(UnitType buildingType, UnitType... nextBuildingTypes) {
		int buildFrameByStrategy = StrategyIdea.currentStrategy.buildTimeMap.frame(buildingType);
		if (buildFrameByStrategy == CommonCode.UNKNOWN) {
			buildFrameByStrategy = StrategyIdea.startStrategy.buildTimeMap.frame(buildingType);
		}
		updateByBuilding(buildFrameByStrategy, buildingType, nextBuildingTypes);
	}
	
	/// 건물에 따른 시간 확정. 연계건물(nextBuilding)에 대한 최소시간 설정
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
				// 가스가 없으면 모든 가스가 필요한 건물의 최소 빌드타임을 대충 올려놓는다.
				if (buildingType.gasPrice() > 0 && InfoUtils.enemyBaseGas() != null) {
					if (InfoUtils.enemyNumUnits(UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor, UnitType.Terran_Refinery) == 0) {
						updateBuildTimeMinimum(buildingType, gasLastCheckFrame + UnitType.Terran_Refinery.buildTime() + 10 * TimeUtils.SECOND);
					}
				}
				updateBuildTimeExpect(buildingType, buildFrameByStrategy);
			}
		}
	}

	/// 유닛에 따른 시간 확정
	private void updateByFlagUnit(UnitType buildingType, UnitType flagUnitType) {
		if (isCertainBuildTime(buildingType)) {
			return;
		}
		if (UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, flagUnitType).isEmpty()) {
			return;
		}

		int expectBuildTime = TimeUtils.elapsedFrames() - flagUnitType.buildTime() - buildingType.buildTime();

		if (buildingType == UnitType.Protoss_Citadel_of_Adun) {
			if (flagUnitType == UnitType.Protoss_Dark_Templar || flagUnitType == UnitType.Protoss_High_Templar) {
				expectBuildTime -= UnitType.Protoss_Templar_Archives.buildTime();
			}
		} else if (buildingType == UnitType.Protoss_Robotics_Facility) {
			if (flagUnitType == UnitType.Protoss_Reaver) {
				expectBuildTime -= UnitType.Protoss_Robotics_Support_Bay.buildTime();
			} else if (flagUnitType == UnitType.Protoss_Observer) {
				expectBuildTime -= UnitType.Protoss_Observatory.buildTime();
			}
		} else if (buildingType == UnitType.Zerg_Lair) {
			if (flagUnitType == UnitType.Zerg_Lurker) {
				expectBuildTime -= TechType.Lurker_Aspect.researchTime();
			} else if (flagUnitType == UnitType.Zerg_Mutalisk) {
				expectBuildTime -= UnitType.Zerg_Spire.buildTime();
			}
		}

		// flagUnit으로 미루어 building이 전략시간보다 빠른 시간에 지어진 것으로 판단되면 바꾸어 확정한다.
		int buildFrameByStrategy = StrategyIdea.currentStrategy.buildTimeMap.frame(buildingType);
		if (buildFrameByStrategy != CommonCode.UNKNOWN && buildFrameByStrategy < expectBuildTime) {
			expectBuildTime = buildFrameByStrategy;
		} else {
			buildFrameByStrategy = StrategyIdea.startStrategy.buildTimeMap.frame(buildingType);
			if (buildFrameByStrategy != CommonCode.UNKNOWN && buildFrameByStrategy < expectBuildTime) {
				expectBuildTime = buildFrameByStrategy;
			}
		}
		updateBuildTimeExpect(buildingType, expectBuildTime);
		addCertainBuildTime(buildingType);
	}

	/// 빌드시간 확정되었는지 여부
	private boolean isCertainBuildTime(UnitType buildingType) {
		return buildTimeCertain.contains(buildingType);
	}

	/// 빌드 예상시간 업데이트
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

	/// 빌드 최소시간 업데이트 (이 시간보다 빠르게 지을 수는 없다.)
	private void updateBuildTimeMinimum(UnitType buildingType, int frame) {
		if (frame == CommonCode.UNKNOWN) {
			return;
		}
		Integer buildTimeMinimum = buildTimeMinimumMap.get(buildingType);
		if (buildTimeMinimum == null || frame > buildTimeMinimum) {
			buildTimeMinimumMap.put(buildingType, frame);
		}
	}

	/// 빌드시간 확정
	private void addCertainBuildTime(UnitType buildingType) {
		buildTimeCertain.add(buildingType);
	}
}
