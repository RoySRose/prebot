package prebot.micro.squad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.CombatManager;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.AirForceControl;
import prebot.micro.targeting.TargetFilter;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceTeam;

public class AirForceSquad extends Squad {

	private AirForceControl airForceControl = new AirForceControl();

	public AirForceSquad() {
		super(SquadInfo.AIR_FORCE);
		setUnitType(UnitType.Terran_Wraith);
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
		if (!AirForceManager.Instance().airForceManagerInitialized()) {
			return;
		}

		AirForceManager.Instance().updateAirForceTeam(unitList);
		
		List<Unit> leaderAirunits = new ArrayList<>();
		for (Unit airunit : unitList) {
			if (AirForceManager.Instance().isLeader(airunit.getID())) {
				leaderAirunits.add(airunit);
			}
		}
		
		// 리더유닛이 먼저 실행되면 member 유닛들은 그 후 같은 명령을 실행한다.
		for (Unit leaderAirunit : leaderAirunits) {
			AirForceTeam airForceTeam = AirForceManager.Instance().airForTeamOfUnit(leaderAirunit.getID());
			Set<UnitInfo> euis = findEnemiesForTeam(airForceTeam.memberList);
			airForceControl.controlIfUnitExist(airForceTeam.memberList, euis);
		}
	}

	public Set<UnitInfo> findEnemiesForTeam(Collection<Unit> unitList) {
		Set<UnitInfo> euis = new HashSet<>();
		
		if (AirForceManager.Instance().isAirForceDefenseMode()) {
			Set<UnitInfo> mainSquadEnemies = getMainSquadEnemies();
			if (mainSquadEnemies != null) {
				euis = mainSquadEnemies;
			}
			
			if (StrategyIdea.mainSquadMode.isAttackMode) {
				for (Unit unit : unitList) {
					if (AirForceManager.Instance().isLeader(unit.getID())) {
						UnitUtils.addEnemyUnitInfosInRadius(TargetFilter.LARVA_LURKER_EGG, euis, unit.getPosition(), unit.getType().sightRange() + MicroConfig.COMMON_ADD_RADIUS, false, true);
					}
				}
			}
			
		} else {
			for (Unit unit : unitList) {
				if (AirForceManager.Instance().isLeader(unit.getID())) {
					UnitUtils.addEnemyUnitInfosInRadius(TargetFilter.LARVA_LURKER_EGG, euis, unit.getPosition(), unit.getType().sightRange() + MicroConfig.COMMON_ADD_RADIUS, false, true);
				}
			}
		}
		return euis;
	}
	
	private Set<UnitInfo> getMainSquadEnemies() {
		MainAttackSquad mainSquad = (MainAttackSquad) CombatManager.Instance().squadData.getSquad(SquadInfo.MAIN_ATTACK.squadName);
		if (mainSquad.squadExecuted()) {
			return mainSquad.euiList;
		} else {
			System.out.println("#### SOMETHING'S WRONG!!! MAIN SQUAD'S EUILIST MUST NOT BE EMPTY ####");
			return null;
		}
	}

	@Override
	public void findEnemies() {
		// nothing
	}
}
