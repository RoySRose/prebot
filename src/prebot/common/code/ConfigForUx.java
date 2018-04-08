package prebot.common.code;

import bwapi.Color;

public class ConfigForUx {

	/// 화면 표시 여부 - 게임 정보
	public static boolean DrawGameInfo = true;
	/// 화면 표시 여부 - 미네랄, 가스
	public static boolean DrawResourceInfo = true;
	/// 화면 표시 여부 - 지도
	public static boolean DrawBWTAInfo = true;
	/// 화면 표시 여부 - 바둑판
	public static boolean DrawMapGrid = false;
	/// 화면 표시 여부 - 유닛 HitPoint
	public static boolean DrawUnitHealthBars = true;
	/// 화면 표시 여부 - 유닛 통계
	public static boolean DrawEnemyUnitInfo = true;
	/// 화면 표시 여부 - 유닛 ~ Target 간 직선
	public static boolean DrawUnitTargetInfo = true;
	/// 화면 표시 여부 - 빌드 큐
	public static boolean DrawProductionInfo = true;
	/// 화면 표시 여부 - 건물 Construction 상황
	public static boolean DrawBuildingInfo = true;
	/// 화면 표시 여부 - 건물 ConstructionPlace 예약 상황
	public static boolean DrawReservedBuildingTiles = false;
	/// 화면 표시 여부 - 정찰 상태
	public static boolean DrawScoutInfo = true;
	/// 화면 표시 여부 - 일꾼 목록
	public static boolean DrawWorkerInfo = true;
	/// 화면 표시 여부 - 마우스 커서	
	public static boolean DrawMouseCursorInfo = true;
	/// 화면 표시 여부 - Manager별 소요 시간
	public static boolean DrawManagerTimeSpent = true;
	
	public static final Color ColorLineTarget = Color.White;
	public static final Color ColorLineMineral = Color.Cyan;
	public static final Color ColorUnitNearEnemy = Color.Red;
	public static final Color ColorUnitNotNearEnemy = Color.Green;

}
