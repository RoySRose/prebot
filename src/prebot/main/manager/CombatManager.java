package prebot.main.manager;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.brain.squad.Squad;
import prebot.common.util.UnitUtils;
import prebot.main.PreBot;
import prebot.micro.SquadData;

public class CombatManager extends GameManager {

	private static CombatManager instance = new CombatManager();

	public static CombatManager Instance() {
		return instance;
	}

	int combatStatus = 0; // 0: 평시

	public SquadData squadData = new SquadData();

	@Override
	public void update() {
		combatUnitArrangement();
		squadExecution();
	}

	private void combatUnitArrangement() {

		// 유효한 공격유닛 필터링(일꾼 포함)
		List<Unit> combatUnitList = new ArrayList<>();
		for (Unit myUnit : PreBot.Broodwar.self().getUnits()) {
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
			squad.update(); // 유효하지 않은 유닛 삭제, 적 탐색
			squad.execute(); // squad 유닛 명령 지시
		}
	}

}
