
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {


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
	
	public int vultureratio = 0;
	public int tankratio = 0;
	public int goliathratio = 0;
	public int wgt = 1;
	private int InitFaccnt = 0;
	
	public enum Strategys { 
		zergBasic
		,zergBasic_HydraWave
		,zergBasic_GiftSet
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
		,terranBasic_Bionic
		} //기본 전략 나열
	public enum StrategysException { zergException_FastLurker
		,zergException_Guardian
		,zergException_NongBong
		,zergException_OnLyLing
		,zergException_PrepareLurker
		,zergException_ReverseRush
		,zergException_HighTech
		,protossException_CarrierMany
		,protossException_Dark
		,protossException_Reaver
		,protossException_Scout
		,protossException_Shuttle
		,protossException_ShuttleMix
		,protossException_ZealotPush
		,protossException_DoubleNexus
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
	public Strategys LastStrategyBasic = null;
	public StrategysException LastStrategyException = null;
	
	public int Attackpoint=0;
	public int MyunitPoint = 0;
	public int ExpansionPoint = 0;
	public int UnitPoint = 0;
	public int CombatStartCase =0;
	
	public StrategyManager() {
		isInitialBuildOrderFinished = false;
		CurrentStrategyBasic = Strategys.protossBasic_Carrier;
		CurrentStrategyException = StrategysException.terranException_Wraith;
	}
	
	public void setCurrentStrategyBasic(Strategys strategy) {
		if(CurrentStrategyBasic != strategy){
			MyBotModule.Broodwar.printf("Set Strategy: " + strategy);
			LastStrategyBasic = CurrentStrategyBasic; 
			CurrentStrategyBasic = strategy;
			setCombatUnitRatio();
			
			MyBotModule.Broodwar.printf("vul:tank:goli, wgt = " + vultureratio+" : " +tankratio+" : "+goliathratio + ", " + wgt);
		}
	}
	public void setCurrentStrategyException(StrategysException strategy) {
		if(CurrentStrategyException != strategy){
			MyBotModule.Broodwar.printf("Set StrategyEX: " + strategy);
			LastStrategyException = CurrentStrategyException;
			CurrentStrategyException = strategy;
			setCombatUnitRatio();

			MyBotModule.Broodwar.printf("vul:tank:goli, wgt = " + vultureratio+" : " +tankratio+" : "+goliathratio + ", " + wgt);
		}
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
		
//		StrategyManager.Instance().setCurrentStrategyBasic(Strategys.zergBasic);
//		StrategyManager.Instance().setCurrentStrategyException(StrategysException.Init);
		InitialBuild.Instance().setInitialBuildOrder();	
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategyInit();
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		
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
	
	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
//		if (enemyBaseRegionVertices.isEmpty()) {
//			ScoutManager.Instance().calculateEnemyRegionVertices();
//			System.out.println(ScoutManager.Instance().enemyBaseRegionVertices.toString());
//		}
//			
//		BaseLocation sourceBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//		
//		System.out.println("sourceBaseLocationPoint : " + sourceBaseLocation.getPoint());
//		System.out.println("sourceBaseLocationP : " + sourceBaseLocation.getPosition());
//		System.out.println("sourceBaseLocationTP : " + sourceBaseLocation.getTilePosition());
//		System.out.println("sourceBaseLocationP : " + sourceBaseLocation.getRegion().getCenter());

		//TODO 전략은 자주 확인할 필요 없다, 1초에 한번 하지만@@!@!@ 초반에는 자주 확인해야된다 아래
		if ((MyBotModule.Broodwar.getFrameCount() < 3000 && MyBotModule.Broodwar.getFrameCount() % 5 == 0)
				||(MyBotModule.Broodwar.getFrameCount() >= 3000 && MyBotModule.Broodwar.getFrameCount() % 23 == 0)) {
			AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		}
		
		if (!isInitialBuildOrderFinished && BuildManager.Instance().buildQueue.isEmpty()) {
			if(isInitialBuildOrderFinished == false){
				MyBotModule.Broodwar.printf("Initial Build Finished");
			}
			isInitialBuildOrderFinished = true;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 29 == 0 && MyBotModule.Broodwar.getFrameCount() > 2500) {
			executeSupplyManagement();
		}
		if (MyBotModule.Broodwar.getFrameCount() % 43 == 0) {
			executeExpansion();
			executeResearch();
		}
		if(isInitialBuildOrderFinished == true){
			if(MyBotModule.Broodwar.getFrameCount() % 53 == 0) {
				executeUpgrade();
			}
		}
		if (isInitialBuildOrderFinished == true) {
			if (MyBotModule.Broodwar.getFrameCount() % 31 == 0){// info 의 멀티 체크가 31 에 돈다 
				executeCombat();
			}
		}
		
		if (isInitialBuildOrderFinished == false) {
			if (MyBotModule.Broodwar.getFrameCount() % 23 == 0){
				executeAddBuildingInit();
			}
		}else if (MyBotModule.Broodwar.getFrameCount() % 113 == 0) { //5초에 한번 팩토리 추가 여부 결정
			executeAddFactory();
		}
		
		if (MyBotModule.Broodwar.getFrameCount() % 5 == 0 && MyBotModule.Broodwar.getFrameCount() > 2500){
			executeWorkerTraining();
			executeCombatUnitTrainingBlocked();

			if (isInitialBuildOrderFinished == true) {
				executeCombatUnitTraining();
			}

		}
		if(MyBotModule.Broodwar.getFrameCount() % 239 == 0) {
			executeSustainBuilding();
		}
		if (MyBotModule.Broodwar.getFrameCount() % 281 == 0) {
			executeFly();
		}
		if (MyBotModule.Broodwar.getFrameCount() % 37 == 0){//약 4초에 한번
			RespondToStrategy.instance().update();//다른 유닛 생성에 비해 제일 마지막에 돌아야 한다. highqueue 이용하면 제일 앞에 있을 것이므로
			AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		}
		
	}
	
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
		return ret;
	}

	public static UnitType chooseunit(int ratea, int rateb, int ratec, int wgt, int tota, int totb, int totc){
		
		if( wgt < 1 || wgt > 3){
			wgt = 1;
		}
		double tempa = 0;
		double tempb = 0;
		double tempc = 0;
		if(ratea == 0){
			tempa = 99999999;	
		}else{
			tempa = 1.0/ratea*tota;
		}
		if(rateb == 0){
			tempb = 99999999;	
		}else{
			tempb = 1.0/rateb*totb;
		}
		if(ratec == 0){
			tempc = 99999999;	
		}else{
			tempc = 1.0/ratec*totc;
		}
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
		return BuildManager.Instance().buildQueue.getItemCount(checkunit) + 
				 MyBotModule.Broodwar.self().allUnitCount(checkunit);
		
//		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//			if (unit.getType() == UnitType.Terran_Factory) {
//				if (unit.isTraining() &&  unit.get) {
//					cnt += unit.getTrainingQueue().size();//TODO trainingqueue 에 모가 있는지를 알수가 없다.
//				}
//			}
//		}
	}
	
	private int GetCurrentTotBlocked(UnitType checkunit) {
		return  MyBotModule.Broodwar.self().allUnitCount(checkunit);
		
//		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//			if (unit.getType() == UnitType.Terran_Factory && unit.isTraining()) {
//				if(unit.getTrainingQueue() != null && unit.getTrainingQueue().size() > 0 && unit.getTrainingQueue().){
//					cnt ++;
//				}
//			}
//		}
	}
	
	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		if(MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed() < 2){
			return;
		}
		
		int tot_mineral_self = 0 ;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center){
				int minerals = WorkerManager.Instance().getWorkerData().getMineralsNearDepot(unit);
				if(minerals > 0){
					if(unit.isCompleted() == false){
						minerals= minerals * unit.getHitPoints()/1500;
					}
					tot_mineral_self += minerals;
				}
			}
			
 		}
		//TODO mineral 이 50 이하이고 scv 도 0 이면 다른고 취소하고 해야되는 로직 입력 필요. 만약 여기 안 넣을꺼면 미네랄 50을 걸어도 되고.. 아니면.... 흠....
		//TODO 현재 mineral 이 50이하인데 scv 가 0 일때 현재 빌드중인거 취소해야하는 만약 취소할것이 없다면!!! 총공격 모드!! 삽입해야되지 않을까
