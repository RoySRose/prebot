package prebot.micro;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.information.UnitInfo;

public class Decision {

	public DecisionType decisionType;
	public Unit unit;
	
	public Position position;
	public UnitInfo eui;
	public KitingOption kOption;

	private Decision(DecisionType decisionType, Unit unit) {
		this.decisionType = decisionType;
		this.unit = unit;
	}

	public static Decision kitingUnit(Unit unit, UnitInfo eui, KitingOption kOption) {
		Decision decision = new Decision(DecisionType.KITING_UNIT, unit);
		decision.eui = eui;
		decision.kOption = kOption;
		return decision;
	}

	public static Decision attackUnit(Unit unit, UnitInfo eui) {
		Decision decision = new Decision(DecisionType.ATTACK_UNIT, unit);
		decision.eui = eui;
		return decision;
	}
	
	public static Decision attackPosition(Unit unit, Position position) {
		Decision decision = new Decision(DecisionType.ATTACK_POSITION, unit);
		decision.position = position;
		return decision;
	}
	
	public static Decision fleeFromUnit(Unit unit, UnitInfo eui, KitingOption kOption) {
		Decision decision = new Decision(DecisionType.FLEE_FROM_UNIT, unit);
		decision.eui = eui;
		decision.kOption = kOption;
		return decision;
	}
	
	public static Decision fleeFromPosition(Unit unit, Position position, KitingOption kOption) {
		Decision decision = new Decision(DecisionType.FLEE_FROM_POSITION, unit);
		decision.position = position;
		decision.kOption = kOption;
		return decision;
	}
	
	public static Decision hold(Unit unit) {
		Decision decision = new Decision(DecisionType.HOLD, unit);
		return decision;
	}
	
	public static Decision stop(Unit unit) {
		Decision decision = new Decision(DecisionType.STOP, unit);
		return decision;
	}

	public enum DecisionType {
		KITING_UNIT(1), ATTACK_UNIT(1), ATTACK_POSITION(1),
		FLEE_FROM_UNIT(2), FLEE_FROM_POSITION(2),
		HOLD(3), STOP(3);

		final int decisionTypeGroup; // 1:공격, 2:회피, 3:정지

		DecisionType(int decisionTypeGroup) {
			this.decisionTypeGroup = decisionTypeGroup;
		}
		
		@Override
		public String toString() {
			return this.name();
		}
	}

	public void perform() {
		if (decisionType == null) {
			return;
		}

		if (decisionType.decisionTypeGroup == 1) {
			performAttack();
		} else if (decisionType.decisionTypeGroup == 2) {
			performFlee();
		} else if (decisionType.decisionTypeGroup == 3) {
			performStop();
		}
	}

	private void performAttack() {
		if (decisionType == DecisionType.KITING_UNIT) {
			MicroUtils.kiting(unit, eui, kOption);
		} else if (decisionType == DecisionType.ATTACK_UNIT) {
			if (MicroUtils.enemyUnitInSight(eui) != null) {
				CommandUtils.attackUnit(unit, eui.unit);
			} else {
				CommandUtils.move(unit, eui.lastPosition);
			}
		} else if (decisionType == DecisionType.ATTACK_POSITION) {
			CommandUtils.attackMove(unit, position);

		}
	}

	private void performFlee() {
		if (decisionType == DecisionType.FLEE_FROM_UNIT) {
			MicroUtils.flee(unit, eui.lastPosition, kOption.fOption);
		} else if (decisionType == DecisionType.FLEE_FROM_POSITION) {
			MicroUtils.flee(unit, position, kOption.fOption);	
		}
	}

	private void performStop() {
		if (decisionType == DecisionType.HOLD) {
			unit.holdPosition();
		} else if (decisionType == DecisionType.STOP) {
			unit.stop();
		}
	}

	@Override
	public String toString() {
		return "Decision [decisionType=" + decisionType + ", unit=" + unit.getType() + "(" + unit.getID() + ")" + ", position=" + position + ", eui=" + eui + ", kOption=" + kOption + "]";
	}
}