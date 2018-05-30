package prebot.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.squad.CheckerSquad;
import prebot.micro.squad.EarlyDefenseSquad;
import prebot.micro.squad.GuerillaSquad;
import prebot.micro.squad.MainAttackSquad;
import prebot.micro.squad.ScvScoutSquad;
import prebot.micro.squad.SpecialSquad;
import prebot.micro.squad.Squad;
import prebot.micro.squad.WatcherSquad;
import prebot.strategy.StrategyIdea;

public class CombatManager {
	private static CombatManager instance = new CombatManager();

	public static CombatManager Instance() {
		return instance;
	}

	// int combatStatus = 0; // 0: 평시

	public SquadData squadData = new SquadData();

	public void onStart() {
		// assign 되지 않는 유닛을 체크하기 위한 Squad
		// IdleSquad idleSquad = new IdleSquad(0, 0);
		// squadData.addSquad(idleSquad);
		// squadData.activateSquad(idleSquad.getSquadName());

		// SCV + 마린 (남는 마린은 벙커 또는 수비 또는 예상확장기지로 이동
		EarlyDefenseSquad earlyDefenseSquad = new EarlyDefenseSquad();
		squadData.addSquad(earlyDefenseSquad);
		
		// (마린) + 벌처 + 탱크 + 골리앗
		MainAttackSquad mainAttackSquad = new MainAttackSquad();
		squadData.addSquad(mainAttackSquad);
		
		WatcherSquad watcherSquad = new WatcherSquad();
		squadData.addSquad(watcherSquad);

		// 정찰용 벌처
		CheckerSquad checkerSquad = new CheckerSquad();
		squadData.addSquad(checkerSquad);
		
		// 게랄라 벌처
		GuerillaSquad guerillaSquad = new GuerillaSquad();
		squadData.addSquad(guerillaSquad);

		// 초반 정찰용 SCV
		ScvScoutSquad scvScoutSquad = new ScvScoutSquad();
		squadData.addSquad(scvScoutSquad);
		
		// 레이스, 베슬, 빌딩 스쿼드
		SpecialSquad specialSquad = new SpecialSquad();
		squadData.addSquad(specialSquad);
		
	}

	public void update() {
		updateSquadData();
		combatUnitArrangement();
		squadExecution();
	}

	private void updateSquadData() {
		for (String squadName : StrategyIdea.squadNameToActivate) {
			squadData.activateSquad(squadName);
		}
		for (String squadName : StrategyIdea.squadNameToDeactivate) {
			squadData.deactivateSquad(squadName);
		}
	}

	private void combatUnitArrangement() {

		// 유효한 공격유닛 필터링(일꾼 포함)
		List<Unit> combatUnitList = new ArrayList<>();
		for (Unit myUnit : Prebot.Broodwar.self().getUnits()) {
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
			for (Unit invalidUnit : squad.invalidUnitList()) {
				squadData.excludeUnitFromSquad(invalidUnit);
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

			squad.findEnemies(); // 적 탐색
			squad.execute(); // squad 유닛 명령 지시
		}
	}
}
