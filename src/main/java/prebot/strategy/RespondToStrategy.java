package prebot.strategy;

import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.MapSpecificInformation.GameMap;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

/// 봇 프로그램 설정
public class RespondToStrategy {
	
	//클로킹 유닛에 대한 대처
	public boolean enemy_dark_templar;
	public boolean enemy_lurker;
	public boolean enemy_wraith;
	public boolean enemy_wraithcloak;
	
	public boolean enemy_guardian;
	
	public boolean enemy_shuttle;
	public boolean enemy_arbiter;
	public boolean enemy_mutal;
	public boolean enemy_scout;
	public boolean enemy_hive;
	
	public boolean need_vessel;
	public boolean need_valkyrie;
	public boolean need_wraith;
	public boolean need_battlecruiser;
	
	public boolean mainBaseTurret;
	public boolean firstChokeTurret;
	
	public boolean prepareDark;
	

	public int max_turret_to_mutal;
	public int max_vessel;
	public int max_valkyrie;
	public int max_wraith;
	public int max_battlecruiser;

	
	public int need_vessel_time;
	
	//초반 터렛 건설에 대한 체크
	private int chk_turret;
	
	public boolean center_gateway = false;
	
	public RespondToStrategy() {
		//클로킹 유닛에 대한 대처
		enemy_dark_templar = false;
		enemy_lurker = false;
		enemy_wraith = false;
		enemy_wraithcloak = false;
		
		enemy_guardian = false;
		
		enemy_shuttle = false;
		enemy_arbiter = false;
		enemy_mutal = false;
		enemy_scout = false;
		enemy_hive = false;
		
		need_vessel = false;
		need_valkyrie = false;
		need_wraith = false;
		need_battlecruiser = false;
		
		mainBaseTurret = false;
		firstChokeTurret = false;
		
		prepareDark = false;
		
		max_vessel = 0;
		max_valkyrie = 0;
		max_wraith = 0;
		max_battlecruiser = 0;
		max_turret_to_mutal = 0;
		
		need_vessel_time = 0;
		
		//초반 터렛 건설에 대한 체크
		chk_turret = 0;
	}
	
	private static RespondToStrategy instance = new RespondToStrategy();
	
	public static RespondToStrategy Instance() {
		return instance;
	}
	
	public boolean needOfEngineeringBay() {
		
		if(enemy_dark_templar || enemy_wraith || enemy_lurker || enemy_shuttle){
			return true;
		}
		return false;
	}
	
	public boolean needOfVessel() {
		
		if(enemy_arbiter || enemy_hive){
			return true;
		}
		return false;
	}
	
	public void update() {
		BuildQueueProvider.Instance().respondSet = true;
		max_turret_to_mutal = 0;
//		if(need_vessel==false && need_vessel_time!=0 && MyBotModule.Broodwar.getFrameCount() - need_vessel_time > 5000){
//			need_vessel = true;
//		}
		
		//최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행 
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			RespondVsProtoss();
		}else if (InformationManager.Instance().enemyRace == Race.Terran) {
			RespondVsTerran();
		}else{
			RespondVsZerg();
		}
		
		RespondExecute();
	}
		
	public boolean once = true;
	public boolean once_tank = true;
	public void RespondVsProtoss() {
		boolean blocked = true;
		//2gate zealot
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO){
			
			if(Prebot.Broodwar.getFrameCount() < 10000){
				BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
				BuildOrderItem checkItem = null; 
	
				if (!tempbuildQueue.isEmpty()) {
					checkItem= tempbuildQueue.getHighestPriorityItem();
					while(true){
						if(tempbuildQueue.canGetNextItem() == true){
							tempbuildQueue.canGetNextItem();
						}else{
							break;
						}
						tempbuildQueue.PointToNextItem();
						checkItem = tempbuildQueue.getItem();
						
						if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Command_Center){
							tempbuildQueue.removeCurrentItem();
						}
					}
				}
			}
//			if(once){
//
//				if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) 
//						+BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory) 
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) <= 2) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,true);
//				}
//				once = false;
//			}
//			이부분은 어째야 할지 모르겠따. 일단 예외처리
			/*if(once_tank) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,true);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,false);
				once_tank = false;
			}*/
			

		}
//		if (StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH) {
//			if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) >= 1
//					&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1) {
//				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3) {
//					if (TempBuildSourceCode.Instance().LiftChecker == false
//							&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4) {
//						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1) {
//							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,
//									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//						}
//					}
//					if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
//							&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker,
//									null) == 0) {
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker,
//								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					}
//				}
//			}
//		}
		
		
		