//		if (MyBotModule.Broodwar.getFrameCount() < 6000 && MyBotModule.Broodwar.self().minerals() < 50 ) {//어차피 저거 이후로는 총 공격이다.
//				
//		}

		//TODO 이거 만약 highest 가 blocking 안하는 애 인데 두번째 애가 블로킹일때는.. scv가 넘겨서 못 만들거 같은데............................. 오히려 안 바꾸는게 초반에 큰 문제를 안 일으킬수도 있고....
		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			int maxworkerCount = tot_mineral_self * 2;
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType()); // workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}
			
			if (workerCount < 70 && workerCount < maxworkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot() && unit.isCompleted() && unit.isTraining() == false) {
						
						BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
						BuildOrderItem checkItem = null; 

						if (!tempbuildQueue.isEmpty()) {
							checkItem= tempbuildQueue.getHighestPriorityItem();
							while(true){
								if(checkItem.blocking == true){
									break;
								}
								if(tempbuildQueue.canSkipCurrentItem() == true){
									tempbuildQueue.skipCurrentItem();
								}else{
									BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
									return;
								}
								checkItem = tempbuildQueue.getItem();
							}
							if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_SCV){
								return;
							}
						}

						if (checkItem == null){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
						}else if(checkItem.metaType.isUnit()){
							if(checkItem.metaType.getUnitType() == UnitType.Terran_Comsat_Station){
								return;
							}else if(checkItem.metaType.getUnitType() != UnitType.Terran_SCV){
								if(workerCount < 4){
									BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
								}else{
									int checkgas = checkItem.metaType.getUnitType().gasPrice() - MyBotModule.Broodwar.self().gas();
									if(checkgas < 0){
										checkgas = 0;
									}
									if(checkItem.blocking == true 
											&& (MyBotModule.Broodwar.self().minerals() > checkItem.metaType.getUnitType().mineralPrice()+50 - checkgas)) {
										BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	//부족한 인구수 충원
	public void executeSupplyManagement() {
		
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem checkItem = null; 

		if (!tempbuildQueue.isEmpty()) {
			checkItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				if(checkItem.blocking == true){
					break;
				}
				if(tempbuildQueue.canSkipCurrentItem() == true){
					tempbuildQueue.skipCurrentItem();
				}else{
					break;
				}
				checkItem = tempbuildQueue.getItem();
			}
			if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot){
				return;
			}
		}
		
		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() < 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 4;
			int fac_cnt = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory);
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
			int facFullOperating =0;
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit == null) continue;
				if (unit.getType() == UnitType.Terran_Factory){
					Faccnt ++;
				}
				if (unit.getType().isResourceDepot() && unit.isCompleted()){
					CCcnt++;
				}
				if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
					if(unit.isTraining() == true){
						facFullOperating++;
					}
				}
			}
						
			if(CCcnt == 1){//TODO 이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨
				if(factoryflag==false && barrackflag==true){
					supplyMargin = 5;
				}else if(factoryflag==true){
					supplyMargin = 5+4*fac_cnt+facFullOperating*2;
				}
			}else{ //if((MyBotModule.Broodwar.getFrameCount()>=6000 && MyBotModule.Broodwar.getFrameCount()<10000) || (Faccnt > 3 && CCcnt == 2)){
				supplyMargin = 8+5*fac_cnt+facFullOperating*2;
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

				if (currentSupplyShortage > onBuildingSupplyCount) {
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						if(InformationManager.Instance().getMainBaseSuppleLimit() <= MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Supply_Depot)){
//							MyBotModule.Broodwar.printf("currentSupplyShortage: " + currentSupplyShortage);
//							MyBotModule.Broodwar.printf("onBuildingSupplyCount: " + onBuildingSupplyCount);
//							MyBotModule.Broodwar.printf("supplyMargin: " + supplyMargin);
//							MyBotModule.Broodwar.printf("supplyTotal: " + MyBotModule.Broodwar.self().supplyTotal());
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Supply_Depot,BuildOrderItem.SeedPositionStrategy.NextSupplePoint, true);
						}else{
//							MyBotModule.Broodwar.printf("currentSupplyShortage: " + currentSupplyShortage);
//							MyBotModule.Broodwar.printf("onBuildingSupplyCount: " + onBuildingSupplyCount);
//							MyBotModule.Broodwar.printf("supplyMargin: " + supplyMargin);
//							MyBotModule.Broodwar.printf("supplyTotal: " + MyBotModule.Broodwar.self().supplyTotal());
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Supply_Depot,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
	}

	public void executeAddBuildingInit() {
		//팩토리
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) 
				+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null) < InitFaccnt) {
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
		}
		//가스
		if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery, null) 
				+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Refinery)
				+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Refinery, null) == 0){
			if(InformationManager.Instance().isGasRushed() == false){
				int barrack=0;
				int supple=0;
				int scv=0;
				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
				{
					if (unit == null) continue;
					if (unit.getType() == UnitType.Terran_Barracks){
						barrack++;
					}
					if (unit.getType() == UnitType.Terran_Supply_Depot  && unit.isCompleted()){
						supple++;
					}
					if (unit.getType() == UnitType.Terran_SCV && unit.isCompleted()){
						scv++;
					}
		 		}
				if(barrack > 0 && supple > 0 && scv>10){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}
		}
	}
	
	public void executeSustainBuilding() {
		
		boolean aca = false;
		boolean acaComplete = false;
		boolean barrack = false;
		boolean engineering = false;
		boolean timeaca = false;
		
	
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			//가스 start
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				
				Unit geyserAround = null;
				
				boolean refineryAlreadyBuilt = false; 
				for (Unit unitsAround: MyBotModule.Broodwar.getUnitsInRadius(unit.getPosition(), 300)){
					if (unitsAround == null) continue;
					if(unitsAround.getType() == UnitType.Terran_Refinery 
							|| unitsAround.getType() == UnitType.Zerg_Extractor 
							|| unitsAround.getType() == UnitType.Protoss_Assimilator){
						refineryAlreadyBuilt = true;
						break;
					}			
					if(unitsAround.getType() == UnitType.Resource_Vespene_Geyser){
						geyserAround = unitsAround;
					}
				}
				
				if(isInitialBuildOrderFinished == false && geyserAround != null){
					if(InformationManager.Instance().getMyfirstGas() !=null){
						if(geyserAround.getPosition().equals(InformationManager.Instance().getMyfirstGas().getPosition())){
							continue;
						}
					}
				}
				
				if(refineryAlreadyBuilt ==false){
					TilePosition findGeyser = ConstructionPlaceFinder.Instance().getRefineryPositionNear(unit.getTilePosition());
					if(findGeyser != null){
						if (findGeyser.getDistance(unit.getTilePosition())*32 > 300){
							continue;
						}
						if(WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) > 5){
							if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Refinery) == 0) {
								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery, findGeyser, false);
							}
						}
					}
				}
				
				if (MyBotModule.Broodwar.getFrameCount() > 15000 && unit.getType() == UnitType.Terran_Academy && unit.isCompleted() ){
					timeaca = true;
				}
			}
			
			if(timeaca){
				
			}
			//가스 end
			//컴샛 start
			if(unit.getType() == UnitType.Terran_Academy){
				aca = true;
				if(unit.isCompleted()){
					acaComplete = true;
				}
			}
			//컴샛 end
			
			//barrack start
			if(unit.getType() == UnitType.Terran_Barracks){
				barrack = true;
			}
			//barrack end
			
			//engineering start
			if(unit.getType() == UnitType.Terran_Engineering_Bay){
				engineering = true;
			}
			//engineering end
 		}
		
		//컴샛 start
		if(acaComplete){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if(unit == null) continue;
				if(unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() && unit.getAddon() == null){
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station, null) 
							+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Comsat_Station, null) == 0){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Comsat_Station, true);
					}
				}
	 		}
		}
		int CC=0;
		if(aca == false){
			CC = MyBotModule.Broodwar.self().completedUnitCount((UnitType.Terran_Command_Center));
			
			if(CC>2 || MyBotModule.Broodwar.getFrameCount() > 15000){
				if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Academy) == 0 
						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null) == 0) {
					MyBotModule.Broodwar.printf("make academy since we have many CC");
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy, false);
				}
			}
		}
		//컴샛 end
		
		//barrack start
		if(barrack == false  && isInitialBuildOrderFinished == true){
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Barracks) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Barracks, null) == 0) {
				MyBotModule.Broodwar.printf("make barrack because no barrack");
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, false);
			}
		}
		//barrack end
		
		//engineering start
		if(engineering == false && RespondToStrategy.instance().needOfEngineeringBay()){
			if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null) == 0) {
				MyBotModule.Broodwar.printf("make Engin because no Engin");
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, false);
			}
		}
		//engineering end
		
		
	}
	
	public void executeAddFactory() {
		
		int CCcnt = 0;
		int maxFaccnt = 0;
		int Faccnt = 0;
		int MachineShopcnt = 0;
		boolean facFullOperating = true;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType().isResourceDepot() && unit.isCompleted()){
				CCcnt++;
			}
			if (unit.getType() == UnitType.Terran_Factory){
				Faccnt ++;
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted()){
				if(unit.isTraining() == false){
					facFullOperating = false;
				}
			}
			if (unit.getType() == UnitType.Terran_Factory && unit.isCompleted() == false){
				facFullOperating = false;
			}
			if (unit.getType() == UnitType.Terran_Machine_Shop){
				MachineShopcnt ++;
			}
 		}
		Faccnt = Faccnt + ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Factory, null);
		
		if(CCcnt <= 1){
			if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				maxFaccnt = 4;
			}else{
				maxFaccnt = 3;
			}
		}else if(CCcnt == 2){
			if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
				maxFaccnt = 7;
			}else{
				maxFaccnt = 6;
			}
		}else if(CCcnt >= 3){
			maxFaccnt = 9;
		}
		
		int additonalmin = 0;
		int additonalgas = 0;
		if(facFullOperating == false){
			additonalmin = (Faccnt-1)*40;
			additonalgas = (Faccnt-1)*20;
		}
		
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory, null) == 0) {
			if(Faccnt == 0){
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			}else if(Faccnt < maxFaccnt){
				if(MyBotModule.Broodwar.self().minerals() > 250 + additonalmin && MyBotModule.Broodwar.self().gas() > 130 + additonalgas){
					if(Faccnt <= 5){
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					}else if(Faccnt <= 6){
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);
					}else{
						BaseLocation bestlocation =null;
						for(BaseLocation locations : InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.self())){
							if(locations == null) continue;
							if (locations.getTilePosition().equals(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition())) continue;
							if (locations.getTilePosition().equals(InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self()).getTilePosition())) continue;
							
							if(bestlocation == null){
								bestlocation = locations;
							}
						}
						if(bestlocation == null){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);
						}else{
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory,bestlocation.getTilePosition(), false);
						}
					}
				}
			}
		}
		
		
