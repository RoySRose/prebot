

import bwapi.UnitType;

public class WraithScoreCalculator extends DefaultScoreCalculator{
	
	public WraithScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1) {
		super(scoreBoard, unitType1);
	}
	
	@Override
	public void calcOptional() {
		addtionalPoint = unitCount/10 * basePoint;
	}
}
