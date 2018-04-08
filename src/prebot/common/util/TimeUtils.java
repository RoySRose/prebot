package prebot.common.util;

import bwapi.Unit;
import prebot.main.PreBot;

public class TimeUtils {
	
	public static final int FRAME = 1;
	public static final int SECOND = 24;
	public static final int MINUTE = 24 * 60;
	
	/// 지나간 프레임이면 true
	public static boolean isPassedFrame(int frame) {
		return frame < PreBot.Broodwar.getFrameCount();
	}

	/// 경과시간을 frame으로 리턴
	public static int elapsedFrames() {
		return PreBot.Broodwar.getFrameCount();
	}
	
	/// 경과시간을 second로 리턴
	public static int elapsedSeconds() {
		return PreBot.Broodwar.getFrameCount() / SECOND;
	}
	
	/// 경과시간을 minute으로 리턴
	public static int elapsedMiniutes() {
		return PreBot.Broodwar.getFrameCount() / MINUTE;
	}
	
	/// 경과시간을 frame으로 리턴
	public static int elapsedFrames(int startFrame) {
		return PreBot.Broodwar.getFrameCount() - startFrame;
	}

	/// 경과시간을 second으로 리턴
	public static int elapsedSeconds(int startFrame) {
		return (PreBot.Broodwar.getFrameCount() - startFrame) / SECOND;
	}

	/// 경과시간을 minute으로 리턴
	public static int elapsedMiniutes(int startFrame) {
		return (PreBot.Broodwar.getFrameCount() - startFrame) / MINUTE;
	}

	/// frame을 second로 단위변경하여 리턴
	public static int framesToSeconds(int frame) {
		return frame / SECOND;
	}

	/// frame을 second로 단위변경하여 리턴
	public static int framesToMinutes(int frame) {
		return frame / MINUTE;
	}
	
	/// 실행할 frame되어야 하는 frame이면 true
	public static boolean executeRotation(int group, int rotationSize) {
		return (PreBot.Broodwar.getFrameCount() % rotationSize) == group;
	}

	/// unit이 실행할 rotation이면 true
	public static boolean executeUnitRotation(Unit unit, int rotationSize) {
		int unitGroup = unit.getID() % rotationSize;
		return executeRotation(unitGroup, rotationSize);
	}
}
