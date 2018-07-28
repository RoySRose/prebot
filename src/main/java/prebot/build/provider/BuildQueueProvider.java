package prebot.build.provider;

import java.util.ArrayList;
import java.util.List;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.items.building.BuilderAcademy;
import prebot.build.provider.items.building.BuilderArmory;
import prebot.build.provider.items.building.BuilderBarracks;
import prebot.build.provider.items.building.BuilderBunker;
import prebot.build.provider.items.building.BuilderCommandCenter;
import prebot.build.provider.items.building.BuilderComsatStation;
import prebot.build.provider.items.building.BuilderControlTower;
import prebot.build.provider.items.building.BuilderEngineeringBay;
import prebot.build.provider.items.building.BuilderFactory;
import prebot.build.provider.items.building.BuilderMachineShop;
import prebot.build.provider.items.building.BuilderMissileTurret;
import prebot.build.provider.items.building.BuilderRefinery;
import prebot.build.provider.items.building.BuilderScienceFacility;
import prebot.build.provider.items.building.BuilderStarport;
import prebot.build.provider.items.building.BuilderSupplyDepot;
import prebot.build.provider.items.tech.BuilderCharonBoosters;
import prebot.build.provider.items.tech.BuilderCloakingField;
import prebot.build.provider.items.tech.BuilderIonThrusters;
import prebot.build.provider.items.tech.BuilderSpiderMines;
import prebot.build.provider.items.tech.BuilderTankSiegeMode;
import prebot.build.provider.items.unit.BuilderGoliath;
import prebot.build.provider.items.unit.BuilderMarine;
import prebot.build.provider.items.unit.BuilderSCV;
import prebot.build.provider.items.unit.BuilderSiegeTank;
import prebot.build.provider.items.unit.BuilderVulture;
import prebot.build.provider.items.unit.BuilderWraith;
import prebot.build.provider.items.upgrade.BuilderApolloReactor;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.common.main.Prebot;

public class BuildQueueProvider extends GameManager {

	//process 를 update 로 바꾸고 GameManager Extend
    private static BuildQueueProvider instance = new BuildQueueProvider();
    /// static singleton 객체를 리턴합니다
    public static BuildQueueProvider Instance() {
        return instance;
    }
    
    /*private boolean isInitialBuildOrderFinished;

	public boolean isInitialBuildOrderFinished() {
		return isInitialBuildOrderFinished;
	}*/

    private boolean sysout = false;
    public boolean respondSet = false;

    List<BuildableItem> buildableList = new ArrayList<>();
    
    
    /*upgrade*/
    BuildableItem apolloReactor; 
    BuildableItem caduceusReactor;
    BuildableItem charonBoosters;
    BuildableItem colossusReactor;
    BuildableItem ionThrusters;
    BuildableItem moebiusReactor;
    BuildableItem ocularImplants;
    BuildableItem terranInfantryArmor;
    BuildableItem terranInfantryWeapons;
    BuildableItem terranShipPlating;
    BuildableItem terranShipWeapons;
    BuildableItem terranVehiclePlating;
    BuildableItem terranVehicleWeapons;
    BuildableItem titanReactor;
    BuildableItem u238Shells;

    /*tech*/
    BuildableItem cloakingField;
    BuildableItem empShockwave;
    BuildableItem irradiate;
    BuildableItem lockdown;
    BuildableItem nuclearStrike;
    BuildableItem opticalFlare;
    BuildableItem personnelCloaking;
    BuildableItem restoration;
    BuildableItem spiderMines;
    BuildableItem stimPacks;
    BuildableItem tankSiegeMode;
    BuildableItem yamatoGun;

    
    
    //BuildCountChecker buildCountChecker;
    ResearchSelector researchSelector;

    
    UpgradeSelector upgradeSelector;
//    BarrackUnitSelector barrackUnitSelector;
    FactoryUnitSelector factoryUnitSelector;
//    StarportUnitSelector starportUnitSelector;

