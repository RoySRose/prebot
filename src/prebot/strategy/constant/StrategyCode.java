package prebot.strategy.constant;

public class StrategyCode {

	public static enum GameMap {
		UNKNOWN, FIGHTING_SPRIRITS, LOST_TEMPLE, THE_HUNTERS
	}

	public static enum KnowlegeStatus {
		WAITING, VERIFING, PROVED_TRUE, PROVED_FALSE, WRITE_FILE, ENDED
	}

	public static enum KnowledgeActionEvaluation {
		DONT_KNOW, GOOD_ACTION, BAD_ACTION
	}

	public static final int ScanDuration = 240; // approximate time that a comsat scan provides vision

	public static final int IGNORE_ENEMY_UNITINFO_SECONDS = 15;
}
