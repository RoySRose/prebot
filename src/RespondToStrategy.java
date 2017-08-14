import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Region;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// 봇 프로그램 설정
public class RespondToStrategy {
	
	//클로킹 유닛에 대한 대처
	public boolean enemy_dark_templar;
	public boolean enemy_lurker;
	public boolean enemy_wraith;
	
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
	
	//유닛체크values()
	public boolean chk_scv;
	public boolean chk_marine;
	public boolean chk_goliath;
	public boolean chk_vulture;
	public boolean chk_siege_tank;
	public boolean chk_vessel;
	public boolean chk_wraith;
	public boolean chk_valkyrie;
	
	//건물체크
	public boolean chk_refinery;
	public boolean chk_barrack;
	public boolean chk_engineering_bay;
	public boolean chk_missile_turret;
	public boolean chk_academy;
	public boolean chk_factory;
	public boolean chk_machine_shop;
	public boolean chk_armory;
	public boolean chk_starport;
	public boolean chk_control_tower;
	public boolean chk_comsat_station;
	public boolean chk_science_facility;
	
	public RespondToStrategy() {
		//클로킹 유닛에 대한 대처
		enemy_dark_templar = false;
		enemy_lurker = false;
		enemy_wraith = false;
		
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
		
		//유닛체크values()
		chk_scv = false;
		chk_marine = false;
		chk_goliath = false;
		chk_vulture = false;
		chk_siege_tank = false;
		chk_vessel = false;
		chk_wraith = false;
		chk_valkyrie = false;
		
		//건물체크
		chk_refinery = false;
		chk_barrack = false;
		chk_engineering_bay = false;
		chk_missile_turret = false;
		chk_academy = false;
		chk_factory = false;
		chk_machine_shop = false;
		chk_armory = false;
		chk_starport = false;
		chk_control_tower = false;
		chk_comsat_station = false;
		chk_science_facility = false;
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
		max_turret_to_mutal = 0;
//		if(need_vessel==false && need_vessel_time!=0 && MyBotModule.Broodwar.getFrameCount() - need_vessel_time > 5000){
//			need_vessel = true;
//		}
		
		//System.out.println("Respond Strategy Manager On Update!!!!!!!!!!!!!!! ");
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {

			if (unit.getType() == UnitType.Terran_SCV ) {
				chk_scv = true;
			}
			if (unit.getType() == UnitType.Terran_Marine) {
				chk_marine = true;
			}
			if (unit.getType() == UnitType.Terran_Goliath) {
				chk_goliath = true;
			}
			if (unit.getType() == UnitType.Terran_Vulture) {
				chk_vulture = true;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
				chk_siege_tank = true;
			}
			if (unit.getType() == UnitType.Terran_Wraith) {
				chk_wraith = true;
			}
			if (unit.getType() == UnitType.Terran_Valkyrie) {
				chk_valkyrie = true;
			}
			if (unit.getType() == UnitType.Terran_Science_Vessel) {
				chk_vessel = true;
			}
			if (unit.getType() == UnitType.Terran_Refinery) {
				chk_refinery = true;
			}
			if (unit.getType() == UnitType.Terran_Barracks) {
				chk_barrack = true;
			}
			if (unit.getType() == UnitType.Terran_Engineering_Bay) {
				chk_engineering_bay = true;
			}
			if (unit.getType() == UnitType.Terran_Missile_Turret) {
				chk_missile_turret = true;
			}
			if (unit.getType() == UnitType.Terran_Academy) {
				chk_academy = true;
			}
			if (unit.getType() == UnitType.Terran_Comsat_Station) {
				chk_comsat_station = true;
			}
			if (unit.getType() == UnitType.Terran_Factory) {
				chk_factory = true;
			}
			if (unit.getType() == UnitType.Terran_Machine_Shop) {
				chk_machine_shop = true;
			}
			if (unit.getType() == UnitType.Terran_Armory) {
				chk_armory = true;
			}
			if (unit.getType() == UnitType.Terran_Starport) {
				chk_starport = true;
			}
			if (unit.getType() == UnitType.Terran_Control_Tower) {
				chk_control_tower = true;
			}
			if (unit.getType() == UnitType.Terran_Science_Facility) {
				chk_science_facility = true;
			}
		}
		
		//최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행 
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			RespondVsProtoss();
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			RespondVsTerran();
		}else{
			RespondVsZerg();
		}
		
