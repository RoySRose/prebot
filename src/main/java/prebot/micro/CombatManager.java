package prebot.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.constant.MicroConfig.Vulture;
import prebot.micro.predictor.GuerillaScore;
import prebot.micro.predictor.VultureFightPredictor;
import prebot.micro.squad.AirForceSquad;
import prebot.micro.squad.BuildingSquad;
import prebot.micro.squad.CheckerSquad;
import prebot.micro.squad.EarlyDefenseSquad;
import prebot.micro.squad.GuerillaSquad;
import prebot.micro.squad.MainAttackSquad;
import prebot.micro.squad.ScvScoutSquad;
import prebot.micro.squad.SpecialSquad;
import prebot.micro.squad.Squad;
import prebot.micro.squad.WatcherSquad;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;
import prebot.strategy.manage.VultureTravelManager;

public class CombatManager extends GameManager {
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

		// SCV + 마린
		// * 포지션: campPosition
		// * 초반 수비(정찰일꾼 견제, 일꾼러시, 가스러시, 파일런러시, 포톤러시, 초반 저글링/마린/질럿 등)
		EarlyDefenseSquad earlyDefenseSquad = new EarlyDefenseSquad();
		squadData.addSquad(earlyDefenseSquad);

		// (마린) + 탱크 + 골리앗
		// * 본진 수비 및 적 공격
		// * 정찰 SCV와 연계된 벙커링 (TBD)
		MainAttackSquad mainAttackSquad = new MainAttackSquad();
		squadData.addSquad(mainAttackSquad);

		// 감시 벌처
		// * 적 감시 및 견제, 적 공격, 마인매설(NOT_MY_OCCUPIED OR ANYWHERE)
		WatcherSquad watcherSquad = new WatcherSquad();
		squadData.addSquad(watcherSquad);

		// 정찰 벌처
		// * 적 진영 정찰, 마인매설(ONLY_GOOD_POSITION)
		CheckerSquad checkerSquad = new CheckerSquad();
		squadData.addSquad(checkerSquad);

		// 초반 정찰용 SCV
		// * 적 base 탐색, 일꾼견제
		// * 12드론 앞마당시 벙커링 (TBD)
		ScvScoutSquad scvScoutSquad = new ScvScoutSquad();
		squadData.addSquad(scvScoutSquad);

		// 레이쓰 특공대
		AirForceSquad airForceSquad = new AirForceSquad();
		squadData.addSquad(airForceSquad);

		// 개별 유닛 - 베슬, 드랍십
		SpecialSquad specialSquad = new SpecialSquad();
		squadData.addSquad(specialSquad);

