package prebot.micro.constant;


import java.util.HashMap;
import java.util.Map;

import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.common.LagObserver;
import prebot.common.main.Prebot;
import prebot.strategy.InformationManager;

public class MicroConfig {

	public static enum MainSquadMode {
		DEFENSE(0.1, false),
		NORMAL(0.8, false),
		ATTCK(0.2, true),
		FD_PRESS(0.0, true),
		SPEED_ATTCK(0.0, true),
		NO_MERCY(0.8, true);


		private MainSquadMode(double maxGuerillaVultureRatio, boolean isAttackMode) {
			this.maxGuerillaVultureRatio = maxGuerillaVultureRatio;
			this.isAttackMode = isAttackMode;
		}
		
		public double maxGuerillaVultureRatio;
		public boolean isAttackMode;
	}
	
	public static enum SquadInfo {
		IDLE("IDLE", 0, 0),
		EARLY_DEFENSE("EARLY_DEFENSE", 1, 30),
		MAIN_ATTACK("MAIN_ATTACK", 2, 150),
		WATCHER("WATCHER", 3, 150),
		CHECKER("CHECKER", 4, 0),
		GUERILLA_("GUERILLA_", 5, 150),
		DEFENSE_("DEFENSE_", 6, 150),
		SCV_SCOUT("SCV_SCOUT", 7, 150),
		SPECIAL("SPECIAL", 8, 500),
		AIR_FORCE("AIR_FORCE", 9, 150),
		BUILDING("BUILDING", 10, 500);
		
		private SquadInfo(String squadName, int priority, int additionalRadius) {
			this.squadName = squadName;
			this.priority = priority;
			this.squadRadius = additionalRadius;
		}
		
		public String squadName;
		public int priority;
		public int squadRadius;
	}