//		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH){
//			if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) >= 1){
//				if(center_gateway){
//					if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
//						if(TempBuildSourceCode.Instance().LiftChecker == false && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
//							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//							}
//						}
//						if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
//								&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
//								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){
//
//							// BlockingEntrance Prebot2버전을 사용하도록 하면서 주석처리
////							TilePosition bunkerPos = new TilePosition(BlockingEntrance.Instance().bunkerX,BlockingEntrance.Instance().bunkerY);
////							ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
////							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker, bunkerPos,true);
//
//						}
//					}
//				}else{
//					if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) >= 1) {
//						if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) < 3){
//							if(TempBuildSourceCode.Instance().LiftChecker == false && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
//								if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//								}
//							}
//							if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
//									&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
//									&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){
//								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//							}
//						}
//					}
//				}
//			}
//		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
				||StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH){
			
		}
	
//		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//				&& StrategyManager.Instance().getCurrentStrategyBasic() != EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO){
//			
//			if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//				}
//			}
//		}else{
//			if(Prebot.Broodwar.enemy().allUnitCount(UnitType.Protoss_Nexus) ==2){
//				if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
//						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//					}
//				}
//			}
//		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_SHUTTLE){
			enemy_shuttle = true;
		}
		
		//protossException_Dark start
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_DARK){
			enemy_dark_templar = true;
			//if(InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, MyBotModule.Broodwar.self())>4){
			need_vessel = true;
			max_vessel = 1;
			//}
		}
		//protossException_Dark end
		
		
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_ARBITER){
			enemy_arbiter = true;
			//if(InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, MyBotModule.Broodwar.self())>4){
			need_vessel = true;
			max_vessel = 2;
			//}
		}
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.PROTOSSEXCEPTION_SCOUT){
			enemy_scout = true;
		}
	}
	
	public void RespondVsTerran() {
		
		max_wraith = 5;
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.TERRANBASIC_BIONIC){
			max_wraith = 0;
			
			//스타포트 지우기
			BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
			BuildOrderItem checkItem = null; 

			if (!tempbuildQueue.isEmpty()) {
				checkItem= tempbuildQueue.getHighestPriorityItem();
				while(true){
					if(tempbuildQueue.canGetNextItem() == true){
						tempbuildQueue.canGetNextItem();
					}else{
						break;
					}
					tempbuildQueue.PointToNextItem();
					checkItem = tempbuildQueue.getItem();
					
					if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Starport){
						tempbuildQueue.removeCurrentItem();
					}
				}
			}
			
		}
		
		
		//terranException_Wraith start
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.TERRANBASIC_MECHANICWITHWRAITH){
			enemy_wraith = true;
		}
		//terranException_Wraith start
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.TERRANEXCEPTION_WRAITHCLOAK){
			enemy_wraithcloak = true;
			need_vessel = true;
			max_vessel = 2;
		}
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.TERRANBASIC_MECHANICWITHWRAITH){
			enemy_wraith = true;
		}
	}
		
	public boolean expanchcker = false;
	public void RespondVsZerg() {	
		
//		if(TempBuildSourceCode.Instance().LiftChecker == false && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Factory) > 1){
//			if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine, true);
//				}
//			}
//		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER
			||StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_FASTLURKER){
			enemy_lurker = true;
			need_vessel = true;
			max_vessel = 1;
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_HIGHTECH){
			need_vessel = true;
			max_vessel = 4;
		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_MUTALMANY){
			
//			if(Prebot.Broodwar.self().hasResearched(TechType.Irradiate) ==false &&Prebot.Broodwar.self().isResearching(TechType.Irradiate) ==false){
//				if(BuildManager.Instance().buildQueue.getItemCount(TechType.Irradiate) < 1){
//					BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Irradiate);
//				}
//			}
			need_vessel = true;
			max_vessel = 4;
		}
//		else{
//			need_valkyrie = false;
//			max_valkyrie = 0;
//		}
		
//		if(MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Metabolic_Boost) == 1){
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//				}
//			}
//		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_MUTAL ||
                StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_MUTALMANY ||
                        StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_HYDRAMUTAL ||
                                StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_LINGMUTAL)
		{
			max_turret_to_mutal = 3;
		}
	
//		if(StrategyManager.Instance().getCurrentStrategyException() == EnemyStrategyException.ZERGEXCEPTION_ONLYLING){
//			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().selfPlayer) < 5	){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) < 1){
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
//				}
//			}
//		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ZERGBASIC_MUTAL){
			enemy_mutal = true;
		}
		
		