		// 배럭, 컴셋 등 빌딩
		BuildingSquad buildingSquad = new BuildingSquad();
		squadData.addSquad(buildingSquad);
	}

	public void update() {
		updateSquadData();
		combatUnitArrangement();
		squadExecution();
	}

	private void updateSquadData() {
	}

	private void combatUnitArrangement() {

		// 유효한 공격유닛 필터링(일꾼 포함)
		List<Unit> combatUnitList = new ArrayList<>();
		for (Unit myUnit : Prebot.Broodwar.self().getUnits()) {
			if (!UnitUtils.isCompleteValidUnit(myUnit)) {
				continue;
			}
			combatUnitList.add(myUnit);
		}

		updateSquadDefault(SquadInfo.EARLY_DEFENSE, combatUnitList);
		updateSquadDefault(SquadInfo.MAIN_ATTACK, combatUnitList);
		updateSquadDefault(SquadInfo.WATCHER, combatUnitList);
		updateSquadDefault(SquadInfo.CHECKER, combatUnitList);
		updateSquadDefault(SquadInfo.SCV_SCOUT, combatUnitList);
		updateSquadDefault(SquadInfo.AIR_FORCE, combatUnitList);
		updateSquadDefault(SquadInfo.SPECIAL, combatUnitList);
		updateSquadDefault(SquadInfo.BUILDING, combatUnitList);

		updateDefenseSquad(combatUnitList);
		updateGuerillaSquad(combatUnitList);
	}

	private void updateSquadDefault(SquadInfo squadInfo, List<Unit> combatUnitList) {
		Squad squad = squadData.getSquad(squadInfo.squadName);

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

	private void squadExecution() {
		for (Squad squad : squadData.getSquadMap().values()) {
			squad.findEnemies(); // 적 탐색
			squad.execute(); // squad 유닛 명령 지시
		}
	}

	private void updateDefenseSquad(List<Unit> combatUnitList) {
		// TODO Auto-generated method stub

	}

	private void updateGuerillaSquad(List<Unit> combatUnitList) {
		int vultureCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Vulture);
		double maxRatio = MainSquadMode.NORMAL.maxGuerillaVultureRatio;
		int maxCount = (int) (vultureCount * maxRatio);

		List<Unit> assignableVultures = new ArrayList<>();
		for (Unit unit : combatUnitList) {
			if (unit.getType() != UnitType.Terran_Vulture) {
				continue;
			}
			Squad unitSqaud = squadData.getUnitSquad(unit);
			if (unitSqaud == null || SquadInfo.GUERILLA_.priority > unitSqaud.getPriority()) {
				assignableVultures.add(unit);
			}
			if (assignableVultures.size() > maxCount) {
				break;
			}
		}

		// 새 게릴라 스퀴드
		newGuerillaSquad(assignableVultures);

		// 게릴라 임무해제
		List<Squad> guerillaSquads = squadData.getSquadList(SquadInfo.GUERILLA_.squadName);
		for (Squad squad : guerillaSquads) {
			if (squad instanceof GuerillaSquad) {
				for (Unit invalidUnit : squad.invalidUnitList()) {
					squadData.excludeUnitFromSquad(invalidUnit);
				}
				if (removeGuerilla((GuerillaSquad) squad)) {
					squadData.removeSquad(squad.getSquadName());
				}
			}
		}
	}

	private void newGuerillaSquad(List<Unit> assignableVultures) {
		BaseLocation bestGuerillaSite = VultureTravelManager.Instance().getBestGuerillaSite(assignableVultures);
		if (bestGuerillaSite != null) {
			// 안개속의 적들을 상대로 계산해서 게릴라 타깃이 가능한지 확인한다.
			List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfosInRadiusForGround(bestGuerillaSite.getPosition(), Vulture.GEURILLA_ENEMY_RADIUS);
			int enemyPower = VultureFightPredictor.powerOfEnemiesByUnitInfo(euiList);
			int vulturePower = VultureFightPredictor.powerOfWatchers(assignableVultures);

			if (vulturePower > enemyPower) {
				String squadName = SquadInfo.GUERILLA_.squadName + bestGuerillaSite.getPosition().toString();
				Squad guerillaSquad = squadData.getSquad(squadName);
				// 게릴라 스쿼드 생성(포지션 별)
				if (guerillaSquad == null) {
					guerillaSquad = new GuerillaSquad(bestGuerillaSite.getPosition());
					squadData.addSquad(guerillaSquad);
				}
				// 게릴라 유닛이 남아있지 않다면 할당한다.
				if (guerillaSquad.unitList.isEmpty()) {
					for (Unit assignableVulture : assignableVultures) {
						squadData.assignUnitToSquad(assignableVulture, guerillaSquad);
						int squadPower = VultureFightPredictor.powerOfWatchers(guerillaSquad.unitList);
						if (squadPower > enemyPower + Vulture.GEURILLA_EXTRA_ENEMY_POWER) {
							break; // 충분한 파워
						}
					}
				}
				VultureTravelManager.Instance().guerillaStart(squadName);
			}
		}
	}

	private boolean removeGuerilla(GuerillaSquad squad) {
		if (squad.unitList.isEmpty()) {
			return true;
		}

		// 게릴라 지역에 적군이 없다.
		if (Prebot.Broodwar.isVisible(squad.getTargetPosition().toTilePosition())) {
			List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfosInRadiusForGround(squad.getTargetPosition(), Vulture.GEURILLA_ENEMY_RADIUS);
			if (euiList.isEmpty()) {
				return true;
			}

			// 일꾼이 없는 경우
			List<Unit> workers = UnitUtils.getUnitsInRadius(PlayerRange.ENEMY, squad.getTargetPosition(), Vulture.GEURILLA_ENEMY_RADIUS, UnitType.Terran_SCV, UnitType.Protoss_Probe, UnitType.Zerg_Drone);
			if (workers.isEmpty()) {
				SmallFightPredict predict = VultureFightPredictor.watcherPredictByUnitInfo(squad.unitList, euiList);
				if (predict == SmallFightPredict.BACK) {
					return true;
				}

				int guerillaScore = GuerillaScore.guerillaScoreByUnitInfo(euiList);
				if (guerillaScore <= 0) {
					return true;
				}
			}
		}
		return false;
	}
}
