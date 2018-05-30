package prebot.micro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.UpgradeUtils;
import prebot.micro.constant.MicroConfig.Flee;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.WatcherPredictResult;

/** @author insaneojw
 *  */
public class WatcherPredictor {
	
	public static int fleeStartFrame = 0;
	
	/** 벌처의 후퇴*/
//	public static WatcherCombatPredictResult predict(List<Unit> vultureList, List<Unit> enemyUnitList) {
//		int vulturePower = powerOfWatchers(vultureList);
//		int enemyPower = powerOfEnemies(enemyUnitList);
//		return getResult(vulturePower, enemyPower);
//	}
	
	public static WatcherPredictResult predictByUnitInfo(List<Unit> vultures, List<UnitInfo> euiList) {
		if (TimeUtils.elapsedSeconds(fleeStartFrame) <= Flee.WATCHER_FLEE_SECONDS) {
			return WatcherPredictResult.BACK;
		}
		
		int vulturePower = powerOfWatchers(vultures);
		int enemyPower = powerOfEnemiesByUnitInfo(euiList);
		
		if (vulturePower > enemyPower) {
			return WatcherPredictResult.ATTACK;
		} else {
			fleeStartFrame = TimeUtils.elapsedFrames();
			return WatcherPredictResult.BACK;
		}
	}

	// 벌처 한기 최대 점수 : 70점 (업그레이드시 100점)
	public static int powerOfWatchers(List<Unit> vultureList) {
		int totalPower = 0;
		for (Unit vulture : vultureList) {
			totalPower += VULTURE_POWER;
			if (UpgradeUtils.isUpgraded(UpgradeType.Ion_Thrusters)) {
				totalPower += BONUS_ION_THRUSTERS;
			}
			totalPower += vulture.getHitPoints() * 40.0  / 75.0; // 50
		}
		return totalPower;
	}
	
	// 저글링 한기 최대 점수 : 15점(업그레이드시 20점)
	// 히드라 한기 최대 점수 : 150점(업그레이드시 200점)
	// 질럿 한기 최대 점수 : 20점(업그레이드시 30점)
	// 드라군 한기 최대 점수 : 300점(업그레이드시 330점)
	public static int powerOfEnemies(List<Unit> enemyUnitList) {
		int enemyTotalPower = 0;
		for (Unit enemyUnit : enemyUnitList) {
			if (!enemyUnit.isCompleted()) {
				continue;
			}
			enemyTotalPower += powerOfUnit(enemyUnit.getType(), enemyUnit.getHitPoints() + enemyUnit.getShields(), enemyUnit);
		}
		return enemyTotalPower;
	}
	
	public static int powerOfEnemiesByUnitInfo(List<UnitInfo> euiList) {
		int enemyTotalPower = 0;
		for (UnitInfo eui : euiList) {
			UnitType unitType = UnitType.Unknown;
			int hitPointsAndShields = 0;

			Unit enemyUnit = UnitUtils.unitInSight(eui);
			if (enemyUnit != null) {
				if (!enemyUnit.isCompleted()) {
					continue;
				}
				unitType = enemyUnit.getType();
				hitPointsAndShields = enemyUnit.getHitPoints() + enemyUnit.getShields();
			} else {
				unitType = eui.getType();
				hitPointsAndShields = eui.getType().maxHitPoints() + eui.getType().maxShields();
			}
			enemyTotalPower += powerOfUnit(unitType, hitPointsAndShields, enemyUnit);
		}
		return enemyTotalPower;
	}

