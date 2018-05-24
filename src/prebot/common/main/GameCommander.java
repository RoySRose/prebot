
package prebot.common.main;

import java.util.ArrayList;
import java.util.List;

import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import prebot.brain.manager.InformationManager;
import prebot.brain.manager.StrategyManager;
import prebot.build.manager.BuildManager;
import prebot.build.manager.ConstructionManager;
import prebot.build.manager.TestManager;
import prebot.common.chat.ChatBot;
import prebot.common.code.ConfigForDebug.DEBUG;
import prebot.common.util.MapTools;
import prebot.common.util.PlayerUtils;
import prebot.micro.manager.CombatManager;
import prebot.micro.manager.WorkerManager;

/// 실제 봇프로그램의 본체가 되는 class<br>
/// 스타크래프트 경기 도중 발생하는 이벤트들이 적절하게 처리되도록 해당 Manager 객체에게 이벤트를 전달하는 관리자 Controller 역할을 합니다
public class GameCommander {
	
	private static GameCommander instance = new GameCommander();

	public static GameCommander Instance() {
		return instance;
	}

	private List<GameManager> managerList = new ArrayList<>();

	public List<GameManager> getManagers() {
		return managerList;
	}

	/// 경기가 종료될 때 일회적으로 발생하는 이벤트를 처리합니다
	public void onEnd(boolean isWinner) {
		StrategyManager.Instance().onEnd(isWinner);
	}

	public void onStart() {
		TilePosition startLocation = Prebot.Game.self().getStartLocation();
		if (startLocation == TilePosition.None || startLocation == TilePosition.Unknown) {
			return;
		}

		StrategyManager.Instance().onStart();
		CombatManager.Instance().onStart();
		MapTools.init();
		
		InformationManager.Instance().setStopWatchTag("InformationManager");
		StrategyManager.Instance().setStopWatchTag("StrategyManager");
		BuildManager.Instance().setStopWatchTag("BuildManager");
		ConstructionManager.Instance().setStopWatchTag("ConstructionManager");
		WorkerManager.Instance().setStopWatchTag("WorkerManager");
		CombatManager.Instance().setStopWatchTag("CombatManager");

		// 매니저 추가(UX Manager 등에서 사용)
		managerList.add(InformationManager.Instance());
		managerList.add(StrategyManager.Instance());
		managerList.add(BuildManager.Instance());
		managerList.add(ConstructionManager.Instance());
		managerList.add(WorkerManager.Instance());
		managerList.add(CombatManager.Instance());
	}

	/// 경기 진행 중 매 프레임마다 발생하는 이벤트를 처리합니다
	public void onFrame() {
		if (!playableCondition()) {
			return;
		}
		
		// 정보수집 및 판단
		InformationManager.Instance().start().update().end();
		StrategyManager.Instance().start().update().end();
//		MapGrid.Instance().update(); // 사용이 필요할 경우 주석 해제
		
		// 자원 사용
		TestManager.Instance().start().update().end();
		BuildManager.Instance().start().update().end();
		ConstructionManager.Instance().start().update().end();
		
		// 유닛 컨트롤
		WorkerManager.Instance().start().update().end();
		CombatManager.Instance().start().update().end();
//		saveGameLog();
	}

