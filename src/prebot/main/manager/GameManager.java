package prebot.main.manager;

import prebot.common.lag.StopWatch;

/**
 * @author insaneojw
 *
 */
public abstract class GameManager {
	private StopWatch stopWatch = new StopWatch();
	private long recorded = 0L;

	public long getRecorded() {
		return recorded;
	}
	
	public void setStopWatchTag(String tag) {
		stopWatch.setTag(tag);
	}
	
	public GameManager start() {
		stopWatch.start();
		return this;
	}

	public abstract GameManager update();

	public void end() {
		recorded = stopWatch.record();
	}
}
