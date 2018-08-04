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
import prebot.build.provider.items.unit.BuilderSiegeTank;
import prebot.build.provider.items.unit.BuilderVulture;
import prebot.build.provider.items.unit.BuilderWraith;
import prebot.build.provider.items.upgrade.BuilderApolloReactor;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
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

	private Map<UpgradeType, Integer> upgradeStartMap = new HashMap<>();
	
	public void startUpgrade(UpgradeType upgradeType) {
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
		
		if (Prebot.Broodwar.self().supplyTotal() - Prebot.Broodwar.self().supplyUsed() < 4) {
			return;
		}
		if (Prebot.Broodwar.self().supplyUsed() > 392) {
			return;
		}
		if (Prebot.Broodwar.self().minerals() < 300) {
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

		boolean goliathInTheQueue = false;
		boolean tankInTheQueue = false;

		BuildOrderItem currentItem = tempbuildQueue.getHighestPriorityItem();
		while (true) {
			if (currentItem.metaType.isUnit()) {
				UnitType unitType = currentItem.metaType.getUnitType();
				if (unitType == UnitType.Terran_Goliath) {
					goliathInTheQueue = true;
				} else if (currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
					tankInTheQueue = true;
					
				} else if (unitType == UnitType.Terran_Supply_Depot
						|| unitType == UnitType.Terran_Missile_Turret
						|| unitType == UnitType.Terran_Vulture
						|| unitType == UnitType.Terran_SCV) {
//						|| unitType.isAddon()) {
					return;
				}
			}
			if (currentItem.blocking || !tempbuildQueue.canSkipCurrentItem()) {
				break;
			}

			tempbuildQueue.skipCurrentItem();
			currentItem = tempbuildQueue.getItem();
		}
		
		boolean isArmoryExists = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Armory) > 0;
		boolean vultureInTheQueue = tempbuildQueue.getItemCount(UnitType.Terran_Vulture) > 0;
		
		for (Unit factory : factories) {
			if (factory.isTraining()) {
				continue;
			}
				
			//TODO else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
			if (currentItem.metaType.isUnit()) {
				if (currentItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop) {
					if (factory.getAddon() == null) {
						continue;
					}
				} else if (currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
					if (factory.getAddon() != null && !factory.getAddon().isCompleted()) {
						continue;
					}
				} else if (currentItem.metaType.getUnitType() == UnitType.Terran_Goliath) {
					if (isArmoryExists) {
						break;
					}
				}
			}
			
			int tot_vulture = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Vulture);
			int tot_tank = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) + Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
			int tot_goliath = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Goliath);
			

			int vultureratio = StrategyIdea.factoryRatio.vulture;
			int tankratio = StrategyIdea.factoryRatio.tank;
			int goliathratio = StrategyIdea.factoryRatio.goliath;
			int wgt = StrategyIdea.factoryRatio.weight;
			
			UnitType selected = FactoryUnitSelector.chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
			
			if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() != selected) {
				if (selected == UnitType.Terran_Siege_Tank_Tank_Mode && !tankInTheQueue && factory.getAddon() != null && factory.getAddon().isCompleted()) {
					int mineralNeed = currentItem.metaType.mineralPrice() + selected.mineralPrice();
					int gasNeed = currentItem.metaType.gasPrice() + selected.gasPrice();
					if (PlayerUtils.enoughResource(mineralNeed, gasNeed)) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return;
					}
				}
				else if (selected == UnitType.Terran_Goliath && !goliathInTheQueue && isArmoryExists) {
					int mineralNeed = currentItem.metaType.mineralPrice() + selected.mineralPrice();
					int gasNeed = currentItem.metaType.gasPrice() + selected.gasPrice();
					if (PlayerUtils.enoughResource(mineralNeed, gasNeed)) {
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
				mineralNeed = currentItem.metaType.mineralPrice() + mineralNeed;
				if (PlayerUtils.enoughResource(mineralNeed, 0)) {
					if (factory.isConstructing() || ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) != 0) {
						continue;
					}
					// if(selected == UnitType.Terran_Goliath && isarmoryexists == false){
					// continue;
					// }
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return;
				}
			}
 		}
	}

	
}
