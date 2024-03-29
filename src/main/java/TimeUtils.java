

import bwapi.Unit;
import bwapi.UnitType;

public class TimeUtils {
	
	public static final int FRAME = 1;
	public static final int SECOND = 24;
	public static final int MINUTE = 24 * 60;
	
	/// 이전 프레임이면 true
	public static boolean beforeTime(int minutes, int seconds) {
		return before(timeToFrames(minutes, seconds));
	}
	
	public static boolean before(int frame) {
		return MyBotModule.Broodwar.getFrameCount() < frame;
	}
	
	/// 지나간 프레임이면 true
	public static boolean afterTime(int minutes, int seconds) {
		return after(timeToFrames(minutes, seconds));
	}
	
	public static boolean after(int frame) {
		return MyBotModule.Broodwar.getFrameCount() > frame;
	}

	/// 경과시간을 frame으로 리턴
	public static int elapsedFrames() {
		return MyBotModule.Broodwar.getFrameCount();
	}
	
	/// 경과시간을 second로 리턴
	public static int elapsedSeconds() {
		return MyBotModule.Broodwar.getFrameCount() / SECOND;
	}
	
	/// 경과시간을 minute으로 리턴
	public static int elapsedMiniutes() {
		return MyBotModule.Broodwar.getFrameCount() / MINUTE;
	}
	
	/// 경과시간을 frame으로 리턴
	public static int elapsedFrames(int startFrame) {
		return MyBotModule.Broodwar.getFrameCount() - startFrame;
	}

	/// 경과시간을 second으로 리턴
	public static int elapsedSeconds(int startFrame) {
		return (MyBotModule.Broodwar.getFrameCount() - startFrame) / SECOND;
	}

	/// 경과시간을 minute으로 리턴
	public static int elapsedMiniutes(int startFrame) {
		return (MyBotModule.Broodwar.getFrameCount() - startFrame) / MINUTE;
	}

	/// frame을 second로 단위변경하여 리턴
	public static int framesToSeconds(int frame) {
		return frame / SECOND;
	}

	/// frame을 second로 단위변경하여 리턴
	public static int framesToMinutes(int frame) {
		return frame / MINUTE;
	}

	/// 유닛의 빌드시간
	public static int buildSeconds(UnitType unitType) {
		return unitType.buildTime() / TimeUtils.SECOND;
	}
	
	/// 남아있는 빌드시간
	public static int remainBuildSeconds(Unit building) {
		if (building.isCompleted()) {
			return CommonCode.UNKNOWN;
		}
		
		double remainRate = (double) (building.getType().maxHitPoints() - building.getHitPoints()) / building.getType().maxHitPoints();
		int remainBuildFrames = (int) (building.getType().buildTime() * remainRate);
		return remainBuildFrames / SECOND;
	}
	
	/// 시작된 빌드시간
	public static int buildStartFrames(Unit building) {
		if (building.isCompleted()) {
			return CommonCode.UNKNOWN;
		}
		if (building.getType() == UnitType.Zerg_Lair || building.getType() == UnitType.Zerg_Hive) { // 레어, 하이브 제외
			return CommonCode.UNKNOWN;
		}
		
		double completeRate = (double) building.getHitPoints() / building.getType().maxHitPoints();
		return elapsedFrames() - (int) (building.getType().buildTime() * completeRate);
	}
	
	public static int timeToFrames(int minutes, int seconds) {
		return minutes * MINUTE + seconds * SECOND;
	}
	
	public static String framesToTimeString(int frames) {
		if (frames == CommonCode.UNKNOWN) {
			return "unknown";
		}
		
		int minutes = framesToMinutes(frames);
		int seconds = framesToSeconds(frames - minutes * MINUTE);
		return minutes + "min " + seconds + "sec";
	}

	/// 실행할 frame되어야 하는 frame이면 true
	public static boolean executeRotation(int group, int rotationSize) {
		return (MyBotModule.Broodwar.getFrameCount() % rotationSize) == group;
//		return true;
	}

	/// unit이 실행할 rotation이면 true
	public static boolean executeUnitRotation(Unit unit, int rotationSize) {
		return !UnitBalancer.skipControl(unit);
//		int unitGroup = unit.getID() % rotationSize;
//		return executeRotation(unitGroup, rotationSize);
	}
	
}
