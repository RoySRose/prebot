package prebot.strategy.constant;

public class StrategyConfig {

	public static final int APPROX_DISTANCE_1280 = 1280;
	public static final int APPROX_DISTANCE_640 = 640;
	public static final int APPROX_DISTANCE_320 = 320;
	
	public static final int SCAN_DURATION = 240; // approximate time that a comsat scan provides vision
	public static final int IGNORE_ENEMY_UNITINFO_SECONDS = 15;
	
	// 기본 전략
	public static enum EnemyStrategy {
		ZERGBASIC(0, 2, 10, 1),
		ZERGBASIC_HYDRAWAVE(1, 5, 2, 2),
//		ZERGBASIC_GIFTSET(0, 2, 10, 1),
		ZERGBASIC_HYDRAMUTAL(1, 5, 2, 2),
		ZERGBASIC_LINGHYDRA(0, 2, 10, 1),
		ZERGBASIC_LINGLURKER(1, 5, 6, 3),
		ZERGBASIC_LINGMUTAL(1, 4, 3, 1),
		ZERGBASIC_LINGULTRA(2, 4, 4, 1),
		ZERGBASIC_MUTAL(1, 2, 10, 1),
		ZERGBASIC_MUTALMANY(5, 6, 2, 1),
		ZERGBASIC_ULTRA(1, 1, 9, 3),
		
		PROTOSSBASIC(0, 1, 12, 3),
		PROTOSSBASIC_CARRIER(5, 6, 1, 1),
		PROTOSSBASIC_DOUBLEPHOTO(5, 5, 0, 2),
		
		TERRANBASIC(1, 2, 8, 3),
		TERRANBASIC_BIONIC(3, 3, 0, 1),
		TERRANBASIC_MECHANIC(0, 2, 0, 1),
		TERRANBASIC_MECHANICWITHWRAITH(10, 5, 0, 1),
		TERRANBASIC_MECHANICAFTER(5, 8, 1, 2),
		TERRANBASIC_BATTLECRUISER(0, 7, 1, 3),
		ATTACKISLAND(0, 9, 1, 3)
		;

		public int vultureRatio;
		public int tankRatio;
		public int goliathRatio;
		public int weight;

		private EnemyStrategy(int vultureRatio, int tankRatio, int goliathRatio, int weight) {
			this.vultureRatio = vultureRatio;
			this.tankRatio = tankRatio;
			this.goliathRatio = goliathRatio;
			this.weight = weight;
		}
	}

	//예외 전략. 예외가 아닐때는 무조건 INIT 으로
	public static enum EnemyStrategyException {
		ZERGEXCEPTION_FASTLURKER(1, 4, 1, 2),
//		ZERGEXCEPTION_GUARDIAN(0, 2, 10, 1),
//		ZERGEXCEPTION_NONGBONG(0, 2, 10, 1),
		ZERGEXCEPTION_ONLYLING(7, 3, 2, 1),
		ZERGEXCEPTION_PREPARELURKER(0, 0, 0, 1),
		ZERGEXCEPTION_REVERSERUSH(1, 3, 3, 2),
		ZERGEXCEPTION_HIGHTECH(0, 0, 0, 1),
//		PROTOSSEXCEPTION_CARRIERMANY(9, 3, 0, 1),
		PROTOSSEXCEPTION_DARK(9, 3, 0, 1),
//		PROTOSSEXCEPTION_REAVER(8, 3, 1, 1),
		PROTOSSEXCEPTION_SCOUT(7, 3, 2, 1),
		PROTOSSEXCEPTION_SHUTTLE(7, 3, 2, 1),
//		PROTOSSEXCEPTION_SHUTTLEMIX(10, 2, 0, 1),
		PROTOSSEXCEPTION_READYTOZEALOT(6, 1, 0, 1),
		PROTOSSEXCEPTION_ZEALOTPUSH(6, 1, 0, 1),
		PROTOSSEXCEPTION_READYTODRAGOON(1, 3, 0, 2),
		PROTOSSEXCEPTION_DRAGOONPUSH(1, 4, 0, 2),
		PROTOSSEXCEPTION_PHOTONRUSH(0, 0, 0, 0),
		PROTOSSEXCEPTION_DOUBLENEXUS(0, 0, 0, 0),
		PROTOSSEXCEPTION_ARBITER(0, 0, 0, 0),
//		TERRANEXCEPTION_CHEESERUSH(0, 0, 0, 0),
//		TERRANEXCEPTION_NUCLEAR(0, 0, 0, 0),
		TERRANEXCEPTION_WRAITHCLOAK(0, 0, 0, 0),
		INIT(0, 0, 0, 0);

