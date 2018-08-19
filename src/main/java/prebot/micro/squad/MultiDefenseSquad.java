package prebot.micro.squad;

import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.Region;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.TankControl;
import prebot.micro.targeting.TargetFilter;

public class MultiDefenseSquad extends Squad {
	
	public enum DefenseType {
		CENTER_DEFENSE, REGION_OCCUPY
	}
	
	private TankControl tankControl = new TankControl();
	
	private DefenseType type;
	private Unit commandCenter = null;
	private Region defenseRegion = null;
	private int defenseUnitAssignedFrame = CommonCode.UNKNOWN;
	
	public DefenseType getType() {
		return type;
	}

	public Unit getCommandCenter() {
		return commandCenter;
	}

	public void setDefenseUnitAssignedFrame(int defenseUnitAssignedFrame) {
		this.defenseUnitAssignedFrame = defenseUnitAssignedFrame;
	}
	
	public boolean alreadyDefenseUnitAssigned() {
		if (type == DefenseType.CENTER_DEFENSE) {
			return TimeUtils.elapsedFrames(defenseUnitAssignedFrame) < 2 * TimeUtils.MINUTE;
		} else {
			return TimeUtils.elapsedFrames(defenseUnitAssignedFrame) < 30 * TimeUtils.SECOND;
		}
	}
	
	public boolean tankFull() {
		int max = 8;
		if (type == DefenseType.CENTER_DEFENSE) {
			if (InfoUtils.enemyRace() == Race.Zerg) {
				max = 3;
			} else if (InfoUtils.enemyRace() == Race.Terran) {
				max = 1;
			} else if (InfoUtils.enemyRace() == Race.Protoss) {
				max = 0;
			}
		}
		return unitList.size() >= max;
	}
	
	public MultiDefenseSquad(Region defenseRegion, Position positionForName) {
		super(SquadInfo.MULTI_DEFENSE_, positionForName);
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Vulture);
		
		this.type = DefenseType.REGION_OCCUPY;
		this.defenseRegion = defenseRegion;
	}
	
	public MultiDefenseSquad(Unit commandCenter) {
		super(SquadInfo.MULTI_DEFENSE_, commandCenter);
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Vulture);
		
		this.type = DefenseType.CENTER_DEFENSE;
		this.commandCenter = commandCenter;
	}

	@Override
	public boolean want(Unit unit) {
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		Position defensePosition = null;
		if (type == DefenseType.CENTER_DEFENSE) {
			if (!UnitUtils.isValidUnit(commandCenter)) {
				System.out.println("invalid commandCetner. " + getSquadName());
				return;
			}
			defensePosition = commandCenter.getPosition();
		} else if (type == DefenseType.REGION_OCCUPY) {
//			if (!UnitUtils.isValidUnit(commandCenter)) {
//				System.out.println("region is null. " + getSquadName());
//				return;
//			}
			defensePosition = defenseRegion.getCenter();
		}
		
		euiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.AIR_UNIT|TargetFilter.LARVA_LURKER_EGG);

		tankControl.setMainPosition(defensePosition);
		tankControl.setSaveUnitLevel(2);
		tankControl.controlIfUnitExist(unitList, euiList);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, getTargetPosition(), UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + MicroConfig.COMMON_ADD_RADIUS);
		for (Unit tank : unitList) {
			if (!TimeUtils.executeUnitRotation(tank, LagObserver.groupsize())) {
				continue;
			}
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, tank.getPosition(), 100);
		}
	}
	
	private Position getTargetPosition() {
		if (type == DefenseType.CENTER_DEFENSE) {
			return commandCenter.getPosition();
		} else if (type == DefenseType.REGION_OCCUPY) {
			return defenseRegion.getCenter();
		}
		return null;
	}
}