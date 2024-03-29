

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/**
 * 각종 Position을 찾는다.
 * 
 * campPosition : 수비 지점 attackPosition : 공격 지점
 * 
 */
public class PositionFinder {
	
	public enum CampType {
		INSIDE, FIRST_CHOKE, EXPANSION, SECOND_CHOKE, READY_TO
	}
	
	private static final int POSITION_EFFECTIVE_FRAME_SIZE = 20 * TimeUtils.SECOND;
	private Position[] enemyGroundEffectivePostions = new Position[POSITION_EFFECTIVE_FRAME_SIZE];
	private Position[] enemyAirEffectivePostions = new Position[POSITION_EFFECTIVE_FRAME_SIZE];
	
	private Position baseInsidePosition = null;
	private Map<Integer, Position> commandCenterInsidePositions = new HashMap<>();
	
	private Position basefirstChokeMiddlePosition = null;
	private Position expansionDefensePosition = null;
	private Position expansionDefensePositionSiege = null;
	
	private int watcherOtherPositionFrame = CommonCode.NONE;
	private Position watcherOtherPosition = null;
	
	
	private boolean scanAtReadyToPosition = false;
	private int scanAtReadyToOrderFrame = CommonCode.NONE;
	private boolean scanAtReadyToDisabled = false;
	
	private Map<Integer, Integer> dropUnitDiscoveredMap = new HashMap<>();
	
	/// comsatControl에서 호출한다.
	public boolean scanAtReadyToPosition() {
		if (scanAtReadyToDisabled) {
			return false;
		}
		
		if (scanAtReadyToPosition) {
			if (scanAtReadyToOrderFrame != CommonCode.NONE && TimeUtils.after(scanAtReadyToOrderFrame)) {
				// comsat을 사용한다.
				scanAtReadyToOrderFrame = CommonCode.NONE;
				scanAtReadyToPosition = false;
				scanAtReadyToDisabled = true;
				return true;
			}
		}
		return false;
	}

	private static PositionFinder instance = new PositionFinder();

	public static PositionFinder Instance() {
		return instance;
	}

	public void update() {
		StrategyIdea.campType = getCampPositionType();
		StrategyIdea.campPosition = campTypeToPosition();
		StrategyIdea.campPositionSiege = campTypeToSiegePositionForSiege();
		StrategyIdea.mainPosition = getMainPosition();

		updateMainSquadCenter();
		updateEnemyUnitPosition();
		updateWatcherPosition();
	}

	/// 주둔지
	private PositionFinder.CampType getCampPositionType() {
		int myTankSupplyCount = UnitUtils.myUnitSupplyCount(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
		int factorySupplyCount = UnitUtils.myFactoryUnitSupplyCount();
		
		int enemyGroundUnitSupplyCount = 0;
		if (InfoUtils.enemyRace() == Race.Protoss) {
			int zealotSupply = InfoUtils.enemyNumUnits(UnitType.Protoss_Zealot) * UnitType.Protoss_Zealot.supplyRequired();
			int dragoonSupply = InfoUtils.enemyNumUnits(UnitType.Protoss_Dragoon) * UnitType.Protoss_Dragoon.supplyRequired();
			int darkSupply = InfoUtils.enemyNumUnits(UnitType.Protoss_Dark_Templar) * UnitType.Protoss_Dark_Templar.supplyRequired();
			int archonSupply = InfoUtils.enemyNumUnits(UnitType.Protoss_Archon) * UnitType.Protoss_Archon.supplyRequired();
			int reaverSupply = InfoUtils.enemyNumUnits(UnitType.Protoss_Reaver) * UnitType.Protoss_Reaver.supplyRequired();
			enemyGroundUnitSupplyCount = zealotSupply + dragoonSupply + darkSupply + archonSupply + reaverSupply;
			
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			int zerglingSupply = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling) * UnitType.Zerg_Zergling.supplyRequired();
			int hydraSupply = InfoUtils.enemyNumUnits(UnitType.Zerg_Hydralisk) * UnitType.Zerg_Hydralisk.supplyRequired();
			int lurkerSupply = InfoUtils.enemyNumUnits(UnitType.Zerg_Lurker) * UnitType.Zerg_Lurker.supplyRequired();
			int ultraSupply = InfoUtils.enemyNumUnits(UnitType.Zerg_Ultralisk) * UnitType.Zerg_Ultralisk.supplyRequired();
			enemyGroundUnitSupplyCount = zerglingSupply + hydraSupply + lurkerSupply + ultraSupply;
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			// MECHANIC NO COUNT
			int marineSupply = InfoUtils.enemyNumUnits(UnitType.Terran_Marine) * UnitType.Terran_Marine.supplyRequired();
			int medicSupply = InfoUtils.enemyNumUnits(UnitType.Terran_Medic) * UnitType.Terran_Medic.supplyRequired();
			enemyGroundUnitSupplyCount = marineSupply + medicSupply;
		}
		
		// 딕텍팅이 괜찮은 경우
		// 적 클로킹유닛이 없거나 / 앞마당에 터렛이 있거나 / 1회이상 컴셋 사용 가능
		if (InfoUtils.enemyRace() == Race.Protoss || InfoUtils.enemyRace() == Race.Zerg) {
			boolean firstExpansionDetectingOk = true;
			if (UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Dark_Templar, UnitType.Protoss_Templar_Archives, UnitType.Zerg_Lurker, UnitType.Zerg_Lurker_Egg)) {
				firstExpansionDetectingOk = false;
				List<Unit> turretList = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Missile_Turret);
				for (Unit turret : turretList) {
					CommonCode.RegionType regionType = PositionUtils.positionToRegionType(turret.getPosition());
					if (regionType == CommonCode.RegionType.MY_FIRST_EXPANSION || regionType == CommonCode.RegionType.MY_THIRD_REGION) {
						firstExpansionDetectingOk = true;
						break;
					}
				}
				if (!firstExpansionDetectingOk) {
					List<Unit> scannerList = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Comsat_Station);
					for (Unit scanner : scannerList) {
						if (scanner.getEnergy() >= 50) {
							firstExpansionDetectingOk = true;
							break;
						}
					}
				}
			}

			
			// 딕텍팅이 괜찮다면 병력 수에 따라 앞마당이나 두번째 초크로 병력을 이동한다.
			if (firstExpansionDetectingOk) {
				int FIRST_EXPANSION_MARGIN = 2 * 4;
				if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DOUBLE)) {
					FIRST_EXPANSION_MARGIN = 0;
				}
				
				
				int READY_TO_SUPPLY = 27 * 4;
				if (StrategyIdea.campType == PositionFinder.CampType.READY_TO) { // 후퇴방지
					READY_TO_SUPPLY = READY_TO_SUPPLY - 12 * 4;
				}
				
				// READY TO 포지션으로 나가기 전에 자리를 잡기 위함
				int SECOND_CHOKE_SUPPLY = 12 * 4;
				if (InfoUtils.enemyRace() == Race.Zerg) {
					SECOND_CHOKE_SUPPLY = 8 * 4;
				}
				if (StrategyIdea.campType == PositionFinder.CampType.SECOND_CHOKE) { // 후퇴방지
					SECOND_CHOKE_SUPPLY = SECOND_CHOKE_SUPPLY - 4 * 4;
				}
				
				// ready to로 이동
				if (UnitUtils.myFactoryUnitSupplyCount() > READY_TO_SUPPLY && UnitUtils.availableScanningCount() >= 1) {
					return PositionFinder.CampType.READY_TO;
				}
				if (UnitUtils.myFactoryUnitSupplyCount() > SECOND_CHOKE_SUPPLY && UnitUtils.availableScanningCount() >= 1) { // ready to로 이동하기 위해 잠시 집결하는 위치
					return PositionFinder.CampType.SECOND_CHOKE;
				}
				
				// 병력이 조금 있거나 앞마당이 차지되었다면 expansion에서 방어한다.
				if (firstExpansionOccupied()
						|| myTankSupplyCount >= 3 * 4
						|| factorySupplyCount >= enemyGroundUnitSupplyCount + FIRST_EXPANSION_MARGIN) {
					return PositionFinder.CampType.EXPANSION;
				}
			}
			
