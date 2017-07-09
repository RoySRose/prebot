package pre.manager;
import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import pre.AnalyzeStrategy;
import pre.BuildOrderItem;
import pre.BuildOrderQueue;
import pre.Config;
import pre.InitialBuild;
import pre.MetaType;
import pre.WorkerData;
import pre.main.MyBotModule;
import pre.manager.CombatManager.CombatStrategy;
import pre.manager.StrategyManager.Strategys;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();
	
	private boolean isInitialBuildOrderFinished;
	
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
	
	
	public enum Strategys { BaiscVsZerg, BaiscVsProtoss, BaiscVsTerran, Init} //기본 전략 나열
	public enum StrategysException { Init, Temp} //예외 전략 나열, 예외가 아닐때는 무조건 Init 으로 

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	private Strategys CurrentStrategyBasic = null;
	private StrategysException CurrentStrategyException = null;
	
	
	public StrategyManager() {
		
		isInitialBuildOrderFinished = false;
		CurrentStrategyBasic = Strategys.valueOf("BaiscVsZerg");
		CurrentStrategyException = StrategysException.valueOf("Init");
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
		//setCombatUnitRatio();
		AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		InitialBuild.Instance().setInitialBuildOrder();	
		InitFaccnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory);
		
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {

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
		if ((MyBotModule.Broodwar.getFrameCount() < 5000 && MyBotModule.Broodwar.getFrameCount() % 4 == 0)
				||(MyBotModule.Broodwar.getFrameCount() > 5000 && MyBotModule.Broodwar.getFrameCount() % 24 == 0)) {
			AnalyzeStrategy.Instance().AnalyzeEnemyStrategy();
		}

		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 == 0) {
			executeSupplyManagement();
		}
		if (MyBotModule.Broodwar.getFrameCount() % 120 == 0) {
			executeExpansion();
		}
		
		
		if (isInitialBuildOrderFinished == true) {
			if (MyBotModule.Broodwar.getFrameCount() % 119 == 0){
				executeCombat();
			}
		}
		
		
		if (MyBotModule.Broodwar.getFrameCount() % 6 == 0){
			executeWorkerTraining();
			executeCombatUnitTrainingBlocked();

			if (isInitialBuildOrderFinished == true) {
				executeCombatUnitTraining();
			}
			executeExeptionalCombatUnitTraining();//다른 유닛 생성에 비해 제일 마지막에 돌아야 한다. highqueue 이용하면 제일 앞에 있을 것이므로
		}
		
		if (isInitialBuildOrderFinished == false) {
			if (MyBotModule.Broodwar.getFrameCount() % 24 == 0){
				executeAddFactoryInit();
			}
		}else if (MyBotModule.Broodwar.getFrameCount() % 120 == 0 && isInitialBuildOrderFinished == true) { //5초에 한번 팩토리 추가 여부 결정
			executeAddFactory();
		}
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
		if (isInitialBuildOrderFinished == false) {
			return;
		}

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
			
			if(MyBotModule.Broodwar.getFrameCount()<14000){
				if(barrackflag==true && factoryflag==false){
					supplyMargin = 6;
				}else if(factoryflag==true){
					supplyMargin = 6+4*fac_cnt;
				}
			}else if(MyBotModule.Broodwar.getFrameCount()>14000 && MyBotModule.Broodwar.getFrameCount()<28000){
				supplyMargin = 8+4*fac_cnt;
			}else{
				supplyMargin = 12+4*fac_cnt;
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
			maxFaccnt = 8;
		}
		
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
		if (!tempbuildQueue.isEmpty()) {
			currentItem= tempbuildQueue.getHighestPriorityItem();
			while(true){
				if(Config.BuildQueueDebugYN){
					System.out.println("picked: "+ currentItem.metaType.getName());
				}
				if(currentItem.blocking == true){
					break;
				}
				if(currentItem.metaType.getUnitType() == UnitType.Terran_Vulture){
					return;
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

		//@@@@@@ 들어가는 선 조건이 있어야 하지 않을지.... 매번 팩토리 다 볼수는 없지 않나????????? 겉에 루프가 없어도 될거 같은데 나중에 실험해 보자.. 한 프레임에 한번만 들어와서 팩토리 하나만 하고 break 하고 다음에 빈거 또 하겠지?
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
	
	public void executeExeptionalCombatUnitTraining() {
		//@@@@@@ 사이언스 베슬이나 발키리 하는 로직
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
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK);
		}
		if(MyBotModule.Broodwar.getFrameCount() > 8000 && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV) < 7){//@@@@@@5드론에서 피해를 봣을때의 기준에 따라서 수치 조정 필요.. 감이 없음
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK); //@@@@@@ 올인에 대한거 하나 만들어야 겠다... 올인이면 공격 취소도 하면 안되므로..
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
	
		//@@@@@@ 일단 공격 중이라면......
		if(point > 120){// 팩토리 유닛이 50마리(즉 스타 인구수 200 일때)
			CombatManager.Instance().setCombatStrategy(CombatStrategy.ATTACK);
		}else{
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK){
				if(point < 40){//@@@@@@ 후반부터는 상대 죽인수가 의미가 없어지기 때문에.... defence 기준을 다르게 잡아야한다.
					CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_INSIDE);
				}
			}else{
				CombatManager.Instance().setCombatStrategy(CombatStrategy.DEFENCE_INSIDE);
			}
		}
	}
	
	public void executeExpansion() {
		
		int CCcnt = 0;
		int MaxCCcount = 5;
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			if (unit.getType() == UnitType.Terran_Command_Center && unit.isCompleted() ){
				if(WorkerManager.Instance().getWorkerData().getMineralsNearDepot(unit) > 5){
					CCcnt++;
				}
			}
 		}
		
		//앞마당 전
		if(CCcnt == 1){
			if (isInitialBuildOrderFinished == false) {
				return;
			}
			if( MyBotModule.Broodwar.self().minerals() > 500 && getFacUnits() > 40){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
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
				if( MyBotModule.Broodwar.self().minerals() > 800 && getFacUnits() > 40){
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
				}
			}
			//공격시 돈 400 넘으면 멀티하기
			if(CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK){
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center, null)
						+ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null)== 0) {
					if( MyBotModule.Broodwar.self().minerals() > 400){
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Command_Center,BuildOrderItem.SeedPositionStrategy.NextExpansionPoint, true);
					}
				}
			}
			
		}
	}
	

	/*
	public void executeCombat() {

		// 공격 모드가 아닐 때에는 전투유닛들을 아군 진영 길목에 집결시켜서 방어
		if (isFullScaleAttackStarted == false) {
			Chokepoint firstChokePoint = BWTA.getNearestChokepoint(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition());

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatUnitType() && unit.isIdle()) {
					commandUtil.attackMove(unit, firstChokePoint.getCenter());
				}
			}

			// 전투 유닛이 2개 이상 생산되었고, 적군 위치가 파악되었으면 총공격 모드로 전환
			if (MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getBasicCombatUnitType()) > 2) {
				if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown  
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {				
					isFullScaleAttackStarted = true;
				}
			}
		}
		// 공격 모드가 되면, 모든 전투유닛들을 적군 Main BaseLocation 로 공격 가도록 합니다
		else {
			//std.cout << "enemy OccupiedBaseLocations : " << InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance()._enemy).size() << std.endl;
			
			if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown 
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) 
			{					
				// 공격 대상 지역 결정
				BaseLocation targetBaseLocation = null;
				double closestDistance = 100000000;

				for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
					double distance = BWTA.getGroundDistance(
						InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition(), 
						baseLocation.getTilePosition());

					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = baseLocation;
					}
				}

				if (targetBaseLocation != null) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						// 건물은 제외
						if (unit.getType().isBuilding()) {
							continue;
						}
						// 모든 일꾼은 제외
						if (unit.getType().isWorker()) {
							continue;
						}
											
						// canAttack 유닛은 attackMove Command 로 공격을 보냅니다
						if (unit.canAttack()) {
							
							if (unit.isIdle()) {
								commandUtil.attackMove(unit, targetBaseLocation.getPosition());
							}
						} 
					}
				}
			}
		}
	}
	*/
}