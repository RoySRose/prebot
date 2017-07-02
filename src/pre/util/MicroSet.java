package pre.util;

import java.util.HashMap;
import java.util.Map;

import bwapi.UnitType;
import bwapi.UpgradeType;
import pre.main.MyBotModule;

public class MicroSet {
	
	public static class Network {
		public static final int LATENCY = MyBotModule.Broodwar.getLatency();
	}
	
	public static class Upgrade {
		private static boolean vultureSpeedUpgrade = false;
		private static boolean goliathAttkRangeUpgrade = false;
		
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
		
		static {
			FLEE_ANGLE_MAP.put(UnitType.Terran_Marine, WIDE_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Vulture, WIDE_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Siege_Tank_Tank_Mode, WIDE_ANGLE);
			FLEE_ANGLE_MAP.put(UnitType.Terran_Goliath, NARROW_ANGLE);
		}
	}
	
	
}
