

import bwapi.Race;
import bwapi.UnitType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreBoard {

    Map<UnitType, Integer> scoreMap;

    public ScoreBoard() {
        scoreMap = new HashMap<>();
        
        scoreMap.put(UnitType.Terran_Goliath ,100);
        scoreMap.put(UnitType.Terran_Siege_Tank_Siege_Mode ,250);
        scoreMap.put(UnitType.Terran_Siege_Tank_Tank_Mode ,250);
        scoreMap.put(UnitType.Terran_Vulture ,70);
        scoreMap.put(UnitType.Terran_Science_Vessel ,100);
        scoreMap.put(UnitType.Terran_Valkyrie ,100);
        scoreMap.put(UnitType.Terran_Wraith ,25);
        
        if (InformationManager.Instance().enemyRace == Race.Terran) {
            scoreMap.put(UnitType.Terran_Firebat ,1);
            scoreMap.put(UnitType.Terran_Ghost ,10);
            scoreMap.put(UnitType.Terran_Goliath ,100);
            scoreMap.put(UnitType.Terran_Marine ,10);
            scoreMap.put(UnitType.Terran_Medic ,1);
            scoreMap.put(UnitType.Terran_SCV ,0);
            scoreMap.put(UnitType.Terran_Siege_Tank_Siege_Mode ,250);
            scoreMap.put(UnitType.Terran_Siege_Tank_Tank_Mode ,250);
            scoreMap.put(UnitType.Terran_Vulture ,70);
            scoreMap.put(UnitType.Terran_Battlecruiser ,500);
            scoreMap.put(UnitType.Terran_Dropship ,0);
            scoreMap.put(UnitType.Terran_Science_Vessel ,20);
            scoreMap.put(UnitType.Terran_Valkyrie ,100);
            scoreMap.put(UnitType.Terran_Wraith ,25);
            scoreMap.put(UnitType.Terran_Academy ,0);
            scoreMap.put(UnitType.Terran_Armory ,0);
            scoreMap.put(UnitType.Terran_Barracks ,0);
            scoreMap.put(UnitType.Terran_Bunker ,0);
            scoreMap.put(UnitType.Terran_Command_Center ,0);
            scoreMap.put(UnitType.Terran_Engineering_Bay ,0);
            scoreMap.put(UnitType.Terran_Factory ,0);
            scoreMap.put(UnitType.Terran_Missile_Turret ,0);
            scoreMap.put(UnitType.Terran_Refinery ,0);
            scoreMap.put(UnitType.Terran_Science_Facility ,0);
            scoreMap.put(UnitType.Terran_Starport ,0);
            scoreMap.put(UnitType.Terran_Supply_Depot ,0);
            scoreMap.put(UnitType.Terran_Comsat_Station ,0);
            scoreMap.put(UnitType.Terran_Control_Tower ,0);
            scoreMap.put(UnitType.Terran_Covert_Ops ,0);
            scoreMap.put(UnitType.Terran_Machine_Shop ,0);
            scoreMap.put(UnitType.Terran_Nuclear_Silo ,0);
            scoreMap.put(UnitType.Terran_Physics_Lab ,0);
        } else if (InformationManager.Instance().enemyRace == Race.Protoss) {
            scoreMap.put(UnitType.Protoss_Archon ,210);
            scoreMap.put(UnitType.Protoss_Dark_Archon ,10);
            scoreMap.put(UnitType.Protoss_Dark_Templar ,280);
            scoreMap.put(UnitType.Protoss_Dragoon ,150);
            scoreMap.put(UnitType.Protoss_High_Templar ,0);
            scoreMap.put(UnitType.Protoss_Probe ,0);
            scoreMap.put(UnitType.Protoss_Reaver ,250);
            scoreMap.put(UnitType.Protoss_Scarab ,0);
            scoreMap.put(UnitType.Protoss_Zealot ,120);
            scoreMap.put(UnitType.Protoss_Arbiter ,200);
            scoreMap.put(UnitType.Protoss_Carrier ,600);
            scoreMap.put(UnitType.Protoss_Corsair ,50);
            scoreMap.put(UnitType.Protoss_Interceptor ,0);
            scoreMap.put(UnitType.Protoss_Observer ,0);
            scoreMap.put(UnitType.Protoss_Scout ,50);
            scoreMap.put(UnitType.Protoss_Shuttle ,0);
            scoreMap.put(UnitType.Protoss_Arbiter_Tribunal ,0);
            scoreMap.put(UnitType.Protoss_Assimilator ,0);
            scoreMap.put(UnitType.Protoss_Citadel_of_Adun ,0);
            scoreMap.put(UnitType.Protoss_Cybernetics_Core ,0);
            scoreMap.put(UnitType.Protoss_Fleet_Beacon ,0);
            scoreMap.put(UnitType.Protoss_Forge ,0);
            scoreMap.put(UnitType.Protoss_Gateway ,0);
            scoreMap.put(UnitType.Protoss_Nexus ,0);
            scoreMap.put(UnitType.Protoss_Observatory ,0);
            scoreMap.put(UnitType.Protoss_Photon_Cannon ,0);
            scoreMap.put(UnitType.Protoss_Pylon ,0);
            scoreMap.put(UnitType.Protoss_Robotics_Facility ,0);
            scoreMap.put(UnitType.Protoss_Robotics_Support_Bay ,0);
            scoreMap.put(UnitType.Protoss_Shield_Battery ,0);
            scoreMap.put(UnitType.Protoss_Stargate ,0);
            scoreMap.put(UnitType.Protoss_Templar_Archives ,0);
        } else {
            scoreMap.put(UnitType.Zerg_Broodling ,0);
            scoreMap.put(UnitType.Zerg_Defiler ,300);
            scoreMap.put(UnitType.Zerg_Drone ,0);
            scoreMap.put(UnitType.Zerg_Egg ,0);
            scoreMap.put(UnitType.Zerg_Hydralisk ,80);
            scoreMap.put(UnitType.Zerg_Infested_Terran ,0);
            scoreMap.put(UnitType.Zerg_Larva ,0);
            scoreMap.put(UnitType.Zerg_Lurker ,120);
            scoreMap.put(UnitType.Zerg_Lurker_Egg ,0);
            scoreMap.put(UnitType.Zerg_Ultralisk ,300);
            scoreMap.put(UnitType.Zerg_Zergling ,30);
            scoreMap.put(UnitType.Zerg_Cocoon ,0);
            scoreMap.put(UnitType.Zerg_Devourer ,20);
            scoreMap.put(UnitType.Zerg_Guardian ,250);
            scoreMap.put(UnitType.Zerg_Mutalisk ,120);
            scoreMap.put(UnitType.Zerg_Overlord ,0);
            scoreMap.put(UnitType.Zerg_Queen ,250);
            scoreMap.put(UnitType.Zerg_Scourge ,5);
            scoreMap.put(UnitType.Zerg_Creep_Colony ,0);
            scoreMap.put(UnitType.Zerg_Defiler_Mound ,0);
            scoreMap.put(UnitType.Zerg_Evolution_Chamber ,0);
            scoreMap.put(UnitType.Zerg_Extractor ,0);
            scoreMap.put(UnitType.Zerg_Greater_Spire ,0);
            scoreMap.put(UnitType.Zerg_Hatchery ,0);
            scoreMap.put(UnitType.Zerg_Hive ,0);
            scoreMap.put(UnitType.Zerg_Hydralisk_Den ,0);
            scoreMap.put(UnitType.Zerg_Infested_Command_Center ,0);
            scoreMap.put(UnitType.Zerg_Lair ,0);
            scoreMap.put(UnitType.Zerg_Nydus_Canal ,0);
            scoreMap.put(UnitType.Zerg_Queens_Nest ,0);
            scoreMap.put(UnitType.Zerg_Spawning_Pool ,0);
            scoreMap.put(UnitType.Zerg_Spire ,0);
            scoreMap.put(UnitType.Zerg_Spore_Colony ,0);
            scoreMap.put(UnitType.Zerg_Sunken_Colony ,0);
            scoreMap.put(UnitType.Zerg_Ultralisk_Cavern ,0);
        }
//            scoreMap.put(UnitType.Terran_Firebat ,1);
//            scoreMap.put(UnitType.Terran_Ghost ,10);
//            scoreMap.put(UnitType.Terran_Goliath ,100);
//            scoreMap.put(UnitType.Terran_Marine ,10);
//            scoreMap.put(UnitType.Terran_Medic ,1);
//            scoreMap.put(UnitType.Terran_SCV ,0);
//            scoreMap.put(UnitType.Terran_Siege_Tank_Siege_Mode ,250);
//            scoreMap.put(UnitType.Terran_Siege_Tank_Tank_Mode ,250);
//            scoreMap.put(UnitType.Terran_Vulture ,70);
//            scoreMap.put(UnitType.Terran_Battlecruiser ,500);
//            scoreMap.put(UnitType.Terran_Dropship ,0);
//            scoreMap.put(UnitType.Terran_Science_Vessel ,20);
//            scoreMap.put(UnitType.Terran_Valkyrie ,10);
//            scoreMap.put(UnitType.Terran_Wraith ,25);
//            scoreMap.put(UnitType.Terran_Academy ,0);
//            scoreMap.put(UnitType.Terran_Armory ,0);
//            scoreMap.put(UnitType.Terran_Barracks ,0);
//            scoreMap.put(UnitType.Terran_Bunker ,0);
//            scoreMap.put(UnitType.Terran_Command_Center ,0);
//            scoreMap.put(UnitType.Terran_Engineering_Bay ,0);
//            scoreMap.put(UnitType.Terran_Factory ,0);
//            scoreMap.put(UnitType.Terran_Missile_Turret ,0);
//            scoreMap.put(UnitType.Terran_Refinery ,0);
//            scoreMap.put(UnitType.Terran_Science_Facility ,0);
//            scoreMap.put(UnitType.Terran_Starport ,0);
//            scoreMap.put(UnitType.Terran_Supply_Depot ,0);
//            scoreMap.put(UnitType.Terran_Comsat_Station ,0);
//            scoreMap.put(UnitType.Terran_Control_Tower ,0);
//            scoreMap.put(UnitType.Terran_Covert_Ops ,0);
//            scoreMap.put(UnitType.Terran_Machine_Shop ,0);
//            scoreMap.put(UnitType.Terran_Nuclear_Silo ,0);
//            scoreMap.put(UnitType.Terran_Physics_Lab ,0);
//            scoreMap.put(UnitType.Protoss_Archon ,210);
//            scoreMap.put(UnitType.Protoss_Dark_Archon ,10);
//            scoreMap.put(UnitType.Protoss_Dark_Templar ,280);
//            scoreMap.put(UnitType.Protoss_Dragoon ,150);
//            scoreMap.put(UnitType.Protoss_High_Templar ,0);
//            scoreMap.put(UnitType.Protoss_Probe ,0);
//            scoreMap.put(UnitType.Protoss_Reaver ,250);
//            scoreMap.put(UnitType.Protoss_Scarab ,0);
//            scoreMap.put(UnitType.Protoss_Zealot ,120);
//            scoreMap.put(UnitType.Protoss_Arbiter ,200);
//            scoreMap.put(UnitType.Protoss_Carrier ,600);
//            scoreMap.put(UnitType.Protoss_Corsair ,50);
//            scoreMap.put(UnitType.Protoss_Interceptor ,0);
//            scoreMap.put(UnitType.Protoss_Observer ,0);
//            scoreMap.put(UnitType.Protoss_Scout ,50);
//            scoreMap.put(UnitType.Protoss_Shuttle ,0);
//            scoreMap.put(UnitType.Protoss_Arbiter_Tribunal ,0);
//            scoreMap.put(UnitType.Protoss_Assimilator ,0);
//            scoreMap.put(UnitType.Protoss_Citadel_of_Adun ,0);
//            scoreMap.put(UnitType.Protoss_Cybernetics_Core ,0);
//            scoreMap.put(UnitType.Protoss_Fleet_Beacon ,0);
//            scoreMap.put(UnitType.Protoss_Forge ,0);
//            scoreMap.put(UnitType.Protoss_Gateway ,0);
//            scoreMap.put(UnitType.Protoss_Nexus ,0);
//            scoreMap.put(UnitType.Protoss_Observatory ,0);
//            scoreMap.put(UnitType.Protoss_Photon_Cannon ,0);
//            scoreMap.put(UnitType.Protoss_Pylon ,0);
//            scoreMap.put(UnitType.Protoss_Robotics_Facility ,0);
//            scoreMap.put(UnitType.Protoss_Robotics_Support_Bay ,0);
//            scoreMap.put(UnitType.Protoss_Shield_Battery ,0);
//            scoreMap.put(UnitType.Protoss_Stargate ,0);
//            scoreMap.put(UnitType.Protoss_Templar_Archives ,0);
//            scoreMap.put(UnitType.Zerg_Broodling ,0);
//            scoreMap.put(UnitType.Zerg_Defiler ,300);
//            scoreMap.put(UnitType.Zerg_Drone ,0);
//            scoreMap.put(UnitType.Zerg_Egg ,0);
//            scoreMap.put(UnitType.Zerg_Hydralisk ,80);
//            scoreMap.put(UnitType.Zerg_Infested_Terran ,0);
//            scoreMap.put(UnitType.Zerg_Larva ,0);
//            scoreMap.put(UnitType.Zerg_Lurker ,120);
//            scoreMap.put(UnitType.Zerg_Lurker_Egg ,0);
//            scoreMap.put(UnitType.Zerg_Ultralisk ,300);
//            scoreMap.put(UnitType.Zerg_Zergling ,30);
//            scoreMap.put(UnitType.Zerg_Cocoon ,0);
//            scoreMap.put(UnitType.Zerg_Devourer ,20);
//            scoreMap.put(UnitType.Zerg_Guardian ,250);
//            scoreMap.put(UnitType.Zerg_Mutalisk ,120);
//            scoreMap.put(UnitType.Zerg_Overlord ,0);
//            scoreMap.put(UnitType.Zerg_Queen ,250);
//            scoreMap.put(UnitType.Zerg_Scourge ,5);
//            scoreMap.put(UnitType.Zerg_Creep_Colony ,0);
//            scoreMap.put(UnitType.Zerg_Defiler_Mound ,0);
//            scoreMap.put(UnitType.Zerg_Evolution_Chamber ,0);
//            scoreMap.put(UnitType.Zerg_Extractor ,0);
//            scoreMap.put(UnitType.Zerg_Greater_Spire ,0);
//            scoreMap.put(UnitType.Zerg_Hatchery ,0);
//            scoreMap.put(UnitType.Zerg_Hive ,0);
//            scoreMap.put(UnitType.Zerg_Hydralisk_Den ,0);
//            scoreMap.put(UnitType.Zerg_Infested_Command_Center ,0);
//            scoreMap.put(UnitType.Zerg_Lair ,0);
//            scoreMap.put(UnitType.Zerg_Nydus_Canal ,0);
//            scoreMap.put(UnitType.Zerg_Queens_Nest ,0);
//            scoreMap.put(UnitType.Zerg_Spawning_Pool ,0);
//            scoreMap.put(UnitType.Zerg_Spire ,0);
//            scoreMap.put(UnitType.Zerg_Spore_Colony ,0);
//            scoreMap.put(UnitType.Zerg_Sunken_Colony ,0);
//            scoreMap.put(UnitType.Zerg_Ultralisk_Cavern ,0);
    }

    public int getPoint(UnitType unitType){
        for (Map.Entry<UnitType, Integer> enemyScore : scoreMap.entrySet()){
            if(enemyScore.getKey() == unitType){
                return enemyScore.getValue();
            }
        }
        return 0;
    }
}
