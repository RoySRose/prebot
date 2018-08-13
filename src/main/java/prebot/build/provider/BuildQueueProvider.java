package prebot.build.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import prebot.build.provider.items.unit.BuilderScienceVessel;
import prebot.build.provider.items.unit.BuilderSiegeTank;
import prebot.build.provider.items.unit.BuilderValkyrie;
import prebot.build.provider.items.unit.BuilderVulture;
import prebot.build.provider.items.unit.BuilderWraith;
import prebot.build.provider.items.upgrade.BuilderTerranShipPlating;
import prebot.build.provider.items.upgrade.BuilderTerranShipWeapons;
import prebot.build.provider.items.upgrade.BuilderTerranVehiclePlating;
import prebot.build.provider.items.upgrade.BuilderTerranVehicleWeapons;
import prebot.common.LagObserver;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.PlayerUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class BuildQueueProvider extends GameManager {

	//process 를 update 로 바꾸고 GameManager Extend
    private static BuildQueueProvider instance = new BuildQueueProvider();
    /// static singleton 객체를 리턴합니다
    public static BuildQueueProvider Instance() {
        return instance;
    }

    private Map<Integer, Integer> notOperatingFactoryTime = new HashMap<>();
	private Map<UpgradeType, Integer> upgradeStartMap = new HashMap<>();
	
	public void startUpgrade(UpgradeType upgradeType) {
//		FileUtils.appendTextToFile("log.txt", "\n set startUpgrade Frame:: " + upgradeType + " :: " + TimeUtils.elapsedFrames());
		upgradeStartMap.put(upgradeType, TimeUtils.elapsedFrames());
	}
	
	public int upgradeRemainingFrame(UpgradeType upgradeType) {
		if (!Prebot.Broodwar.self().isUpgrading(upgradeType)) {
			return CommonCode.UNKNOWN;
		}
		Integer startFrame = upgradeStartMap.get(upgradeType);
		if (startFrame == null) {
			return CommonCode.UNKNOWN;
		}
		return upgradeType.upgradeTime() - TimeUtils.elapsedFrames(startFrame);
	}
    
    /*private boolean isInitialBuildOrderFinished;

	public boolean isInitialBuildOrderFinished() {
		return isInitialBuildOrderFinished;
	}*/

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
    
    public FactoryUnitSelector getFactoryUnitSelector() {
		return factoryUnitSelector;
	}

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
        buildableList.add(new BuilderScienceVessel        (new MetaType(UnitType.Terran_Science_Vessel)));
        buildableList.add(new BuilderValkyrie             (new MetaType(UnitType.Terran_Valkyrie)));
        
        /*
        buildableList.add(new BuilderBattlecruiser        (new MetaType(UnitType.Terran_Battlecruiser),starportUnitSelector));
        buildableList.add(new BuilderDropship             (new MetaType(UnitType.Terran_Dropship),starportUnitSelector));
        
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
        terranShipPlating          = new BuilderTerranShipPlating    (new MetaType(UpgradeType.Terran_Ship_Plating  ), upgradeSelector);
        terranShipWeapons          = new BuilderTerranShipWeapons    (new MetaType(UpgradeType.Terran_Ship_Weapons  ), upgradeSelector);
        terranVehiclePlating       = new BuilderTerranVehiclePlating (new MetaType(UpgradeType.Terran_Vehicle_Plating  ), upgradeSelector);
        terranVehicleWeapons       = new BuilderTerranVehicleWeapons (new MetaType(UpgradeType.Terran_Vehicle_Weapons  ), upgradeSelector);
        /*
        
        apolloReactor              = new BuilderApolloReactor        (new MetaType(UpgradeType.Apollo_Reactor  ));
        caduceusReactor            = new BuilderCaduceusReactor      (new MetaType(UpgradeType.Caduceus_Reactor  ), upgradeSelector);
        colossusReactor            = new BuilderColossusReactor      (new MetaType(UpgradeType.Colossus_Reactor  ), upgradeSelector);
        moebiusReactor             = new BuilderMoebiusReactor       (new MetaType(UpgradeType.Moebius_Reactor  ), upgradeSelector);
        ocularImplants             = new BuilderOcularImplants       (new MetaType(UpgradeType.Ocular_Implants  ), upgradeSelector);
        terranInfantryArmor        = new BuilderTerranInfantryArmor  (new MetaType(UpgradeType.Terran_Infantry_Armor  ), upgradeSelector);
        terranInfantryWeapons      = new BuilderTerranInfantryWeapons(new MetaType(UpgradeType.Terran_Infantry_Weapons  ), upgradeSelector);
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
        buildableList.add(terranShipPlating);
        buildableList.add(terranShipWeapons);
        buildableList.add(terranVehiclePlating);
        buildableList.add(terranVehicleWeapons);
        /*buildableList.add(apolloReactor);
        buildableList.add(caduceusReactor);
        buildableList.add(colossusReactor);
        buildableList.add(moebiusReactor);
        buildableList.add(ocularImplants);
        buildableList.add(terranInfantryArmor);
        buildableList.add(terranInfantryWeapons);
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
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Ship_Plating) == 3) {
    		buildableList.remove(terranShipPlating);
    	}
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Ship_Weapons) == 3) {
    		buildableList.remove(terranShipWeapons);
    	}
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 3) {
    		buildableList.remove(terranVehiclePlating);
    	}
    	if(Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 3) {
    		buildableList.remove(terranVehicleWeapons);
    	}
    	
//    	 buildableList.add(terranShipPlating);
//         buildableList.add(terranShipWeapons);
//         buildableList.add(terranVehiclePlating);
//         buildableList.add(terranVehicleWeapons);

    }

	public void update() {
		if (TimeUtils.executeRotation(3, LagObserver.managerRotationSize())) {
			return;
		}
		
    	turnOffReseach();
    	researchSelector.select();
        upgradeSelector.select();
        factoryUnitSelector.select();
        for(BuildableItem buildableItem: buildableList) {
    		buildableItem.process();
    	}

       	executeCombatUnitTrainingBlocked();
    }
    
    public void executeCombatUnitTrainingBlocked() {
		
		if (Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed() < 4) {
			return;
		}
		if (Prebot.Broodwar.self().supplyUsed() > 392) {
			return;
		}
		if (Prebot.Broodwar.self().minerals() < 500) {
			return;
		}
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		if (tempbuildQueue.isEmpty()) {
			return;
		}
		List<Unit> factories = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
		if (factories.isEmpty()) {
			return;
		}
		
		// 팩토리를 일정 시간 이상 가동되지 않았을 때를 비가동 팩토리로 본다.
		List<Unit> notOperatingFactories = new ArrayList<>();
		for (Unit factory : factories) {
			if (factory.isTraining()) {
				if (notOperatingFactoryTime.containsKey(factory.getID())) {
					notOperatingFactoryTime.remove(factory.getID());
				}
				continue;
			}
			
			Integer notOperatingFrame = notOperatingFactoryTime.get(factory.getID());
			if (notOperatingFrame == null) {
				notOperatingFactoryTime.put(factory.getID(), TimeUtils.elapsedFrames());
			} else {
				if (TimeUtils.elapsedSeconds(notOperatingFrame) >= 3) {
					notOperatingFactories.add(factory);
				}
			}
		}
		
		if (notOperatingFactories.isEmpty()) {
			return;
		}
		
//		System.out.println("notOperatingFactories.size() = " + notOperatingFactories.size());

		boolean goliathInTheQueue = false;
		boolean tankInTheQueue = false;

		BuildOrderItem blockingItem = tempbuildQueue.getHighestPriorityItem();
		while (true) {
			if (blockingItem.metaType.isUnit()) {
				UnitType unitType = blockingItem.metaType.getUnitType();
				if (unitType == UnitType.Terran_Goliath) {
					goliathInTheQueue = true;
				} else if (blockingItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
					tankInTheQueue = true;
					
				} else if (unitType == UnitType.Terran_Supply_Depot
						|| unitType == UnitType.Terran_Missile_Turret
						|| unitType == UnitType.Terran_Vulture
						|| unitType == UnitType.Terran_SCV) {
//						|| unitType.isAddon()) {
					return;
				}
			}
			if (blockingItem.blocking || !tempbuildQueue.canSkipCurrentItem()) {
				break;
			}

			tempbuildQueue.skipCurrentItem();
			blockingItem = tempbuildQueue.getItem();
		}
		
		boolean isArmoryExists = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Armory) > 0;
		boolean vultureInTheQueue = tempbuildQueue.getItemCount(UnitType.Terran_Vulture) > 0;
		
		int totVulture = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Vulture);
		int totTank = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
		int totGoliath = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Goliath);

		int vultureratio = StrategyIdea.factoryRatio.vulture;
		int tankratio = StrategyIdea.factoryRatio.tank;
		int goliathratio = StrategyIdea.factoryRatio.goliath;
		int wgt = StrategyIdea.factoryRatio.weight;
		
		for (Unit factory : notOperatingFactories) {
			if (factory.isTraining()) {
				continue;
			}
				
			//TODO else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
			if (blockingItem.metaType.isUnit()) {
				if (blockingItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop) {
					if (factory.getAddon() == null) {
						continue;
					}
				} else if (blockingItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
					if (factory.getAddon() != null && !factory.getAddon().isCompleted()) {
						continue;
					}
				} else if (blockingItem.metaType.getUnitType() == UnitType.Terran_Goliath) {
					if (isArmoryExists) {
						break;
					}
				}
			}
			
			UnitType selected = FactoryUnitSelector.chooseunit(vultureratio, tankratio, goliathratio, wgt, totVulture, totTank, totGoliath);
			
			if (blockingItem.metaType.isUnit() && blockingItem.metaType.getUnitType() != selected) {
				if (selected == UnitType.Terran_Siege_Tank_Tank_Mode && !tankInTheQueue && factory.getAddon() != null && factory.getAddon().isCompleted()) {
					int mineralNeed = blockingItem.metaType.mineralPrice() + selected.mineralPrice();
					int gasNeed = blockingItem.metaType.gasPrice() + selected.gasPrice();
					if (PlayerUtils.enoughResource(mineralNeed, gasNeed)) {
						System.out.println("block tank provided");
						BuildManager.Instance().buildQueue.queueAsHighestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return;
					}
				}
				else if (selected == UnitType.Terran_Goliath && !goliathInTheQueue && isArmoryExists) {
					int mineralNeed = blockingItem.metaType.mineralPrice() + selected.mineralPrice();
					int gasNeed = blockingItem.metaType.gasPrice() + selected.gasPrice();
					if (PlayerUtils.enoughResource(mineralNeed, gasNeed)) {
						System.out.println("block goliath provided");
						BuildManager.Instance().buildQueue.queueAsHighestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return;
					}
				}
				
			}
			
			if (!vultureInTheQueue) {
				int mineralNeed = selected.mineralPrice();
				if (Prebot.Broodwar.self().gas() < 250) {
					mineralNeed = 75;
				}
				mineralNeed = blockingItem.metaType.mineralPrice() + mineralNeed;
				if (PlayerUtils.enoughResource(mineralNeed, 0)) {
					if (factory.isConstructing() || ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) != 0) {
						continue;
					}
					// if(selected == UnitType.Terran_Goliath && isarmoryexists == false){
					// continue;
					// }
					System.out.println("block vulture provided");
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return;
				}
			}
 		}
	}

	
}
