package prebot.micro.constant;

public class MicroCode {
	
	/// 일꾼 유닛에게 지정하는 임무의 종류
	public static enum CombatStrategy {
		DEFENCE_INSIDE, DEFENCE_CHOKEPOINT, ATTACK_ENEMY // , READY_TO_ATTACK
	}

	public static enum CombatStrategyDetail {
		VULTURE_JOIN_SQUAD, NO_WAITING_CHOKE, NO_CHECK_NO_GUERILLA, ATTACK_NO_MERCY, MINE_STRATEGY_FOR_TERRAN
	}

	public static enum SquadOrderType {
		NONE, IDLE, ATTACK, DEFEND, HOLD, WATCH, CHECK, GUERILLA
	}

	public static enum Result {
		LOSS, WIN, BIG_WIN
	}
	
	public static class Combat {
		public static final int IDLE_PRIORITY = 0;
		public static final int ATTACK_PRIORITY = 2;
		public static final int WATCHER_PRIORITY = 3;
		public static final int CHECKER_PRIORITY = 4;
		public static final int GUERILLA_PRIORITY = 5;
		public static final int BASE_DEFENSE_PRIORITY = 6;
		public static final int SCOUT_DEFENSE_PRIORITY = 7;
		public static final int EARLY_DEFENSE_PRIORITY = 10;

		public static final int WRAITH_PRIORITY = 100;
		public static final int VESSEL_PRIORITY = 101;

		public static final int IDLE_RADIUS = 100;
		public static final int ATTACK_RADIUS = 300;
		public static final int WATCHER_RADIUS = 300;
		public static final int CHECKER_RADIUS = 150;
		public static final int GUERILLA_RADIUS = 300;
		public static final int BASE_DEFENSE_RADIUS = 500; // 32 * 25
		public static final int SCOUT_DEFENSE_RADIUS = 1500;
		public static final int MARINE_RADIUS = 300;
		public static final int WRAITH_RADIUS = 300;
		public static final int VESSEL_RADIUS = 600;
	}

	public static class OldSquadName {

		public static final String IDLE = "Idle";
		public static final String SCOUT_DEFENSE = "ScoutDefense";
		public static final String MAIN_ATTACK = "MainAttack";
		public static final String BASE_DEFENSE_ = "BaseDefense_";
		public static final String CHECKER = "Checker";
		public static final String GUERILLA_ = "Guerilla_";
		public static final String MARINE = "Marine";
		public static final String WRAITH = "Wraith";
		public static final String VESSEL = "Vessel";
		public static final String BUILDING = "Building";
	}
}
