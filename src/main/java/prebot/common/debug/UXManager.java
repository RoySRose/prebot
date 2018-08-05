package prebot.common.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Bullet;
import bwapi.BulletType;
import bwapi.Color;
import bwapi.Force;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Polygon;
import bwta.Region;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.InitialBuildProvider.AdaptStrategyStatus;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.build.prebot1.ConstructionTask;
import prebot.build.provider.BuildQueueProvider;
import prebot.common.LagObserver;
import prebot.common.MapGrid;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.constant.CommonConfig.UxConfig;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.CombatManager;
import prebot.micro.Decision;
import prebot.micro.Minerals;
import prebot.micro.WorkerData;
import prebot.micro.WorkerManager;
import prebot.micro.squad.Squad;
import prebot.micro.squad.WatcherSquad;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.StrategyManager;
import prebot.strategy.TravelSite;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceTeam;
import prebot.strategy.manage.ClueManager;
import prebot.strategy.manage.EnemyBuildTimer;
import prebot.strategy.manage.StrategyAnalyseManager;
import prebot.strategy.manage.VultureTravelManager;

/// 봇 프로그램 개발의 편의성 향상을 위해 게임 화면에 추가 정보들을 표시하는 class<br>
/// 여러 Manager 들로부터 정보를 조회하여 Screen 혹은 Map 에 정보를 표시합니다
public class UXManager {
	
	private int uxOption = 1;

	public void setUxOption(int uxOption) {
		this.uxOption = uxOption;
	}
	
	private boolean hasSavedBWTAInfo = false;
	private int[][] blue = null;
	private int[][] cyan = null;
	private int[][] orange = null;
	private int[][] purple = null;
	private List<Position> yellow = new ArrayList<Position>();
	private List<Position> green1 = new ArrayList<Position>();
	private List<Position> green2 = new ArrayList<Position>();
	private List<Position> red1 = new ArrayList<Position>();
	private List<Position> red2 = new ArrayList<Position>();
	
	private final int dotRadius = 2;
	public Unit leader = null;
	
	private String bulletTypeName = "";
	private String tempUnitName = "";
	
	private Map<Integer, Decision> decisionListForUx = new HashMap<>();
	private static UXManager instance = new UXManager();
	
	/// static singleton 객체를 리턴합니다
	public static UXManager Instance() {
		return instance;
	}
	
	/// 경기가 시작될 때 일회적으로 추가 정보를 출력합니다
	public void onStart() {
	}

	/// 경기 진행 중 매 프레임마다 추가 정보를 출력하고 사용자 입력을 처리합니다
	public void update() {
		if (uxOption == 0) {
			drawDebugginUxMenu();
			
		} else if (uxOption == 1) {
			drawGameInformationOnScreen(5, 5);
//			drawUnitStatisticsOnScreen1(400, 15);
			// drawUnitStatisticsOnScreen2(370, 50);
			drawBWTAResultOnMap();
//			drawMapGrid();
			// 빌드오더큐 : 빌드 실행 전
			drawBuildOrderQueueOnScreen(500, 50);
			// 빌드 실행 상황 : 건물 건설, 유닛 생산, 업그레이드, 리서치
			drawBuildStatusOnScreen(370, 50);
			// 건물 건설 큐. 건물 건설 상황
			drawConstructionQueueOnScreenAndMap(200, 150);
			// 건물이 건설될 위치
			// 건물 건설 장소 예약 지점
			drawReservedBuildingTilesOnMap();
			// 건물 건설 불가 구역 (미네랄/가스/베이스 사이)
			drawTilesToAvoidOnMap();
//			drawLeaderUnitOnMap();
			// drawUnitExtendedInformationOnMap();
			// 각 일꾼들의 임무 상황
//			drawWorkerStateOnScreen(260, 60);
			// 베이스캠프당 일꾼 수
//			drawWorkerCountOnMap();
			// 일꾼 자원채취 임무 상황
			drawWorkerMiningStatusOnMap();
			// 정찰
//			drawScoutInformation(220, 330);
			// 공격
			drawUnitTargetOnMap();
			// 미사일, 럴커의 보이지않는 공격등을 표시
			// drawBulletsOnMap();
			drawnextPoints();
			// draw tile position of mouse cursor
			int mouseX = Prebot.Broodwar.getMousePosition().getX() + Prebot.Broodwar.getScreenPosition().getX();
			int mouseY = Prebot.Broodwar.getMousePosition().getY() + Prebot.Broodwar.getScreenPosition().getY();
			Prebot.Broodwar.drawTextMap(mouseX + 20, mouseY, "(" + (int) (mouseX / UxConfig.TILE_SIZE) + ", " + (int) (mouseY / UxConfig.TILE_SIZE) + ")");
			Prebot.Broodwar.drawTextMap(mouseX + 20, mouseY + 10, "(" + (int) (mouseX) + ", " + (int) (mouseY) + ")");
			//미네랄PATH
		} else if (uxOption == 2) {
			drawStrategy();
			
		} else if (uxOption == 3) {
			drawEnemyBuildTimer();
			
		} else if (uxOption == 4) {
			drawSquadInfoOnMap(20, 30);
			drawManagerTimeSpent(500, 220);
			drawDecision();
			drawEnemyAirDefenseRange();
			drawAirForceInformation();
			drawVulturePolicy();
			
		} else if (uxOption == 5) {
			drawEnemyBaseToBaseTime();
			
		} else if (uxOption == 6) {
			drawBigWatch();
			drawManagerTimeSpent(500, 220);
		}

		
		drawUnitIdOnMap();
		drawPositionInformation();
		drawTimer();
		drawPathData();
		drawSquadUnitTagMap();
		
		clearDecisionListForUx();
	}

	private void drawDecision() {
		for (Integer unitId : decisionListForUx.keySet()) {
			Unit unit = Prebot.Broodwar.getUnit(unitId);
			Decision decision = decisionListForUx.get(unitId);
			Prebot.Broodwar.drawTextMap(unit.getPosition(), UxColor.CHAR_YELLOW + decision.toString());
			if (decision.eui != null) {
				Prebot.Broodwar.drawLineMap(unit.getPosition(), decision.eui.getLastPosition(), Color.Yellow);
			}
		}
	}
	
	public void addDecisionListForUx(Unit unit, Decision decision) {
		decisionListForUx.put(unit.getID(), decision);
	}
	
	public void clearDecisionListForUx() {
		decisionListForUx.clear();
	}

	private void drawTimer() {
		char color = UxColor.CHAR_WHITE;
		if (StrategyIdea.initiated) {
			color = UxColor.CHAR_RED;
		}
		Prebot.Broodwar.drawTextScreen(170, 353, color + StrategyIdea.mainSquadMode.toString() + ": " + TimeUtils.framesToTimeString(TimeUtils.elapsedFrames()) + "(" + TimeUtils.elapsedFrames() + ")");
	}
	
