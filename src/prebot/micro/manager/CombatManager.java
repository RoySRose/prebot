package prebot.micro.manager;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.common.util.UnitUtils;
import prebot.main.GameManager;
import prebot.main.Prebot;
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
		squadData.addOrUpdateSquad(new MainDefenseSquad());
		squadData.addOrUpdateSquad(new WatcherSquad());
		squadData.addOrUpdateSquad(new CheckerSquad());
		squadData.addOrUpdateSquad(new ScvScoutSquad());
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
			squad.removeInvalidUnits(); // 유효하지 않은 유닛 삭제
			squad.findEnemies(); // 적 탐색
			squad.execute(); // squad 유닛 명령 지시
		}
	}

}
