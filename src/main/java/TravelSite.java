

import bwta.BaseLocation;

public class TravelSite {
	public TravelSite(BaseLocation baseLocation, int visitFrame, int visitAssignedFrame, int guerillaExamFrame, int index) {
		this.baseLocation = baseLocation;
		this.visitFrame = visitFrame;
		this.visitAssignedFrame = visitAssignedFrame;
		this.guerillaExamFrame = guerillaExamFrame;
		this.index = index;
	}
	public BaseLocation baseLocation;
	public int visitFrame;
	public int visitAssignedFrame;
	public int guerillaExamFrame;
	public int index;

	@Override
	public String toString() {
		return baseLocation.getPosition() + "\nvisitFrame=" + TimeUtils.framesToTimeString(visitFrame)
				+ "\nvisitAssignedFrame=" + TimeUtils.framesToTimeString(visitAssignedFrame)
				+ "\nguerillaExamFrame=" + TimeUtils.framesToTimeString(guerillaExamFrame)
				+ "\nindex=" + index;
	}
}