

import java.util.HashMap;
import java.util.Map;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class MicroSet {

//	public static class FreeKitingRadius {
//		public static final int MARINE = (int) (UnitType.Terran_Marine.topSpeed() * UnitType.Terran_Marine.groundWeapon().damageCooldown() * 0.8);
//		public static final int VULTURE = (int) (UnitType.Terran_Vulture.topSpeed() * UnitType.Terran_Vulture.groundWeapon().damageCooldown() * 0.8);
//		public static final int GOLIATH = (int) (UnitType.Terran_Goliath.topSpeed() * UnitType.Terran_Goliath.groundWeapon().damageCooldown() * 0.8);
//		public static final int TANK = (int) (UnitType.Terran_Siege_Tank_Tank_Mode.topSpeed() * UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().damageCooldown() * 0.8);
//	}
	
	public static class Vulture {
		public static final int GEURILLA_RADIUS = 400;
		public static final int PELOTON_RADIUS = 400;
		public static final int GEURILLA_EXTRA_POWER = 150;
		public static final int GEURILLA_INTERVAL_FRAME = 20 * 24; // 20초
		public static final int CHECKER_INTERVAL_FRAME = 30 * 24; // 30초

		public static final int IGNORE_MOVE_FRAME = 3 * 24; // 3초

		// TODO 변동 값
	    public static int maxNumWatcher = 50;
		public static int maxNumChecker = 0;
		public static int spiderMineNumPerPosition = 1;
		
		// 마인 매설 관련 상수
		public static final int MINE_EXACT_RADIUS = 10;
		public static final int MINE_SPREAD_RADIUS = 500;

		public static final int MINE_BETWEEN_DIST = 50;
		public static final int MINE_REMOVE_TANK_DIST = 150;
		public static final int RESV_EXPIRE_FRAME = 24 * 3;
	}
	
	public static class Tank {
		public static final int SIEGE_MODE_MIN_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange();
		public static final int SIEGE_MODE_MAX_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
		public static final int TANK_MODE_RANGE = UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange();

		public static final int SIEGE_MODE_INNER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().innerSplashRadius();
		public static final int SIEGE_MODE_MEDIAN_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().medianSplashRadius();
		public static final int SIEGE_MODE_OUTER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().outerSplashRadius();
		
		public static final int SIEGE_LINK_DISTANCE = 250;
		public static final int SIEGE_ARRANGE_DISTANCE = 150;
	}
	
	public static class Common {
		public static final double BACKOFF_DIST_SIEGE_TANK = 100.0;
		public static final double BACKOFF_DIST_DEF_TOWER = 150.0;
		public static final double BACKOFF_DIST_RANGE_ENEMY = 200.0;
		
		public static final int NO_UNIT_FRAME = 15 * 24;
		public static final int TANK_SQUAD_SIZE = 2;
		public static final int MAIN_SQUAD_COVERAGE = 150;
		public static final int ARRIVE_DECISION_RANGE = 100;
	}
	 
	public static class RiskRadius {
		public static int getRiskRadius(UnitType unitType) {
			Integer radius = RISK_RADIUS_MAP.get(unitType);
			if (radius == null) {
				MyBotModule.Broodwar.sendText("radius is null");
				return RISK_RADIUS_DEFAULT;
			}
			return radius;
		}
		
		private static Map<UnitType, Integer> RISK_RADIUS_MAP = new HashMap<>();

		public static final int RISK_RADIUS_DEFAULT = 150;
		public static final int RISK_RADIUS_VULTURE = 190;
		public static final int RISK_RADIUS_VUTRURE_SPEED = 220;
		public static final int RISK_RADIUS_TANK = 150;
		public static final int RISK_RADIUS_GOLIATH = 150;
		public static final int RISK_RADIUS_WRAITH = 220;
		public static final int RISK_RADIUS_VESSEL = 150;
		
		static {
			RISK_RADIUS_MAP.put(UnitType.Terran_Marine, RISK_RADIUS_VULTURE);
			RISK_RADIUS_MAP.put(UnitType.Terran_Vulture, RISK_RADIUS_VUTRURE_SPEED);
			RISK_RADIUS_MAP.put(UnitType.Terran_Siege_Tank_Tank_Mode, RISK_RADIUS_TANK);
			RISK_RADIUS_MAP.put(UnitType.Terran_Goliath, RISK_RADIUS_GOLIATH);
			RISK_RADIUS_MAP.put(UnitType.Terran_Wraith, RISK_RADIUS_WRAITH);
			RISK_RADIUS_MAP.put(UnitType.Terran_Science_Vessel, RISK_RADIUS_VESSEL);
		}
	}
	
	public static class Network {
		public static final int LATENCY = MyBotModule.Broodwar.getLatency(); // LAN (UDP) : 5
	}
	
	public static class Upgrade {
		private static boolean vultureSpeedUpgrade = false;
		private static boolean goliathAttkRangeUpgrade = false;
		
		private static boolean siegeModeUpgrade = false;
		private static boolean spiderMineUpgrade = false;
		
		public static double getUpgradeAdvantageAmount(UpgradeType upgrade) {
			if (upgrade == UpgradeType.Ion_Thrusters) {
				if (vultureSpeedUpgrade) {
					return 3.0 * 24.0; // 벌처 업그레이드 된 스피드 안나온다. 걍 frame당 3pixel 더 간다고 치자.
				} else if (!vultureSpeedUpgrade && MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) > 0) {
					vultureSpeedUpgrade = true;
					MyBotModule.Broodwar.sendText("Ion Thrusters Upgraded!");
					return 3.0 * 24.0;
				}
			} else if (upgrade == UpgradeType.Charon_Boosters) {
				if (goliathAttkRangeUpgrade) {
					return 3.0 * 24.0; // 골리앗 대공 사정거리 업그레이드 : 5(→8)
				} else if (!goliathAttkRangeUpgrade && MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) > 0) {
					goliathAttkRangeUpgrade = true;
					MyBotModule.Broodwar.sendText("Charon Boosters Upgraded!");
					return 3.0 * 24.0;
				}
			}
			return 0.0;
		}
		
		public static boolean hasResearched(TechType tech) {
			if (tech == TechType.Tank_Siege_Mode) {
				if (siegeModeUpgrade) {
					return true;
				} else if (InformationManager.Instance().selfPlayer.hasResearched(TechType.Tank_Siege_Mode)) {
					siegeModeUpgrade = true;
					MyBotModule.Broodwar.sendText("Siege Mode Upgraded!");
					return true;
				}
			} else if (tech == TechType.Spider_Mines) {
				if (spiderMineUpgrade) {
					return true;
				} else if (InformationManager.Instance().selfPlayer.hasResearched(TechType.Spider_Mines)) {
					spiderMineUpgrade = true;
					MyBotModule.Broodwar.sendText("Spider Mines Upgraded!");
					return true;
				} 
			}
			return false;
		}
	}
	
	public static class FleeAngle {
		
		public static Integer[] getFleeAngle(UnitType fleeUnit) {
			Integer[] angle = FLEE_ANGLE_MAP.get(fleeUnit);
			if (angle == null) {
				MyBotModule.Broodwar.sendText("flee angle is null");
				return WIDE_ANGLE;
			}
			
			return angle;
		}

		public static final Map<UnitType, Integer[]> FLEE_ANGLE_MAP = new HashMap<>();
		public static final Integer[] NARROW_ANGLE = { -5, +5, -10, +10, -15, +15 };
		public static final Integer[] WIDE_ANGLE = { -10, +10, -20, +20, -30, +30, -40, +40, -50, +50, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
		public static final Integer[] EIGHT_360_ANGLE = {45, 90, 135, 180, 225, 270, 315, 360};
		
		static {
			FLEE_ANGLE_MAP.put(UnitType.Terran_Marine, WIDE_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Vulture, WIDE_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Siege_Tank_Tank_Mode, NARROW_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Goliath, NARROW_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Wraith, WIDE_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Science_Vessel, NARROW_ANGLE);
		}
	}
	
	
}
