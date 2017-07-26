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

/// �? ?��로그?�� ?��?��
public class RespondToStrategy {
	
	//?��로킹 ?��?��?�� ???�� ??�?
	public boolean enemy_dark_templar = false;
	public boolean enemy_lurker = false;
	public boolean enemy_wraith = false;
	
	public boolean enemy_guardian = false;
	
	public boolean enemy_shuttle = false;
	
	//초반 ?��?�� 건설?�� ???�� 체크
	private int chk_turret = 0;
	
	//?��?��체크
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
		
		
		
		
		
		//enemy_dark_templar & enemy_lurker & enemy_wraith ?��로킹 ?��?��?�� ???�� ??�?
		if(enemy_dark_templar || enemy_wraith || enemy_lurker){
			//System.out.println("?��로킹 ?��?�� ??�?================");
			//?��?��?��?��?�� ?���??��?���? 베이까�??�� 짓고 갈거?���?�?.
			//?��?��?�� ?��?��?�� ?��?��?�� ?��?��?�� ???�� ??비이?��. 고로 ?���? ?��베에 ???�� 컨트롤�? ?��?��.
			//마인?�� ?���?
			//컴셋까�?�? �??���??��.
			
			/*마인?? ?��?�� ?��?��
			 * if(!InformationManager.Instance().selfPlayer.hasResearched(TechType.Spider_Mines) && BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines) < 1){
				//마인개발?�� ?��?��?��?���?, 빌드?��?��?�� ?��?���?
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Machine_Shop, InformationManager.Instance().selfPlayer) < 1){
						//&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop) < 1){
					//�??��졌거?�� �??���?�? ?��?�� 머신?��?�� ?��?���?
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop) < 1){
						//빌드?��?�� 머신?��?�� ?��?���? 바로 만들?���?
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Machine_Shop,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}else{
					//머신?��?�� ?��거나 만들?���?�? ?��?���?
					if(BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines) < 1){
						int chk_cnt = 0;
						for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
							if (unit.getType() == UnitType.Terran_Machine_Shop && unit.canResearch() && chk_cnt == 0){
								//머신?��?�� 비어?��?���? ?��?���?
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
				//컴셋?�� ?��?���?
				if(!chk_academy){
					//?��카데미�? ?��?���?
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) < 1){
						//�??��졌거?�� 건설중인�? ?��?��?�� 빌드?��?��?�� ?��?���? ?��카데미�?? 빌드?��?�� ?��?��
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Academy,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}else{
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) < 1){
						//빌드?��?�� 컴셋?�� ?��?��?��, ?��카데미�? ?��?��?��?��?��면빌?��?��?�� 컴셋 ?��?��
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Comsat_Station.mineralPrice()
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Comsat_Station.gasPrice()){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
			
			if(!chk_engineering_bay){
				//System.out.println("?��베없?��");
				//?��베�? ?��?���?
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1){
					//�??��졌거?�� 건설중인�? ?��?��?�� 빌드?��?��?�� ?��?���? ?��카데미�?? 빌드?��?�� ?��?��
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
							BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					System.out.println("?���? 빌드?�� ?��?��");
				}
			}else{
				//?��베�? ?��?���?
				//System.out.println("?��베있?��!!!!!!!!!!!!!!!!!!");
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
			
			/*베슬?? ?��?�� 주석처리. ?��?��조건 ?��?���?�? ?���?.
			//베슬?�� ?��?�� 로직
			if(chk_factory){
				//?��?�� ?��?��리�? ?��?��?��?���?
				//if(){ ?���?분�? ?��?��조건?�� ?��?���??�� ?��?��.
				if(!chk_starport){
					//?��???��?���? ?��?���?
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) < 1){
						//빌드?���? 체크?���? ?��?���?
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Starport.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Starport.gasPrice()){
							//�??��?? 미네?��?�� 충분?��?���?
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Starport,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
					
				}else{
					//?��???��?���? ?��?���?
					if(!chk_control_tower){
						//컨트�? ???���? ?��?���?
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) < 1){
							//빌드?���? 체크?���?
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Control_Tower.mineralPrice() 
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Control_Tower.gasPrice()){
								//�??��?? 미네?��?�� 충분?��?���?
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower,
										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
					}else{
						//컨트�? ???���? ?��?���?
						if(!chk_science_facility){
							//?��?��?��?�� ?��?��리티�? ?��?���?
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) < 1){
								//빌드?���? 체크?���?
								if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Science_Facility.mineralPrice() 
										&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Science_Facility.gasPrice()){
									//�??��?? 미네?��?�� 충분?��?���?
									BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Science_Facility,
											BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
								}
							}
						}else{
							//?��?��?��?�� ?��?��리티�? ?��?���?
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel) < 1){
								//빌드?���? 체크?���?
								if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Science_Vessel.mineralPrice() 
										&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Science_Vessel.gasPrice()){
									//�??��?? 미네?��?�� 충분?��?���?
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
		 * ?��?�� �??��?��?? 보류
		//zergException_Guardian start
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.zergException_Guardian){
			//�??��?�� ??�?
			enemy_dark_templar = true;
		}
		//zergException_Guardian end
		 * 
		 */
		if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_Shuttle){
			enemy_shuttle = true;
		}
		