	private boolean playableCondition() {
		return !Prebot.Game.isPaused() && !PlayerUtils.isDisabled(Prebot.Game.self()) && !PlayerUtils.isDisabled(Prebot.Game.enemy());
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Create 될 때 발생하는 이벤트를 처리합니다
	public void onUnitCreate(Unit unit) {
		InformationManager.Instance().onUnitCreate(unit);
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Destroy 될 때 발생하는 이벤트를 처리합니다
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
		WorkerManager.Instance().onUnitMorph(unit);
	}

	/// 유닛(건물/지상유닛/공중유닛)의 소속 플레이어가 바뀔 때 발생하는 이벤트를 처리합니다<br>
	/// Gas Geyser에 어떤 플레이어가 Refinery 건물을 건설했을 때, Refinery 건물이 파괴되었을 때, Protoss 종족 Dark Archon 의 Mind Control 에 의해 소속 플레이어가 바뀔 때 발생합니다
	public void onUnitRenegade(Unit unit) {
		// Vespene_Geyser (가스 광산) 에 누군가가 건설을 했을 경우
		// MyBotModule.Broodwar.sendText("A %s [%p] has renegaded. It is now owned by %s", unit.getType().c_str(), unit,
		// unit.getPlayer().getName().c_str());

		InformationManager.Instance().onUnitRenegade(unit);
	}

	/// 유닛(건물/지상유닛/공중유닛)의 하던 일 (건물 건설, 업그레이드, 지상유닛 훈련 등)이 끝났을 때 발생하는 이벤트를 처리합니다
	public void onUnitComplete(Unit unit) {
		InformationManager.Instance().onUnitComplete(unit);

		// ResourceDepot 및 Worker 에 대한 처리
		WorkerManager.Instance().onUnitComplete(unit);
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Discover 될 때 발생하는 이벤트를 처리합니다<br>
	/// 아군 유닛이 Create 되었을 때 라든가, 적군 유닛이 Discover 되었을 때 발생합니다
	public void onUnitDiscover(Unit unit) {
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Evade 될 때 발생하는 이벤트를 처리합니다<br>
	/// 유닛이 Destroy 될 때 발생합니다
	public void onUnitEvade(Unit unit) {
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Show 될 때 발생하는 이벤트를 처리합니다<br>
	/// 아군 유닛이 Create 되었을 때 라든가, 적군 유닛이 Discover 되었을 때 발생합니다
	public void onUnitShow(Unit unit) {
		InformationManager.Instance().onUnitShow(unit);

		// ResourceDepot 및 Worker 에 대한 처리
		// WorkerManager.Instance().onUnitShow(unit);
	}

	/// 보이던 유닛이 Hide 될 때 발생합니다
	public void onUnitHide(Unit unit) {
		InformationManager.Instance().onUnitHide(unit);
	}

	/// 핵미사일 발사가 감지되었을 때 발생하는 이벤트를 처리합니다
	public void onNukeDetect(Position target) {
	}

	/// 다른 플레이어가 대결을 나갔을 때 발생하는 이벤트를 처리합니다
	public void onPlayerLeft(Player player) {
	}

	/// 게임을 저장할 때 발생하는 이벤트를 처리합니다
	public void onSaveGame(String gameName) {
	}

	/// 텍스트를 입력 후 엔터를 하여 다른 플레이어들에게 텍스트를 전달하려 할 때 발생하는 이벤트를 처리합니다
	public void onSendText(String text) {
		if (DEBUG.isDebugMode) {
			ChatBot.operateChatBot(text);
		}
	}

	/// 다른 플레이어로부터 텍스트를 전달받았을 때 발생하는 이벤트를 처리합니다
	public void onReceiveText(Player player, String text) {
		ChatBot.reply(text);
	}
	
	/// 이번 게임 중간에 상시적으로 로그를 저장합니다
//	private void saveGameLog() {
//
//		// 100 프레임 (5초) 마다 1번씩 로그를 기록합니다
//		// 참가팀 당 용량 제한이 있고, 타임아웃도 있기 때문에 자주 하지 않는 것이 좋습니다
//		// 로그는 봇 개발 시 디버깅 용도로 사용하시는 것이 좋습니다
//		if (PreBot.Broodwar.getFrameCount() % 100 != 0) {
//			return;
//		}
//
//		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
//		String gameLogFileName = "bwapi-data\\write\\NoNameBot_LastGameLog.dat";
//
//		String mapName = PreBot.Broodwar.mapFileName();
//		mapName = mapName.replace(' ', '_');
//		String enemyName = PreBot.Broodwar.enemy().getName();
//		enemyName = enemyName.replace(' ', '_');
//		String myName = PreBot.Broodwar.self().getName();
//		myName = myName.replace(' ', '_');
//
//		StringBuilder ss = new StringBuilder();
//		ss.append(mapName + " ");
//		ss.append(myName + " ");
//		ss.append(PreBot.Broodwar.self().getRace().toString() + " ");
//		ss.append(enemyName + " ");
//		ss.append(InformationManager.Instance().enemyRace.toString() + " ");
//		ss.append(PreBot.Broodwar.getFrameCount() + " ");
//		ss.append(PreBot.Broodwar.self().supplyUsed() + " ");
//		ss.append(PreBot.Broodwar.self().supplyTotal() + "\n");
//
//		FileUtil.appendTextToFile(gameLogFileName, ss.toString());
//	}
}