package prebot.common.main;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.common.LagObserver;
import prebot.common.MapGrid;
import prebot.common.debug.chat.ChatBot;
import prebot.common.util.CommandUtils;
import prebot.common.util.PlayerUtil;
import prebot.micro.CombatManager;
import prebot.micro.WorkerManager;
import prebot.micro.old.OldCombatManager;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyManager;

/// 실제 봇프로그램의 본체가 되는 class<br>
/// 스타크래프트 경기 도중 발생하는 이벤트들이 적절하게 처리되도록 해당 Manager 객체에게 이벤트를 전달하는 관리자 Controller 역할을 합니다
public class GameCommander {

	/// 디버깅용 플래그 : 어느 Manager 가 에러를 일으키는지 알기위한 플래그
//	private LagObserver logObserver = new LagObserver();
	public boolean scoutFlag = false;

	private static GameCommander instance = new GameCommander();
	private LagObserver logObserver = new LagObserver();
	
	/// static singleton 객체를 리턴합니다
	public static GameCommander Instance() {
		return instance;
	}
	
	/// 경기가 시작될 때 일회적으로 발생하는 이벤트를 처리합니다
	public void onStart() 
	{
		TilePosition startLocation = Prebot.Broodwar.self().getStartLocation();
		if (startLocation == TilePosition.None || startLocation == TilePosition.Unknown) {
			return;
		}
		StrategyManager.Instance().onStart();
		CombatManager.Instance().onStart();
	}

	/// 경기가 종료될 때 일회적으로 발생하는 이벤트를 처리합니다
	public void onEnd(boolean isWinner)
	{
		StrategyManager.Instance().onEnd(isWinner);
	}

	/// 경기 진행 중 매 프레임마다 발생하는 이벤트를 처리합니다
	public void onFrame()
	{

		if (!playableCondition()) {
			return;
		}

		try {
			logObserver.start();

			InformationManager.Instance().updateTimeCheck();
			MapGrid.Instance().updateTimeCheck();
			StrategyManager.Instance().updateTimeCheck();
			
			BuildManager.Instance().updateTimeCheck();
			ConstructionManager.Instance().updateTimeCheck();

//			ScoutManager.Instance().updateTimeCheck();
			WorkerManager.Instance().updateTimeCheck();
//			OldCombatManager.Instance().updateTimeCheck();
			CombatManager.Instance().updateTimeCheck();
			
			logObserver.observe();

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}


	/// 유닛(건물/지상유닛/공중유닛)이 Create 될 때 발생하는 이벤트를 처리합니다
	public void onUnitCreate(Unit unit) { 
		InformationManager.Instance().onUnitCreate(unit);
		
		if(unit.getType() == UnitType.Terran_Command_Center  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			ConstructionPlaceFinder.Instance().setTilesToAvoidCCAddon(unit);
		}
		if((unit.getType() == UnitType.Terran_Factory||unit.getType() == UnitType.Terran_Starport ||unit.getType() == UnitType.Terran_Science_Facility)  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			ConstructionPlaceFinder.Instance().setTilesToAvoidFac(unit);
		}
	}

	///  유닛(건물/지상유닛/공중유닛)이 Destroy 될 때 발생하는 이벤트를 처리합니다
	public void onUnitDestroy(Unit unit) {
		// ResourceDepot 및 Worker 에 대한 처리
		WorkerManager.Instance().onUnitDestroy(unit);

		InformationManager.Instance().onUnitDestroy(unit); 
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
		
		if(unit.getType() == UnitType.Terran_Command_Center  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			
			if(OldCombatManager.Instance().getClosestMineral(unit)!=null){
				unit.setRallyPoint(OldCombatManager.Instance().getClosestMineral(unit));
			}
		}
		
		if(unit.getType() == UnitType.Terran_Barracks  && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			
			if (InformationManager.Instance().enemyRace == Race.Protoss || InformationManager.Instance().enemyRace == Race.Terran) {
				unit.lift();
//				for (Unit myUnit : MyBotModule.Broodwar.self().getUnits())
//				{
					
//					if (myUnit.getType() == UnitType.Terran_Command_Center && myUnit.isCompleted())
//					{
//						unit.setRallyPoint(new Position( (unit.getX()*7 + myUnit.getX())/8, (unit.getY()*7 + myUnit.getY())/8));
//					}
//				}
			}
			else{
				for (Unit myUnit : Prebot.Broodwar.self().getUnits())
				{
					if (myUnit.getType() == UnitType.Terran_Command_Center && myUnit.isCompleted())
					{
						if(OldCombatManager.Instance().getBestPosition(myUnit)==null){
						}else{
							unit.setRallyPoint(OldCombatManager.Instance().getBestPosition(myUnit));
						}
					}
				}
			}
		}
		
		if(unit.getType() == UnitType.Terran_Bunker && unit.getPlayer() == InformationManager.Instance().selfPlayer ){
			for (Unit myUnit : Prebot.Broodwar.self().getUnits())
			{
				if ((myUnit.getType() == UnitType.Terran_Marine) && myUnit.isCompleted())
				{
					CommandUtils.attackMove(myUnit, unit.getPosition());
				}
				if(myUnit.getType() == UnitType.Terran_Barracks && unit.isCompleted()){
					myUnit.setRallyPoint(unit.getPosition());
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

	public boolean getScoutFlag(){ 
		return scoutFlag;
	}

	private boolean playableCondition() {
		return !Prebot.Broodwar.isPaused() && !PlayerUtil.isDisabled(Prebot.Broodwar.self()) && !PlayerUtil.isDisabled(Prebot.Broodwar.enemy());
	}

}