package prebot.common.code;

import bwapi.UnitType;

/// 마이크로 컨트롤 세부 조절
public class ConfigForMicro {
	public static class Angles {
		public static final int[] NARROW = { 0, -5, +5, -10, +10, -15, +15 };
		public static final int[] WIDE = { 0, -10, +10, -20, +20, -30, +30, -40, +40, -50, +50, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
		public static final int[] AROUND_360 = { 0, 45, 90, 135, 180, 225, 270, 315 };
	}
	
	public static class Flee {
		public static final int BACKOFF_DIST = 64;
	}
	
	public static class Tank {
		public static final int SIEGE_MODE_MIN_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange(); // 64
		public static final int SIEGE_MODE_MAX_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange(); // 384
		public static final int SIEGE_MODE_SIGHT = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange(); // 320
		public static final int TANK_MODE_RANGE = UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange(); // 224

		public static final int SIEGE_MODE_INNER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().innerSplashRadius();
		public static final int SIEGE_MODE_MEDIAN_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().medianSplashRadius();
		public static final int SIEGE_MODE_OUTER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().outerSplashRadius();
	}
	
}
