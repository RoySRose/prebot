package prebot.micro.squad;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.VultureControl;
import prebot.micro.targeting.TargetFilter;
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
		euiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.AIR_UNIT|TargetFilter.LARVA_LURKER_EGG);
		vultureControl.setTargetPosition(targetPosition);
		vultureControl.controlIfUnitExist(unitList, euiList);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, targetPosition, UnitType.Terran_Vulture.sightRange() + MicroConfig.COMMON_ADD_RADIUS);
		for (Unit unit : unitList) {
			if (!VultureTravelManager.Instance().guerillaIgnoreModeEnabled(getSquadName())) {
				UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, unit.getPosition(), unit.getType().sightRange() + MicroConfig.COMMON_ADD_RADIUS);
			}
		}
	}
}