	private void drawEnemyBuildTimer() {
		
		Map<UnitType, Integer> buildTimeExpectMap = EnemyBuildTimer.Instance().buildTimeExpectMap;
		Map<UnitType, Integer> buildTimeMinimumMap = EnemyBuildTimer.Instance().buildTimeMinimumMap;
		Set<UnitType> buildTimeCertain = EnemyBuildTimer.Instance().buildTimeCertain;

		int y = 20;
		Prebot.Broodwar.drawTextScreen(20, y += 15, "engine Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.engineeringBayBuildStartFrame));
		Prebot.Broodwar.drawTextScreen(20, y += 15, "turret Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.turretBuildStartFrame));
		Prebot.Broodwar.drawTextScreen(20, y += 15, "turret Need  Frame : " + TimeUtils.framesToTimeString(StrategyIdea.turretNeedFrame));
		y += 15;
		
		Prebot.Broodwar.drawTextScreen(20, y += 15, "academy Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.academyFrame));
		Prebot.Broodwar.drawTextScreen(20, y += 15, "comsat Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.academyFrame + UnitType.Terran_Academy.buildTime()));
		y += 15;
		
		Prebot.Broodwar.drawTextScreen(20, y += 15, "darkTemplarInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().darkTemplarInMyBaseFrame));
		Prebot.Broodwar.drawTextScreen(20, y += 15, "reaverInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().reaverInMyBaseFrame));
		Prebot.Broodwar.drawTextScreen(20, y += 15, "mutaliskInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().mutaliskInMyBaseFrame));
		Prebot.Broodwar.drawTextScreen(20, y += 15, "lurkerInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().lurkerInMyBaseFrame));
		y += 15;

		for (UnitType unitType : buildTimeExpectMap.keySet()) {
			Integer buildTimeExpect = buildTimeExpectMap.get(unitType);
			if (buildTimeExpect != null && buildTimeExpect != CommonCode.UNKNOWN) {
				String expect = TimeUtils.framesToTimeString(buildTimeExpect);
				String minimum = "";
				Integer buildMinimum = buildTimeMinimumMap.get(unitType);
				if (buildMinimum != null && buildMinimum != CommonCode.UNKNOWN) {
					minimum = TimeUtils.framesToTimeString(buildMinimum);
				}
				
				Prebot.Broodwar.drawTextScreen(20, y += 15, unitType + " : " + expect + " - min: " + minimum + " (" + buildTimeCertain.contains(unitType) + ")");
			}
		}
	}

	private void drawDebugginUxMenu() {
		Prebot.Broodwar.drawTextScreen(20, 20, "1. Default Information");
		Prebot.Broodwar.drawTextScreen(20, 35, "2. Strategy Information");
		Prebot.Broodwar.drawTextScreen(20, 50, "3. Position Finder Test");
		Prebot.Broodwar.drawTextScreen(20, 65, "4. Air Micro Test");
		Prebot.Broodwar.drawTextScreen(20, 80, "5. Unit Bast To Base");
	}

	// 게임 개요 정보를 Screen 에 표시합니다
	public void drawGameInformationOnScreen(int x, int y) {
//		MyBotModule.Broodwar.drawTextScreen(x, y, white + "Players : ");
//		MyBotModule.Broodwar.drawTextScreen(x + 50, y, MyBotModule.Broodwar.self().getTextColor() + MyBotModule.Broodwar.self().getName() + "(" + InformationManager.Instance().selfRace + ") " + white + " vs.  " + 
//				InformationManager.Instance().enemyPlayer.getTextColor() + InformationManager.Instance().enemyPlayer.getName() + "(" + InformationManager.Instance().enemyRace + ")");
//		y += 12;
//
//		MyBotModule.Broodwar.drawTextScreen(x, y, white + "Map : ");
//		MyBotModule.Broodwar.drawTextScreen(x + 50, y, white + MyBotModule.Broodwar.mapFileName() + " (" + MyBotModule.Broodwar.mapWidth() + " x " +  MyBotModule.Broodwar.mapHeight() + " size)");
//		MyBotModule.Broodwar.setTextSize();
//		y += 12;

//		MyBotModule.Broodwar.drawTextScreen(x, y, white + "Time : ");
//		MyBotModule.Broodwar.drawTextScreen(x + 50, y, "" + white + MyBotModule.Broodwar.getFrameCount());
//		MyBotModule.Broodwar.drawTextScreen(x + 90, y, "" + white + (int)(MyBotModule.Broodwar.getFrameCount() / (23.8 * 60)) + ":" + (int)((int)(MyBotModule.Broodwar.getFrameCount() / 23.8) % 60));
//		y += 11;
		
		
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Current Strategy : ");
		Prebot.Broodwar.drawTextScreen(x + 100, y, "" + UxColor.CHAR_WHITE + StrategyIdea.currentStrategy.name());
		y += 11;
		
		String history = "";
		for (int i = StrategyIdea.strategyHistory.size() - 1; i >= 0; i--) {
			if (i == StrategyIdea.strategyHistory.size() - 3) {
				history = "... "+ history;
				break;
			} else {
				history = StrategyIdea.strategyHistory.get(i).name() + " -> "+ history;
			}
		}
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Strategy History : ");
		Prebot.Broodwar.drawTextScreen(x + 100, y, "" + UxColor.CHAR_WHITE + history);
		y += 11;
		
		int vultureCount = UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Vulture);
		int tankCount = UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
		int goliathCount = UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Goliath);
		Prebot.Broodwar.drawTextScreen(x + 100, y + 5, UxColor.CHAR_TEAL + "" + vultureCount + "      " + tankCount + "        " + goliathCount);
		Prebot.Broodwar.drawTextScreen(x, y, "" + UxColor.CHAR_WHITE + StrategyIdea.factoryRatio);
		y += 11;
		
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Wraith Count : ");
		Prebot.Broodwar.drawTextScreen(x + 75, y, "" + UxColor.CHAR_WHITE + StrategyIdea.wraithCount);
		y += 11;

		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_RED + "MYKillScore : ");
		Prebot.Broodwar.drawTextScreen(x + 70, y, "" + UxColor.CHAR_RED + Prebot.Broodwar.self().getKillScore());
		y += 11;
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_RED + "MYRazingScore : ");
		Prebot.Broodwar.drawTextScreen(x + 85, y, "" + UxColor.CHAR_RED + Prebot.Broodwar.self().getRazingScore());
		y += 11;
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_PURPLE + "EnemyKillScore : ");
		Prebot.Broodwar.drawTextScreen(x + 85, y, "" + UxColor.CHAR_PURPLE + Prebot.Broodwar.enemy().getKillScore());
		y += 11;
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_PURPLE + "EnemyRazingScore : ");
		Prebot.Broodwar.drawTextScreen(x + 100, y, "" + UxColor.CHAR_PURPLE + Prebot.Broodwar.enemy().getRazingScore());
		y += 11;
	}

	/// APM (Action Per Minute) 숫자를 Screen 에 표시합니다
	public void drawAPM(int x, int y) {
		int bwapiAPM = Prebot.Broodwar.getAPM();
		Prebot.Broodwar.drawTextScreen(x, y, "APM : " + bwapiAPM);
	}

	/// Players 정보를 Screen 에 표시합니다
	public void drawPlayers() {
		for (Player p : Prebot.Broodwar.getPlayers()) {
			Prebot.Broodwar.sendText("Player [" + p.getID() + "]: " + p.getName() + " is in force: " + p.getForce().getName());
		}
	}

	/// Player 들의 팀 (Force) 들의 정보를 Screen 에 표시합니다
	public void drawForces() {
		for (Force f :  Prebot.Broodwar.getForces()) {
			Prebot.Broodwar.sendText("Force " + f.getName() + " has the following players:");
			for (Player p : f.getPlayers()) {
				Prebot.Broodwar.sendText("  - Player [" + p.getID() + "]: " + p.getName());
			}
		}
	}

	/// Unit 의 HitPoint 등 추가 정보를 Map 에 표시합니다
	public void drawUnitExtendedInformationOnMap() {
		int verticalOffset = -10;

		if(InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer) != null)
		{
			// draw enemy units
			Iterator<Integer> it = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
			
			// C++ : for (final Unit kv : InformationManager.Instance().getUnitData(MyBotModule.game.enemy()).getUnits())
			while(it.hasNext())
			{
				final UnitInfo ui= InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
	
				UnitType type = ui.getType();
				int hitPoints = ui.getLastHealth();
				int shields = ui.getLastShields();
	
				Position pos = ui.getLastPosition();
	
				int left = pos.getX() - type.dimensionLeft();
				int right = pos.getX() + type.dimensionRight();
				int top = pos.getY() - type.dimensionUp();
				int bottom = pos.getY() + type.dimensionDown();
	
				// 적 유닛이면 주위에 박스 표시
				if (!Prebot.Broodwar.isVisible(ui.getLastPosition().toTilePosition())) {
					Prebot.Broodwar.drawBoxMap(new Position(left, top), new Position(right, bottom), Color.Grey, false);
					Prebot.Broodwar.drawTextMap(new Position(left + 3, top + 4), ui.getType().toString());
				}
	
				// 유닛의 HitPoint 남아있는 비율 표시
				if (!type.isResourceContainer() && type.maxHitPoints() > 0)
				{
					double hpRatio = (double)hitPoints / (double)type.maxHitPoints();
	
					Color hpColor = Color.Green;
					if (hpRatio < 0.66) hpColor = Color.Orange;
					if (hpRatio < 0.33) hpColor = Color.Red;
	
					int ratioRight = left + (int)((right - left) * hpRatio);
					int hpTop = top + verticalOffset;
					int hpBottom = top + 4 + verticalOffset;
	
					Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
					Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), hpColor, true);
					Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);
	
					int ticWidth = 3;
	
					for (int i = left; i < right - 1; i += ticWidth) {
						Prebot.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
					}
				}
	
				// 유닛의 Shield 남아있는 비율 표시
				if (!type.isResourceContainer() && type.maxShields() > 0) {
					double shieldRatio = (double)shields / (double)type.maxShields();
	
					int ratioRight = left + (int)((right - left) * shieldRatio);
					int hpTop = top - 3 + verticalOffset;
					int hpBottom = top + 1 + verticalOffset;
	
					Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
					Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), Color.Blue, true);
					Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);
	
					int ticWidth = 3;
	
					for (int i = left; i < right - 1; i += ticWidth) {
						Prebot.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
					}
				}
			}
		}

		// draw neutral units and our units
		for (Unit unit : Prebot.Broodwar.getAllUnits()) {
			if (unit.getPlayer() == InformationManager.Instance().enemyPlayer) {
				continue;
			}

			final Position pos = unit.getPosition();

			int left = pos.getX() - unit.getType().dimensionLeft();
			int right = pos.getX() + unit.getType().dimensionRight();
			int top = pos.getY() - unit.getType().dimensionUp();
			int bottom = pos.getY() + unit.getType().dimensionDown();

			//MyBotModule.game.drawBoxMap(BWAPI.Position(left, top), BWAPI.Position(right, bottom), Color.Grey, false);

			// 유닛의 HitPoint 남아있는 비율 표시
			if (!unit.getType().isResourceContainer() && unit.getType().maxHitPoints() > 0) {
				double hpRatio = (double)unit.getHitPoints() / (double)unit.getType().maxHitPoints();

				Color hpColor = Color.Green;
				if (hpRatio < 0.66) hpColor = Color.Orange;
				if (hpRatio < 0.33) hpColor = Color.Red;

				int ratioRight = left + (int)((right - left) * hpRatio);
				int hpTop = top + verticalOffset;
				int hpBottom = top + 4 + verticalOffset;

				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), hpColor, true);
				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), hpColor.Black, false);

				int ticWidth = 3;

				for (int i = left; i < right - 1; i += ticWidth) {
					Prebot.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
				}
			}

			// 유닛의 Shield 남아있는 비율 표시
			if (!unit.getType().isResourceContainer() && unit.getType().maxShields() > 0) {
				double shieldRatio = (double)unit.getShields() / (double)unit.getType().maxShields();

				int ratioRight = left + (int)((right - left) * shieldRatio);
				int hpTop = top - 3 + verticalOffset;
				int hpBottom = top + 1 + verticalOffset;

				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), Color.Blue, true);
				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);

				int ticWidth = 3;

				for (int i = left; i < right - 1; i += ticWidth) {
					Prebot.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
				}
			}

			// Mineral / Gas 가 얼마나 남아있는가
			if (unit.getType().isResourceContainer() && unit.getInitialResources() > 0) {
				double mineralRatio = (double)unit.getResources() / (double)unit.getInitialResources();

				int ratioRight = left + (int)((right - left) * mineralRatio);
				int hpTop = top + verticalOffset;
				int hpBottom = top + 4 + verticalOffset;

				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), Color.Cyan, true);
				Prebot.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);

				int ticWidth = 3;

				for (int i = left; i < right - 1; i += ticWidth) {
					Prebot.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
				}
			}
		}
	}

	/// UnitType 별 통계 정보를 Screen 에 표시합니다
	public void drawUnitStatisticsOnScreen1(int x, int y) {
		int currentY = y;

		// 아군이 입은 피해 누적값
		Prebot.Broodwar.drawTextScreen(x, currentY, UxColor.CHAR_WHITE + " Self Loss:" + UxColor.CHAR_WHITE + " Minerals: " + UxColor.CHAR_BROWN + InformationManager.Instance().getUnitData(Prebot.Broodwar.self()).getMineralsLost() + UxColor.CHAR_WHITE + " Gas: " + UxColor.CHAR_RED + InformationManager.Instance().getUnitData(Prebot.Broodwar.self()).getGasLost());
		currentY += 10;

		// 아군 모든 유닛 숫자 합계
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " allUnitCount: " + MyBotModule.Broodwar.self().allUnitCount(UnitType.AllUnits));
		//currentY += 10;

		// 아군 건설/훈련 완료한 유닛 숫자 합계
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " completedUnitCount: " + MyBotModule.Broodwar.self().completedUnitCount(UnitType.AllUnits));
		//currentY += 10;

		// 아군 건설/훈련중인 유닛 숫자 합계
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " incompleteUnitCount: " + MyBotModule.Broodwar.self().incompleteUnitCount(UnitType.AllUnits));
		//currentY += 10;

		// 아군 유닛 파괴/사망 숫자 누적값
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " deadUnitCount: " + MyBotModule.Broodwar.self().deadUnitCount(UnitType.AllUnits));
		//currentY += 10;

		// 상대방 유닛을 파괴/사망 시킨 숫자 누적값
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " killedUnitCount: " + MyBotModule.Broodwar.self().killedUnitCount(UnitType.AllUnits));
		//currentY += 10;

		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " UnitScore: " + MyBotModule.Broodwar.self().getUnitScore());
		//currentY += 10;
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " RazingScore: " + MyBotModule.Broodwar.self().getRazingScore());
		//currentY += 10;
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " BuildingScore: " + MyBotModule.Broodwar.self().getBuildingScore());
		//currentY += 10;
		//MyBotModule.Broodwar.drawTextScreen(x, currentY,  white + " KillScore: " + MyBotModule.Broodwar.self().getKillScore());
		//currentY += 10;

		// 적군이 입은 피해 누적값
		if(InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer) != null)
		{
			Prebot.Broodwar.drawTextScreen(x, currentY, UxColor.CHAR_BROWN + " Enemy Loss:" + UxColor.CHAR_WHITE +" Minerals: " + UxColor.CHAR_RED + InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getMineralsLost() + UxColor.CHAR_WHITE + " Gas: " + UxColor.CHAR_TEAL + InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getGasLost());
		}
	}	
		
	public void drawUnitStatisticsOnScreen2(int x, int y) {
			
		int currentY = y;
		
		// 적군의 UnitType 별 파악된 Unit 숫자를 표시
		Prebot.Broodwar.drawTextScreen(x,		 currentY, UxColor.CHAR_WHITE + " UNIT NAME");
		//MyBotModule.Broodwar.drawTextScreen(x + 110, currentY + 20, white + " Created");
		//MyBotModule.Broodwar.drawTextScreen(x + 150, currentY + 20, white + " Dead");
		Prebot.Broodwar.drawTextScreen(x + 85, currentY, UxColor.CHAR_WHITE + " Alive");

		int yspace = 0;
		
		Set<String> allUnit = new HashSet<String>();
		Iterator<String> it = null;
		if(InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer) != null)
		{
			it = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumCreatedUnits().keySet().iterator();
			while(it.hasNext())
			{
				String unit = it.next();
				allUnit.add(unit);
			}
//			it = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumDeadUnits().keySet().iterator();
//			while(it.hasNext())
//			{
//				String unit = it.next();
//				allUnit.add(unit);
//			}
			it = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumUnits().keySet().iterator();
			while(it.hasNext())
			{
				String unit = it.next();
				allUnit.add(unit);
			}
			
			it = allUnit.iterator();
			// for (UnitType t : UnitType.allUnitTypes())
			while(it.hasNext())
			{
				tempUnitName = it.next();
				
				int numCreatedUnits = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumCreatedUnits(tempUnitName);
//				int numDeadUnits = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumDeadUnits(tempUnitName);
				int numUnits = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getNumUnits(tempUnitName);
	
				String displayname = tempUnitName.replace("Protoss", "P");
				displayname = displayname.replace("Terran", "T");
				displayname = displayname.replace("Zerg", "Z");
				
				if (numUnits > 0)
				{
					Prebot.Broodwar.drawTextScreen(x,		 currentY + 30 + ((yspace)* 10), displayname);
//					MyBotModule.Broodwar.drawTextScreen(x + 120, currentY + 30 + ((yspace)* 10), "" + numCreatedUnits);
//					MyBotModule.Broodwar.drawTextScreen(x + 160, currentY + 30 + ((yspace)* 10), "" + numDeadUnits);
					Prebot.Broodwar.drawTextScreen(x + 110, currentY + 30 + ((yspace)* 10), "" + numUnits);
					yspace++;
				}
			}
		}
		
		yspace++;

		// 아군의 UnitType 별 파악된 Unit 숫자를 표시
		allUnit = new HashSet<String>();
		it = null;
		if(InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer) != null)
		{
			it = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumCreatedUnits().keySet().iterator();
			while(it.hasNext())
			{
				String unit = it.next();
				allUnit.add(unit);
			}
//			it = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumDeadUnits().keySet().iterator();
//			while(it.hasNext())
//			{
//				String unit = it.next();
//				allUnit.add(unit);
//			}
			it = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits().keySet().iterator();
			while(it.hasNext())
			{
				String unit = it.next();
				allUnit.add(unit);
			}
			
			it = allUnit.iterator();
			// for (UnitType t : UnitType.allUnitTypes())
			while(it.hasNext())
			{
				tempUnitName = it.next();
				int numCreatedUnits = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumCreatedUnits(tempUnitName);
//				int numDeadUnits = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumDeadUnits(tempUnitName);
				int numUnits = InformationManager.Instance().getUnitData(InformationManager.Instance().selfPlayer).getNumUnits(tempUnitName);
	
				String temp2 = "Terran";
				String displayname = tempUnitName.replace(temp2, "T");
				
				if (numUnits > 0)
				{
					Prebot.Broodwar.drawTextScreen(x,		 currentY + 30 + ((yspace)* 10), displayname);
					//MyBotModule.Broodwar.drawTextScreen(x + 120, currentY + 30 + ((yspace)* 10), "" + numCreatedUnits);
					//MyBotModule.Broodwar.drawTextScreen(x + 160, currentY + 30 + ((yspace)* 10), "" + numDeadUnits);
					Prebot.Broodwar.drawTextScreen(x + 110, currentY + 30 + ((yspace)* 10), "" + numUnits);
					yspace++;
				}
			}
		}
	}

	/// BWTA 라이브러리에 의한 Map 분석 결과 정보를 Map 에 표시합니다
	public void drawBWTAResultOnMap() {
		/*//we will iterate through all the base locations, and draw their outlines.
		// C+ . for (std.set<BWTA.BaseLocation*>.const_iterator i = BWTA.getBaseLocations().begin(); i != BWTA.getBaseLocations().end(); i++)
		for(BaseLocation baseLocation : BWTA.getBaseLocations())
		{
			TilePosition p = baseLocation.getTilePosition();
			Position c = baseLocation.getPosition();

			//draw outline of Base location 
			MyBotModule.Broodwar.drawBoxMap(p.getX() * 32, p.getY() * 32, p.getX() * 32 + 4 * 32, p.getY() * 32 + 3 * 32, Color.Blue);

			//draw a circle at each mineral patch
			// C++ : for (BWAPI.Unitset.iterator j = (*i).getStaticMinerals().begin(); j != (*i).getStaticMinerals().end(); j++)
			for(Unit unit : baseLocation.getStaticMinerals())
			{
				Position q = unit.getInitialPosition();
				MyBotModule.Broodwar.drawCircleMap(q.getX(), q.getY(), 30, Color.Cyan);
			}

			//draw the outlines of vespene geysers
			// C++ : for (BWAPI.Unitset.iterator j = (*i).getGeysers().begin(); j != (*i).getGeysers().end(); j++)
			for(Unit unit :baseLocation.getGeysers() )
			{
				TilePosition q = unit.getInitialTilePosition();
				MyBotModule.Broodwar.drawBoxMap(q.getX() * 32, q.getY() * 32, q.getX() * 32 + 4 * 32, q.getY() * 32 + 2 * 32, Color.Orange);
			}

			//if this is an island expansion, draw a yellow circle around the base location
			if (baseLocation.isIsland())
			{
				MyBotModule.Broodwar.drawCircleMap(c, 80, Color.Yellow);
			}
		}

		//we will iterate through all the regions and draw the polygon outline of it in green.
		// C++ : for (std.set<BWTA.Region*>.const_iterator r = BWTA.getRegions().begin(); r != BWTA.getRegions().end(); r++)
		for(Region region : BWTA.getRegions())
		{
			Polygon p = region.getPolygon();
			for (int j = 0; j<p.getPoints().size(); j++)
			{
				Position point1 = p.getPoints().get(j);
				Position point2 = p.getPoints().get((j + 1) % p.getPoints().size());
				MyBotModule.Broodwar.drawLineMap(point1, point2, Color.Green);
			}
		}

		//we will visualize the chokepoints with red lines
		// C++ : for (std.set<BWTA.Region*>.const_iterator r = BWTA.getRegions().begin(); r != BWTA.getRegions().end(); r++)
		for(Region region : BWTA.getRegions())
		{
			// C++ : for (std.set<BWTA.Chokepoint*>.const_iterator c = (*r).getChokepoints().begin(); c != (*r).getChokepoints().end(); c++)
			for(Chokepoint Chokepoint : region.getChokepoints())
			{
				Position point1 = Chokepoint.getSides().first;
				Position point2 = Chokepoint.getSides().second;
				MyBotModule.Broodwar.drawLineMap(point1, point2, Color.Red);
			}
		}*/
		int blueCount = 0;
		int cyanCount = 0;
		int orangeCount = 0;
//		int purpleCount = 0;
		
		if(hasSavedBWTAInfo == false)
		{
			for(BaseLocation baseLocation : BWTA.getBaseLocations())
			{
				blueCount++;
//				purpleCount++;
				for(Unit unit : baseLocation.getStaticMinerals())
				{
					cyanCount++;
				}
				for(Unit unit :baseLocation.getGeysers() )
				{
					orangeCount++;
				}
				
			}
			
			blue = new int[blueCount][4];
			int blueIndex = 0;
			cyan = new int[cyanCount][2];
			int cyanIndex = 0;
			orange = new int[orangeCount][4];
			int orangeIndex = 0;
			
//			purple = new int[purpleCount][4];
//			int purpleIndex = 0;
			
			for(BaseLocation baseLocation : BWTA.getBaseLocations())
			{
				TilePosition p = baseLocation.getTilePosition();
				Position c = baseLocation.getPosition();
				
				blue[blueIndex][0] = p.getX() * 32;
				blue[blueIndex][1] = p.getY() * 32;
				blue[blueIndex][2] = p.getX() * 32 + 4 * 32;
				blue[blueIndex][3] = p.getY() * 32 + 3 * 32;
				blueIndex++;
				
//				purple[purpleIndex][0] = (p.getX()+4) * 32;
//				purple[purpleIndex][1] = (p.getY()+1) * 32;
//				purple[purpleIndex][2] = (p.getX()+4) * 32 + 2 * 32;
//				purple[purpleIndex][3] = (p.getY()+1) * 32 + 2 * 32;
//				purpleIndex++;
				
				//draw a circle at each mineral patch
				// C++ : for (BWAPI.Unitset.iterator j = (*i).getStaticMinerals().begin(); j != (*i).getStaticMinerals().end(); j++)
				for(Unit unit : baseLocation.getStaticMinerals())
				{
					Position q = unit.getInitialPosition();
					cyan[cyanIndex][0] = q.getX();
					cyan[cyanIndex][1] = q.getY();
					cyanIndex++;
				}

				//draw the outlines of vespene geysers
				// C++ : for (BWAPI.Unitset.iterator j = (*i).getGeysers().begin(); j != (*i).getGeysers().end(); j++)
				for(Unit unit :baseLocation.getGeysers() )
				{
					TilePosition q = unit.getInitialTilePosition();
					orange[orangeIndex][0] = q.getX() * 32;
					orange[orangeIndex][1] = q.getY() * 32;
					orange[orangeIndex][2] = q.getX() * 32 + 4 * 32;
					orange[orangeIndex][3] = q.getY() * 32 + 2 * 32;
					orangeIndex++;
				}

				//if this is an island expansion, draw a yellow circle around the base location
				if (baseLocation.isIsland())
				{
					yellow.add(c);
				}
			}

			//we will iterate through all the regions and draw the polygon outline of it in green.
			// C++ : for (std.set<BWTA.Region*>.const_iterator r = BWTA.getRegions().begin(); r != BWTA.getRegions().end(); r++)
			for(Region region : BWTA.getRegions())
			{
				Polygon p = region.getPolygon();
				for (int j = 0; j<p.getPoints().size(); j++)
				{
					green1.add(p.getPoints().get(j));
					green2.add(p.getPoints().get((j + 1) % p.getPoints().size()));
				}
			}

			//we will visualize the chokepoints with red lines
			// C++ : for (std.set<BWTA.Region*>.const_iterator r = BWTA.getRegions().begin(); r != BWTA.getRegions().end(); r++)
			for(Region region : BWTA.getRegions())
			{
				// C++ : for (std.set<BWTA.Chokepoint*>.const_iterator c = (*r).getChokepoints().begin(); c != (*r).getChokepoints().end(); c++)
				for(Chokepoint Chokepoint : region.getChokepoints())
				{
					red1.add(Chokepoint.getSides().first);
					red2.add(Chokepoint.getSides().second);
				}
			}
			hasSavedBWTAInfo = true;
			
//			System.out.println(blueCount + " " + cyanCount + " " + orangeCount + " " + yellowCount + " " + greenCount + " " + redCount);
		}

		if(hasSavedBWTAInfo)
		{
			for(int i1=0 ; i1<blue.length ; i1++)
			{
				Prebot.Broodwar.drawBoxMap(blue[i1][0], blue[i1][1], blue[i1][2], blue[i1][3], Color.Blue);
			}
//			for(int i1=0 ; i1<purple.length ; i1++)
//			{
//				Prebot.Broodwar.drawBoxMap(purple[i1][0], purple[i1][1], purple[i1][2], purple[i1][3], Color.Purple);
//			}
			for(int i2=0 ; i2<cyan.length ; i2++)
			{
				Prebot.Broodwar.drawCircleMap(cyan[i2][0], cyan[i2][1], 30, Color.Cyan);	
			}
			for(int i3=0 ; i3<orange.length ; i3++)
			{
				Prebot.Broodwar.drawBoxMap(orange[i3][0], orange[i3][1], orange[i3][2], orange[i3][3], Color.Orange);
			}
			for(int i4=0 ; i4<yellow.size() ; i4++)
			{
				Prebot.Broodwar.drawCircleMap(yellow.get(i4), 80, Color.Yellow);	
			}
			for(int i5=0 ; i5<green1.size() ; i5++)
			{
				Prebot.Broodwar.drawLineMap(green1.get(i5), green2.get(i5), Color.Green);	
			}
			for(int i6=0 ; i6<red1.size() ; i6++)
			{
				Prebot.Broodwar.drawLineMap(red1.get(i6), red2.get(i6), Color.Red);	
			}			

			// OccupiedBaseLocation 을 원으로 표시
			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer)) {
				Prebot.Broodwar.drawCircleMap(baseLocation.getPosition(), 10 * UxConfig.TILE_SIZE, Color.Blue);	
			}
			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
				Prebot.Broodwar.drawCircleMap(baseLocation.getPosition(), 10 * UxConfig.TILE_SIZE, Color.Red);	
			}

			// ChokePoint, BaseLocation 을 텍스트로 표시
			if (InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self()) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getPosition(), "My MainBaseLocation");
			}
			if (InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self()) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self()).getCenter(), "My First ChokePoint");
			}
			if (InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.self()) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.self()).getCenter(), "My Second ChokePoint");
			}
			if (InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.self()) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.self()).getPosition(), "My First ExpansionLocation");
			}

			if (InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer).getPosition(), "Enemy MainBaseLocation");
			}
			if (InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer).getCenter(), "Enemy First ChokePoint");
			}
			if (InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer).getCenter(), "Enemy Second ChokePoint");
			}
			if (InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer) != null) {
				Prebot.Broodwar.drawTextMap(InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer).getPosition(), "Enemy First ExpansionLocation");
			}
			
		}
	}

	/// Tile Position 그리드를 Map 에 표시합니다
	public void drawMapGrid() {
		int	cellSize = MapGrid.Instance().getCellSize();
		int	mapWidth = MapGrid.Instance().getMapWidth();
		int mapHeight = MapGrid.Instance().getMapHeight();
		int	rows = MapGrid.Instance().getRows();
		int	cols = MapGrid.Instance().getCols();
		
		for (int i = 0; i<cols; i++) {
			Prebot.Broodwar.drawLineMap(i*cellSize, 0, i*cellSize, mapHeight, Color.Blue);
		}

		for (int j = 0; j<rows; j++) {
			Prebot.Broodwar.drawLineMap(0, j*cellSize, mapWidth, j*cellSize, Color.Blue);
		}
		
		for (int r = 0; r < rows; r+=2)
		{
			for (int c = 0; c < cols; c+=2)
			{
				Prebot.Broodwar.drawTextMap(c * 32, r * 32, c + "," + r);
			}
		}		
	}

	/// BuildOrderQueue 를 Screen 에 표시합니다
	public void drawBuildOrderQueueOnScreen(int x, int y) {
		char initialFinishedColor = InitialBuildProvider.Instance().getAdaptStrategyStatus() != AdaptStrategyStatus.COMPLETE ? UxColor.CHAR_WHITE : UxColor.CHAR_YELLOW;
		Prebot.Broodwar.drawTextScreen(x, y, initialFinishedColor + " <Build Order>");

		/*
		std.deque< BuildOrderItem >* queue = BuildManager.Instance().buildQueue.getQueue();
		size_t reps = queue.size() < 24 ? queue.size() : 24;
		for (size_t i(0); i<reps; i++) {
			const MetaType & type = (*queue)[queue.size() - 1 - i].metaType;
			MyBotModule.game.drawTextScreen(x, y + 10 + (i * 10), " %s", type.getName().c_str());
		}
		*/

		Deque<BuildOrderItem> buildQueue = BuildManager.Instance().buildQueue.getQueue();
		int itemCount = 0;

		// C++ : for (std.deque<BuildOrderItem>.reverse_iterator itr = buildQueue.rbegin(); itr != buildQueue.rend(); itr++) {
		// C++ : 			BuildOrderItem & currentItem = *itr;
		// C++ : 			MyBotModule.game.drawTextScreen(x, y + 10 + (itemCount * 10), " %s", currentItem.metaType.getName().c_str());
		// C++ : 			itemCount++;
		// C++ : 			if (itemCount >= 24) break;
		// C++ : 		}
		
		Object[] tempQueue = buildQueue.toArray();
		
		for(int i=0 ; i<tempQueue.length ; i++){
			BuildOrderItem currentItem = (BuildOrderItem)tempQueue[i];
			Prebot.Broodwar.drawTextScreen(x, y + 10 + (itemCount * 10), currentItem.blocking + " " + UxColor.CHAR_WHITE + currentItem.metaType.getName());
			itemCount++;
			if (itemCount >= 24) break;
		}
	}

	/// Build 진행 상태를 Screen 에 표시합니다
	public void drawBuildStatusOnScreen(int x, int y) {
		// 건설 / 훈련 중인 유닛 진행상황 표시
		Vector<Unit> unitsUnderConstruction = new Vector<Unit>();
		for (Unit unit : Prebot.Broodwar.self().getUnits())
		{
			if (unit != null && unit.isBeingConstructed())
			{
				unitsUnderConstruction.add(unit);
			}
		}

		// sort it based on the time it was started
		Object[] tempArr = unitsUnderConstruction.toArray();
		//Arrays.sort(tempArr);
		unitsUnderConstruction = new Vector<Unit>();
		for(int i=0 ; i<tempArr.length ; i++){
			unitsUnderConstruction.add((Unit)tempArr[i]);
		}
		// C++ : std.sort(unitsUnderConstruction.begin(), unitsUnderConstruction.end(), CompareWhenStarted());

		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + " <Build Status>");

		int reps = unitsUnderConstruction.size() < 10 ? unitsUnderConstruction.size() : 10;

		for (Unit unit : unitsUnderConstruction)
		{
			y += 10;
			UnitType t = unit.getType();
			if (t == UnitType.Zerg_Egg)
			{
				t = unit.getBuildType();
			}

			Prebot.Broodwar.drawTextScreen(x, y, "" + UxColor.CHAR_WHITE + t + " (" + unit.getRemainingBuildTime() + ")");
		}

		// Tech Research 표시

		// Upgrade 표시
	}

	/// Construction 을 하기 위해 예약해둔 Tile 들을 Map 에 표시합니다
	public void drawReservedBuildingTilesOnMap() {
		boolean[][] reserveMap = ConstructionPlaceFinder.Instance().getReserveMap();
		if(reserveMap.length > 0 && reserveMap[0] != null && reserveMap[0].length > 0)
		{
			int rwidth = reserveMap.length;
			int rheight = reserveMap[0].length;

			for (int x = 0; x < rwidth; ++x)
			{
				for (int y = 0; y < rheight; ++y)
				{
					if (reserveMap[x][y])
					{
						int x1 = x * 32 + 8;
						int y1 = y * 32 + 8;
						int x2 = (x + 1) * 32 - 8;
						int y2 = (y + 1) * 32 - 8;

						Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Yellow, false);
					}
				}
			}
		}
	}
	
	/// Construction 을 하지 못하는 Tile 들을 Map 에 표시합니다
	public void drawTilesToAvoidOnMap() {
//		Set<TilePosition> tilesToAvoid = ConstructionPlaceFinder.Instance().getTilesToAvoid();
//		for (TilePosition t : tilesToAvoid)
//		{
//			int x1 = t.getX() * 32 + 8;
//			int y1 = t.getY() * 32 + 8;
//			int x2 = (t.getX() + 1) * 32 - 8;
//			int y2 = (t.getY() + 1) * 32 - 8;
//
//			Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Orange, false);
//		}
//		
//		Set<TilePosition> tilesToAvoid3 = ConstructionPlaceFinder.Instance().getTilesToAvoidAbsolute();
//		for (TilePosition t : tilesToAvoid3)
//		{
//			int x1 = t.getX() * 32 + 8;
//			int y1 = t.getY() * 32 + 8;
//			int x2 = (t.getX() + 1) * 32 - 8;
//			int y2 = (t.getY() + 1) * 32 - 8;
//
//			Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Purple, false);
//		}
//		
//		Set<TilePosition> tilesToAvoidSupply = ConstructionPlaceFinder.Instance().getTilesToAvoidSupply();
//		for (TilePosition t : tilesToAvoidSupply)
//		{
//			int x1 = t.getX() * 32 + 8;
//			int y1 = t.getY() * 32 + 8;
//			int x2 = (t.getX() + 1) * 32 - 8;
//			int y2 = (t.getY() + 1) * 32 - 8;
//
//			Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Red, false);
//		}
		
//		FileUtils.appendTextToFile("log.txt", "\n drawTilesToAvoidOnMap");
		
		Set<TilePosition> tilesToAvoidMaxRange = ConstructionPlaceFinder.Instance().getTilesToAvoidMaxRange();
		for (TilePosition t : tilesToAvoidMaxRange)
		{
			int x1 = t.getX() * 32 + 8;
			int y1 = t.getY() * 32 + 8;
			int x2 = (t.getX() + 1) * 32 - 8;
			int y2 = (t.getY() + 1) * 32 - 8;

			Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Cyan, false);
		}
		
		Set<TilePosition> tilesToAvoidBasePoint = ConstructionPlaceFinder.Instance().getTilesToAvoidBasePotin();
		for (TilePosition t : tilesToAvoidBasePoint)
		{
			int x1 = t.getX() * 32 + 8;
			int y1 = t.getY() * 32 + 8;
			int x2 = (t.getX() + 1) * 32 - 8;
			int y2 = (t.getY() + 1) * 32 - 8;

			Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Red, false);
		}
		
	}

	/// ConstructionQueue 를 Screen 에 표시합니다
	public void drawConstructionQueueOnScreenAndMap(int x, int y) {
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + " <Construction Status>");

		int yspace = 0;

		Vector<ConstructionTask> constructionQueue = ConstructionManager.Instance().getConstructionQueue();

		for (final ConstructionTask b : constructionQueue)
		{
			String constructionState = "";

			if (b.getStatus() == ConstructionTask.ConstructionStatus.Unassigned.ordinal())
			{
				Prebot.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), "" + UxColor.CHAR_WHITE + b.getType() + " - No Worker");
			}
			else if (b.getStatus() == ConstructionTask.ConstructionStatus.Assigned.ordinal())
			{
				if (b.getConstructionWorker() == null) {
					Prebot.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), b.getType() + " - Assigned Worker Null");
				}			
				else {
					Prebot.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), b.getType() + " - Assigned Worker " + b.getConstructionWorker().getID() + ", Position (" + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY() + ")");
				}

				int x1 = b.getFinalPosition().getX() * 32;
				int y1 = b.getFinalPosition().getY() * 32;
				int x2 = (b.getFinalPosition().getX()+ b.getType().tileWidth()) * 32;
				int y2 = (b.getFinalPosition().getY() + b.getType().tileHeight()) * 32;

				Prebot.Broodwar.drawLineMap(b.getConstructionWorker().getPosition().getX(), b.getConstructionWorker().getPosition().getY(), (x1 + x2) / 2, (y1 + y2) / 2, Color.Orange);
				Prebot.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Red, false);
			}
			else if (b.getStatus() == ConstructionTask.ConstructionStatus.UnderConstruction.ordinal())
			{
				Prebot.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), "" + UxColor.CHAR_WHITE + b.getType() + " - Under Construction");
			}
			yspace++;
		}
	}

	public void drawnextPoints() {
		
//		Position nextEX = InformationManager.Instance().getNextExpansionLocation().getPosition();
//		Position nextBuild = InformationManager.Instance().getLastBuildingLocation().toPosition();
//		Position lastBuild2 = InformationManager.Instance().getLastBuildingLocation2().toPosition();
		
	    BaseLocation getExpansionLocation = InformationManager.Instance().getExpansionLocation;
	    BaseLocation secondStartPosition = InformationManager.Instance().getSecondStartPosition();
	    TilePosition getLastBuildingLocation = InformationManager.Instance().getLastBuildingLocation;
	    TilePosition getLastBuildingFinalLocation = InformationManager.Instance().getLastBuildingFinalLocation;
	    
		if(secondStartPosition!= null) {
			Prebot.Broodwar.drawTextScreen(10, 110, "secondStartPosition: " + secondStartPosition.getTilePosition());
			Prebot.Broodwar.drawTextMap(secondStartPosition.getPosition(), "secondStartPosition");
		}else {
			Prebot.Broodwar.drawTextScreen(10, 110, "secondStartPosition: null");
		}
		if(getExpansionLocation!= null) {
			Prebot.Broodwar.drawTextScreen(10, 120, "getExpansionLocation: " + getExpansionLocation.getTilePosition());
			Prebot.Broodwar.drawTextMap(getExpansionLocation.getPosition(), "nextEX");
		}else {
			Prebot.Broodwar.drawTextScreen(10, 120, "getExpansionLocation: null");
		}
		if(getLastBuildingLocation!= null) {
			Prebot.Broodwar.drawTextScreen(10, 130, "getLastBuildingLocation: " + getLastBuildingLocation);
			Prebot.Broodwar.drawTextMap(getLastBuildingLocation.toPosition(), "nextBuild");
		}else {
			Prebot.Broodwar.drawTextScreen(10, 130, "getLastBuildingLocation: null");
		}
		if(getLastBuildingFinalLocation!= null) {
			Prebot.Broodwar.drawTextScreen(10, 140, "getLastBuildingFinalLocation: " + getLastBuildingFinalLocation);
			Prebot.Broodwar.drawTextMap(getLastBuildingFinalLocation.toPosition(), "LastBuild");
		}else {
			Prebot.Broodwar.drawTextScreen(10, 140, "getLastBuildingFinalLocation: null");
		}

		
		Prebot.Broodwar.drawTextScreen(10, 150, "mainBaseLocationFull: " + BuildManager.Instance().mainBaseLocationFull);
		Prebot.Broodwar.drawTextScreen(10, 160, "secondChokePointFull: " + BuildManager.Instance().secondChokePointFull);
		Prebot.Broodwar.drawTextScreen(10, 170, "secondStartLocationFull: " + BuildManager.Instance().secondStartLocationFull);
		Prebot.Broodwar.drawTextScreen(10, 180, "fisrtSupplePointFull: " + BuildManager.Instance().fisrtSupplePointFull);

	}
	
	
	
	/// Unit 의 Id 를 Map 에 표시합니다
	public void drawUnitIdOnMap() {
		for (Unit unit : Prebot.Broodwar.self().getUnits())
		{
			if(unit.getType().isBuilding()){
				Prebot.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
				Prebot.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 25, "" + UxColor.CHAR_WHITE + unit.getTilePosition().getX() + " / " + unit.getTilePosition().getY());
			}else{
				Prebot.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
			}
			
		}
		for (Unit unit : Prebot.Broodwar.enemy().getUnits())
		{
			Prebot.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
		}
	}

	public void drawLeaderUnitOnMap() {
		
//		
//		if(leader!=null){
//			for (Unit unit : MyBotModule.Broodwar.self().getUnits())
//			{
//				if(unit.getID() == leader.getID())
//				MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + blue + "LEADER");
//			}
//		}
	}
	
	/// Worker Unit 들의 상태를 Screen 에 표시합니다
	public void drawWorkerStateOnScreen(int x, int y) {
		WorkerData  workerData = WorkerManager.Instance().getWorkerData();

		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "<Workers : " + workerData.getNumMineralWorkers() + ">");

		int yspace = 0;

		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;

			// Mineral / Gas / Idle Worker 는 표시 안한다
			if (workerData.getJobCode(unit) == 'M' || workerData.getJobCode(unit) == 'I' || workerData.getJobCode(unit) == 'G') {
				continue;
			}

			Prebot.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), UxColor.CHAR_WHITE + " " + unit.getID());

			if (workerData.getJobCode(unit) == 'B') {
				Prebot.Broodwar.drawTextScreen(x + 30, y + 10 + ((yspace++) * 10), UxColor.CHAR_WHITE + " " + workerData.getJobCode(unit) + " " + unit.getBuildType() + " " + (unit.isConstructing() ? 'Y' : 'N') + " (" + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY() + ")");
			}
			else {
				Prebot.Broodwar.drawTextScreen(x + 30, y + 10 + ((yspace++) * 10), UxColor.CHAR_WHITE + " " + workerData.getJobCode(unit));
			}
		}
	}

	/// ResourceDepot 별 Worker 숫자를 Map 에 표시합니다
	public void drawWorkerCountOnMap() {
		for (Unit depot : WorkerManager.Instance().getWorkerData().getDepots())
		{
			if (depot == null) continue;

			int x = depot.getPosition().getX() - 64;
			int y = depot.getPosition().getY() - 32;

			Prebot.Broodwar.drawBoxMap(x - 2, y - 1, x + 75, y + 14, Color.Black, true);
			Prebot.Broodwar.drawTextMap(x, y, UxColor.CHAR_WHITE + " Workers: " + WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(depot));
		}
	}

	/// Worker Unit 의 자원채취 현황을 Map 에 표시합니다
	public void drawWorkerMiningStatusOnMap() {
		WorkerData  workerData = WorkerManager.Instance().getWorkerData();

		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			Position pos = worker.getTargetPosition();

			Prebot.Broodwar.drawTextMap(worker.getPosition().getX(), worker.getPosition().getY() - 5, "" + UxColor.CHAR_WHITE + workerData.getJobCode(worker));
			
			Prebot.Broodwar.drawLineMap(worker.getPosition().getX(), worker.getPosition().getY(), pos.getX(), pos.getY(), Color.Cyan);

			/*
			// ResourceDepot ~ Worker 사이에 직선 표시
			BWAPI.Unit depot = workerData.getWorkerDepot(worker);
			if (depot) {
				MyBotModule.game.drawLineMap(worker.getPosition().x, worker.getPosition().y, depot.getPosition().x, depot.getPosition().y, Color.Orange);
			}
			*/
		}
	}

	/// 정찰 상태를 Screen 에 표시합니다
	public void drawScoutInformation(int x, int y)
	{
//		int currentScoutStatus = OldScoutManager.Instance().getScoutStatus();
//		String scoutStatusString = null;
//
//		if(currentScoutStatus == OldScoutManager.ScoutStatus.MovingToAnotherBaseLocation.ordinal()){
//			scoutStatusString = "Moving To Another Base Location";
//		}else if(currentScoutStatus == OldScoutManager.ScoutStatus.MoveAroundEnemyBaseLocation.ordinal()){
//			scoutStatusString = "Move Around Enemy BaseLocation";
//		}else if(currentScoutStatus == OldScoutManager.ScoutStatus.NoScout.ordinal()){
//			scoutStatusString = "No Scout";
//		}else{
//			scoutStatusString = "No Scout";
//		}

		// get the enemy base location, if we have one
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);

		if (enemyBaseLocation != null) {
			Prebot.Broodwar.drawTextScreen(x, y, "Enemy MainBaseLocation : (" + enemyBaseLocation.getTilePosition().getX() + ", " + enemyBaseLocation.getTilePosition().getY() + ")");
		}
		else {
			Prebot.Broodwar.drawTextScreen(x, y, "Enemy MainBaseLocation : Unknown");
		}