		if(enemy_shuttle){
			//?��?? ??�?
			if(!chk_engineering_bay){
				//?���??��?���? 베이�? ?��?���?
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) < 1){
					//빌드?���? 체크?���?
					/*if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Engineering_Bay.mineralPrice()){ 
						//미네?��?�� 충분?��?���?
						 * ?��베는 미네?�� 체크 ?��?���?(?��?�� 미네?�� ?��?��?���? ?��?�� �??��?�� ?��?���?) 
						 */
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Engineering_Bay,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					//}
				}
			}
			if(StrategyManager.Instance().getCurrentStrategyException() == StrategyManager.StrategysException.protossException_ShuttleMix){
				//?��????�? 중에. ?��?�� ?��???�� 보면. 골리?�� 추�?
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
				//?��?��?�� ?��머리?�� ?��겠�?�? ?��쨌든 ?��?���?
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) < 1){
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Armory.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Armory.gasPrice()){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Armory,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
			}
			//?��머리?? ?��???��?��?�� 별개�? 체크?���? ?��?��?�� 건설?��?�� ?��?��.
			if(!chk_starport){
				//?��???��?���? ?��?���?
				if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) < 1){
					//빌드?���? 체크?���? ?��?���?
					if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Starport.mineralPrice() 
							&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Starport.gasPrice()){
						//�??��?? 미네?��?�� 충분?��?���?
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Starport,
								BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					}
				}
				
			}else{
				//?��???��?���? ?��?���?
				if(!chk_control_tower){
					//컨트�? ???���? ?��?���?
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) < 1){
						//빌드?���? 체크?���?
						if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Control_Tower.mineralPrice() 
								&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Control_Tower.gasPrice()){
							//�??��?? 미네?��?�� 충분?��?���?
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Control_Tower,
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}else{
					//컨트�? ???���? ?��?���?
					if(!chk_valkyrie){
						//발키리�? ?��?���?
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Valkyrie) < 1){
							//빌드?���? 체크?���?
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Valkyrie.mineralPrice() 
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Valkyrie.gasPrice()){
								//�??��?? 미네?��?�� 충분?��?���?
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Valkyrie,
										BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
							}
						}
					}else{
						//발키리�? ?��?���?
						if(MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Valkyrie) < 6){
							//발키�? ?���? 체크?��?�� 6기�? ?��?��?���?(?��?�� 6�?)
							if(MyBotModule.Broodwar.self().minerals() >= UnitType.Terran_Valkyrie.mineralPrice() 
									&& MyBotModule.Broodwar.self().gas() >= UnitType.Terran_Valkyrie.gasPrice()){
								//�??��?? 미네?��?�� 충분?��?���?
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