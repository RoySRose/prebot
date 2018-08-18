package prebot.macro.scorecalculator;

import bwapi.UnitType;
import prebot.macro.util.ScoreBoard;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;

public class WraithScoreCalculator extends DefaultScoreCalculator{
	
	public WraithScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1) {
		super(scoreBoard, unitType1);
	}
	
	@Override
	public void calcOptional() {
		addtionalPoint = unitCount/10 * basePoint;
	}
}
