package prebot.common.util;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;

public class CommandUtils {

	public static void attackUnit(Unit attacker, Unit target) {
		if (attacker == null || target == null) {
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(attacker.getLastCommandFrame()) || attacker.isAttackFrame()) {
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to attack this target, ignore this command
		if (currentCommand.getUnitCommandType() == UnitCommandType.Attack_Unit && currentCommand.getTarget().getID() == target.getID()) {
			return;
		}

		// if nothing prevents it, attack the target
		attacker.attack(target);
	}

	public static void attackMove(Unit attacker, final Position targetPosition) {
		// Position 객체에 대해서는 == 가 아니라 equals() 로 비교해야 합니다
		if (attacker == null || !targetPosition.isValid()) {
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(attacker.getLastCommandFrame()) || attacker.isAttackFrame()) {
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to attack this target, ignore this command
		if (currentCommand.getUnitCommandType() == UnitCommandType.Attack_Move && currentCommand.getTargetPosition().equals(targetPosition)) {
			return;
		}

		// if nothing prevents it, attack the target
		attacker.attack(targetPosition);
	}

	public static void move(Unit attacker, final Position targetPosition) {
		if (attacker == null || !targetPosition.isValid()) {
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(attacker.getLastCommandFrame()) || attacker.isAttackFrame()) {
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Move) && (currentCommand.getTargetPosition().equals(targetPosition))
				&& attacker.isMoving()) {
			return;
		}

		// if nothing prevents it, attack the target
		attacker.move(targetPosition);
	}

	public static void rightClick(Unit unit, Unit target) {
		if (unit == null || target == null) {
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(unit.getLastCommandFrame()) || unit.isAttackFrame()) {
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = unit.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Right_Click_Unit)
				&& (target.getPosition().equals(currentCommand.getTargetPosition()))) {
			return;
		}

		// if nothing prevents it, attack the target
		unit.rightClick(target);
	}

	public static void repair(Unit unit, Unit target) {
		if (unit == null || target == null) {
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (!TimeUtils.isPassedFrame(unit.getLastCommandFrame()) || unit.isAttackFrame()) {
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = unit.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Repair) && (currentCommand.getTarget().getID() == target.getID())) {
			return;
		}

		// if nothing prevents it, attack the target
		unit.repair(target);
	}

}