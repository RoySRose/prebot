package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.AirForceControl;
import prebot.strategy.manage.AirForceManager;

public class AirForceSquad extends Squad {

	private AirForceControl airForceControl = new AirForceControl();

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
	public void execute() {
		// airForceUnit에 할당되지 않은 레이쓰 할당
		for (Unit wraith : unitList) {
			AirForceManager.Instance().assignAirForceUnit(wraith);
		}
		// airForceUnit 머지
		AirForceManager.Instance().mergeAirForceUnit(unitList);
		
		List<Unit> leaderUnits = new ArrayList<>();
		List<Unit> memberUnits = new ArrayList<>();
		for (Unit wraith : unitList) {
			if (AirForceManager.Instance().isLeader(wraith.getID())) {
				leaderUnits.add(wraith);
			} else {
				memberUnits.add(wraith);
			}
		}
		
		// 리더유닛이 먼저 실행되면 member 유닛들은 그 후 같은 명령을 실행하게 된다.
		airForceControl.control(leaderUnits, euiList);
		airForceControl.control(memberUnits, euiList);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		for (Unit wraith : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, wraith.getPosition(), wraith.getType().sightRange() + SquadInfo.AIR_FORCE.squadRadius);
		}
	}
}
