package prebot.micro.control;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.information.UnitInfo;

public abstract class MicroControl {
	public Position goalPosition;
	public List<Unit> sqaudUnitList;
	public List<UnitInfo> enemyUnitInfoList;
	
	public void prepare(Position goalPosition, List<Unit> sqaudUnitList, List<UnitInfo> enemyUnitInfoList) {
		this.goalPosition = goalPosition;
		this.sqaudUnitList = sqaudUnitList;
		this.enemyUnitInfoList = enemyUnitInfoList;
	}
	public abstract void control(Unit controlUnit);
}
