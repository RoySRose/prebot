package prebot.strategy.constant;

import bwapi.UnitType;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap;

public class TimeMapForZerg {
	
	public static BuildTimeMap ZERG_5DRONE() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Spawning_Pool, 0, 45)
		.put(UnitType.Zerg_Lair, 10, 0);
	}
	
	public static BuildTimeMap ZERG_9DRONE() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Spawning_Pool, 1, 15)
		.put(UnitType.Zerg_Overlord, 1, 35)
		.put(UnitType.Zerg_Hatchery, 2, 25)
		.put(UnitType.Zerg_Lair, 3, 40);
	}
	
	public static BuildTimeMap ZERG_9DRONE_GAS() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Spawning_Pool, 1, 15)
		.put(UnitType.Zerg_Extractor, 1, 20)
		.put(UnitType.Zerg_Overlord, 1, 30)
		.put(UnitType.Zerg_Lair, 2, 40);
	}
	
	public static BuildTimeMap ZERG_9DRONE_GAS_DOUBLE() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Spawning_Pool, 1, 15)
		.put(UnitType.Zerg_Extractor, 1, 20)
		.put(UnitType.Zerg_Overlord, 1, 30)
		.put(UnitType.Zerg_Hatchery, 3, 20)
		.put(UnitType.Zerg_Lair, 3, 50);
	}
	
	public static BuildTimeMap ZERG_OVERPOOL() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Overlord, 0, 55)
		.put(UnitType.Zerg_Spawning_Pool, 1, 20)
		.put(UnitType.Zerg_Hatchery, 2, 5)
		.put(UnitType.Zerg_Lair, 3, 30);
	}
	
	public static BuildTimeMap ZERG_OVERPOOL_GAS() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Overlord, 0, 55)
		.put(UnitType.Zerg_Spawning_Pool, 1, 20)
		.put(UnitType.Zerg_Extractor, 1, 30)
		.put(UnitType.Zerg_Lair, 2, 45);
	}
	public static BuildTimeMap ZERG_OVERPOOL_GAS_DOUBLE() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Overlord, 0, 55)
		.put(UnitType.Zerg_Spawning_Pool, 1, 20)
		.put(UnitType.Zerg_Extractor, 1, 30)
		.put(UnitType.Zerg_Hatchery, 3, 0)
		.put(UnitType.Zerg_Lair, 3, 25);
	}
	
	public static BuildTimeMap ZERG_2HAT_GAS() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Overlord, 0, 55)
		.put(UnitType.Zerg_Hatchery, 1, 50)
		.put(UnitType.Zerg_Spawning_Pool, 2, 5)
		.put(UnitType.Zerg_Extractor, 2, 20)
		.put(UnitType.Zerg_Lair, 3, 5);
	}
	
	public static BuildTimeMap ZERG_3HAT() {
		return new BuildTimeMap()
		.put(UnitType.Zerg_Overlord, 0, 55)
		.put(UnitType.Zerg_Hatchery, 1, 50)
		.put(UnitType.Zerg_Spawning_Pool, 2, 5)
		.put(UnitType.Zerg_Hatchery, 2, 50)
		.put(UnitType.Zerg_Extractor, 3, 0)
		.put(UnitType.Zerg_Lair, 3, 50);
	}
	
}
