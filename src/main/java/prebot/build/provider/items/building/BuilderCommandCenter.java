package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem.SeedPositionStrategy;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;
import prebot.strategy.manage.PositionFinder.CampType;

public class BuilderCommandCenter extends DefaultBuildableItem {

    public BuilderCommandCenter(MetaType metaType){
        super(metaType);
    }
    
	public final boolean buildCondition() {
		if (Prebot.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center) > 0) {
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
		
		
		Race enemyRace = InfoUtils.enemyRace();
		if (enemyRace == Race.Zerg) {
//			if (allCommandCenterCount == 1) {
//				if (UnitUtils.myFactoryUnitSupplyCount() + UnitUtils.myWraithUnitSupplyCount() >= 25) {
//					BaseLocation enemyFirstExpansion = InfoUtils.enemyFirstExpansion();
//					List<BaseLocation> enemyOccupiedBases = InfoUtils.enemyOccupiedBases();
//
//					for (BaseLocation enemyOccupiedBase : enemyOccupiedBases) {
//						if (enemyFirstExpansion.equals(enemyOccupiedBase)) {
//							setSeedPositionStrategy(getExpansionSeedPosition());
//							setBlocking(true);
//							return true;
//						}
//					}
//				}
//			}
			
		} else if (enemyRace == Race.Protoss) {
	    	// TODO 이니셜에 들어가야하는 부분인지 확인
			if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DOUBLE)
					&& !StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.QUICK_ATTACK)) {
		    	int allCommandCenterCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
				if (allCommandCenterCount == 1) {
					setCommandCenterBlockAndSeedPosition();
					return true;
				}
			}
		}
	    		
		return executeExpansion();
    }

	public boolean executeExpansion() {
		if (TimeUtils.beforeTime(10, 0)) {
			if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT) {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) < 2) {
					return false;
				}
			}

			if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.ONE_FACTORY) {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) < 1) {
					return false;
				}
			}

			if (StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_FACTORY) {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) < 2) {
					return false;
				}
			}
		}

		int maxCCcount = 4;
		int mineralValidCommandCenterCount = 0;
		
		List<Unit> commandCenters = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		for (Unit commandCenter : commandCenters) {
			if (validMineralCountNearDepot(commandCenter) > 6) {
				mineralValidCommandCenterCount++;
			}
		}
		if (mineralValidCommandCenterCount >= maxCCcount) {
			return false;
		}

		int factoryUnitCount = UnitUtils.myFactoryUnitSupplyCount();
		int realCCcnt = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);

		// 앞마당 전
		if (realCCcnt == 1) {
//			if (포인트에 따라 좀더 빨리 지을 수 있는 케이스) {}
			if (Prebot.Broodwar.self().minerals() > 400) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
		}

		// 앞마당 이후
		else if (realCCcnt >= 2) {
			
			boolean isAttackMode = StrategyIdea.mainSquadMode.isAttackMode;

			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
			if (Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 120) {
				setCommandCenterBlockAndSeedPosition();
				return true;

			}
			// 공격시 돈 250 넘으면 멀티하기
			if (isAttackMode && Prebot.Broodwar.self().minerals() > 250) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			
			// 800 넘으면 멀티하기
			if (Prebot.Broodwar.self().minerals() > 800) {
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

			int temp = 0;
			for (Unit units : commandCenters) {
				temp += WorkerManager.Instance().getWorkerData().getMineralsSumNearDepot(units);
			}
			if (temp < 8000 && isAttackMode) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
			if (factoryUnitCount > 160) {
				setCommandCenterBlockAndSeedPosition();
				return true;
			}
		}
		
		return false;
	}

	private int validMineralCountNearDepot(Unit commandCenter) {
		if (!UnitUtils.isValidUnit(commandCenter)) {
			return 0;
		}

		int mineralsNearDepot = 0;
		for (Unit mineral : Prebot.Broodwar.neutral().getUnits()) {
			if (mineral.getType() != UnitType.Resource_Mineral_Field) {
				continue;
			}
			if (mineral.getDistance(commandCenter) < 450 && mineral.getResources() > 200) {
				mineralsNearDepot++;
			}
		}
		return mineralsNearDepot;
	}

	private void setCommandCenterBlockAndSeedPosition() {
		SeedPositionStrategy seedPosition = null;

		int allCommandCenterCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
    	if (allCommandCenterCount <= 1) {
    		if (StrategyIdea.campType == CampType.INSIDE || StrategyIdea.campType == CampType.FIRST_CHOKE) {
    			seedPosition = SeedPositionStrategy.MainBaseLocation;
    		} else {
    			seedPosition = SeedPositionStrategy.FirstExpansionLocation;
    		}
    	} else if (allCommandCenterCount >= 2) {
    		seedPosition = SeedPositionStrategy.FirstExpansionLocation;
    	}
    	
    	setSeedPositionStrategy(seedPosition);
    	setBlocking(true);
	}
}
