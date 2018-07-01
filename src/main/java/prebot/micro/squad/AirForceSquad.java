package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.AirForceControl;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceTeam;

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
		// airForceUnit 머지
		AirForceManager.Instance().updateAirForceTeam(unitList);
		
		List<Unit> leaderUnits = new ArrayList<>();
		for (Unit wraith : unitList) {
			if (AirForceManager.Instance().isLeader(wraith.getID())) {
				leaderUnits.add(wraith);
			}
		}
		
		// 리더유닛이 먼저 실행되면 member 유닛들은 그 후 같은 명령을 실행한다.
		for (Unit leaderWraith : leaderUnits) {
			AirForceTeam airForceTeam = AirForceManager.Instance().airForTeamOfWraith(leaderWraith.getID());
			List<UnitInfo> euiList = findEnemies(airForceTeam.memberList);
			airForceControl.control(airForceTeam.memberList, euiList);
		}
	}

	public List<UnitInfo> findEnemies(List<Unit> unitList) {
		List<UnitInfo> euiList = new ArrayList<>();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForAir(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.AIR_FORCE.squadRadius);
		}
		return euiList;
	}
	
	@Override
	public void findEnemies() {
		// nothing
	}
}
