

import bwapi.UnitType;

public class ValkyrieScoreCalculator extends DefaultScoreCalculator{
	
	public ValkyrieScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1) {
		super(scoreBoard, unitType1);
	}
	
	@Override
	public void calcOptional() {
		
		if(StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR1 
				|| StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR2){
			if(unitCount > 2) {
				addtionalPoint = (float) (basePoint * 2.5);
			}
		}
	}
}
