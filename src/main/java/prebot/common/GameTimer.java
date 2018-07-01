package prebot.common;

/**
 * @author insaneojw
 *
 */
public class GameTimer {
	
	private boolean microTime;
	private long time;

	public GameTimer() {
		this.setTime();
	}
	
	public GameTimer(boolean microTime) {
		this.microTime = microTime;
		this.setTime();
	}

	public long record() {
		long record = currrentTime() - time;
		this.setTime();
		return record;
	}
	
	private void setTime() {
		this.time = this.currrentTime();
	}
	
	private long currrentTime() {
		if (microTime) {
			return System.nanoTime() / 1000;
		} else {
			return System.currentTimeMillis();
		}
	}
}
