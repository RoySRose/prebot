package prebot.strategy.constant;

import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.strategy.constant.EnemyStrategyOptions.DefaultTimeMap;

public class TimeMapForProtoss {
	
	public static DefaultTimeMap PROTOSS_1GATE_CORE() {
		return new DefaultTimeMap()
		.put(UnitType.Protoss_Gateway, 1, 20)
		.put(UnitType.Protoss_Assimilator, 1, 30)
		.put(UnitType.Protoss_Cybernetics_Core, 2, 00);
	}
	
	public static DefaultTimeMap PROTOSS_2GATE() {
		return new DefaultTimeMap()
		.put(UnitType.Protoss_Gateway, 1, 20)
		.put(UnitType.Protoss_Gateway, 1, 45)
		.put(UnitType.Protoss_Assimilator, 3, 00)
		.put(UnitType.Protoss_Cybernetics_Core, 3, 20);
	}
	
	public static DefaultTimeMap PROTOSS_DOUBLE() {
		return new DefaultTimeMap()
		.put(UnitType.Protoss_Nexus, 1, 55)
		.put(UnitType.Protoss_Gateway, 2, 5)
		.put(UnitType.Protoss_Assimilator, 2, 20)
		.put(UnitType.Protoss_Gateway, 2, 45)
		.put(UnitType.Protoss_Cybernetics_Core, 2, 55);
	}
	
	public static DefaultTimeMap PROTOSS_FORGE_DEFENSE() {
		return new DefaultTimeMap();
	}
	
	public static DefaultTimeMap PROTOSS_FORGE_CANNON_RUSH() {
		return new DefaultTimeMap()
		.put(UnitType.Protoss_Forge, 1, 40)
		.put(UnitType.Protoss_Photon_Cannon, 2, 15)
		.put(UnitType.Protoss_Cybernetics_Core, 4, 20);
	}
	
	public static DefaultTimeMap PROTOSS_FORGE_DOUBLE() {
		return new DefaultTimeMap()
		.put(UnitType.Protoss_Forge, 1, 40)
		.put(UnitType.Protoss_Photon_Cannon, 2, 15)
		.put(UnitType.Protoss_Nexus, 2, 30)
		.put(UnitType.Protoss_Cybernetics_Core, 3, 50);
	}
	
	public static DefaultTimeMap PROTOSS_GATE_DOUBLE() {
		return new DefaultTimeMap()
		.put(UnitType.Protoss_Forge, 1, 40)
		.put(UnitType.Protoss_Gateway, 2, 15)
		.put(UnitType.Protoss_Nexus, 2, 30)
		.put(UnitType.Protoss_Cybernetics_Core, 3, 50);
	}

	public static DefaultTimeMap PROTOSS_FAST_DARK() {
		return new DefaultTimeMap()
		.putAll(PROTOSS_1GATE_CORE())
		.put(UnitType.Protoss_Citadel_of_Adun, 2, 40)
		.put(UnitType.Protoss_Templar_Archives, 3, 20);
	}

	public static DefaultTimeMap PROTOSS_DARK_DROP() {
		return new DefaultTimeMap()
		.putAll(PROTOSS_1GATE_CORE())
		.put(UnitType.Protoss_Citadel_of_Adun, 2, 40)
		.put(UnitType.Protoss_Templar_Archives, 3, 20);
	}

	public static DefaultTimeMap PROTOSS_FAST_DRAGOON() {
		return new DefaultTimeMap()
		.putAll(PROTOSS_1GATE_CORE())
		.put(UpgradeType.Singularity_Charge, 2, 50)
		.put(UnitType.Protoss_Gateway, 3, 10);
	}

	public static DefaultTimeMap PROTOSS_ROBOTICS_REAVER() {
		return new DefaultTimeMap()
		.put(UpgradeType.Singularity_Charge, 2, 50)
		.put(UnitType.Protoss_Robotics_Facility, 3, 40)
		.put(UnitType.Protoss_Gateway, 4, 0)
		.put(UnitType.Protoss_Observatory, 4, 40);
	}

	public static DefaultTimeMap PROTOSS_ROBOTICS_OB_DRAGOON() {
		return new DefaultTimeMap()
		.put(UpgradeType.Singularity_Charge, 2, 50)
		.put(UnitType.Protoss_Robotics_Facility, 3, 40)
		.put(UnitType.Protoss_Gateway, 4, 0)
		.put(UnitType.Protoss_Observatory, 4, 40);
	}

	public static DefaultTimeMap PROTOSS_STARGATE() {
		return new DefaultTimeMap();
	}

	public static DefaultTimeMap PROTOSS_DOUBLE_GROUND() {
		return new DefaultTimeMap();
	}

	public static DefaultTimeMap PROTOSS_DOUBLE_CARRIER() {
		return new DefaultTimeMap();
	}
	
}
