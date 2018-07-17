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
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.StrategyIdea;
import prebot.strategy.MapSpecificInformation.GameMap;

public class BuilderMissileTurret extends DefaultBuildableItem {

    public BuilderMissileTurret(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) == 0) {
    		return false;
    	}
    	
    	boolean mainBaseTurret;
    	boolean firstChokeTurret;

    		
    		if(StrategyIdea.turretBuildStartFrame < Prebot.Broodwar.getFrameCount()) {
				int turretcnt = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
				
//				첫번째 터렛이 없고, 입막 터렛 위치가 지정되어있을경우
				if( !UnitUtils.myUnitDiscovered(UnitType.Terran_Missile_Turret) 
					&& BlockingEntrance.Instance().entrance_turret != TilePosition.None
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret) 
					+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, null) == 0
					){
//					System.out.println("BlockingEntrance Turret ==> " + BlockingEntrance.Instance().entrance_turret.getX() + " / " +BlockingEntrance.Instance().entrance_turret.getY());
					setHighPriority(true);
					setBlocking(true);
					setTilePosition(BlockingEntrance.Instance().entrance_turret);
//					setSeedPositionStrategy(tempBaseLocation.getPosition().toTilePosition());
					return true;
				}
				
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
					BaseLocation tempBaseLocation = InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self());
					BaseLocation tempExpLocation = InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.self());
					Chokepoint tempChokePoint = InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self());
					Chokepoint temp2ChokePoint = InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.self());
	
					mainBaseTurret = false;
					firstChokeTurret = false;
					Boolean secondChokeTurret = false;
					Boolean firstChokeMainHalfTurret = false;
					Boolean firstChokeExpHalfTurret = false;
					
	//				MyBotModule.Broodwar.drawCircleMap(tempBaseLocation.getRegion().getCenter(),180, Color.White);
						if (tempBaseLocation != null) {
							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(tempBaseLocation.getPosition(),350+turretcnt*15);
	//						MyBotModule.Broodwar.drawCircleMap(tempBaseLocation.getRegion().getCenter(),300+turretcnt*15, Color.Red);
							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									mainBaseTurret = true;
								}
							}
							if (!mainBaseTurret) {
////								빌드큐에 터렛이 있어야 아래 체크가 가능하므로, 미리 체크
//								if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret)+ 
//										+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition()) == 0){
//									System.out.println("터렛 빌드큐에 없음");
//									setHighPriority(true);
//									setBlocking(true);
//									setTilePosition(tempBaseLocation.getPosition().toTilePosition());
//									return true;
//								}
								//if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getRegion().getCenter().toTilePosition(), 300)
								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), 300)
								
									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), 300) == 0){
									
//									System.out.println("터렛 빌드큐에 있는 터렛 위치 체크");
									//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), true);
									setHighPriority(true);
									setBlocking(true);
									setTilePosition(tempBaseLocation.getPosition().toTilePosition());
//									setSeedPositionStrategy(tempBaseLocation.getPosition().toTilePosition());
									return true;
								}
							}
						}
	
						if (tempBaseLocation != null) { 
							//Position firstChokeMainHalf = new Position((tempBaseLocation.getRegion().getCenter().getX() + tempChokePoint.getX()*2)/3 - 60, (tempBaseLocation.getRegion().getCenter().getY() + tempChokePoint.getY()*2)/3 - 60);
							Position firstChokeMainHalf = new Position((tempBaseLocation.getPosition().getX() + tempChokePoint.getX()*2)/3 - 60, (tempBaseLocation.getPosition().getY() + tempChokePoint.getY()*2)/3 - 60);
							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(firstChokeMainHalf,180+turretcnt*15);
	//						MyBotModule.Broodwar.drawCircleMap(firstChokeMainHalf,180+turretcnt*15, Color.Orange);	
	
							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									firstChokeMainHalfTurret = true;
								}
							}
							if (!firstChokeMainHalfTurret) {
//								빌드큐에 터렛이 있어야 아래 체크가 가능하므로, 미리 체크								
								if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret)+ 
										+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition()) == 0){
									setHighPriority(true);
									setBlocking(true);
									setTilePosition(firstChokeMainHalf.toTilePosition());
									return true;
								}
								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeMainHalf.toTilePosition(), 180) 
										+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition(), 180) == 0){
									//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition(), true);
									setHighPriority(true);
									setBlocking(true);
									setTilePosition(firstChokeMainHalf.toTilePosition());
									return true;
								}
							}
					}
						
						if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.THE_HUNTERS){
						if (tempChokePoint != null) {
							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(tempChokePoint.getCenter(),150+turretcnt*15);
	//						MyBotModule.Broodwar.drawCircleMap(tempChokePoint.getCenter(),150+turretcnt*15, Color.Blue);
							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									firstChokeTurret = true;
								}
							}
							if (!firstChokeTurret) {
								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 150) 
									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 150) == 0){
									//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), true);
									
									setHighPriority(true);
									setBlocking(true);
									setTilePosition(tempChokePoint.getCenter().toTilePosition());
									
									return true;
								}
							}
						}  
					}else{
						if (tempBaseLocation != null) { 
	   						Position firstChokeExpHalf = new Position((tempExpLocation.getPosition().getX()*2 + tempChokePoint.getX())/3, (tempExpLocation.getPosition().getY()*2 + tempChokePoint.getY())/3);
	   						List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(firstChokeExpHalf.getPoint(),210+turretcnt*15);
	//   						MyBotModule.Broodwar.drawCircleMap(firstChokeExpHalf,150+turretcnt*15, Color.Blue);
	   						for(Unit turret : turretInRegion){
	   							if (turret.getType() == UnitType.Terran_Missile_Turret) {
	   								firstChokeExpHalfTurret = true;
	   							}
	   						}
	   						if (!firstChokeExpHalfTurret) {
	   							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeExpHalf.toTilePosition(), 150) 
	   									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeExpHalf.toTilePosition(), 150) == 0){
	   								//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, firstChokeExpHalf.toTilePosition(), true);
	   								setHighPriority(true);
									setBlocking(true);
									setTilePosition(firstChokeExpHalf.toTilePosition());
	   								return true;
	   							}
	   						}
						}
						
					}
						
					if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) > 1){
						if (temp2ChokePoint != null) {
							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(temp2ChokePoint.getCenter(),100+turretcnt*15);
	
							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									secondChokeTurret = true;
								}
							}
							if (!secondChokeTurret) {
								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, temp2ChokePoint.getCenter().toTilePosition(), 100) 
										+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, temp2ChokePoint.getCenter().toTilePosition(), 100) == 0){
									//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,  temp2ChokePoint.getCenter().toTilePosition(), true);
									setHighPriority(true);
									setBlocking(true);
									setTilePosition(temp2ChokePoint.getCenter().toTilePosition());
									return true;
								}
							}
						}
					}
				}
			}
	        
