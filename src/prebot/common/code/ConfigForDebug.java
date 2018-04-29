package prebot.common.code;

import bwapi.Color;

/// 봇 프로그램 설정
public class ConfigForDebug {

	public static class BOT_INFO {
		public static final String BOT_NAME = "prebot2";
		public static final String BOT_AUTHOR = "prebotOfFour";
	}

	public static class DEBUG {
		public static boolean isDebugMode = true;

		/// 로컬에서 게임을 실행할 때 게임스피드 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
		/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame<br>
		/// Fastest: 42 ms/frame. 1초에 24 frame. 일반적으로 1초에 24frame을 기준 게임속도로 합니다<br>
		/// Normal: 67 ms/frame. 1초에 15 frame<br>
		/// As fast as possible : 0 ms/frame. CPU가 할수있는 가장 빠른 속도.
		public static int setLocalSpeed = 0;

		/// 로컬에서 게임을 실행할 때 FrameSkip (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
		/// frameskip을 늘리면 화면 표시도 업데이트 안하므로 훨씬 빠릅니다
		public static int setFrameSkip = 0;

		/// 로컬에서 게임을 실행할 때 사용자 키보드/마우스 입력 허용 여부 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)
		public static boolean enableUserInput = true;

		/// 로컬에서 게임을 실행할 때 전체 지도를 다 보이게 할 것인지 여부 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)
		public static boolean enableCompleteMapInformation = false;
	}

	public static class UX {

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

}