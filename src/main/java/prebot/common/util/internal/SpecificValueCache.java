package prebot.common.util.internal;

import java.util.HashMap;
import java.util.Map;

import prebot.common.util.TimeUtils;

public class SpecificValueCache {
	
	public enum ValueType {
		ACTIVATED_COMMAND_COUNT;
	};
	
	private static Map<ValueType, Object> data = new HashMap<>();
	private static Map<ValueType, Integer> dataSavedFrame = new HashMap<>();
	
	public static <T> T get(ValueType valueType, Class<T> type) {
		Object value = data.get(valueType);
		Integer frame = dataSavedFrame.get(valueType);
		if (frame == null || frame < TimeUtils.elapsedFrames()) {
			return null;
		}
		return type.cast(value);
	}
	
	public static void put(ValueType valueType, Object value) {
		data.put(valueType, value);
		dataSavedFrame.put(valueType, TimeUtils.elapsedFrames());
	}

}
