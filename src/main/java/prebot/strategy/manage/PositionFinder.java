package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.CombatManager;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.squad.Squad;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;

/**
 * 각종 Position을 찾는다.
 * 
 * campPosition : 수비 지점 attackPosition : 공격 지점
 * 
 */
public class PositionFinder {
	
	private static final int POSITION_EFFECTIVE_FRAME_SIZE = 20 * TimeUtils.SECOND;
	private Position[] enemyGroundEffectivePostions = new Position[POSITION_EFFECTIVE_FRAME_SIZE];
	private Position[] enemyAirEffectivePostions = new Position[POSITION_EFFECTIVE_FRAME_SIZE];
	
	private Position commandCenterInsidePosition = null;
	private Position basefirstChokeMiddlePosition = null;
	
	private static PositionFinder instance = new PositionFinder();

	public static PositionFinder Instance() {
		return instance;
	}

	public void update() {
		StrategyIdea.campPosition = getCampPosition();
		StrategyIdea.mainPosition = getMainPosition();

		updateMainSquadCenter();
		updateEnemyUnitPosition();
		updateWatcherPosition();
	}

	/// 주둔지
	private Position getCampPosition() {
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
			if (UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Dark_Templar, UnitType.Protoss_Templar_Archives, UnitType.Zerg_Lurker, UnitType.Zerg_Egg)) {
				firstExpansionDetectingOk = false;
				List<Unit> turretList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Missile_Turret);
				for (Unit turret : turretList) {
					RegionType regionType = PositionUtils.positionToRegionType(turret.getPosition());
					if (regionType == RegionType.MY_FIRST_EXPANSION) {
						firstExpansionDetectingOk = true;
						break;
					}
				}
				if (!firstExpansionDetectingOk) {
					List<Unit> scannerList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Comsat_Station);
					for (Unit scanner : scannerList) {
						if (scanner.getEnergy() >= 50) {
							firstExpansionDetectingOk = true;
							break;
						}
					}
				}
			}