		RespondExecute();
	}
		
	public void RespondVsProtoss() {
		boolean blocked = true;
		//2gate zealot
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ZealotPush){
			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) >= 1){
				if(blocked){
					//입구 막혔으면 이미 한마리가 있음
				}else{
					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) == 0){
						if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) < 4){
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine) < 1){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Marine,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
						if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Bunker) < 1
								&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Bunker) < 1
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Bunker, null) == 0){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Bunker,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}else{
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null) == 0) {
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, true);
						}
					}
				}
			}
//			//멀티 지우기
//			BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//			BuildOrderItem checkItem = null; 
//
//			if (!tempbuildQueue.isEmpty()) {
//				checkItem= tempbuildQueue.getHighestPriorityItem();
//				while(true){
//					if(tempbuildQueue.canGetNextItem() == true){
//						tempbuildQueue.canGetNextItem();
//					}else{
//						break;
//					}
//					tempbuildQueue.PointToNextItem();
//					checkItem = tempbuildQueue.getItem();
//					
//					if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Command_Center){
//						tempbuildQueue.removeCurrentItem();
//					}
//				}
//			}
//			
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1
//					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) == 0){
//				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture,
//						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			}
//			
//			//팩토리 추가
//			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory) == 0
//					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) == 0
//					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 1){
//				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,
//						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			}
		}
		
