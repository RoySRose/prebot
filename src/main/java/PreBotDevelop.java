

import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Flag.Enum;
import bwta.BWTA;

public class PreBotDevelop extends DefaultBWListener {

	private boolean isGameLostConditionSatisfied = false; /// 자동 패배 체크 결과
	private int gameLostConditionSatisfiedFrame = 0; /// 자동 패배 조건이 시작된 프레임 시점
	private int maxDurationForGameLostCondition = 100; /// 자동 패배 조건이 만족된채 게임을 유지시키는 최대 프레임 수

	private Mirror mirror = new Mirror();
	public static Game Broodwar;
	private GameCommander gameCommander;

	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();	
	}

	@Override
	public void onStart() {
		Broodwar = mirror.getGame();
		gameCommander = GameCommander.Instance();
		if (Broodwar.isReplay()) {
			return;
		}
		if(CommonConfig.Config.EnableCompleteMapInformation){
			Broodwar.enableFlag(Enum.CompleteMapInformation.getValue());
		}

		if(CommonConfig.Config.EnableUserInput){
			Broodwar.enableFlag(Enum.UserInput.getValue());
		}
		Broodwar.setCommandOptimizationLevel(1);
		Broodwar.setLocalSpeed(CommonConfig.Config.SetLocalSpeed);
		Broodwar.setFrameSkip(CommonConfig.Config.SetFrameSkip);

		BWTA.readMap();
		BWTA.analyze();
		BWTA.buildChokeNodes();

		gameCommander.onStart();
	}

	/// 경기가 종료될 때 일회적으로 발생하는 이벤트를 처리합니다
	@Override
	public void onEnd(boolean isWinner) {
		gameCommander.onEnd(isWinner);
		System.exit(0);
	}

	/// 경기 진행 중 매 프레임마다 발생하는 이벤트를 처리합니다
	@Override
	public void onFrame() {
		if (Broodwar.isReplay()) {
			return;
		}

		// Pause 상태에서는 timeStartedAtFrame 를 계속 갱신해서, timeElapsedAtFrame 이 제대로
		// 계산되도록 한다
		try {
//			BigWatch.clear();
			gameCommander.onFrame();
		} catch (Exception e) {
			Broodwar.printf("[Error Stack Trace]");
			Broodwar.sendText(e.toString());
			for (StackTraceElement ste : e.getStackTrace()) {
				Broodwar.sendText(ste.toString());
			}
		}

		// 화면 출력 및 사용자 입력 처리
		// 빌드서버에서는 Dependency가 없는 빌드서버 전용 UXManager 를 실행시킵니다
		PreBotUXManager.Instance().update();

		if (timeToGG()) {
			Broodwar.leaveGame();
		}

	}

	/// 유닛(건물/지상유닛/공중유닛)이 Create 될 때 발생하는 이벤트를 처리합니다
	@Override
	public void onUnitCreate(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitCreate(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Destroy 될 때 발생하는 이벤트를 처리합니다
	@Override
	public void onUnitDestroy(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitDestroy(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Morph 될 때 발생하는 이벤트를 처리합니다<br>
	/// Zerg 종족의 유닛은 건물 건설이나 지상유닛/공중유닛 생산에서 거의 대부분 Morph 형태로 진행됩니다
	@Override
	public void onUnitMorph(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitMorph(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)의 하던 일 (건물 건설, 업그레이드, 지상유닛 훈련 등)이 끝났을 때 발생하는 이벤트를 처리합니다
	@Override
	public void onUnitComplete(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitComplete(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)의 소속 플레이어가 바뀔 때 발생하는 이벤트를 처리합니다<br>
	/// Gas Geyser에 어떤 플레이어가 Refinery 건물을 건설했을 때, Refinery 건물이 파괴되었을 때, Protoss
	/// 종족 Dark Archon 의 Mind Control 에 의해 소속 플레이어가 바뀔 때 발생합니다
	@Override
	public void onUnitRenegade(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitRenegade(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Discover 될 때 발생하는 이벤트를 처리합니다<br>
	/// 아군 유닛이 Create 되었을 때 라든가, 적군 유닛이 Discover 되었을 때 발생합니다
	@Override
	public void onUnitDiscover(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitDiscover(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Evade 될 때 발생하는 이벤트를 처리합니다<br>
	/// 유닛이 Destroy 될 때 발생합니다
	@Override
	public void onUnitEvade(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitEvade(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Show 될 때 발생하는 이벤트를 처리합니다<br>
	/// 아군 유닛이 Create 되었을 때 라든가, 적군 유닛이 Discover 되었을 때 발생합니다
	@Override
	public void onUnitShow(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitShow(unit);
		}
	}

	/// 유닛(건물/지상유닛/공중유닛)이 Hide 될 때 발생하는 이벤트를 처리합니다<br>
	/// 보이던 유닛이 Hide 될 때 발생합니다
	@Override
	public void onUnitHide(Unit unit) {
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitHide(unit);
		}
	}

	/// 핵미사일 발사가 감지되었을 때 발생하는 이벤트를 처리합니다
	@Override
	public void onNukeDetect(Position target) {
		if (!Broodwar.isReplay()) {
			gameCommander.onNukeDetect(target);
		}
	}

	/// 다른 플레이어가 대결을 나갔을 때 발생하는 이벤트를 처리합니다
	@Override
	public void onPlayerLeft(Player player) {
		if (!Broodwar.isReplay()) {
			gameCommander.onPlayerLeft(player);
		}
	}

	/// 게임을 저장할 때 발생하는 이벤트를 처리합니다
	@Override
	public void onSaveGame(String gameName) {
		if (!Broodwar.isReplay()) {
			gameCommander.onSaveGame(gameName);
		}
	}

	/// 텍스트를 입력 후 엔터를 하여 다른 플레이어들에게 텍스트를 전달하려 할 때 발생하는 이벤트를 처리합니다
	@Override
	public void onSendText(String text) {
		gameCommander.onSendText(text);
		Broodwar.sendText(text);
	}

	/// 다른 플레이어로부터 텍스트를 전달받았을 때 발생하는 이벤트를 처리합니다
	@Override
	public void onReceiveText(Player player, String text) {
		Broodwar.printf(player.getName() + " said \"" + text + "\"");

		gameCommander.onReceiveText(player, text);
	}

	/// GG 선언
	private boolean timeToGG() {
		int canProduceBuildingCount = 0;
		int canAttackBuildingCount = 0;
		int canDoSomeThingNonBuildingUnitCount = 0;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType().isBuilding()) {

				// 생산 가능 건물이 하나라도 있으면 게임 지속 가능.
				if (unit.getType().canProduce()) {
					canProduceBuildingCount++;
					break;
				}

				// 공격 가능 건물이 하나라도 있으면 게임 지속 가능. 크립콜로니는 현재는 공격능력을 갖고있지 않지만, 향후
				// 공격능력을 가질 수 있는 건물이므로 카운트에 포함
				if (unit.getType().canAttack() || unit.getType() == UnitType.Zerg_Creep_Colony) {
					canAttackBuildingCount++;
					break;
				}

			} else {
				// 생산 능력을 가진 유닛이나 공격 능력을 가진 유닛, 특수 능력을 가진 유닛이 하나라도 있으면 게임 지속 가능
				// 즉, 라바, 퀸, 디파일러, 싸이언스베쓸, 다크아칸 등은 게임 승리를 이끌 가능성이 조금이라도 있음
				// 치료, 수송, 옵저버 능력만 있는 유닛만 있으면 게임 중지.
				// 즉, 메딕, 드랍쉽, 오버로드, 옵저버, 셔틀만 존재하면, 게임 승리를 이끌 능력이 없음
				if (unit.getType().canAttack() || unit.getType().canProduce() || (unit.getType().isSpellcaster() && unit.getType() != UnitType.Terran_Medic)
						|| unit.getType() == UnitType.Zerg_Larva || unit.getType() == UnitType.Zerg_Egg || unit.getType() == UnitType.Zerg_Lurker_Egg
						|| unit.getType() == UnitType.Zerg_Cocoon) {
					canDoSomeThingNonBuildingUnitCount++;
					break;
				}
			}
		}

		// 자동 패배조건 만족하게 된 프레임 기록
		if (canDoSomeThingNonBuildingUnitCount == 0 && canProduceBuildingCount == 0 && canAttackBuildingCount == 0 && isGameLostConditionSatisfied == false) {
			MyBotModule.Broodwar.sendText("I lost because I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");
			MyBotModule.Broodwar.sendText("GG");
			System.out.println("I lost because I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");

			isGameLostConditionSatisfied = true;
			gameLostConditionSatisfiedFrame = MyBotModule.Broodwar.getFrameCount();
		}
		// 자동 패배조건 벗어나게 되면 리셋
		else if (canDoSomeThingNonBuildingUnitCount != 0 || canProduceBuildingCount != 0 || canAttackBuildingCount != 0) {
			isGameLostConditionSatisfied = false;
		}

		// 자동 패배조건 만족 상황이 일정시간 동안 지속되었으면 게임 패배로 처리
		if (isGameLostConditionSatisfied) {

			MyBotModule.Broodwar.drawTextScreen(250, 100, "I lost because I HAVE NO UNIT TO DEFEAT ENEMY PLAYER");
			MyBotModule.Broodwar.drawTextScreen(250, 115,
					"I will leave game in " + (maxDurationForGameLostCondition - (MyBotModule.Broodwar.getFrameCount() - gameLostConditionSatisfiedFrame)) + " frames");

			if (MyBotModule.Broodwar.getFrameCount() - gameLostConditionSatisfiedFrame >= maxDurationForGameLostCondition) {
				return true;
			}
		}
		return false;
	}

}