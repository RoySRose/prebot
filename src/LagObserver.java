public class LagObserver {

	private static int groupsize = 1; // 1 ... 24
	public static int groupsize() {
		return groupsize;
	}

	private static final boolean ADJUST_ON = true;
	private static final long DEFAULT_WARN_DURATION = 55;
	
	private static final long MILLISEC_MAX_COAST = 55;
	private static final long MILLISEC_MIN_COAST = 30;
	
	private static final int GROUP_MAX_SIZE = 48; // max : 2초 딜레이
	private static final int GROUP_MIN_SIZE = 1;
	
	private static final String LAG_RELIEVE_ADJUSTMENT = "Lag Relieve Adjustment: LELEL - %d ... (%s)";

	private long warnDuration;
	private long startTime;
	
	public LagObserver() {
		this.warnDuration = DEFAULT_WARN_DURATION;
	}
	
	public LagObserver(long warnDuration) {
		this.warnDuration = warnDuration;
	}

	public void start() {
		this.startTime = System.currentTimeMillis();
	}
	
	public long observe() {
		long duration = System.currentTimeMillis() - this.startTime;
//		if (duration >= warnDuration) {
//			System.out.println("############## " + duration + "##############");
//		}
		this.startTime = System.currentTimeMillis();
		return duration;
	}
	
	public void adjustment(long cost) {
		if (ADJUST_ON) {
			if (cost > MILLISEC_MAX_COAST) {
				if (groupsize < GROUP_MAX_SIZE) {
					groupsize++;
					if (groupsize >= 5) {
						MyBotModule.Broodwar.printf(String.format(LAG_RELIEVE_ADJUSTMENT, groupsize, "UP"));
					}
				}
			} else if (cost < MILLISEC_MIN_COAST) {
				if (groupsize > GROUP_MIN_SIZE) {
					groupsize--;
					if (groupsize >= 5) {
						MyBotModule.Broodwar.printf(String.format(LAG_RELIEVE_ADJUSTMENT, groupsize, "DOWN"));
					}
				}
			} else {
				// 적당 ㅎㅎ
			}
		}
	}

}
