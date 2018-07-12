package prebot.build.provider;

import java.util.ArrayList;
import java.util.List;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.items.building.*;
import prebot.build.provider.items.tech.*;
import prebot.build.provider.items.unit.*;
import prebot.build.provider.items.upgrade.*;
import prebot.common.MetaType;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.manage.AttackExpansionManager;

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
    //ResearchSelector2 researchSelector2;
    UpgradeSelector upgradeSelector;
    BarrackUnitSelector barrackUnitSelector;
    FactoryUnitSelector factoryUnitSelector;
    StarportUnitSelector starportUnitSelector;

    public BuildQueueProvider(){

        researchSelector = new ResearchSelector();
        //researchSelector2 = new ResearchSelector2();
        upgradeSelector = new UpgradeSelector();
        barrackUnitSelector = new BarrackUnitSelector();
        factoryUnitSelector = new FactoryUnitSelector();
        starportUnitSelector = new StarportUnitSelector();

        //Unit
        //
        
        /*unit*/
        /*
        buildableList.add(new BuilderFirebat              (new MetaType(UnitType.Terran_Firebat), barrackUnitSelector));
        buildableList.add(new BuilderGhost                (new MetaType(UnitType.Terran_Ghost), barrackUnitSelector));
        buildableList.add(new BuilderMarine               (new MetaType(UnitType.Terran_Marine), barrackUnitSelector));
        buildableList.add(new BuilderMedic                (new MetaType(UnitType.Terran_Medic), barrackUnitSelector));*/
        
        buildableList.add(new BuilderGoliath              (new MetaType(UnitType.Terran_Goliath), factoryUnitSelector));
        buildableList.add(new BuilderVulture              (new MetaType(UnitType.Terran_Vulture), factoryUnitSelector));
        buildableList.add(new BuilderSiegeTank				(new MetaType(UnitType.Terran_Siege_Tank_Tank_Mode), factoryUnitSelector));
        buildableList.add(new BuilderWraith               (new MetaType(UnitType.Terran_Wraith), starportUnitSelector));
        
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
        /*
        apolloReactor              = new BuilderApolloReactor        (new MetaType(UpgradeType.Apollo_Reactor  ), upgradeSelector);
        caduceusReactor            = new BuilderCaduceusReactor      (new MetaType(UpgradeType.Caduceus_Reactor  ), upgradeSelector);
        charonBoosters             = new BuilderCharonBoosters       (new MetaType(UpgradeType.Charon_Boosters  ), upgradeSelector);
        colossusReactor            = new BuilderColossusReactor      (new MetaType(UpgradeType.Colossus_Reactor  ), upgradeSelector);
        ionThrusters               = new BuilderIonThrusters         (new MetaType(UpgradeType.Ion_Thrusters  ), upgradeSelector);
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
        /*cloakingField              = new BuilderCloakingField        (new MetaType(TechType.Cloaking_Field    ), researchSelector2);
        empShockwave               = new BuilderEMPShockwave         (new MetaType(TechType.EMP_Shockwave    ), researchSelector2);
        irradiate                  = new BuilderIrradiate            (new MetaType(TechType.Irradiate    ), researchSelector2);
        lockdown                   = new BuilderLockdown             (new MetaType(TechType.Lockdown    ), researchSelector2);
        nuclearStrike              = new BuilderNuclearStrike        (new MetaType(TechType.Nuclear_Strike    ), researchSelector2);
        opticalFlare               = new BuilderOpticalFlare         (new MetaType(TechType.Optical_Flare    ), researchSelector2);
        personnelCloaking          = new BuilderPersonnelCloaking    (new MetaType(TechType.Personnel_Cloaking    ), researchSelector2);
        restoration                = new BuilderRestoration          (new MetaType(TechType.Restoration    ), researchSelector2);
        spiderMines                = new BuilderSpiderMines          (new MetaType(TechType.Spider_Mines    ), researchSelector2);
        stimPacks                  = new BuilderStimPacks            (new MetaType(TechType.Stim_Packs    ), researchSelector2);
        tankSiegeMode              = new BuilderTankSiegeMode        (new MetaType(TechType.Tank_Siege_Mode    ), researchSelector2);
        yamatoGun                  = new BuilderYamatoGun            (new MetaType(TechType.Yamato_Gun    ), researchSelector2);*/



        //Activate
        /*upgrade*/
        buildableList.add(charonBoosters);
        buildableList.add(ionThrusters);
        /*buildableList.add(apolloReactor);
        buildableList.add(caduceusReactor);
        buildableList.add(charonBoosters);
        buildableList.add(colossusReactor);
        buildableList.add(ionThrusters);
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
        /*buildableList.add(cloakingField);
        buildableList.add(empShockwave);
        buildableList.add(irradiate);
        buildableList.add(lockdown);
        buildableList.add(nuclearStrike);
        buildableList.add(opticalFlare);
        buildableList.add(personnelCloaking);
        buildableList.add(restoration);
        buildableList.add(spiderMines);
        buildableList.add(stimPacks);
        buildableList.add(tankSiegeMode);
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
    	if(Prebot.Broodwar.self().hasResearched(TechType.Spider_Mines)) {
    		buildableList.remove(spiderMines);
    	}
    	if(Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)) {
    		buildableList.remove(tankSiegeMode);
    	}
    	
    	
    	
//    	buildableList.remove(apolloReactor);
//    	buildableList.remove(caduceusReactor);
//    	
//    	buildableList.remove(colossusReactor);
//    	
//    	buildableList.remove(moebiusReactor);
//    	buildableList.remove(ocularImplants);
//    	buildableList.remove(terranInfantryArmor);
//    	buildableList.remove(terranInfantryWeapons);
//    	buildableList.remove(terranShipPlating);
//    	buildableList.remove(terranShipWeapons);
//    	buildableList.remove(terranVehiclePlating);
//    	buildableList.remove(terranVehicleWeapons);
//    	buildableList.remove(titanReactor);
//    	buildableList.remove(u238Shells);
//
//    	/*tech*/
//    	buildableList.remove(cloakingField);
//    	buildableList.remove(empShockwave);
//    	buildableList.remove(irradiate);
//    	buildableList.remove(lockdown);
//    	buildableList.remove(nuclearStrike);
//    	buildableList.remove(opticalFlare);
//    	buildableList.remove(personnelCloaking);
//    	buildableList.remove(restoration);
//    	
//    	buildableList.remove(stimPacks);
//    	
//    	buildableList.remove(yamatoGun);
    }

    public void update(){
    	turnOffReseach();
    	
    	/*if (BuildConditionChecker.Instance().EXOK == false && Prebot.Broodwar.getFrameCount() % 2 == 0) {
    		BuildConditionChecker.Instance().executeFirstex();
		}*/
    	
    	
    	//if (Prebot.Broodwar.getFrameCount() % 43 == 0) {
	        researchSelector.select();
	        //researchSelector2.select();
		//}
    	
//    	if(!InitialBuildProvider.Instance().InitialBuildFinished) {
//    		InitialBuildProvider.Instance().updateInitialBuild();
//    	}
    		
		//셀렉터들에 필요한 유닛카운트 미리 집계
//		if (Prebot.Broodwar.getFrameCount() % 239 == 0) {
//			BuildConditionChecker.Instance().executeSustainUnits();
//		}
		
//		if (Prebot.Broodwar.getFrameCount() < 10000) {
//			if (Prebot.Broodwar.getFrameCount() % 29 == 0) {
//				BuildConditionChecker.Instance().executeFly();
//			}
//		} else {
//			if (Prebot.Broodwar.getFrameCount() % 281 == 0) {
//				BuildConditionChecker.Instance().executeFly();
//			}
//		}
		
		
//		if (InitialBuildProvider.Instance().InitialBuildFinished && Prebot.Broodwar.getFrameCount() % 53 == 0) {
	        upgradeSelector.select();
//		}
        //barrackUnitSelector.select();
        factoryUnitSelector.select();
        //starportUnitSelector.select();
        
        /*if(!sysout) {
        	
        	for(BuildableItem buildableItem: buildableList) {
        		FileUtils.appendTextToFile("log.txt", "\n BuildableItem || " + buildableItem.toString());
        	}
        	sysout = true;
        	
        }*/

        for(BuildableItem buildableItem: buildableList) {
        	//FileUtils.appendTextToFile("log.txt", "\n buildableItem.process() before|| " + buildableItem.toString());
    		buildableItem.process();
    		//FileUtils.appendTextToFile("log.txt", "\n buildableItem.process() after|| " + buildableItem.toString());
    	}
        
        /*respond strategy 대응*/
//        if ((Prebot.Broodwar.getFrameCount() < 13000 && Prebot.Broodwar.getFrameCount() % 5 == 0)
//				|| (Prebot.Broodwar.getFrameCount() >= 13000 && Prebot.Broodwar.getFrameCount() % 23 == 0)) { // Analyze 와 동일하게
//			RespondToStrategy.Instance().update();
//			// RespondToStrategyOld.Instance().update();// 다른 유닛 생성에 비해 제일 마지막에 돌아야 한다. highqueue 이용하면 제일 앞에 있을 것이므로
//			// AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
//		}
//        if(respondSet) {
//		    researchSelector.select();
//		    upgradeSelector.select();
//		    //barrackUnitSelector.select();
//		    factoryUnitSelector.select();
//        	for(BuildableItem buildableItem: buildableList) {
//        		buildableItem.process();
//        	}
//        	respondSet = false;
//        }
        
	//}
    }

	
}
