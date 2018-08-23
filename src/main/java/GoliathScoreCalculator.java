

import bwapi.UnitType;

public class GoliathScoreCalculator extends DefaultScoreCalculator{
	
	public GoliathScoreCalculator(ScoreBoard scoreBoard, UnitType unitType1) {
		super(scoreBoard, unitType1);
	}
	
	@Override
	public void calcOptional() {
		if(StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR1 
				|| StrategyIdea.currentStrategy == EnemyStrategy.ZERG_AIR2
				|| StrategyIdea.currentStrategy == EnemyStrategy.ZERG_MIXED){
			addtionalPoint = (float) (basePoint * 0.2);
		}
		
		if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_PROTOSS_AIR2 
				|| StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_PROTOSS_AIR3
				){
			addtionalPoint = (float) (basePoint * 0.3);
		}
	}
}
