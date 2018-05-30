package prebot.strategy.constant;

public class StrategyConfig {

	public static final int APPROX_DISTANCE_1280 = 1280;
	public static final int APPROX_DISTANCE_640 = 640;
	public static final int APPROX_DISTANCE_320 = 320;
	
	public static final int SCAN_DURATION = 240; // approximate time that a comsat scan provides vision
	public static final int IGNORE_ENEMY_UNITINFO_SECONDS = 15;
	
	// 기본 전략
	public static enum EnemyStrategy {
		ZERGBASIC(0, 2, 10, 1),
		ZERGBASIC_HYDRAWAVE(0, 2, 10, 1),
		ZERGBASIC_GIFTSET(1, 5, 2, 2),
		ZERGBASIC_HYDRAMUTAL(0, 2, 10, 1),
		ZERGBASIC_LINGHYDRA(1, 5, 6, 3),
		ZERGBASIC_LINGLURKER(1, 4, 3, 1),
		ZERGBASIC_LINGMUTAL(2, 4, 4, 1),
		ZERGBASIC_LINGULTRA(1, 2, 10, 1),
		ZERGBASIC_MUTAL(5, 6, 2, 1),
		ZERGBASIC_MUTALMANY(1, 1, 9, 3),
		ZERGBASIC_ULTRA(0, 1, 12, 3),
		PROTOSSBASIC(5, 6, 1, 1),
		PROTOSSBASIC_CARRIER(5, 5, 0, 2),
		PROTOSSBASIC_DOUBLEPHOTO(1, 2, 8, 3),
		TERRANBASIC(3, 3, 0, 1),
		TERRANBASIC_BIONIC(0, 2, 0, 1),
		TERRANBASIC_MECHANIC(10, 5, 0, 1),
		TERRANBASIC_MECHANICWITHWRAITH(5, 8, 1, 2),
		TERRANBASIC_MECHANICAFTER(0, 7, 1, 3),
		TERRANBASIC_BATTLECRUISER(0, 9, 1, 3),
		ATTACKISLAND(0, 0, 0, 0)
		// ,TERRANBASIC_REVERSERUSH( 0,  0,  0,  0)
		;

		public int vultureRatio;
		public int tankRatio;
		public int goliathRatio;
		public int weight;

		private EnemyStrategy(int vultureRatio, int tankRatio, int goliathRatio, int weight) {
			this.vultureRatio = vultureRatio;
			this.tankRatio = tankRatio;
			this.goliathRatio = goliathRatio;
			this.weight = weight;
		}
	}
	
	
	//예외 전략. 예외가 아닐때는 무조건 INIT 으로
	public static enum EnemyStrategyException {
		ZERGEXCEPTION_FASTLURKER(1, 4, 1, 2),
		ZERGEXCEPTION_GUARDIAN(0, 2, 10, 1),
		ZERGEXCEPTION_NONGBONG(0, 2, 10, 1),
		ZERGEXCEPTION_ONLYLING(7, 3, 2, 1),
		ZERGEXCEPTION_PREPARELURKER(0, 0, 0, 1),
		ZERGEXCEPTION_REVERSERUSH(0, 0, 0, 1),
		ZERGEXCEPTION_HIGHTECH(1, 3, 3, 2),
		PROTOSSEXCEPTION_CARRIERMANY(0, 0, 0, 1),
		PROTOSSEXCEPTION_DARK(9, 3, 0, 1),
		PROTOSSEXCEPTION_REAVER(9, 3, 0, 1),
		PROTOSSEXCEPTION_SCOUT(8, 3, 1, 1),
		PROTOSSEXCEPTION_SHUTTLE(7, 3, 2, 1),
		PROTOSSEXCEPTION_SHUTTLEMIX(7, 3, 2, 1),
		PROTOSSEXCEPTION_READYTOZEALOT(10, 2, 0, 1),
		PROTOSSEXCEPTION_ZEALOTPUSH(6, 1, 0, 1),
		PROTOSSEXCEPTION_READYTODRAGOON(6, 1, 0, 1),
		PROTOSSEXCEPTION_DRAGOONPUSH(1, 3, 0, 2),
		PROTOSSEXCEPTION_PHOTONRUSH(1, 4, 0, 2),
		PROTOSSEXCEPTION_DOUBLENEXUS(0, 0, 0, 0),
		PROTOSSEXCEPTION_ARBITER(0, 0, 0, 0),
		TERRANEXCEPTION_CHEESERUSH(0, 0, 0, 0),
		TERRANEXCEPTION_NUCLEAR(0, 0, 0, 0),
		TERRANEXCEPTION_WRAITHCLOAK(0, 0, 0, 0),
		INIT(0, 0, 0, 0);

		public int vultureRatio;
		public int tankRatio;
		public int goliathRatio;
		public int weight;

		private EnemyStrategyException(int vultureRatio, int tankRatio, int goliathRatio, int weight) {
			this.vultureRatio = vultureRatio;
			this.tankRatio = tankRatio;
			this.goliathRatio = goliathRatio;
			this.weight = weight;
		}
	}

	public static enum EnemyBuild {
		UNKNOWN,
		FAIL_TO_RECOGNIZE,
		ZERG_PHASE1_4OR5DRONE,
		ZERG_PHASE1_9DRONE_GAS,
		ZERG_PHASE1_9DRONE_NO_GAS,
		ZERG_PHASE1_OVER_POOL_GAS,
		ZERG_PHASE1_OVER_POOL_NO_GAS,
		ZERG_PHASE1_12DRONE_MULTI_GAS,
		ZERG_PHASE1_12DRONE_MULTI_NO_GAS,
	}
}
