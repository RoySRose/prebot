package prebot.main.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import bwta.BaseLocation;
import prebot.brain.Idea;
import prebot.brain.buildaction.BuildAction;
import prebot.brain.history.History;
import prebot.brain.history.HistoryFrame;
import prebot.brain.knowledge.Knowledge;
import prebot.brain.knowledge.inerrable.InerrableEssentials;
import prebot.brain.squad.Squad;
import prebot.common.util.FileUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.internal.UnitCache;
import prebot.main.Prebot;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager extends GameManager {

	public History historyData = new History();
	private Idea idea = null;

	public Idea getIdea() {
		return idea;
	}

	private static StrategyManager instance = new StrategyManager();

	public static StrategyManager Instance() {
		return instance;
	}

	public StrategyManager() {
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		// 과거 게임 기록을 로딩합니다
		loadGameRecordList();
	}

	/// 경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
//		saveGameRecordList(isWinner);
	}

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public GameManager update() {
		// history를 쌓는다.
		this.stackHistory();
		this.learnFromKnowledge();
		this.reflectIdea();
		return this;
	}

	private void stackHistory() {
		int seconds = TimeUtils.elapsedSeconds();
		UnitCache unitCache = UnitCache.getCurrentCache();

		// TODO unit 정보를 담는다. 판단에 어떤 정보가 더 필요할지에 따라 정보가 추가되어야 한다. 
		HistoryFrame historyFrame = new HistoryFrame(seconds, unitCache);
		historyData.add(historyFrame);
	}

	private void learnFromKnowledge() {
		if (idea == null) {
			idea = new Idea();
			idea.addKnowledge(new InerrableEssentials.FindInitialStrategy());
			idea.addKnowledge(new InerrableEssentials.UsePrebot1BuildStrategy());
		}

		for (Knowledge knowledge : idea.knowledgeList) {
			knowledge.learning();
		}
		idea.arrangeKnowledgeList();
		this.setDummyIdea();
	}

	private void reflectIdea() {
		this.executeBuildActions();
		this.updateSquadComposition();
	}
	
	private void executeBuildActions() {
		for (BuildAction buildAction : idea.buildActionList) {
			if (buildAction.buildCondition()) {
				BuildManager.Instance().buildQueue.queueItem(buildAction.buildOrderItem);
			}
		}
	}
	
	private void updateSquadComposition() {
		for (Squad squad : idea.squadList) {
			CombatManager.Instance().squadData.addOrUpdateSquad(squad);
		}

		List<Squad> removeSquadList = new ArrayList<>();
		for (Squad oldSquad : CombatManager.Instance().squadData.squadMap.values()) {
			
			boolean squadExist = false;
			for (Squad newSquad : idea.squadList) {
				if (newSquad.getName().equals(oldSquad.getName())) {
					squadExist = true;
					break;
				}
			}
			if (!squadExist) {
				removeSquadList.add(oldSquad);
			}
		}
		for (Squad removeSquad : removeSquadList) {
			CombatManager.Instance().squadData.removeSquad(removeSquad);
		}
	}

	private void setDummyIdea() {
		if (idea.strategyName == null) {
			idea.strategyName = "DUMMY'S IDEA";
		}

		BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(Prebot.Game.self());
		if (myBase != null) {
			idea.campPosition = myBase.getPosition();
		}

		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(Prebot.Game.enemy());
		if (enemyBase != null) {
			idea.attackPosition = enemyBase.getPosition();
		} else {
			idea.attackPosition = idea.campPosition;
		}
		
		if (idea.expansionBase == null) {
			idea.expansionBase = InformationManager.Instance().getFirstExpansionLocation(Prebot.Game.self());
		}
	}
	

//	private Strategy testThink(Idea previousIdea) {
//		if (previousIdea == null) {
//			return InitialStrategies.initialStrategy(Prebot.Game.enemy().getRace());
//		}
//		
//		// TOOD 결과로 나온 STRATEGY를 새로운(new) 객체로 리턴
//		// 분석결과(analysed data)
//		Strategy analysedStrategy = analysedStrategy(previousIdea.strategy);
//		
//		// 1. 빌드액션
//		// - 초반빌드큐는 항상 모든 빌드아이템을 리턴하고, 처리된 항목은 제외한다.
//		if (analysedStrategy instanceof InitialStrategy) {
//			analysedStrategy.buildActionList = this.finishedBuildActionRemoved(analysedStrategy.buildActionList);
//			if (analysedStrategy.buildActionList.isEmpty()) {
//				return GeneralStrategies.generalStrategy(Prebot.Game.enemy().getRace());
//			}
//			
//		} else if (analysedStrategy instanceof GeneralStrategy) {
//			// nothing to do
//		}
//		
//		// 2. 스쿼드리스트
//		// TODO 스쿼드 구성
//		
//		return analysedStrategy;
//	}

