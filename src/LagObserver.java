public class LagObserver {

	private static int groupsize = 1; // 1 ... 24
	public static int groupsize() {
		return groupsize;
	}

	private static final boolean ADJUST_ON = true;
	
	private static final long MILLISEC_MAX_COAST = 50;
//	private static final long MILLISEC_MIN_COAST = 30;
	
	private static final int GROUP_MAX_SIZE = 48; // max : 2초 딜레이
	private static final int GROUP_MIN_SIZE = 1;
	
	private static final int OBSERVER_CAPACITY = 15 * 24; // 15초간 delay가 없어서 groupsize를 낮춘다.
	private static long[] observedTime = new long[OBSERVER_CAPACITY];
	
	private static final String LAG_RELIEVE_ADJUSTMENT = "Lag Relieve Adjustment: LELEL - %d ... (%s)";

	private long startTime;
	
	public LagObserver() {
	}
	
	public void start() {
		this.startTime = System.currentTimeMillis();
	}
	
	public void observe() {
		observedTime[MyBotModule.Broodwar.getFrameCount() % OBSERVER_CAPACITY] = System.currentTimeMillis() - this.startTime;
		this.startTime = System.currentTimeMillis();
		this.adjustment();
	}
	
	public void adjustment() {
		if (ADJUST_ON) {
			long cost = observedTime[MyBotModule.Broodwar.getFrameCount() % OBSERVER_CAPACITY];
			
			if (cost > MILLISEC_MAX_COAST) {
				if (groupsize < GROUP_MAX_SIZE) {
					groupsize++;
					if(Config.BroodwarDebugYN){
						MyBotModule.Broodwar.printf(String.format(LAG_RELIEVE_ADJUSTMENT, groupsize, "UP"));
					}
				}
			} else {
				if (groupsize > GROUP_MIN_SIZE) {
					boolean exceedTimeExist = false;
					for (long t : observedTime) {
						if (t >= MILLISEC_MAX_COAST) {
							exceedTimeExist = true;
							break;
						}
					}
					
					if (!exceedTimeExist) {
						groupsize--;
						if(Config.BroodwarDebugYN){
							MyBotModule.Broodwar.printf(String.format(LAG_RELIEVE_ADJUSTMENT, groupsize, "DOWN"));
						}
					}
				}
			}
		}
	}

}
