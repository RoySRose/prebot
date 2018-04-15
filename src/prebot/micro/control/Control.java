package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.brain.Idea;
import prebot.information.UnitInfo;
import prebot.main.manager.StrategyManager;

public abstract class Control {
	public Idea idea;
	public List<Unit> sqaudUnitList;
	public List<UnitInfo> enemyUnitInfoList;
	
	public void prepare(List<Unit> sqaudUnitList, List<UnitInfo> enemyUnitInfoList) {
		this.idea = StrategyManager.Instance().getIdea();
		this.sqaudUnitList = sqaudUnitList;
		this.enemyUnitInfoList = enemyUnitInfoList;
	}
	
	public abstract void control();
}
