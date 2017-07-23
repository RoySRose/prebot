package pre.util;

public class LagTest {

	private static final long DEFAULT_WARN_DURATION = 30;
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
		test(null);
	}
	
	public void test(String tag) {
		long currentTime = System.currentTimeMillis();
		String section = "line" + new Throwable().getStackTrace()[1].getLineNumber();
		if (tag != null) { section += " *" + tag; }
		if (currentTime - startTime > warnDuration) {
			System.out.println("### " + name + "." + section + " : " + (currentTime - startTime) + " millisec");
		}
		startTime = currentTime;
	}

}
