package prebot.micro.squad;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.VultureControl;
import prebot.strategy.manage.VultureTravelManager;

public class GuerillaSquad extends Squad {
	private Position targetPosition;
	private VultureControl vultureControl = new VultureControl();
	
	public Position getTargetPosition() {
		return targetPosition;
	}

	public GuerillaSquad(Position position) {
		super(SquadInfo.GUERILLA_, position);
		this.targetPosition = position;
		setUnitType(UnitType.Terran_Vulture);
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
		vultureControl.setTargetPosition(targetPosition);
		vultureControl.control(unitList, euiList);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, targetPosition, UnitType.Terran_Vulture.sightRange() + SquadInfo.CHECKER.squadRadius);
		for (Unit unit : unitList) {
			if (!VultureTravelManager.Instance().guerillaIgnoreModeEnabled(getSquadName())) {
				UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.CHECKER.squadRadius);
			}
		}
	}
}