	public static class Angles {
		public static final int[] NARROW = { 0, -5, +5, -10, +10, -15, +15 };
		public static final int[] WIDE = { 0, -10, +10, -20, +20, -30, +30, -40, +40, -50, +50, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
		public static final int[] AIR_FORCE_DRIVE = { 0, -10, +10, -20, +20, -30, +30, -40, +40, -50, +50, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
		public static final int[] AIR_FORCE_RETREAT_LEFT = { -80, -70, -60, -50, -40, -30, -20, -10, 0, +10, +20, +30, +40, +50, +60, +70, +80 };
		public static final int[] AIR_FORCE_RETREAT_RIGHT = { +80, +70, +60, +50, +40, +30, +20, +10, 0, -10, -20, -30, -40, -50, -60, -70, -80 };
		public static final int[] AROUND_360 = { 0, 45, 90, 135, 180, 225, 270, 315 };
	}

	public static class Flee {
		public static final int RISK_RADIUS_DEFAULT = 150;
		public static final int RISK_RADIUS_VULTURE = 190;
		public static final int RISK_RADIUS_VUTRURE_SPEED = 220;
		public static final int RISK_RADIUS_TANK = 150;
		public static final int RISK_RADIUS_GOLIATH = 150;
		public static final int RISK_RADIUS_WRAITH = 220;
		public static final int RISK_RADIUS_VESSEL = 150;
	}
	
//	public static class FreeKitingRadius {
//		public static final int MARINE = (int) (UnitType.Terran_Marine.topSpeed() * UnitType.Terran_Marine.groundWeapon().damageCooldown() * 0.8);
//		public static final int VULTURE = (int) (UnitType.Terran_Vulture.topSpeed() * UnitType.Terran_Vulture.groundWeapon().damageCooldown() * 0.8);
//		public static final int GOLIATH = (int) (UnitType.Terran_Goliath.topSpeed() * UnitType.Terran_Goliath.groundWeapon().damageCooldown() * 0.8);
//		public static final int TANK = (int) (UnitType.Terran_Siege_Tank_Tank_Mode.topSpeed() * UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().damageCooldown() * 0.8);
//	}
	
	/// 각각의 Refinery 마다 투입할 일꾼 최대 숫자
	public static final int WORKERS_PER_REFINERY = 3;

	public static class Vulture {
		public static final int GEURILLA_ENEMY_RADIUS = 400;
		public static final int GEURILLA_EXTRA_ENEMY_POWER = 150;
		public static final int GEURILLA_INTERVAL_FRAME = 35 * 24; // 게릴라지역이 선정되고 일정시간이 지나야 다시 게릴라 지역으로 선정가능
		public static final int GEURILLA_FREE_VULTURE_COUNT = 15; // GEURILLA_INTERVAL_FRAME 대기 없이 게릴라벌처를 할당가능한 숫자
		public static final int CHECKER_IGNORE_FRAME = 30 * 24; // 10초
		public static final int CHECKER_INTERVAL_FRAME = 30 * 24; // 30초
		public static final int CHECKER_RETIRE_FRAME = 20 * 24; // 20초
		
		public static final int CHECKER_MAX_COUNT = 3;

		public static final int IGNORE_MOVE_FRAME = 5 * 24; // 5초
		
		// 마인 매설 관련 상수
		public static final int MINE_EXACT_RADIUS = 10;
		public static final int MINE_SPREAD_RADIUS = 250;
		public static final int MINE_ENEMY_RADIUS = 50;
		public static final int MINE_ENEMY_TARGET_DISTANCE = 500;
		public static final int MINE_MAX_NUM = 150;
		
		public static final int VULTURE_JOIN_SQUAD_FRAME = 13 * 24;
		public static final int VULTURE_JOIN_SQUAD_FRAME_TERRAN = 18 * 24;
		
		public static int getVultureJoinSquadFrame(Race race) {
			if (race == Race.Terran) {
				return VULTURE_JOIN_SQUAD_FRAME_TERRAN;
			} else {
				return VULTURE_JOIN_SQUAD_FRAME;
			}
		}
	}
	
	public static class Tank {
		public static final int SIEGE_MODE_MIN_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange(); // 64
		public static final int SIEGE_MODE_MAX_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange(); // 384
		public static final int SIEGE_MODE_SIGHT = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange(); // 320
		public static final int TANK_MODE_RANGE = UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange(); // 224

		public static final int SIEGE_MODE_INNER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().innerSplashRadius();
		public static final int SIEGE_MODE_MEDIAN_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().medianSplashRadius();
		public static final int SIEGE_MODE_OUTER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().outerSplashRadius();
		
		public static final int SIEGE_LINK_DISTANCE = 250;
		public static final int OTHER_UNIT_COUNT_SIEGE_AGAINST_MELEE = 6;
	}
	
	public static class Common {
		public static final boolean versusMechanicSet() {
			if (InformationManager.Instance().enemyRace != Race.Terran) {
				return false;
			} else {
				return true;
			}
		}
		
		public static final int DEFENSE_SECONDCHOKE_SIZE = 9;
		public static final int DEFENSE_READY_TO_ATTACK_SIZE = 14;
		public static final int DEFENSE_READY_TO_ATTACK_SIZE_TERRAN = 2;
		
		public static final double BACKOFF_DIST_SIEGE_TANK = 100.0;
		public static final double BACKOFF_DIST_DEF_TOWER = 120.0;
		public static final double BACKOFF_DIST_RANGE_ENEMY = 180.0;
		
		public static final int TANK_SQUAD_SIZE = 2;
		public static final int MAIN_SQUAD_COVERAGE = 150;
		public static final int MAIN_SQUAD_COVERAGE2 = 250;
		public static final int ARRIVE_DECISION_RANGE = 100;
		
		public static final int NO_UNIT_FRAME = 15 * 24;
		public static final int NO_SIEGE_FRAME = 120 * 24;
		
		public static int NO_UNIT_FRAME(UnitType unitType) {
			int noUnitFrames = NO_UNIT_FRAME;
			if (unitType == UnitType.Terran_Siege_Tank_Tank_Mode || unitType == UnitType.Terran_Siege_Tank_Siege_Mode) {
				noUnitFrames = NO_SIEGE_FRAME;
			}
			return noUnitFrames;
		}
	}
	 
	public static class RiskRadius {
		public static int getRiskRadius(UnitType unitType) {
			Integer radius = RISK_RADIUS_MAP.get(unitType);
			if (radius == null) {
//				MyBotModule.Broodwar.sendText("radius is null");
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
		public static final int LATENCY = Prebot.Broodwar.getLatency(); // LAN (UDP) : 5
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
				} else if (!vultureSpeedUpgrade && Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) > 0) {
					vultureSpeedUpgrade = true;
//					MyBotModule.Broodwar.sendText("Ion Thrusters Upgraded!");
					return 3.0 * 24.0;
				}
			} else if (upgrade == UpgradeType.Charon_Boosters) {
				if (goliathAttkRangeUpgrade) {
					return 3.0 * 24.0; // 골리앗 대공 사정거리 업그레이드 : 5(→8)
				} else if (!goliathAttkRangeUpgrade && Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) > 0) {
					goliathAttkRangeUpgrade = true;
//					MyBotModule.Broodwar.sendText("Charon Boosters Upgraded!");
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
//					MyBotModule.Broodwar.sendText("Siege Mode Upgraded!");
					return true;
				}
			} else if (tech == TechType.Spider_Mines) {
				if (spiderMineUpgrade) {
					return true;
				} else if (InformationManager.Instance().selfPlayer.hasResearched(TechType.Spider_Mines)) {
					spiderMineUpgrade = true;
//					MyBotModule.Broodwar.sendText("Spider Mines Upgraded!");
					return true;
				} 
			}
			return false;
		}
	}
	
	public static class FleeAngle {
		
		public static Integer[] getLagImproveAngle(Integer[] angle) {
			if (LagObserver.groupsize() >= 3) {
				if (WIDE_ANGLE.equals(angle)) {
					return WIDE_ANGLE_LOWER;
				} else if (NARROW_ANGLE.equals(angle)) {
					return NARROW_ANGLE_LOWER;
				} else if (EIGHT_360_ANGLE.equals(angle)) {
					return EIGHT_360_ANGLE_LOWER;
				}
			} else if (LagObserver.groupsize() >= 5) {
				if (WIDE_ANGLE.equals(angle)) {
					return WIDE_ANGLE_LOWEST;
				} else if (NARROW_ANGLE.equals(angle)) {
					return NARROW_ANGLE_LOWEST;
				} else if (EIGHT_360_ANGLE.equals(angle)) {
					return EIGHT_360_ANGLE_LOWEST;
				}
			}
			
			return angle;
		}
		
		public static Integer[] getFleeAngle(UnitType fleeUnit) {
			Integer[] angle = FLEE_ANGLE_MAP.get(fleeUnit);
			if (angle == null) {
//				MyBotModule.Broodwar.sendText("flee angle is null");
				angle = WIDE_ANGLE;
			}
			
			return getLagImproveAngle(angle);
		}

		public static final Integer[] NARROW_ANGLE = { -5, +5, -10, +10, -15, +15 };
		public static final Integer[] WIDE_ANGLE = { -10, +10, -20, +20, -30, +30, -40, +40, -50, +50, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
		public static final Integer[] EIGHT_360_ANGLE = { 45, 90, 135, 180, 225, 270, 315, 360 };
		
		private static final Integer[] NARROW_ANGLE_LOWER = { -10, +10 };
		private static final Integer[] WIDE_ANGLE_LOWER = { -10, +10, -30, +30, -50, +50, -70, +70 };
		private static final Integer[] EIGHT_360_ANGLE_LOWER = { 90, 180, 270, 360 };
		
		private static final Integer[] NARROW_ANGLE_LOWEST = { -10, +10 };
		private static final Integer[] WIDE_ANGLE_LOWEST = { -10, +10, -30, +30 };
		private static final Integer[] EIGHT_360_ANGLE_LOWEST = { 120, 240, 360 };

		private static final Map<UnitType, Integer[]> FLEE_ANGLE_MAP = new HashMap<>();
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
