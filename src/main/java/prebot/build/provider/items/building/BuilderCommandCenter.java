package prebot.build.provider.items.building;

import java.util.List;

import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyManager;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuilderCommandCenter extends DefaultBuildableItem {

    public BuilderCommandCenter(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
        System.out.println("CommandCenter build condition check");
        if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
				&& StrategyManager.Instance().getCurrentStrategyBasic() != EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO){
			
			if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) == 1){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					return true;
				}
			}
		}
        
        if(RespondToStrategy.Instance().expanchcker==false){
        	if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==2) {
				RespondToStrategy.Instance().expanchcker = true;
				return false;
			}
			BaseLocation enemyMainbase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
			
			if(enemyMainbase != null && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1 && UnitUtils.myFactoryUnitSupplyCount() >= 25){
				
				
				BaseLocation enemyFEbase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
				List<BaseLocation> enemybases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);
				
				for(BaseLocation check : enemybases){
					if(enemyFEbase.equals(check)){
						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
							RespondToStrategy.Instance().expanchcker = true;
							//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
							return true;
							
							
						}
					}
				}
			}
			
		}
        
        
        
        return false;
    }


}