//			System.out.println("facto : " + factorySupplyCount);
//			System.out.println("enemy : " + enemyGroundUnitSupplyCount);
//			System.out.println("########################################################");
			
			// 딕텍팅이 괜찮다면 병력 수에 따라 앞마당이나 두번째 초크로 병력을 이동한다.
			if (firstExpansionDetectingOk) {
				int SECOND_CHOKE_MARGIN = 15 * 4; // TODO 추후 상수로 변경
				int FIRST_EXPANSION_MARGIN = 8 * 4; // TODO 추후 상수로 변경
				if (StrategyIdea.buildTimeMap.featureEnabled(Feature.DOUBLE)) {
					SECOND_CHOKE_MARGIN = 2 * 4;
					FIRST_EXPANSION_MARGIN = 0;
				}
				
				// 병력이 쌓였다면 second choke에서 방어한다.
				if (myTankSupplyCount >= 10 * 4
						|| factorySupplyCount >= enemyGroundUnitSupplyCount + SECOND_CHOKE_MARGIN) {
					return InfoUtils.mySecondChoke().getCenter();
				}
				// 병력이 조금 있거나 앞마당이 차지되었다면 expansion에서 방어한다.
				if (myTankSupplyCount >= 4 * 4
						|| factorySupplyCount >= enemyGroundUnitSupplyCount + FIRST_EXPANSION_MARGIN
						|| firstExpansionOccupied()) {
					return firstExpansionBackwardPosition();
				}
			}
			
			if (InfoUtils.enemyRace() == Race.Zerg) {
				if (factorySupplyCount > 4 * 1) {
					return firstChokeDefensePosition();
				}
				// 마린이 일정이상 쌓이지 않았다면 커맨드센터 수비
				int marineCount = InfoUtils.myNumUnits(UnitType.Terran_Marine) / 2;
				if (marineCount <= Math.max(enemyGroundUnitSupplyCount, 3)) {
					return commandCenterInsidePosition();
				}
			}
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			// 병력이 쌓였다면 second choke에서 방어한다.
			if (myTankSupplyCount >= 6 * 4 && InfoUtils.myReadyToPosition() != null) {
				return InfoUtils.myReadyToPosition();
			}
			// 병력이 조금 있거나 앞마당이 차지되었다면 expansion에서 방어한다.
			if (myTankSupplyCount >= 3 * 4 || firstExpansionOccupied()) {
				return InfoUtils.mySecondChoke().getCenter();
			}
		}

		return firstChokeDefensePosition();
		

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

	/// 메인부대 위치 지점
	private Position getMainPosition() {
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			BaseLocation enemyBase = InfoUtils.enemyBase();
			if (enemyBase == null) {
				return InfoUtils.mySecondChoke().getCenter();
			}
			
			if (!enemyBaseDestroyed(enemyBase)) {
				if (StrategyIdea.mainSquadMode == MainSquadMode.SPEED_ATTCK) {
					return enemyBase.getPosition();
				} else {
					// TODO 병력이 뭉쳐서 움적이기 위한 전략 getNextChoke의 업그레이드 필요
					// 백만년 조이기를 하지 않기 위해 checker로 탐색된 곳과 적 주력병력 주둔지를 고려하여
					// 안전한 위치까지 바로 전진하도록 한다.
					return enemyBase.getPosition();
				}
			} else {
				return letsfindRatPosition();
			}

		} else {
			return getCampPosition();
		}
	}

	private void updateMainSquadCenter() {
		Squad squad = CombatManager.Instance().squadData.getSquad(SquadInfo.MAIN_ATTACK.squadName);
		Unit leader = UnitUtils.leaderOfUnit(squad.unitList);
		if (leader != null) {
			Position centerPosition = UnitUtils.centerPositionOfUnit(squad.unitList, leader.getPosition(), 800);
			StrategyIdea.mainSquadCoverRadius = 250 + (int) (Math.log(squad.unitList.size()) * 80);
			StrategyIdea.mainSquadCenter = centerPosition;
		} else {
			StrategyIdea.mainSquadCoverRadius = 250;
			StrategyIdea.mainSquadCenter = StrategyIdea.campPosition;
		}
	}

	private void updateEnemyUnitPosition() {
		int sumOfTotalX = 0;
		int sumOfTotalY = 0;
		int totalCount = 0;
		
		int sumOfAirX = 0;
		int sumOfAirY = 0;
		int airCount = 0;
		
		int sumOfGroundX = 0;
		int sumOfGroundY = 0;
		int groundCount = 0;

		Position myFirstExpansionPosition = InfoUtils.myFirstExpansion().getPosition();

		List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL);
		List<UnitInfo> closeEuiList = new ArrayList<>();

		double closestDistance = CommonCode.DOUBLE_MAX;
		UnitInfo closestEui = null;
		for (UnitInfo eui : euiList) {
			if (eui.getType().isWorker() || eui.getType().isBuilding()) {
				continue;
			}
			if (eui.getType().groundWeapon() == WeaponType.None && eui.getType().airWeapon() == WeaponType.None) {
				continue;
			}
			if (eui.getType() == UnitType.Terran_Vulture_Spider_Mine) {
				continue;
			}
			if (UnitUtils.ignorableEnemyUnitInfo(eui)) {
				continue;
			}
			double distance = myFirstExpansionPosition.getDistance(eui.getLastPosition());
			if (distance <= 1250) {
				if (distance < closestDistance) {
					closestEui = eui;
					closestDistance = distance;
				}
				closeEuiList.add(eui);
			}
			sumOfTotalX += eui.getLastPosition().getX();
			sumOfTotalY += eui.getLastPosition().getY();
			totalCount++;
		}
		
		for (UnitInfo eui : closeEuiList) {
			Position euiPosition = eui.getLastPosition();
			Position closestEuiPosition = closestEui.getLastPosition();
			if (euiPosition.getDistance(closestEuiPosition) > 400) {
				continue;
			}
			
			if (eui.getType().isFlyer()) {
				sumOfAirX += euiPosition.getX();
				sumOfAirY += euiPosition.getY();
				airCount++;				
			} else {
				sumOfGroundX += euiPosition.getX();
				sumOfGroundY += euiPosition.getY();
				groundCount++;
			}
		}
		

		Position totalEnemyCneterPosition;
		Position nearGroundEnemyPosition;
		Position nearAirEnemyPosition;
		if (totalCount > 0) {
			totalEnemyCneterPosition = new Position(sumOfTotalX / totalCount, sumOfTotalY / totalCount);
		} else {
			totalEnemyCneterPosition = Position.Unknown;
		}
		if (groundCount > 0) {
			nearGroundEnemyPosition = new Position(sumOfGroundX / groundCount, sumOfGroundY / groundCount);
		} else {
			nearGroundEnemyPosition = Position.Unknown;
		}
		if (airCount > 0) {
			nearAirEnemyPosition = new Position(sumOfAirX / airCount, sumOfAirY / airCount);
		} else {
			nearAirEnemyPosition = Position.Unknown;
		}
		enemyGroundEffectivePostions[TimeUtils.elapsedFrames() % POSITION_EFFECTIVE_FRAME_SIZE] = nearGroundEnemyPosition;
		enemyAirEffectivePostions[TimeUtils.elapsedFrames() % POSITION_EFFECTIVE_FRAME_SIZE] = nearAirEnemyPosition;

		StrategyIdea.totalEnemyCneterPosition = totalEnemyCneterPosition;
		StrategyIdea.nearGroundEnemyPosition = nearGroundEnemyPosition;
		StrategyIdea.nearAirEnemyPosition = nearAirEnemyPosition;
		
		
		// 적 상태
		EnemyUnitStatus enemyStatus;
		
		Region myRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		if (InfoUtils.euiListInMyRegion(myRegion).isEmpty()) {
			if (StrategyIdea.nearGroundEnemyPosition != Position.Unknown
					|| StrategyIdea.nearAirEnemyPosition != Position.Unknown) {
				enemyStatus = EnemyUnitStatus.COMMING;	
			} else {
				enemyStatus = EnemyUnitStatus.SLEEPING;
			}
		} else {
			enemyStatus = EnemyUnitStatus.IN_MY_REGION;
		}
		StrategyIdea.enemyUnitStatus = enemyStatus;
