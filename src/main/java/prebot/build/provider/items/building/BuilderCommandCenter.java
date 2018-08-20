package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderItem.SeedPositionStrategy;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.MyBotModule;
import prebot.common.util.InfoUtils;
import prebot.common.util.PlayerUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.micro.constant.MicroConfig;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions;
import prebot.strategy.manage.PositionFinder;

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
//					System.out.println("fast 2nd commandcenter - for double (vs protoss)");
					return true;
				}
				
			} else if (InfoUtils.enemyRace() == Race.Terran) {
				List<Unit> bunkers = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Bunker);
				List<Unit> tankWraiths = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL, UnitType.Terran_Wraith, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
				if (!bunkers.isEmpty() && tankWraiths.size() >= 4) {
					setCommandCenterBlockAndSeedPosition();
//					System.out.println("fast 2nd commandcenter - bunker defense (vs terran)");
					return true;
				}
			}
			
			if (MyBotModule.Broodwar.self().minerals() > 400) {
				if (InfoUtils.enemyRace() == Race.Protoss && !StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.DOUBLE)) {
					if (UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Siege_Tank_Tank_Mode)) {
						setCommandCenterBlockAndSeedPosition();
//						System.out.println("normal 2nd commandcenter - over 400 minerals");
						return true;
					}
				} else if (UnitUtils.myUnitDiscovered(UnitType.Terran_Starport)) {
					if (UnitUtils.myUnitDiscovered(UnitType.Terran_Wraith, UnitType.Terran_Valkyrie) || MyBotModule.Broodwar.self().minerals() > 600) {
						setCommandCenterBlockAndSeedPosition();
//						System.out.println("normal 2nd commandcenter - over 400 minerals");
						return true;
					}
				} else {
					setCommandCenterBlockAndSeedPosition();
//					System.out.println("normal 2nd commandcenter - over 400 minerals");
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
//				System.out.println("normal next commandcenter - over 30 mechanics & over 600 minerals");
				return true;

			}
			// 공격시 돈 250 넘으면 멀티하기
			if (isAttackMode && StrategyIdea.mainSquadMode != MicroConfig.MainSquadMode.SPEED_ATTCK && MyBotModule.Broodwar.self().minerals() > 250) {
				setCommandCenterBlockAndSeedPosition();
//				System.out.println("attack and next commandcenter - attack & over 250 minerals");
				return true;
			}
			
			// READY TO POSITION 까지 나왔는데 아직 커맨드가 2개이면 250 넘었을 때 멀티
			if (StrategyIdea.campType == PositionFinder.CampType.READY_TO && allCommandCenterCount == 2 && MyBotModule.Broodwar.self().minerals() > 250) {
				setCommandCenterBlockAndSeedPosition();
//				System.out.println("ready to commandcenter - only 2 center & over 250 minerals");
				return true;
			}
			
			// 800 넘으면 멀티하기
			if (MyBotModule.Broodwar.self().minerals() > 800) {
				setCommandCenterBlockAndSeedPosition();
//				System.out.println("minerals next commandcenter - over 800 minerals");
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
//			System.out.println("minerals lefted only " + leftMinerals);
			if (leftMinerals < 8000 && isAttackMode) {
				setCommandCenterBlockAndSeedPosition();
//				System.out.println("next commandcenter - minerals lefted only " + leftMinerals);
				return true;
			}
			if (leftMinerals < 4000) {
				setCommandCenterBlockAndSeedPosition();
//				System.out.println("next commandcenter - minerals lefted only " + leftMinerals);
				return true;
			}
			if (factoryUnitCount > 40 * 4) {
//				System.out.println("next commandcenter - over 40 mechanics");
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
		}
		
		return false;
	}

	

	private void setCommandCenterBlockAndSeedPosition() {
		BuildOrderItem.SeedPositionStrategy seedPosition = null;

		int allCommandCenterCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
    	if (allCommandCenterCount <= 1) {
    		if (StrategyIdea.campType == PositionFinder.CampType.INSIDE || StrategyIdea.campType == PositionFinder.CampType.FIRST_CHOKE) {
    			seedPosition = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
    		} else {
    			seedPosition = BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation;
    		}
    	} else if (allCommandCenterCount >= 2) {
    		seedPosition = BuildOrderItem.SeedPositionStrategy.NextExpansionPoint;
    	}
    	
    	setSeedPositionStrategy(seedPosition);
    	setBlocking(true);
	}
}