	private static int powerOfUnit(UnitType unitType, int hitPointsAndShields, Unit enemyUnit) {
		Integer enemyPower = VULTURE_TARGET.get(unitType);
		if (enemyPower == null) {
			return 0;
		}

		if (InformationManager.Instance().enemyRace == Race.Zerg) {
			if (unitType == UnitType.Zerg_Zergling) {
				if (UpgradeUtils.isEnemyUpgraded(UpgradeType.Anabolic_Synthesis)) {
					enemyPower += BONUS_ANABOLIC_SYNTHESIS;
				}
			} else if (unitType == UnitType.Zerg_Hydralisk) {
				if (UpgradeUtils.isEnemyUpgraded(UpgradeType.Muscular_Augments)) {
					enemyPower += BONUS_MUSCULAR_AUGMENTS;
				}
				if (UpgradeUtils.isEnemyUpgraded(UpgradeType.Grooved_Spines)) {
					enemyPower += BONUS_GROOVED_SPINES;
				}
				enemyPower += hitPointsAndShields; // 80
			} else if (unitType == UnitType.Zerg_Lurker) {
				if (enemyUnit == null || enemyUnit.isBurrowed()) {
					enemyPower += BONUS_LURKER_BURROWED;
				}
			}
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			if (unitType == UnitType.Protoss_Zealot) {
				if (UpgradeUtils.isEnemyUpgraded(UpgradeType.Leg_Enhancements)) {
					enemyPower += BONUS_LEG_ENHANCEMENTS;
				}
			} else if (unitType == UnitType.Protoss_Dragoon) {
				if (UpgradeUtils.isEnemyUpgraded(UpgradeType.Singularity_Charge)) {
					enemyPower += BONUS_SINGULARITY_CHARGE;
				}
				enemyPower += hitPointsAndShields; // 80 + 100
			}
			
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
			// TODO
		}
		
		return enemyPower;
	}
	
	private static final int VULTURE_POWER = 30;
	private static final int BONUS_ION_THRUSTERS = 30;
	
	private static final int BONUS_ANABOLIC_SYNTHESIS = 5;
	private static final int BONUS_MUSCULAR_AUGMENTS = 25;
	private static final int BONUS_GROOVED_SPINES = 25;
	private static final int BONUS_LURKER_BURROWED = 300;
	
	private static final int BONUS_LEG_ENHANCEMENTS = 10;
	private static final int BONUS_SINGULARITY_CHARGE = 30;
	
	private static final Map<UnitType, Integer> VULTURE_TARGET = new HashMap<>();
	
	static {
		VULTURE_TARGET.put(UnitType.Zerg_Larva, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Lurker_Egg, 0);
		VULTURE_TARGET.put(UnitType.Zerg_Drone, 1);
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
		
		VULTURE_TARGET.put(UnitType.Protoss_High_Templar, 0); // 하템 암살해야됨
		VULTURE_TARGET.put(UnitType.Protoss_Dark_Archon, 0);
		VULTURE_TARGET.put(UnitType.Protoss_Probe, 1);
		VULTURE_TARGET.put(UnitType.Protoss_Zealot, 20);
		VULTURE_TARGET.put(UnitType.Protoss_Archon, 50);
		VULTURE_TARGET.put(UnitType.Protoss_Dark_Templar, 100);
		VULTURE_TARGET.put(UnitType.Protoss_Dragoon, 120); // hitpoint 점수 사용 *
		VULTURE_TARGET.put(UnitType.Protoss_Scout, 300);
		VULTURE_TARGET.put(UnitType.Protoss_Carrier, 400);
		VULTURE_TARGET.put(UnitType.Protoss_Reaver, 400);
		VULTURE_TARGET.put(UnitType.Protoss_Photon_Cannon, 600);
		
		
		VULTURE_TARGET.put(UnitType.Terran_Marine, 30); // 하템 암살해야됨
		VULTURE_TARGET.put(UnitType.Terran_Medic, 30);
		VULTURE_TARGET.put(UnitType.Terran_Firebat, 30);
		VULTURE_TARGET.put(UnitType.Terran_Siege_Tank_Tank_Mode, 400);
		VULTURE_TARGET.put(UnitType.Terran_Siege_Tank_Siege_Mode, 400);
		VULTURE_TARGET.put(UnitType.Terran_Vulture, 100);
		VULTURE_TARGET.put(UnitType.Terran_Goliath, 400);
		VULTURE_TARGET.put(UnitType.Terran_Bunker, 400);
	}

}
