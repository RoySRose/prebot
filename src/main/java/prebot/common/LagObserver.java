package prebot.common;

import prebot.common.main.Prebot;

public class LagObserver {

	private static int groupsize = 1; // 1 ... 24
	public static int groupsize() {
		return groupsize;
	}
	
	private static int managerLagLevel = 1;
	private static final int MANAGER_ROTATION_SIZE = 7;
	public static int managerRotationSize() { // 7, 14, 21, 28
		return MANAGER_ROTATION_SIZE * managerLagLevel;
	}

	private static final boolean ADJUST_ON = true;
	
	private static final long MILLISEC_MAX_COAST = 50;
//	private static final long MILLISEC_MIN_COAST = 30;
	
	
	private static int groupMaxSize = 48; // max : 2초 딜레이
	private static int groupMinSize = 4;
	
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
		observedTime[Prebot.Broodwar.getFrameCount() % OBSERVER_CAPACITY] = System.currentTimeMillis() - this.startTime;
		this.startTime = System.currentTimeMillis();
		this.adjustment();
	}
	
	public void adjustment() {
		if (ADJUST_ON) {
			long cost = observedTime[Prebot.Broodwar.getFrameCount() % OBSERVER_CAPACITY];
			
			if (cost > MILLISEC_MAX_COAST) {
				if (groupsize < groupMaxSize) {
					groupsize++;
				}
			} else {
				if (Prebot.Broodwar.self().supplyUsed() > 300) {
					groupMinSize = 6;
				} else {
					groupMinSize = 1;
				}
				
				if (groupsize > groupMinSize) {
					boolean exceedTimeExist = false;
					for (long t : observedTime) {
						if (t >= MILLISEC_MAX_COAST) {
							exceedTimeExist = true;
							break;
						}
					}
					if (!exceedTimeExist) {
						groupsize--;
					}
				}
			}
			
			managerLagLevel = groupsize / 6 + 1;				
		}
	}

}
