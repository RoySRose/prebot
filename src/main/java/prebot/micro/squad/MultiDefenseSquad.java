package prebot.micro.squad;

import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.control.factory.TankControl;
import prebot.micro.targeting.TargetFilter;

public class MultiDefenseSquad extends Squad {
	
	public enum DefenseType {
		CENTER_DEFENSE, REGION_OCCUPY
	}
	
	private TankControl tankControl = new TankControl();
	
	private MultiDefenseSquad.DefenseType type;
	private Unit commandCenter = null;
	private Position defensePosition = null;
	private int defenseUnitAssignedFrame = CommonCode.UNKNOWN;
	
	public MultiDefenseSquad.DefenseType getType() {
		return type;
	}

	public Unit getCommandCenter() {
		return commandCenter;
	}

	public void setDefenseUnitAssignedFrame(int defenseUnitAssignedFrame) {
		this.defenseUnitAssignedFrame = defenseUnitAssignedFrame;
	}
	
	public boolean alreadyDefenseUnitAssigned() {
		if (type == MultiDefenseSquad.DefenseType.CENTER_DEFENSE) {
			return TimeUtils.elapsedFrames(defenseUnitAssignedFrame) < 2 * TimeUtils.MINUTE;
		} else {
			return TimeUtils.elapsedFrames(defenseUnitAssignedFrame) < 30 * TimeUtils.SECOND;
		}
	}
	
	public boolean tankFull() {
		int max = 8;
		if (type == MultiDefenseSquad.DefenseType.CENTER_DEFENSE) {
			if (InfoUtils.enemyRace() == Race.Zerg) {
				max = 3;
			} else if (InfoUtils.enemyRace() == Race.Terran) {
				if (!UnitUtils.enemyUnitDiscovered(UnitType.Terran_Dropship)) {
					max = 1;
				} else {
					max = 3;
				}
			} else if (InfoUtils.enemyRace() == Race.Protoss) {
				max = 0;
			}
		}
		return unitList.size() >= max;
	}
	
	public MultiDefenseSquad(Position basePosition) {
		super(MicroConfig.SquadInfo.MULTI_DEFENSE_, basePosition);
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Vulture);
		
		Position centerPosition = TilePositionUtils.getCenterTilePosition().toPosition();
		double radian = MicroUtils.targetDirectionRadian(centerPosition, basePosition);
		Position defensePosition = MicroUtils.getMovePosition(centerPosition, radian, 1000).makeValid();
		
		this.type = MultiDefenseSquad.DefenseType.REGION_OCCUPY;
		this.defensePosition = defensePosition;
	}
	
	public MultiDefenseSquad(Unit commandCenter) {
		super(MicroConfig.SquadInfo.MULTI_DEFENSE_, commandCenter);
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Vulture);
		
		this.type = MultiDefenseSquad.DefenseType.CENTER_DEFENSE;
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
		if (type == MultiDefenseSquad.DefenseType.CENTER_DEFENSE) {
			if (!UnitUtils.isValidUnit(commandCenter)) {
				System.out.println("invalid commandCetner. " + getSquadName());
				return;
			}
			defensePosition = commandCenter.getPosition();
		} else if (type == MultiDefenseSquad.DefenseType.REGION_OCCUPY) {
//			if (!UnitUtils.isValidUnit(commandCenter)) {
//				System.out.println("region is null. " + getSquadName());
//				return;
//			}
			defensePosition = this.defensePosition;
		}
		
		euiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.AIR_UNIT|TargetFilter.LARVA_LURKER_EGG);

		tankControl.setMainPosition(defensePosition);
		tankControl.setSaveUnitLevel(2);
		tankControl.controlIfUnitExist(unitList, euiList);
	}

	@Override
	public void findEnemies() {
		boolean findNearCenter = false;
		
		euiList.clear();
		Position targetPosition = getTargetPosition();
		for (Unit tank : unitList) {
			if (!TimeUtils.executeUnitRotation(tank, LagObserver.groupsize())) {
				continue;
			}
			if (tank.getDistance(targetPosition) < 250) {
				continue;
			}
			if (!TimeUtils.executeUnitRotation(tank, LagObserver.groupsize())) {
				continue;
			}
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, tank.getPosition(), 100);
			findNearCenter = true;
		}
		if (findNearCenter) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, targetPosition, UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + MicroConfig.COMMON_ADD_RADIUS);
		}
	}
	
	private Position getTargetPosition() {
		if (type == MultiDefenseSquad.DefenseType.CENTER_DEFENSE) {
			return commandCenter.getPosition();
		} else if (type == MultiDefenseSquad.DefenseType.REGION_OCCUPY) {
			return this.defensePosition;
		}
		return null;
	}
}