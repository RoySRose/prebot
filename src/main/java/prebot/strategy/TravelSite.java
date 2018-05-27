package prebot.strategy;

import bwta.BaseLocation;

public class TravelSite {
	TravelSite(BaseLocation baseLocation, int visitFrame, int visitAssignedFrame, int guerillaExamFrame) {
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
		return "TravelSite [baseLocation=" + baseLocation.getPosition() + ", visitFrame=" + visitFrame + ", visitAssignedFrame="
				+ visitAssignedFrame + ", guerillaExamFrame=" + guerillaExamFrame + "]";
	}
}