package pre.util;

import pre.main.MyBotModule;

public class CommonUtils {
	public static boolean executeOncePerFrame(int frame, int executeFrame) {
		return MyBotModule.Broodwar.getFrameCount() % frame == executeFrame;
	}
}
