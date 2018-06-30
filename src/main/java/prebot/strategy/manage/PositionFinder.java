package prebot.strategy.manage;

import java.util.List;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.CombatManager;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.squad.Squad;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;

/**
 * 각종 Position을 찾는다.
 * 
 * campPosition : 수비 지점 attackPosition : 공격 지점
 * 
 */
public class PositionFinder {

	private static PositionFinder instance = new PositionFinder();

	public static PositionFinder Instance() {
		return instance;
	}

	public void update() {
		StrategyIdea.campPosition = getCampPosition();
		StrategyIdea.mainPosition = getMainPosition();

		StrategyIdea.mainSquadCenter = getMainSquadCenter();
		StrategyIdea.watcherPosition = getWatcherPosition(); // TODO 변경필요

		// 분리 필요
		BaseLocation myBase = InfoUtils.myBase();
		Region myRegion = BWTA.getRegion(myBase.getPosition());
		List<UnitInfo> euiList = InfoUtils.euiListInMyRegion(myRegion);
		if (euiList.isEmpty()) {
			StrategyIdea.enemyUnitStatus = EnemyUnitStatus.SLEEPING;
		} else {
			StrategyIdea.enemyUnitStatus = EnemyUnitStatus.IN_MY_REGION;
		}
	}

	private Position getMainSquadCenter() {
		Squad squad = CombatManager.Instance().squadData.getSquad(SquadInfo.MAIN_ATTACK.squadName);
		Unit leader = UnitUtils.leaderOfUnit(squad.unitList);
		if (UnitUtils.isValidUnit(leader)) {
			return leader.getPosition();
		} else {
			return getMainPosition();
		}
	}

	private Position getWatcherPosition() {
		if (InfoUtils.enemyBase() != null) {
			return InfoUtils.enemyBase().getPosition();
		} else {
			return InfoUtils.mySecondChoke().getCenter();
		}
	}

	/// 주둔지
	private Position getCampPosition() {
		int factoryUnitCount = InfoUtils.myNumUnits(UnitType.Terran_Siege_Tank_Tank_Mode,
				UnitType.Terran_Siege_Tank_Siege_Mode , UnitType.Terran_Vulture, UnitType.Terran_Goliath);
		
		int enemyUnitCount = 0;
		if (InfoUtils.enemyRace() == Race.Protoss) {
			enemyUnitCount = (int) (InfoUtils.enemyNumUnits(UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon, UnitType.Protoss_Dark_Templar, UnitType.Protoss_Archon) * 1.5);
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			enemyUnitCount = InfoUtils.enemyNumUnits(UnitType.Zerg_Zergling, UnitType.Zerg_Hydralisk, UnitType.Zerg_Mutalisk) / 2;
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			enemyUnitCount = InfoUtils.enemyNumUnits(UnitType.Terran_Marine); // MECHANIC NO COUNT
		}
		
		boolean detectingOk = true;
		
		// 앞마당에 터렛이 있거나, 스캔이 한번의 스캔이 있어야 한다.
		boolean invisibleEnemyExist = UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Dark_Templar, UnitType.Protoss_Templar_Archives, UnitType.Protoss_Citadel_of_Adun, UnitType.Zerg_Lurker);
		if (invisibleEnemyExist) {
			detectingOk = false;
			List<Unit> turretList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Missile_Turret);
			for (Unit turret : turretList) {
				RegionType regionType = PositionUtils.positionToRegionType(turret.getPosition());
				if (regionType == RegionType.MY_FIRST_EXPANSION) {
					detectingOk = true;
					break;
				}
			}
			
			if (!detectingOk) {
				List<Unit> scannerList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Spell_Scanner_Sweep);
				for (Unit scanner : scannerList) {
					if (scanner.getEnergy() >= 50) {
						detectingOk = true;
						break;
					}
				}
			}
		}
		
		if (detectingOk) {
			int secondChokeBonus = 5;
			int firstExpansionBonus = 2;
			if (StrategyIdea.currentStrategy.defaultTimeMap.isDouble() || StrategyIdea.currentStrategy.defaultTimeMap.isMechanic()) {
				secondChokeBonus = 0;
				firstExpansionBonus = 0;
			}
			
			// 병력이 쌓였다면 second choke에서 방어한다.
			if (factoryUnitCount >= enemyUnitCount + secondChokeBonus) {
				return InfoUtils.mySecondChoke().getCenter();
			}
			// 병력이 조금 있거나 앞마당이 차지되었다면 expansion에서 방어한다.
			if (factoryUnitCount >= enemyUnitCount + firstExpansionBonus || firstExpansionOccupied()) {
				return firstExpansionBackwardPosition();
			}
		}

		if (entranceBlocked()) {
			return entranceBlockedPosition();
		} else {
			// 마린이 일정이상 쌓였거나
			int marineCount = InfoUtils.myNumUnits(UnitType.Terran_Marine) / 2;
			if (factoryUnitCount + marineCount > Math.max(enemyUnitCount, 3)) {
				return firstChokeDefensePosition();
			} else {
				/// 커맨드센터 수비 필요
				return commandCenterInsidePosition();
			}
		}
	}

	private boolean entranceBlocked() {
		return InfoUtils.enemyRace() == Race.Protoss;
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

	private Position getEnemyCampPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	/// 첫번째 확장기지를 차지하였는지 여부
	private boolean firstExpansionOccupied() {
		BaseLocation firstExpansion = InfoUtils.myFirstExpansion();
		for (BaseLocation occupiedBase : InfoUtils.myOccupiedBases()) {
			if (occupiedBase == firstExpansion) {
				return true;
			}
		}
		return false;
	}

	private Position entranceBlockedPosition() {
		return firstChokeDefensePosition();
	}

	/// 커맨드센터와 미네랄 사이의 방어지역
	private Position commandCenterInsidePosition() {
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
		
		return new Position(finalx, finaly);
	}

	/// First Choke Point 방어지역
	private Position firstChokeDefensePosition() {
		Pair<Position, Position> pairPosition = InfoUtils.myFirstChoke().getSides();
		Position p1 = new Position(pairPosition.first.getX(), pairPosition.second.getY());
		Position p2 = new Position(pairPosition.second.getX(), pairPosition.first.getY());

		BaseLocation myBase = InfoUtils.myBase();
		double p1FromMyBase = p1.getApproxDistance(myBase.getPosition());
		double p2FromMyBase = p2.getApproxDistance(myBase.getPosition());
		return p1FromMyBase < p2FromMyBase ? p1 : p2;
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
