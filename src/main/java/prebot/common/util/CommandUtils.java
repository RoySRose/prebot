package prebot.common.util;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import prebot.common.main.Prebot;

public class CommandUtils {
	public static void attackUnit(Unit unit, Unit target) {
		if (validCommand(unit, target, UnitCommandType.Attack_Unit, true, false)) {
			unit.attack(target);
		}
	}

	public static void attackMove(Unit unit, Position targetPosition) {
		if (validCommand(unit, targetPosition, UnitCommandType.Attack_Move, true, false)) {
			unit.attack(targetPosition);
		}
	}

	public static void move(Unit unit, Position targetPosition) {
		if (validCommand(unit, targetPosition, UnitCommandType.Move, true, false)) {
			unit.move(targetPosition);
		}
	}

	public static void rightClick(Unit unit, Unit target) {
		if (validCommand(unit, target, UnitCommandType.Right_Click_Unit, false, true)) {
			unit.rightClick(target);
		}
	}

	public static void rightClick(Unit unit, Position position) {
		if (validCommand(unit, position, UnitCommandType.Right_Click_Position, false, true)) {
			unit.rightClick(position);
		}
	}

	public static void repair(Unit unit, Unit target) {
		if (validCommand(unit, target, UnitCommandType.Repair, true, false)) {
			unit.repair(target);
		}
	}

	public static void useTechPosition(Unit unit, TechType tech, Position position) {
		if (validCommand(unit, position, UnitCommandType.Use_Tech_Position, true, false)) {
			unit.useTech(tech, position);
		}
	}

	public static void useTechTarget(Unit unit, TechType tech, Unit target) {
		if (validCommand(unit, target, UnitCommandType.Use_Tech_Unit, true, false)) {
			unit.useTech(tech, target);
		}
	}
	
	public static void load(Unit bunkerOrDropShip, Unit target) {
		if (validCommand(bunkerOrDropShip, target, UnitCommandType.Load, true, false)) {
			bunkerOrDropShip.load(target);
		}
	}
	
	public static void unload(Unit bunkerOrDropShip, Unit target) {
		if (validCommand(bunkerOrDropShip, target, UnitCommandType.Unload, true, false)) {
			bunkerOrDropShip.unload(target);
		}
	}

	public static void siege(Unit tank) {
		if (tank.canSiege() && validCommand(tank, UnitCommandType.Siege)) {
			tank.siege();
		}
	}
	
	public static void unsiege(Unit tank) {
		if (tank.canUnsiege() && validCommand(tank, UnitCommandType.Unsiege)) {
			tank.unsiege();
		}
	}

	public static void holdPosition(Unit unit) {
		if (validCommand(unit, UnitCommandType.Hold_Position)) {
			unit.holdPosition();
		}
	}

	private static boolean validCommand(Unit unit, UnitCommandType commandType) {
		if (!UnitUtils.isValidUnit(unit)) {
			return false;
		}
		if (!TimeUtils.after(unit.getLastCommandFrame())) {
			return false;
		}
		UnitCommand currentCommand = unit.getLastCommand();
		if (currentCommand.getUnitCommandType() == commandType) {
			return false;
		}
		return true;
	}
	
	private static boolean validCommand(Unit unit, Unit target, UnitCommandType commandType, boolean notIssueOnAttackFrame, boolean checkCommandByPosition) {
		if (!UnitUtils.isValidUnit(unit)) {
			return false;
		}
		if (!UnitUtils.isValidUnit(target)) {
			return false;
		}
		if (!TimeUtils.after(unit.getLastCommandFrame())) {
			return false;
		}
		if (notIssueOnAttackFrame && unit.isAttackFrame()) {
			return false;
		}
		UnitCommand currentCommand = unit.getLastCommand();
		if (currentCommand.getUnitCommandType() == commandType) {
			if (checkCommandByPosition) {
				if (currentCommand.getTargetPosition().equals(target.getPosition())) {
					return false;
				}
			} else if (currentCommand.getTarget().getID() == target.getID()) {
				return false;
			}
		}
		return true;
	}

	private static boolean validCommand(Unit unit, Position position, UnitCommandType commandType, boolean notIssueOnAttackFrame, boolean issueIfNotMoving) {
		if (!UnitUtils.isValidUnit(unit)) {
			return false;
		}
		if (!PositionUtils.isValidPosition(position)) {
			return false;
		}
		if (unit.getLastCommandFrame() >= Prebot.Broodwar.getFrameCount()) {
			return false;
		}
		if (notIssueOnAttackFrame && unit.isAttackFrame()) {
			return false;
		}
		
		if (issueIfNotMoving && !unit.isMoving()) {
			return true;
		}
		UnitCommand currentCommand = unit.getLastCommand();
		if (currentCommand.getUnitCommandType() == commandType) {
			if (currentCommand.getTargetPosition().equals(position)) {
				return false;
			}
		}
		
		return true;
	}
}