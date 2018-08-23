

import bwapi.UnitType;

public abstract class DefaultScoreCalculator implements ScoreCalculator{
	
	protected final UnitType unitType;
	protected final ScoreBoard scoreBoard;
	
	protected int unitCount;
	
	protected float basePoint;
	protected float addtionalPoint;
	
	public DefaultScoreCalculator(ScoreBoard scoreBoard , UnitType unitType) {
		this.scoreBoard = scoreBoard;
		this.unitType = unitType;
		this.unitCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, unitType);
		this.basePoint =0;
		this.addtionalPoint =0;
	}
	
	public DefaultScoreCalculator(ScoreBoard scoreBoard , UnitType unitType1,  UnitType unitType2) {
		this.scoreBoard = scoreBoard;
		this.unitType = unitType1;
		this.unitCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, unitType, unitType2);
	}
	
	public float getPoint() {
		calcBase();
		calcOptional();
		
		return basePoint + addtionalPoint;
	}
	
	private void calcBase() {
		// TODO Auto-generated method stub
		basePoint = unitCount * scoreBoard.getPoint(unitType);
	}
	
	public void calcOptional() {
		
	}
}
