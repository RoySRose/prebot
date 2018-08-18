package prebot.macro.scorecalculator;

import bwapi.UnitType;
import prebot.macro.util.ScoreBoard;

public class TankScoreCalculator extends DefaultScoreCalculator{
	
	public TankScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1, UnitType unitType2) {
		super(scoreBoard, unitType1, unitType2);
	}
	
	@Override
	public void calcOptional() {
		if(unitCount > 5) {
			addtionalPoint += (unitCount -5)/24 * basePoint;
        }
	}
}
