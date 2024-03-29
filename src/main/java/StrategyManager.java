

import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager extends GameManager {

	private static StrategyManager instance = new StrategyManager();

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
	}

	/// 경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		// saveGameRecordList(isWinner);
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	}

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		if (TimeUtils.executeRotation(LagObserver.managerExecuteRotation(LagObserver.MANAGER1, 0), LagObserver.managerRotationSize())) {
			// 전략 파악
			InitialAction.Instance().update();
			StrategyAnalyseManager.Instance().update();
			ActionManager.Instance().update();
			DefenseTowerTimer.Instance().update();

			SpiderMineManger.Instance().update();
			VultureTravelManager.Instance().update();
			TankPositionManager.Instance().update();	
		}

		if (TimeUtils.executeRotation(LagObserver.managerExecuteRotation(LagObserver.MANAGER1, 1), LagObserver.managerRotationSize())) {
			AirForceManager.Instance().update();
			PositionFinder.Instance().update();
			EnemyBaseFinder.Instance().update();
			expansionOkay();
			changeMainSquadMode();
		}
	}

	/// 테스트용 임시 공격 타이밍
	private void changeMainSquadMode() {
		if (AttackDecisionMaker.Instance().decision == Decision.NO_MERCY_ATTACK) {
			StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.NO_MERCY;
			
		} else if (AttackDecisionMaker.Instance().decision == Decision.FULL_ATTACK) {
			if (StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.QUICK_ATTACK)) {
				StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.SPEED_ATTCK;
			} else {
				StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.ATTCK;
			}
			
		} else {
			StrategyIdea.mainSquadMode = MicroConfig.MainSquadMode.NORMAL;
		}
	}

	private void expansionOkay() {
		boolean expansionOkay = false;
		BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
		List<Unit> commandCenterList = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center);
		for (Unit commandCenter : commandCenterList) {
			if (commandCenter.isLifted()) {
				continue;
			}
				
			if(commandCenter.getTilePosition().getX() == myFirstExpansion.getTilePosition().getX()
					&& commandCenter.getTilePosition().getY() == myFirstExpansion.getTilePosition().getY()) {
				expansionOkay = true;
				break;
			}
		}
		StrategyIdea.EXOK = expansionOkay;
	}
	
}