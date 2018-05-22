package prebot.micro.manager;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.brain.Idea;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.SquadData;
import prebot.micro.squad.Squad;
import prebot.micro.squad.impl.CheckerSquad;
import prebot.micro.squad.impl.MainDefenseSquad;
import prebot.micro.squad.impl.ScoutDefenseSquad;
import prebot.micro.squad.impl.ScvScoutSquad;
import prebot.micro.squad.impl.WatcherSquad;

public class CombatManager extends GameManager {

	private static CombatManager instance = new CombatManager();

	public static CombatManager Instance() {
		return instance;
	}

//	int combatStatus = 0; // 0: 평시

	public SquadData squadData = new SquadData();
	
	public void onStart() {
		// assign 되지 않는 유닛을 체크하기 위한 Squad
//		IdleSquad idleSquad = new IdleSquad(0, 0);
//		squadData.addSquad(idleSquad);
//		squadData.activateSquad(idleSquad.getSquadName());
		
		// 기본 공격병력(마린, 벌처)도 갖춰지지 않은 상태에서 상대일 때 activated상태이다.
		ScoutDefenseSquad scvDefenseSquad = new ScoutDefenseSquad(1, 0);
		squadData.addSquad(scvDefenseSquad);
		squadData.activateSquad(scvDefenseSquad.getSquadName());
		
		// 공격명령이 내려지지 않은 상태의 메인스쿼드
		MainDefenseSquad mainDefenseSquad = new MainDefenseSquad(2, 100);
		squadData.addSquad(mainDefenseSquad);
		squadData.activateSquad(mainDefenseSquad.getSquadName());
		
		// 메인스쿼드와 공격지점 사이를 움직이는 벌처부대(기본적으로 activated상태이고, 본진방어가 필요한 경우 deactivated)
		WatcherSquad watcherSquad = new WatcherSquad(3, 100);
		squadData.addSquad(watcherSquad);
		squadData.activateSquad(watcherSquad.getSquadName());
		
		// 정찰벌처 스쿼드 (마인이 많은 벌처 우선으로 선발됨)
		CheckerSquad checkerSquad = new CheckerSquad(4, 100);
		squadData.addSquad(checkerSquad);
		squadData.activateSquad(checkerSquad.getSquadName());
		
		// SCV 정찰 스쿼드 (초반 정찰을 위해 할당됨)
		ScvScoutSquad scvScoutSquad = new ScvScoutSquad(5, 100);
		squadData.addSquad(scvScoutSquad);
		squadData.activateSquad(scvScoutSquad.getSquadName());
		
		// 비상상태에서 발동되는 SCV 수비
//		ScvDefenseSquad scoutDefenseSquad = new ScvDefenseSquad(5, 100);
//		squadData.addSquad(scoutDefenseSquad);
//		squadData.activateSquad(scoutDefenseSquad.getSquadName());
	}

	@Override
	public GameManager update() {
		updateSquadData();
		combatUnitArrangement();
		squadExecution();
		return this;
	}

	private void updateSquadData() {
		for (String squadName : Idea.of().squadNameToActivate) {
			squadData.activateSquad(squadName);
		}
		for (String squadName : Idea.of().squadNameToDeactivate) {
			squadData.deactivateSquad(squadName);
		}
	}

	private void combatUnitArrangement() {

		// 유효한 공격유닛 필터링(일꾼 포함)
		List<Unit> combatUnitList = new ArrayList<>();
		for (Unit myUnit : Prebot.Game.self().getUnits()) {
			if (!UnitUtils.isValidUnit(myUnit)) {
				continue;
			}
			combatUnitList.add(myUnit);
		}

		// 스쿼드 유닛 할당
		for (Squad squad : squadData.squadMap.values()) {
			if (!squad.isActivated()) {
				continue;
			}
			
			List<Unit> assignableUnitList = new ArrayList<>();
			for (Unit unit : combatUnitList) {
				if (squad.want(unit) && squadData.canAssignUnitToSquad(unit, squad)) {
					assignableUnitList.add(unit);
				}
			}
			List<Unit> recruitUnitList = squad.recruit(assignableUnitList);
			for (Unit recuitUnit : recruitUnitList) {
				squadData.assignUnitToSquad(recuitUnit, squad);
			}
		}
	}

	private void squadExecution() {
		for (Squad squad : squadData.squadMap.values()) {
			if (!squad.isActivated()) {
				continue;
			}
			
			squad.removeInvalidUnits(); // 유효하지 않은 유닛 삭제
			squad.findEnemies(); // 적 탐색
			squad.execute(); // squad 유닛 명령 지시
		}
	}

}