//	private Strategy analysedStrategy(Strategy previousStrategy) {
//		if (previousStrategy instanceof GeneralStrategy) {
//			return GeneralStrategies.generalStrategy(Prebot.Game.enemy().getRace());
//		} else {
//			return InitialStrategies.initialStrategy(Prebot.Game.enemy().getRace());
//		}
//	}
//	private void executeExpansion() {
//		BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Command_Center, SeedPositionStrategy.NextExpansionLocation, false);
//	}
//
//	private void executeResearch() {
//	}
//
//	private void executeUpgrade() {
//	}
//
//	private void executeTrainStarportUnit() {
//	}
//
//	// 일꾼 계속 추가 생산
//	private void executeTrainWorker() {
//		if (Prebot.Game.self().minerals() < 50) {
//			return;
//		}
//		if (Prebot.Game.self().supplyTotal() - Prebot.Game.self().supplyUsed() < 2) {
//			return;
//		}
//		
//		// 일꾼 수(workerCount) = 현재 일꾼 수 + 생산중인 일꾼 수
//		int workerCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.ALL);
//		
//		// 미네랄 수(selfMineralCount) = 커맨드센터 근처 미네랄 (미완성 커맨드센터는 체력대비 적게 계산)
//		int selfMineralCount = 0;
//		for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.ALL)) {
//			// getMineralsNearDepot 메소드의 미네랄 검색 범위에 따라 중복으로 카운트 될 수 있음
//			int minerals = UnitUtils.getNearMineralsCount(commandCenter);
//			if (minerals > 0) {
//				if (!commandCenter.isCompleted()) {
//					// 완성되지 않은 resource depot이면 체력비율로 미네랄 수를 적게 카운트
//					double completionDegree = commandCenter.getHitPoints() / commandCenter.getType().maxHitPoints();
//					minerals = (int) (minerals * completionDegree);
//				}
//				selfMineralCount += minerals;
//			}
//		}
//		
//		//TODO 아래 계산식은 오류로 보임. selfMineralCount * 2로 변경함. 테스트 필요. 
//		//selfMineralCount * 2 + 8 * PreBot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
//		
//		// 보유 미네랄 수 대비 현재 일꾼이 적으면 일꾼을 생산한다.
//		int maxWorkerCount = Math.min(GameConstant.MAX_WORKER_COUNT, selfMineralCount * 2);
//		if (workerCount < maxWorkerCount) {
//			for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE)) {
//				if (!commandCenter.isTraining() && commandCenter.getHitPoints() > GameConstant.UNIT_PRODUCE_HITPOINT) {
//					BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_SCV, false);
//				}
//			}
//		}
//	}
//	
//	/// 팩토리 및 애드온 건설 (생산과 물량전에 중요한 값. TODO 성욱이 코드를 잘 이해 못해서 대충 해놓음. 테스트 및 수정 필요) 
//	private void executeBuildFactory() {
//		executeBuildNewFactory();
//		executeBuildMachineShop();
//	}
//
//	/// 팩토리 건설
//	private void executeBuildNewFactory() {
//	
//		int commandCenterCount = UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE);
//		int factoryCount = UnitUtils.getUnitCount(UnitType.Terran_Factory, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE);
//		int factoryMaxCount = commandCenterCount * 3; // TODO 조절 필요
//		
//		if (factoryCount >= factoryMaxCount) {
//			return;
//		}
//		
//		// 팩토리가 0개이면 무조건 빌드큐에 넣는다.
//		if (factoryCount == 0) {
//			BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			return;
//		}
//	
//		// 팩토리 증설은 어느정도의 자원이 남는다고 생각될 때 실행된다?
//		int mineralForIncreaseFactory = 200;
//		int gasForIncreaseFactory = 100;
//		
//		boolean factoryFullOperating = true;
//		for (Unit factory : UnitUtils.getUnitList(UnitType.Terran_Factory, UnitFindRange.COMPLETE)) {
//			if (!factory.isCompleted() || !factory.isTraining()) {
//				factoryFullOperating = false;
//				break;
//			}
//		}
//		if (!factoryFullOperating) { // 쉬고 있는 팩토리가 있으면 좀 더 자원이 많다고 생각될 때
//			mineralForIncreaseFactory += (factoryCount - 1) * 40;
//			gasForIncreaseFactory += (factoryCount - 1) * 20;
//		}
//			
//		if (Prebot.Game.self().minerals() > mineralForIncreaseFactory && Prebot.Game.self().gas() > gasForIncreaseFactory) {
//			BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Factory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//		}
//	}
//
//	/// 애드온 부착
//	private void executeBuildMachineShop() {
//		int factoryCount = UnitUtils.getUnitCount(UnitType.Terran_Factory, UnitFindRange.COMPLETE);
//		int facotryWithMachineShopCount = 0;
//		int facotryMachineShopBuildableCount = 0;
//		
//		for (Unit factory : UnitUtils.getUnitList(UnitType.Terran_Factory, UnitFindRange.COMPLETE)) {
//			if (factory.getAddon() != null) {
//				facotryWithMachineShopCount++;
//			} else if (factory.canBuildAddon() && TilePositionUtils.addOnBuildable(factory.getTilePosition())) {
//				facotryMachineShopBuildableCount++;
//			}
//		}
//		
//		if (facotryMachineShopBuildableCount > 0) {
//			int idealFactoryCount = (int) (factoryCount * 0.6);
//			if (Prebot.Game.self().gas() > 300
//					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode) > 0) {
//				idealFactoryCount++;
//			}
//			
//			if (facotryWithMachineShopCount < idealFactoryCount) {
//				if (Prebot.Game.self().minerals() > 50 && Prebot.Game.self().gas() > 50) {
//					BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Machine_Shop, false);
//				}
//			}
//		}
//	}
//
//	/// 테란 운영 상의 건물 건설
//	private void executeBuildSustain() {
//
//		executeBuildRefinery();
//		executeBuildBarracks();
//		executeBuildEngineeringBay();
//
//		executeBuildArmory();
//
//		executeBuildAcademyAndComsat();
//		executeBuildStarportAndScienceFacility();
//	}
//
//	/// 가스(refinery) 건설.
//	/// 조건1) 커맨드센터 근처에 리파이너리가 이미 건설되어 있으면 안된다.
//	/// 조건2) 커맨드센터 근처에 가스통이 있어야 한다.
//	/// 조건3) 커맨드센터에 할당된 일꾼이 일정수 이상이어야 한다.
//	private void executeBuildRefinery() {
//		for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE)) {
//			boolean refineryAlreadyBuilt = false; 
//			for (Unit refinery : UnitUtils.getUnitList(UnitType.Terran_Refinery, UnitFindRange.ALL)) {
//				if (refinery.getDistance(commandCenter.getPosition()) < 300) {
//					refineryAlreadyBuilt = true;
//					break;
//				}
//			}
//			if (refineryAlreadyBuilt) {
//				continue;
//			}
//			
//			TilePosition refineryTilePosition = ConstructionPlaceFinder.Instance().getRefineryPositionNear(commandCenter.getTilePosition());
//			if (refineryTilePosition == null || refineryTilePosition.getDistance(commandCenter.getTilePosition()) * 32 > 300) {
//				continue;
//			}
//			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(commandCenter) < 10) {
//				continue;
//			}
//	
//			BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Refinery, refineryTilePosition, false);
//			return;
//		}
//	}
//
//	private void executeBuildBarracks() {
//		//barrack
//		List<Unit> constructingBarracksList = UnitUtils.getUnitList(UnitType.Terran_Barracks, UnitFindRange.CONSTRUCTION_QUEUE);
//		List<Unit> completeBarracksList = UnitUtils.getUnitList(UnitType.Terran_Barracks, UnitFindRange.COMPLETE);
//		
//		if (constructingBarracksList.isEmpty()) {
//			boolean needNewBarracks = false;
//			if (completeBarracksList.isEmpty()) {
//				needNewBarracks = true;
//				
//			} else if (completeBarracksList.size() == 1) {
//				Unit onlyOneBarracks = completeBarracksList.get(0);
//				int lowHitPoints = UnitType.Terran_Barracks.maxHitPoints() / 3;
//				// 하나있는 1/3미만 에너지의 배럭이 공격당하면
//				if (onlyOneBarracks.isUnderAttack() && onlyOneBarracks.getHitPoints() < lowHitPoints) {
//					needNewBarracks = true;
//				}
//			}
//			if (needNewBarracks) {
//				BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Barracks, false);
//			}
//		}
//	}
//
//	private void executeBuildEngineeringBay() {
//		// engineering
//		List<Unit> constructingEngineeringBayList = UnitUtils.getUnitList(UnitType.Terran_Engineering_Bay, UnitFindRange.CONSTRUCTION_QUEUE);
//		List<Unit> completeEngineeringBayList = UnitUtils.getUnitList(UnitType.Terran_Engineering_Bay, UnitFindRange.COMPLETE);
//
//		if (constructingEngineeringBayList.isEmpty()) {
//			boolean needNewEngineeringBay = false;
//			if (completeEngineeringBayList.isEmpty()) {
//				needNewEngineeringBay = true;
//
//			} else if (completeEngineeringBayList.size() == 1) {
//				Unit onlyOneEngineeringBay = completeEngineeringBayList.get(0);
//				int lowHitPoints = UnitType.Terran_Engineering_Bay.maxHitPoints() / 3;
//				// 하나있는 1/3미만 에너지의 엔베가 공격당하면
//				if (onlyOneEngineeringBay.isUnderAttack() && onlyOneEngineeringBay.getHitPoints() < lowHitPoints) {
//					needNewEngineeringBay = true;
//				}
//			}
//			if (needNewEngineeringBay) {
//				BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Engineering_Bay, false);
//			}
//		}
//	}
//
//	private void executeBuildArmory() {
//		// armory start1
//		boolean hasArmory = UnitUtils.hasUnit(UnitType.Terran_Armory, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE);
//		boolean hasCompleteArmory = UnitUtils.hasUnit(UnitType.Terran_Armory, UnitFindRange.COMPLETE);
//
//		if (!hasArmory) {
//			if (UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE) >= 2 || Prebot.Game.getFrameCount() > 15000) {
//				BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Armory, false);
//			}
//		}
//	}
//
//	private void executeBuildAcademyAndComsat() {
//
//		int activeCommandCenterCount = 0;
//		for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE)) {
//			if (WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(commandCenter) > 8) {
//				activeCommandCenterCount++;
//			}
//		}
//
//		boolean hasAcademy = UnitUtils.hasUnit(UnitType.Terran_Academy, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE);
//		boolean hasCompleteAcademy = UnitUtils.hasUnit(UnitType.Terran_Academy, UnitFindRange.COMPLETE);
//
//		if (hasCompleteAcademy) {
//			for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE)) {
//				if (commandCenter.canBuildAddon() && Prebot.Game.self().allUnitCount(UnitType.Terran_Comsat_Station) < 4) {
//					if (Prebot.Game.self().minerals() >= 50 && Prebot.Game.self().gas() >= 50) {
//						BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Comsat_Station, false);
//						break;
//					}
//				}
//			}
//		} else if (!hasAcademy) {
//			if (activeCommandCenterCount >= 2 || Prebot.Game.getFrameCount() > 15000) {
//				BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Academy, false);
//			}
//		}
//	}
//
//	private void executeBuildStarportAndScienceFacility() {
//		// scienceVessel start
//		boolean hasStarport = UnitUtils.hasUnit(UnitType.Terran_Starport, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE);
//		boolean hasCompleteStarport = UnitUtils.hasUnit(UnitType.Terran_Starport, UnitFindRange.COMPLETE);
//
//		boolean hasScienceFacility = UnitUtils.hasUnit(UnitType.Terran_Science_Facility, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE);
//		boolean hasCompleteScienceFacility = UnitUtils.hasUnit(UnitType.Terran_Science_Facility, UnitFindRange.COMPLETE);
//
//		boolean hasControlTower = UnitUtils.hasUnit(UnitType.Terran_Control_Tower, UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE);
//		boolean hasCompleteControlTower = UnitUtils.hasUnit(UnitType.Terran_Control_Tower, UnitFindRange.COMPLETE);
//		
//		int commandCenterCount = UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE);
//
//		if ((RespondToStrategy.needVessel && commandCenterCount == 2) || commandCenterCount >= 3) {
//			if (!hasStarport) {
//				BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Starport, false);
//			} else {
//				if (hasCompleteStarport) {
//					// 컨트롤 타워가 없다면
////					if (starportUnit != null && starportUnit.canBuildAddon()) {
////						if (PreBot.Broodwar.self().minerals() > 50 && PreBot.Broodwar.self().gas() > 50) {
////							if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower, null)
////									+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null) == 0) {
////								BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Control_Tower, true);
////							}
////						}
////					}
//
//					if (!hasScienceFacility) {
//						BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Science_Facility, false);
//					}
//					if (hasCompleteScienceFacility && hasControlTower) {
//						if (UnitUtils.getUnitCount(UnitType.Terran_Science_Vessel, UnitFindRange.ALL) < RespondToStrategy.maxVessel) {
//							BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Science_Vessel, false);
//						}
//					}
//				}
//			}
//		}
//	}
//
//	// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
//	// SupplyProvider를 추가 건설/생산한다
//	private void executeBuildSupply() {
//		if (Prebot.Game.self().supplyTotal() > 400) {
//			return;
//		}
//
//		// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼
//		// 부족해지면 새 서플라이를 짓도록 한다
//		// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
//		int commandCenterCount = UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE);
//		int factoryCount = UnitUtils.getUnitCount(UnitType.Terran_Factory, UnitFindRange.COMPLETE);
//
//		int factoryOperatingCount = 0;
//		for (Unit factory : UnitUtils.getUnitList(UnitType.Terran_Factory, UnitFindRange.COMPLETE)) {
//			if (factory.isTraining()) {
//				factoryOperatingCount++;
//			}
//		}
//
//		// TODO 아래 계산식 최적화 필요. 일단 의미를 알기 어려워서 코드만으로 효과를 예상하기가 힘들다.
//		int supplyMargin = 5;
//		if (commandCenterCount == 1) {// TODO "이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨." 라고 주석으로 쓰여있었음.
//			if (factoryCount > 0) {
//				supplyMargin = 6 + 4 * factoryCount + factoryOperatingCount * 2;
//			}
//		} else {
//			supplyMargin = 11 + 4 * factoryCount + factoryOperatingCount * 2;
//		}
//
//		// currentSupplyShortage 를 계산한다
//		int currentSupplyShortage = Prebot.Game.self().supplyUsed() + supplyMargin - Prebot.Game.self().supplyTotal();
//
//		if (currentSupplyShortage <= 0) {
//			return;
//		}
//
//		// 생산/건설 중인 Supply를 센다
//		int onBuildingSupplyCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Supply_Depot, null)
//				* UnitType.Terran_Supply_Depot.supplyProvided();
//
//		if (currentSupplyShortage > onBuildingSupplyCount) {
//			// int requiredSupplyCount = (currentSupplyShortage - onBuildingSupplyCount) / TypeUtils.getBasicSupplyProviderUnitType().supplyProvided() + 1;
//			// required.put(TypeUtils.getBasicSupplyProviderUnitType(), requiredSupplyCount);
//			BuildManager.Instance().buildQueue.qHigh(UnitType.Terran_Supply_Depot, SeedPositionStrategy.MainBaseLocation, false);
//		}
//	}
//
//	private void executeBuildTurret() {
//	}
	
	


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
	


	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

	/// 과거 전체 게임 기록을 로딩합니다
	private void loadGameRecordList() {

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
				if (st.hasMoreTokens()) {
					tempGameRecord.mapName = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myName = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myRace = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myWinCount = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.myLoseCount = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.enemyName = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.enemyRace = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.enemyRealRace = st.nextToken();
				}
				if (st.hasMoreTokens()) {
					tempGameRecord.gameFrameCount = Integer.parseInt(st.nextToken());
				}

				gameRecordList.add(tempGameRecord);
			}
		} catch (FileNotFoundException e) {
			System.out.println("loadGameRecord failed. Could not open file :" + gameRecordFileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/// 과거 전체 게임 기록 + 이번 게임 기록을 저장합니다
	private void saveGameRecordList(boolean isWinner) {

		// 이번 게임의 파일 저장은 bwapi-data\write 폴더에 하시면 됩니다.
		// bwapi-data\write 폴더에 저장된 파일은 대회 서버가 다음 경기 때 bwapi-data\read 폴더로 옮겨놓습니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\write\\NoNameBot_GameRecord.dat";

		System.out.println("saveGameRecord to file: " + gameRecordFileName);

		String mapName = Prebot.Game.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = Prebot.Game.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = Prebot.Game.self().getName();
		myName = myName.replace(' ', '_');

		/// 이번 게임에 대한 기록
		GameRecord thisGameRecord = new GameRecord();
		thisGameRecord.mapName = mapName;
		thisGameRecord.myName = myName;
		thisGameRecord.myRace = Prebot.Game.self().getRace().toString();
		thisGameRecord.enemyName = enemyName;
		thisGameRecord.enemyRace = Prebot.Game.enemy().getRace().toString();
		thisGameRecord.enemyRealRace = InformationManager.Instance().enemyRace.toString();
		thisGameRecord.gameFrameCount = Prebot.Game.getFrameCount();
		if (isWinner) {
			thisGameRecord.myWinCount = 1;
			thisGameRecord.myLoseCount = 0;
		} else {
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

		FileUtils.overwriteToFile(gameRecordFileName, ss.toString());
	}

}