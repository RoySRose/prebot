package prebot.strategy;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.RegionType;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;

public class StrategyIdea {
	
	// [적 전략 및 빌드매니저 대응 정보]
	// 빌드매니저: 초반빌드, 마린수, 팩토리유닛비율, 업그레이드순서, 애드온 타이밍 등의 정보를 사용
	// 컴뱃매니저: 상대 전략에 따른 마이크로 변화 (TBD)
	// - watcher : 전략에 따라 벌처의 MinePositionLevel이 변경된다.
	// - barrack : 입구봉쇄, 위치 변경 등
	public static EnemyStrategy startStrategy = EnemyStrategy.UNKNOWN;
	public static EnemyStrategy currentStrategy = EnemyStrategy.UNKNOWN;
	public static List<EnemyStrategy> strategyHistory = new ArrayList<>();

	// 벙커가 필요한 frame (bunkerBuildStartFrame 이전에는 벙커를 짓지 않는다.) 
	public static int bunkerNeedFrame = CommonCode.INT_MAX;
	public static int bunkerBuildStartFrame = CommonCode.INT_MAX;
	public static RegionType bunkerRegionType = RegionType.MY_BASE;

	// 터렛이 필요한 frame (turretBuildStartFrame 이전에는 터렛을 짓지 않는다.)
	public static int turretNeedFrame = CommonCode.INT_MAX;
	public static int turretBuildStartFrame = CommonCode.INT_MAX;
	public static int engineeringBayBuildStartFrame = CommonCode.INT_MAX;
	public static List<RegionType> turretRegionType = new ArrayList<>();

	// 적 전략 특이사항
	public static boolean gasRushed = false;
	public static boolean photonRushed = false;
	
	// [적군 위치에 따른 상태 파악]
	// 빌드매니저: COMMING, IN_MY_REGION인 경우 벌처, 팩토리가 없는 경우 마린을 우선적으로 추가 생산한다. (마린 6기 이상이면 추가생산 없음, 벌처 2기 이상이면 추가생산 없음 - 적용 특이사항 예시) (initial이 끝나기 전)
	// 컴뱃매니저: ?
	public static EnemyUnitStatus enemyUnitStatus = EnemyUnitStatus.SLEEPING;
	public static Position enemySquadPosition = null; // 적 유닛 포지션
	
	// [아군 메인부대의 전투개시 여부]
	// 컴뱃매니저: 시즈모드 변경시, watcher전투 판단시 
	public static boolean initiated = false;
	
	// [메인스쿼드 모드]
	// 모드에 따라 mainSquadPosition이 변경된다.
	// 모드에 따라 게릴라벌처 투입 비율이 조정된다.
	// 수비모드, 공격모드에 따라 적 선택 방식이 달라진다.(수비모드인 경우 건물 위주, 공격모드인 경우 유닛 위주)
	// 공격모드 종류에 따라 적 진영으로 빠르게 전진할지, 부하가 걸리는 컨트롤을 사용할지 여부 등을 결정한다. 
	public static MainSquadMode mainSquadMode = MainSquadMode.NORMAL;
	
	// [포지션 정보]
	public static Position campPosition = null; // 수비 포지션
	public static Position mainPosition = null; // 메인스쿼드 목표 포지션
	public static Position watcherPosition = null; // watcher 포지션
	public static Position mainSquadCenter = null; // 메인스쿼드 유닛들의 센터 포지션

	public static BaseLocation enemyBaseExpected = null; // 적base 예상 지점

	public static int checkerMaxNumber = 0; // 정찰벌처 최대수
	public static int spiderMineNumberPerPosition = 1; // 
	public static int spiderMineNumberPerGoodPosition = 1;
	
	// [전략에 따른 가스조절]
	public static boolean gasAdjustment = false;
	public static int gasAdjustmentWorkerCount = 0;

	// [정찰SCV 할당]
	public static boolean assignScoutScv = false;
	
}
