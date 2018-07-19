package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;
import prebot.strategy.StrategyManager;
import prebot.strategy.TempBuildSourceCode;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;
import prebot.strategy.manage.AttackExpansionManager;

public class BuilderCommandCenter extends DefaultBuildableItem {

    public BuilderCommandCenter(MetaType metaType){
        super(metaType);
    }
    
    private boolean expanchcker = false;

    public final boolean buildCondition(){
        //System.out.println("CommandCenter build condition check");
    	
    	if (Prebot.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center) > 0) {
			return false;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) != 0) {
			return false;
		}
    	
		if(InformationManager.Instance().enemyRace == Race.Zerg) {
			
//			if(expanchcker == false){
				BaseLocation enemyMainbase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
				
				if(enemyMainbase != null && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1 
						&& (UnitUtils.myFactoryUnitSupplyCount() >= 25 || UnitUtils.myWraithUnitSupplyCount() >= 25)){
					
					
					BaseLocation enemyFEbase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
					List<BaseLocation> enemybases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);
					
					for(BaseLocation check : enemybases){
						if(enemyFEbase.equals(check)){
//							expanchcker = true;
							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
							setBlocking(true);
							return true;
							
						}
					}
				}
				if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==2) {
//					expanchcker = true;
					return false;
				}
//				setBlocking(true);
//				setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
//				return true;
				return false;
//			}
			
		}else if(InformationManager.Instance().enemyRace == Race.Protoss) {
		
	    	if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
					&& StrategyManager.Instance().getCurrentStrategyBasic() != EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO){
				
				if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){

						setBlocking(true);
						setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
						return true;

				}
			}else{
				if(Prebot.Broodwar.enemy().allUnitCount(UnitType.Protoss_Nexus) ==2){
					if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
						setBlocking(true);
						setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
						return true;
					}
				}
			}
		}
	    		
		return executeExpansion();

        
//        return false;
    }
    
    
    public boolean executeExpansion() {
    	if(StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT) {
    		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) < 2) {
    			return false;
    		}
    	}
    	
    	if(StrategyIdea.currentStrategy.expansionOption == ExpansionOption.ONE_FACTORY) {
    		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) < 1) {
    			return false;
    		}
    	}
    	
    	if(StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_FACTORY) {
    		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) < 2) {
    			return false;
    		}
    	}
		

		int MaxCCcount = 4;
		int CCcnt = 0;
		
		List<Unit> CommandCenter = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Armory);

		for (Unit unit : CommandCenter) {
			
			if (AttackExpansionManager.Instance().getValidMineralsForExspansionNearDepot(unit) > 6) {
				CCcnt++;

			}
		}
		
		if (CCcnt >= MaxCCcount) {
			return false;
		}

		int factoryUnitCount = UnitUtils.myFactoryUnitSupplyCount();
		int RealCCcnt = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);

		// 앞마당 전
		if (RealCCcnt == 1) {// TODO 이거 손봐야된다... 만약 위로 띄어서 해야한다면?? 본진에 지어진거 카운트 안되는 상황에서 앞마당에 지어버리겟네

			if ((Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 40 && AttackExpansionManager.Instance().UnitPoint > 15)
					|| (factoryUnitCount > 60 && AttackExpansionManager.Instance().Attackpoint > 60 && AttackExpansionManager.Instance().ExpansionPoint > 0)) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					return true;

			}
			if (Prebot.Broodwar.getFrameCount() > 9000 && Prebot.Broodwar.self().minerals() > 400) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					return true;

			}
			if (Prebot.Broodwar.getFrameCount() > 12000 && factoryUnitCount > 40) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					return true;

			}
			if (Prebot.Broodwar.getFrameCount() > 80000 && factoryUnitCount > 80) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					return true;

			}
			if ((Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 40)) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
					return true;

			}

		}

		// 앞마당 이후
		else if (RealCCcnt >= 2) {
			
			boolean isAttackMode = StrategyIdea.mainSquadMode.isAttackMode;

			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
			if (Prebot.Broodwar.self().minerals() > 600 && factoryUnitCount > 120) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;

			}
			// 공격시 돈 250 넘으면 멀티하기
			if (isAttackMode && Prebot.Broodwar.self().minerals() > 250) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;
				}
			}
			// 800 넘으면 멀티하기
			if (Prebot.Broodwar.self().minerals() > 800) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;
//				}
			}

			// 500 넘고 유리하면
			if (Prebot.Broodwar.self().minerals() > 500 && factoryUnitCount > 50 && AttackExpansionManager.Instance().Attackpoint > 30) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;
//				}
			}
			// 200 넘고 유리하면
			if (Prebot.Broodwar.self().minerals() > 200 && factoryUnitCount > 80 
					&& AttackExpansionManager.Instance().Attackpoint > 40 && AttackExpansionManager.Instance().ExpansionPoint >= 0) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;
//				}
			}
			// 공격시 돈 250 넘으면 멀티하기

			if (Prebot.Broodwar.getFrameCount() > 14000) {

				if (factoryUnitCount > 80 && Prebot.Broodwar.self().deadUnitCount() - Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Vulture) < 12) {
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
						setBlocking(true);
						setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
						return true;
//					}
				}
			}

			int temp = 0;
			for (Unit units : CommandCenter) {
				temp += WorkerManager.Instance().getWorkerData().getMineralsSumNearDepot(units);
			}
			if (temp < 8000 && isAttackMode) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;

			}
			if (factoryUnitCount > 160) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					setBlocking(true);
					setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextExpansionPoint);
					return true;
//				}
			}
		}
		
		return false;
	}


}