    public BuildQueueProvider(){

    	researchSelector = new ResearchSelector();
        //researchSelector2 = new ResearchSelector2();
        upgradeSelector = new UpgradeSelector();
//        barrackUnitSelector = new BarrackUnitSelector();
        factoryUnitSelector = new FactoryUnitSelector();
//        starportUnitSelector = new StarportUnitSelector();

        //Unit
        //
        
        /*unit*/
        /*
        buildableList.add(new BuilderFirebat              (new MetaType(UnitType.Terran_Firebat), barrackUnitSelector));
        buildableList.add(new BuilderGhost                (new MetaType(UnitType.Terran_Ghost), barrackUnitSelector));
        buildableList.add(new BuilderMedic                (new MetaType(UnitType.Terran_Medic), barrackUnitSelector));*/
        
        buildableList.add(new BuilderGoliath              (new MetaType(UnitType.Terran_Goliath), factoryUnitSelector));
        buildableList.add(new BuilderVulture              (new MetaType(UnitType.Terran_Vulture), factoryUnitSelector));
        buildableList.add(new BuilderSiegeTank				(new MetaType(UnitType.Terran_Siege_Tank_Tank_Mode), factoryUnitSelector));
        buildableList.add(new BuilderWraith               (new MetaType(UnitType.Terran_Wraith)));
        buildableList.add(new BuilderMarine               (new MetaType(UnitType.Terran_Marine)));
        
        /*
        buildableList.add(new BuilderBattlecruiser        (new MetaType(UnitType.Terran_Battlecruiser),starportUnitSelector));
        buildableList.add(new BuilderDropship             (new MetaType(UnitType.Terran_Dropship),starportUnitSelector));
        buildableList.add(new BuilderScienceVessel        (new MetaType(UnitType.Terran_Science_Vessel), starportUnitSelector));
        buildableList.add(new BuilderValkyrie             (new MetaType(UnitType.Terran_Valkyrie), starportUnitSelector));
        buildableList.add(new BuilderWraith               (new MetaType(UnitType.Terran_Wraith), starportUnitSelector));*/
        
        
        /*SCV*/
        buildableList.add(new BuilderSCV              (new MetaType(UnitType.Terran_SCV)));

        /*building*/
        buildableList.add(new BuilderAcademy              (new MetaType(UnitType.Terran_Academy)));
        buildableList.add(new BuilderArmory               (new MetaType(UnitType.Terran_Armory)));
        buildableList.add(new BuilderBarracks             (new MetaType(UnitType.Terran_Barracks)));
        buildableList.add(new BuilderBunker               (new MetaType(UnitType.Terran_Bunker)));
        buildableList.add(new BuilderCommandCenter        (new MetaType(UnitType.Terran_Command_Center)));
        buildableList.add(new BuilderComsatStation        (new MetaType(UnitType.Terran_Comsat_Station)));
        buildableList.add(new BuilderControlTower         (new MetaType(UnitType.Terran_Control_Tower)));
        //buildableList.add(new BuilderCovertOps            (new MetaType(UnitType.Terran_Covert_Ops)));
        buildableList.add(new BuilderEngineeringBay       (new MetaType(UnitType.Terran_Engineering_Bay)));
        buildableList.add(new BuilderFactory              (new MetaType(UnitType.Terran_Factory)));
        buildableList.add(new BuilderMachineShop          (new MetaType(UnitType.Terran_Machine_Shop)));
        buildableList.add(new BuilderMissileTurret        (new MetaType(UnitType.Terran_Missile_Turret)));
        //buildableList.add(new BuilderNuclearSilo          (new MetaType(UnitType.Terran_Nuclear_Silo)));
        //buildableList.add(new BuilderPhysicsLab           (new MetaType(UnitType.Terran_Physics_Lab)));
        buildableList.add(new BuilderRefinery             (new MetaType(UnitType.Terran_Refinery)));
        buildableList.add(new BuilderScienceFacility      (new MetaType(UnitType.Terran_Science_Facility)));
        buildableList.add(new BuilderStarport             (new MetaType(UnitType.Terran_Starport)));
        buildableList.add(new BuilderSupplyDepot          (new MetaType(UnitType.Terran_Supply_Depot)));
        
        //buildableList.add(new BuilderFactory              (new MetaType(UnitType.Terran_Factory)));
        //buildableList.add(new BuilderMachineShop          (new MetaType(UnitType.Terran_Machine_Shop)));
        //buildableList.add(new BuilderSupplyDepot          (new MetaType(UnitType.Terran_Supply_Depot)));

        /*upgrade(tech)*/
        ionThrusters               = new BuilderIonThrusters         (new MetaType(UpgradeType.Ion_Thrusters  ), researchSelector);
        charonBoosters             = new BuilderCharonBoosters       (new MetaType(UpgradeType.Charon_Boosters  ), researchSelector);
        apolloReactor              = new BuilderApolloReactor        (new MetaType(UpgradeType.Apollo_Reactor  ));
        
        /*
        
        caduceusReactor            = new BuilderCaduceusReactor      (new MetaType(UpgradeType.Caduceus_Reactor  ), upgradeSelector);
        colossusReactor            = new BuilderColossusReactor      (new MetaType(UpgradeType.Colossus_Reactor  ), upgradeSelector);
        moebiusReactor             = new BuilderMoebiusReactor       (new MetaType(UpgradeType.Moebius_Reactor  ), upgradeSelector);
        ocularImplants             = new BuilderOcularImplants       (new MetaType(UpgradeType.Ocular_Implants  ), upgradeSelector);
        terranInfantryArmor        = new BuilderTerranInfantryArmor  (new MetaType(UpgradeType.Terran_Infantry_Armor  ), upgradeSelector);
        terranInfantryWeapons      = new BuilderTerranInfantryWeapons(new MetaType(UpgradeType.Terran_Infantry_Weapons  ), upgradeSelector);
        terranShipPlating          = new BuilderTerranShipPlating    (new MetaType(UpgradeType.Terran_Ship_Plating  ), upgradeSelector);
        terranShipWeapons          = new BuilderTerranShipWeapons    (new MetaType(UpgradeType.Terran_Ship_Weapons  ), upgradeSelector);
        terranVehiclePlating       = new BuilderTerranVehiclePlating (new MetaType(UpgradeType.Terran_Vehicle_Plating  ), upgradeSelector);
        terranVehicleWeapons       = new BuilderTerranVehicleWeapons (new MetaType(UpgradeType.Terran_Vehicle_Weapons  ), upgradeSelector);
        titanReactor               = new BuilderTitanReactor         (new MetaType(UpgradeType.Titan_Reactor  ), upgradeSelector);
        u238Shells                 = new BuilderU238Shells           (new MetaType(UpgradeType.U_238_Shells  ), upgradeSelector);*/

        /*Research(tech)*/
        spiderMines                = new BuilderSpiderMines          (new MetaType(TechType.Spider_Mines    ), researchSelector);
        tankSiegeMode              = new BuilderTankSiegeMode        (new MetaType(TechType.Tank_Siege_Mode    ), researchSelector);
        cloakingField              = new BuilderCloakingField        (new MetaType(TechType.Cloaking_Field    ));
        /*empShockwave               = new BuilderEMPShockwave         (new MetaType(TechType.EMP_Shockwave    ), researchSelector2);
        irradiate                  = new BuilderIrradiate            (new MetaType(TechType.Irradiate    ), researchSelector2);
        lockdown                   = new BuilderLockdown             (new MetaType(TechType.Lockdown    ), researchSelector2);
        nuclearStrike              = new BuilderNuclearStrike        (new MetaType(TechType.Nuclear_Strike    ), researchSelector2);
        opticalFlare               = new BuilderOpticalFlare         (new MetaType(TechType.Optical_Flare    ), researchSelector2);
        personnelCloaking          = new BuilderPersonnelCloaking    (new MetaType(TechType.Personnel_Cloaking    ), researchSelector2);
        restoration                = new BuilderRestoration          (new MetaType(TechType.Restoration    ), researchSelector2);
        stimPacks                  = new BuilderStimPacks            (new MetaType(TechType.Stim_Packs    ), researchSelector2);
        yamatoGun                  = new BuilderYamatoGun            (new MetaType(TechType.Yamato_Gun    ), researchSelector2);*/



        //Activate
        /*upgrade*/
        buildableList.add(charonBoosters);
        buildableList.add(ionThrusters);
        buildableList.add(apolloReactor);
        /*buildableList.add(caduceusReactor);
        buildableList.add(colossusReactor);
        buildableList.add(moebiusReactor);
        buildableList.add(ocularImplants);
        buildableList.add(terranInfantryArmor);
        buildableList.add(terranInfantryWeapons);
        buildableList.add(terranShipPlating);
        buildableList.add(terranShipWeapons);
        buildableList.add(terranVehiclePlating);
        buildableList.add(terranVehicleWeapons);
        buildableList.add(titanReactor);
        buildableList.add(u238Shells);*/

        /*tech*/
        buildableList.add(spiderMines);
        buildableList.add(tankSiegeMode);
        buildableList.add(cloakingField);
        /*buildableList.add(empShockwave);
        buildableList.add(irradiate);
        buildableList.add(lockdown);
        buildableList.add(nuclearStrike);
        buildableList.add(opticalFlare);
        buildableList.add(personnelCloaking);
        buildableList.add(restoration);
        buildableList.add(stimPacks);
        buildableList.add(yamatoGun);*/

    }