//			if (InfoUtils.enemyRace() == Race.Zerg) {
//				if (!firstExpansionOccupied()) {
//					return PositionFinder.CampType.EXPANSION;
//				}
//			}
			return PositionFinder.CampType.FIRST_CHOKE;
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			
			// 병력이 쌓였다면 second choke에서 방어한다.
			if (myTankSupplyCount >= 3 * 4 && InfoUtils.myReadyToPosition() != null) {
				if (!scanAtReadyToDisabled && !scanAtReadyToPosition) {
					scanAtReadyToPosition = true;
					scanAtReadyToOrderFrame = TimeUtils.elapsedFrames() + 3 * TimeUtils.SECOND;
				}
				return PositionFinder.CampType.READY_TO;
			}
			// 병력이 조금 있거나 앞마당이 차지되었다면 expansion에서 방어한다.
			if (myTankSupplyCount >= 2 * 4 || firstExpansionOccupied()) {
				int tankCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
				if (tankCount >= 1) {
					return PositionFinder.CampType.SECOND_CHOKE;
				} else {
					return PositionFinder.CampType.EXPANSION;
				}
			}
			return PositionFinder.CampType.FIRST_CHOKE;
			
		} else {
			return PositionFinder.CampType.SECOND_CHOKE;
		}
		

