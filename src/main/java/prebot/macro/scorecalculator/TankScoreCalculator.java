package prebot.macro.scorecalculator;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.util.InfoUtils;
import prebot.macro.util.ScoreBoard;

public class TankScoreCalculator extends DefaultScoreCalculator{
	
	public TankScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1, UnitType unitType2) {
		super(scoreBoard, unitType1, unitType2);
	}
	
	@Override
	public void calcOptional() {
		
		if (InfoUtils.enemyRace() != Race.Terran) {
			if(unitCount > 5) {
				addtionalPoint += (unitCount -5)/24 * basePoint;
	        }
		}
	}
}
