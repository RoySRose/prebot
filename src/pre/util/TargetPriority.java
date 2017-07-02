package pre.util;

import java.util.HashMap;
import java.util.Map;

import bwapi.UnitType;
import pre.main.MyBotModule;

public class TargetPriority {
	
	public static int getPriority(UnitType attacker, UnitType target) {
		Map<UnitType, Integer> map = PRIORITY_MAP.get(attacker);
		if (map == null) {
			MyBotModule.Broodwar.sendText("priority map is null");
			return 10;
		}
		
		return map.get(target);
	}
	
	private static final Map<UnitType, Map<UnitType, Integer>> PRIORITY_MAP = new HashMap<>();
	
	static {
//		Map<UnitType, Integer> vulturePriorityMap = new HashMap<>();
//		Map<UnitType, Integer> tankModePriorityMap = new HashMap<>();
//		Map<UnitType, Integer> siegeModePriorityMap = new HashMap<>();
		Map<UnitType, Integer> goliathPriorityMap = new HashMap<>();
//		PRIORITY_MAP.put(UnitType.Terran_Vulture.toString(), vulturePriorityMap);
//		PRIORITY_MAP.put(UnitType.Terran_Siege_Tank_Tank_Mode.toString(), tankModePriorityMap);
//		PRIORITY_MAP.put(UnitType.Terran_Siege_Tank_Siege_Mode.toString(), siegeModePriorityMap);
		PRIORITY_MAP.put(UnitType.Terran_Goliath, goliathPriorityMap);
		
		goliathPriorityMap.put(UnitType.Terran_Vulture_Spider_Mine, 	200);
		goliathPriorityMap.put(UnitType.Terran_Ghost,					190);
		goliathPriorityMap.put(UnitType.Terran_Wraith,					180);
		goliathPriorityMap.put(UnitType.Terran_Battlecruiser,			170);
		goliathPriorityMap.put(UnitType.Terran_Dropship,				160);
		goliathPriorityMap.put(UnitType.Terran_Siege_Tank_Siege_Mode,	150);
		goliathPriorityMap.put(UnitType.Terran_Siege_Tank_Tank_Mode,	140);
		goliathPriorityMap.put(UnitType.Terran_Goliath,					130);
		goliathPriorityMap.put(UnitType.Terran_Marine,					120);
		goliathPriorityMap.put(UnitType.Terran_Medic,					110);
		goliathPriorityMap.put(UnitType.Terran_Firebat,					100);
		goliathPriorityMap.put(UnitType.Terran_Vulture,					90);
		goliathPriorityMap.put(UnitType.Terran_Science_Vessel,			80);
		goliathPriorityMap.put(UnitType.Terran_SCV,						70);
		goliathPriorityMap.put(UnitType.Terran_Valkyrie,				60);
		
		goliathPriorityMap.put(UnitType.Protoss_Arbiter,				150);
		goliathPriorityMap.put(UnitType.Protoss_Carrier,				140);
		goliathPriorityMap.put(UnitType.Protoss_Scout,					130);
		goliathPriorityMap.put(UnitType.Protoss_High_Templar,			120);
		goliathPriorityMap.put(UnitType.Protoss_Dark_Templar,			110);
		goliathPriorityMap.put(UnitType.Protoss_Reaver,					100);
		goliathPriorityMap.put(UnitType.Protoss_Shuttle,				90);
		goliathPriorityMap.put(UnitType.Protoss_Archon,					80);
		goliathPriorityMap.put(UnitType.Protoss_Zealot,					70);
		goliathPriorityMap.put(UnitType.Protoss_Dragoon,				60);
		goliathPriorityMap.put(UnitType.Protoss_Probe,					50);
		goliathPriorityMap.put(UnitType.Protoss_Interceptor,			40);
		goliathPriorityMap.put(UnitType.Protoss_Dark_Archon,			30);
		goliathPriorityMap.put(UnitType.Protoss_Corsair,				20);
		
		goliathPriorityMap.put(UnitType.Zerg_Guardian,					200);
		goliathPriorityMap.put(UnitType.Zerg_Infested_Terran,			190);
		goliathPriorityMap.put(UnitType.Zerg_Zergling,					180);
		goliathPriorityMap.put(UnitType.Zerg_Guardian,					170);
		goliathPriorityMap.put(UnitType.Zerg_Mutalisk,					160);
		goliathPriorityMap.put(UnitType.Zerg_Defiler,					150);
		goliathPriorityMap.put(UnitType.Zerg_Lurker,					140);
		goliathPriorityMap.put(UnitType.Zerg_Scourge,					130);
		goliathPriorityMap.put(UnitType.Zerg_Hydralisk,					120);
		goliathPriorityMap.put(UnitType.Zerg_Ultralisk,					110);
		goliathPriorityMap.put(UnitType.Zerg_Queen,						100);
		goliathPriorityMap.put(UnitType.Zerg_Devourer,					90);
		goliathPriorityMap.put(UnitType.Zerg_Drone,						80);
		goliathPriorityMap.put(UnitType.Zerg_Overlord,					70);
		goliathPriorityMap.put(UnitType.Zerg_Lurker_Egg,				60);
		goliathPriorityMap.put(UnitType.Zerg_Larva,						50);
		goliathPriorityMap.put(UnitType.Zerg_Egg,						40);
	}

}
