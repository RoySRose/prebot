package pre.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import pre.main.MyBotModule;

public class CombatExpectation {

	private static final int VULTURE_POWER = 30;
	private static final int BONUS_ION_THRUSTERS = 30;
	
	private static final int BONUS_ANABOLIC_SYNTHESIS = 5;
	private static final int BONUS_MUSCULAR_AUGMENTS = 25;
	private static final int BONUS_GROOVED_SPINES = 25;
	private static final int BONUS_LURKER_BURROWED = 300;
	
	private static final int BONUS_LEG_ENHANCEMENTS = 10;
	private static final int BONUS_SINGULARITY_CHARGE = 30;

	public static boolean expectVultureVictory(List<Unit> vultures, List<Unit> targets) {
		// 벌처 한기 최대 점수 : 70점 (업그레이드시 100점)
		int vulturePower = 0;
		for (Unit vulture : vultures) {
			vulturePower += VULTURE_POWER;
			if (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) > 0) {
				vulturePower += BONUS_ION_THRUSTERS;
			}
			vulturePower += vulture.getHitPoints() * 40.0  / 75.0; // 50
		}

		// 저글링 한기 최대 점수 : 15점(업그레이드시 20점)
		// 히드라 한기 최대 점수 : 150점(업그레이드시 200점)
		// 질럿 한기 최대 점수 : 20점(업그레이드시 30점)
		// 드라군 한기 최대 점수 : 300점(업그레이드시 330점)
		int enemyPower = 0;
		for (Unit target : targets) {
			if (VULTURE_TARGET.get(target.getType()) == null) {
				continue;
			}
			enemyPower += VULTURE_TARGET.get(target.getType());
			
			if (target.getType() == UnitType.Zerg_Zergling && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Anabolic_Synthesis) > 0) {
				enemyPower += BONUS_ANABOLIC_SYNTHESIS;
			} else if (target.getType() == UnitType.Zerg_Hydralisk) {
				if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Muscular_Augments) > 0) {
					enemyPower += BONUS_MUSCULAR_AUGMENTS;
				}
				if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
					enemyPower += BONUS_GROOVED_SPINES;
				}
				enemyPower += target.getHitPoints(); // 80
			} else if (target.getType() == UnitType.Zerg_Lurker && target.isBurrowed()) {
				enemyPower += BONUS_LURKER_BURROWED;
			} else if (target.getType() == UnitType.Protoss_Zealot && MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Leg_Enhancements) > 0) {
				enemyPower += BONUS_LEG_ENHANCEMENTS;
			} else if (target.getType() == UnitType.Protoss_Dragoon) {
				if (MyBotModule.Broodwar.enemy().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
					enemyPower += BONUS_SINGULARITY_CHARGE;
				}
				enemyPower += target.getShields() + target.getHitPoints(); // 80 + 100
			}
		}
		
		boolean winExpected = vulturePower >= enemyPower;
		
//		System.out.println("vulturePower : " + vulturePower);
//		System.out.println("enemyPower   : " + enemyPower);
//		System.out.println("winExpected  : " + winExpected);
		
		return winExpected;
	}
	

	private static final Map<UnitType, Integer> VULTURE_TARGET = new HashMap<>();
	
	static {
		VULTURE_TARGET.put(UnitType.Zerg_Larva, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Drone, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Broodling, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker, 10);
		VULTURE_TARGET.put(UnitType.Zerg_Zergling, 15);
		VULTURE_TARGET.put(UnitType.Zerg_Infested_Terran, 15);
		VULTURE_TARGET.put(UnitType.Zerg_Hydralisk, 70); // hitpoint 점수 사용 *
		VULTURE_TARGET.put(UnitType.Zerg_Ultralisk, 100);
		VULTURE_TARGET.put(UnitType.Zerg_Defiler, 100);
		VULTURE_TARGET.put(UnitType.Zerg_Mutalisk, 300);
		VULTURE_TARGET.put(UnitType.Zerg_Guardian, 400);
		VULTURE_TARGET.put(UnitType.Zerg_Sunken_Colony, 800);
		
		
		VULTURE_TARGET.put(UnitType.Protoss_High_Templar, 0);
		VULTURE_TARGET.put(UnitType.Protoss_Dark_Archon, 0);
		VULTURE_TARGET.put(UnitType.Protoss_Probe, 10);
		VULTURE_TARGET.put(UnitType.Protoss_Zealot, 20);
		VULTURE_TARGET.put(UnitType.Protoss_Archon, 50);
		VULTURE_TARGET.put(UnitType.Protoss_Dark_Templar, 100);
		VULTURE_TARGET.put(UnitType.Protoss_Dragoon, 120); // hitpoint 점수 사용 *
		VULTURE_TARGET.put(UnitType.Protoss_Scout, 300);
		VULTURE_TARGET.put(UnitType.Protoss_Carrier, 400);
		VULTURE_TARGET.put(UnitType.Protoss_Reaver, 400);
		VULTURE_TARGET.put(UnitType.Protoss_Photon_Cannon, 600);
	}
}
