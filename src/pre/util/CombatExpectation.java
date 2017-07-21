package pre.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import pre.main.MyBotModule;
import pre.util.MicroSet.Upgrade;

public class CombatExpectation {
	
	private static int BONUS_ION_THRUSTERS = 30;
	
	private static int BONUS_ANABOLIC_SYNTHESIS = 5;
	private static int BONUS_MUSCULAR_AUGMENTS = 30;
	private static int BONUS_GROOVED_SPINES = 30;
	private static int BONUS_LURKER_BURROWED = 200;
	
	public static boolean expectVulture(List<Unit> vultures, List<Unit> targets) {
		
//		int vulturePower = 0;
//		for (Unit vulture : vultures) {
//			vulturePower += 25;
//			vulturePower += vulture.getHitPoints(); // 75
//		}
//		if (MicroSet.Upgrade.getUpgradeAdvantageAmount(UpgradeType.Ion_Thrusters) > 0) {
//			vulturePower += BONUS_ION_THRUSTERS * vultures.size();
//		}
//		
//		int zerglingBonus = 0;
//		int hydraBonus = 0;
//		if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Anabolic_Synthesis) > 0) {
//			zerglingBonus = BONUS_ANABOLIC_SYNTHESIS;
//		}
//		if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Muscular_Augments) > 0) {
//			hydraBonus = BONUS_MUSCULAR_AUGMENTS;
//		}
//		if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
//			hydraBonus = BONUS_GROOVED_SPINES;
//		}
//		
//		int enemyPower = 0;
//		for (Unit target : targets) {
//			enemyPower += VULTURE_TARGET.get(target.getType());
//			if (target.getType() == UnitType.Zerg_Zergling) {
//				enemyPower += zerglingBonus;
//			} else if (target.getType() == UnitType.Zerg_Hydralisk) {
//				enemyPower += hydraBonus;
//			} else if (target.getType() == UnitType.Zerg_Lurker && target.isBurrowed()) {
//				enemyPower += BONUS_LURKER_BURROWED;
//			}
//		}
		
		
		return false;
	}
	

	private static final Map<UnitType, Integer> VULTURE_TARGET = new HashMap<>();
	
	static {

		VULTURE_TARGET.put(UnitType.Zerg_Larva, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker_Egg, 0);
		
		VULTURE_TARGET.put(UnitType.Zerg_Drone, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Broodling, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker, 10);
		
		VULTURE_TARGET.put(UnitType.Zerg_Zergling, 20);
		VULTURE_TARGET.put(UnitType.Zerg_Infested_Terran, 20);
		VULTURE_TARGET.put(UnitType.Zerg_Ultralisk, 20);
		
		VULTURE_TARGET.put(UnitType.Zerg_Hydralisk, 100);
		VULTURE_TARGET.put(UnitType.Zerg_Defiler, 100);
	}
}
