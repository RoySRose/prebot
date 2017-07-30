package pre.util;

import bwapi.Unit;
import pre.main.MyBotModule;

public class CommonUtils {
	public static boolean executeRotation(int group, int rotationSize) {
		return MyBotModule.Broodwar.getFrameCount() % rotationSize == group;
	}
	
	public static boolean executeUnitRotation(Unit unit, int rotationSize) {
		int unitGroup = unit.getID() % rotationSize;
		return executeRotation(unitGroup, rotationSize);
	}
}
