package prebot.common.code;

import bwapi.UnitType;

public class GameConstant {
	
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
