package prebot.strategy;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.micro.constant.MicroCode.CombatStrategy;
import prebot.micro.constant.MicroCode.MainSquadMode;
import prebot.strategy.action.Action;
import prebot.strategy.constant.StrategyConfig.EnemyBuild;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;

public class StrategyIdea {
	
	public static List<Action> newActionList = new ArrayList<>();
	public static EnemyBuild enemyBuildPhase1 = EnemyBuild.UNKNOWN;
	public static EnemyBuild enemyBuildPhase2 = EnemyBuild.UNKNOWN;
	public static EnemyBuild enemyBuildPhase3 = EnemyBuild.UNKNOWN;
	
	public static EnemyStrategy enemyStrategy;
	public static EnemyStrategyException enemyStrategyException;
	
	public static Position campPosition = null;
	public static Position attackPosition = null;

	public static Position enemyCampPosition = null;
	public static MainSquadMode mainSquadMode = MainSquadMode.NORMAL;
	
	public static boolean initialized = false;
	public static boolean noCheckNoGuerilla = false;
	public static boolean attackWithoutDelay = false;
	
	public static boolean gasRushed = false;
	public static boolean photonRushed = false;
	public static boolean pushSiegeLine = false;

	// TODO 변동 값
	public static int checkerMaxNumber = 0;
	public static int spiderMineNumberPerPosition = 1;
	public static int spiderMineNumberPerGoodPosition = 1;
	
	public static int watcherFleeStartFrame = 0;
	
	
//	int tankCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
//			+ Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode);
//	int goliathCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath);
//	tankCount + goliathCount >= MicroConfig.Common.DEFENSE_READY_TO_ATTACK_SIZE_TERRAN
	
	public static BaseLocation enemyBaseExpected = null;

	public static boolean isMechanic = false;
	
	public static boolean assignScoutScv = false;
	
	public static List<String> squadNameToActivate = new ArrayList<>();
	public static List<String> squadNameToDeactivate = new ArrayList<>();
	
	public static boolean gasAdjustment = false;
	public static int gasAdjustmentWorkerCount = 0;
	
	public static CombatStrategy combatStrategy = CombatStrategy.DEFENCE_CHOKEPOINT;
	public static Chokepoint currTargetChoke = null;
	public static boolean scoutDefenseNeeded = false;
	public static boolean isCenterOccupied = false;
	
	
	
}