//		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.Init
//				&& StrategyManager.Instance().getLastStrategyException() == StrategyManager.StrategysException.protossException_ZealotPush){
//			//컴맨드 추가
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 1){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Command_Center,
//							BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//		}
//		
//		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.Init
//				&& StrategyManager.Instance().getLastStrategyException() == StrategyManager.StrategysException.protossException_DragoonPush){
//			//컴맨드 추가
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 1){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Command_Center,
//							BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//		}
		

//		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.Init
//				&& StrategyManager.Instance().getLastStrategyException() == StrategyManager.StrategysException.protossException_PhotonRush){
//			//컴맨드 추가
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 1){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) == 0){
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Command_Center,
//							BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				}
//			}
//		}
		
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DragoonPush
				||StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_PhotonRush){
			
			//멀티 지우기
//			BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//			BuildOrderItem checkItem = null; 
//	
//			//컴맨드 빼기, 벌쳐 빼기
//			if (!tempbuildQueue.isEmpty()) {
//				checkItem= tempbuildQueue.getHighestPriorityItem();
//				while(true){
//					if(tempbuildQueue.canGetNextItem() == true){
//						tempbuildQueue.canGetNextItem();
//					}else{
//						break;
//					}
//					tempbuildQueue.PointToNextItem();
//					checkItem = tempbuildQueue.getItem();
//					
//					if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Command_Center){
//						tempbuildQueue.removeCurrentItem();
//					}
//					if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Vulture){
//						if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1){
//							tempbuildQueue.removeCurrentItem();
//						}
//					}
//				}
//			}
//			//팩토리 추가
//			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory) == 0
//					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) == 0
//					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 1){
//				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,
//						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			}
//			
//			//시즈 넣기
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1
//					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode) == 0
//					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop) >= 1
//					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) < 3){
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,
//						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			}
		}
	
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_DoubleNexus){
			
			if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) ==1){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
//			if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1
//					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode) == 0
//					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop) >= 1
//					&& MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) < 3){
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,
//						BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Shuttle){
			enemy_shuttle = true;
		}
		
		//protossException_Dark start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Dark){
			enemy_dark_templar = true;
			//if(InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, MyBotModule.Broodwar.self())>4){
			need_vessel = true;
			max_vessel = 1;
			//}
		}
		//protossException_Dark end
		
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Arbiter){
			enemy_arbiter = true;
			//if(InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, MyBotModule.Broodwar.self())>4){
			need_vessel = true;
			max_vessel = 2;
			//}
		}
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Scout){
			enemy_scout = true;
		}
	}
	
	public void RespondVsTerran() {
		
		max_wraith = 5;
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.terranBasic_Bionic){
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
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.terranException_WraithCloak){
			enemy_wraith = true;
			need_vessel = true;
			max_vessel = 2;
		}
	}
		
	public void RespondVsZerg() {	
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_PrepareLurker
			||StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_FastLurker){
			enemy_lurker = true;
			need_vessel = true;
			max_vessel = 1;
		}
		
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_HighTech){
			need_vessel = true;
			max_vessel = 4;
		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_MutalMany){
			need_valkyrie = true;
			max_valkyrie = 5;
			max_turret_to_mutal = 4;
		}else{
			need_valkyrie = false;
			max_valkyrie = 0;
			if(AnalyzeStrategy.Instance().mutalStrategy){
				max_turret_to_mutal = 4;
			}
		}
	
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_OnLyLing){
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Vulture,InformationManager.Instance().selfPlayer) < 5	){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) < 1){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				}
			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_Mutal){
			enemy_mutal = true;
		}
		
		
	}
		
		
	public void RespondExecute() {	
		
//		if(prepareDark == true){
//			if(!chk_engineering_bay){
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay)
//						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
//								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					//}
//				}
//			}
//		}
			
		//enemy_dark_templar & enemy_lurker & enemy_wraith 클로킹 유닛에 대한 대비
		//if(enemy_dark_templar || enemy_wraith || enemy_lurker || enemy_arbiter || enemy_mutal || prepareDark){
		if(enemy_dark_templar || enemy_wraith || enemy_lurker || enemy_arbiter || prepareDark){
	           if(need_vessel_time ==0){
	                need_vessel_time = MyBotModule.Broodwar.getFrameCount();
	            }
		
			if(!chk_comsat_station && StrategyManager.Instance().getFacUnits() >= 32){
				//컴셋이 없다면
				if(!chk_academy){
					//아카데미가 없다면
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0){
						//지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Academy,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}else{
					if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) > 0){ 
						//아카데미가 완성되었고
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0){
							//빌드큐에 컴셋이 없는데, 아카데미가 완성되었다면빌드큐에 컴셋 입력
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
							}
						}
					}
				}
			}
			
			if(!chk_engineering_bay  && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 2){
				//System.out.println("엔베없다");
				//엔베가 없다면
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
					//지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
					System.out.println("engineering because of cloaked");
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}else{
				//엔베가 있다면
				//System.out.println("엔베있다!!!!!!!!!!!!!!!!!!");
				if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0) {
					// 엔베가 완성이 되었다면
					BaseLocation tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
					Chokepoint tempChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());
					Chokepoint temp2ChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
					
					mainBaseTurret = false;
					firstChokeTurret = false;
					Boolean secondChokeTurret = false;
					Boolean firstChokeHalfTurret = false;
					
					// first choke point
					if (tempChokePoint != null) {
						// myRegion = BWTA.getRegion(tempChokePoint.getPoint());
						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempChokePoint.getCenter(),100);

						for(Unit turret : turretInRegion){
							if (turret.getType() == UnitType.Terran_Missile_Turret) {
								firstChokeTurret = true;
							}
						}
						if (!firstChokeTurret) {
							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 100) 
							+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().toTilePosition(), 100) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(
										UnitType.Terran_Missile_Turret,
										tempChokePoint.getCenter().toTilePosition(), true);
							}
						}
					}		
					
					if (tempBaseLocation != null) {
						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempBaseLocation.getRegion().getCenter(),250);

						for(Unit turret : turretInRegion){
							if (turret.getType() == UnitType.Terran_Missile_Turret) {
								mainBaseTurret = true;
							}
						}
						if (!mainBaseTurret) {
							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getRegion().getCenter().toTilePosition(), 300) 
							+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, tempBaseLocation.getRegion().getCenter().toTilePosition(), 300) < 1){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(
										UnitType.Terran_Missile_Turret,
										tempBaseLocation.getRegion().getCenter().toTilePosition(), true);
							}
						}
					}
					
					
					if (tempBaseLocation != null) { 
						
						Position firstChokeHalf = new Position((tempBaseLocation.getRegion().getCenter().getX() + tempChokePoint.getX()*2)/3 , (tempBaseLocation.getRegion().getCenter().getY() + tempChokePoint.getY()*2)/3);
						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(firstChokeHalf,100);

						for(Unit turret : turretInRegion){
							if (turret.getType() == UnitType.Terran_Missile_Turret) {
								firstChokeHalfTurret = true;
							}
						}
						if (!firstChokeHalfTurret) {
							if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret,firstChokeHalf.toTilePosition(), 100) 
							+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, firstChokeHalf.toTilePosition(), 100) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(
										UnitType.Terran_Missile_Turret,
										firstChokeHalf.toTilePosition(), true);
							}
						}
					}
					
					
					//BaseLocation FirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
					
					if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) > 1){
						
						if (temp2ChokePoint != null) {
							// myRegion = BWTA.getRegion(tempChokePoint.getPoint());
							List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(temp2ChokePoint.getCenter(),100);

							for(Unit turret : turretInRegion){
								if (turret.getType() == UnitType.Terran_Missile_Turret) {
									secondChokeTurret = true;
								}
							}
							if (!secondChokeTurret) {

								if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, temp2ChokePoint.getCenter().toTilePosition(), 100) 
								+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, temp2ChokePoint.getCenter().toTilePosition(), 100) == 0){

									BuildManager.Instance().buildQueue.queueAsHighestPriority(
											UnitType.Terran_Missile_Turret,
											temp2ChokePoint.getCenter().toTilePosition(), true);
									System.out.println("build turret to second choke ===>>>>  (" +temp2ChokePoint.getCenter().toTilePosition().getX()+"," +temp2ChokePoint.getCenter().toTilePosition().getY()+")");
								}
							}
						}
						
						//타지역 멀티
						List<BaseLocation> tempBaseLocationList = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
						//List tempBaseLocationList = InformationManager.Instance().ge(InformationManager.Instance().selfPlayer);
						for(BaseLocation baseLocation : tempBaseLocationList){
							boolean location_turret = false;
							//tempBaseLocation = (BaseLocation)tempBaseLocationList.get(a);
							//System.out.println("baseLocation ==>>> (" + baseLocation.getPoint().toTilePosition().getX() +","+baseLocation.getPoint().toTilePosition().getY()+")" );
							if (baseLocation != null) {
								List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(baseLocation.getRegion().getCenter(),250);
						              //List<Unit> turretInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().selfPlayer);
					          	for(Unit turret : turretInRegion){
									if (turret.getType() == UnitType.Terran_Missile_Turret) {
										location_turret = true;
									}
								}
					          	
					          	if(!location_turret){
						              if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, baseLocation.getRegion().getCenter().toTilePosition(), 250) 
									+ ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, baseLocation.getRegion().getCenter().toTilePosition(), 250) == 0){
										BuildManager.Instance().buildQueue.queueAsHighestPriority(
												UnitType.Terran_Missile_Turret,
												baseLocation.getRegion().getCenter().toTilePosition(), true);
									}
					          	}
							}
						}
					}
				}
			}
		}
		
		

		
