package prebot.micro.squad;

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.AirForceControl;
import prebot.strategy.manage.AirForceManager;

public class AirForceSquad extends Squad {

	AirForceControl airForceControl = new AirForceControl();

	public AirForceSquad() {
		super(SquadInfo.AIR_FORCE);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Wraith;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void setTargetPosition() {
		targetPosition = AirForceManager.Instance().getCurrentTargetPosition();
	}

	@Override
	public void execute() {
		airForceControl.control(unitList, euiList, targetPosition);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		for (Unit wraith : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, wraith.getPosition(), wraith.getType().sightRange() + SquadInfo.AIR_FORCE.squadRadius);
		}
	}
}
