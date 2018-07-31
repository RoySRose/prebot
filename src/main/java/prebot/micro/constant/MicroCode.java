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
}
