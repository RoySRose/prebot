package pre;

import java.util.List;
import java.util.Set;

import bwapi.Color;
import bwapi.Player;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import pre.main.MyBotModule;
import pre.manager.BuildManager;
import pre.manager.InformationManager;
import pre.manager.StrategyManager;
import pre.manager.StrategyManager.Strategys;
import pre.manager.StrategyManager.StrategysException;
import pre.util.MicroUtils;

/// 봇 프로그램 설정
public class RespondToStrategy {
	
	//클로킹 유닛에 대한 대처
	public boolean enemy_dark_templar = false;
	public boolean enemy_lurker = false;
	public boolean enemy_wraith = false;
	
	public boolean enemy_guardian = false;
	
	public boolean enemy_shuttle = false;
	
	//초반 터렛 건설에 대한 체크
	private int chk_turret = 0;
	
	//유닛체크
	public boolean chk_scv = false;
	public boolean chk_marine = false;
	public boolean chk_goliath = false;
	public boolean chk_vulture = false;
	public boolean chk_siege_tank = false;
	public boolean chk_vessel = false;
	public boolean chk_wraith = false;
	public boolean chk_valkyrie = false;
	
	//건물체크
	public boolean chk_refinery = false;
	public boolean chk_barrack = false;
	public boolean chk_engineering_bay = false;
	public boolean chk_missile_turret = false;
	public boolean chk_academy = false;
	public boolean chk_factory = false;
	public boolean chk_machine_shop = false;
	public boolean chk_armory = false;
	public boolean chk_starport = false;
	public boolean chk_control_tower = false;
	public boolean chk_comsat_station = false;
	public boolean chk_science_facility = false;
	 
	private static RespondToStrategy instance = new RespondToStrategy();
	
	public static RespondToStrategy instance() {
		return instance;
	}
	

	public void update() {
		//System.out.println("Respond Strategy Manager On Update!!!!!!!!!!!!!!! ");
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {

			if (unit.getType() == UnitType.Terran_SCV) {
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
		
		//protossException_Dark start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Dark){
			enemy_dark_templar = true;
		}
		//protossException_Dark end
		
		//terranException_Wraith start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.terranException_Wraith){
			enemy_wraith = true;
		}
		//terranException_Wraith end
		
		//zergException_PrepareLurker ||  zergException_FastLurker start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_PrepareLurker
			||StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_FastLurker){
			enemy_lurker = true;
		}
		//zergException_PrepareLurker ||  zergException_FastLurker end
		
		
		
		
		
		//enemy_dark_templar & enemy_lurker & enemy_wraith 클로킹 유닛에 대한 대비
		if(enemy_dark_templar || enemy_wraith || enemy_lurker){
			//System.out.println("클로킹 유닛 대비================");
			//이니셜에서 엔지니어링 베이까지는 짓고 갈거이므로.
			//익셉션 다크는 패스트 다크에 대한 대비이다. 고로 아직 사베에 대한 컨트롤은 없다.
			//마인업 하고
			//컴셋까지만 지어준다.
			
			/*마인은 일단 패스
			 * if(!InformationManager.Instance().selfPlayer.hasResearched(TechType.Spider_Mines) && BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines) < 1){
				//마인개발이 안되어있고, 빌드큐에도 없다면
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Machine_Shop, InformationManager.Instance().selfPlayer) < 1){
						//&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop) < 1){
					//지어졌거나 지어지고 있는 머신샵이 없다면
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop) < 1){
						//빌드큐에 머신샵이 없다면 바로 만들도록
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}else{
					//머신샵이 있거나 만들어지고 있다면
					if(BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines) < 1){
						int chk_cnt = 0;
						for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
							if (unit.getType() == UnitType.Terran_Machine_Shop && unit.canResearch() && chk_cnt == 0){
								//머신샵이 비어있는게 있다면
								BuildManager.Instance().buildQueue.queueAsHighestPriority(TechType.Spider_Mines, true);
								chk_cnt = 1;
								break;
								
							}
						}
						if(chk_cnt == 0){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(TechType.Spider_Mines, false);
						}
					}
				}
			}*/
			

			
			if(!chk_comsat_station){
				//컴셋이 없다면
				if(!chk_academy){
					//아카데미가 없다면
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1){
						//지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Academy,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}else{
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1){
						//빌드큐에 컴셋이 없는데, 아카데미가 완성되었다면빌드큐에 컴셋 입력
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
			
			if(!chk_engineering_bay){
				//System.out.println("엔베없다");
				//엔베가 없다면
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1){
					//지어졌거나 건설중인게 없는데 빌드큐에도 없다면 아카데미를 빌드큐에 입력
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					System.out.println("엔베 빌드큐 입력");
				}
			}else{
				//엔베가 있다면
				//System.out.println("엔베있다!!!!!!!!!!!!!!!!!!");
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret) < 1){
					if(chk_turret == 0){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.FirstChokePoint, true);
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						
						chk_turret ++;
					}
					
					if(chk_turret == 1){
						BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
						List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
						for (BaseLocation occupied : occupiedBases) {
							if (occupied == firstExpansion) {
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.SecondChokePoint, true);
								//break;
								chk_turret ++;
							}
						}
					}
					
