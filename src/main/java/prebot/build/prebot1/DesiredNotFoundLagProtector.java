package prebot.build.prebot1;

import java.util.HashMap;
import java.util.Map;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.TimeUtils;

public class DesiredNotFoundLagProtector {
	
	private static final int INITIAL_TIME = 10 * TimeUtils.SECOND;
	
	private Map<UnitType, DesiredNotFoundInfo> desiredNotFoundCountMap = new HashMap<>();
	
	private class DesiredNotFoundInfo {
		public int notFoundCount;
		public int suspendStartFrame;
		public int suspendDurationFrame;
		
		public DesiredNotFoundInfo(UnitType buildingType) {
			super();
			this.notFoundCount = 0;
			this.suspendStartFrame = CommonCode.NONE;
			this.suspendDurationFrame = INITIAL_TIME;
		}
	}

	public boolean isSuspended(UnitType buildingType) {
		DesiredNotFoundInfo desiredNotFoundInfo = desiredNotFoundCountMap.get(buildingType);
		if (desiredNotFoundInfo == null || desiredNotFoundInfo.suspendStartFrame == CommonCode.NONE) {
			return false;
		}
		if (TimeUtils.elapsedFrames(desiredNotFoundInfo.suspendStartFrame) < desiredNotFoundInfo.suspendDurationFrame) {
			return true;
		} else {
			desiredNotFoundInfo.suspendStartFrame = CommonCode.NONE; // 정지 시작시간을 초기화 한다.
			desiredNotFoundInfo.suspendDurationFrame *= 2; // 정지 시간을 2배로 증가시킨다.
			
			String message = "*** " + buildingType + " unlocked ***";
			Prebot.Broodwar.printf(message);
			System.out.println(message);
			return false;
		}
	}

	public void update(UnitType buildingType) {
		DesiredNotFoundInfo desiredNotFoundInfo = desiredNotFoundCountMap.get(buildingType);
		if (desiredNotFoundInfo == null) {
			desiredNotFoundInfo = new DesiredNotFoundInfo(buildingType);
		}
		desiredNotFoundInfo.notFoundCount = desiredNotFoundInfo.notFoundCount + 1;
		desiredNotFoundCountMap.put(buildingType, desiredNotFoundInfo);
		
		if (desiredNotFoundInfo.notFoundCount > 10) {
			desiredNotFoundInfo.notFoundCount = 0;
			desiredNotFoundInfo.suspendStartFrame = TimeUtils.elapsedFrames();

			String message = "*** " + buildingType + " locked - " + TimeUtils.framesToTimeString(desiredNotFoundInfo.suspendDurationFrame) + " ***";
			Prebot.Broodwar.printf(message);
			System.out.println(message);
		}
	}
	
}
