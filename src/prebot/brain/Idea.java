package prebot.brain;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.brain.strategy.Strategy;
import prebot.brain.stratgy.enemy.EnemyBuild;
import prebot.common.code.Code.CommonCode;
import prebot.main.manager.StrategyManager;

public class Idea {
	public static Idea of() {
		return StrategyManager.Instance().getIdea();
	}

	public Strategy strategy = null;
	public List<EnemyBuild> enemyBuildList = new ArrayList<>();
	
	public boolean gasAdjustment = false; // true인 경우 gasAdjustmentWorkerCount에 따른 가스조절 
	public int gasAdjustmentWorkerCount = 0;
	
	public int checkerMaxCount = 0;
	public int scvScoutMaxCount = 0;
	
	public Position campPosition = null;
	public Position attackPosition = null;
	public BaseLocation expansionBase = null;

	public BaseLocation enemeyExpectedBase = null;
	
	// by knowledge
	public int darkTemplarSafeTime = CommonCode.NONE; // DarkTemplarSafeTime

}
