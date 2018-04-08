package prebot.common.code;

/// 봇 프로그램 설정
public class ConfigForDebug {
	
	public static class BOT_INFO {
		public static final String BOT_NAME = "starport8080";
		public static final String BOT_AUTHOR = "blood-compiler";
	}
	
	public static class DEBUG {
		public static boolean isDebugMode = true;
		
		/// 로컬에서 게임을 실행할 때 게임스피드 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
		/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame<br>
		/// Fastest: 42 ms/frame.  1초에 24 frame. 일반적으로 1초에 24frame을 기준 게임속도로 합니다<br>
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
	
	public static class IO {
		public static String logFilename = BOT_INFO.BOT_NAME + "_LastGameLog.dat"; /// 로그 파일 이름
		public static String readDirectory = "bwapi-data\\read\\"; /// 읽기 파일 경로
		public static String writeDirectory = "bwapi-data\\write\\"; /// 쓰기 파일 경로
	}
	
}