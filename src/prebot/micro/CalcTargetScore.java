package prebot.micro;

import prebot.information.UnitInfo;

public class CalcTargetScore {
	
	private int unitWeight;
	private int splashWeight;
	private int distanceWeight;
	private int hitpointWeight;
	
	public CalcTargetScore(int unitWeight, int splashWeight, int distanceWeight, int hitpointWeight) {
		this.unitWeight = unitWeight;
		this.splashWeight = splashWeight;
		this.distanceWeight = distanceWeight;
		this.hitpointWeight = hitpointWeight;
	}

	public int calculate(UnitInfo eui) {
		return 1;
	}

}
