package prebot.common.util;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;

public class CommandUtils {

	public static void attackUnit(Unit attacker, Unit target) {
		if (!invalidCommand(attacker, target, UnitCommandType.Attack_Unit)) {
			attacker.attack(target);
		}
	}

	public static void attackMove(Unit attacker, final Position targetPosition) {
		if (!invalidCommand(attacker, targetPosition, UnitCommandType.Attack_Move)) {
			attacker.attack(targetPosition);
		}
	}

	public static void move(Unit attacker, final Position targetPosition) {
		if (!invalidCommand(attacker, targetPosition, UnitCommandType.Move)) {
			attacker.move(targetPosition);
		}
	}

	public static void rightClick(Unit unit, Unit target) {
		if (!invalidCommand(unit, target, UnitCommandType.Right_Click_Unit)) {
			unit.rightClick(target);
		}
	}

	public static void repair(Unit unit, Unit target) {
		if (!invalidCommand(unit, target, UnitCommandType.Repair)) {
			unit.repair(target);
		}
	}
	
	public static void load(Unit bunkerOrDropShip, Unit target) {
		if (!invalidCommand(bunkerOrDropShip, target, UnitCommandType.Load)) {
			bunkerOrDropShip.load(target);
		}
	}
	
	public static void unload(Unit bunkerOrDropShip, Unit target) {
		if (!invalidCommand(bunkerOrDropShip, target, UnitCommandType.Unload)) {
			bunkerOrDropShip.unload(target);
		}
	}

	private static boolean invalidCommand(Unit attacker, Unit target, UnitCommandType unitCommandType) {
		if (attacker == null || target == null) {
			return true;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(attacker.getLastCommandFrame()) || attacker.isAttackFrame()) {
			return true;
		}

		// get the unit's current command
		// if we've already told this unit to attack this target, ignore this command
		UnitCommand currentCommand = attacker.getLastCommand();
		if (currentCommand.getUnitCommandType() == unitCommandType) {
			// TODO right click은 position으로 command에 저장되는가?
			if (currentCommand.getUnitCommandType() == UnitCommandType.Right_Click_Unit) {
				if (target.getPosition().equals(currentCommand.getTargetPosition())) {
					return true;
				}
			} else {
				if (currentCommand.getTarget().getID() == target.getID()) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean invalidCommand(Unit attacker, final Position targetPosition, UnitCommandType unitCommandType) {
		// Position 객체에 대해서는 == 가 아니라 equals() 로 비교해야 합니다
		if (attacker == null || !targetPosition.isValid()) {
			return true;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(attacker.getLastCommandFrame()) || attacker.isAttackFrame()) {
			return true;
		}

		// get the unit's current command
		// if we've already told this unit to attack this target, ignore this command
		UnitCommand currentCommand = attacker.getLastCommand();
		if (currentCommand.getUnitCommandType() == unitCommandType && currentCommand.getTargetPosition().equals(targetPosition)) {
			return true;
		}
		
		return false;
	}

}