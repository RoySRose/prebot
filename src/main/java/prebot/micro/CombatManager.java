package prebot.micro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.debug.BigWatch;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.IConditions.UnitCondition;
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
import prebot.micro.squad.MultiDefenseSquad;
import prebot.micro.squad.ScvScoutSquad;
import prebot.micro.squad.SpecialSquad;
import prebot.micro.squad.Squad;
import prebot.micro.squad.WatcherSquad;
import prebot.micro.targeting.TargetFilter;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
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
		BigWatch.start("combatUnitArrangement");

		// 팩토리
		updateSquadDefault(SquadInfo.MAIN_ATTACK); // 탱크, 골리앗, 발키리
		updateTankDefenseSquad(); // 탱크
		
		updateSquadDefault(SquadInfo.AIR_FORCE); // 레이스
		updateSquadDefault(SquadInfo.SPECIAL); // 베슬
		updateSquadDefault(SquadInfo.BUILDING); // 빌딩, 컴셋

		// SCV유형별 구분
		updateSquadDefault(SquadInfo.EARLY_DEFENSE); // SCV
		updateSquadDefault(SquadInfo.SCV_SCOUT); // SCV

		// 벌처유형별 구분
		updateSquadDefault(SquadInfo.WATCHER); // 벌처
		updateSquadDefault(SquadInfo.CHECKER); // 벌처
		updateGuerillaSquad(); // 벌처
		
		BigWatch.record("combatUnitArrangement");
	}

	private void squadExecution() {
		Squad mainSquad = squadData.getSquadMap().get(SquadInfo.MAIN_ATTACK.squadName);
		mainSquad.findEnemiesAndExecuteSquad();
		
		for (Squad squad : squadData.getSquadMap().values()) {
			squad.findEnemiesAndExecuteSquad(); // squad 유닛 명령 지시
		}
	}

	private void updateSquadDefault(SquadInfo squadInfo) {
		Squad squad = squadData.getSquad(squadInfo.squadName);

		for (Unit invalidUnit : squad.invalidUnitList()) {
			squadData.exclude(invalidUnit);
		}
		
		List<Unit> squadTypeUnitList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, squad.getUnitTypes());
		
		List<Unit> assignableUnitList = new ArrayList<>();
		for (Unit unit : squadTypeUnitList) {
			if (squad.want(unit)) {
				assignableUnitList.add(unit);
			}
		}
		List<Unit> recruitUnitList = squad.recruit(assignableUnitList);
		for (Unit recuitUnit : recruitUnitList) {
			squadData.assign(recuitUnit, squad);
		}
	}

	private void updateTankDefenseSquad() {
		// exclude invalid unit & remove invalid squad
		int defenseTankCount = 0;
		Set<Integer> defenseTankIdSet = new HashSet<>();
		List<Squad> squadList = squadData.getSquadList(SquadInfo.MULTI_DEFENSE_.squadName);
		for (Squad defenseSquad : squadList) {
			if (defenseSquad instanceof MultiDefenseSquad) {
				Unit commandCenter = ((MultiDefenseSquad) defenseSquad).getCommandCenter();
				if (!UnitUtils.isValidUnit(commandCenter)) {
					squadData.removeSquad(defenseSquad.getSquadName());
				}
				for (Unit invalidUnit : defenseSquad.invalidUnitList()) {
					squadData.exclude(invalidUnit);
				}
				
				defenseTankCount += defenseSquad.unitList.size();
				
				for (Unit unit : defenseSquad.unitList) {
					defenseTankIdSet.add(unit.getID());
				}
			} else {
				System.out.println("Not a defenseSquad: " + defenseSquad.getSquadName());
			}
		}
		
//		if (!StrategyIdea.mainSquadMode.isAttackMode) {
//			return;
//		}
		
		List<Unit> tanks = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
		if (tanks.size() / 5 < defenseTankCount) { // 방어 탱크는 전체탱크의 20%를 넘을 수 없다.
			return;
		}
		
		// leader가 readyToPosition을 지나쳤어야 한다. (이동 경로를 확보하기 위함)
		Position leaderPosition = StrategyIdea.mainSquadLeaderPosition;
		Position myReadyTo = InfoUtils.myReadyToPosition();
		Position enemyReadyTo = InfoUtils.enemyReadyToPosition();
		if (!PositionUtils.isAllValidPosition(leaderPosition, myReadyTo, enemyReadyTo)) {
			return;
		}
		double enemyToLeader = enemyReadyTo.getDistance(leaderPosition);
		double enemyToReadyTo = enemyReadyTo.getDistance(myReadyTo);
		
		if (enemyToLeader > enemyToReadyTo) {
			return;
		}
		
		// create defense squad
		List<Unit> commandCenters = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Command_Center);

		Region baseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		Region expansionRegion = BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
		for (Unit commandCenter : commandCenters) {
			Region centerRegion = BWTA.getRegion(commandCenter.getPosition());
//			if (centerRegion == baseRegion) {
//				continue;
//			}
//			if (centerRegion == expansionRegion) {
//				continue;
//			}
			
			Race enemyRace = InfoUtils.enemyRace();
			if (enemyRace == Race.Zerg) {
				if (centerRegion == baseRegion) {
					continue;
				}
			} else if (enemyRace == Race.Protoss) {
				if (centerRegion != baseRegion && centerRegion != expansionRegion) {
					continue;
				}
			} else if (enemyRace == Race.Terran) {
				if (centerRegion == expansionRegion) {
					continue;
				}
			}
			
			String squadName = SquadInfo.MULTI_DEFENSE_.squadName + commandCenter.getID();
			Squad defenseSquad = squadData.getSquad(squadName);
			// 게릴라 스쿼드 생성(포지션 별)
			if (defenseSquad == null) {
				defenseSquad = new MultiDefenseSquad(commandCenter);
				squadData.addSquad(defenseSquad);
			}
		}
		
		// assign units
		List<Squad> updatedSquadList = squadData.getSquadList(SquadInfo.MULTI_DEFENSE_.squadName);
		for (Squad defenseSquad : updatedSquadList) {
			MultiDefenseSquad squad = (MultiDefenseSquad) defenseSquad;
			if (squad.alreadyDefenseUnitAssigned()) {
				continue;
			}
			if (squad.unitList.size() >= 3) {
				continue;
			}
			
			Unit comandCenter = squad.getCommandCenter();
			Set<UnitInfo> euis = UnitUtils.getEnemyUnitInfosInRadius(TargetFilter.UNFIGHTABLE, comandCenter.getPosition(), UnitType.Terran_Command_Center.sightRange(), true, true);
			if (!euis.isEmpty()) {
				continue;
			}
			
			Unit closestTank = UnitUtils.getClosestUnitToPosition(tanks, comandCenter.getPosition(), new UnitCondition() {
				@Override
				public boolean correspond(Unit unit) {
					return !defenseTankIdSet.contains(unit.getID());
				}
			});
			squadData.assign(closestTank, squad);
			squad.setDefenseUnitAssignedFrame(TimeUtils.elapsedFrames());
		}
	}

	private void updateGuerillaSquad() {
		
		int vultureCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Vulture);
		double maxRatio = StrategyIdea.mainSquadMode.maxGuerillaVultureRatio;
		if (StrategyIdea.nearGroundEnemyPosition != null) {
			maxRatio = 0.0d;
		} else {
			if (InfoUtils.enemyRace() == Race.Terran && StrategyIdea.mainSquadMode == MainSquadMode.ATTCK) {
				maxRatio = MainSquadMode.NORMAL.maxGuerillaVultureRatio;
			}
		}
		
		int gurillaFailFrame = VultureTravelManager.Instance().getGurillaFailFrame();
		if (TimeUtils.elapsedFrames(gurillaFailFrame) < 2 * TimeUtils.SECOND) {
			maxRatio = 0.0d;
		}
		
		int maxCount = (int) (vultureCount * maxRatio);

		List<Unit> assignableVultures = new ArrayList<>();
		List<Unit> squadTypeUnitList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture);
		for (Unit unit : squadTypeUnitList) {
			Squad unitSqaud = squadData.getSquad(unit);
			if (unitSqaud instanceof GuerillaSquad) {
				continue;
			}
			
			assignableVultures.add(unit);
			if (assignableVultures.size() >= maxCount) {
				break;
			}
		}

		// 새 게릴라 스퀴드
		newGuerillaSquad(assignableVultures);

		// 게릴라 임무해제
		List<Squad> guerillaSquads = squadData.getSquadList(SquadInfo.GUERILLA_.squadName);
		for (Squad squad : guerillaSquads) {
			for (Unit invalidUnit : squad.invalidUnitList()) {
				squadData.exclude(invalidUnit);
			}
			if (removeGuerilla((GuerillaSquad) squad)) {
				squadData.removeSquad(squad.getSquadName());
			}
		}
	}

	private void newGuerillaSquad(List<Unit> assignableVultures) {
		BaseLocation bestGuerillaSite = VultureTravelManager.Instance().getBestGuerillaSite(assignableVultures);
		if (bestGuerillaSite == null) {
			return;
		}

		// 안개속의 적들을 상대로 계산해서 게릴라 타깃이 가능한지 확인한다.
		Set<UnitInfo> euiList = UnitUtils.getAllEnemyUnitInfosInRadiusForGround(bestGuerillaSite.getPosition(), Vulture.GEURILLA_ENEMY_RADIUS);
		int enemyPower = VultureFightPredictor.powerOfEnemiesByUnitInfo(euiList);
		int vulturePower = VultureFightPredictor.powerOfWatchers(assignableVultures);
		if (vulturePower < enemyPower) {
			return;
		}
		
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
				squadData.assign(assignableVulture, guerillaSquad);
				int squadPower = VultureFightPredictor.powerOfWatchers(guerillaSquad.unitList);
				if (squadPower > enemyPower + Vulture.GEURILLA_EXTRA_ENEMY_POWER) {
					break; // 충분한 파워
				}
			}
		}
		VultureTravelManager.Instance().guerillaStart(squadName);
	}

	private boolean removeGuerilla(GuerillaSquad squad) {
		if (squad.unitList.isEmpty()) {
			VultureTravelManager.Instance().setGurillaFailFrame(TimeUtils.elapsedFrames());
			return true;
		}

		// 게릴라 지역에 적군이 없다.
		if (Prebot.Broodwar.isVisible(squad.getTargetPosition().toTilePosition())) {
			Set<UnitInfo> euiList = UnitUtils.getAllEnemyUnitInfosInRadiusForGround(squad.getTargetPosition(), Vulture.GEURILLA_ENEMY_RADIUS);
			if (euiList.isEmpty()) {
				return true;
			}

			// 일꾼이 없는 경우
			List<Unit> workers = UnitUtils.getUnitsInRadius(PlayerRange.ENEMY, squad.getTargetPosition(), Vulture.GEURILLA_ENEMY_RADIUS, UnitType.Terran_SCV, UnitType.Protoss_Probe, UnitType.Zerg_Drone);
			if (workers.isEmpty()) {
				int vulturePower = VultureFightPredictor.powerOfWatchers(squad.unitList);
				int enemyPower = VultureFightPredictor.powerOfEnemiesByUnitInfo(euiList);
				if (vulturePower < enemyPower - 50) { // 질것 같으면 후퇴
					System.out.println("remove guerialla - retreat");
					return true;
				}

				int guerillaScore = GuerillaScore.guerillaScoreByUnitInfo(euiList); // 이득볼게 없으면 후퇴
				if (guerillaScore <= 0) {
					System.out.println("remove guerialla - nothing to do");
					return true;
				}
			}
		}
		return false;
	}
}
