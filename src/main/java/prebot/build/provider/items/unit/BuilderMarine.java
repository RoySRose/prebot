package prebot.build.provider.items.unit;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BarrackUnitSelector;
import prebot.build.provider.BuildConditionChecker;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.FactoryUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;
import prebot.strategy.StrategyManager;
import prebot.strategy.TempBuildSourceCode;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuilderMarine extends DefaultBuildableItem {

	BarrackUnitSelector barrackUnitSelector;

    public BuilderMarine(MetaType metaType, BarrackUnitSelector barrackUnitSelector){
        super(metaType);
        this.barrackUnitSelector = barrackUnitSelector;
    }
    
    public boolean liftChecker = true;

    public final boolean buildCondition(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
    	
//    	liftChecker = false;

        if(barrackUnitSelector.getSelected().equals(metaType.getUnitType())) {
            return true;
        }else{
        	if(BuildQueueProvider.Instance().respondSet) {
        		
        		List<Unit> Barracks = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Barracks);
        		
        		
//        		배럭이 몇개가 있던 착륙해 있는 배럭이 한개라도 있다면 마린 생산 가능
        		for(Unit unit : Barracks) {
        			if(!unit.lift()) {
        				liftChecker = false;
        				break;
        			}
        		}
        		
        		if(InformationManager.Instance().enemyRace == Race.Zerg) {
        			if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) > 1){
        				if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
        					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//        						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine, true);
        						setBlocking(true);
        						setHighPriority(true);
        						return true;
        					}
        				}
        			}
        		}else if(InformationManager.Instance().enemyRace == Race.Protoss) {
        		
	        		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH){
        				if(RespondToStrategy.Instance().center_gateway){
        					if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
        						if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
        							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//        								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
        								setBlocking(true);
            							setHighPriority(true);
            							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
            							return true;
        							}
        						}

	        				}else{
	        					if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) >= 1) {
	        						if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
	        							if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
	        								if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
	//        									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
	        									setBlocking(true);
	                							setHighPriority(true);
	                							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
	                							return true;
	        								}
	        							}
	        						}
	        					}
	        				}
	        			}
	        		}
	        		
	        		
        		}
        		
        		if(liftChecker == false && StrategyIdea.enemyUnitStatus == EnemyUnitStatus.IN_MY_REGION){
        			if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
        				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//        					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine, true);
        					setBlocking(true);
        					setHighPriority(true);
        					return true;
        				}
        			}
        		}
        	}
            return false;
        }
    }
}