//		System.out.println(enemyStatus);
	}
	
	private void updateWatcherPosition() {
		Position watcherPosition = null;
		
		// watcher 특수 포지션
		if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DEFENSE_DROP)) {
			watcherPosition = InfoUtils.myBase().getPosition(); // 드롭인 경우 본진
			
		} else if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DEFENSE_FRONT)) {
			
			// 앞라인 방어인 경우 second choke (단, 딕텍팅을 대비하는 경우라면, 시간에 맞추어서 secondChoke로 변경)
			if (!StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DETECT_IMPORTANT) || TimeUtils.after(StrategyIdea.turretBuildStartFrame)) {
				watcherPosition = InfoUtils.mySecondChoke().getCenter();
			}
		}
		
		// watcher 기본 포지션
		if (watcherPosition == null) {
			if (PositionUtils.isValidGroundPosition(StrategyIdea.nearGroundEnemyPosition)) {
				watcherPosition = StrategyIdea.nearGroundEnemyPosition;
			} else if (InfoUtils.enemyBase() != null) {
				watcherPosition = InfoUtils.enemyBase().getPosition();
				// if (otherWatcherPosition != null) {watcherPosition = otherWatcherPosition;} else {}
			} else {
				watcherPosition = InfoUtils.mySecondChoke().getCenter();
			}
		}
		
		StrategyIdea.watcherPosition = watcherPosition;
	}

	/// 첫번째 확장기지를 차지하였는지 여부
	private boolean firstExpansionOccupied() {
		BaseLocation firstExpansion = InfoUtils.myFirstExpansion();
		for (BaseLocation occupiedBase : InfoUtils.myOccupiedBases()) {
			if (occupiedBase == firstExpansion) {
				return true;
			}
		}
		
		List<Unit> defenseTowerList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Bunker, UnitType.Terran_Missile_Turret);
		for (Unit bunkerOrTurret : defenseTowerList) {
			RegionType towerRegionType = PositionUtils.positionToRegionType(bunkerOrTurret.getPosition());
			if (towerRegionType == RegionType.MY_FIRST_EXPANSION || towerRegionType == RegionType.MY_THIRD_REGION) {
				return true;
			}
		}
		
		return false;
	}

	/// 커맨드센터와 미네랄 사이의 방어지역
	public Position commandCenterInsidePosition() {
		if (this.commandCenterInsidePosition != null) {
			return commandCenterInsidePosition;
		}
		
		int x = 0;
		int y = 0;
		int mineralCnt = 0;
		
		Position basePosition = InfoUtils.myBase().getPosition();
		for (Unit mineral : Prebot.Broodwar.neutral().getUnits()){
			if ((mineral.getType() == UnitType.Resource_Mineral_Field) && mineral.getDistance(basePosition) < 320) {
				x += mineral.getPosition().getX();
				y += mineral.getPosition().getY();
				mineralCnt++;
			}
		}
		if (mineralCnt == 0) {
			return basePosition;
		}
		int finalx = x / mineralCnt;
		int finaly = y / mineralCnt;
		finalx = (finalx + basePosition.getX()) / 2;
		finaly = (finaly + basePosition.getY()) / 2;
		
		return commandCenterInsidePosition = new Position(finalx, finaly);
	}

	public Position basefirstChokeMiddlePosition() {
		if (this.basefirstChokeMiddlePosition != null) {
			return basefirstChokeMiddlePosition;
		}
		Position firstChokePosition = InfoUtils.myFirstChoke().getCenter();
		Position myBasePosition = InfoUtils.myBase().getPosition();
		double radian = MicroUtils.targetDirectionRadian(firstChokePosition, myBasePosition);

		return basefirstChokeMiddlePosition = MicroUtils.getMovePosition(firstChokePosition, radian, 500);
	}

	/// second choke보다 안쪽 포지션
	private Position firstExpansionBackwardPosition() {
		Chokepoint firstChoke = InfoUtils.myFirstChoke();
		Chokepoint secondChoke = InfoUtils.mySecondChoke();
		BaseLocation firstExpansion = InfoUtils.myFirstExpansion();
		
		int x = (firstChoke.getX() + secondChoke.getX() + firstExpansion.getPosition().getX()) / 3;
		int y = (firstChoke.getY() + secondChoke.getY() + firstExpansion.getPosition().getY()) / 3;
		return new Position(x, y);

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
	
	/// First Choke Point 방어지역
	private Position firstChokeDefensePosition() {
		Position firstChokePosition = InfoUtils.myFirstChoke().getCenter();
		Position myBasePosition = InfoUtils.myBase().getPosition();
		double radian = MicroUtils.targetDirectionRadian(firstChokePosition, myBasePosition);

		Position firstChokeDefensePosition = MicroUtils.getMovePosition(firstChokePosition, radian, 220);
		if (PositionUtils.isValidPosition(firstChokeDefensePosition)) {
			return firstChokeDefensePosition;
		} else {
			return firstChokePosition;
		}
	}

	private boolean enemyBaseDestroyed(BaseLocation enemyBase) {
		if (!Prebot.Broodwar.isVisible(enemyBase.getTilePosition())) {
			return false;
		}

		List<Unit> enemyUnitList = UnitUtils.getUnitsInRadius(PlayerRange.ENEMY, enemyBase.getPosition(), 300,
				UnitType.Protoss_Nexus, UnitType.Terran_Command_Center, UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive);
		return enemyUnitList.isEmpty();
	}

	private Position letsfindRatPosition() {
		// 적 건물
		for (UnitInfo eui : InfoUtils.enemyUnitInfoMap().values()) {
			if (eui.getType().isBuilding() && PositionUtils.isValidPosition(eui.getLastPosition())) {
				return eui.getLastPosition();
			}
		}

		// 적 유닛
		for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
			if (unit.getType() == UnitType.Zerg_Larva || !unit.isVisible()) {
				continue;
			}
			return unit.getPosition();
		}

		// starting location 중에서 탐험되지 않은 지역
		List<BaseLocation> startingBases = InfoUtils.mapInformation().getStartingBaseLocation();
		for (BaseLocation startingBase : startingBases) {
			if (!Prebot.Broodwar.isExplored(startingBase.getTilePosition())) {
				return startingBase.getPosition();
			}
		}

		// 앞마당 지역
		BaseLocation enemyExpansion = InfoUtils.enemyFirstExpansion();
		if (enemyExpansion != null && !Prebot.Broodwar.isExplored(enemyExpansion.getTilePosition())) {
			return enemyExpansion.getPosition();
		}

		// 제 3멀티 중에서 탐험되지 않은 지역
		List<BaseLocation> otherExpansions = InfoUtils.enemyOtherExpansionsSorted();
		if (otherExpansions != null) {
			for (BaseLocation otherExpansion : otherExpansions) {
				if (!Prebot.Broodwar.isExplored(otherExpansion.getTilePosition())) {
					return otherExpansion.getPosition();
				}
			}
		}

		return new Position(2222, 2222);
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
