package prebot.strategy;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.strategy.constant.EnemyStrategy;

public class StrategyIdea {
	
	public static EnemyStrategy enemyStrategy = EnemyStrategy.UNKNOWN;
	public static EnemyStrategy phase01 = EnemyStrategy.UNKNOWN;
	public static EnemyStrategy phase02 = EnemyStrategy.UNKNOWN;
	public static EnemyStrategy phase03 = EnemyStrategy.UNKNOWN;
	
	public static boolean initiated = false;
	public static MainSquadMode mainSquadMode = MainSquadMode.NORMAL;
	
	public static Position mainSquadPosition = null;
	public static Position campPosition = null;
	public static Position attackPosition = null;

	public static int checkerMaxNumber = 0;
	public static int spiderMineNumberPerPosition = 1;
	public static int spiderMineNumberPerGoodPosition = 1;
	
	public static boolean gasAdjustment = false;
	public static int gasAdjustmentWorkerCount = 0;
	
	public static Position enemySquadPosition = null;
	
	public static boolean enemiesInMyBase = false;

	public static boolean isCenterOccupied = false;
	public static boolean gasRushed = false;
	public static boolean photonRushed = false;
	public static boolean pushSiegeLine = false;
	
	
//	int tankCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//			+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode);
//	int goliathCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath);
//	tankCount + goliathCount >= MicroConfig.Common.DEFENSE_READY_TO_ATTACK_SIZE_TERRAN
	
	public static BaseLocation enemyBaseExpected = null;
	
	public static boolean assignScoutScv = false;
	
	
	
}