//		if(expanchcker==false){
//			BaseLocation enemyMainbase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
//			
//			if(enemyMainbase != null && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1 && UnitUtils.myFactoryUnitSupplyCount() >= 25){
//				
//				
//				BaseLocation enemyFEbase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
//				List<BaseLocation> enemybases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer);
//				
//				for(BaseLocation check : enemybases){
//					if(enemyFEbase.equals(check)){
//						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
//								+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
//							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//							expanchcker = true;
//							
//						}
//					}
//				}
//			}
//			if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==2)
//				expanchcker = true;
//		}
	}
		
		
	public void RespondExecute() {

		// if(prepareDark == true){
		// if(!chk_engineering_bay){
		// if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay)
		// +
		// ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay,
		// null) == 0){
		// BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
		// BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// //}
		// }
		// }
		// }
		
		//marine for fast zergling and zealot start
//		if(TempBuildSourceCode.Instance().LiftChecker == false && StrategyIdea.enemyUnitStatus == EnemyUnitStatus.IN_MY_REGION){
//			if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine, true);
//				}
//			}
//		}
		//marine for fast zergling and zealot end
		// enemy_dark_templar & enemy_lurker & enemy_wraith 클로킹 유닛에 대한 대비
		// if(enemy_dark_templar || enemy_wraith || enemy_lurker ||
		// enemy_arbiter || enemy_mutal || prepareDark){
		if (enemy_dark_templar || enemy_wraith || enemy_lurker || enemy_arbiter || prepareDark) {
			if (need_vessel_time == 0) {
				need_vessel_time = Prebot.Broodwar.getFrameCount();
			}

//			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Comsat_Station) && UnitUtils.myFactoryUnitSupplyCount() >= 32) {
//				// 컴셋이 없다면
//				if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Academy)) {
//					// 아카데미가 없다면
//					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1
//							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
//						// 지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Academy,
//								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					}
//				} else {
//					if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) > 0) {
//						// 아카데미가 완성되었고
//						if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1
//								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0) {
//							// 빌드큐에 컴셋이 없는데, 아카데미가 완성되었다면빌드큐에 컴셋 입력
//							if (Prebot.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
//									&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()) {
//								BuildManager.Instance().buildQueue
//										.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
//							}
//						}
//					}
//				}
//			}

//			if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay) && (InformationManager.Instance().enemyRace != Race.Protoss || Prebot.Broodwar.getFrameCount() > 5000)){
//				
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
//					//BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//				}
//			}else{
			if(UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay) && (InformationManager.Instance().enemyRace != Race.Protoss || Prebot.Broodwar.getFrameCount() > 5000)){
				int turretcnt = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0 && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
					BaseLocation tempBaseLocation = InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self());
					BaseLocation tempExpLocation = InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.self());
					Chokepoint tempChokePoint = InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self());
					Chokepoint temp2ChokePoint = InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.self());
 
					mainBaseTurret = false;
					firstChokeTurret = false;
					Boolean secondChokeTurret = false;
					Boolean firstChokeMainHalfTurret = false;
					Boolean firstChokeExpHalfTurret = false;
					
