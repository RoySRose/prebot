package prebot.build.provider.items.building;

import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
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
import prebot.strategy.MapSpecificInformation.GameMap;
import prebot.strategy.constant.EnemyStrategyOptions;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;
import prebot.strategy.constant.EnemyStrategyOptions.FactoryRatio;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class BuilderFactory extends DefaultBuildableItem {

    public BuilderFactory(MetaType metaType){
        super(metaType);
    }
    
    private int vultureratio = 0;
    private int tankratio = 0;
    private int goliathratio = 0;
    private int wgt = 1;
    
    private boolean EXOK = false;

    public final boolean buildCondition(){
//    	if(BuildQueueProvider.Instance().respondSet) {
//    		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO){
//		    	if(RespondToStrategy.Instance().once){
//					
//					/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,true);
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,false);*/
//					if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) 
//							+BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory) 
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) <= 2) {
//	//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,true);
//						setBlocking(true);
//						return true;
//					}
//					RespondToStrategy.Instance().once = false;
//				}
//	    	}
//    	}else {
////        System.out.println("Factory build condition check");
//    		if (InitialBuildProvider.Instance().InitialBuildFinished == false && Prebot.Broodwar.getFrameCount() % 23 == 0) {
//	    		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory)
//	    				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) < BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory)) {
//	//    			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//	    			setHighPriority(true);
//	    			setBlocking(true);
//	    			setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
//	    			return true;
//	    		}
//    		}
//	        
//    		
//    		if(InitialBuildProvider.Instance().InitialBuildFinished && Prebot.Broodwar.getFrameCount() % 113 == 0) {
    	executeFirstex();
    	if(StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT) {
    		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) < 2) {
    			return false;
    		}
    		
    		if(!EXOK) {
    			return false;
    		}
    		
    	}
    			
    			int CCcnt = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
    			int maxFaccnt = 0;
    			int Faccnt = 0;
    			boolean facFullOperating = true;
    			
    			List<Unit> factory = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Factory);
    			
    			

    			for (Unit unit : factory) {
//    				if (unit == null)
//    					continue;
//    				if (unit.getType().isResourceDepot() && unit.isCompleted()) {
//    					CCcnt++;
//    				}
    				if (unit.isCompleted()) {
    					Faccnt++;
    				}
    				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()) {
    					if (unit.isTraining() == false) {
    						facFullOperating = false;
    					}
    				}
    				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() == false) {
    					facFullOperating = false;
    				}
    			}

    			if (CCcnt <= 1) {
    					maxFaccnt = 3;
    			} else if (CCcnt == 2) {
    					maxFaccnt = 6;
    			} else if (CCcnt >= 3) {
    				maxFaccnt = 9;
    			}

    			Faccnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);

    			int additonalmin = 0;
    			int additonalgas = 0;
    			if (facFullOperating == false) {
    				additonalmin = (Faccnt - 1) * 40;
    				additonalgas = (Faccnt - 1) * 20;
    			}

    			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) == 0) {
    				if (Faccnt == 0) {
//    					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
    					setBlocking(true);
    					setHighPriority(true);
    					return true;
    					
    				} else if (Faccnt < maxFaccnt) {
    					if (Prebot.Broodwar.self().minerals() > 200 + additonalmin && Prebot.Broodwar.self().gas() > 100 + additonalgas) {
    						if (Faccnt == 2) {
    							if (UnitUtils.myFactoryUnitSupplyCount() > 24 && facFullOperating) {
//    								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
    								return true;
    							}
    						} else if (Faccnt <= 6) {
//    							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
    							return true;
    						} else {
//    							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.LastBuilingPoint, false);
    							setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.LastBuilingPoint);
    							return true;
    						}
    					}
    				}
    			}


    			
//    			
//    		}
//    	}
//		
//        
        
        return false;
    }
    
    public void executeFirstex() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
			Unit checkCC = null;
			BaseLocation temp = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
			for (Unit unit : Prebot.Broodwar.self().getUnits()) {

				if (unit.getType() != UnitType.Terran_Command_Center) {
					continue;
				}
				if (unit.getTilePosition().getX() == BlockingEntrance.Instance().starting.getX() && unit.getTilePosition().getY() == BlockingEntrance.Instance().starting.getY()) {
					continue;
				} else {
					checkCC = unit;
					break;
				}
			}
			if (checkCC != null) {
				if (checkCC.isLifted() == false) {
					if (checkCC.getTilePosition().getX() != temp.getTilePosition().getX() || checkCC.getTilePosition().getY() != temp.getTilePosition().getY()) {
						checkCC.lift();
					}
				} else {
					checkCC.land(new TilePosition(temp.getTilePosition().getX(), temp.getTilePosition().getY()));
				}
				if (checkCC.isLifted() == false && checkCC.getTilePosition().getX() == temp.getTilePosition().getX()
						&& checkCC.getTilePosition().getY() == temp.getTilePosition().getY()) {
					EXOK = true;
				}
			}
		}
	}
    
    
//    public int getFacUnits(){
//		
//		int tot=0;
//		for (Unit unit : Prebot.Broodwar.self().getUnits())
//		{
//			if (unit == null) continue;
//			if (unit.getType() == UnitType.Terran_Vulture && unit.isCompleted() ){
//				tot++;
//			}
//			if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && unit.isCompleted() ){
//				tot++;
//			}
//			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && unit.isCompleted() ){
//				tot++;
//			}
//			if (unit.getType() == UnitType.Terran_Goliath && unit.isCompleted() ){
//				tot++;
//			}
// 		}
//		return tot*4; //인구수 기준이므로
//	}
//    
//    
//    
//    private void setCombatUnitRatio() {
//		vultureratio = StrategyIdea.currentStrategy.ratio.vulture;
//		tankratio = StrategyIdea.currentStrategy.ratio.tank;
//		goliathratio = StrategyIdea.currentStrategy.ratio.goliath;
//		
//		
//		
//
////		// config setting 가지고 오기
////		if (StrategyManager.Instance().currentStrategyException == EnemyStrategyException.INIT) {
////			vultureratio = StrategyManager.Instance().currentStrategy.vultureRatio;
////			tankratio = StrategyManager.Instance().currentStrategy.tankRatio;
////			goliathratio = StrategyManager.Instance().currentStrategy.goliathRatio;
////			wgt = StrategyManager.Instance().currentStrategy.weight;
////		} else {
////			vultureratio = StrategyManager.Instance().currentStrategyException.vultureRatio;
////			tankratio = StrategyManager.Instance().currentStrategyException.tankRatio;
////			goliathratio = StrategyManager.Instance().currentStrategyException.goliathRatio;
////			wgt = StrategyManager.Instance().currentStrategyException.weight;
////
////			if (vultureratio == 0 && tankratio == 0 && goliathratio == 0) {
////				vultureratio = StrategyManager.Instance().currentStrategy.vultureRatio;
////				tankratio = StrategyManager.Instance().currentStrategy.tankRatio;
////				goliathratio = StrategyManager.Instance().currentStrategy.goliathRatio;
////				wgt = StrategyManager.Instance().currentStrategy.weight;
////			}
////		}
//	}


}