//		if (currentScoutStatus == OldScoutManager.ScoutStatus.NoScout.ordinal()) {
//			Prebot.Broodwar.drawTextScreen(x, y + 10, "No Scout Unit");
//		}
//		else {
//			
//			Unit scoutUnit = OldScoutManager.Instance().getScoutUnit();
//			if (scoutUnit != null) {
//				Prebot.Broodwar.drawTextScreen(x, y + 10, "Scout Unit : " + scoutUnit.getType() + " " + scoutUnit.getID() + " (" + scoutUnit.getTilePosition().getX() + ", " + scoutUnit.getTilePosition().getY() + ")");
//	
//				Position scoutMoveTo = scoutUnit.getTargetPosition();
//	
//				if (scoutMoveTo != null && scoutMoveTo != Position.None && scoutMoveTo.isValid()) {
//	
//					double currentScoutTargetDistance;
//	
//					if (currentScoutStatus == OldScoutManager.ScoutStatus.MovingToAnotherBaseLocation.ordinal()) {
//						if (scoutUnit.getType().isFlyer()) {
//							currentScoutTargetDistance = (int)(scoutUnit.getPosition().getDistance(scoutMoveTo));
//						}
//						else {
//							currentScoutTargetDistance = PositionUtils.getGroundDistance(scoutUnit.getPosition(), scoutMoveTo);
//						}
//	
//						Prebot.Broodwar.drawTextScreen(x, y + 20, "Target = (" + scoutMoveTo.getX() / UxConfig.TILE_SIZE + ", " + scoutMoveTo.getY() / UxConfig.TILE_SIZE + ") Distance = " + currentScoutTargetDistance);
//					}
//					/*
//					else if (currentScoutStatus == ScoutManager.ScoutStatus.MoveAroundEnemyBaseLocation.ordinal()) {
//	
//						Vector<Position> vertices = ScoutManager.Instance().getEnemyRegionVertices();
//						for (int i = 0 ; i < vertices.size() ; ++i)
//						{
//							MyBotModule.Broodwar.drawCircleMap(vertices.get(i), 4, Color.Green, false);
//							MyBotModule.Broodwar.drawTextMap(vertices.get(i), "" + i);
//						}
//						MyBotModule.Broodwar.drawCircleMap(scoutMoveTo, 5, Color.Red, true);
//					}
//					*/
//				}
//			}
//		}
	}

	/// Unit 의 Target 으로 잇는 선을 Map 에 표시합니다
	public void drawUnitTargetOnMap() 
	{
		for (Unit unit : Prebot.Broodwar.self().getUnits())
		{
			if (unit != null && unit.isCompleted() && !unit.getType().isBuilding() && !unit.getType().isWorker())
			{
				Unit targetUnit = unit.getTarget();
				if (targetUnit != null && targetUnit.getPlayer() != Prebot.Broodwar.self()) {
					Prebot.Broodwar.drawCircleMap(unit.getPosition(), dotRadius, Color.Red, true);
					Prebot.Broodwar.drawCircleMap(targetUnit.getTargetPosition(), dotRadius, Color.Red, true);
					Prebot.Broodwar.drawLineMap(unit.getPosition(), targetUnit.getTargetPosition(), Color.Red);
				}
				else if (unit.isMoving()) {
					Prebot.Broodwar.drawCircleMap(unit.getPosition(), dotRadius, Color.Orange, true);
					Prebot.Broodwar.drawCircleMap(unit.getTargetPosition(), dotRadius, Color.Orange, true);
					Prebot.Broodwar.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.Orange);
				}

			}
		}
	}

	/// Bullet 을 Map 에 표시합니다 <br>
	/// Cloaking Unit 의 Bullet 표시에 쓰입니다
	public void drawBulletsOnMap()
	{
		for (Bullet b : Prebot.Broodwar.getBullets())
		{
			Position p = b.getPosition();
			double velocityX = b.getVelocityX();
			double velocityY = b.getVelocityY();

			if(b.getType() == BulletType.Acid_Spore) bulletTypeName = "Acid_Spore";
			else if(b.getType() == BulletType.Anti_Matter_Missile) bulletTypeName = "Anti_Matter_Missile";
			else if(b.getType() == BulletType.Arclite_Shock_Cannon_Hit) bulletTypeName = "Arclite_Shock_Cannon_Hit";
			else if(b.getType() == BulletType.ATS_ATA_Laser_Battery) bulletTypeName = "ATS_ATA_Laser_Battery";
			else if(b.getType() == BulletType.Burst_Lasers) bulletTypeName = "Burst_Lasers";
			else if(b.getType() == BulletType.C_10_Canister_Rifle_Hit) bulletTypeName = "C_10_Canister_Rifle_Hit";
			else if(b.getType() == BulletType.Consume) bulletTypeName = "Consume";
			else if(b.getType() == BulletType.Corrosive_Acid_Shot) bulletTypeName = "Corrosive_Acid_Shot";
			else if(b.getType() == BulletType.Dual_Photon_Blasters_Hit) bulletTypeName = "Dual_Photon_Blasters_Hit";
			else if(b.getType() == BulletType.EMP_Missile) bulletTypeName = "EMP_Missile";
			else if(b.getType() == BulletType.Ensnare) bulletTypeName = "Ensnare";
			else if(b.getType() == BulletType.Fragmentation_Grenade) bulletTypeName = "Fragmentation_Grenade";
			else if(b.getType() == BulletType.Fusion_Cutter_Hit) bulletTypeName = "Fusion_Cutter_Hit";
			else if(b.getType() == BulletType.Gauss_Rifle_Hit) bulletTypeName = "Gauss_Rifle_Hit";
			else if(b.getType() == BulletType.Gemini_Missiles) bulletTypeName = "Gemini_Missiles";
			else if(b.getType() == BulletType.Glave_Wurm) bulletTypeName = "Glave_Wurm";
			else if(b.getType() == BulletType.Halo_Rockets) bulletTypeName = "Halo_Rockets";
			else if(b.getType() == BulletType.Invisible) bulletTypeName = "Invisible";
			else if(b.getType() == BulletType.Longbolt_Missile) bulletTypeName = "Longbolt_Missile";
			else if(b.getType() == BulletType.Melee) bulletTypeName = "Melee";
			else if(b.getType() == BulletType.Needle_Spine_Hit) bulletTypeName = "Needle_Spine_Hit";
			else if(b.getType() == BulletType.Neutron_Flare) bulletTypeName = "Neutron_Flare";
			else if(b.getType() == BulletType.None) bulletTypeName = "None";
			else if(b.getType() == BulletType.Optical_Flare_Grenade) bulletTypeName = "Optical_Flare_Grenade";
			else if(b.getType() == BulletType.Particle_Beam_Hit) bulletTypeName = "Particle_Beam_Hit";
			else if(b.getType() == BulletType.Phase_Disruptor) bulletTypeName = "Phase_Disruptor";
			else if(b.getType() == BulletType.Plague_Cloud) bulletTypeName = "Plague_Cloud";
			else if(b.getType() == BulletType.Psionic_Shockwave_Hit) bulletTypeName = "Psionic_Shockwave_Hit";
			else if(b.getType() == BulletType.Psionic_Storm) bulletTypeName = "Psionic_Storm";
			else if(b.getType() == BulletType.Pulse_Cannon) bulletTypeName = "Pulse_Cannon";
			else if(b.getType() == BulletType.Queen_Spell_Carrier) bulletTypeName = "Queen_Spell_Carrier";
			else if(b.getType() == BulletType.Seeker_Spores) bulletTypeName = "Seeker_Spores";
			else if(b.getType() == BulletType.STA_STS_Cannon_Overlay) bulletTypeName = "STA_STS_Cannon_Overlay";
			else if(b.getType() == BulletType.Subterranean_Spines) bulletTypeName = "Subterranean_Spines";
			else if(b.getType() == BulletType.Sunken_Colony_Tentacle) bulletTypeName = "Sunken_Colony_Tentacle";
			else if(b.getType() == BulletType.Unknown) bulletTypeName = "Unknown";
			else if(b.getType() == BulletType.Yamato_Gun) bulletTypeName = "Yamato_Gun";
			
			// 아군 것이면 녹색, 적군 것이면 빨간색
			Prebot.Broodwar.drawLineMap(p, new Position(p.getX() + (int)velocityX, p.getY() + (int)velocityY), b.getPlayer() == Prebot.Broodwar.self() ? Color.Green : Color.Red);
			if(b.getType() != null)
			{
				Prebot.Broodwar.drawTextMap(p, (b.getPlayer() == Prebot.Broodwar.self() ? "" + UxColor.CHAR_TEAL : "" + UxColor.CHAR_RED) + bulletTypeName);
			}
		}
	}

	private void drawSquadUnitTagMap() {
		// draw neutral units and our units
		for (Squad squad : CombatManager.Instance().squadData.getSquadMap().values()) {
			Color color = UxColor.SQUAD_COLOR.get(squad.getClass());
			if (color == null) {
				continue;
			}
			String squadName = squad.getSquadName();
			
			SmallFightPredict smallFightPredict = null;
			if (squad instanceof WatcherSquad) {
				smallFightPredict = ((WatcherSquad) squad).getSmallFightPredict();
			}
			
			if (squadName.length() > 4) {
				squadName = squadName.substring(0, 4);
			}

			for (Unit unit : squad.unitList) {
				Prebot.Broodwar.drawCircleMap(unit.getPosition(), 10, color);
				Prebot.Broodwar.drawTextMap(unit.getPosition().getX() - 20, unit.getPosition().getY() - 30, squadName);
				if (smallFightPredict != null && smallFightPredict == SmallFightPredict.BACK) {
					Prebot.Broodwar.drawTextMap(unit.getPosition().getX() - 20, unit.getPosition().getY() - 15, UxColor.CHAR_RED + smallFightPredict.toString());
				}
			}
			
			Map<Integer, TravelSite> checkerSiteMap = VultureTravelManager.Instance().getCheckerSiteMap();
			for (Integer checkerId : checkerSiteMap.keySet()) {
				Unit unit = Prebot.Broodwar.getUnit(checkerId);
				if (UnitUtils.isValidUnit(unit)) {
					TravelSite travelSite = checkerSiteMap.get(checkerId);
					Prebot.Broodwar.drawTextMap(unit.getPosition().getX() - 20, unit.getPosition().getY() - 5, UxColor.CHAR_ORANGE + travelSite.baseLocation.getPosition().toString());
				}
			}
		}
	}
	
	private void drawSquadInfoOnMap(int x, int y) {
		// TODO Auto-generated method stub
		/// ConstructionQueue 를 Screen 에 표시합니다
		Prebot.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "<Squad Name>");
		Prebot.Broodwar.drawTextScreen(x +110, y, UxColor.CHAR_WHITE + " <Unit Size>");
		
		y += 15;
		for (Squad squad : CombatManager.Instance().squadData.getSquadMap().values()) {
			Color squadColor = UxColor.SQUAD_COLOR.get(squad.getClass());
			if (squadColor != null) {
				Prebot.Broodwar.drawTextScreen(x, y, "" + UxColor.COLOR_TO_CHARACTER.get(squadColor) + squad.getSquadName());
			} else {
				Prebot.Broodwar.drawTextScreen(x, y, "" + "*" + squad.getSquadName());
			}
			String unitIds = " ... ";
			for (Unit unit : squad.unitList) {
				unitIds = unitIds + unit.getID() + "/";
			}
			Prebot.Broodwar.drawTextScreen(x +120, y, "" + squad.unitList.size() + unitIds);
			y+=10;
		}
	}
	
	private void drawManagerTimeSpent(int x, int y) {
		List<GameManager> gameManagers = Arrays.asList(
				InformationManager.Instance(),
				StrategyManager.Instance(),
				MapGrid.Instance(),
				BuildManager.Instance(),
				BuildQueueProvider.Instance(),
				ConstructionManager.Instance(),
				WorkerManager.Instance(),
				CombatManager.Instance());
		
		
		int currentY = y;
		for (GameManager gameManager : gameManagers) {
			currentY += 10;
			Prebot.Broodwar.drawTextScreen(x, currentY, UxColor.CHAR_PURPLE + gameManager.getClass().getSimpleName());

			char drawColor = UxColor.CHAR_WHITE;
			if (gameManager.getRecorded() > 10L) {
				drawColor = UxColor.CHAR_TEAL;
			} else if (gameManager.getRecorded() > 30L) {
				drawColor = UxColor.CHAR_RED;
			}
			Prebot.Broodwar.drawTextScreen(x + 103, currentY, ": " + drawColor + gameManager.getRecorded());
		}

		Prebot.Broodwar.drawTextScreen(x, currentY += 15, "* group size: " + LagObserver.groupsize());
	}

	private void drawBigWatch() {
		Map<String, Long> resultTimeMap = BigWatch.getResultTimeMap();
		Map<String, Long> recordTimeMap = BigWatch.getRecordTimeMap();

		List<String> tags = new ArrayList<>(recordTimeMap.keySet());
		Collections.sort(tags);
		
		int currentY = 0;
		for (String tag : tags) {
			Long resultTime = resultTimeMap.get(tag);
			resultTime = resultTime == null ? 0L : resultTime;
			Long recordTime = recordTimeMap.get(tag);
			
			char drawColor = UxColor.CHAR_WHITE;
			if (recordTime > 10L) {
				drawColor = UxColor.CHAR_TEAL;
			} else if (recordTime > 30L) {
				drawColor = UxColor.CHAR_RED;
			}
			Prebot.Broodwar.drawTextScreen(10, currentY += 10, UxColor.CHAR_WHITE + tag + " : " + resultTime + " / " + drawColor + recordTime);
		}
	}
	
	private void drawPathData(){
		for (Unit depot : UnitUtils.getUnitList(UnitType.Terran_Command_Center)) {
			List<Minerals> mineralsList = WorkerData.depotMineral.get(depot);
			if (mineralsList == null) { // TODO
//				System.out.println("mineralsList is null.");
//				if (depot != null) {
//					System.out.println("depot=" + depot.getID() +  "" + depot.getPosition());
//				} else {
//					System.out.println("depot is null");
//				}
				return;
			}
			
			for(Minerals minr : mineralsList){
				if(minr.mineralTrick != null ){
					Prebot.Broodwar.drawCircleMap(minr.mineralUnit.getPosition().getX(),minr.mineralUnit.getPosition().getY(),4,Color.Blue,true );
					Prebot.Broodwar.drawCircleMap(minr.mineralTrick.getPosition().getX(),minr.mineralTrick.getPosition().getY(),4,Color.Purple,true );
				}
			}


			for(Minerals minr : WorkerData.depotMineral.get(depot)){
				if( minr.posTrick != bwapi.Position.None ){
					Prebot.Broodwar.drawCircleMap( minr.posTrick.getX(),minr.posTrick.getY(),4,Color.Red,true );
					Prebot.Broodwar.drawCircleMap(minr.mineralUnit.getPosition().getX(),minr.mineralUnit.getPosition().getY(),4,Color.Yellow,true );
				}
			}

			//Broodwar->drawCircleMap(Minerals[0].posTrick.x(),Minerals[0].posTrick.y(),2,Colors::Purple,true);

			//Prebot.Broodwar.drawCircleMap(Mineral.Instance().CCtrick.getX(),Mineral.Instance().CCtrick.getY(),2,Color.Brown,true);
			//for(int i=0; i< MineralManager.Instance().minerals.size(); i++){
				//Prebot.Broodwar.drawTextMap( minr.mineral.getPosition().getX(),minr.mineral.getPosition().getY(), "(" + (int)(MineralManager.Instance().minerals.get(i).MinToCC) + (int)(MineralManager.Instance().minerals.get(i).CCToMin)  + ")");
			//}
		}
		
	}

	private void drawStrategy() {
		String upgradeString = "";
		for (MetaType metaType : StrategyIdea.upgrade) {
			upgradeString += metaType.getName() + " > ";
		}
		
		int y = 10;
		Race enemyRace = InfoUtils.enemyRace();
		EnemyStrategy strategy = StrategyIdea.currentStrategy;
		int phase = StrategyAnalyseManager.Instance().getPhase();
		
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "[" + strategy.name() + " ...(phase "+ phase + ")]");
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "FAC RATIO : " + StrategyIdea.factoryRatio);
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "UPGRADE   : " + upgradeString);
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "MARINE CNT : " + StrategyIdea.marineCount);
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "ADDON     : " + StrategyIdea.addOnOption);
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "EXPANSION : " + StrategyIdea.expansionOption);
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "WRAITH CNT : " + StrategyIdea.wraithCount);
		Prebot.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "MISSION    : " + strategy.missionTypeList);
		Prebot.Broodwar.drawTextScreen(20, y += 20, UxColor.CHAR_YELLOW + "" + strategy.buildTimeMap);
		
		Prebot.Broodwar.drawTextScreen(20, 260, "" + UxColor.CHAR_YELLOW + ClueManager.Instance().getClueInfoList());

		y = 10;
		for (EnemyStrategy enemyStrategy : EnemyStrategy.values()) {
			if (enemyStrategy.name().startsWith(enemyRace.toString().toUpperCase())) {
				Prebot.Broodwar.drawTextScreen(400, y += 10, "" + UxColor.CHAR_YELLOW + enemyStrategy.name());
			}
		}
	}
	
	private void drawEnemyAirDefenseRange() {
		List<UnitInfo> airDefenseEuiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL, UnitUtils.enemyAirDefenseUnitType());
		for (UnitInfo eui : airDefenseEuiList) {
			if (eui.getType() == UnitType.Terran_Bunker) {
				Prebot.Broodwar.drawCircleMap(eui.getLastPosition(), Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 96, Color.White);
			} else {
				Prebot.Broodwar.drawCircleMap(eui.getLastPosition(), eui.getType().airWeapon().maxRange(), Color.White);
			}
		}
		List<UnitInfo> wraithKillerEuiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL, UnitUtils.wraithKillerUnitType());
		for (UnitInfo eui : wraithKillerEuiList) {
			Prebot.Broodwar.drawCircleMap(eui.getLastPosition(), eui.getType().airWeapon().maxRange(), Color.Grey);
		}
	}
	
	private void drawAirForceInformation() {
		// wraith moving
		for (Unit unit : UnitUtils.getUnitList(UnitType.Terran_Wraith)) {
			if (unit.isMoving()) {
				Prebot.Broodwar.drawCircleMap(unit.getPosition(), dotRadius, Color.Orange, true);
				Prebot.Broodwar.drawCircleMap(unit.getTargetPosition(), dotRadius, Color.Orange, true);
				Prebot.Broodwar.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.Orange);
			}
		}
		
		// target position
		List<Position> targetPositions = AirForceManager.Instance().getTargetPositions();
		for (int i = 0; i < targetPositions.size(); i++) {
			Prebot.Broodwar.drawTextMap(targetPositions.get(i), "position#" + i);
		}
		
		// air force team
		int y = 150;
		Set<AirForceTeam> airForceTeamSet = new HashSet<>(AirForceManager.Instance().getAirForceTeamMap().values());
		List<AirForceTeam> airForceList = new ArrayList<>(airForceTeamSet);
		airForceList.sort(new Comparator<AirForceTeam>() {
			@Override
			public int compare(AirForceTeam a1, AirForceTeam a2) {
				int memberGap = a1.memberList.size() - a2.memberList.size();
				int idGap = a1.leaderUnit.getID() - a2.leaderUnit.getID();
				return memberGap * 100 + idGap;
			}
		});
		
		for (AirForceTeam airForceTeam : airForceList) {
			char color = UxColor.CHAR_WHITE;
			if (airForceTeam.repairCenter != null) {
				color = UxColor.CHAR_RED;
			}
			Position position = airForceTeam.leaderUnit.getPosition();
			Prebot.Broodwar.drawTextMap(position.getX(), position.getY() - 10, color + "leader#" + airForceTeam.leaderUnit.getID());
			
			Position targetPosition = new Position(airForceTeam.getTargetPosition().getX(), airForceTeam.getTargetPosition().getY() - 10);
			Prebot.Broodwar.drawTextMap(targetPosition, UxColor.CHAR_RED + "*" + airForceTeam.leaderUnit.getID());
			Prebot.Broodwar.drawTextScreen(20, y += 15, "" + UxColor.CHAR_YELLOW + airForceTeam.toString());
		}
		Prebot.Broodwar.drawTextScreen(20, y += 15, "Defense Mode? " + AirForceManager.Instance().isAirForceDefenseMode());
		Prebot.Broodwar.drawTextScreen(20, y += 15, "strike level=" + AirForceManager.Instance().getStrikeLevel());
		Prebot.Broodwar.drawTextScreen(20, y += 15, "total achievement=" + AirForceManager.Instance().getAchievementEffectiveFrame());
		Prebot.Broodwar.drawTextScreen(20, y += 15, "accumulated achievement=" + AirForceManager.Instance().getAccumulatedAchievement());
		Prebot.Broodwar.drawTextScreen(20, y += 15, "wraith count=" + StrategyIdea.wraithCount);
	}

	private void drawVulturePolicy() {
		int y = 10;
		Prebot.Broodwar.drawTextScreen(400, y += 15, "[vulture policy]");
		Prebot.Broodwar.drawTextScreen(400, y += 15, "checkerMaxNumber=" + StrategyIdea.checkerMaxNumber);
		Prebot.Broodwar.drawTextScreen(400, y += 15, "spiderMineNumberPerPosition=" + StrategyIdea.spiderMineNumberPerPosition);
		Prebot.Broodwar.drawTextScreen(400, y += 15, "spiderMineNumberPerGoodPosition=" + StrategyIdea.spiderMineNumberPerGoodPosition);
		Prebot.Broodwar.drawTextScreen(400, y += 15, "watcherMinePositionLevel=" + StrategyIdea.watcherMinePositionLevel);
	}
	
	private void drawEnemyBaseToBaseTime() {
		int y = 0;
		Prebot.Broodwar.drawTextScreen(10, y += 15, "campPosition : " + StrategyIdea.campPosition + " / " + StrategyIdea.campType);
		Prebot.Broodwar.drawTextScreen(10, y += 15, "mainPosition : " + StrategyIdea.mainPosition);
		Prebot.Broodwar.drawTextScreen(10, y += 15, "watcherPosition : " + StrategyIdea.watcherPosition);
		Prebot.Broodwar.drawTextScreen(10, y += 15, "mainSquadCenter : " + StrategyIdea.mainSquadCenter);
		Prebot.Broodwar.drawTextScreen(10, y += 15, "enemyGroundSquadPosition : " + StrategyIdea.nearGroundEnemyPosition);
		Prebot.Broodwar.drawTextScreen(10, y += 15, "enemyAirSquadPosition : " + StrategyIdea.nearAirEnemyPosition);
		
		y += 10;
		Prebot.Broodwar.drawTextScreen(10, y += 15, "enemyBase : " + InfoUtils.enemyBase().getPosition());
		Prebot.Broodwar.drawTextScreen(10, y += 15, "enemyFirstExpansion : " + InfoUtils.enemyFirstExpansion().getPosition());
//		for (Entry<UnitType, Integer> unitType : InformationManager.Instance().baseToBaseUnit.entrySet()) {
//			Prebot.Broodwar.drawTextScreen(20, y += 10, "" + UxColor.CHAR_YELLOW + unitType.getKey() + " : " + unitType.getValue());
//		}
	}

	private void drawPositionInformation() {

		if (StrategyIdea.campPosition.equals(StrategyIdea.mainPosition)) {
			Prebot.Broodwar.drawTextMap(StrategyIdea.campPosition, "camp & main");
		} else {
			if (StrategyIdea.campPosition != null) {
				Prebot.Broodwar.drawTextMap(StrategyIdea.campPosition, "camp");
			}
			if (StrategyIdea.mainPosition != null) {
				Prebot.Broodwar.drawTextMap(StrategyIdea.mainPosition, "main");
			}
		}
		if (StrategyIdea.watcherPosition != null) {
			Prebot.Broodwar.drawTextMap(StrategyIdea.watcherPosition, "watcherPos");
		}
		if (StrategyIdea.mainSquadCenter != null) {
			Prebot.Broodwar.drawTextMap(StrategyIdea.mainSquadCenter, "mainSqCntr");
			Prebot.Broodwar.drawCircleMap(StrategyIdea.mainSquadCenter.getX(), StrategyIdea.mainSquadCenter.getY(), StrategyIdea.mainSquadCoverRadius, Color.Cyan);
		}
		if (StrategyIdea.nearGroundEnemyPosition != null) {
			Prebot.Broodwar.drawTextMap(StrategyIdea.nearGroundEnemyPosition, "nearEnemySq(Ground)");
		}
		if (StrategyIdea.nearAirEnemyPosition != null) {
			Prebot.Broodwar.drawTextMap(StrategyIdea.nearAirEnemyPosition, "nearEnemySq(Air)");
		}
		if (StrategyIdea.totalEnemyCneterPosition != null) {
			Prebot.Broodwar.drawTextMap(StrategyIdea.totalEnemyCneterPosition, "totalEnemySq");
		}
		if (InfoUtils.myReadyToPosition() != null) {
			Prebot.Broodwar.drawTextMap(InfoUtils.myReadyToPosition(), "myReadyTo");
		}
		if (InfoUtils.enemyReadyToPosition() != null) {
			Prebot.Broodwar.drawTextMap(InfoUtils.enemyReadyToPosition(), "enemyReadyTo");
		}
		if (VultureTravelManager.Instance().getTravelSites() != null) {
			for (TravelSite site : VultureTravelManager.Instance().getTravelSites()) {
				Prebot.Broodwar.drawTextMap(site.baseLocation.getPosition(), "travel site\n" + site);
			}
		}
	}

}