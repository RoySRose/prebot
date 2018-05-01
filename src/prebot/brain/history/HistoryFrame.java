package prebot.brain.history;

import prebot.common.util.internal.UnitCache;

public class HistoryFrame {
	
	private int historyFrame;
	private UnitCache unitCache;
	
	public HistoryFrame(int historyFrame, UnitCache unitCache) {
		this.historyFrame = historyFrame;
		this.unitCache = unitCache;
	}

	public int getHistoryFrame() {
		return historyFrame;
	}

	public UnitCache getUnitCache() {
		return unitCache;
	}
	
}
