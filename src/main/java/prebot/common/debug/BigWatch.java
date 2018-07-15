package prebot.common.debug;

import java.util.HashMap;
import java.util.Map;

import prebot.common.util.TimeUtils;

public class BigWatch {
	
	private static final int TIME_TO_LIVE_SECONDS = 10;
	private static Map<String, Long> startTimeMap = new HashMap<>();
	private static Map<String, Long> resultTimeMap = new HashMap<>();
	
	private static Map<String, Long> recordMap = new HashMap<>();
	private static Map<String, Integer> recordFrameMap = new HashMap<>();

	public static Map<String, Long> getResultTimeMap() {
		return resultTimeMap;
	}

	public static Map<String, Long> getRecordTimeMap() {
		return recordMap;
	}
	
	public static void clear() {
		for (String tag : resultTimeMap.keySet()) {
			Long recordTime = recordMap.get(tag);
			Long time = resultTimeMap.get(tag);
			if (recordTime == null || time > recordTime) {
				recordMap.put(tag, time);
				recordFrameMap.put(tag, TimeUtils.elapsedFrames());
			}
		}
		
		for (String tag : recordFrameMap.keySet()) {
			Integer recordFrame = recordFrameMap.get(tag);
			if (TimeUtils.elapsedFrames(recordFrame) > TIME_TO_LIVE_SECONDS * TimeUtils.SECOND) {
				recordMap.remove(tag);
			}
		}
		
		startTimeMap.clear();
		resultTimeMap.clear();
	}
	
	public static void start(String tag) {
		Long time = startTimeMap.get(tag);
		if (time != null) {
			System.out.println("##### ALREADY EXIST " + tag + "#####");
			return;
		}
		startTimeMap.put(tag, System.currentTimeMillis());
	}
	
	
	public static void record(String tag) {
		Long time = startTimeMap.get(tag);
		if (time == null) {
			System.out.println("##### NO TIME - " + tag + " #####");
			return;
		}
		resultTimeMap.put(tag, System.currentTimeMillis() - time);
	}
}
