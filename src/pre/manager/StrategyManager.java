package pre.manager;
import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import pre.AnalyzeStrategy;
import pre.BuildOrderItem;
import pre.BuildOrderQueue;
import pre.Config;
import pre.ConstructionPlaceFinder;
import pre.InitialBuild;
import pre.MapGrid;
import pre.MetaType;
import pre.WorkerData;
import pre.main.MyBotModule;
import pre.manager.CombatManager.CombatStrategy;
import pre.manager.StrategyManager.Strategys;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {
	//농봉용 변수
	public int NongBong_Chk = 0;
	public int NongBong_Cnt = 0;
	public Unit NongBong_FirstMineral = null;
	public Unit NongBong_LastMineral = null;
	public Unit NongBong_Gas = null;
	public Unit NongBong_Attack = null;
	public Unit NongBong_InfoWorker = null;
	public Unit depot = null;
	public List<Unit> mineralPatches = null;

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();
	
	private boolean isInitialBuildOrderFinished;
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가를 위한 변수 및 메소드 선언
		/// 한 게임에 대한 기록을 저장하는 자료구조
		private class GameRecord {
			String mapName;
			String enemyName;
			String enemyRace;
			String enemyRealRace;
			String myName;
			String myRace;
			int gameFrameCount = 0;
			int myWinCount = 0;
			int myLoseCount = 0;
		}
		/// 과거 전체 게임들의 기록을 저장하는 자료구조
		ArrayList<GameRecord> gameRecordList = new ArrayList<GameRecord>();
	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
		
	private boolean previousStrategyStatus;
	//private String CurrentStrategyBasic;
	//private String CurrentStrategyException;
	//private boolean CreateSCVOn;
	//private boolean AttackUnitCreate;
	
	public int vultureratio = 0;
	public int tankratio = 0;
	public int goliathratio = 0;
	public int wgt = 1;
	private int InitFaccnt = 0;
	
	public enum Strategys { 
		zergBasic
		,zergBasic_GiftSet
		,zergBasic_HighTech
		,zergBasic_HydraMutal
		,zergBasic_LingHydra
		,zergBasic_LingLurker
		,zergBasic_LingMutal
		,zergBasic_LingUltra
		,zergBasic_Mutal
		,zergBasic_MutalMany
		,zergBasic_Ultra
		,protossBasic
		,protossBasic_Carrier
		,protossBasic_Templer
		,terranBasic
		,terranBasic_AirUnit
		,terranBasic_AirUnitPlus
		,terranBasic_BattleCruiser
		,terranBasic_DropShip
		,terranBasic_NoSearch
		,terranBasic_ReverseRush
		//,terranBasicBio
		} //기본 전략 나열
	public enum StrategysException {
		zergException_FastLurker
		,zergException_Guardian
		,zergException_HydraWave
		,zergException_NongBong
		,zergException_OnLyLing
		,zergException_PrepareLurker
		,zergException_ReverseRush
		,protossException_CarrierMany
		,protossException_Dark
		,protossException_Reaver
		,protossException_Scout
		,protossException_Shuttle
		,protossException_ShuttleMix
		,terranException_CheeseRush
		,terranException_NuClear
		,terranException_Wraith
		,Init} //예외 전략 나열, 예외가 아닐때는 무조건 Init 으로 

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	private Strategys CurrentStrategyBasic = null;
	private StrategysException CurrentStrategyException = null;
	
	
	public StrategyManager() {
		
		isInitialBuildOrderFinished = false;
		CurrentStrategyBasic = Strategys.zergBasic;
		CurrentStrategyException = StrategysException.Init;
	}
	
	public void setCurrentStrategyBasic(Strategys strategy) {
		CurrentStrategyBasic = strategy;
		setCombatUnitRatio();
//		System.out.println("==setting ratio==");
//		System.out.println("vultureratio" + vultureratio);
//		System.out.println("tankratio" + tankratio);
//		System.out.println("goliathratio" + goliathratio);
//		System.out.println("wgt" + wgt);
//		
//		int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
//		int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
//		int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
//		
//		System.out.println("tot_vulture" + tot_vulture);
//		System.out.println("tot_tank" + tot_tank);
//		System.out.println("tot_goliath" + tot_goliath);
//		System.out.println("wgt" + wgt);
//		UnitType selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
//		System.out.println("chose: " + selected);
	}
	public void setCurrentStrategyException(StrategysException strategy) {
		CurrentStrategyException = strategy;
		setCombatUnitRatio();
	}
	public Strategys getCurrentStrategyBasic() {
		return CurrentStrategyBasic;
	}

	public StrategysException getCurrentStrategyException() {
		return CurrentStrategyException;
	}
	

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		// 과거 게임 기록을 로딩합니다
		//loadGameRecordList();
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
				
		//setCombatUnitRatio();
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		InitialBuild.Instance().setInitialBuildOrder();	
		InitFaccnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory);
		
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		//saveGameRecordList(isWinner);
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////		
	}
	public int getMineralPatchesNearDepotOnlySelf(Unit depot)
	{
	    // if there are minerals near the depot, add them to the set
	    int radius = 320;
	    int mineralsNearDepot =0;
	    for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < radius)
			{
	            mineralsNearDepot++;
			}
		}
	    return mineralsNearDepot;
	}
//	public static int gcd(int a, int b)
//	{ //삼항 연산자 축약형 
//	    return (a % b == 0 ? b : gcd(b,a%b));
//	}