//	        if(RespondToStrategy.Instance().enemy_scout 
//	        		|| RespondToStrategy.Instance().enemy_shuttle 
//	        		|| RespondToStrategy.Instance().enemy_wraith){
//				if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
//						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
//								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
//									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//						}
//					}
//				}else{
//					if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 2){
//						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
//							if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
//									&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
//								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
//										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//							}
//						}
//					}
//				}
//			}
//	        
//	        if(RespondToStrategy.Instance().enemy_arbiter){
//				if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
//						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
//								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
//							/*BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
//									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
//							return true;
//						}
//					}
//				}else{
//					if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
//							InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 4)
//							|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 4){
//						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
//							if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
//									&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
//								/*BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
//										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);*/
//								return true;
//							}
//						}
//					}
//				}
//			}
//	        
//	        if (RespondToStrategy.Instance().max_turret_to_mutal != 0) {
//				
//				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0 && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
//					int build_turret_cnt = 0;
//					int turretcnt =  Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
//					//지역 멀티
//					
//					BaseLocation mainBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//					BaseLocation expBase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//					if (mainBase != null) {
//						
//						List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(mainBase.getPosition(), 550+turretcnt*15);
//						build_turret_cnt = 0;
//						for(Unit unit: turretInRegion){
//							if (unit.getType() == UnitType.Terran_Missile_Turret) {
//								build_turret_cnt++;
//							}
//						}
//
//						if (build_turret_cnt < max_turret_to_mutal) {
//							if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, mainBase.getPosition().toTilePosition(), 300) < 1
//									&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	mainBase.getPosition().toTilePosition(), 300) == 0) {
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, mainBase.getPosition().toTilePosition(),true);
//							}
//						}
//					}
//					if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) > 1){
//						if (expBase != null) {
//							
//							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(expBase.getPosition(), 300+turretcnt*15);
//							build_turret_cnt = 0;
//							for(Unit unit: turretInRegion){
//								if (unit.getType() == UnitType.Terran_Missile_Turret) {
//									build_turret_cnt++;
//								}
//							}
//	
//							if (build_turret_cnt < max_turret_to_mutal) {
//								if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, expBase.getPosition().toTilePosition(), 300) < 1
//										&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	expBase.getPosition().toTilePosition(), 300) == 0) {
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, expBase.getPosition().toTilePosition(),true);
//								}
//							}
//						}
//					}
//				}
//			}
//	    }
        return false;
    }
}
