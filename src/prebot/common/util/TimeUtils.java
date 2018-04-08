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
	public static int frames() {
		return PreBot.Broodwar.getFrameCount();
	}
	
	/// 경과시간을 second로 리턴
	public static int seconds() {
		return PreBot.Broodwar.getFrameCount() / SECOND;
	}
	
	/// 경과시간을 minute으로 리턴
	public static int miniutes() {
		return PreBot.Broodwar.getFrameCount() / MINUTE;
	}
	
	/// 경과시간을 frame으로 리턴
	public static int frames(int startFrame) {
		return PreBot.Broodwar.getFrameCount() - startFrame;
	}

	/// 경과시간을 second으로 리턴
	public static int seconds(int startFrame) {
		return (PreBot.Broodwar.getFrameCount() - startFrame) / SECOND;
	}

	/// 경과시간을 minute으로 리턴
	public static int miniutes(int startFrame) {
		return (PreBot.Broodwar.getFrameCount() - startFrame) / MINUTE;
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
