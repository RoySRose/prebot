package prebot.micro.manager;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.SquadData;
import prebot.micro.squad.Squad;
import prebot.micro.squad.impl.CheckerSquad;
import prebot.micro.squad.impl.MainDefenseSquad;
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
		MainDefenseSquad mainDefenseSquad = new MainDefenseSquad(1, 100);
		WatcherSquad watcherSquad = new WatcherSquad(2, 100);
		CheckerSquad checkerSquad = new CheckerSquad(3, 100);
		ScvScoutSquad scvScoutSquad = new ScvScoutSquad(4, 100);
		
		squadData.addSquad(mainDefenseSquad);
		squadData.activateSquad(mainDefenseSquad);
		
		squadData.addSquad(watcherSquad);
		squadData.activateSquad(watcherSquad);
		
		squadData.addSquad(checkerSquad);
		squadData.activateSquad(checkerSquad);
		
		squadData.addSquad(scvScoutSquad);
		squadData.activateSquad(scvScoutSquad);
	}

	@Override
	public GameManager update() {
		updateSquadData();
		combatUnitArrangement();
		squadExecution();
		return this;
	}

	private void updateSquadData() {
//		
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
			
			for (Unit unit : combatUnitList) {
				if (!squad.want(unit)) {
					continue;
				}
				squadData.assignUnitToSquad(unit, squad);
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
