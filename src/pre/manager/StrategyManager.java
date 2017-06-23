package pre.manager;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import pre.BuildOrderItem;
import pre.InitialBuild;
import pre.MetaType;
import pre.main.MyBotModule;
import pre.util.CommandUtil;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();

	private boolean isFullScaleAttackStarted;
	private boolean isInitialBuildOrderFinished;
	
	private boolean previousStrategyStatus;
	private boolean CurrentStrategyStatus;
	private boolean CreateSCVOn;
	private boolean AttackUnitCreate;
	

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	public StrategyManager() {
		isFullScaleAttackStarted = false;
		isInitialBuildOrderFinished = false;
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		InitialBuild.Instance().setInitialBuildOrder();		
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {

	}

	public static int gcd(int a, int b)
	{ //삼항 연산자 축약형 
	    return (a % b == 0 ? b : gcd(b,a%b));
	}

	public static int lcm(int a,int b){
	    return a*b/gcd(a,b);
	}
	
	public static int least(int a, int b, int c, int checker){
		
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
		
		int res = 0;
	
		if( wgt < 1 || wgt > 3)
			wgt = 1;
		
		res = lcm(lcm(ratea,rateb),ratec);
		int tempa = res/ratea*tota;
		int tempb = res/rateb*totb;
		int tempc = res/ratec*totc;
		
		int num = least(tempa,tempb,tempc,wgt);
		
		if(num == 2){//1:벌쳐, 2:시즈, 3:골리앗
			return UnitType.Terran_Goliath;
		}else if(num == 3){
			return UnitType.Terran_Siege_Tank_Tank_Mode;
		}else{
			return UnitType.Terran_Vulture;
		}
	}
	
	private int getcurrenttot(UnitType checkunit) {
		
		int cnt;
		cnt = BuildManager.Instance().buildQueue.getItemCount(checkunit) + 
				 MyBotModule.Broodwar.self().completedUnitCount(checkunit);
		
		return cnt;
	}

	
	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		
		//@@@@@@ 초반 4드론 5드론 scv 러쉬 등에 대해서도 초반 빌드오더 버려야함
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}

		//@@@@@@ 상대 정보를 판단해서 분석하고 전략 세팅하는것 필요
		//AnalyzeStrategy();
		
		executeWorkerTraining();
		executeSupplyManagement();
		executeBasicCombatUnitTraining();
		executeCombat();
	}

	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}
			
			if (workerCount < 30) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue
									.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
								// std.cout << "worker enqueue" << std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(
										new MetaType(InformationManager.Instance().getWorkerType()), false);
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

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
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
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Factory) {
					factoryflag = true;
				}
				if (unit.getType() == UnitType.Terran_Barracks) {
					barrackflag = true;
				}
			}
			
			if(MyBotModule.Broodwar.getFrameCount()<14000){
				if(barrackflag==true && factoryflag==false){
					supplyMargin = 6;
				}else if(factoryflag==true){
					supplyMargin = 4+4*fac_cnt;
				}
			}else if(MyBotModule.Broodwar.getFrameCount()>14000 && MyBotModule.Broodwar.getFrameCount()<28000){
				supplyMargin = 6+4*fac_cnt;
			}else{
				supplyMargin = 8+4*fac_cnt;
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

	public void executeAddFactory() {
		//@@@@@@ 팩토리 전체가 다 생산하고 있고 자원이 일정 이상 남았을시에 추가 팩토리를 짓는데.... 세부 기준은? 
		
		
		
	}
	public void executeBasicCombatUnitTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		int ratea = 1;
		int rateb = 2;
		int ratec = 3;
		int wgt = 1;
		
		//@@@@@@ getTrainingQueue 에 있는 애들도 확인해야 할듯? buildqueue 와 달라보임
		int tot_vulture = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Vulture);
		int tot_tank = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
						+ MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode);
		int tot_goliath = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Goliath);
		//int faccnt = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory);
		UnitType selected = chooseunit(ratea, rateb, ratec, wgt, tot_vulture, tot_tank, tot_goliath);; 
		
		
		//@@@@@@ 들어가는 선 조건이 있어야 하지 않을지.... 매번 팩토리 다 볼수는 없지 않나????????? 겉에 루프가 없어도 될거 같은데 나중에 실험해 보자.. 한 프레임에 한번만 들어와서 팩토리 하나만 하고 break 하고 다음에 빈거 또 하겠지?
		
		if(selected == UnitType.Terran_Vulture && MyBotModule.Broodwar.self().minerals() >= 100 && MyBotModule.Broodwar.self().supplyUsed() < 396
				||selected == UnitType.Terran_Siege_Tank_Tank_Mode && MyBotModule.Broodwar.self().minerals() >= 170 && MyBotModule.Broodwar.self().gas() >= 120 && MyBotModule.Broodwar.self().supplyUsed() < 392
				||selected == UnitType.Terran_Goliath && MyBotModule.Broodwar.self().minerals() >= 120 && MyBotModule.Broodwar.self().minerals() >= 70 && MyBotModule.Broodwar.self().supplyUsed() < 396){
			
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Factory) {
					if (unit.isTraining() == false) {	
						if(selected == UnitType.Terran_Vulture && MyBotModule.Broodwar.self().minerals() >= 95 && MyBotModule.Broodwar.self().supplyUsed() < 396){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Vulture,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							tot_vulture++;
						}else if(selected == UnitType.Terran_Siege_Tank_Tank_Mode && MyBotModule.Broodwar.self().minerals() >= 170 && MyBotModule.Broodwar.self().gas() >= 120 && MyBotModule.Broodwar.self().supplyUsed() < 392){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Siege_Tank_Tank_Mode,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							tot_tank++;
						}
						else if(selected == UnitType.Terran_Goliath && MyBotModule.Broodwar.self().minerals() >= 120 && MyBotModule.Broodwar.self().minerals() >= 70 && MyBotModule.Broodwar.self().supplyUsed() < 396){
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Goliath,BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
							tot_goliath++;
						}
						selected = chooseunit(ratea, rateb, ratec, wgt, tot_vulture, tot_tank, tot_goliath);
					}
				}
			}
		}
		
		//@@ 멀티를 하기위해서 생산을 쉬어야 한다면??? 어떻게 판단하지?
		//@@ 사이언스 베슬이나 발키리 하는 로직
		
		
	}

	public void executeCombat() {
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Marine) >= 5) {
			CombatManager.Instance().setAggressive(true);
		} else {
			CombatManager.Instance().setAggressive(false);
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