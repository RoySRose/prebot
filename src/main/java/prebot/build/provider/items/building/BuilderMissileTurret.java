package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions;

public class BuilderMissileTurret extends DefaultBuildableItem {

	public BuilderMissileTurret(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) == 0) {
			return false;
		}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) >= 10) {
			return false;
		}
		if (TimeUtils.before(StrategyIdea.turretBuildStartFrame)) {
			return false;
		}

		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret);
		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, null);

		// 첫번째 터렛이 없고, 입막 터렛 위치가 지정되어있을경우
		if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Missile_Turret) && BlockingEntrance.Instance().entrance_turret1 != TilePosition.None
				&& buildQueueCount + constructionCount == 0) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(BlockingEntrance.Instance().entrance_turret1);
			return true;
		}
		
		if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Missile_Turret) == 1) {
			if(BlockingEntrance.Instance().entrance_turret2 != TilePosition.None && buildQueueCount + constructionCount == 0) {
				setHighPriority(true);
				setBlocking(true);
				setTilePosition(BlockingEntrance.Instance().entrance_turret2);
				return true;
			}
		}
		
		int max_turret = 1;
		
		int add_turret = 0;
		
		if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_DARK_DROP
			|| StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK
			 || StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_ROBOTICS_REAVER) {
			add_turret = 1;
		}
		
//		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Dark_Templar)||UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Shuttle)) {
//			max_turret = 2;
//		}
//		
		
		
		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Zerg_Mutalisk)) {
			max_turret = 3;
		}
		
		

		
		BaseLocation myBase = InfoUtils.myBase();
		BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
		Chokepoint myFirstChoke = InfoUtils.myFirstChoke();
		Chokepoint mySecondChoke = InfoUtils.mySecondChoke();

		int turretCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);

		// 미사일 터렛이 많을수록 더 넓은 지역을 커버하니 지을 수가 없게 되는것이 아닌지??
		// 베이스는 숫자를 (350 != 300) 일부러 다르게 한것인가? 터렛범위에 빌드/컨스트럭션 큐 범위
		if (noTurretNearPosition(myBase.getPosition(), 350, 300, turretCount, max_turret+add_turret)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(myBase.getPosition().toTilePosition());
			return true;
		}

		Position firstChokeMainHalf = new Position((myBase.getPosition().getX() + myFirstChoke.getX() * 2) / 3 - 60,
				(myBase.getPosition().getY() + myFirstChoke.getY() * 2) / 3 - 60);
		if (noTurretNearPosition(firstChokeMainHalf, 180, 180, turretCount, max_turret)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(firstChokeMainHalf.toTilePosition());
			return true;
		}
		
		Position firstChokeExpHalf = new Position((myFirstExpansion.getPosition().getX() * 2 + myFirstChoke.getX()) / 3,
				(myFirstExpansion.getPosition().getY() * 2 + myFirstChoke.getY()) / 3);
		if (noTurretNearPosition(firstChokeMainHalf, 210, 150, turretCount, max_turret)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(firstChokeExpHalf.toTilePosition());
			return true;
		}

		// TODO COMPLETE, ALL 테스트에 따른 변경여부 결정
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) > 1) {
			if (noTurretNearPosition(mySecondChoke.getCenter(), 100, 100, turretCount, max_turret)) {
				setHighPriority(true);
				setBlocking(true);
				setTilePosition(mySecondChoke.getCenter().toTilePosition());
				return true;
			}
		}
		
		
		
		
//		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0 && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
//			int build_turret_cnt = 0;
//			int turretcnt =  Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
//			//지역 멀티
//			
//			if (myBase != null) {
//				
//				List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(myBase.getPosition(), 600+turretcnt*10);
//				build_turret_cnt = 0;
//				for(Unit unit: turretInRegion){
//					if (unit.getType() == UnitType.Terran_Missile_Turret) {
//						build_turret_cnt++;
//					}
//				}
//
//				if (build_turret_cnt < max_turret_to_mutal) {
//					if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, myBase.getPosition().toTilePosition(), 300) < 1
//							&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	myBase.getPosition().toTilePosition(), 300) == 0) {
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, myBase.getPosition().toTilePosition(),true);
//					}
//				}
//			}
//			if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) > 1){
//				if (myFirstExpansion != null) {
//					
//					List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(myFirstExpansion.getPosition(), 600+turretcnt*10);
//					build_turret_cnt = 0;
//					for(Unit unit: turretInRegion){
//						if (unit.getType() == UnitType.Terran_Missile_Turret) {
//							build_turret_cnt++;
//						}
//					}
//
//					if (build_turret_cnt < max_turret_to_mutal) {
//						if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, myFirstExpansion.getPosition().toTilePosition(), 300) < 1
//								&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	myFirstExpansion.getPosition().toTilePosition(), 300) == 0) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, myFirstExpansion.getPosition().toTilePosition(),true);
//						}
//					}
//				}
//			}
//		}
		
		return false;
	}

	private boolean noTurretNearPosition(Position centerPosition, int radius1, int radius2, int turretCount, int maxTurretCnt) {
		List<Unit> turretNearBase = UnitUtils.getUnitsInRadius(PlayerRange.ALL, centerPosition, radius1 + turretCount * 15, UnitType.Terran_Missile_Turret);
		if( maxTurretCnt <= turretNearBase.size()) {
			return false;
		}
//		if (!turretNearBase.isEmpty()) {
//			return false;
//		}

		int buildQueueCountNear = BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius2);
		int constructionCountNear = ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius2);
		return buildQueueCountNear + constructionCountNear == 0;
	}
	
	
}
