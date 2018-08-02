package prebot.strategy;

import bwta.BaseLocation;
import prebot.common.util.TimeUtils;

public class TravelSite {
	public TravelSite(BaseLocation baseLocation, int visitFrame, int visitAssignedFrame, int guerillaExamFrame) {
		this.baseLocation = baseLocation;
		this.visitFrame = visitFrame;
		this.visitAssignedFrame = visitAssignedFrame;
		this.guerillaExamFrame = guerillaExamFrame;
	}
	public BaseLocation baseLocation;
	public int visitFrame;
	public int visitAssignedFrame;
	public int guerillaExamFrame;

	@Override
	public String toString() {
		return baseLocation.getPosition() + "\nvisitFrame=" + TimeUtils.framesToTimeString(visitFrame)
				+ "\nvisitAssignedFrame=" + TimeUtils.framesToTimeString(visitAssignedFrame)
				+ "\nguerillaExamFrame=" + TimeUtils.framesToTimeString(guerillaExamFrame);
	}
}