//					MyBotModule.Broodwar.drawCircleMap(tempBaseLocation.getRegion().getCenter(),180, Color.White);
 					if (tempBaseLocation != null) {
 						List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(tempBaseLocation.getPosition(),350+turretcnt*15);
// 						MyBotModule.Broodwar.drawCircleMap(tempBaseLocation.getRegion().getCenter(),300+turretcnt*15, Color.Red);
 						for(Unit turret : turretInRegion){
 							if (turret.getType() == UnitType.Terran_Missile_Turret) {
 								mainBaseTurret = true;
 							}
 						}
						if (!mainBaseTurret) {
							//if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getRegion().getCenter().toTilePosition(), 300)
							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), 300)
							
								+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), 300) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempBaseLocation.getPosition().toTilePosition(), true);
							}
						}
 					}
 
 					if (tempBaseLocation != null) { 
   						//Position firstChokeMainHalf = new Position((tempBaseLocation.getRegion().getCenter().getX() + tempChokePoint.getX()*2)/3 - 60, (tempBaseLocation.getRegion().getCenter().getY() + tempChokePoint.getY()*2)/3 - 60);
 						Position firstChokeMainHalf = new Position((tempBaseLocation.getPosition().getX() + tempChokePoint.getX()*2)/3 - 60, (tempBaseLocation.getPosition().getY() + tempChokePoint.getY()*2)/3 - 60);
   						List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(firstChokeMainHalf,180+turretcnt*15);
//   						MyBotModule.Broodwar.drawCircleMap(firstChokeMainHalf,180+turretcnt*15, Color.Orange);	

   						for(Unit turret : turretInRegion){
   							if (turret.getType() == UnitType.Terran_Missile_Turret) {
   								firstChokeMainHalfTurret = true;
   							}
   						}
   						if (!firstChokeMainHalfTurret) {
   							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeMainHalf.toTilePosition(), 180) 
   									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition(), 180) == 0){
   								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, firstChokeMainHalf.toTilePosition(), true);
   							}
   						}
					}
 					
 					if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.THE_HUNTERS){
						if (tempChokePoint != null) {
							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(tempChokePoint.getCenter(),150+turretcnt*15);
//							MyBotModule.Broodwar.drawCircleMap(tempChokePoint.getCenter(),150+turretcnt*15, Color.Blue);
							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									firstChokeTurret = true;
								}
							}
							if (!firstChokeTurret) {
								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 150) 
									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 150) == 0){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), true);
								}
							}
						}  
					}else{
						if (tempBaseLocation != null) { 
	   						Position firstChokeExpHalf = new Position((tempExpLocation.getPosition().getX()*2 + tempChokePoint.getX())/3, (tempExpLocation.getPosition().getY()*2 + tempChokePoint.getY())/3);
	   						List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(firstChokeExpHalf.getPoint(),210+turretcnt*15);
//	   						MyBotModule.Broodwar.drawCircleMap(firstChokeExpHalf,150+turretcnt*15, Color.Blue);
	   						for(Unit turret : turretInRegion){
	   							if (turret.getType() == UnitType.Terran_Missile_Turret) {
	   								firstChokeExpHalfTurret = true;
	   							}
	   						}
	   						if (!firstChokeExpHalfTurret) {
	   							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeExpHalf.toTilePosition(), 150) 
	   									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeExpHalf.toTilePosition(), 150) == 0){
	   								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, firstChokeExpHalf.toTilePosition(), true);
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
 									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,  temp2ChokePoint.getCenter().toTilePosition(), true);
 								}
 							}
 						}
 					}
				}
			}
		}
		
//		if(enemy_scout || enemy_shuttle || enemy_wraith){
//			if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
//					if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
//							&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
//								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					}
//				}
//			}else{
//				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 2){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
//						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
//								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
//							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
//									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//						}
//					}
//				}
//			}
//		}
		
//		if(enemy_arbiter){
//			if(!UnitUtils.myUnitDiscovered(UnitType.Terran_Armory)){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
//					if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
//							&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
//								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					}
//				}
//			}else{
//				if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
//						InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 4)
//						|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 4){
//					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
//						if(Prebot.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
//								&& Prebot.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
//							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
//									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//						}
//					}
//				}
//			}
//		}
		
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.TERRANBASIC_BATTLECRUISER){
			need_battlecruiser = true;
			max_battlecruiser = 8;
		}else if(StrategyManager.Instance().getCurrentStrategyBasic() == EnemyStrategy.ATTACKISLAND){
			need_battlecruiser = true;
			max_battlecruiser = 8;
		}else{
			need_battlecruiser = false;
			max_battlecruiser = 0;
		}

		if (max_turret_to_mutal != 0) {

//			if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay)) {
//				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) 
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay,	null) == 0) {
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
//							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//				}
//			} else {
			if (UnitUtils.myUnitDiscovered(UnitType.Terran_Engineering_Bay)) {
				if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0 && Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) < 10) {
					int build_turret_cnt = 0;
					int turretcnt =  Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
					//지역 멀티
					
					BaseLocation mainBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					BaseLocation expBase = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
					if (mainBase != null) {
						
						List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(mainBase.getPosition(), 550+turretcnt*15);
						build_turret_cnt = 0;
						for(Unit unit: turretInRegion){
							if (unit.getType() == UnitType.Terran_Missile_Turret) {
								build_turret_cnt++;
							}
						}

						if (build_turret_cnt < max_turret_to_mutal) {
							if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, mainBase.getPosition().toTilePosition(), 300) < 1
									&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	mainBase.getPosition().toTilePosition(), 300) == 0) {
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, mainBase.getPosition().toTilePosition(),true);
							}
						}
					}
					if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) > 1){
						if (expBase != null) {
							
							List<Unit> turretInRegion = Prebot.Broodwar.getUnitsInRadius(expBase.getPosition(), 300+turretcnt*15);
							build_turret_cnt = 0;
							for(Unit unit: turretInRegion){
								if (unit.getType() == UnitType.Terran_Missile_Turret) {
									build_turret_cnt++;
								}
							}
	
							if (build_turret_cnt < max_turret_to_mutal) {
								if (BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, expBase.getPosition().toTilePosition(), 300) < 1
										&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret,	expBase.getPosition().toTilePosition(), 300) == 0) {
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, expBase.getPosition().toTilePosition(),true);
								}
							}
						}
					}
				}
			}
		}
	}
}