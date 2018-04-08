package prebot.brain.history;

import java.util.ArrayList;
import java.util.List;

public class History {
	private List<HistoryFrame> historyList = new ArrayList<>();
	
	public void add(HistoryFrame historyFrame) {
		historyList.add(historyFrame);
	}
}