		public int vultureRatio;
		public int tankRatio;
		public int goliathRatio;
		public int weight;

		private EnemyStrategyException(int vultureRatio, int tankRatio, int goliathRatio, int weight) {
			this.vultureRatio = vultureRatio;
			this.tankRatio = tankRatio;
			this.goliathRatio = goliathRatio;
			this.weight = weight;
		}
	}
	

	
	// 저그전 기본전략
	// 8배럭 -> 2scv정찰 -> 1팩 -> 벌처 -> 애드온 -> 마인업 -> 2팩 -> 멀티 -> 아머리 -> 골리앗

	// 토스전 기본전략
	// 입구막기 -> 1팩 -> 마린6기 -> 애드온 -> 탱크 -> 마인업 -> 벌처 -> 앞마당전진(FD) -> 멀티 -> 시즈업

	// 테란전 기본전략
	// 1배럭 -> 커맨드 -> 마린3기 -> 벙커 -> 가스 -> 1팩 -> 1스타 -> 시즈업 -> 3팩

	public static enum EnemyStrategyNew {
		
		ZERG_BASIC(0, 2, 10, 1),
		ZERG_HYDRAWAVE(1, 5, 2, 2),
		ZERG_GIFTSET(0, 2, 10, 1),
		ZERG_HYDRAMUTAL(1, 5, 6, 3),
		ZERG_LINGHYDRA(1, 4, 3, 1),
		ZERG_LINGLURKER(2, 4, 4, 1),
		ZERG_LINGMUTAL(1, 2, 10, 1),
		ZERG_LINGULTRA(5, 6, 2, 1),
		ZERG_MUTAL(1, 1, 9, 3),
		ZERG_MUTALMANY(0, 1, 12, 3),
		ZERG_ULTRA(5, 6, 1, 1),
		
		PROTOSS_BASIC(5, 5, 0, 2),
		PROTOSS_CARRIER(1, 2, 8, 3),
		PROTOSS_DOUBLEPHOTO(3, 3, 0, 1),
		
		TERRAN_BASIC(0, 2, 0, 1),
		TERRAN_BIONIC(10, 5, 0, 1),
		TERRAN_MECHANIC(5, 8, 1, 2),
		TERRAN_MECHANIC_WITH_WRAITH(0, 7, 1, 3),
		TERRAN_MECHANIC_AFTER(0, 9, 1, 3),
		TERRAN_BATTLECRUISER(5, 1, 1, 2),
		
		ATTACKISLAND(0, 0, 1, 3);

		public int vultureRatio;
		public int tankRatio;
		public int goliathRatio;
		public int weight;

		private EnemyStrategyNew(int vultureRatio, int tankRatio, int goliathRatio, int weight) {
			this.vultureRatio = vultureRatio;
			this.tankRatio = tankRatio;
			this.goliathRatio = goliathRatio;
			this.weight = weight;
		}
	}
	
	//예외 전략. 예외가 아닐때는 무조건 INIT 으로
	public static enum EnemyStrategyExceptionNew {
		
		// 저그전 기본전략
		// 8배럭 -> 2scv정찰 -> 1팩 -> 벌처 -> 애드온 -> 마인업 -> 2팩 -> 멀티 -> 아머리 -> 골리앗
		
		// * 정상적인 저그 앞마당
		// 발동조건: 정상적인 2해처리 타이밍 적앞마당
		// 대응내용: 마린공격 & SCV1-정찰 & SCV2-마린 도착시간에 맞춘 벙커건설
		// 해제조건: 벙커폭파 & 마린전멸
		// 완료조건: 벌처생산
		ZERG_BUNKERING(1, 4, 1, 2),
		
		// * 원해처리 빠른 럴커
		// 발동조건1: 빠른가스+본진외해처리X (예측)
		// 발동조건2: 원해처리 빠른레어
		// 대응내용: 2팩토리 벌탱 & 마인업-시즈업 & 마인inBase
		// 해제조건: 느린가스 | 적느린레어 | 적앞마당 | 적스파이어계열
		// 완료조건: 충분한 탱크 & 터렛 & 스캔
		ZERG_FASTLURKER(1, 4, 1, 2),
		
		// * 노레어 다수 저글링
		// 발동조건: 노레어 | 다수저글링
		// 대응내용: 2팩토리 벌처 & 스피드업
		// 해제조건: 적레어 & 적히드라계열
		// 완료조건: 스피드업 & 충분한 벌처
		ZERG_ONLYLING(7, 3, 2, 1),
		
