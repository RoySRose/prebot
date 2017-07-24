package pre.util;

public class LagTest {

//	private static final int CONSOLE_OUT_FRAME = 24 * 10;
	private static final long DEFAULT_WARN_DURATION = 50;
	private static final String ROOT_PACKAGE = "pre.";
	
	private String name;
	private long startTime;
	private long warnDuration;
	
	public LagTest() {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		String className = ste.getClassName();
		String methodName = ste.getMethodName();
		
		this.name = className.replace(ROOT_PACKAGE, "") + "." + methodName;
		this.startTime = System.currentTimeMillis();
		this.warnDuration = DEFAULT_WARN_DURATION;
	}
	
	public LagTest(long warnDuration) {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		String className = ste.getClassName();
		String methodName = ste.getMethodName();
		
		this.name = className.replace(ROOT_PACKAGE, "") + "." + methodName;
		this.startTime = System.currentTimeMillis();
		this.warnDuration = warnDuration;
	}

	public void test() {
//		if(!CommonUtils.executeOncePerFrame(CONSOLE_OUT_FRAME, 0)) {
//			return;
//		}
		
		long currentTime = System.currentTimeMillis();
		String section = "line" + new Throwable().getStackTrace()[1].getLineNumber();
		if (currentTime - startTime > warnDuration) {
			System.out.println("### " + name + "." + section + " : " + (currentTime - startTime) + " millisec");
		}
		startTime = currentTime;
	}
	
	public void test(String tag) {
//		if(!CommonUtils.executeOncePerFrame(CONSOLE_OUT_FRAME, 0)) {
//			return;
//		}
		
		long currentTime = System.currentTimeMillis();
		String section = "line" + new Throwable().getStackTrace()[1].getLineNumber() + " *" + tag;
		if (currentTime - startTime > warnDuration) {
			System.out.println("### " + name + "." + section + " : " + (currentTime - startTime) + " millisec");
		}
		startTime = currentTime;
	}

}
