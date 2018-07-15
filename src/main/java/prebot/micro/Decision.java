package prebot.micro;

import bwapi.Unit;
import prebot.common.debug.UXManager;
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
	public Unit myUnit;
	public UnitInfo eui;
	
	private Decision(DecisionType type, Unit myUnit) {
		this.type = type;
		this.myUnit = myUnit;
	}

	private Decision(DecisionType type, Unit myUnit, UnitInfo eui) {
		this.type = type;
		this.myUnit = myUnit;
		this.eui = eui;
	}

	public static Decision kitingUnit(Unit myUnit, UnitInfo eui) {
		Decision decision = new Decision(DecisionType.KITING_UNIT, myUnit, eui);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}

	public static Decision attackUnit(Unit myUnit, UnitInfo eui) {
		Decision decision = new Decision(DecisionType.ATTACK_UNIT, myUnit, eui);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision fleeFromUnit(Unit myUnit, UnitInfo eui) {
		Decision decision = new Decision(DecisionType.FLEE_FROM_UNIT, myUnit, eui);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision attackPosition(Unit myUnit) {
		Decision decision = new Decision(DecisionType.ATTACK_POSITION, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision fleeFromPosition(Unit myUnit) {
		Decision decision = new Decision(DecisionType.FLEE_FROM_POSITION, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision hold(Unit myUnit) {
		Decision decision = new Decision(DecisionType.HOLD, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision stop(Unit myUnit) {
		Decision decision = new Decision(DecisionType.STOP, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision change(Unit myUnit) {
		Decision decision = new Decision(DecisionType.CHANGE_MODE, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision move(Unit myUnit) {
		Decision decision = new Decision(DecisionType.RIGHT_CLICK, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}
	
	public static Decision unite(Unit myUnit) {
		Decision decision = new Decision(DecisionType.UNITE, myUnit);
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
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