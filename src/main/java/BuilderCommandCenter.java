

import java.util.List;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class BuilderCommandCenter extends DefaultBuildableItem {

    public BuilderCommandCenter(MetaType metaType){
        super(metaType);
    }
    
	public final boolean buildCondition() {
		if (MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center) > 0) {
			return false;
		}
		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center);
    	if (buildQueueCount > 0) {
    		return false;
    	}
    	
    	int constructionQueueCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null);
    	if (constructionQueueCount > 0) {
			return false;
		}
    	if (InfoUtils.enemyBase() == null) {
			return false;
		}
		
		return executeExpansion();
    }

	public boolean executeExpansion() {
		if (TimeUtils.beforeTime(8, 0) && !PlayerUtils.enoughResource(800, 0)) {
			if (StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.TWO_STARPORT) {
				if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Starport) < 2) {
					return false;
				}
			} else if (StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.ONE_STARPORT) {
				if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Starport) < 1) {
					return false;
				}
			} else if (StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.ONE_FACTORY) {
				if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory) < 1) {
					return false;
				}
			} else if (StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.TWO_FACTORY) {
				if (MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory) < 2) {
					return false;
				}
			}
		}

		int maxCCcount = 4;
		int mineralValidCommandCenterCount = 0;
		
		List<Unit> commandCenters = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		for (Unit commandCenter : commandCenters) {
			if (validMineralCountNearDepot(commandCenter) > 6) {
				mineralValidCommandCenterCount++;
			}
		}
		
		if (mineralValidCommandCenterCount >= maxCCcount) {
			return false;
		}
		
		// 총 최대 커맨드 개수 7개
		if (UnitUtils.hasUnitOrWillBeCount(UnitType.Terran_Command_Center) > 7) {
			return false;
		}

		int factoryUnitCount = UnitUtils.myFactoryUnitSupplyCount();
		int allCommandCenterCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);

		// 앞마당 전
		if (allCommandCenterCount == 1) {
//			if (포인트에 따라 좀더 빨리 지을 수 있는 케이스) {}
			if (InfoUtils.enemyRace() == Race.Protoss) {
		    	// TODO 이니셜에 들어가야하는 부분인지 확인
				if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DOUBLE)
						&& !StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.QUICK_ATTACK)) {
					setCommandCenterBlockAndSeedPosition();
					return true;
				}
				
			} else if (InfoUtils.enemyRace() == Race.Terran) {
				List<Unit> bunkers = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Bunker);
				List<Unit> tankWraiths = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL, UnitType.Terran_Wraith, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
				if (!bunkers.isEmpty() && tankWraiths.size() >= 4) {
					setCommandCenterBlockAndSeedPosition();
					return true;
				}
			}
			
			if (MyBotModule.Broodwar.self().minerals() > 400) {
				if (InfoUtils.enemyRace() == Race.Protoss && !StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DOUBLE)) {
					if (UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Siege_Tank_Tank_Mode)) {
						setCommandCenterBlockAndSeedPosition();
						return true;
					}
				} else if (UnitUtils.myUnitDiscovered(UnitType.Terran_Starport)) {
					if (UnitUtils.myUnitDiscovered(UnitType.Terran_Wraith, UnitType.Terran_Valkyrie) || MyBotModule.Broodwar.self().minerals() > 600) {
						setCommandCenterBlockAndSeedPosition();
						return true;
					}
				} else {
					setCommandCenterBlockAndSeedPosition();
					return true;
				}
			}
		}

		// 앞마당 이후
		else if (allCommandCenterCount >= 2) {
			
			boolean isAttackMode = StrategyIdea.mainSquadMode.isAttackMode;

			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
			if (MyBotModule.Broodwar.self().minerals() > 600 && factoryUnitCount > 30 * 4) {
				setCommandCenterBlockAndSeedPosition();
				return true;

			}
			// 공격시 돈 250 넘으면 멀티하기
			if (isAttackMode && StrategyIdea.mainSquadMode != MicroConfig.MainSquadMode.SPEED_ATTCK && MyBotModule.Broodwar.self().minerals() > 250) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			
			// READY TO POSITION 까지 나왔는데 아직 커맨드가 2개이면 250 넘었을 때 멀티
			if (StrategyIdea.campType == PositionFinder.CampType.READY_TO && allCommandCenterCount == 2 && MyBotModule.Broodwar.self().minerals() > 250) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			
			// 800 넘으면 멀티하기
			if (MyBotModule.Broodwar.self().minerals() > 800) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			
			// 500 넘고 유리하면
//			if (Prebot.Broodwar.self().minerals() > 500 && factoryUnitCount > 50 && AttackExpansionManager.Instance().Attackpoint > 30) {
//				setBlocking(true);
//				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				return true;
//			}
//			// 200 넘고 유리하면
//			if (Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 80 
//					&& AttackExpansionManager.Instance().Attackpoint > 40 && AttackExpansionManager.Instance().ExpansionPoint >= 0) {
//				setBlocking(true);
//				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//				return true;
//			}
			// 공격시 돈 250 넘으면 멀티하기

//			if (TimeUtils.afterTime(10, 0)) {
//				if (factoryUnitCount > 80 && Prebot.Broodwar.self().deadUnitCount() - Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture) < 12) {
//					setBlocking(true);
//					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
//					return true;
//				}
//			}

			int leftMinerals = 0;
			for (Unit units : commandCenters) {
				leftMinerals += WorkerManager.Instance().getWorkerData().getMineralsSumNearDepot(units);
			}
			if (leftMinerals < 8000 && isAttackMode) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			if (leftMinerals < 4000) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			if (factoryUnitCount > 40 * 4) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
		}
		
		return false;
	}

	

	private void setCommandCenterBlockAndSeedPosition() {
		BuildOrderItem.SeedPositionStrategy seedPosition = BuildOrderItem.SeedPositionStrategy.NoLocation;
		TilePosition seedTilePosition = TilePosition.None;

		int allCommandCenterCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
    	if (allCommandCenterCount <= 1) {
    		if (StrategyIdea.campType == PositionFinder.CampType.INSIDE || StrategyIdea.campType == PositionFinder.CampType.FIRST_CHOKE) {
//    			seedPosition = SeedPositionStrategy.MainBaseLocation;
    			if(BlockingEntrance.Instance().entrance_turret1 != TilePosition.None) {
    				seedTilePosition = BlockingEntrance.Instance().entrance_turret1;
    			}else {
    				seedPosition = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
    			}
    		} else {
    			seedPosition = BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation;
    		}
    	} else if (allCommandCenterCount >= 2) {
    		seedPosition = BuildOrderItem.SeedPositionStrategy.NextExpansionPoint;
    	}
    	if(TilePositionUtils.isValidTilePosition(seedTilePosition)){
//    		//FileUtils.appendTextToFile("log.txt", "\n set command center to 1 turret position :: " + seedTilePosition);
    		setTilePosition(seedTilePosition);
    	}else {
//    		//FileUtils.appendTextToFile("log.txt", "\n set command center to :: " + seedPosition);
    		setSeedPositionStrategy(seedPosition);
    	}
    	setBlocking(true);
	}
}