//	public static int lcm(int a,int b){
//	    return a*b/gcd(a,b);
//	}
	
	public static int least(double a, double b, double c, int checker){
		
		
		int ret=0;
		if(a>b){
			if(b>c){
				ret = 3;
			}else{
				ret = 2;
			}
		}else{
			if(a>c){
				ret = 3;
			}else{
				ret = 1;
			}
		}
//		System.out.println("ret: " + ret);
		
		if(ret==1){
			if(a==b&&checker!=3){
				ret = checker;
			}else if(a==c&&checker!=2){
				ret = checker;
			}
		}else if(ret==2){
			if(b==a&&checker!=3){
				ret = checker;
			}else if(b==c&&checker!=1){
				ret = checker;
			}
		}else if(ret==3){
			if(c==a&&checker!=2){
				ret = checker;
			}else if(c==b&&checker!=1){
				ret = checker;
			}
		}
//		System.out.println("res: " + ret);
		return ret;
	}

	public static UnitType chooseunit(int ratea, int rateb, int ratec, int wgt, int tota, int totb, int totc){
		
		if( wgt < 1 || wgt > 3){
			wgt = 1;
		}
		
		double tempa = 0;
		double tempb = 0;
		double tempc = 0;
				
		//res = lcm(lcm(ratea,rateb),ratec);
		if(ratea == 0){
			tempa = 1.0/0.000001*tota;	
		}else{
			tempa = 1.0/ratea*tota;
		}
		if(rateb == 0){
			tempb = 1.0/0.000001*totb;	
		}else{
			tempb = 1.0/rateb*totb;
		}
		if(ratec == 0){
			tempc = 1.0/0.000001*totc;	
		}else{
			tempc = 1.0/ratec*totc;
		}
		
//		System.out.println("tempa " + tempa);
//		System.out.println("tempb " + tempb);
//		System.out.println("tempc " + tempc);
		
		int num = least(tempa,tempb,tempc,wgt);
		
		if(num == 3){//1:벌쳐, 2:시즈, 3:골리앗
			return UnitType.Terran_Goliath;
		}else if(num == 2){
			return UnitType.Terran_Siege_Tank_Tank_Mode;
		}else{
			return UnitType.Terran_Vulture;
		}
	}
	
	private int GetCurrentTot(UnitType checkunit) {
		int cnt;
		cnt = BuildManager.Instance().buildQueue.getItemCount(checkunit) + 
				 MyBotModule.Broodwar.self().allUnitCount(checkunit);
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Factory) {
				if (unit.isTraining()) {
					cnt += unit.getTrainingQueue().size();
				}
			}
		}
		return cnt;
	}
	
	private int GetCurrentTotBlocked(UnitType checkunit) {
		int cnt;
		cnt =  MyBotModule.Broodwar.self().allUnitCount(checkunit);
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Factory) {
				if (unit.isTraining()) {
					cnt += unit.getTrainingQueue().size();
				}
			}
		}
		return cnt;
	}

	
	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		
		//@@@@@@ 전략은 자주 확인할 필요 없다, 1초에 한번 하지만@@!@!@ 초반에는 자주 확인해야된다 아래
//		if ((MyBotModule.Broodwar.getFrameCount() < 5000 && MyBotModule.Broodwar.getFrameCount() % 4 == 0)
//				||(MyBotModule.Broodwar.getFrameCount() > 5000 && MyBotModule.Broodwar.getFrameCount() % 24 == 0)) {
//			AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
//		}
////
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			if(isInitialBuildOrderFinished == false){
				MyBotModule.Broodwar.printf("Initial Build Finished");
			}
			isInitialBuildOrderFinished = true;
			
		}
////		
//		// 1초에 한번만 실행
//		if (MyBotModule.Broodwar.getFrameCount() % 24 == 0) {
//			executeSupplyManagement();
//		}
		if (MyBotModule.Broodwar.getFrameCount() % 24 == 0) {
			executeExpansion();
		}
		if(MyBotModule.Broodwar.getFrameCount() % 239 == 0) {
			executeAddRefinery();
		}
//		if(isInitialBuildOrderFinished){
//			if(MyBotModule.Broodwar.getFrameCount() % 53 == 0) {
//				executeUpgrade();
//				executeResearch();
//			}
//		}
//		if (isInitialBuildOrderFinished == true) {
//			if (MyBotModule.Broodwar.getFrameCount() % 120 == 0){// info 의 멀티 체크가 120 에 돈다 
//				executeCombat();
//			}
//		}
//		
//		if (isInitialBuildOrderFinished == false) {
//			if (MyBotModule.Broodwar.getFrameCount() % 24 == 0){
//				executeAddFactoryInit();
//			}
//		}else if (MyBotModule.Broodwar.getFrameCount() % 120 == 0 && isInitialBuildOrderFinished == true) { //5초에 한번 팩토리 추가 여부 결정
//			executeAddFactory();
//		}
//		
//		if (MyBotModule.Broodwar.getFrameCount() % 6 == 0){
//			executeWorkerTraining();
//			executeCombatUnitTrainingBlocked();
//
//			if (isInitialBuildOrderFinished == true) {
//				executeCombatUnitTraining();
//			}
//			//ResondToStrategy.instance().update();//다른 유닛 생성에 비해 제일 마지막에 돌아야 한다. highqueue 이용하면 제일 앞에 있을 것이므로
//		}
	}

	
	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
//		if (isInitialBuildOrderFinished == false) {
//			return;
//		}
		
		int tot_mineral_self = 0 ;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType().isResourceDepot()){
				int minerals = getMineralPatchesNearDepotOnlySelf(unit);
				if(minerals > 0){
					if(unit.isCompleted() == false){
						minerals= minerals * unit.getHitPoints()/1500;
					}
					tot_mineral_self += minerals;
				}
			}
			
 		}

		//@@@@@@ mineral 이 50 이하이고 scv 도 0 이면 다른고 취소하고 해야되는 로직 입력 필요. 만약 여기 안 넣을꺼면 미네랄 50을 걸어도 되고.. 아니면.... 흠....
		//@@@@@@ 현재 mineral 이 50이하인데 scv 가 0 일때 현재 빌드중인거 취소해야하는 만약 취소할것이 없다면!!! 총공격 모드!! 삽입해야되지 않을까
