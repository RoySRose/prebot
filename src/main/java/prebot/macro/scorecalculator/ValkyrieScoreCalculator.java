package prebot.macro.scorecalculator;

import bwapi.UnitType;
import prebot.macro.util.ScoreBoard;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;

public class ValkyrieScoreCalculator extends DefaultScoreCalculator{
	
	public ValkyrieScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1) {
		super(scoreBoard, unitType1);
	}
	
	@Override
	public void calcOptional() {
		
		if(StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR1 
				|| StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR2){
			addtionalPoint = (float) (basePoint * 1.5);
		}
	}
}
