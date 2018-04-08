package prebot.common.lag;

/**
 * 테스트를 위해 소요되는 시간을 체크
 * 
 * @author insaneojw
 */
public class LagTester {

	private static final String UNKNOWN = "?";
	private static final long DEFAULT_WARN_DURATION = 0;

	private String tag;
	private long warnDuration;
	private boolean isMicroTimeTest; // 15.625ms == 15625000nanoseconds

	private long timeStarted;

	public LagTester(String tag, long warnDuration, boolean isMicroTimeTest) {
		this.tag = tag;
		this.warnDuration = warnDuration;
		this.isMicroTimeTest = isMicroTimeTest;
	}

	public LagTester() {
		this.tag = "";
		this.warnDuration = DEFAULT_WARN_DURATION;
		this.isMicroTimeTest = false;
	}

	private String getTestName() {
		for (StackTraceElement ste : new Throwable().getStackTrace()) {
			if (ste.getClassName().endsWith(this.getClass().getSimpleName())) {
				continue;
			}
			String[] classTrace = ste.getClassName().split("\\.");
			return "(" + classTrace[classTrace.length - 1] + ".java:" + ste.getLineNumber() + ")";
			// return ste.toString();
		}
		return UNKNOWN;
	}

	public void prepare() {
		if (isMicroTimeTest) {
			this.timeStarted = System.nanoTime() / 1000;
		} else {
			this.timeStarted = System.currentTimeMillis();
		}
	}

	public long test() {
		long timeCurrent = 0L;
		if (isMicroTimeTest) {
			timeCurrent = System.nanoTime() / 1000;
		} else {
			timeCurrent = System.currentTimeMillis();
		}

		long timeSpent = timeCurrent - this.timeStarted;
		this.timeStarted = timeCurrent;

		if (timeSpent > warnDuration) {
			System.out.println("### " + (tag.isEmpty() ? getTestName() : tag) + " -> " + timeSpent + " " + (isMicroTimeTest ? " microsec" : " millisec"));
		}

		return timeSpent;
	}

}
