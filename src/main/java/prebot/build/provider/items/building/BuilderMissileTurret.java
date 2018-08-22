package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.MyBotModule;
import prebot.common.util.FileUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation;
import prebot.strategy.StrategyIdea;
import prebot.strategy.manage.PositionFinder;

public class BuilderMissileTurret extends DefaultBuildableItem {

	public BuilderMissileTurret(MetaType metaType) {
		super(metaType);
	}
	public int add_turret = 0;

	public final boolean buildCondition() {
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) == 0) {
			return false;
		}
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) >= 10) {
			return false;
		}
		if (TimeUtils.before(StrategyIdea.turretBuildStartFrame)) {
			return false;
		}

//		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret);
//		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, null);

		if (!StrategyIdea.EXOK) {
			
//			List<Unit> commandCenters = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Command_Center);
			// 첫번째 터렛이 없고, 입막 터렛 위치가 지정되어있을경우
//			noTurretNearPosition
			if (TilePositionUtils.isValidTilePosition(BlockingEntrance.Instance().entrance_turret1)) {
				if(noTurretNearPosition(BlockingEntrance.Instance().entrance_turret1.toPosition(), 80, 30, 0, 1, 10)) {
					setHighPriority(true);
					setBlocking(true);
					setTilePosition(BlockingEntrance.Instance().entrance_turret1);
					FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct entrance_turret1 :: " + BlockingEntrance.Instance().entrance_turret1);
					return true;
				}
			}
			
			if (TilePositionUtils.isValidTilePosition(BlockingEntrance.Instance().entrance_turret2)) {
				if(noTurretNearPosition(BlockingEntrance.Instance().entrance_turret2.toPosition(), 80, 30, 0, 1, 10)) {
					setHighPriority(true);
					setBlocking(true);
					setTilePosition(BlockingEntrance.Instance().entrance_turret2);
					FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct entrance_turret2 :: " + BlockingEntrance.Instance().entrance_turret2);
					return true;
				}
			}
//			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Missile_Turret) && BlockingEntrance.Instance().entrance_turret1 != TilePosition.None
//					&& buildQueueCount + constructionCount == 0) {
//				setHighPriority(true);
//				setBlocking(true);
//				setTilePosition(BlockingEntrance.Instance().entrance_turret1);
//				return true;
//			}
			
//			if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Missile_Turret) == 1) {
//				if(BlockingEntrance.Instance().entrance_turret2 != TilePosition.None && buildQueueCount + constructionCount == 0) {
//					setHighPriority(true);
//					setBlocking(true);
//					setTilePosition(BlockingEntrance.Instance().entrance_turret2);
//					return true;
//				}
//			}
		}
		
//		if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_DARK_DROP
//			|| StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK
//			 || StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_ROBOTICS_REAVER) {
//			add_turret = 1;
//		}
		
//		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Dark_Templar)||UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Shuttle)) {
//			max_turret = 2;
//		}
//		
		int baseTurret = 1;
		
		int otherCommandTurret = 1;
		
		if(MyBotModule.Broodwar.enemy().getRace()== Race.Protoss) {
			baseTurret = 2;
//			otherCommandTurret = 1;
		}
		
		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Zerg_Mutalisk)
			||UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Spire)
			) {
//			max_turret = 2;
			add_turret = 1;
		}
		
		

		
		BaseLocation myBase = InfoUtils.myBase();
		BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
		Chokepoint myFirstChoke = InfoUtils.myFirstChoke();
		Chokepoint mySecondChoke = InfoUtils.mySecondChoke();

//		int turretCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
		int turretCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Missile_Turret);

		// 미사일 터렛이 많을수록 더 넓은 지역을 커버하니 지을 수가 없게 되는것이 아닌지??
		// 베이스는 숫자를 (350 != 300) 일부러 다르게 한것인가? 터렛범위에 빌드/컨스트럭션 큐 범위
//		System.out.println(" turret postion check start");
		if (noTurretNearPosition(myBase.getPosition(), 250, 200, turretCount, baseTurret+add_turret, 15)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(myBase.getPosition().toTilePosition());
//			FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct myBase :: " + myBase.getPosition().toTilePosition());
			return true;
		}
		
		Position mutalDef = PositionFinder.Instance().expansionDefensePositionSiege();
		
//		System.out.println("Const Turret to mutalDef :: " + mutalDef.toTilePosition());
		
		if (noTurretNearPosition(mutalDef, 150, 120, turretCount, 1+add_turret, 15)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(mutalDef.toTilePosition());
//			System.out.println("need turret to  mutalDef :: " + mutalDef.toTilePosition());
			FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct mutalDef :: " + mutalDef.toTilePosition());
			return true;
		}

		Position firstChokeMainHalf = new Position((myBase.getPosition().getX() + myFirstChoke.getX() * 2) / 3 - 60,
				(myBase.getPosition().getY() + myFirstChoke.getY() * 2) / 3 - 60);
