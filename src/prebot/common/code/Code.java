package prebot.common.code;

public class Code {

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

	public enum WatcherPredictResult {
		BACK, ATTACK
	}

	public enum KnowlegeStatus {
		WAITING, VERIFING, PROVED_TRUE, PROVED_FALSE, WRITE_FILE, ENDED
	}

	public enum KnowledgeActionEvaluation {
		DONT_KNOW, GOOD_ACTION, BAD_ACTION
	}

	/// 일꾼 유닛에게 지정하는 임무의 종류
	public enum WorkerJob {
		MINERALS, GAS, BUILD, COMBAT, IDLE, REPAIR, MOVE, DEFAULT
	};

}
