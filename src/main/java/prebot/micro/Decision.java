package prebot.micro;

import prebot.strategy.UnitInfo;

public class Decision {

	public enum DecisionType {
		KITING_UNIT("KU"),
		ATTACK_UNIT("AU"),
		ATTACK_POSITION("AP"),
		FLEE_FROM_UNIT("FU"),
		FLEE_FROM_POSITION("FP"),
		HOLD("H"),
		STOP("S"),
		CHANGE_MODE("C"),
		RIGHT_CLICK("R"),
		UNITE("U");
		
		private DecisionType(String shortName) {
			this.SHORTNAME = shortName;
		}
		
		public String SHORTNAME;
	}

	public DecisionType type;
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
	
	public static Decision stop() {
		return new Decision(DecisionType.STOP);
	}
	
	public static Decision change() {
		return new Decision(DecisionType.CHANGE_MODE);
	}
	
	public static Decision move() {
		return new Decision(DecisionType.RIGHT_CLICK);
	}
	
	public static Decision unite() {
		return new Decision(DecisionType.UNITE);
	}

	@Override
	public String toString() {
		if (eui == null) {
			return type.SHORTNAME;
		} else {
			return type.SHORTNAME + " -> " + eui.getType();
		}
	}
}