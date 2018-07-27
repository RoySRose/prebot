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
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.AirForceControl;
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
			Set<UnitInfo> euis = findEnemiesForTeam(airForceTeam.memberList);
			airForceControl.control(airForceTeam.memberList, euis);
		}
	}

	public Set<UnitInfo> findEnemiesForTeam(Collection<Unit> unitList) {
		Set<UnitInfo> euis = new HashSet<>();
		
		if (AirForceManager.Instance().isAirForceDefenseMode()) {
//			List<Unit> myBuildings = UnitUtils.getUnitsInRegion(RegionType.MY_BASE, PlayerRange.SELF, new UnitCondition() {
//				@Override public boolean correspond(Unit unit) {
//					return unit.getType().isBuilding() && !unit.isFlying();
//				}
//			});
//			euiList.clear();
//			for (Unit building : myBuildings) {
//				UnitUtils.addEnemyUnitInfosInRadius(euiList, building.getPosition(), building.getType().sightRange() + SquadInfo.AIR_FORCE.squadRadius);
//			}
			
			Set<UnitInfo> mainSquadEnemies = getMainSquadEnemies();
			if (mainSquadEnemies != null) {
				euis = mainSquadEnemies;
				
			} else { // not happen logic
				for (Unit unit : unitList) {
					UnitUtils.addEnemyUnitInfosInRadiusForAir(euis, unit.getPosition(), unit.getType().sightRange() + SquadInfo.AIR_FORCE.squadRadius);
				}
			}
			
		} else {
			for (Unit unit : unitList) {
				UnitUtils.addEnemyUnitInfosInRadius(euis, unit.getPosition(), unit.getType().sightRange() + SquadInfo.AIR_FORCE.squadRadius);
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