//		if (noTurretNearPosition(firstChokeMainHalf, 150, 100, turretCount, max_turret)) {
//			setHighPriority(true);
//			setBlocking(true);
//			setTilePosition(firstChokeMainHalf.toTilePosition());
//			return true;
//		}
		if (StrategyIdea.EXOK) {
			Position betweenChoke = Position.None;
			
			if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.FIGHTING_SPIRITS) {
				betweenChoke = new Position((firstChokeMainHalf.getX() * 4 + mySecondChoke.getX() * 7) / 11,
				(firstChokeMainHalf.getY() * 4 + mySecondChoke.getY() * 7) / 11);
			}else {
				betweenChoke = new Position((firstChokeMainHalf.getX() * 3 + mySecondChoke.getX() * 4) / 7,
				(firstChokeMainHalf.getY() * 4 + mySecondChoke.getY() * 7) / 11);
			}
			
	//		Position betweenChoke = new Position((firstChokeMainHalf.getX() * 3 + mySecondChoke.getX() * 4) / 7,
	//				(firstChokeMainHalf.getY() * 4 + mySecondChoke.getY() * 7) / 11);
			
			if (noTurretNearPosition(betweenChoke, 150, 120, turretCount, 1, 15)) {
				setHighPriority(true);
				setBlocking(true);
				setTilePosition(betweenChoke.toTilePosition());
//				FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct betweenChoke :: " + betweenChoke.toTilePosition());
				return true;
			}
		
//		Position firstChokeExpHalf = new Position((myFirstExpansion.getPosition().getX() * 2 + myFirstChoke.getX()) / 3,
//				(myFirstExpansion.getPosition().getY() * 2 + myFirstChoke.getY()) / 3);
//		
//		if (noTurretNearPosition(firstChokeExpHalf, 150, 100, turretCount, 1+add_turret)) {
//			setHighPriority(true);
//			setBlocking(true);
//			setTilePosition(firstChokeExpHalf.toTilePosition());
//			return true;
//		}
		
		
		
		
			// TODO COMPLETE, ALL 테스트에 따른 변경여부 결정
//			if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) > 1) {
			if (noTurretNearPosition(mySecondChoke.getCenter(), 120, 100, turretCount, 1+add_turret, 15)) {
				setHighPriority(true);
				setBlocking(true);
				setTilePosition(mySecondChoke.getCenter().toTilePosition());
//				FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct mySecondChoke :: " + mySecondChoke.getCenter().toTilePosition());
				return true;
			}
			
			if (noTurretNearPosition(myFirstExpansion.getPosition(), 250, 150, turretCount, otherCommandTurret+add_turret, 10)) {
				setHighPriority(true);
				setBlocking(true);
				setTilePosition(myFirstExpansion.getPosition().toTilePosition());
//				FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct myFirstExpansion :: " + myFirstExpansion.getPosition().toTilePosition());
				return true;
			}
//			}
		}
		

		List<Unit> commandCenters = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Command_Center);
		if(commandCenters.size() > 2) {
			for (Unit commandCenter : commandCenters) {
//				20180820. hkk. 커맨드센터가 완성되지 않았는데, 공격받고 있거나, 건설중이 아닌 상태가 되면 주변 미사일터렛을 지으러 가지 않는다.
				if(!commandCenter.isCompleted() && (!commandCenter.isConstructing() || commandCenter.isUnderAttack())){
					continue;
				}
				if(!TilePositionUtils.equals(myBase.getTilePosition(), commandCenter.getTilePosition())
					&& !TilePositionUtils.equals(myFirstExpansion.getTilePosition(), commandCenter.getTilePosition())) {
//					FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: not main base :: " + commandCenter.getTilePosition());
					if (validMineralCountNearDepot(commandCenter) > 6) {
						if (noTurretNearPosition(commandCenter.getPosition(), 150, 150, turretCount, otherCommandTurret+add_turret, 10)) {
							setHighPriority(true);
							setBlocking(true);
							setTilePosition(commandCenter.getPosition().toTilePosition());
//							FileUtils.appendTextToFile("log.txt", "\n BuilderMissileTurret :: construct near command :: " + commandCenter.getTilePosition());
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean noTurretNearPosition(Position centerPosition, int radius1, int radius2, int turretCount, int maxTurretCnt, int mutilple) {
		
		int buildQueueCountNear = BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius1 + turretCount * mutilple);
		int constructionCountNear = ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius1 + turretCount * mutilple);
		List<Unit> turretNearBase = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.SELF, centerPosition, radius1 + turretCount * mutilple, UnitType.Terran_Missile_Turret);
		
		if(maxTurretCnt <= buildQueueCountNear + constructionCountNear + turretNearBase.size()) {
//			System.out.println(" there is turret quere exists :: " + maxTurretCnt + " :: " + (buildQueueCountNear + constructionCountNear));
			return false;
		}
		
		
//		if( maxTurretCnt <= turretNearBase.size()) {
////			System.out.println(" there is real turret exists :: " + maxTurretCnt + " :: " + (buildQueueCountNear + constructionCountNear));
//			return false;
//		}
//		if (!turretNearBase.isEmpty()) {
//			return false;
//		}
		
//		Race enemyRace = Prebot.Broodwar.enemy().getRace();
		
		int radiusP = 0;
		
		if(MyBotModule.Broodwar.enemy().getRace() == Race.Zerg) {
			radiusP = UnitType.Zerg_Lurker.groundWeapon().maxRange();
		}else {
			radiusP = 80;
		}
		
		List<Unit> nearInvisibleUnit = UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, centerPosition, radiusP);
		for(Unit unit : nearInvisibleUnit) {
			if(unit.getType() == UnitType.Protoss_Dark_Templar || unit.getType() == UnitType.Zerg_Lurker) {
				if(UnitUtils.availableScanningCount() == 0) {
					return false;
				}
				
			}
		}
//		System.out.println("Const Turret to centerPosition :: " + centerPosition.toTilePosition());

//		int buildQueueCountNear = BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius2);
//		int constructionCountNear = ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius2);
//		int constructionCountNear = 0;
		return true;
	}
	
	
}
