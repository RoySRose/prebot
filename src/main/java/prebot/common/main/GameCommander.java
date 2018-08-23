package prebot.common.main;

import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.build.provider.BuildQueueProvider;
import prebot.common.MapGrid;
import prebot.common.debug.BigWatch;
import prebot.common.debug.chat.ChatBot;
import prebot.common.util.PlayerUtils;
import prebot.common.util.UnitUtils;
import prebot.macro.AttackDecisionMaker;
import prebot.micro.CombatManager;
import prebot.micro.WorkerManager;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyManager;

/// 실제 봇프로그램의 본체가 되는 class<br>
/// 스타크래프트 경기 도중 발생하는 이벤트들이 적절하게 처리되도록 해당 Manager 객체에게 이벤트를 전달하는 관리자 Controller 역할을 합니다
public class GameCommander {

	private static GameCommander instance = new GameCommander();
    private UnitBalancer unitBalancer = new UnitBalancer(); // for debugging

	/// static singleton 객체를 리턴합니다
	public static GameCommander Instance() {
		return instance;
	}
	boolean once = false;
	/// 경기가 시작될 때 일회적으로 발생하는 이벤트를 처리합니다
	public void onStart() 
	{
		System.out.println("onStart() started");
		TilePosition startLocation = MyBotModule.Broodwar.self().getStartLocation();
		if (startLocation == TilePosition.None || startLocation == TilePosition.Unknown) {
			return;
		}

        BlockingEntrance.Instance().onStart();
		ConstructionPlaceFinder.Instance().setTilesToAvoidSupply();
		ConstructionPlaceFinder.Instance().setTilesToAvoidBaseLocation();
		InitialBuildProvider.Instance().onStart();
		StrategyManager.Instance().onStart();
        AttackDecisionMaker.Instance().onStart();
		CombatManager.Instance().onStart();
		System.out.println("onStart() finished");
	}

	/// 경기가 종료될 때 일회적으로 발생하는 이벤트를 처리합니다
	public void onEnd(boolean isWinner)
	{
		StrategyManager.Instance().onEnd(isWinner);
	}