//		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Reaver){
//			//리버 대비
//			if(!chk_engineering_bay){
//				//엔지니어링 베이가 없다면
//				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
//					//빌드큐를 체크하고
//					/*if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Engineering_Bay.mineralPrice()){ 
//						//미네랄이 충분하다면
//						 * 엔베는 미네랄 체크 안하고(단순 미네랄 유닛이고 후딱 지어야 하니까) 
//						 */
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
//								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//					//}
//				}
//			}else{
//				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0){
//					
//					//System.out.println("리버 전략 대응");
//					//Position tempBaseLocation = null;
//					boolean mainBaseTurret = false;
//					
//					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//						Position tempBaseLocation = null;
//						if(unit.getType() == UnitType.Terran_Command_Center){
//							//System.out.println("방어해야할 커맨드가 있따.");
//							tempBaseLocation = unit.getPosition();
//						}
//						int mainBaseTurret_cnt = 0;
//						if (tempBaseLocation != null) {
//							List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempBaseLocation, 300);
//					              
//			     			 for(int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt ++){
//				     			 if(turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)){
//				     				 //System.out.println("Turret Exists at Main Base Location !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//				     				
//				     				mainBaseTurret_cnt ++ ; 
//				     				//break;
//				     			 }
//			     			 }
//			     			 if(mainBaseTurret_cnt >= 3){
//			     				 //System.out.println("방어 터렛 3기 이상");
//			     				 mainBaseTurret = true;
//			     				//StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
//			     			 }else{
//			     				//System.out.println("방어 터렛 3기 미만");
//		     					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPoint().toTilePosition())< 1
//		     							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPoint().toTilePosition())==0){
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
//											tempBaseLocation.getPoint().toTilePosition(), true);
//			     				 }
//			     			 }
//						}
//					}
//				}
//			}
//		}
		
		if(enemy_scout || enemy_shuttle){
			if(!chk_armory){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}else{
				if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
						InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 2)
						|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 2){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
		
		if(enemy_arbiter){
			if(!chk_armory){
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null) == 0){
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}else{
				if((InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) <
						InformationManager.Instance().getNumUnits(UnitType.Protoss_Arbiter,InformationManager.Instance().enemyPlayer) * 4)
						|| InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath,InformationManager.Instance().selfPlayer) < 4){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) < 1){
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Goliath.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Goliath.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
		
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.terranBasic_BattleCruiser){
			need_battlecruiser = true;
			max_battlecruiser = 8;
		}else if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.AttackIsland){
			need_battlecruiser = true;
			max_battlecruiser = 8;
		}else{
			need_battlecruiser = false;
			max_battlecruiser = 0;
		}

		if(max_turret_to_mutal != 0){
			
			if(!chk_engineering_bay){
				//System.out.println("엔베없다");
				//엔베가 없다면
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0){
					//지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}else{
				//엔베가 있다면
				//System.out.println("엔베있다!!!!!!!!!!!!!!!!!!");
				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) > 0){
					//엔베가 완성이 되었다면
					BaseLocation tempBaseLocation =InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
	            	Chokepoint tempChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());;
	            	//Position tempPosition;
	            	
	            	//BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			              bwta.Region myRegion = null; 
			              
			           boolean mainBaseTurret = false;
			           boolean	firstChokeTurret = false;
			           int build_turret_cnt = 0;
			        //first choke point
			           /*if (tempChokePoint != null) {
						//myRegion = BWTA.getRegion(tempChokePoint.getPoint());
						List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempChokePoint.getCenter(), 300);
						
						 for(int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt ++){
			     			 if(turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)){
			     				build_turret_cnt++;
			     			 }
		     			 }
						 //first choke 는 1~3개
						 if(build_turret_cnt < max_turret_to_mutal -3){
		     				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().getPoint().toTilePosition())< 1
		     						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().getPoint().toTilePosition()) == 0){
		     					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().getPoint().toTilePosition(), true);
		     				}
		     				
		     			}
					}*/
			          /*tempChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
			           if (tempChokePoint != null) {
							//myRegion = BWTA.getRegion(tempChokePoint.getPoint());
							List<Unit> turretInRegion = MyBotModule.Broodwar.getUnitsInRadius(tempChokePoint.getCenter(), 300);
							
							 for(int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt ++){
				     			 if(turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)){
				     				firstChokeTurret = true;
				     			 }
			     			 }
							 if(!firstChokeTurret){
			     				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().getPoint().toTilePosition())< 1
			     						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, tempChokePoint.getCenter().getPoint().toTilePosition()) == 0){
			     					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
			     							tempChokePoint.getCenter().getPoint().toTilePosition(), true);
			     				}
			     				
			     			}
						}*/

			           //20170812_본진 앞마당 빼고 전체 통합
			           /*
			           //본진
					if (tempBaseLocation != null) {
						myRegion = tempBaseLocation.getRegion();
				              List<Unit> turretInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().selfPlayer);

				             build_turret_cnt = 0; 
		     			 for(int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt ++){
			     			 if(turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)){
			     				build_turret_cnt++;
			     			 }
		     			 }
		     			 if(build_turret_cnt < max_turret_to_mutal)
		     			 {
		     				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPoint().toTilePosition())< 1
		     						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPoint().toTilePosition()) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
										tempBaseLocation.getPoint().toTilePosition(), true);
		     				}
		     			 }
					}
					
					//앞마당
					tempBaseLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
					if (tempBaseLocation != null) {
						myRegion = tempBaseLocation.getRegion();
				              List<Unit> turretInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().selfPlayer);

				             build_turret_cnt = 0; 
		     			 for(int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt ++){
			     			 if(turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)){
			     				build_turret_cnt++;
			     			 }
		     			 }
		     			 //앞마당은 2~4개
		     			 if(build_turret_cnt < max_turret_to_mutal-3)
		     			 {
		     				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPoint().toTilePosition())< 1
		     						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, tempBaseLocation.getPoint().toTilePosition()) == 0){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
										tempBaseLocation.getPoint().toTilePosition(), true);
		     				}
		     			 }
					}
					*/
					
					//타지역 멀티
					List<BaseLocation> tempBaseLocationList = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
					//List tempBaseLocationList = InformationManager.Instance().ge(InformationManager.Instance().selfPlayer);
					for(BaseLocation baseLocation : tempBaseLocationList){
						//tempBaseLocation = (BaseLocation)tempBaseLocationList.get(a);
						//System.out.println("baseLocation ==>>> (" + baseLocation.getPoint().toTilePosition().getX() +","+baseLocation.getPoint().toTilePosition().getY()+")" );
						if (baseLocation != null) {
							myRegion = baseLocation.getRegion();
					              List<Unit> turretInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().selfPlayer);

					             build_turret_cnt = 0; 
			     			 for(int turret_cnt = 0; turret_cnt < turretInRegion.size(); turret_cnt ++){
				     			 if(turretInRegion.get(turret_cnt).getType().equals(UnitType.Terran_Missile_Turret)){
				     				build_turret_cnt++;
				     			 }
			     			 }
			     			 //System.out.println("max_turret_to_mutal ==>>>>  " + (max_turret_to_mutal-1));
			     			//System.out.println("build_turret_cnt ==>>>>  " + build_turret_cnt);
			     			 //터렛은 3~5개
			     			 if(build_turret_cnt < max_turret_to_mutal-1)
			     			 {
			     				 /*System.out.println("max_turret_to_mutal ==>>>>  " + (max_turret_to_mutal-1));
			     				System.out.println("build_turret_cnt ==>>>>  " + build_turret_cnt);*/
			     				//if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret, baseLocation.getPoint().toTilePosition())< 1
			     				if(BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, baseLocation.getPoint().toTilePosition(),250)< 1
			     						&& ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, baseLocation.getPoint().toTilePosition(),250) == 0){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
											baseLocation.getPoint().toTilePosition(), true);
									//System.out.println("지으라고 한곳 ==>>>> " + );
			     				}
			     			 }
						}
					}
				}
			}
		}
	}
}