//		if (InfoUtils.enemyRace() == Race.Protoss) {
////			if (InformationManager.Instance().isBlockingEnterance()) {
////			}
//			return firstChokeDefensePosition();
//			
//		} else if (InfoUtils.enemyRace() == Race.Terran) {
////			if (entranceBlocked()) { }
//			// 테란은 언덕 랜덤데미지를 활용해야 한다.
//			return firstChokeDefensePosition();
//			
//		} else { //if (InfoUtils.enemyRace() == Race.Zerg) {
//			return firstChokeDefensePosition();
//			
//			// 마린이 일정이상 쌓였어야 한다.
////			int marineCount = InfoUtils.myNumUnits(UnitType.Terran_Marine) / 2;
////			if (factorySupplyCount + marineCount > Math.max(enemyUnitCount, 3)) {
////				return firstChokeDefensePosition();
////			} else {
////				/// 커맨드센터 수비 필요
////				return commandCenterInsidePosition();
////			}
//		}
	}

	private Position campTypeToPosition() {
		PositionFinder.CampType campType = StrategyIdea.campType;
		
		Position defensePosition = defensePosition(campType);
		if (defensePosition != null) {
			return defensePosition;
		}
		
		if (campType == PositionFinder.CampType.INSIDE) {
			return baseInsidePosition();
			
		} else if (campType == PositionFinder.CampType.FIRST_CHOKE) {
			return firstChokeDefensePosition();
			
		} else if (campType == PositionFinder.CampType.EXPANSION) {
			return expansionDefensePosition();
			
		} else if (campType == PositionFinder.CampType.SECOND_CHOKE) {
			return InfoUtils.mySecondChoke().getCenter();
			
		} else { // if (campType == PositionFinder.CampType.READY_TO) {
			return InfoUtils.myReadyToPosition();
		}
	}
	
	private Position campTypeToSiegePositionForSiege() {
		PositionFinder.CampType campType = StrategyIdea.campType;
		Position defensePosition = defensePosition(campType);
		if (defensePosition != null) {
			return defensePosition;
		}

		if (campType == PositionFinder.CampType.FIRST_CHOKE || campType == PositionFinder.CampType.EXPANSION) {
			return expansionDefensePositionSiege();
		} else {
			return StrategyIdea.campPosition;
		}
	}

	private Position defensePosition(CampType campType) {
		List<Region> defenseMyRegion = new ArrayList<>();
		
		if (!InfoUtils.euiListInBase().isEmpty()) {
			Region myBaseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
			defenseMyRegion.add(myBaseRegion);
		}
		if (campType != PositionFinder.CampType.INSIDE && campType != PositionFinder.CampType.FIRST_CHOKE
				&& !InfoUtils.euiListInExpansion().isEmpty()) {
			Region myExpansionRegion = BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
			defenseMyRegion.add(myExpansionRegion);
		}
		
		if (!defenseMyRegion.isEmpty()) {
			if (StrategyIdea.nearGroundEnemyPosition != Position.Unknown && defenseMyRegion.contains(BWTA.getRegion(StrategyIdea.nearGroundEnemyPosition))) {
				return StrategyIdea.nearGroundEnemyPosition;
				
			} else if (StrategyIdea.dropEnemyPosition != Position.Unknown && defenseMyRegion.contains(BWTA.getRegion(StrategyIdea.dropEnemyPosition))) {
				return StrategyIdea.dropEnemyPosition;
			}
		}
		
		return null;
	}

	/// 메인부대 위치 지점
	private Position getMainPosition() {
		if (TimeUtils.before(StrategyIdea.findRatFinishFrame)) {
			if (InfoUtils.enemyBase() != null) {
				StrategyIdea.findRatFinishFrame = CommonCode.NONE;
			}
		}
		
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			BaseLocation enemyBase = InfoUtils.enemyBase();
			if (enemyBase == null) {
				return InfoUtils.mySecondChoke().getCenter();
			}
			
			
			if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.QUICK_ATTACK)) {
				return enemyBase.getPosition();
				
			} else {
				if (!enemyBaseDestroyed(enemyBase)) {
					// TODO 병력이 뭉쳐서 움적이기 위한 전략 getNextChoke의 업그레이드 필요
					// 백만년 조이기를 하지 않기 위해 checker로 탐색된 곳과 적 주력병력 주둔지를 고려하여
					// 안전한 위치까지 바로 전진하도록 한다.
					if (InfoUtils.enemyRace() == Race.Terran && UnitUtils.myFactoryUnitSupplyCount() < 10 * 4) {
						return InfoUtils.enemyReadyToPosition();
					}
					BaseLocation enemyFirstExpansion = InfoUtils.enemyFirstExpansion();
					if (enemyFirstExpansion != null && InfoUtils.enemyFirstExpansionOccupied()) {
						return enemyFirstExpansion.getPosition();
					} else {
						return enemyBase.getPosition();
					}
					
				} else {
					return letsfindRatPosition();
				}
			}

		} else {
			return StrategyIdea.campPosition;
		}
	}

	private void updateMainSquadCenter() {
		Squad squad = CombatManager.Instance().squadData.getSquad(MicroConfig.SquadInfo.MAIN_ATTACK.squadName);
		Unit leader = UnitUtils.leaderOfUnit(squad.unitList);
		if (leader != null) {
			Position centerPosition = UnitUtils.centerPositionOfUnit(squad.unitList, leader.getPosition(), 800);
			StrategyIdea.mainSquadCoverRadius = 200 + (int) (Math.log(squad.unitList.size()) * 60);
			StrategyIdea.mainSquadCenter = centerPosition;
			StrategyIdea.mainSquadLeaderPosition = leader.getPosition();
		} else {
			StrategyIdea.mainSquadCoverRadius = 255;
			StrategyIdea.mainSquadCenter = StrategyIdea.campPosition;
			StrategyIdea.mainSquadLeaderPosition = StrategyIdea.campPosition;
		}
		
		// readyToPosition을 지나쳤는지 여부
		Position leaderPosition = StrategyIdea.mainSquadLeaderPosition;
		Position myReadyTo = InfoUtils.myReadyToPosition();
		Position enemyReadyTo = InfoUtils.enemyReadyToPosition();
		if (!PositionUtils.isAllValidPosition(leaderPosition, myReadyTo, enemyReadyTo)) {
			return;
		}
		
		double enemyToLeader = enemyReadyTo.getDistance(leaderPosition);
		double enemyToReadyTo = enemyReadyTo.getDistance(myReadyTo);
		StrategyIdea.mainSquadCrossBridge = enemyToLeader < enemyToReadyTo + 150;
	}

	private void updateEnemyUnitPosition() {
		int sumOfTotalX = 0;
		int sumOfTotalY = 0;
		int totalCount = 0;

		List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL);
		for (UnitInfo eui : euiList) {
			if (!TargetFilter.excludeByFilter(eui, TargetFilter.LARVA_LURKER_EGG|TargetFilter.UNFIGHTABLE|TargetFilter.SPIDER_MINE|TargetFilter.BUILDING)) {
				sumOfTotalX += eui.getLastPosition().getX();
				sumOfTotalY += eui.getLastPosition().getY();
				totalCount++;
			}
		}

		Set<UnitInfo> euiListNear = new HashSet<>();
		euiListNear.addAll(InfoUtils.euiListInBase());
		euiListNear.addAll(InfoUtils.euiListInExpansion());
		euiListNear.addAll(InfoUtils.euiListInThirdRegion());
		euiListNear.addAll(UnitUtils.getEnemyUnitInfosInRadius(TargetFilter.GROUND_UNIT|TargetFilter.INVISIBLE|TargetFilter.UNFIGHTABLE,
				InfoUtils.myBase().getPosition(), 550, true, true));
		
		int sumOfAirX = 0;
		int sumOfAirY = 0;
		int airCount = 0;
		
		int sumOfGroundX = 0;
		int sumOfGroundY = 0;
		int groundCount = 0;
		
		int sumOfDropX = 0;
		int sumOfDropY = 0;
		int dropCount = 0;
		
		for (UnitInfo eui : euiListNear) {
			if (!MicroUtils.combatEnemyType(eui.getType())) {
				continue;
			}
			if (UnitUtils.unitInSight(eui) == null) {
				continue;
			}
			if (eui.getType() == UnitType.Zerg_Overlord) {
				continue;
			}
			
			if (eui.getType() == UnitType.Terran_Dropship || eui.getType() == UnitType.Protoss_Shuttle) {
				if (dropUnitDiscoveredMap.get(eui.getUnitID()) == null) {
					dropUnitDiscoveredMap.put(eui.getUnitID(), TimeUtils.elapsedFrames());
				}
				
				Integer discoveredFrame = dropUnitDiscoveredMap.get(eui.getUnitID());
				if (TimeUtils.elapsedFrames(discoveredFrame) < 15 * TimeUtils.SECOND) {
					sumOfDropX += eui.getLastPosition().getX();
					sumOfDropY += eui.getLastPosition().getY();
					dropCount++;
				}
				
			} else if (eui.getType().isFlyer()) {
				sumOfAirX += eui.getLastPosition().getX();
				sumOfAirY += eui.getLastPosition().getY();
				airCount++;
				
			} else {
				sumOfGroundX += eui.getLastPosition().getX();
				sumOfGroundY += eui.getLastPosition().getY();
				groundCount++;
			}
		}
		

		Position totalEnemyCneterPosition = Position.Unknown;
		Position nearGroundEnemyPosition = Position.Unknown;
		Position nearAirEnemyPosition = Position.Unknown;
		Position dropEnemyPosition = Position.Unknown;
		
		if (totalCount > 0) {
			totalEnemyCneterPosition = new Position(sumOfTotalX / totalCount, sumOfTotalY / totalCount).makeValid();
		}
		if (groundCount > 0) {
			nearGroundEnemyPosition = new Position(sumOfGroundX / groundCount, sumOfGroundY / groundCount).makeValid();
		}
		if (airCount > 0) {
			nearAirEnemyPosition = new Position(sumOfAirX / airCount, sumOfAirY / airCount).makeValid();
		}
		if (dropCount > 0) {
			dropEnemyPosition = new Position(sumOfDropX / dropCount, sumOfDropY / dropCount).makeValid();
		}
		
		enemyGroundEffectivePostions[TimeUtils.elapsedFrames() % POSITION_EFFECTIVE_FRAME_SIZE] = nearGroundEnemyPosition;
		enemyAirEffectivePostions[TimeUtils.elapsedFrames() % POSITION_EFFECTIVE_FRAME_SIZE] = nearAirEnemyPosition;

		StrategyIdea.totalEnemyCneterPosition = totalEnemyCneterPosition;
		StrategyIdea.nearGroundEnemyPosition = nearGroundEnemyPosition;
		StrategyIdea.nearAirEnemyPosition = nearAirEnemyPosition;
		StrategyIdea.dropEnemyPosition = dropEnemyPosition;
		
		// 적 상태
		StrategyCode.EnemyUnitStatus enemyStatus;
		
		if (!InfoUtils.euiListInBase().isEmpty()) {
			enemyStatus = StrategyCode.EnemyUnitStatus.IN_MY_REGION;
		} else {
			if (StrategyIdea.nearGroundEnemyPosition != Position.Unknown
					|| StrategyIdea.nearAirEnemyPosition != Position.Unknown
					|| StrategyIdea.dropEnemyPosition != Position.Unknown) {
				enemyStatus = StrategyCode.EnemyUnitStatus.COMMING;	
			} else {
				enemyStatus = StrategyCode.EnemyUnitStatus.SLEEPING;
			}
		}
		StrategyIdea.enemyUnitStatus = enemyStatus;
	}
	
	private void updateWatcherPosition() {
		Position watcherPosition = Position.Unknown;
		
		if (StrategyIdea.mainSquadMode.isAttackMode && InfoUtils.enemyRace() != Race.Terran) {
			watcherPosition = StrategyIdea.mainPosition;
		}
		
		// watcher 방어모드1
		if (watcherPosition == Position.Unknown) {
			if (PositionUtils.isValidGroundPosition(StrategyIdea.nearGroundEnemyPosition)) {
				watcherPosition = StrategyIdea.nearGroundEnemyPosition;
			}
		}
		
		// watcher 방어모드2
		if (watcherPosition == Position.Unknown) {
			Set<UnitInfo> euiListInBase = InfoUtils.euiListInBase();
			for (UnitInfo eui : euiListInBase) {
				if (!eui.getType().isFlyer()) {
					watcherPosition = eui.getLastPosition();
					break;
				}
			}
		}

		// watcher 방어모드(특수)
		if (watcherPosition == Position.Unknown) {
			if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DEFENSE_DROP)) {
				// 드롭인 경우 본진과 세컨초크를 왓다리갓다리 한다.
				int nSeconds = 12 * TimeUtils.SECOND;
				int zeroToNSeconds = MyBotModule.Broodwar.getFrameCount() % nSeconds;
				if (zeroToNSeconds < (nSeconds / 2)) {
					watcherPosition = InfoUtils.myBase().getPosition();	
				} else {
					watcherPosition = InfoUtils.mySecondChoke().getCenter();
				}
				
			} else if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DEFENSE_FRONT)) {
				
				// 앞라인 방어인 경우 second choke (단, 딕텍팅을 대비하는 경우라면, 시간에 맞추어서 secondChoke로 변경)
				if (!StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DETECT_IMPORTANT) || TimeUtils.after(StrategyIdea.turretBuildStartFrame)) {
					watcherPosition = InfoUtils.mySecondChoke().getCenter();
				} else if (InfoUtils.enemyBase() != null) {
					watcherPosition = InfoUtils.enemyBase().getPosition();
				}
			}
		}
		
		// watcher 특수 포지션
		if (watcherPosition == Position.Unknown) {
			if (InfoUtils.enemyBase() != null) {
				// 대략적으로다가 지상유닛에 안전한 상황에서는 watcher를 다른 곳으로 돌린다.
				if (watcherOtherPosition != null) {
					watcherPosition = watcherOtherPosition;
				} else {
					watcherPosition = watcherSpecialPosition();
				}
			}
		}

		// watcher 기본 포지션
		if (watcherPosition == Position.Unknown) {
			if (InfoUtils.enemyBase() != null) {
				if (InfoUtils.enemyRace() == Race.Terran && MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 0) {
					watcherPosition = InfoUtils.enemyReadyToPosition();
				} else {
					watcherPosition = InfoUtils.enemyBase().getPosition();
				}
					
			} else {
				watcherPosition = InfoUtils.mySecondChoke().getCenter();
			}
		}
		
		StrategyIdea.watcherPosition = watcherPosition;
	}
	
	public Position watcherSpecialPosition() {
		if (StrategyIdea.mainSquadMode.isAttackMode && InfoUtils.enemyRace() != Race.Terran) {
			return Position.Unknown;
		}

		// 1. 방어가 필요하고 벌처로 방어가능한 내 멀티
		List<BaseLocation> myOccupiedBases = InfoUtils.myOccupiedBases();
		if (!myOccupiedBases.isEmpty()) {
			BaseLocation myOccupiedNeedDefense = null;
			Set<UnitInfo> enemyUnitInfosInRadius = new HashSet<>();
			Position centerPosition = TilePositionUtils.getCenterTilePosition().toPosition();
			for (BaseLocation occupiedBase : myOccupiedBases) {
				if (occupiedBase.getPosition().getDistance(centerPosition) < 500) {
					continue;
				}
				enemyUnitInfosInRadius = UnitUtils.getEnemyUnitInfosInRadius(TargetFilter.AIR_UNIT|TargetFilter.UNFIGHTABLE, occupiedBase.getPosition(), 500, true, false);
				if (enemyUnitInfosInRadius.isEmpty()) {
					continue;
				}
				// 만약 이길수 있다면
				Squad watcherSquad = CombatManager.Instance().squadData.getSquadMap().get(MicroConfig.SquadInfo.WATCHER.squadName);
				int enemyPower = VultureFightPredictor.powerOfEnemiesByUnitInfo(enemyUnitInfosInRadius);
				int watcherPower = VultureFightPredictor.powerOfWatchers(watcherSquad.unitList);
				if (watcherPower > enemyPower) {
					myOccupiedNeedDefense = occupiedBase;
					break;
				}
			}
			
			if (myOccupiedNeedDefense != null) {
				watcherOtherPositionFrame = TimeUtils.elapsedFrames();	
				watcherOtherPosition = myOccupiedNeedDefense.getPosition();
				return watcherOtherPosition;
			}
		}
		
//		boolean canGoOtherPosition = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode) >= 10;
//		
//		if (!canGoOtherPosition) {
//			if (InfoUtils.enemyRace() == Race.Zerg) {
//				int enemyGroundUnitPower = UnitUtils.enemyGroundUnitPower();
//				int zergingCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling);
//				canGoOtherPosition = enemyGroundUnitPower <= 2 && zergingCount <= 2;
//					
//			} else if (InfoUtils.enemyRace() == Race.Protoss) {
//				int enemyGroundUnitPower = UnitUtils.enemyGroundUnitPower();
//				canGoOtherPosition = enemyGroundUnitPower <= 3;
//			}	
//		}
//		
		// 2. 내 first expansion과 가까운 적 멀티 (시야가 밝혀지지 않은)
		if (InfoUtils.enemyRace() == Race.Terran) {
			List<BaseLocation> enemyOccupiedBases = InfoUtils.enemyOccupiedBases();
			if (!enemyOccupiedBases.isEmpty()) {
				Position centerPosition = TilePositionUtils.getCenterTilePosition().toPosition();
				BaseLocation enemyOccupied = BaseLocationUtils.getGroundClosestBaseToPosition(enemyOccupiedBases, InfoUtils.enemyFirstExpansion(), new IConditions.BaseCondition() {
					@Override public boolean correspond(BaseLocation base) {
						if (base.equals(InfoUtils.enemyBase()) || base.equals(InfoUtils.enemyFirstExpansion())) {
							return false;
						}
						if (base.getPosition().getDistance(centerPosition) < 500) {
							return false;
						}
//						Set<UnitInfo> enemyUnitInfosInRadius = UnitUtils.getEnemyUnitInfosInRadius(TargetFilter.AIR_UNIT|TargetFilter.UNFIGHTABLE, base.getPosition(), 500, true, false);
//						if (enemyUnitInfosInRadius.isEmpty()) {
//							return false;
//						}
						return true;
					}
				});
				if (enemyOccupied != null) {
					watcherOtherPositionFrame = TimeUtils.elapsedFrames();	
					watcherOtherPosition = enemyOccupied.getPosition();
					return watcherOtherPosition;
				}
			}
		}
		
		return Position.Unknown;
	}
	
	/// 첫번째 확장기지를 차지하였는지 여부
	private boolean firstExpansionOccupied() {
		List<Unit> commandCenterOrDefenseTowerList = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL,
				UnitType.Terran_Command_Center, UnitType.Terran_Missile_Turret, UnitType.Terran_Bunker);
		for (Unit bunkerOrTurret : commandCenterOrDefenseTowerList) {
			CommonCode.RegionType towerRegionType = PositionUtils.positionToRegionType(bunkerOrTurret.getPosition());
			if (towerRegionType == CommonCode.RegionType.MY_FIRST_EXPANSION || towerRegionType == CommonCode.RegionType.MY_THIRD_REGION) {
				return true;
			}
		}
		
		return false;
	}

	public Position baseInsidePosition() {
		if (baseInsidePosition != null) {
			return baseInsidePosition;
		}
		
		Position basePosition = InfoUtils.myBase().getPosition();
		Position bunkerPosition = null;
		if (InfoUtils.enemyRace() == Race.Zerg) {
			TilePosition bunker = BlockingEntrance.Instance().bunker1;
			if (bunker != null) {
				bunkerPosition = bunker.toPosition();
			}
		}
		
		if (bunkerPosition != null) {
			int x = basePosition.getX() + bunkerPosition.getX();
			int y = basePosition.getY() + bunkerPosition.getY();
			return baseInsidePosition = new Position(x / 2, y / 2).makeValid();
		} else {
			return basePosition;
		}
	}
	
	/// 커맨드센터와 미네랄 사이의 방어지역
	public Position commandCenterInsidePosition() {
		TilePosition baseTile = InfoUtils.myBase().getTilePosition();
		List<Unit> commandCenterList = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		
		for (Unit commandCenter : commandCenterList) {
			if (commandCenter.getTilePosition().equals(baseTile)) {
				return commandCenterInsidePosition(commandCenter);
			}
		}
		return InfoUtils.myBase().getPosition();
	}

	/// 커맨드센터와 미네랄 사이의 방어지역
	public Position commandCenterInsidePosition(Unit commandCenter) {
		Position insidePosition = commandCenterInsidePositions.get(commandCenter.getID());
		if (insidePosition != null) {
			return insidePosition;
		}

		int x = 0;
		int y = 0;
		int mineralCnt = 0;

		for (Unit mineral : MyBotModule.Broodwar.neutral().getUnits()) {
			if ((mineral.getType() == UnitType.Resource_Mineral_Field) && mineral.getDistance(commandCenter) < 320) {
				x += mineral.getPosition().getX();
				y += mineral.getPosition().getY();
				mineralCnt++;
			}
		}
		if (mineralCnt == 0) {
			return commandCenter.getPosition();
		}
		int finalx = x / mineralCnt;
		int finaly = y / mineralCnt;
		finalx = (finalx + commandCenter.getPosition().getX()) / 2;
		finaly = (finaly + commandCenter.getPosition().getY()) / 2;

		insidePosition = new Position(finalx, finaly).makeValid();
		commandCenterInsidePositions.put(commandCenter.getID(), insidePosition);
		return insidePosition;
	}
		
	public Position baseFirstChokeMiddlePosition() {
		if (firstExpansionOccupied()) {
			return InfoUtils.myFirstChoke().getCenter();
			
		} else {
			if (this.basefirstChokeMiddlePosition != null) {
				return basefirstChokeMiddlePosition;
			}
			Position firstChokePosition = InfoUtils.myFirstChoke().getCenter();
			Position myBasePosition = InfoUtils.myBase().getPosition();
			double radian = MicroUtils.targetDirectionRadian(firstChokePosition, myBasePosition);

			return basefirstChokeMiddlePosition = MicroUtils.getMovePosition(firstChokePosition, radian, 500);
		}
	}

	/// second choke보다 안쪽 포지션
	public Position expansionDefensePosition() {
		if (expansionDefensePosition != null) {
			return expansionDefensePosition;
		}
		
		Chokepoint firstChoke = InfoUtils.myFirstChoke();
		Chokepoint secondChoke = InfoUtils.mySecondChoke();
		BaseLocation firstExpansion = InfoUtils.myFirstExpansion();
		
		int x = (firstChoke.getX() + secondChoke.getX() + firstExpansion.getPosition().getX()) / 3;
		int y = (firstChoke.getY() + secondChoke.getY() + firstExpansion.getPosition().getY()) / 3;
		return expansionDefensePosition = new Position(x, y);

		//First Expansion에서 약간 물러난 위치 (prebot 1 - overwatch, circuit 맵에 맞지 않음)
//		double distanceFromFirstChoke = firstChoke.getDistance(secondChoke);
//		double distanceFromExpansion = firstExpansion.getDistance(secondChoke);
//		if (distanceFromFirstChoke < distanceFromExpansion) {
//			return firstChoke.getCenter();
//		} else {
//			int x = (firstChoke.getX() + firstExpansion.getX()) / 2;
//			int y = (firstChoke.getY() + firstExpansion.getY()) / 2;
//			return new Position(x, y);
//		}
	}
	
	public Position expansionDefensePositionSiege() {
		if (expansionDefensePositionSiege != null) {
			return expansionDefensePositionSiege;
		}
		Position myBasePosition = InfoUtils.myBase().getPosition();
		Position firstChokePosition = InfoUtils.myFirstChoke().getCenter();
		
		Pair<Position, Position> secondChokeSides = InfoUtils.mySecondChoke().getSides();
		double firstDistance = myBasePosition.getDistance(secondChokeSides.first);
		double secondDistance = myBasePosition.getDistance(secondChokeSides.second);
		
		Position positionBaseSided = firstDistance < secondDistance ? secondChokeSides.first : secondChokeSides.second;
		
		Position defensePosition = null;

		if (InfoUtils.enemyRace() == Race.Zerg) {
			double radian1 = MicroUtils.targetDirectionRadian(positionBaseSided, myBasePosition);
			defensePosition = MicroUtils.getMovePosition(positionBaseSided, radian1, 210);
			
			double radian2 = MicroUtils.targetDirectionRadian(defensePosition, firstChokePosition);
			defensePosition = MicroUtils.getMovePosition(defensePosition, radian2, 30);
			
		} else {
			double radian1 = MicroUtils.targetDirectionRadian(positionBaseSided, myBasePosition);
			defensePosition = MicroUtils.getMovePosition(positionBaseSided, radian1, 170);
			
			double radian2 = MicroUtils.targetDirectionRadian(defensePosition, firstChokePosition);
			defensePosition = MicroUtils.getMovePosition(defensePosition, radian2, 30);
		}
		
		return expansionDefensePositionSiege = defensePosition;
	}
	
	/// First Choke Point 방어지역
	public Position firstChokeDefensePosition() {
		Position firstChokePosition = InfoUtils.myFirstChoke().getCenter();
		Position firstChokeDefensePosition = firstChokePosition;
		double radian = 0;
		if (InfoUtils.enemyRace() == Race.Zerg) {
			Position myBasePosition = InfoUtils.myBase().getPosition();
			radian = MicroUtils.targetDirectionRadian(firstChokePosition, myBasePosition);
			firstChokeDefensePosition = MicroUtils.getMovePosition(firstChokePosition, radian, 250);
		}else{
			/*Position firstSupplePos = BlockingEntrance.Instance().first_supple.toPosition();
			radian = MicroUtils.targetDirectionRadian(firstChokePosition, firstSupplePos);
			Position fleeVector = new Position((int) (10 * Math.cos(radian)),
					(int) (10 * Math.sin(radian))); // 이동벡터
			firstChokeDefensePosition = new Position(firstSupplePos.getX() + fleeVector.getX(),
					firstSupplePos.getY() + fleeVector.getY());*/
			firstChokeDefensePosition = InformationManager.Instance().isSafePosition();
	
		}
		if (PositionUtils.isValidPosition(firstChokeDefensePosition)) {
			return firstChokeDefensePosition;
		} else {
			return firstChokePosition;
		}
	}

	public boolean enemyBaseDestroyed(BaseLocation enemyBase) {
		if (!MyBotModule.Broodwar.isExplored(enemyBase.getTilePosition())) {
			return false;
		}

		Set<UnitInfo> euis = UnitUtils.getEnemyUnitInfosInRadius(TargetFilter.NO_FILTER, enemyBase.getPosition(), 300, false, false,
				UnitType.Protoss_Nexus, UnitType.Terran_Command_Center, UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive);
		return euis.isEmpty();
	}

	// 쥐 함수
	private Position letsfindRatPosition() {
		List<UnitInfo> enemyResourceDepots = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL, UnitType.Terran_Command_Center, UnitType.Protoss_Nexus
				, UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive);
		
		if (!enemyResourceDepots.isEmpty()) {
			return enemyResourceDepots.get(0).getLastPosition();
		}
		
		// 적 건물
		for (UnitInfo eui : InfoUtils.enemyUnitInfoMap().values()) {
			if (eui.getType().isBuilding() && PositionUtils.isValidPosition(eui.getLastPosition())) {
				return eui.getLastPosition();
			}
		}

		// 적 유닛
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (unit.getType() == UnitType.Zerg_Larva || unit.getType().isFlyer() || !unit.isVisible()) {
				continue;
			}
			return unit.getPosition();
		}

		// starting location 중에서 탐험되지 않은 지역
		List<BaseLocation> startingBases = InfoUtils.mapInformation().getStartingBaseLocation();
		for (BaseLocation startingBase : startingBases) {
			if (!MyBotModule.Broodwar.isExplored(startingBase.getTilePosition())) {
				return startingBase.getPosition();
			}
		}

		// 앞마당 지역
		BaseLocation enemyExpansion = InfoUtils.enemyFirstExpansion();
		if (enemyExpansion != null && !MyBotModule.Broodwar.isExplored(enemyExpansion.getTilePosition())) {
			return enemyExpansion.getPosition();
		}
		
		// 제 3멀티 중에서 탐험되지 않은 지역
		List<BaseLocation> otherExpansions = InfoUtils.enemyOtherExpansions();
		if (otherExpansions != null) {
			for (BaseLocation otherExpansion : otherExpansions) {
				if (!MyBotModule.Broodwar.isExplored(otherExpansion.getTilePosition())) {
					return otherExpansion.getPosition();
				}
			}
		}
		StrategyIdea.findRatFinishFrame = TimeUtils.elapsedFrames() + 10 * TimeUtils.SECOND;
		return new Position(2222, 2222);
	}

	public boolean otherPositionTimeUp(Unit regroupLeader) {
		if (watcherOtherPosition == null) {
			return false;
		}
		
		// 일정시간 경과 후퇴
		if (TimeUtils.elapsedSeconds(watcherOtherPositionFrame) > 90) {
			watcherOtherPosition = null;
			return true;
		}
		
		// 일꾼이 없으면 후퇴
		if (regroupLeader.getDistance(watcherOtherPosition) < 200) {
			Set<UnitInfo> euis = UnitUtils.getEnemyUnitInfosInRadius(TargetFilter.AIR_UNIT|TargetFilter.UNFIGHTABLE, watcherOtherPosition, 500, true, false);
			if (euis.isEmpty()) {
				watcherOtherPosition = null;
				return true;
			}
		}
		
		return false;
	}

	//
	// private Position getNextChokePosition(Squad squad) {
	// BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
	// Chokepoint enemyFirstChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
	//
	// if (squad.getName().equals(SquadName.MAIN_ATTACK) && enemyBaseLocation != null && enemyFirstChoke != null) {
	// if (squad.getUnitSet().isEmpty()) {
	// StrategyIdea.currTargetChoke = null;
	// currTargetChokeExpiredFrame = 0;
	// return null;
	// }
	//
	// Position squadPosition = MicroUtils.centerOfUnits(squad.getUnitSet());
	// Position enemyFirstChokePosition = enemyFirstChoke.getCenter();
	// if (currTargetChokeExpiredFrame != 0
	// && currTargetChokeExpiredFrame < Prebot.Broodwar.getFrameCount()) { // 적의 first chokePoint 도착
	// return enemyBaseLocation.getPosition(); // TODO 언덕 위로 올라가는 것에 대한 판단. 상대의 주력병력을 소모시키전에 언덕위로 진입하는 것은 위험할 수 있다.
	// }
	//
	// // 현재 주력병력이 어느 chokepoint에 위치해있는가?(가까운 chokepoint)
	// if (StrategyIdea.currTargetChoke == null) {
	// Chokepoint closestChoke = null;
	// double closestDist = 999999;
	// for (Chokepoint choke : BWTA.getChokepoints()) {
	// double distToChoke = squadPosition.getDistance(choke.getCenter());
	// if (distToChoke < closestDist) {
	// closestChoke = choke;
	// closestDist = distToChoke;
	// }
	// }
	// StrategyIdea.currTargetChoke = closestChoke;
	// }
	//
	// // 현재의 chokepoint에 도착했다고 판단되면 next chokepoint를 찾는다.
	// // next chokepoint는 최단거리 상에 있는 가장 가까운 chokepoint이다.
	// boolean nextChokeFind = false;
	// if (currTargetChokeExpiredFrame < Prebot.Broodwar.getFrameCount()) {
	// nextChokeFind = true;
	// }
	//
	// if (nextChokeFind) {
	// if (squadPosition.equals(StrategyIdea.currTargetChoke)) {
	// return null;
	// }
	//
	// int chokeToEnemyChoke = PositionUtils.getGroundDistance(StrategyIdea.currTargetChoke.getCenter(), enemyFirstChokePosition); // 현재chokepoint ~ 목적지chokepoint
	//
	// Chokepoint nextChoke = null;
	// int closestChokeToNextChoke = 999999;
	// for (Chokepoint choke : BWTA.getChokepoints()) {
	// if (choke.equals(StrategyIdea.currTargetChoke)) {
	// continue;
	// }
	// int chokeToNextChoke = PositionUtils.getGroundDistance(StrategyIdea.currTargetChoke.getCenter(), choke.getCenter()); // 현재chokepoint ~ 다음chokepoint
	// int nextChokeToEnemyChoke = PositionUtils.getGroundDistance(choke.getCenter(), enemyFirstChokePosition); // 다음chokepoint ~ 목적지chokepoint
	// if (chokeToNextChoke + nextChokeToEnemyChoke < chokeToEnemyChoke + 10 && chokeToNextChoke > 10 && chokeToNextChoke < closestChokeToNextChoke) { // 최단거리 오차범위 10 * 32
	// nextChoke = choke;
	// closestChokeToNextChoke = chokeToNextChoke;
	// }
	// }
	// if (nextChoke != null) {
	// StrategyIdea.currTargetChoke = nextChoke;
	// currTargetChokeExpiredFrame = Prebot.Broodwar.getFrameCount() + (int) (closestChokeToNextChoke * 24.0 * getWaitingPeriod());
	// }
	// }
	// }
	// if (StrategyIdea.currTargetChoke != null) {
	// return StrategyIdea.currTargetChoke.getCenter();
	// } else {
	// return null;
	// }
	// }

}
