package prebot.common.util;

import java.util.Map;

import bwapi.Position;
import prebot.common.util.internal.HardCodeConfig;
import prebot.common.util.internal.HardCodeInfo;

public class HardCodePositionUtils {
	public static HardCodeInfo getHardCodeInfo(Position position) {
		Map<Position, HardCodeInfo> hardCodeInfoMap = HardCodeConfig.getHardCodeInfoMap();
		HardCodeInfo hardCodeInfo = hardCodeInfoMap.get(position);
		if (hardCodeInfo == null) {
			hardCodeInfo = new HardCodeInfo();
		}
		return hardCodeInfo;
	}
}
