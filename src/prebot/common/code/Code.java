package prebot.common.code;

public class Code {
	
	public static class SquadName {
		public static final String CHECKER = "CHECKER";
		public static final String IDLE = "IDLE";
		public static final String MAIN_DEFENSE = "MAIN_DEFENSE";
		public static final String SCOUT_DEFENSE = "SCOUT_DEFENSE";
		public static final String SCV_SCOUT = "SCV_SCOUT";
		public static final String WATCHER = "WATCHER";
		public static final String EARLY_DEFENSE = "EARLY_DEFENSE";
	}

	public static class CommonCode {
		public static final int INDEX_NOT_FOUND = -1;
		public static final int NONE = -1;

		public static final int INT_MAX = 100000000;
		public static final double DOUBLE_MAX = 100000000.0;
	}

	public static enum UnitFindRange {
		COMPLETE, INCOMPLETE, ALL, CONSTRUCTION_QUEUE, ALL_AND_CONSTRUCTION_QUEUE
	}

	public static enum PlayerRange {
		SELF, ENEMY, NEUTRAL, ALL
	}

	public static enum EnemyUnitFindRange {
		VISIBLE, INVISIBLE, ALL
	}

	public static enum StrategyRuleResult {
		CERTAINLY, MAYBE, DISCARD
	}

	public static enum WatcherPredictResult {
		BACK, ATTACK
	}

	public static enum KnowlegeStatus {
		WAITING, VERIFING, PROVED_TRUE, PROVED_FALSE, WRITE_FILE, ENDED
	}

	public static enum KnowledgeActionEvaluation {
		DONT_KNOW, GOOD_ACTION, BAD_ACTION
	}

	/// 일꾼 유닛에게 지정하는 임무의 종류
	public static enum WorkerJob {
		MINERALS, GAS, BUILD, COMBAT, IDLE, REPAIR, MOVE, DEFAULT
	};
	
	/// 맵
	public static enum GameMap {
		UNKNOWN, FIGHTING_SPRIRITS
	}
	
	public static enum InitialBuildType {
		NONE, ONE_FACTORY_MULTI, FAST_BARRACKS_ONE_FACOTRY_MULTI, TWO_FACTORY_MULTI;
	}
}
