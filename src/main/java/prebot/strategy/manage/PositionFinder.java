package prebot.strategy.manage;

import java.util.List;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

/**
 * 각종 Position을 찾는다.
 * 
 * campPosition : 수비 지점
 * attackPosition : 공격 지점
 * 
 */
public class PositionFinder {
	
	private static PositionFinder instance = new PositionFinder();

	public static PositionFinder Instance() {
		return instance;
	}

	public void update() {
		StrategyIdea.campPosition = getCampPosition();
		StrategyIdea.attackPosition = getAttackPosition();
		
		if (!StrategyIdea.mainSquadMode.isAttackMode) {
			StrategyIdea.mainSquadPosition = StrategyIdea.campPosition;
		} else {
			StrategyIdea.mainSquadPosition = StrategyIdea.attackPosition;
		}
	}

	/// 주둔지
	private Position getCampPosition() {
		if (!firstExpansionOccupied()) {
			if (defenseInside()) {
				 return commandCenterInsidePosition();
			} else {
				return firstChokeDefensePosition();
			}
		} else {
			if (defenseSecondChoke()) {
				 return InfoUtils.mySecondChoke().getCenter();
			} else {
				return firstExpansionBackwardPosition();
			}
		}
	}

	/// 공격지점
	private Position getAttackPosition() {
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
		}
		
		return letsfindRatPosition();
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

	/// 커맨드센터 수비 필요
	private boolean defenseInside() {
		// TODO 초반 저글링 공격 등 타이밍상 first choke에서 수비가 불가한 경우 true
		// 수비가 가능해지는 시점에 false로 변경
		return false;
	}

	/// 커맨드센터와 미네랄 사이의 방어지역
	private Position commandCenterInsidePosition() {
		return null;
	}
	
	/// 전진수비 여부
	private boolean defenseSecondChoke() {
		// TODO 1. 병력이 일정이상 쌓였을 때
		// 2. 상대가 테란이면 좀더 빨리 포지션을 차지해야 한다.
		return false;
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

	/// First Expansion에서 약간 물러난 위치
	private Position firstExpansionBackwardPosition() {
		Chokepoint firstChoke = InfoUtils.myFirstChoke();
		Chokepoint secondChoke = InfoUtils.mySecondChoke();
		BaseLocation firstExpansion = InfoUtils.myFirstExpansion();

		double distanceFromFirstChoke = firstChoke.getDistance(secondChoke);
		double distanceFromExpansion = firstExpansion.getDistance(secondChoke);
		if (distanceFromFirstChoke < distanceFromExpansion) {
			return firstChoke.getCenter();
		} else {
			int x = (firstChoke.getX() + firstExpansion.getX()) / 2;
			int y = (firstChoke.getY() + firstExpansion.getY()) / 2;
			return new Position(x, y);
		}
	}

	private boolean enemyBaseDestroyed(BaseLocation enemyBase) {
		if (!Prebot.Broodwar.isVisible(enemyBase.getTilePosition())) {
			return false;
		}

		List<Unit> enemyUnitList = UnitUtils.getUnitsInRadius(PlayerRange.ENEMY, enemyBase.getPosition(), 300, UnitType.Protoss_Nexus, UnitType.Terran_Command_Center,
				UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive);
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