		// * 정상적인 히드라럴커
		// 발동조건: 레어 & 히드라계열
		// 대응조건: 2팩 전 스타포트 레이스 -> 멀티 (111)
		ZERG_PREPARELURKER(0, 0, 0, 1),
		
		// 토스전 기본전략
		// 입구막기 -> 1팩 -> 마린6기 -> 애드온 -> 탱크 -> 마인업 -> 벌처 -> 앞마당전진(FD) -> 멀티 -> 시즈업

		// * 투게이트 질럿
		// 발동조건: 투게이트웨이 빌드
		// 대응조건: 1팩 -> 벌처 -> 애드온 -> 마인업 -> 2팩 -> 멀티
		// 해제조건: 벌처 4기 이상
		PROTOSS_READYTOZEALOT(6, 1, 0, 1),
		PROTOSS_ZEALOTPUSH(6, 1, 0, 1),
		
		PROTOSS_READYTODRAGOON(1, 3, 0, 2),
		PROTOSS_DRAGOONPUSH(1, 4, 0, 2),
		
		// * 패스트다크
		// 발동조건: 빠른 아둔
		// 대응조건: 벌처수비모드 & 마인inBase & 빠른 컴셋
		// 해제조건: 다수벌처 & 컴셋 & 터렛 & 시야에 다크템플러 없음
		PROTOSS_DARK(9, 3, 0, 1),
//		PROTOSS_REAVER(8, 3, 1, 1),
//		PROTOSS_SCOUT(7, 3, 2, 1),
		
		// * 셔틀
		// 발동조건: 셔틀발견
		// 대응조건: 골리앗비율 살짝
		PROTOSS_SHUTTLE(7, 3, 2, 1),
//		PROTOSS_SHUTTLEMIX(10, 2, 0, 1),

		// * 캐논러시
		// 발동조건: 본진 또는 앞마당에 파일런 또는 포톤캐논
		// 대응조건: 가까운 곳의 건물이면 건물당 SCV 3기를 붙인다. 미네랄 일꾼 최소 3기는 남겨둔다. 완성된 캐논이 체력 30이상의 완성된 캐논이 1개 이상이면 회군한다.
		// 해제조건: 본진 또는 앞마당에 건물이 없다.
		PROTOSS_PHOTONRUSH(0, 0, 0, 0),
		
		// * 포톤 더블넥
		// 발동조건: 포톤캐논을 박으면서 더블넥시도
		// 대응조건: 1팩 -> 애드온 -> 시즈업 -> 탱크 -> 벌처 -> 마인업 & 공격, 빠른 아카데미
		// 해제조건: 아군 탱크 전멸
		PROTOSS_PHOTONDOUBLENEXUS(0, 0, 0, 0),
		
		// * 아비터
		// 발동조건: 아비터계열 발견
		// 대응조건: 골리앗비율 살짝
		PROTOSS_ARBITER(0, 0, 0, 0),
//		TERRANE_CHEESERUSH(0, 0, 0, 0),
//		TERRAN_NUCLEAR(0, 0, 0, 0),
		TERRAN_WRAITHCLOAK(0, 0, 0, 0),
		
		INIT(0, 0, 0, 0);

		public int vultureRatio;
		public int tankRatio;
		public int goliathRatio;
		public int weight;

		private EnemyStrategyExceptionNew(int vultureRatio, int tankRatio, int goliathRatio, int weight) {
			this.vultureRatio = vultureRatio;
			this.tankRatio = tankRatio;
			this.goliathRatio = goliathRatio;
			this.weight = weight;
		}
	}

	public static enum EnemyBuild {
		UNKNOWN,
		FAIL_TO_RECOGNIZE,
		ZERG_PHASE1_4OR5DRONE,
		ZERG_PHASE1_9DRONE_GAS,
		ZERG_PHASE1_9DRONE_NO_GAS,
		ZERG_PHASE1_OVER_POOL_GAS,
		ZERG_PHASE1_OVER_POOL_NO_GAS,
		ZERG_PHASE1_12DRONE_MULTI_GAS,
		ZERG_PHASE1_12DRONE_MULTI_NO_GAS,

		PROTOSS_PHASE1_ONEGATE_GAS,
		PROTOSS_PHASE1_TWOGATE,
		PROTOSS_PHASE1_DOUBLE_NEXSUS,

		PROTOSS_PHASE2_ADD_GATE,
		PROTOSS_PHASE2_ROBOTICS,
		PROTOSS_PHASE2_TEMPLAR,
		PROTOSS_PHASE2_STARGATE,
		PROTOSS_PHASE2_NO_MULTI,
	}
}
