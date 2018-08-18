package prebot.macro.scorecalculator;

import bwapi.UnitType;
import prebot.macro.util.ScoreBoard;

public class VultureScoreCalculator extends DefaultScoreCalculator{
	
	public VultureScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1) {
		super(scoreBoard, unitType1);
	}
	
	@Override
	public void calcOptional() {
		
	}
}
