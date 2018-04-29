package prebot.common.code;

/// 마이크로 컨트롤 세부 조절
public class ConfigForMicro {
	public static class Angles {
		public static final int[] NARROW = { 0, -5, +5, -10, +10, -15, +15 };
		public static final int[] WIDE = { 0, -10, +10, -20, +20, -30, +30, -40, +40, -50, +50, -60, +60, -70, +70, -80, +80, -90, +90, -100, +100 };
		public static final int[] AROUND_360 = { 0, 45, 90, 135, 180, 225, 270, 315 };
	}

	public static class Flee {
		public static final int BACKOFF_DIST = 64;
		public static final int WATCHER_FLEE_SECONDS = 8;

		public static final int RISK_RADIUS_DEFAULT = 150;
		public static final int RISK_RADIUS_VULTURE = 190;
		public static final int RISK_RADIUS_VUTRURE_SPEED = 220;
		public static final int RISK_RADIUS_TANK = 150;
		public static final int RISK_RADIUS_GOLIATH = 150;
		public static final int RISK_RADIUS_WRAITH = 220;
		public static final int RISK_RADIUS_VESSEL = 150;
	}

}