    public void turnOffReseach(){

        //Deactivate
    	/*upgrade*/
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1) {
    		buildableList.remove(ionThrusters);
    	}
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1) {
    		buildableList.remove(charonBoosters);
    	}
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Apollo_Reactor) == 1) {
    		buildableList.remove(apolloReactor);
    	}
    	if(Prebot.Broodwar.self().hasResearched(TechType.Spider_Mines)) {
    		buildableList.remove(spiderMines);
    	}
    	if(Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)) {
    		buildableList.remove(tankSiegeMode);
    	}
    	if(Prebot.Broodwar.self().hasResearched(TechType.Cloaking_Field)) {
    		buildableList.remove(cloakingField);
    	}

    }

    public void update(){
    	turnOffReseach();
    	researchSelector.select();
        upgradeSelector.select();
        factoryUnitSelector.select();
        for(BuildableItem buildableItem: buildableList) {
    		buildableItem.process();
    	}
        
    }
    
    public void executeCombatUnitTrainingBlocked() {
		
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem currentItem = null; 
		Boolean goliathInTheQueue = false;
		Boolean tankInTheQueue = false;
		Boolean isarmoryexists = false;
		
		int vultureratio = StrategyIdea.factoryRatio.vulture;
		int tankratio = StrategyIdea.factoryRatio.tank;
		int goliathratio = StrategyIdea.factoryRatio.goliath;
		int wgt = StrategyIdea.factoryRatio.weight;
		
		if(Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed() < 4){
			return;
		}
		if (!tempbuildQueue.isEmpty()) {
			currentItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				
				
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					goliathInTheQueue = true;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					tankInTheQueue = true;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot){
					return;
				}
//				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType().isAddon()){
//					return;
//				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret){
					return;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Vulture){
					return;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_SCV){
					return;
				}
				if(currentItem.blocking == true){
					break;
				}
				if(tempbuildQueue.canSkipCurrentItem() == true){
					tempbuildQueue.skipCurrentItem();
				}else{
					break;
				}
				currentItem = tempbuildQueue.getItem();
			}
		}else{
			return;
		}
		
		
		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Armory) > 0) {
			isarmoryexists = true;
		}
		
		List<Unit> factory = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
		
		if(factory.size() > 0) {
			return;
		}
		
		for (Unit unit : factory)
		{
			if (unit.isTraining() == false){
				
//				if(unit.isConstructing() == true){
//					continue;
//				}
				
				//TODO else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop && unit.getAddon() == null ){
					continue;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					if(unit.getAddon() != null && unit.getAddon().isCompleted() != true){
						continue;
					}
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					if(isarmoryexists){
						break;
					}
				}
				
				boolean eventually_vulture = true;
				
				int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
				int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
				int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
				
				UnitType selected = null; 
				
				selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
				
				
				int minNeed = selected.mineralPrice();
				
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType()!=selected){
					if(selected == UnitType.Terran_Siege_Tank_Tank_Mode && tankInTheQueue == false){
						if(unit.getAddon() != null && unit.getAddon().isCompleted() == true){
							if(currentItem.metaType.mineralPrice()+minNeed < Prebot.Broodwar.self().minerals() &&
									currentItem.metaType.gasPrice()+selected.gasPrice() < Prebot.Broodwar.self().gas() && Prebot.Broodwar.self().supplyUsed() <= 392){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
								eventually_vulture = false;
							}
						}
					}else if(selected == UnitType.Terran_Goliath && goliathInTheQueue == false){
						if(isarmoryexists){
							if(currentItem.metaType.mineralPrice()+minNeed < Prebot.Broodwar.self().minerals() &&
									currentItem.metaType.gasPrice()+selected.gasPrice() < Prebot.Broodwar.self().gas() && Prebot.Broodwar.self().supplyUsed() <= 392){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
								eventually_vulture = false;
							}
						}
					}
				}
				
				if(eventually_vulture){
					if(Prebot.Broodwar.self().gas() < 250){
						minNeed = 75;
					}
					
					if(currentItem.metaType.mineralPrice()+minNeed < Prebot.Broodwar.self().minerals() && Prebot.Broodwar.self().supplyUsed() <= 392){
						if((unit.isConstructing() == true) || ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) != 0){
							continue;
						}
//						if(selected == UnitType.Terran_Goliath && isarmoryexists == false){
//							continue;
//						}
						if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) == 0){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						}
					}
				}
			}
 		}
	}
    
    public int GetCurrentTotBlocked(UnitType checkunit) {
		int cnt = Prebot.Broodwar.self().allUnitCount(checkunit);
		return cnt;
	}
    
    public static UnitType chooseunit(int ratea, int rateb, int ratec, int wgt, int tota, int totb, int totc) {

		if (wgt < 1 || wgt > 3) {
			wgt = 1;
		}
		double tempa = 0;
		double tempb = 0;
		double tempc = 0;
		if (ratea == 0) {
			tempa = 99999999;
		} else {
			tempa = 1.0 / ratea * tota;
		}
		if (rateb == 0) {
			tempb = 99999999;
		} else {
			tempb = 1.0 / rateb * totb;
		}
		if (ratec == 0) {
			tempc = 99999999;
		} else {
			tempc = 1.0 / ratec * totc;
		}
		int num = least(tempa, tempb, tempc, wgt);
		if (num == 3) {// 1:벌쳐, 2:시즈, 3:골리앗
			return UnitType.Terran_Goliath;
		} else if (num == 2) {
			return UnitType.Terran_Siege_Tank_Tank_Mode;
		} else {
			return UnitType.Terran_Vulture;
		}
	}
	
	public static int least(double a, double b, double c, int checker) {

		int ret = 0;
		if (a > b) {
			if (b > c) {
				ret = 3; // a>b>c
			} else {
				ret = 2; // a>b, b>=c
			}
		} else {
			if (a > c) { // a<=b, a>c
				ret = 3;
			} else { // a<=b, a<=c
				ret = 1;
			}
		}
		if (ret == 1) {
			if (a == b && checker != 3) {
				ret = checker;
			} else if (a == c && checker != 2) {
				ret = checker;
			}
		} else if (ret == 2) {
			if (b == a && checker != 3) {
				ret = checker;
			} else if (b == c && checker != 1) {
				ret = checker;
			}
		} else if (ret == 3) {
			if (c == a && checker != 2) {
				ret = checker;
			} else if (c == b && checker != 1) {
				ret = checker;
			}
		}
		return ret;
	}

	
}
