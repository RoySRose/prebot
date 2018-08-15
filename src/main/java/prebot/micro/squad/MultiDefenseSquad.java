package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.MicroUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.TankControl;
import prebot.micro.targeting.TargetFilter;

public class MultiDefenseSquad extends Squad {
	private TankControl tankControl = new TankControl();
	private Unit commandCenter = null;
	private int defenseUnitAssignedFrame = CommonCode.UNKNOWN;
	
	public Unit getCommandCenter() {
		return commandCenter;
	}

	public void setDefenseUnitAssignedFrame(int defenseUnitAssignedFrame) {
		this.defenseUnitAssignedFrame = defenseUnitAssignedFrame;
	}
	
	public boolean alreadyDefenseUnitAssigned() {
		return TimeUtils.elapsedFrames(defenseUnitAssignedFrame) < 4 * TimeUtils.MINUTE;
	}
	
	public MultiDefenseSquad(Unit commandCenter) {
		super(SquadInfo.MULTI_DEFENSE_, commandCenter.getID());
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
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
		if (!UnitUtils.isValidUnit(commandCenter)) {
			System.out.println("invalid commandCetner. " + getSquadName());
			return;
		}
		
		euiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.AIR_UNIT|TargetFilter.LARVA_LURKER_EGG);
		
		tankControl.setSaveUnitLevel(2);
		tankControl.setMainPosition(commandCenter.getPosition());
		tankControl.controlIfUnitExist(unitList, euiList);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, commandCenter.getPosition(), UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + MicroConfig.COMMON_ADD_RADIUS);
		for (Unit tank : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, tank.getPosition(), UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange());
		}
	}
}