	public void onFrame()
	{

		if (!playableCondition()) {
			return;
		}

		try {
			BigWatch.start("... GAME COMMANDER ...");
			
			InformationManager.Instance().updateTimeCheck();
			MapGrid.Instance().updateTimeCheck();
			StrategyManager.Instance().updateTimeCheck();
			
			// progressive & complete => initial end
			InitialBuildProvider.Instance().updateInitialBuild();
			BuildQueueProvider.Instance().updateTimeCheck();
			BuildManager.Instance().updateTimeCheck();
			
			ConstructionManager.Instance().updateTimeCheck();

			unitBalancer.update();
			
			WorkerManager.Instance().updateTimeCheck();
			CombatManager.Instance().updateTimeCheck();
			AttackDecisionMaker.Instance().updateTimeCheck();
			
			BigWatch.record("... GAME COMMANDER ...");

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}


	/// 유닛(건물/지상유닛/공중유닛)이 Create 될 때 발생하는 이벤트를 처리합니다
	public void onUnitCreate(Unit unit) { 
		InformationManager.Instance().onUnitCreate(unit);
		
//		if (unit.getPlayer() == InformationManager.Instance().selfPlayer) {
//			if (unit.getType() == UnitType.Terran_Command_Center) {
//				ConstructionPlaceFinder.Instance().setTilesToAvoidAddonBuilding(unit);
//			}
//			if ((unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport || unit.getType() == UnitType.Terran_Science_Facility)) {
			if ((unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport)) {
				ConstructionPlaceFinder.Instance().setTilesToAvoidAddonBuilding(unit);
			}
//			if ((unit.getType() == UnitType.Terran_Supply_Depot || unit.getType() == UnitType.Terran_Academy || unit.getType() == UnitType.Terran_Armory)) {
//				ConstructionPlaceFinder.Instance().setTilesToAvoidSupply(unit);
//			}
//		}
	}

	///  유닛(건물/지상유닛/공중유닛)이 Destroy 될 때 발생하는 이벤트를 처리합니다
	public void onUnitDestroy(Unit unit) {
		// ResourceDepot 및 Worker 에 대한 처리
		WorkerManager.Instance().onUnitDestroy(unit);
		InformationManager.Instance().onUnitDestroy(unit); 
//		if ((unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport || unit.getType() == UnitType.Terran_Science_Facility)) {
		if (unit.getType() == UnitType.Terran_Factory || unit.getType() == UnitType.Terran_Starport
			||unit.getType() == UnitType.Terran_Machine_Shop || unit.getType() == UnitType.Terran_Control_Tower) {
			ConstructionPlaceFinder.Instance().setTilesToAvoidAddonBuildingFree(unit);
		}
//		if (unit.getType() == UnitType.Terran_Machine_Shop || unit.getType() == UnitType.Terran_Control_Tower) { 
////				|| unit.getType() == UnitType.Terran_Covert_Ops || unit.getType() == UnitType.Terran_Physics_Lab) {
//			ConstructionPlaceFinder.Instance().setTilesToAvoidAddonBuildingFree(unit);
//		}
	}
	
	/// 유닛(건물/지상유닛/공중유닛)이 Morph 될 때 발생하는 이벤트를 처리합니다<br>
	/// Zerg 종족의 유닛은 건물 건설이나 지상유닛/공중유닛 생산에서 거의 대부분 Morph 형태로 진행됩니다
	public void onUnitMorph(Unit unit) { 
		InformationManager.Instance().onUnitMorph(unit);

		// Zerg 종족 Worker 의 Morph 에 대한 처리
		//WorkerManager.Instance().onUnitMorph(unit);
	}

	/// 유닛(건물/지상유닛/공중유닛)의 소속 플레이어가 바뀔 때 발생하는 이벤트를 처리합니다<br>
	/// Gas Geyser에 어떤 플레이어가 Refinery 건물을 건설했을 때, Refinery 건물이 파괴되었을 때, Protoss 종족 Dark Archon 의 Mind Control 에 의해 소속 플레이어가 바뀔 때 발생합니다
	public void onUnitRenegade(Unit unit) {
		// Vespene_Geyser (가스 광산) 에 누군가가 건설을 했을 경우
		//MyBotModule.Broodwar.sendText("A %s [%p] has renegaded. It is now owned by %s", unit.getType().c_str(), unit, unit.getPlayer().getName().c_str());

		InformationManager.Instance().onUnitRenegade(unit);
	}
	
	// 일꾼 탄생/파괴 등에 대한 업데이트 로직 버그 수정 : onUnitShow 가 아니라 onUnitComplete 에서 처리하도록 수정
	/// 유닛(건물/지상유닛/공중유닛)의 하던 일 (건물 건설, 업그레이드, 지상유닛 훈련 등)이 끝났을 때 발생하는 이벤트를 처리합니다
	public void onUnitComplete(Unit unit)
	{
		InformationManager.Instance().onUnitComplete(unit);
		// ResourceDepot 및 Worker 에 대한 처리
		WorkerManager.Instance().onUnitComplete(unit);
		
		if (unit.getPlayer() == InformationManager.Instance().selfPlayer) {
			if (unit.getType() == UnitType.Terran_Command_Center) {
				Unit closestMineral = UnitUtils.getClosestUnitToPosition(MyBotModule.Broodwar.getMinerals(), unit.getPosition());
				if (closestMineral != null) {
					unit.setRallyPoint(closestMineral);
				}
			}
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Discover 될 때 발생하는 이벤트를 처리합니다<br>
	/// 아군 유닛이 Create 되었을 때 라든가, 적군 유닛이 Discover 되었을 때 발생합니다
	public void onUnitDiscover(Unit unit) {
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Evade 될 때 발생하는 이벤트를 처리합니다<br>
	/// 유닛이 Destroy 될 때 발생합니다
	public void onUnitEvade(Unit unit) {
	}	

	// 일꾼 탄생/파괴 등에 대한 업데이트 로직 버그 수정 : onUnitShow 가 아니라 onUnitComplete 에서 처리하도록 수정
	/// 유닛(건물/지상유닛/공중유닛)이 Show 될 때 발생하는 이벤트를 처리합니다<br>
	/// 아군 유닛이 Create 되었을 때 라든가, 적군 유닛이 Discover 되었을 때 발생합니다
	public void onUnitShow(Unit unit) { 
		InformationManager.Instance().onUnitShow(unit); 
		
		// ResourceDepot 및 Worker 에 대한 처리
		//WorkerManager.Instance().onUnitShow(unit);
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Hide 될 때 발생하는 이벤트를 처리합니다<br>
	/// 보이던 유닛이 Hide 될 때 발생합니다
	public void onUnitHide(Unit unit) {
		InformationManager.Instance().onUnitHide(unit); 
	}

	// onNukeDetect, onPlayerLeft, onSaveGame 이벤트를 처리할 수 있도록 메소드 추가

	/// 핵미사일 발사가 감지되었을 때 발생하는 이벤트를 처리합니다
	public void onNukeDetect(Position target){
	}

	/// 다른 플레이어가 대결을 나갔을 때 발생하는 이벤트를 처리합니다
	public void onPlayerLeft(Player player){
	}

	/// 게임을 저장할 때 발생하는 이벤트를 처리합니다
	public void onSaveGame(String gameName){
	}		

	/// 텍스트를 입력 후 엔터를 하여 다른 플레이어들에게 텍스트를 전달하려 할 때 발생하는 이벤트를 처리합니다
	public void onSendText(String text) {
		ChatBot.operateChatBot(text);
	}

	/// 다른 플레이어로부터 텍스트를 전달받았을 때 발생하는 이벤트를 처리합니다
	public void onReceiveText(Player player, String text){
	}

	private boolean playableCondition() {
		return !MyBotModule.Broodwar.isPaused() && !PlayerUtils.isDisabled(MyBotModule.Broodwar.self()) && !PlayerUtils.isDisabled(MyBotModule.Broodwar.enemy());
	}

}