//		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//		BuildOrderItem checkItem = null; 
//
//		if (!tempbuildQueue.isEmpty()) {
//			checkItem= tempbuildQueue.getHighestPriorityItem();
//			while(true){
//				if(checkItem.blocking == true){
//					break;
//				}
//				if(tempbuildQueue.canSkipCurrentItem() == true){
//					tempbuildQueue.skipCurrentItem();
//				}else{
//					break;
//				}
//				checkItem = tempbuildQueue.getItem();
//			}
////			if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop){
////				if(Faccnt > MachineShopcnt+1)
////				tempbuildQueue.removeCurrentItem();
////			}
//		}
		
		if( MachineShopcnt < 4 && MachineShopcnt + 2 < Faccnt){
			if(MyBotModule.Broodwar.self().minerals() > 50 && MyBotModule.Broodwar.self().gas() > 50){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop, true);
				}
			}
		}
		
//		if(currentItem.metaType.isUnit() == true && currentItem.metaType.isRefinery()){
//			tempbuildQueue.removeCurrentItem();
//			break;
//		}
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
			
			if(vultureratio == 0 && tankratio == 0 && goliathratio == 0){
				vultureratio = Config.vultureratio[CurrentStrategyBasic.ordinal()];
				tankratio = Config.tankratio[CurrentStrategyBasic.ordinal()];
				goliathratio = Config.goliathratio[CurrentStrategyBasic.ordinal()];
				wgt = Config.wgt[CurrentStrategyBasic.ordinal()];
			}
		}
	}
	public int getFacUnits(){
		
		int tot=0;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Vulture && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && unit.isCompleted() ){
				tot++;
			}
			if (unit.getType() == UnitType.Terran_Goliath && unit.isCompleted() ){
				tot++;
			}
 		}
		return tot*4; //인구수 기준이므로
	}

	public void executeCombatUnitTrainingBlocked() {
		
		BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem currentItem = null; 
		Boolean goliathInTheQueue = false;
		Boolean tankInTheQueue = false;
		
		if(MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed() < 4){
			return;
		}
		if (!tempbuildQueue.isEmpty()) {
			currentItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				
				
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					goliathInTheQueue = true;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					tankInTheQueue = true;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot){
					return;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Vulture){
					return;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_SCV){
					return;
				}
				if(currentItem.blocking == true){
					break;
				}
				if(tempbuildQueue.canSkipCurrentItem() == true){
					tempbuildQueue.skipCurrentItem();
				}else{
					break;
				}
				currentItem = tempbuildQueue.getItem();
			}
		}else{
			return;
		}
		
		if(Config.BuildQueueDebugYN){
			System.out.println("picked: "+ currentItem.metaType.getName());
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
				
				//TODO else 가 들어가야할까. addon 있는놈일때는 신겨 안쓰게끔?
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Machine_Shop && unit.getAddon() == null ){
					continue;
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode){
					if(unit.getAddon() != null && unit.getAddon().isCompleted() != true){
						continue;
					}
				}
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Goliath){
					if(isarmoryexists){
						break;
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
				
				if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType()!=selected){
					if(selected == UnitType.Terran_Siege_Tank_Tank_Mode && tankInTheQueue == false){
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
					}else if(selected == UnitType.Terran_Goliath && goliathInTheQueue == false){
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
//						if(selected == UnitType.Terran_Goliath && isarmoryexists == false){
//							continue;
//						}
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

		int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
		int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
		int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
		
		UnitType selected = null; 
		int currentinbuildqueuecnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null) +
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode, null) +
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath, null);

		if(currentinbuildqueuecnt == 0){
			selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
			
			
			if(selected.mineralPrice() < MyBotModule.Broodwar.self().minerals() &&	selected.gasPrice() < MyBotModule.Broodwar.self().gas() && MyBotModule.Broodwar.self().supplyUsed() <= 392){
				if(Config.BuildQueueDebugYN){
					System.out.println("queue in selected by basic:" + selected);
				}
				BuildManager.Instance().buildQueue.queueAsLowestPriority(selected,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
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

	public boolean enemyExspansioning(){
		//int cnt = MyBotModule.Broodwar.enemy().incompleteUnitCount(InformationManager.Instance().getBasicResourceDepotBuildingType(InformationManager.Instance().enemyRace));
		List<UnitInfo> enemyCC = null;
		enemyCC = InformationManager.Instance().getEnemyUnits(InformationManager.Instance().getBasicResourceDepotBuildingType(MyBotModule.Broodwar.enemy().getRace()));
		
		for (UnitInfo target : enemyCC) {
			if(target.isCompleted()){
				return true;
			}
		}
		return false;
	}
	public int selfExspansioning(){
		
		int cnt=0;
		for(Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center){
				if(unit.isCompleted() == false){
					cnt++;
				}else if(WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(unit) < 7){
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	
	//TODO 상대방 신규 멀티를 찾았을때 공격 여부 한번더 돌려야함(상대 멀티 진행 여부 판단해야되므로
	public void executeCombat() {
		
		int unitPoint = 0;
		int expansionPoint = 0;
		int totPoint = 0;
		
		int selfbasecnt = 0;
		int enemybasecnt = 0;
		
		//boolean allaware = InformationManager.Instance().isReceivingEveryMultiInfo();
		
		//TODO 아군은 돌아가기 시작하고 scv 특정 수 이상 붙은 컴맨드만 쳐야 하지 않나?
		selfbasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().selfPlayer);
		enemybasecnt = InformationManager.Instance().getOccupiedBaseLocationsCnt(InformationManager.Instance().enemyPlayer);
		//TODO상대가 다른곳에 파일론만 지으면.. 문제 되는데..
		//CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_INSIDE);
		
		if(enemybasecnt == 0){
			return;
		}
		
		int myunitPoint = getFacUnits();
		
		//공통 예외 상황
		if(myunitPoint > 280 || MyBotModule.Broodwar.self().supplyUsed() > 392){//팩토리 유닛  130 이상 또는 서플 196 이상 사용시(스타 내부에서는 2배)
			MyBotModule.Broodwar.printf("Many Unit Attack!");
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			CombatStartCase = 1;
		}
		if(MyBotModule.Broodwar.getFrameCount() > 8000 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV) < 7){//TODO5드론에서 피해를 봣을때의 기준에 따라서 수치 조정 필요.. 감이 없음
			MyBotModule.Broodwar.printf("Too Less SCV ATTACK!!");
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY); //TODO 올인에 대한거 하나 만들어야 겠다... 올인이면 공격 취소도 하면 안되므로..
		}
		
		
		
		//공통 기본 로직, 팩토리 유닛 50마리 기준 유닛 Kill Death 상황에 따라 변경.
		
		int deadcombatunit = getTotDeadCombatUnits();
		int killedcombatunit = getTotKilledCombatUnits();
		int totworkerdead = MyBotModule.Broodwar.self().deadUnitCount(UnitType.Terran_SCV)*2;
		int totworkerkilled = MyBotModule.Broodwar.self().killedUnitCount(InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace))*2;
		int totaldeadunit = MyBotModule.Broodwar.self().deadUnitCount();
		int totalkilledunit = MyBotModule.Broodwar.self().deadUnitCount();
		
		
		
		//if(selfbasecnt > enemybasecnt){ //약 시작 ~ 15분까지
		if(MyBotModule.Broodwar.getFrameCount() < 20000){ //약 시작 ~ 15분까지
			unitPoint += (totworkerkilled - totworkerdead) * (-MyBotModule.Broodwar.getFrameCount()/40000.0*3.0 + 3.0);
			unitPoint += (killedcombatunit - deadcombatunit);
			
		}else if(MyBotModule.Broodwar.getFrameCount() < 40000){ //약 15분 ~ 28분까지  // 여기서부턴 시간보다.... 현재 전체 규모수가 중요할듯?
			unitPoint += (totworkerkilled - totworkerdead) * (-MyBotModule.Broodwar.getFrameCount()/40000.0*3.0 + 3.0);
			unitPoint += (killedcombatunit - deadcombatunit)* (-MyBotModule.Broodwar.getFrameCount()/20000.0 + 2.0);
		}
		
		
		if(selfbasecnt == enemybasecnt && selfExspansioning() ==0 && enemyExspansioning() == false){ //베이스가 동일할때
			if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
				expansionPoint += 5;
			}
		}else if(selfbasecnt > enemybasecnt){//우리가 베이스가 많으면
			if(selfbasecnt - enemybasecnt == 1){
				if(selfExspansioning() > 0){
					if(enemyExspansioning() == false){
						expansionPoint -= 15;
					}else{ 
						expansionPoint += 30;
					}
				}else if(selfExspansioning() == 0){
					if(enemyExspansioning() == true){
						expansionPoint += 40;
					}
					if(enemyExspansioning() == false){
						expansionPoint += 20;
					}
				}
			}
			if(selfbasecnt - enemybasecnt > 1 && selfbasecnt - enemybasecnt > selfExspansioning()){
				expansionPoint += 40;
			}
		}else if(selfbasecnt < enemybasecnt){//우리가 베이스가 적으면
			if(enemybasecnt - selfbasecnt == 1){
				if(selfExspansioning() > 0){ 
					if(enemyExspansioning() == false){
						expansionPoint -= 20;
						if (InformationManager.Instance().enemyRace == Race.Zerg || InformationManager.Instance().enemyRace == Race.Protoss) {
							expansionPoint += 5;
						}
					}else{
						expansionPoint -= 10;
					}
				}else{
					if(enemyExspansioning() == true){
						expansionPoint += 10;
					}
				}
			}else{
				expansionPoint -= 20;
			}
		}
		
		//종족별 예외 상황
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if( myunitPoint > 80 && killedcombatunit-deadcombatunit > myunitPoint/4 ){//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
		}
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			if( myunitPoint > 80 && killedcombatunit-deadcombatunit > myunitPoint/3 ){//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
			
			//앞마당 photo
//			if(StrategysException.protossException_ForgeDouble && myunitPoint >= 60 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3){
//				MyBotModule.Broodwar.printf("Ap Ma Dang Photo?! Attack!!!");
//				CombatStartCase == 3
//				CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
//			}
			
			//캐리어
			//본진 photo<-- 무시
		}
				
		if (InformationManager.Instance().enemyRace == Race.Zerg) {
			if( myunitPoint > 80 && killedcombatunit-deadcombatunit > myunitPoint/3 ){//죽인수 - 죽은수 가  현재 내 유닛의 일정 비율이 넘으면 가산점
				unitPoint += 20;
			}
			//triple hatchery
		}

		//내 팩토리 유닛 인구수 만큼 추가
		totPoint = 	myunitPoint + expansionPoint + unitPoint;
		
		if(totPoint > 120 && CombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY){// 팩토리 유닛이 30마리(즉 스타 인구수 200 일때)
			MyBotModule.Broodwar.printf("Total Combat Point Over 120 Attack!!");
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK_ENEMY);
			CombatStartCase = 2;
		}
		
		
		if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY){
			if(CombatStartCase == 1 && myunitPoint < 20){
				MyBotModule.Broodwar.printf("Retreat1");
				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
			}
			if(CombatStartCase == 2 && totPoint < 40){
				MyBotModule.Broodwar.printf("Retreat2");
				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
			}
			if(CombatStartCase == 3 && (myunitPoint < 8 || unitPoint < -20) ){
				MyBotModule.Broodwar.printf("Retreat3");
				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_CHOKEPOINT);
			}
		}
		
		MyunitPoint = myunitPoint;
		ExpansionPoint = expansionPoint;
		UnitPoint = unitPoint;
		Attackpoint = totPoint;
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
				else if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) > 2 && getFacUnits() > 120 && MyBotModule.Broodwar.self().minerals()> 250 && MyBotModule.Broodwar.self().gas()> 225 ){
					
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
		
		boolean VS = (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1 ? true : false)
				||(MyBotModule.Broodwar.self().isUpgrading(UpgradeType.Ion_Thrusters) ? true : false);
		boolean VM = (MyBotModule.Broodwar.self().hasResearched(TechType.Spider_Mines))
				||(MyBotModule.Broodwar.self().isResearching(TechType.Spider_Mines));
		boolean TS = (MyBotModule.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode))
				||(MyBotModule.Broodwar.self().isResearching(TechType.Tank_Siege_Mode));
		boolean GR = (MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false)
				||(MyBotModule.Broodwar.self().isUpgrading(UpgradeType.Charon_Boosters) ? true : false);
		
		if(VS&&VM&&TS&&GR) return; // 4개 모두 완료이면
		
		int currentResearched =0;
		if(VS){	currentResearched++;}
		if(VM){	currentResearched++;}
		if(TS){	currentResearched++;}
		if(GR){	currentResearched++;}
		
		if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) < 2 
				&& currentResearched >= 2 
				&& getFacUnits() < 32 
				&& !(MyBotModule.Broodwar.self().minerals()> 200 && MyBotModule.Broodwar.self().gas()> 150)) return;
		
		MetaType vultureSpeed = new MetaType(UpgradeType.Ion_Thrusters);
		MetaType vultureMine = new MetaType(TechType.Spider_Mines);
		MetaType TankSiegeMode = new MetaType(TechType.Tank_Siege_Mode);
		MetaType GoliathRange = new MetaType(UpgradeType.Charon_Boosters);
		
		MetaType vsZerg[] = new MetaType[]{vultureMine, GoliathRange, TankSiegeMode, vultureSpeed};
		boolean vsZergbool[] = new boolean[]{VM, GR, TS, VS};
		MetaType vsZergHydra[] = new MetaType[]{vultureMine, TankSiegeMode, GoliathRange, vultureSpeed};
		boolean vsZergHydrabool[] = new boolean[]{VM, TS, GR, VS};
		MetaType vsTerran[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
		boolean vsTerranbool[] = new boolean[]{VM, VS, TS, GR};
		MetaType vsTerranBio[] = new MetaType[]{vultureSpeed, TankSiegeMode, vultureMine, GoliathRange};
		boolean vsTerranBiobool[] = new boolean[]{VS, TS, VM, GR};
		MetaType vsProtoss[] = new MetaType[]{TankSiegeMode, vultureMine, vultureSpeed, GoliathRange};
		boolean vsProtossbool[] = new boolean[]{TS, VM, VS, GR};
		MetaType vsProtossZealot[] = new MetaType[]{vultureSpeed, vultureMine, TankSiegeMode, GoliathRange};
		boolean vsProtossZealotbool[] = new boolean[]{VS, VM, TS, GR};
		MetaType vsProtossAggressive[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
		boolean vsProtossAggressivebool[] = new boolean[]{VM, VS, TS, GR};
		
		MetaType[] Current = null;
		boolean[] Currentbool = null;
		boolean air = true;
		boolean terranBio = false;
		
		if(MyBotModule.Broodwar.enemy().getRace() == Race.Protoss){
			Current = vsProtoss;
			Currentbool = vsProtossbool;
			if(CurrentStrategyException == StrategyManager.StrategysException.protossException_ZealotPush
					|| (CurrentStrategyException == StrategyManager.StrategysException.Init
					    && LastStrategyException == StrategyManager.StrategysException.protossException_ZealotPush)){
				Current = vsProtossZealot;
				Currentbool = vsProtossZealotbool;
			}
//			if(CurrentStrategyException == StrategyManager.StrategysException.protossException_DoubleNexus
//			(CurrentStrategyException == StrategyManager.StrategysException.NoEarlyDragoonAttack
//					|| (CurrentStrategyException == StrategyManager.StrategysException.Init
//					    && LastStrategyException == StrategyManager.StrategysException.NoEarlyDragoonAttack))){
//				Current = vsProtossAggressive;
//				Currentbool = vsProtossAggressivebool;
//			}
			
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
			if(CurrentStrategyBasic == Strategys.terranBasic_Bionic){
				Current = vsTerranBio;
				Currentbool = vsTerranBiobool;
				terranBio = true;
			}else{
				Current = vsTerran;
				Currentbool = vsTerranbool;
			}
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
			air= true;//저그는 하늘 그냥 준비한다.
			if(CurrentStrategyBasic == Strategys.zergBasic_HydraWave
					|| CurrentStrategyBasic == Strategys.zergBasic_LingHydra
					){
				Current = vsZergHydra;
				Currentbool = vsZergHydrabool;
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
							if(!air && !(MyBotModule.Broodwar.self().minerals()> 300 && MyBotModule.Broodwar.self().gas()> 300)){
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
							UpgradeType tempU = null;
							if(Current[i].isUpgrade()){
								boolean booster = false;
								for (Unit unitcheck : MyBotModule.Broodwar.self().getUnits())
								{
									if(unitcheck.getType() == UnitType.Terran_Armory && unitcheck.isCompleted()){
										booster = true;
									}
								}
								tempU = Current[i].getUpgradeType();
								if(tempU == UpgradeType.Charon_Boosters && booster == false){
									 return;
								}
							}
							if(currentResearched==0){
								BuildManager.Instance().buildQueue.queueAsLowestPriority(Current[i], true);
							}else{
								BuildManager.Instance().buildQueue.queueAsLowestPriority(Current[i], false);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	public int getValidMineralsForExspansionNearDepot(Unit depot)
	{
		if (depot == null) { return 0; }

		int mineralsNearDepot = 0;

		for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 320 && unit.getResources() > 200)
			{
				mineralsNearDepot++;
			}
		}

		return mineralsNearDepot;
	}
	public void executeExpansion() {
		
		if(MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.Terran_Command_Center)>0){
			return;
		}
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
				+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null) != 0) {
			return;
		}
		
		int MaxCCcount = 4;
		int CCcnt =0 ;
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				if(getValidMineralsForExspansionNearDepot(unit) > 6){
					CCcnt++;
				}
			}
 		}
		if(CCcnt >= MaxCCcount){
			return;
		}
		
		int RealCCcnt = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
		int RealcompletedCCcnt = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
		
		//앞마당 전
		if(CCcnt == 1 && RealCCcnt < 2){//TODO 이거 손봐야된다... 만약 위로 띄어서 해야한다면?? 본진에 지어진거 카운트 안되는 상황에서 앞마당에 지어버리겟네
			if (isInitialBuildOrderFinished == false) {
				return;
			}
			
			if((MyBotModule.Broodwar.self().minerals() > 600 && getFacUnits() > 40) || (getFacUnits() > 60 && Attackpoint > 60 && MyBotModule.Broodwar.self().minerals() > 400)){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					MyBotModule.Broodwar.printf("Build First Expansion");
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			if( MyBotModule.Broodwar.getFrameCount() > 6000 && MyBotModule.Broodwar.self().minerals() > 400){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					MyBotModule.Broodwar.printf("Build First Expansion");
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
				}
			}
			
		}
		
		//앞마당 이후
		if(CCcnt >= 2){
			
			// 돈이 600 넘고 아군 유닛이 많으면 멀티하기
			if( MyBotModule.Broodwar.self().minerals() > 600 && getFacUnits() > 120){
				MyBotModule.Broodwar.printf("Build Next Expansion");
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//공격시 돈 250 넘으면 멀티하기
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY && MyBotModule.Broodwar.self().minerals() > 250){
				MyBotModule.Broodwar.printf("Build Next Expansion");
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//800 넘으면 멀티하기
			if(MyBotModule.Broodwar.self().minerals() > 800){
				MyBotModule.Broodwar.printf("Build Next Expansion");
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			
			//500 넘고 유리하면
			if( MyBotModule.Broodwar.self().minerals() > 500 && getFacUnits() > 50 && Attackpoint > 50){
				MyBotModule.Broodwar.printf("Build Next Expansion");
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//200 넘고 유리하면
			if( MyBotModule.Broodwar.self().minerals() > 200 && getFacUnits() > 50 && Attackpoint > 80){
				MyBotModule.Broodwar.printf("Build Next Expansion");
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
		}
	}

	private void executeFly() {
		if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 1){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
			{
				if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay)&& unit.isCompleted()){
					unit.lift();
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