//		if (MyBotModule.Broodwar.getFrameCount() < 6000 && MyBotModule.Broodwar.self().minerals() < 50 ) {//어차피 저거 이후로는 총 공격이다.
//				
//		}

		//@@@@@@ 이거 만약 highest 가 blocking 안하는 애 인데 두번째 애가 블로킹일때는.. scv가 넘겨서 못 만들거 같은데............................. 오히려 안 바꾸는게 초반에 큰 문제를 안 일으킬수도 있고....
		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			
			int maxworkerCount = tot_mineral_self * 2 + 8 * MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType()); // workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}
			
			if (workerCount < 60 && workerCount < maxworkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isCompleted() && unit.isTraining() == false) {
							
							BuildOrderItem currentItem = null;
							if(BuildManager.Instance().buildQueue.isEmpty() == false){
								currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
							}
							if (currentItem == null){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
							}else if(currentItem.metaType.getUnitType() != UnitType.Terran_SCV){
								if(workerCount < 4){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
								}else{
									if(currentItem.blocking == true && MyBotModule.Broodwar.self().minerals() > currentItem.metaType.getUnitType().mineralPrice()+50 ){
										BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
									}else{
										// 빌드큐에 일꾼 생산이 1개는 있도록 한다
										if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
											BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
	// SupplyProvider를 추가 건설/생산한다
	public void executeSupplyManagement() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
//		if (isInitialBuildOrderFinished == false) {
//			return;
//		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 4;
			int fac_cnt = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory);
			boolean barrackflag = false;
			boolean factoryflag = false;
			

			if(factoryflag==false){
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Terran_Factory  && unit.isCompleted()) {
						factoryflag = true;
					}
					if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
						barrackflag = true;
					}
				}
			}
			
			int Faccnt=0;
			int CCcnt=0;
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit == null) continue;
				if (unit.getType() == UnitType.Terran_Factory){
					Faccnt ++;
				}
				if (unit.getType().isResourceDepot() && unit.isCompleted()){
					CCcnt++;
				}
			}
						
			if(MyBotModule.Broodwar.getFrameCount()<14000 || (Faccnt <= 3 && CCcnt == 1)){//TODO 이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨
				if(barrackflag==true && factoryflag==false){
					supplyMargin = 6;
				}else if(factoryflag==true){
					supplyMargin = 6+4*fac_cnt;
				}
			}else if((MyBotModule.Broodwar.getFrameCount()>14000 && MyBotModule.Broodwar.getFrameCount()<28000) || (Faccnt > 3 && CCcnt == 2)){
				supplyMargin = 8+6*fac_cnt;
			}else{
				supplyMargin = 12+7*fac_cnt;
			}
			
			
			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin + 1 - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				
				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;

				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
				onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
						InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
						* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();

				//System.out.println("currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {
					
					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						System.out.println("enqueue supply provider " + InformationManager.Instance().getBasicSupplyProviderUnitType());
						BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}
	}

	public void executeAddFactoryInit() {
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) 
				+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) < InitFaccnt) {
			System.out.println("addfac in Init, InitFaccnt :" + InitFaccnt + ", tot: "
		+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) 
		+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory)
		+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null));
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
		}
	}
	
	public void executeAddFactory() {
		
		int CCcnt = 0;
		int maxFaccnt = 0;
		int Faccnt = 0;
		int MachineShopcnt = 0;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType().isResourceDepot() && unit.isCompleted()){
				CCcnt++;
			}
			if (unit.getType() == UnitType.Terran_Factory){
				Faccnt ++;
			}
			if (unit.getType() == UnitType.Terran_Machine_Shop){
				MachineShopcnt ++;
			}
			
 		}
		Faccnt = Faccnt + ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);
		
		if(CCcnt <= 1){
			//@@@@@@ 헌터이면 팩이 4? 3?
			maxFaccnt = 3;		
		}else if(CCcnt == 2){
			//@@@@@@ 헌터이면 팩이 7? 6?
			maxFaccnt = 6;
		}else if(CCcnt >= 3){
			maxFaccnt = 9;
		}//@@@@@@ fac 7 8 9 는 다른 자리에?
		
		//@@@@@@ fac 만들고 있을때 빌드큐에서 빠지고 construrction 큐에 들어가는데.. 같이 볼필요 없을까?
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) == 0) {
			if(Faccnt == 0){
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			}else if(Faccnt <= maxFaccnt){
				if(MyBotModule.Broodwar.self().minerals() > 200 + Faccnt*80 && MyBotModule.Broodwar.self().gas() > 100 + Faccnt * 50){
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					if(Config.BuildQueueDebugYN){
						System.out.print(" Adding Fac ");
					}
				}
			}
		}
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null) == 0) {
			if(MachineShopcnt + 1 < Faccnt ){
				if(MyBotModule.Broodwar.self().minerals() > 50 && MyBotModule.Broodwar.self().gas() > 50){
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					if(Config.BuildQueueDebugYN){
						System.out.print(" Adding MS ");
					}
				}
			}
		}
	}
	
	public void setCombatUnitRatio(){
		
		vultureratio = 0;
		tankratio = 0;
		goliathratio = 0;
		wgt = 1;
		
		//config setting 가지고 오기
		if(CurrentStrategyException.toString() == "Init"){
			vultureratio = Config.vultureratio[CurrentStrategyBasic.ordinal()];
			tankratio = Config.tankratio[CurrentStrategyBasic.ordinal()];
			goliathratio = Config.goliathratio[CurrentStrategyBasic.ordinal()];
			wgt = Config.wgt[CurrentStrategyBasic.ordinal()];
		}else{
			vultureratio = Config.vultureratioexception[CurrentStrategyException.ordinal()];
			tankratio = Config.tankratioexception[CurrentStrategyException.ordinal()];
			goliathratio = Config.goliathratioexception[CurrentStrategyException.ordinal()];
			wgt = Config.wgtexception[CurrentStrategyException.ordinal()];
		}
	}
	
	public boolean isFacUnit(BuildOrderItem currentItem){
		
		if(currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
			return true;
		}
		if(currentItem.metaType.getUnitType() == UnitType.Terran_Vulture){
			return true;
		}
		if(currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
			return true;
		}
		return false;
	}
	
	public int getFacUnits(){
		
		int tot=0;
		
		tot = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture)
				+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
				+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode)
				+MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Goliath);
		
		return tot*4; //인구수 기준이므로
	}

	public void executeCombatUnitTrainingBlocked() {
		
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		
		BuildOrderItem currentItem = null; 
		if(Config.BuildQueueDebugYN){
			System.out.print("@@@@@@@@Frame : " + MyBotModule.Broodwar.getFrameCount() + ", ");
		}
		
		if(MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed() < 4){
			return;
		}
		
		if (!tempbuildQueue.isEmpty()) {
			currentItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				if(Config.BuildQueueDebugYN){
					System.out.println("picked: "+ currentItem.metaType.getName());
				}
				if(currentItem.metaType.getUnitType() == UnitType.Terran_Vulture){
					return;
				}
				if(currentItem.blocking == true){
					break;
				}
				
				if(tempbuildQueue.canSkipCurrentItem() == true){
					tempbuildQueue.skipCurrentItem();
				}else{
					return;
				}
				currentItem = tempbuildQueue.getNextItem();
			}
		}else{
			return;
		}
		
		boolean isarmoryexists = false;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Armory && unit.isCompleted()){
				isarmoryexists = true;
			}
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() && unit.isTraining() == false){
				
				if(Config.BuildQueueDebugYN){
					System.out.println("unitID: " + unit.getID());
				}
				if(unit.isConstructing() == true){
					if(Config.BuildQueueDebugYN){
						System.out.println("factory is making machin shop!!");
					}
					continue;
				}
				
//				if(unit.getAddon() != null){
//					System.out.println("unit addon completed: " + unit.getAddon().isCompleted());
//					if(unit.getAddon().isCompleted() == false){
//						System.out.println("should be here");
//						continue;
//					}
//				}
				//@@@@@@ else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
				if(currentItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop && unit.getAddon() == null ){
					continue;
				}
				//필요하려나?
				if(currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					if(unit.getAddon() != null && unit.getAddon().isCompleted() == true){
						continue;
					}
				}
				if(currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					if(isarmoryexists){
						continue;
					}
				}
				
				
				boolean eventually_vulture = true;
				
				int tot_vulture = GetCurrentTotBlocked(UnitType.Terran_Vulture);
				int tot_tank = GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTotBlocked(UnitType.Terran_Siege_Tank_Siege_Mode);
				int tot_goliath = GetCurrentTotBlocked(UnitType.Terran_Goliath);
				
				UnitType selected = null; 
				
				if(Config.BuildQueueDebugYN){
					System.out.print("currentItem : " + currentItem.metaType.getUnitType());
				}
				
				selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
				if(Config.BuildQueueDebugYN){
					System.out.print(", selected:" + selected);
				}
				
				if(currentItem.metaType.getUnitType()==selected){
					if(Config.BuildQueueDebugYN){
						System.out.println("pick and select same skipping");
					}
				}else if(selected == UnitType.Terran_Siege_Tank_Tank_Mode){
					if(unit.getAddon() != null && unit.getAddon().isCompleted() == true){
						if(currentItem.metaType.mineralPrice()+selected.mineralPrice() < MyBotModule.Broodwar.self().minerals() &&
								currentItem.metaType.gasPrice()+selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							if(Config.BuildQueueDebugYN){
								System.out.println("queueIn : Tank");
							}
							eventually_vulture = false;
						}
					}
				}else if(selected == UnitType.Terran_Goliath){
					if(Config.BuildQueueDebugYN){
						System.out.print(", isarmoryexists:" + isarmoryexists);
					}
					if(isarmoryexists){
						if(currentItem.metaType.mineralPrice()+selected.mineralPrice() < MyBotModule.Broodwar.self().minerals() &&
								currentItem.metaType.gasPrice()+selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							if(Config.BuildQueueDebugYN){
								System.out.println("queueIn : Goliath");
							}
							eventually_vulture = false;
						}
					}
				}
				if(Config.BuildQueueDebugYN){
					System.out.println("eventually_vulture:" + eventually_vulture);
				}
				
				if(eventually_vulture){
					if(currentItem.metaType.mineralPrice()+75 < MyBotModule.Broodwar.self().minerals() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
						if(Config.BuildQueueDebugYN){
							System.out.println("unitgetaddon: "+ unit.getAddon() );
						
							if(unit.getAddon() != null ){
								System.out.println("unitgetaddoncompl: "+ unit.getAddon().isCompleted() );
							}
						}
						if(Config.BuildQueueDebugYN){
							System.out.println("getconstruction queue: "+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) );
						}
						if((unit.isConstructing() == true) || ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) != 0){
							continue;
						}
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Vulture,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						if(Config.BuildQueueDebugYN){
							System.out.println("queueIn : Vulture");
						}
					}
				}
			}
 		}
	}
	
	public void executeCombatUnitTraining() {

		int Faccnt = 0;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Factory){
				Faccnt ++;
			}
 		}
		
		
		int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
		int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
		int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
		
		UnitType selected = null; 
		int currentinbuildqueuecnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null) +
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode, null) +
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath, null);

		//TODO 들어가는 선 조건이 있어야 하지 않을지.... 매번 팩토리 다 볼수는 없지 않나????????? 겉에 루프가 없어도 될거 같은데 나중에 실험해 보자.. 한 프레임에 한번만 들어와서 팩토리 하나만 하고 break 하고 다음에 빈거 또 하겠지?
		for(int i=0; i< Faccnt - currentinbuildqueuecnt; i++){
			selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
			if(Config.BuildQueueDebugYN){
				System.out.println("selected:" + selected);
			}
			
			if(selected.mineralPrice() < MyBotModule.Broodwar.self().minerals() &&	selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
				BuildManager.Instance().buildQueue.queueAsLowestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				if(selected == UnitType.Terran_Siege_Tank_Tank_Mode){
					tot_tank++;
				}else if(selected == UnitType.Terran_Goliath){
					tot_goliath++;
				}else if(selected == UnitType.Terran_Vulture){
					tot_vulture++;
				}				
			}
		}
	}
	
	public int getTotDeadCombatUnits(){
		
		int res =0;
		int tot =0;
		int totscv = 0;
		int totmarine = 0;
		
		tot = MyBotModule.Broodwar.self().deadUnitCount();
		totmarine = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_Marine);
		totscv = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_SCV);
		
		res = tot - totmarine - totscv;
		res = res*2 + totmarine;
		
		return res*2;//스타에서는 두배
	}
	
	public int getTotKilledCombatUnits(){
		
		int res = 0;
		int tot = 0;
		
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			int totworker = 0;
			int totbio = 0;
			
			tot = MyBotModule.Broodwar.self().killedUnitCount();
			totbio = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Marine)
					+ MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Firebat)
					+ MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_Medic);
			totworker = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Terran_SCV);

			res = tot - totbio - totworker;
			res = res*2 + totbio;
			
			return res*2;
			
		}else if (InformationManager.Instance().enemyRace == Race.Zerg) {
			
			int totworker = 0;
			int totzerling = 0;
			int tothydra = 0;
			int totmutal = 0;
			int totoverload = 0;
			
			//tot = MyBotModule.Broodwar.self().killedUnitCount(UnitType.AllUnits);
			totzerling = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Zergling);
			tothydra = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Hydralisk);
			totmutal = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Mutalisk);
			totoverload = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Overlord);
			//totworker = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Zerg_Drone);

			res = totzerling + tothydra*2 + totmutal*4 + totoverload*3;
			return res; //저그는 저글링 때문에 이미 2배 함
			
		}else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			int totworker = 0;
			
			tot = MyBotModule.Broodwar.self().killedUnitCount();
			totworker = MyBotModule.Broodwar.self().killedUnitCount(UnitType.Protoss_Probe);

			res = tot - totworker;
			res = res*2;
			
			return res*2;
		}else{
			return 0;
		}
		
	}

	

	public boolean enemymulting(){
		int cnt = MyBotModule.Broodwar.enemy().incompleteUnitCount(InformationManager.Instance().getBasicResourceDepotBuildingType());

		if (cnt > 0){
			return true;
		}
		return false;
	}
	
	
	//@@@@@@ 상대방 신규 멀티를 찾았을때 공격 여부 한번더 돌려야함(상대 멀티 진행 여부 판단해야되므로
	public void executeCombat() {
		
		int point = 0;
		int selfbasecnt = 0;
		int enemeybasecnt = 0;
		boolean allaware = InformationManager.Instance().isReceivingEveryMultiInfo();
		
		//@@@@@@ 아군은 돌아가기 시작하고 scv 특정 수 이상 붙은 컴맨드만 쳐야 하지 않나?
		selfbasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().selfPlayer);
		enemeybasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().enemyPlayer);
		//CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_INSIDE);
		
		//공통 예외 상황
		if(getFacUnits() > 280 || MyBotModule.Broodwar.self().supplyUsed() > 392){//팩토리 유닛  130 이상 또는 서플 196 이상 사용시(스타 내부에서는 2배)
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
		}
		if(MyBotModule.Broodwar.getFrameCount() > 8000 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV) < 7){//@@@@@@5드론에서 피해를 봣을때의 기준에 따라서 수치 조정 필요.. 감이 없음
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY); //@@@@@@ 올인에 대한거 하나 만들어야 겠다... 올인이면 공격 취소도 하면 안되므로..
		}
		
		//종족별 예외 상황
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			
		}
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			//앞마당 photo
			//캐리어
			//본진 photo
			//다크템, 시타델)
		}
		if (InformationManager.Instance().enemyRace == Race.Zerg) {
			//triple hatchery
		}
		
		//공통 기본 로직, 팩토리 유닛 50마리 기준 유닛 Kill Death 상황에 따라 변경.
		
		int deadcombatunit = getTotDeadCombatUnits();
		int killedcombatunit = getTotKilledCombatUnits();
		int totworkerdead = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_SCV);
		int totworkerkilled = MyBotModule.Broodwar.self().killedUnitCount(InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace));
		int totaldeadunit = MyBotModule.Broodwar.self().deadUnitCount();
		int totalkilledunit = MyBotModule.Broodwar.self().deadUnitCount();
		
		int myunit = getFacUnits();
		
		if(MyBotModule.Broodwar.getFrameCount() < 20000){ //약 시작 ~ 15분까지
			point += (totworkerkilled - totworkerdead) *2* (-MyBotModule.Broodwar.getFrameCount()/40000.0*3.0 + 3.0);
			point += (killedcombatunit - deadcombatunit);
			
		}else if(MyBotModule.Broodwar.getFrameCount() < 40000){ //약 15분 ~ 28분까지  // 여기서부턴 시간보다.... 현재 전체 규모수가 중요할듯?
			point += (totworkerkilled - totworkerdead)*2* (-MyBotModule.Broodwar.getFrameCount()/40000.0*3.0 + 3.0);
			point += (killedcombatunit - deadcombatunit)* (-MyBotModule.Broodwar.getFrameCount()/20000.0 + 2.0);
			
			if(selfbasecnt > 1 && enemeybasecnt > 1){
				if(allaware ==true){
					if (InformationManager.Instance().enemyRace == Race.Terran) {			
						double temp = (selfbasecnt - enemeybasecnt)/1000.0-20; //멀티 하나당 frame 에 따라서 최고 20점
						if(temp < -20){ //최소 점수 존재
							temp = -20;
						}
						point += temp;
					}else{
						double temp = (selfbasecnt+1 - enemeybasecnt)/1000.0-20; //저그 프로는 하나 정도 내주고 계산
						if(temp < -20){ //최소 점수 존재
							temp = -20;
						}
						point += temp;
					}
				}
			}else if(selfbasecnt > 1 && enemeybasecnt == 1){
				point += 20;
			}
		
		}else{ //28분 ~ 종료시까지
			//point += (killedcombatunit - deadcombatunit);
			if(selfbasecnt > 1 && enemeybasecnt > 1){
				if(allaware ==true){
					if (InformationManager.Instance().enemyRace == Race.Terran) {			
						double temp = (selfbasecnt - enemeybasecnt) * 20; //멀티 하나당 20점
						if(temp < -20){ //최소 점수 존재
							temp = -20;
						}
						point += temp;
					}else{
						double temp = (selfbasecnt+1 - enemeybasecnt) * 20; //멀티 하나당 20점
						if(temp < -20){ //최소 점수 존재
							temp = -20;
						}
						point += temp;
					}
				}
			}else if(selfbasecnt > 1 && enemeybasecnt == 1){
				point += 20;
			}
		}
		
		//내 팩토리 유닛 인구수 만큼 추가
		point += myunit;
		
		//상대가 멀티를 짓는중이라면?
		if(enemymulting() ==true){
			point += 10;
		}
		
		//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if( myunit > 80 && killedcombatunit-deadcombatunit > myunit/4 ){
				point += 20;
			}
		}else{
			if( myunit > 80 && killedcombatunit-deadcombatunit > myunit/3 ){
				point += 20;
			}
		}
	
		if (MyBotModule.Broodwar.getFrameCount() % 240 == 0){
			System.out.println("Current Attack Point: " + point);
		}
		//@@@@@@ 일단 공격 중이라면......
		if(point > 120){// 팩토리 유닛이 30마리(즉 스타 인구수 200 일때)
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
		}else{
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY){
				if(point < 40){//@@@@@@ 후반부터는 상대 죽인수가 의미가 없어지기 때문에.... defence 기준을 다르게 잡아야한다.
					CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
				}
			}else{
				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
			}
		}
	}
	
	public void executeUpgrade() {

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			
			if (unit.getType() == UnitType.Terran_Armory && unit.isCompleted() && unit.canUpgrade()){
				//Fac Unit 18 마리 이상 되면 1단계 업그레이드 시도
				if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) ==0 && getFacUnits() > 72 && unit.canUpgrade(UpgradeType.Terran_Vehicle_Weapons) 
						&& MyBotModule.Broodwar.self().minerals()> 100 && MyBotModule.Broodwar.self().gas()> 100){
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
					}
				}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) ==0 && getFacUnits() > 72 && unit.canUpgrade(UpgradeType.Terran_Vehicle_Plating)
						&& MyBotModule.Broodwar.self().minerals()> 100 && MyBotModule.Broodwar.self().gas()> 100){
					if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
					}
				}
				//Fac Unit 30 마리 이상, 일정 이상의 자원 2단계
				else if(getFacUnits() > 120 && MyBotModule.Broodwar.self().minerals()> 500 && MyBotModule.Broodwar.self().gas()> 300 ){
					
					if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 1 && unit.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)){
						if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
						}
					}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 1 && unit.canUpgrade(UpgradeType.Terran_Vehicle_Plating)){
						if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
						}
					}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 2 && unit.canUpgrade(UpgradeType.Terran_Vehicle_Weapons)){//3단계
						if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
						}
					}else if(MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 2 && unit.canUpgrade(UpgradeType.Terran_Vehicle_Plating)){
						if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
						}
					}else{
						boolean Science_Facility = false;
						boolean Starport = false;
						boolean Starport_complete = false;
						
						for (Unit structure : MyBotModule.Broodwar.self().getUnits()){
							if (structure.getType() == UnitType.Terran_Science_Facility){
								Science_Facility = true;
							}
							if (structure.getType() == UnitType.Terran_Starport){
								Starport = true;
								if(structure.isCompleted()){
									Starport_complete = true;
								}
							}
						}
					
						if(Starport == false){//Fac 은 무조건 있다고 본다. 
							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0) {
								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							}
						}else if(Science_Facility== false && Starport_complete){
							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Facility) == 0) {
								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Facility, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							} 
						}
					}
				}
			}
 		}
	}
	
	public void executeResearch() {
		
		if(getFacUnits() < 32) return;
			
		boolean VS = MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1 ? true : false;;
		boolean VM = MyBotModule.Broodwar.self().hasResearched(TechType.Spider_Mines);
		boolean TS = MyBotModule.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode);
		boolean GR = MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false;
		
		if(VS&&VM&&TS&&GR) return; // 4개 모두 완료이면
		
		MetaType vultureSpeed = new MetaType(UpgradeType.Ion_Thrusters);
		MetaType vultureMine = new MetaType(TechType.Spider_Mines);
		MetaType TankSiegeMode = new MetaType(TechType.Tank_Siege_Mode);
		MetaType GoliathRange = new MetaType(UpgradeType.Charon_Boosters);
		
		MetaType vsZerg[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
		boolean vsZergbool[] = new boolean[]{VM, VS, TS, GR};
		MetaType vsZergMutal[] = new MetaType[]{vultureMine, vultureSpeed, GoliathRange, TankSiegeMode};
		boolean vsZergMutalbool[] = new boolean[]{VM, VS, GR, TS};
		MetaType vsTerran[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
		boolean vsTerranbool[] = new boolean[]{VM, VS, TS, GR};
		MetaType vsTerranBio[] = new MetaType[]{vultureSpeed, TankSiegeMode, vultureMine, GoliathRange};
		boolean vsTerranBiobool[] = new boolean[]{VS, TS, VM, GR};
		MetaType vsProtoss[] = new MetaType[]{TankSiegeMode, vultureMine, vultureSpeed, GoliathRange};
		boolean vsProtossbool[] = new boolean[]{TS, VM, VS, GR};
		
		MetaType[] Current = null;
		boolean[] Currentbool = null;
		boolean air = true;
		boolean terranBio = false;
		
		if(MyBotModule.Broodwar.enemy().getRace() == Race.Protoss){
			Current = vsProtoss;
			Currentbool = vsProtossbool;
			air = false;
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == UnitType.Protoss_Stargate
						|| unit.getType() == UnitType.Protoss_Arbiter
						|| unit.getType() == UnitType.Protoss_Carrier
						|| unit.getType() == UnitType.Protoss_Corsair
						|| unit.getType() == UnitType.Protoss_Scout
						|| unit.getType() == UnitType.Protoss_Arbiter_Tribunal
						|| unit.getType() == UnitType.Protoss_Fleet_Beacon
						|| unit.getType() == UnitType.Protoss_Shuttle
						){
					air = true;
				}
			}
		}else if(MyBotModule.Broodwar.enemy().getRace() == Race.Terran){
//			if(CurrentStrategyBasic == Strategys.terranBasicBio){
//				Current = vsTerranBio;
//				Currentbool = vsTerranBiobool;
//				terranBio = true;
//			}else{
				Current = vsTerran;
				Currentbool = vsTerranbool;
//			}
			air = false;
			for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()){
				if(unit.getType() == UnitType.Terran_Starport
						|| unit.getType() == UnitType.Terran_Science_Facility
						|| unit.getType() == UnitType.Terran_Dropship
						|| unit.getType() == UnitType.Terran_Science_Vessel
						|| unit.getType() == UnitType.Terran_Wraith
						|| unit.getType() == UnitType.Terran_Battlecruiser
						|| unit.getType() == UnitType.Terran_Physics_Lab
						){
					air = true;
				}
			}
		}else {
			if(CurrentStrategyBasic == Strategys.zergBasic_LingMutal
					|| CurrentStrategyBasic == Strategys.zergBasic_Mutal
					){
				Current = vsZergMutal;
				Currentbool = vsZergMutalbool;
			}else{
				Current = vsZerg;
				Currentbool = vsZergbool;
			}
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			
			if (unit.getType() == UnitType.Terran_Machine_Shop && unit.isCompleted() && unit.canUpgrade()){
				if(Currentbool == null) return;
				for(int i = 0; i<4; i++){
					if(Currentbool[i] == true){
						continue;
					}else{
						if(i==3 && Current[3].getUpgradeType() == UpgradeType.Charon_Boosters){
							if(!air){
								continue;
							}
						}
						if(terranBio && i==2 && Current[2].getTechType() == TechType.Spider_Mines){
							if((getFacUnits() > 48 && MyBotModule.Broodwar.self().minerals()> 300 && MyBotModule.Broodwar.self().gas()> 200) || getFacUnits() > 100 ){
								
							}else{
								continue;
							}
						}
						if(BuildManager.Instance().buildQueue.getItemCount(Current[i]) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(Current[i], true);
						}
						break;
					}
				}
			}
		}
	}
	
	public void executeAddRefinery() {
		
		//이거는 updat() 쪽에서 10초에 한번씩만 돌아도 될거 같고. // frame 239마다. 
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				
				boolean refineryAlreadyBuilt = false; 
				for (Unit Refinery : MyBotModule.Broodwar.getUnitsInRadius(unit.getPosition(), 300)){
					if (Refinery == null) continue;
					if(Refinery.getType() == UnitType.Terran_Refinery){
						refineryAlreadyBuilt = true;
						break;
					}
				}
				if(refineryAlreadyBuilt ==false){
					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
					if(findGeyser != null){
						if (findGeyser.getDistance(unit.getTilePosition())*32 > 300){
							continue;
						}
						if(WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 5)
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
									BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
							}
					}
				}
			}
 		}
	}
	
	public void executeExpansion() {
		
		if(MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center)>0){
			return;
		}
		
		int MaxCCcount = 4;
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			int tempCC =0 ;
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				if(WorkerManager.Instance().getWorkerData().getMineralsNearDepot(unit) > 5){
					tempCC++;
				}
			}
			if(tempCC >= MaxCCcount){
				return;
			}
 		}
		
		int CCcnt = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
		
		
		//앞마당 전
		if(CCcnt == 1){//TODO 이거 손봐야된다... 만약 위로 띄어서 해야한다면?? 본진에 지어진거 카운트 안되는 상황에서 앞마당에 지어버리겟네
			if (isInitialBuildOrderFinished == false) {
				return;
			}
			if( MyBotModule.Broodwar.self().minerals() > 500 && getFacUnits() > 40){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					MyBotModule.Broodwar.printf("Build First CC");
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if( MyBotModule.Broodwar.getFrameCount() > 8000 && MyBotModule.Broodwar.self().minerals() > 400){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			
		}
		
		//앞마당 이후
		if(CCcnt >= 2 && CCcnt <= MaxCCcount){
			
			// 돈이 800 넘으면 멀티하기
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
					+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
				if( MyBotModule.Broodwar.self().minerals() > 600 && getFacUnits() > 40){
					MyBotModule.Broodwar.printf("Build Next CC1");
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//공격시 돈 400 넘으면 멀티하기
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					if( MyBotModule.Broodwar.self().minerals() > 400){
						MyBotModule.Broodwar.printf("Build Next CC2");
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					}
				}
			}
			
		}
	}
	
	public void executeNongBong() {
		if (MyBotModule.Broodwar.getFrameCount() % 3 != 0) {
			//System.out.println("tot_mineral_self: "+ tot_mineral_self);
			return;
		}
		
		if(NongBong_Chk < 8){
			//미네랄 클릭 8회 미만 - 8회째는 다음 미네랄 클릭으로 간다
			System.out.println("mineral Right_Click ==>>> " + NongBong_Chk + " times !!!!!!!!!!!!!");
			int mineral_chk = 0;
			if(NongBong_Chk  == 0){
				// 농봉에 사용할 scv는 미네랄 일꾼 -2
				NongBong_Cnt = WorkerManager.Instance().getNumMineralWorkers() -2;
				//농봉 시작일때 미네랄 / 가스 / 어택 유닛에 대한 정보를 가져온다.
				for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
					NongBong_InfoWorker = worker;
				}
				depot = WorkerManager.Instance().getWorkerData().getWorkerDepot(NongBong_InfoWorker) ;
				System.out.println("depot ==>>>>>>>> " + depot);
				mineralPatches = WorkerManager.Instance().getWorkerData().getMineralPatchesNearDepot(depot);
				System.out.println(" 미네랄 개수 =====>>>>  " + mineralPatches.size());
				
				int radius = 320;
				for (Unit unit : MyBotModule.Broodwar.getAllUnits())
				{
					if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < radius){
						if(mineral_chk == 0){
							//최초 미네랄 발견
							NongBong_FirstMineral = unit;
						}
						//라스트 미네랄은 보일때마다 넣어줌.
						NongBong_LastMineral = unit;
						mineral_chk++;
					}
					if ((unit.getType() == UnitType.Resource_Vespene_Geyser) && unit.getDistance(depot) < radius)
					{
						NongBong_Gas = unit;
					}
				}
				NongBong_Attack = depot;
				
				System.out.println("농봉 변수까지 담았다.");
				//mineral_chk 변수는 여기서 일꾼 마리수 체크용으로 재활용
				
			}
			mineral_chk = 0;
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				System.out.println("getNumMineralWorkers() ==>>> " + WorkerManager.Instance().getNumMineralWorkers());
				//전체 미네랄 일꾼 -2 만큼만 농봉에 사용하자
				if(NongBong_Cnt > mineral_chk){
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Minerals && NongBong_Chk == 0){
						System.out.println("goto Minerals!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
						CommandUtil.rightClick(worker, NongBong_FirstMineral);
						WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongM, (Unit)null);
						mineral_chk ++;
					}
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongM && NongBong_Chk != 0){
						System.out.println("goto Minerals NongBongM!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
						CommandUtil.rightClick(worker, NongBong_FirstMineral);
						mineral_chk ++;
					}
				}
			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 8){
			//미네랄 한번 더 클릭
			System.out.println("NongBong_Chk == 8 &&  Last Mineral Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 미네랄로 무브했던 NongBongM 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongM){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					System.out.println("goto Mineral Last!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					CommandUtil.rightClick(worker, NongBong_LastMineral);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongG, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 9){
			//가스클릭
			System.out.println("NongBong_Chk == 9 &&  Gas Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 미네랄로 무브했던 NongBongG 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongG){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					System.out.println("goto Gas!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					CommandUtil.rightClick(worker, NongBong_Gas);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongGS, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 10){
			//가스 쉬프트 우클릭
			System.out.println("NongBong_Chk == 10 &&  Gas Shift Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 가스로 무브했던 NongBongGS 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongGS){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					//CommandUtil.rightClick(worker, NongBong_Gas);
					System.out.println("goto Gas Shift!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					worker.rightClick(NongBong_Gas, true);
					WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongAT, (Unit)null);
				}

			}
			NongBong_Chk ++;
		}else if(NongBong_Chk == 11){
			//어택 시프트 우클릭
			System.out.println("NongBong_Chk == 11 &&  Attack Shift Click !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 가스로 시프트 무브했던 NongBongAT 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongAT){
					//최초 미네랄과 반대쪽 미네랄로 간다.
					//CommandUtil.rightClick(worker, NongBong_Gas);
					System.out.println("Attack Shift!!!!!!!!!!!! ==>>>>>>> " + worker.getID());
					worker.attack(depot, true);
					//WorkerManager.Instance().getWorkerData().setWorkerJob(worker, WorkerData.WorkerJob.NongBongAT, (Unit)null);
				}

			}
			NongBong_Chk = 1000;
		}else if(NongBong_Chk == 1000){
			for (Unit worker :  WorkerManager.Instance().getWorkerData().getWorkers()){
				//이전에 가스로 시프트 무브했던 NongBongAT 상태 일꾼들 대상
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.NongBongAT){
					System.out.println("NongBongAt ======>>> " + worker.getID());
				}

			}
			NongBong_Chk = 100000;
		}
	}
	
	
	public void executeMarineIntoBunker() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			//System.out.println("tot_mineral_self: "+ tot_mineral_self);
			return;
		}
		
		int haveMarine = 0;
		int haveBunker = 0;
		
		Unit theBunker = null;
		for (Unit unit : MyBotModule.Broodwar.getAllUnits()){
			if(unit.getType() == UnitType.Terran_Bunker){
				haveBunker ++;
				theBunker = unit;
			}
			
			if(unit.getType() == UnitType.Terran_Marine){
				haveMarine ++;
				if(haveBunker > 0 && haveMarine >0){
					theBunker.load(unit);
				}
			}
			
		}
		
	}
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

	/// 과거 전체 게임 기록을 로딩합니다
	void loadGameRecordList() {
	
		// 과거의 게임에서 bwapi-data\write 폴더에 기록했던 파일은 대회 서버가 bwapi-data\read 폴더로 옮겨놓습니다
		// 따라서, 파일 로딩은 bwapi-data\read 폴더로부터 하시면 됩니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\read\\NoNameBot_GameRecord.dat";
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(gameRecordFileName));

			System.out.println("loadGameRecord from file: " + gameRecordFileName);

			String currentLine;
			StringTokenizer st;  
			GameRecord tempGameRecord;
			while ((currentLine = br.readLine()) != null) {
				
				st = new StringTokenizer(currentLine, " ");
				tempGameRecord = new GameRecord();
				if (st.hasMoreTokens()) { tempGameRecord.mapName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myWinCount = Integer.parseInt(st.nextToken());}
				if (st.hasMoreTokens()) { tempGameRecord.myLoseCount = Integer.parseInt(st.nextToken());}
				if (st.hasMoreTokens()) { tempGameRecord.enemyName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.enemyRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.enemyRealRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.gameFrameCount = Integer.parseInt(st.nextToken());}
			
				gameRecordList.add(tempGameRecord);
			}
		} catch (FileNotFoundException e) {
			System.out.println("loadGameRecord failed. Could not open file :" + gameRecordFileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
	}

	/// 과거 전체 게임 기록 + 이번 게임 기록을 저장합니다
	void saveGameRecordList(boolean isWinner) {

		// 이번 게임의 파일 저장은 bwapi-data\write 폴더에 하시면 됩니다.
		// bwapi-data\write 폴더에 저장된 파일은 대회 서버가 다음 경기 때 bwapi-data\read 폴더로 옮겨놓습니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\write\\NoNameBot_GameRecord.dat";

		System.out.println("saveGameRecord to file: " + gameRecordFileName);

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		/// 이번 게임에 대한 기록
		GameRecord thisGameRecord = new GameRecord();
		thisGameRecord.mapName = mapName;
		thisGameRecord.myName = myName;
		thisGameRecord.myRace = MyBotModule.Broodwar.self().getRace().toString();
		thisGameRecord.enemyName = enemyName;
		thisGameRecord.enemyRace = MyBotModule.Broodwar.enemy().getRace().toString();
		thisGameRecord.enemyRealRace = InformationManager.Instance().enemyRace.toString();
		thisGameRecord.gameFrameCount = MyBotModule.Broodwar.getFrameCount();
		if (isWinner) {
			thisGameRecord.myWinCount = 1;
			thisGameRecord.myLoseCount = 0;
		}
		else {
			thisGameRecord.myWinCount = 0;
			thisGameRecord.myLoseCount = 1;
		}
		// 이번 게임 기록을 전체 게임 기록에 추가
		gameRecordList.add(thisGameRecord);

		// 전체 게임 기록 write
		StringBuilder ss = new StringBuilder();
		for (GameRecord gameRecord : gameRecordList) {
			ss.append(gameRecord.mapName + " ");
			ss.append(gameRecord.myName + " ");
			ss.append(gameRecord.myRace + " ");
			ss.append(gameRecord.myWinCount + " ");
			ss.append(gameRecord.myLoseCount + " ");
			ss.append(gameRecord.enemyName + " ");
			ss.append(gameRecord.enemyRace + " ");
			ss.append(gameRecord.enemyRealRace + " ");
			ss.append(gameRecord.gameFrameCount + "\n");
		}
		
		//Common.overwriteToFile(gameRecordFileName, ss.toString());
	}

	/// 이번 게임 중간에 상시적으로 로그를 저장합니다
	void saveGameLog() {
		
		// 100 프레임 (5초) 마다 1번씩 로그를 기록합니다
		// 참가팀 당 용량 제한이 있고, 타임아웃도 있기 때문에 자주 하지 않는 것이 좋습니다
		// 로그는 봇 개발 시 디버깅 용도로 사용하시는 것이 좋습니다
		if (MyBotModule.Broodwar.getFrameCount() % 100 != 0) {
			return;
		}

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameLogFileName = "bwapi-data\\write\\NoNameBot_LastGameLog.dat";

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		StringBuilder ss = new StringBuilder();
		ss.append(mapName + " ");
		ss.append(myName + " ");
		ss.append(MyBotModule.Broodwar.self().getRace().toString() + " ");
		ss.append(enemyName + " ");
		ss.append(InformationManager.Instance().enemyRace.toString() + " ");
		ss.append(MyBotModule.Broodwar.getFrameCount() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyUsed() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyTotal() + "\n");

		//Common.appendTextToFile(gameLogFileName, ss.toString());
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
}