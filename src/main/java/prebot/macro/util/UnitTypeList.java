package prebot.macro.util;

import bwapi.DamageType;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.strategy.InformationManager;

import java.util.Arrays;
import java.util.List;

public class UnitTypeList {

    private static final List<UnitType> ZERG_UNITTYPE;
    private static final List<UnitType> PROTOSS_UNITTYPE;
    private static final List<UnitType> TERRAN_UNITTYPE;

    static {
        TERRAN_UNITTYPE = Arrays.asList(
                UnitType.Terran_Firebat,
                UnitType.Terran_Ghost,
                UnitType.Terran_Goliath,
                UnitType.Terran_Marine,
                UnitType.Terran_Medic,
                UnitType.Terran_SCV,
                UnitType.Terran_Siege_Tank_Siege_Mode,
                UnitType.Terran_Siege_Tank_Tank_Mode,
                UnitType.Terran_Vulture,
                UnitType.Terran_Battlecruiser,
                UnitType.Terran_Dropship,
                UnitType.Terran_Science_Vessel,
                UnitType.Terran_Valkyrie,
                UnitType.Terran_Wraith,
                UnitType.Terran_Academy,
                UnitType.Terran_Armory,
                UnitType.Terran_Barracks,
                UnitType.Terran_Bunker,
                UnitType.Terran_Command_Center,
                UnitType.Terran_Engineering_Bay,
                UnitType.Terran_Factory,
                UnitType.Terran_Missile_Turret,
                UnitType.Terran_Refinery,
                UnitType.Terran_Science_Facility,
                UnitType.Terran_Starport,
                UnitType.Terran_Supply_Depot,
                UnitType.Terran_Comsat_Station,
                UnitType.Terran_Control_Tower,
                UnitType.Terran_Covert_Ops,
                UnitType.Terran_Machine_Shop,
                UnitType.Terran_Nuclear_Silo,
                UnitType.Terran_Physics_Lab);

        PROTOSS_UNITTYPE = Arrays.asList(
                UnitType.Protoss_Archon,
                UnitType.Protoss_Dark_Archon,
                UnitType.Protoss_Dark_Templar,
                UnitType.Protoss_Dragoon,
                UnitType.Protoss_High_Templar,
                UnitType.Protoss_Probe,
                UnitType.Protoss_Reaver,
                UnitType.Protoss_Scarab,
                UnitType.Protoss_Zealot,
                UnitType.Protoss_Arbiter,
                UnitType.Protoss_Carrier,
                UnitType.Protoss_Corsair,
                UnitType.Protoss_Interceptor,
                UnitType.Protoss_Observer,
                UnitType.Protoss_Scout,
                UnitType.Protoss_Shuttle,
                UnitType.Protoss_Arbiter_Tribunal,
                UnitType.Protoss_Assimilator,
                UnitType.Protoss_Citadel_of_Adun,
                UnitType.Protoss_Cybernetics_Core,
                UnitType.Protoss_Fleet_Beacon,
                UnitType.Protoss_Forge,
                UnitType.Protoss_Gateway,
                UnitType.Protoss_Nexus,
                UnitType.Protoss_Observatory,
                UnitType.Protoss_Photon_Cannon,
                UnitType.Protoss_Pylon,
                UnitType.Protoss_Robotics_Facility,
                UnitType.Protoss_Robotics_Support_Bay,
                UnitType.Protoss_Shield_Battery,
                UnitType.Protoss_Stargate,
                UnitType.Protoss_Templar_Archives
                );

        ZERG_UNITTYPE = Arrays.asList(
                UnitType.Zerg_Broodling,
                UnitType.Zerg_Defiler,
                UnitType.Zerg_Drone,
                UnitType.Zerg_Egg,
                UnitType.Zerg_Hydralisk,
                UnitType.Zerg_Infested_Terran,
                UnitType.Zerg_Larva,
                UnitType.Zerg_Lurker,
                UnitType.Zerg_Lurker_Egg,
                UnitType.Zerg_Ultralisk,
                UnitType.Zerg_Zergling,
                UnitType.Zerg_Cocoon,
                UnitType.Zerg_Devourer,
                UnitType.Zerg_Guardian,
                UnitType.Zerg_Mutalisk,
                UnitType.Zerg_Overlord,
                UnitType.Zerg_Queen,
                UnitType.Zerg_Scourge,
                UnitType.Zerg_Creep_Colony,
                UnitType.Zerg_Defiler_Mound,
                UnitType.Zerg_Evolution_Chamber,
                UnitType.Zerg_Extractor,
                UnitType.Zerg_Greater_Spire,
                UnitType.Zerg_Hatchery,
                UnitType.Zerg_Hive,
                UnitType.Zerg_Hydralisk_Den,
                UnitType.Zerg_Infested_Command_Center,
                UnitType.Zerg_Lair,
                UnitType.Zerg_Nydus_Canal,
                UnitType.Zerg_Queens_Nest,
                UnitType.Zerg_Spawning_Pool,
                UnitType.Zerg_Spire,
                UnitType.Zerg_Spore_Colony,
                UnitType.Zerg_Sunken_Colony,
                UnitType.Zerg_Ultralisk_Cavern);
    }

    public static List<UnitType> getAllType() {
        if (InformationManager.Instance().enemyRace == Race.Terran) {
            return TERRAN_UNITTYPE;
        } else if (InformationManager.Instance().enemyRace == Race.Protoss) {
            return PROTOSS_UNITTYPE;
        } else {
            return ZERG_UNITTYPE;
        }
    }
}
