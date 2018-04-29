package prebot.micro;

import bwapi.Unit;
import prebot.information.UnitInfo;

public class Decision {

	public enum DecisionType {
		KITING_UNIT, ATTACK_UNIT, ATTACK_POSITION, FLEE_FROM_UNIT, FLEE_FROM_POSITION, HOLD, STOP;
	}

	public DecisionType type;
	public Unit unit;
	public UnitInfo eui;
	
	private Decision(DecisionType type) {
		this.type = type;
	}

	private Decision(DecisionType type, UnitInfo eui) {
		this.eui = eui;
		this.type = type;
	}

	public static Decision kitingUnit(UnitInfo eui) {
		return new Decision(DecisionType.KITING_UNIT, eui);
	}

	public static Decision attackUnit(UnitInfo eui) {
		return new Decision(DecisionType.ATTACK_UNIT, eui);
	}
	
	public static Decision fleeFromUnit(UnitInfo eui) {
		return new Decision(DecisionType.FLEE_FROM_UNIT, eui);
	}
	
	public static Decision attackPosition() {
		return new Decision(DecisionType.ATTACK_POSITION);
	}
	
	public static Decision fleeFromPosition() {
		return new Decision(DecisionType.FLEE_FROM_POSITION);
	}
	
	public static Decision hold() {
		return new Decision(DecisionType.HOLD);
	}
	
	public static Decision stop(Unit unit) {
		return new Decision(DecisionType.STOP);
	}

	@Override
	public String toString() {
		return "Decision [type=" + type + ", unit=" + unit + ", eui=" + eui + "]";
	}
}