					//Chokepoint choke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
					
					/*else if(chk_turret == 1){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.FirstChokePoint, true);
						chk_turret ++;
					}else if(chk_turret == 2){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						//InitialBuild.Instance().queueBuildSeed(false, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
						chk_turret ++;
					}else if(chk_turret == 3){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, true);
						//InitialBuild.Instance().queueBuildSeed(false, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
						chk_turret ++;
					}*/
					/*else if(chk_turret == 4){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, true);
						//InitialBuild.Instance().queueBuildSeed(false, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard);
						chk_turret ++;
					}else if(chk_turret == 5){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Missile_Turret,
								BuildOrderItem.SeedPositionStrategy.FirstChokePoint, true);
						//InitialBuild.Instance().queueBuildSeed(false, UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.FirstChokePoint);
						chk_turret ++;
					}*/
				}
			}
			
			/*베슬은 일단 주석처리. 특정조건 정해지면 넣기.
			//베슬을 위한 로직
			if(chk_factory){
				//일단 팩토리가 있어야하고
				//if(){ 이부분은 특정조건이 들어가야 한다.
				if(!chk_starport){
					//스타포트가 없다면
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) < 1){
						//빌드큐를 체크하고 없다면
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Starport.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Starport.gasPrice()){
							//가스와 미네랄이 충분하다면
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Starport,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
					
				}else{
					//스타포트가 있다면
					if(!chk_control_tower){
						//컨트롤 타워가 없다면
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) < 1){
							//빌드큐를 체크하고
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Control_Tower.mineralPrice() 
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Control_Tower.gasPrice()){
								//가스와 미네랄이 충분하다면
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower,
										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
					}else{
						//컨트롤 타워가 있다면
						if(!chk_science_facility){
							//사이언스 퍼실리티가 없다면
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) < 1){
								//빌드큐를 체크하고
								if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Science_Facility.mineralPrice() 
										&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Science_Facility.gasPrice()){
									//가스와 미네랄이 충분하다면
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Science_Facility,
											BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
								}
							}
						}else{
							//사이언스 퍼실리티가 있다면
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel) < 1){
								//빌드큐를 체크하고
								if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Science_Vessel.mineralPrice() 
										&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Science_Vessel.gasPrice()){
									//가스와 미네랄이 충분하다면
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Science_Vessel,
											BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
								}
							}
						}
					}
				}
			}*/
		}
		//enemy_dark_templar end
		
		/*
		 * 일단 가디언은 보류
		//zergException_Guardian start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_Guardian){
			//가디언 대비
			enemy_dark_templar = true;
		}
		//zergException_Guardian end
		 * 
		 */
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Shuttle){
			enemy_shuttle = true;
		}
		
		if(enemy_shuttle){
			//셔틀 대비
			if(!chk_engineering_bay){
				//엔지니어링 베이가 없다면
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1){
					//빌드큐를 체크하고
					/*if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Engineering_Bay.mineralPrice()){ 
						//미네랄이 충분하다면
						 * 엔베는 미네랄 체크 안하고(단순 미네랄 유닛이고 후딱 지어야 하니까) 
						 */
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					//}
				}
			}
			if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ShuttleMix){
				//셔틀대비 중에. 실제 셔틀을 보면. 골리앗 추가
				if(!chk_armory){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1){
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
		
		if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_MutalMany){
			if(!chk_armory){
				//당연히 아머리는 있겠지만 어쨌든 없다면
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1){
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}
			//아머리와 스타포트는 별개로 체크하고 동시에 건설할수 있다.
			if(!chk_starport){
				//스타포트가 없다면
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) < 1){
					//빌드큐를 체크하고 없다면
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Starport.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Starport.gasPrice()){
						//가스와 미네랄이 충분하다면
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Starport,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
				
			}else{
				//스타포트가 있다면
				if(!chk_control_tower){
					//컨트롤 타워가 없다면
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) < 1){
						//빌드큐를 체크하고
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Control_Tower.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Control_Tower.gasPrice()){
							//가스와 미네랄이 충분하다면
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}else{
					//컨트롤 타워가 있다면
					if(!chk_valkyrie){
						//발키리가 없다면
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Valkyrie) < 1){
							//빌드큐를 체크하고
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Valkyrie.mineralPrice() 
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Valkyrie.gasPrice()){
								//가스와 미네랄이 충분하다면
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Valkyrie,
										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
					}else{
						//발키리가 있다면
						if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Valkyrie) < 6){
							//발키리 수를 체크해서 6기가 안된다면(일단 6기)
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Valkyrie.mineralPrice() 
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Valkyrie.gasPrice()){
								//가스와 미네랄이 충분하다면
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Valkyrie,
										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
					}
				}
			}
		}
	}
}