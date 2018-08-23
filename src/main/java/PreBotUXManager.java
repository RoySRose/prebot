

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
import java.util.Map.Entry;
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

/// 봇 프로그램 개발의 편의성 향상을 위해 게임 화면에 추가 정보들을 표시하는 class<br>
/// 여러 Manager 들로부터 정보를 조회하여 Screen 혹은 Map 에 정보를 표시합니다
public class PreBotUXManager {
	
	private int uxOption = 0;

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
	
	private UnitType factorySelected = UnitType.None;
	
	private Map<Integer, MicroDecision> decisionListForUx = new HashMap<>();
	private static PreBotUXManager instance = new PreBotUXManager();
	
	/// static singleton 객체를 리턴합니다
	public static PreBotUXManager Instance() {
		return instance;
	}
	
	/// 경기가 시작될 때 일회적으로 추가 정보를 출력합니다
	public void onStart() {
	}

	/// 경기 진행 중 매 프레임마다 추가 정보를 출력하고 사용자 입력을 처리합니다
	public void update() {
		if (uxOption == 0) {
			UXManager.Instance().update();
//			drawDebugginUxMenu();
		} else {
			
			if (uxOption == 1) {
				drawGameInformationOnScreen(5, 5);
				drawBWTAResultOnMap();
				drawBuildOrderQueueOnScreen(500, 50);
				drawBuildStatusOnScreen(370, 50);
				drawConstructionQueueOnScreenAndMap(200, 150);
				drawReservedBuildingTilesOnMap();
				drawTilesToAvoidOnMap();
				drawWorkerMiningStatusOnMap();
				drawUnitTargetOnMap();
				drawnextPoints();
				drawTurretMap();
				drawManagerTimeSpent(490, 200);

				// draw tile position of mouse cursor
				int mouseX = MyBotModule.Broodwar.getMousePosition().getX() + MyBotModule.Broodwar.getScreenPosition().getX();
				int mouseY = MyBotModule.Broodwar.getMousePosition().getY() + MyBotModule.Broodwar.getScreenPosition().getY();
				MyBotModule.Broodwar.drawTextMap(mouseX + 20, mouseY,
						"(" + (int) (mouseX / CommonConfig.UxConfig.TILE_SIZE) + ", " + (int) (mouseY / CommonConfig.UxConfig.TILE_SIZE) + ")");
				MyBotModule.Broodwar.drawTextMap(mouseX + 20, mouseY + 10, "(" + (int) (mouseX) + ", " + (int) (mouseY) + ")");
				// 미네랄PATH
			} else if (uxOption == 2) {
				drawStrategy();

			} else if (uxOption == 3) {
				drawEnemyBuildTimer();
				// drawCCtoScvCount();
			} else if (uxOption == 4) {
				drawSquadInfoOnMap(20, 30);
				drawManagerTimeSpent(490, 200);
				drawDecision();
				drawEnemyAirDefenseRange();
				drawAirForceInformation();
				drawVulturePolicy();

			} else if (uxOption == 5) {
				drawEnemyBaseToBaseTime();

			} else if (uxOption == 6) {
				drawBigWatch();
				drawManagerTimeSpent(490, 200);
			} else if (uxOption == 7) {
				drawTurretMap();
				drawTilesToAvoidOnMap();
				drawReservedBuildingTilesOnMap();
			} else if (uxOption == 8) {
				drawExpectedResource();
				//drawExpectedResource2();
			}

			drawMineralIdOnMap();
			drawUnitIdOnMap();
			drawPositionInformation();
			drawTimer();
			drawPathData();
			drawSquadUnitTagMap();
		}

		clearDecisionListForUx();
		
//		if (TimeUtils.executeRotation(1, 11)) {
//			ConstructionPlaceFinder.Instance().debugBuildLocationSet();
//		}
//		
//		if (TimeUtils.executeRotation(1, 233)) {
//			ConstructionPlaceFinder.Instance().debugBuildLocationPrint();
//		}
	}

	
	private void drawExpectedResource2() {
		 int m=190;
        //MyBotModule.Broodwar.drawTextScreen(190, 10, "EnemyPredictedUnitLIst: " + AttackDecisionMaker.Instance().predictedTotalEnemyAttackUnit.size());
		int l=10;
        for (Entry<UnitType, MutableFloat> enenmy : AttackDecisionMaker.Instance().predictedTotalEnemyAttackUnit.entrySet()){
        	float cnt = enenmy.getValue().get();
           	UnitType unitType = enenmy.getKey();
           	//MyBotModule.Broodwar.drawTextScreen(m, l+=10, unitType.toString().substring(7, 14)+" : "+cnt);
        }
	}
    private void drawExpectedResource() {
	    Map<UnitInfo, EnemyCommandInfo> enemyCommandInfoMap = AttackDecisionMaker.Instance().enemyResourceDepotInfoMap;

	    int y=0;
	    MyBotModule.Broodwar.drawTextScreen(10, y+=10, "this mymineral  : " + MyBotModule.Broodwar.self().gatheredMinerals());
	    MyBotModule.Broodwar.drawTextScreen(10, y+=10, "total enemy cnt : " + enemyCommandInfoMap.size());
        MyBotModule.Broodwar.drawTextScreen(10, y+=10, "mygas           : " + MyBotModule.Broodwar.self().gatheredGas());
        MyBotModule.Broodwar.drawTextScreen(10, y+=10, "mywrkcnt(real)  : " + MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_SCV));
        MyBotModule.Broodwar.drawTextScreen(10, y+=10, "frame ==== " + MyBotModule.Broodwar.getFrameCount());
        
        y=drawCalculation(10, y);
        
        
        y+=10;
	    if (enemyCommandInfoMap.size() == 0){
            MyBotModule.Broodwar.drawTextScreen(10, y+=10, "No enemy base info");
        }else{

	        int k =1;
	        int x=10;
	        int temp=y;
            for (Map.Entry<UnitInfo, EnemyCommandInfo> entry : enemyCommandInfoMap.entrySet())
            {
            	y=temp;
                UnitInfo unitInfo = entry.getKey();
                EnemyCommandInfo enemyCommandInfo = entry.getValue();

                if(AttackDecisionMaker.Instance().skipResourceDepot.size() ==0){
                    MyBotModule.Broodwar.drawTextScreen(10, y += 10, "skipped base : no skipped base");
                }else {
                    for (UnitInfo skip : AttackDecisionMaker.Instance().skipResourceDepot) {
                        MyBotModule.Broodwar.drawTextScreen(10, y += 10, "skipped base : " + skip.getLastPosition());
                    }
                }
                
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, k++ + " base"  + unitInfo.getLastPosition() + ", " + enemyCommandInfo.mineralCalculator.getMineralCount());
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "isMainBase      : "  + enemyCommandInfo.isMainBase);
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "has gas         : "  + enemyCommandInfo.gasCalculator.hasGasBuilding());
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "mineral(real)   : " + ((int)enemyCommandInfo.mineralCalculator.getFullCheckMineral()+(int)50));
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "mineral(r+p):   : " + ((int)enemyCommandInfo.uxmineral+(int)50));
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "gas(real)       : " + enemyCommandInfo.gasCalculator.getRealGas());
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "gas(r+p)        : " + enemyCommandInfo.gasCalculator.getGas());
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "fullWorkerFrame : " + enemyCommandInfo.fullWorkerFrame);
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "halfWorkerFrame : " + enemyCommandInfo.halfWorkerFrame);
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "wrkcnt(real)    : " + enemyCommandInfo.workerCounter.realWorkerCount);
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "wrkcnt(r+p)     : " + ((int)enemyCommandInfo.workerCounter.getWorkerCount(enemyCommandInfo.lastFullCheckFrame)));
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "fwrkcnt         : " + ((int)enemyCommandInfo.lastFullCheckWorkerCount));
                MyBotModule.Broodwar.drawTextScreen(x, y+=10, "last full check : " + enemyCommandInfo.lastFullCheckFrame);
                x+=140;
            }

        }
	    
    }
    
    private int drawCalculation(int x, int y) {
    	 y+=10;
    	 MyBotModule.Broodwar.drawTextScreen(x, y+=10, UxColor.CHAR_RED +  "Decision : " + AttackDecisionMaker.Instance().decision + ", phase3: " +  StrategyAnalyseManager.Instance().getPhase() +", strategy" + StrategyIdea.currentStrategy.name());
    	 MyBotModule.Broodwar.drawTextScreen(x, y+=10, "MineralToPredict: " + AttackDecisionMaker.Instance().UXMineralToPredict);
         MyBotModule.Broodwar.drawTextScreen(x, y+=10, "GasToPredict    : " + AttackDecisionMaker.Instance().UXGasToPredict);
         MyBotModule.Broodwar.drawTextScreen(x, y+=10, "MineralMinus:   : " + AttackDecisionMaker.Instance().UXMinusMineralToPredict);
         MyBotModule.Broodwar.drawTextScreen(x, y+=10, "GasMinus        : " + AttackDecisionMaker.Instance().UXMinusGasToPredict);
         
         MyBotModule.Broodwar.drawTextScreen(x, y+=10, "my point        : " + AttackDecisionMaker.Instance().tempMypoint);
         MyBotModule.Broodwar.drawTextScreen(x, y+=10, "enemy point     : " + AttackDecisionMaker.Instance().tempEnemypoint);
         return y;
    }

	private void drawDecision() {
		for (Integer unitId : decisionListForUx.keySet()) {
			Unit unit = MyBotModule.Broodwar.getUnit(unitId);
			MicroDecision decision = decisionListForUx.get(unitId);
			MyBotModule.Broodwar.drawTextMap(unit.getPosition(), UxColor.CHAR_YELLOW + decision.toString());
			if (decision.eui != null) {
				MyBotModule.Broodwar.drawLineMap(unit.getPosition(), decision.eui.getLastPosition(), Color.Yellow);
			}
		}
	}
	
	public void addDecisionListForUx(Unit unit, MicroDecision decision) {
		decisionListForUx.put(unit.getID(), decision);
	}
	
	public void clearDecisionListForUx() {
		decisionListForUx.clear();
	}

	private void drawTimer() {
		char battleColor = UxColor.CHAR_WHITE;
		if (StrategyIdea.initiated) {
			battleColor = UxColor.CHAR_RED;
		}
		MyBotModule.Broodwar.drawTextScreen(170, 353, battleColor + StrategyIdea.mainSquadMode.toString() + ": " + TimeUtils.framesToTimeString(TimeUtils.elapsedFrames()) + "(" + TimeUtils.elapsedFrames() + ")");
		
		char apmColor = UxColor.CHAR_WHITE;
		int apm = MyBotModule.Broodwar.getAPM();
		if (apm > 3000) {
			apmColor = UxColor.CHAR_RED;
		} else if (apm > 2000) {
			apmColor = UxColor.CHAR_YELLOW;
		} else if (apm > 1000) {
			apmColor = UxColor.CHAR_GREEN;
		} else {
			apmColor = UxColor.CHAR_WHITE;
		}
		MyBotModule.Broodwar.drawTextScreen(395, 353, apmColor + "APM : " + MyBotModule.Broodwar.getAPM());
	}
	
	private void drawEnemyBuildTimer() {
		
		Map<UnitType, Integer> buildTimeExpectMap = EnemyBuildTimer.Instance().buildTimeExpectMap;
		Map<UnitType, Integer> buildTimeMinimumMap = EnemyBuildTimer.Instance().buildTimeMinimumMap;
		Set<UnitType> buildTimeCertain = EnemyBuildTimer.Instance().buildTimeCertain;

		int y = 20;
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "engine Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.engineeringBayBuildStartFrame));
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "turret Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.turretBuildStartFrame));
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "turret Need  Frame : " + TimeUtils.framesToTimeString(StrategyIdea.turretNeedFrame));
		y += 15;
		
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "academy Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.academyFrame));
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "comsat Build Frame : " + TimeUtils.framesToTimeString(StrategyIdea.academyFrame + UnitType.Terran_Academy.buildTime()));
		y += 15;
		
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "darkTemplarInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().darkTemplarInMyBaseFrame));
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "reaverInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().reaverInMyBaseFrame));
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "mutaliskInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().mutaliskInMyBaseFrame));
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "lurkerInMyBaseFrame : " + TimeUtils.framesToTimeString(EnemyBuildTimer.Instance().lurkerInMyBaseFrame));
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
				
				MyBotModule.Broodwar.drawTextScreen(20, y += 15, unitType + " : " + expect + " - min: " + minimum + " (" + buildTimeCertain.contains(unitType) + ")");
			}
		}
	}

	private void drawDebugginUxMenu() {
		MyBotModule.Broodwar.drawTextScreen(20, 20, "1. Default Information");
		MyBotModule.Broodwar.drawTextScreen(20, 35, "2. Strategy Information");
		MyBotModule.Broodwar.drawTextScreen(20, 50, "3. Position Finder Test");
		MyBotModule.Broodwar.drawTextScreen(20, 65, "4. Air Micro Test");
		MyBotModule.Broodwar.drawTextScreen(20, 80, "5. Unit Bast To Base");
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
		
		
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Current Strategy : ");
		MyBotModule.Broodwar.drawTextScreen(x + 100, y, "" + UxColor.CHAR_WHITE + StrategyIdea.currentStrategy.name());
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
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Strategy History : ");
		MyBotModule.Broodwar.drawTextScreen(x + 100, y, "" + UxColor.CHAR_WHITE + history);
		y += 11;
		
		int vultureCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Vulture);
		int tankCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
		int goliathCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.ALL, UnitType.Terran_Goliath);
		
		UnitType selected = BuildQueueProvider.Instance().getFactoryUnitSelector().getSelected();
		if (selected != UnitType.None) {
			factorySelected = selected;
		}
		
		
		MyBotModule.Broodwar.drawTextScreen(x + 100, y + 5, UxColor.CHAR_TEAL + "" + vultureCount + "      " + tankCount + "        " + goliathCount);
		MyBotModule.Broodwar.drawTextScreen(x, y, "" + UxColor.CHAR_WHITE + StrategyIdea.factoryRatio + ", selected=" + factorySelected);
		y += 11;
		
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Wraith Count : ");
		MyBotModule.Broodwar.drawTextScreen(x + 75, y, "" + UxColor.CHAR_WHITE + StrategyIdea.wraithCount + " / " + UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Wraith));
		y += 11;
		
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "Valkyrie Count : ");
		MyBotModule.Broodwar.drawTextScreen(x + 75, y, "" + UxColor.CHAR_WHITE + StrategyIdea.valkyrieCount + " / " + UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Valkyrie));
		y += 11;

		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_RED + "MYKillScore : ");
		MyBotModule.Broodwar.drawTextScreen(x + 70, y, "" + UxColor.CHAR_RED + MyBotModule.Broodwar.self().getKillScore());
		y += 11;
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_RED + "MYRazingScore : ");
		MyBotModule.Broodwar.drawTextScreen(x + 85, y, "" + UxColor.CHAR_RED + MyBotModule.Broodwar.self().getRazingScore());
		y += 11;
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_PURPLE + "EnemyKillScore : ");
		MyBotModule.Broodwar.drawTextScreen(x + 85, y, "" + UxColor.CHAR_PURPLE + MyBotModule.Broodwar.enemy().getKillScore());
		y += 11;
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_PURPLE + "EnemyRazingScore : ");
		MyBotModule.Broodwar.drawTextScreen(x + 100, y, "" + UxColor.CHAR_PURPLE + MyBotModule.Broodwar.enemy().getRazingScore());
		y += 11;
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_TEAL + "Reserved Resource : " + ConstructionManager.Instance().getReservedMinerals() + " / " + ConstructionManager.Instance().getReservedGas());
		y += 11;
	}

	/// APM (Action Per Minute) 숫자를 Screen 에 표시합니다
	public void drawAPM(int x, int y) {
		int bwapiAPM = MyBotModule.Broodwar.getAPM();
		MyBotModule.Broodwar.drawTextScreen(x, y, "APM : " + bwapiAPM);
	}

	/// Players 정보를 Screen 에 표시합니다
	public void drawPlayers() {
		for (Player p : MyBotModule.Broodwar.getPlayers()) {
			MyBotModule.Broodwar.sendText("Player [" + p.getID() + "]: " + p.getName() + " is in force: " + p.getForce().getName());
		}
	}

	/// Player 들의 팀 (Force) 들의 정보를 Screen 에 표시합니다
	public void drawForces() {
		for (Force f :  MyBotModule.Broodwar.getForces()) {
			MyBotModule.Broodwar.sendText("Force " + f.getName() + " has the following players:");
			for (Player p : f.getPlayers()) {
				MyBotModule.Broodwar.sendText("  - Player [" + p.getID() + "]: " + p.getName());
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
				if (!MyBotModule.Broodwar.isVisible(ui.getLastPosition().toTilePosition())) {
					MyBotModule.Broodwar.drawBoxMap(new Position(left, top), new Position(right, bottom), Color.Grey, false);
					MyBotModule.Broodwar.drawTextMap(new Position(left + 3, top + 4), ui.getType().toString());
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
	
					MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
					MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), hpColor, true);
					MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);
	
					int ticWidth = 3;
	
					for (int i = left; i < right - 1; i += ticWidth) {
						MyBotModule.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
					}
				}
	
				// 유닛의 Shield 남아있는 비율 표시
				if (!type.isResourceContainer() && type.maxShields() > 0) {
					double shieldRatio = (double)shields / (double)type.maxShields();
	
					int ratioRight = left + (int)((right - left) * shieldRatio);
					int hpTop = top - 3 + verticalOffset;
					int hpBottom = top + 1 + verticalOffset;
	
					MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
					MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), Color.Blue, true);
					MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);
	
					int ticWidth = 3;
	
					for (int i = left; i < right - 1; i += ticWidth) {
						MyBotModule.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
					}
				}
			}
		}

		// draw neutral units and our units
		for (Unit unit : MyBotModule.Broodwar.getAllUnits()) {
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

				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), hpColor, true);
				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), hpColor.Black, false);

				int ticWidth = 3;

				for (int i = left; i < right - 1; i += ticWidth) {
					MyBotModule.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
				}
			}

			// 유닛의 Shield 남아있는 비율 표시
			if (!unit.getType().isResourceContainer() && unit.getType().maxShields() > 0) {
				double shieldRatio = (double)unit.getShields() / (double)unit.getType().maxShields();

				int ratioRight = left + (int)((right - left) * shieldRatio);
				int hpTop = top - 3 + verticalOffset;
				int hpBottom = top + 1 + verticalOffset;

				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), Color.Blue, true);
				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);

				int ticWidth = 3;

				for (int i = left; i < right - 1; i += ticWidth) {
					MyBotModule.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
				}
			}

			// Mineral / Gas 가 얼마나 남아있는가
			if (unit.getType().isResourceContainer() && unit.getInitialResources() > 0) {
				double mineralRatio = (double)unit.getResources() / (double)unit.getInitialResources();

				int ratioRight = left + (int)((right - left) * mineralRatio);
				int hpTop = top + verticalOffset;
				int hpBottom = top + 4 + verticalOffset;

				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey, true);
				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), Color.Cyan, true);
				MyBotModule.Broodwar.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);

				int ticWidth = 3;

				for (int i = left; i < right - 1; i += ticWidth) {
					MyBotModule.Broodwar.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
				}
			}
		}
	}

	/// UnitType 별 통계 정보를 Screen 에 표시합니다
	public void drawUnitStatisticsOnScreen1(int x, int y) {
		int currentY = y;

		// 아군이 입은 피해 누적값
		MyBotModule.Broodwar.drawTextScreen(x, currentY, UxColor.CHAR_WHITE + " Self Loss:" + UxColor.CHAR_WHITE + " Minerals: " + UxColor.CHAR_BROWN + InformationManager.Instance().getUnitData(MyBotModule.Broodwar.self()).getMineralsLost() + UxColor.CHAR_WHITE + " Gas: " + UxColor.CHAR_RED + InformationManager.Instance().getUnitData(MyBotModule.Broodwar.self()).getGasLost());
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
			MyBotModule.Broodwar.drawTextScreen(x, currentY, UxColor.CHAR_BROWN + " Enemy Loss:" + UxColor.CHAR_WHITE +" Minerals: " + UxColor.CHAR_RED + InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getMineralsLost() + UxColor.CHAR_WHITE + " Gas: " + UxColor.CHAR_TEAL + InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer).getGasLost());
		}
	}	
		
	public void drawUnitStatisticsOnScreen2(int x, int y) {
			
		int currentY = y;
		
		// 적군의 UnitType 별 파악된 Unit 숫자를 표시
		MyBotModule.Broodwar.drawTextScreen(x,		 currentY, UxColor.CHAR_WHITE + " UNIT NAME");
		//MyBotModule.Broodwar.drawTextScreen(x + 110, currentY + 20, white + " Created");
		//MyBotModule.Broodwar.drawTextScreen(x + 150, currentY + 20, white + " Dead");
		MyBotModule.Broodwar.drawTextScreen(x + 85, currentY, UxColor.CHAR_WHITE + " Alive");

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
					MyBotModule.Broodwar.drawTextScreen(x,		 currentY + 30 + ((yspace)* 10), displayname);
//					MyBotModule.Broodwar.drawTextScreen(x + 120, currentY + 30 + ((yspace)* 10), "" + numCreatedUnits);
//					MyBotModule.Broodwar.drawTextScreen(x + 160, currentY + 30 + ((yspace)* 10), "" + numDeadUnits);
					MyBotModule.Broodwar.drawTextScreen(x + 110, currentY + 30 + ((yspace)* 10), "" + numUnits);
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
					MyBotModule.Broodwar.drawTextScreen(x,		 currentY + 30 + ((yspace)* 10), displayname);
					//MyBotModule.Broodwar.drawTextScreen(x + 120, currentY + 30 + ((yspace)* 10), "" + numCreatedUnits);
					//MyBotModule.Broodwar.drawTextScreen(x + 160, currentY + 30 + ((yspace)* 10), "" + numDeadUnits);
					MyBotModule.Broodwar.drawTextScreen(x + 110, currentY + 30 + ((yspace)* 10), "" + numUnits);
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
			
		}

		if(hasSavedBWTAInfo)
		{
			for(int i1=0 ; i1<blue.length ; i1++)
			{
				MyBotModule.Broodwar.drawBoxMap(blue[i1][0], blue[i1][1], blue[i1][2], blue[i1][3], Color.Blue);
			}
//			for(int i1=0 ; i1<purple.length ; i1++)
//			{
//				Prebot.Broodwar.drawBoxMap(purple[i1][0], purple[i1][1], purple[i1][2], purple[i1][3], Color.Purple);
//			}
			for(int i2=0 ; i2<cyan.length ; i2++)
			{
				MyBotModule.Broodwar.drawCircleMap(cyan[i2][0], cyan[i2][1], 30, Color.Cyan);	
			}
			for(int i3=0 ; i3<orange.length ; i3++)
			{
				MyBotModule.Broodwar.drawBoxMap(orange[i3][0], orange[i3][1], orange[i3][2], orange[i3][3], Color.Orange);
			}
			for(int i4=0 ; i4<yellow.size() ; i4++)
			{
				MyBotModule.Broodwar.drawCircleMap(yellow.get(i4), 80, Color.Yellow);	
			}
			for(int i5=0 ; i5<green1.size() ; i5++)
			{
				MyBotModule.Broodwar.drawLineMap(green1.get(i5), green2.get(i5), Color.Green);	
			}
			for(int i6=0 ; i6<red1.size() ; i6++)
			{
				MyBotModule.Broodwar.drawLineMap(red1.get(i6), red2.get(i6), Color.Red);	
			}			

			// OccupiedBaseLocation 을 원으로 표시
			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer)) {
				MyBotModule.Broodwar.drawCircleMap(baseLocation.getPosition(), 10 * CommonConfig.UxConfig.TILE_SIZE, Color.Blue);	
			}
			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
				MyBotModule.Broodwar.drawCircleMap(baseLocation.getPosition(), 10 * CommonConfig.UxConfig.TILE_SIZE, Color.Red);	
			}

			// ChokePoint, BaseLocation 을 텍스트로 표시
			if (InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getPosition(), "My MainBaseLocation");
			}
			if (InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getCenter(), "My First ChokePoint");
			}
			if (InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self()) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self()).getCenter(), "My Second ChokePoint");
			}
			if (InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self()) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self()).getPosition(), "My First ExpansionLocation");
			}

			if (InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer).getPosition(), "Enemy MainBaseLocation");
			}
			if (InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer).getCenter(), "Enemy First ChokePoint");
			}
			if (InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer).getCenter(), "Enemy Second ChokePoint");
			}
			if (InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer) != null) {
				MyBotModule.Broodwar.drawTextMap(InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer).getPosition(), "Enemy First ExpansionLocation");
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
			MyBotModule.Broodwar.drawLineMap(i*cellSize, 0, i*cellSize, mapHeight, Color.Blue);
		}

		for (int j = 0; j<rows; j++) {
			MyBotModule.Broodwar.drawLineMap(0, j*cellSize, mapWidth, j*cellSize, Color.Blue);
		}
		
		for (int r = 0; r < rows; r+=2)
		{
			for (int c = 0; c < cols; c+=2)
			{
				MyBotModule.Broodwar.drawTextMap(c * 32, r * 32, c + "," + r);
			}
		}		
	}

	/// BuildOrderQueue 를 Screen 에 표시합니다
	public void drawBuildOrderQueueOnScreen(int x, int y) {
		char initialFinishedColor;
		InitialBuildProvider.AdaptStrategyStatus adaptStrategyStatus = InitialBuildProvider.Instance().getAdaptStrategyStatus();
		if (adaptStrategyStatus == InitialBuildProvider.AdaptStrategyStatus.COMPLETE) {
			initialFinishedColor = UxColor.CHAR_WHITE;
		} else if (adaptStrategyStatus == InitialBuildProvider.AdaptStrategyStatus.PROGRESSING) {
			initialFinishedColor = UxColor.CHAR_YELLOW;
		} else {
			initialFinishedColor = UxColor.CHAR_GREEN;
		}
		
		MyBotModule.Broodwar.drawTextScreen(x, y, initialFinishedColor + " <Build Order>");

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
			MyBotModule.Broodwar.drawTextScreen(x, y + 10 + (itemCount * 10), currentItem.blocking + " " + UxColor.CHAR_WHITE + currentItem.metaType.getName());
			itemCount++;
			if (itemCount >= 24) break;
		}
	}

	/// Build 진행 상태를 Screen 에 표시합니다
	public void drawBuildStatusOnScreen(int x, int y) {
		// 건설 / 훈련 중인 유닛 진행상황 표시
		Vector<Unit> unitsUnderConstruction = new Vector<Unit>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
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

		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + " <Build Status>");

		int reps = unitsUnderConstruction.size() < 10 ? unitsUnderConstruction.size() : 10;

		for (Unit unit : unitsUnderConstruction)
		{
			y += 10;
			UnitType t = unit.getType();
			if (t == UnitType.Zerg_Egg)
			{
				t = unit.getBuildType();
			}

			MyBotModule.Broodwar.drawTextScreen(x, y, "" + UxColor.CHAR_WHITE + t + " (" + unit.getRemainingBuildTime() + ")");
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

						MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Yellow, false);
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
//			MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Orange, false);
//		}
		int y=0;
		int x=0;
		
		
//		for(y = 0; y < 128 ; y++) {
//			for(x = 0; x < 128 ; x++) {
//				if(ConstructionPlaceFinder.Instance().getTilesToAvoid(x, y)) {
//					int x1 = x * 32 + 8;
//					int y1 = y * 32 + 8;
//					int x2 = (x + 1) * 32 - 8;
//					int y2 = (y + 1) * 32 - 8;
//	
//					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Orange, false);
//				}
////				
//			}
//		}
		
		
		for(y = 0; y < 128 ; y++) {
			for(x = 0; x < 128 ; x++) {
				if(ConstructionPlaceFinder.Instance().getTilesToAvoid(x, y)) {
					int x1 = x * 32 + 8;
					int y1 = y * 32 + 8;
					int x2 = (x + 1) * 32 - 8;
					int y2 = (y + 1) * 32 - 8;
	
					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Orange, false);
				}
				if(ConstructionPlaceFinder.Instance().getTilesToAvoidAbsolute(x, y)) {
					int x1 = x * 32 + 8;
					int y1 = y * 32 + 8;
					int x2 = (x + 1) * 32 - 8;
					int y2 = (y + 1) * 32 - 8;
	
					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Purple, false);
				}
				if(ConstructionPlaceFinder.Instance().getTilesToAvoidSupply(x, y)) {
					int x1 = x * 32 + 8;
					int y1 = y * 32 + 8;
					int x2 = (x + 1) * 32 - 8;
					int y2 = (y + 1) * 32 - 8;
	
					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Red, false);
				}
//				if(ConstructionPlaceFinder.Instance().getTilesToAvoidAddonBuilding(x, y)) {
//					int x1 = x * 32 + 8;
//					int y1 = y * 32 + 8;
//					int x2 = (x + 1) * 32 - 8;
//					int y2 = (y + 1) * 32 - 8;
//	
//					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Black, false);
//				}
//				
			}
		}
	}

	/// ConstructionQueue 를 Screen 에 표시합니다
	public void drawConstructionQueueOnScreenAndMap(int x, int y) {
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + " <Construction Status>");

		int yspace = 0;

		Vector<ConstructionTask> constructionQueue = ConstructionManager.Instance().getConstructionQueue();

		for (final ConstructionTask b : constructionQueue)
		{
			String constructionState = "";

			if (b.getStatus() == ConstructionTask.ConstructionStatus.Unassigned.ordinal())
			{
				MyBotModule.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), "" + UxColor.CHAR_WHITE + b.getType() + " - No Worker");
			}
			else if (b.getStatus() == ConstructionTask.ConstructionStatus.Assigned.ordinal())
			{
				if (b.getConstructionWorker() == null) {
					MyBotModule.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), b.getType() + " - Assigned Worker Null");
				}			
				else {
					MyBotModule.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), b.getType() + " - Assigned Worker " + b.getConstructionWorker().getID() + ", Position (" + b.getFinalPosition().getX() + "," + b.getFinalPosition().getY() + ")");
				}

				int x1 = b.getFinalPosition().getX() * 32;
				int y1 = b.getFinalPosition().getY() * 32;
				int x2 = (b.getFinalPosition().getX()+ b.getType().tileWidth()) * 32;
				int y2 = (b.getFinalPosition().getY() + b.getType().tileHeight()) * 32;

				MyBotModule.Broodwar.drawLineMap(b.getConstructionWorker().getPosition().getX(), b.getConstructionWorker().getPosition().getY(), (x1 + x2) / 2, (y1 + y2) / 2, Color.Orange);
				MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Red, false);
			}
			else if (b.getStatus() == ConstructionTask.ConstructionStatus.UnderConstruction.ordinal())
			{
				MyBotModule.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), "" + UxColor.CHAR_WHITE + b.getType() + " - Under Construction");
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
			MyBotModule.Broodwar.drawTextScreen(10, 120, "secondStartPosition: " + secondStartPosition.getTilePosition());
			MyBotModule.Broodwar.drawTextMap(secondStartPosition.getPosition(), "secondStartPosition");
		}else {
			MyBotModule.Broodwar.drawTextScreen(10, 120, "secondStartPosition: null");
		}
		if(getExpansionLocation!= null) {
			MyBotModule.Broodwar.drawTextScreen(10, 130, "getExpansionLocation: " + getExpansionLocation.getTilePosition());
			MyBotModule.Broodwar.drawTextMap(getExpansionLocation.getPosition(), "nextEX");
		}else {
			MyBotModule.Broodwar.drawTextScreen(10, 130, "getExpansionLocation: null");
		}
		if(getLastBuildingLocation!= null) {
			MyBotModule.Broodwar.drawTextScreen(10, 140, "getLastBuildingLocation: " + getLastBuildingLocation);
			MyBotModule.Broodwar.drawTextMap(getLastBuildingLocation.toPosition(), "nextBuild");
		}else {
			MyBotModule.Broodwar.drawTextScreen(10, 140, "getLastBuildingLocation: null");
		}
		if(getLastBuildingFinalLocation!= null) {
			MyBotModule.Broodwar.drawTextScreen(10, 150, "getLastBuildingFinalLocation: " + getLastBuildingFinalLocation);
			MyBotModule.Broodwar.drawTextMap(getLastBuildingFinalLocation.toPosition(), "LastBuild");
		}else {
			MyBotModule.Broodwar.drawTextScreen(10, 150, "getLastBuildingFinalLocation: null");
		}

		
		MyBotModule.Broodwar.drawTextScreen(10, 160, "mainBaseLocationFull: " + BuildManager.Instance().mainBaseLocationFull);
		MyBotModule.Broodwar.drawTextScreen(10, 170, "secondChokePointFull: " + BuildManager.Instance().secondChokePointFull);
		MyBotModule.Broodwar.drawTextScreen(10, 180, "secondStartLocationFull: " + BuildManager.Instance().secondStartLocationFull);
		MyBotModule.Broodwar.drawTextScreen(10, 190, "fisrtSupplePointFull: " + BuildManager.Instance().fisrtSupplePointFull);
		
		MyBotModule.Broodwar.drawTextScreen(10, 200, "myMainbaseLocation : " + InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
		MyBotModule.Broodwar.drawTextScreen(10, 210, "enemyMainbaseLocation : " + InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()).getTilePosition());

	}
	
	
	public void drawMineralIdOnMap() {
        for (Unit unit : MyBotModule.Broodwar.getStaticMinerals()) {
		
			MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
		}
	}
	/// Unit 의 Id 를 Map 에 표시합니다
	public void drawUnitIdOnMap() {
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if(unit.getType().isBuilding()){
				MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
				MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 25, "" + UxColor.CHAR_WHITE + unit.getTilePosition().getX() + " / " + unit.getTilePosition().getY());
			}else{
				MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
			}
			
		}
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY() + 5, "" + UxColor.CHAR_WHITE + unit.getID());
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

		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "<Workers : " + workerData.getNumMineralWorkers() + ">");

		int yspace = 0;

		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;

			// Mineral / Gas / Idle Worker 는 표시 안한다
			if (workerData.getJobCode(unit) == 'M' || workerData.getJobCode(unit) == 'I' || workerData.getJobCode(unit) == 'G') {
				continue;
			}

			MyBotModule.Broodwar.drawTextScreen(x, y + 10 + ((yspace)* 10), UxColor.CHAR_WHITE + " " + unit.getID());

			if (workerData.getJobCode(unit) == 'B') {
				MyBotModule.Broodwar.drawTextScreen(x + 30, y + 10 + ((yspace++) * 10), UxColor.CHAR_WHITE + " " + workerData.getJobCode(unit) + " " + unit.getBuildType() + " " + (unit.isConstructing() ? 'Y' : 'N') + " (" + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY() + ")");
			}
			else {
				MyBotModule.Broodwar.drawTextScreen(x + 30, y + 10 + ((yspace++) * 10), UxColor.CHAR_WHITE + " " + workerData.getJobCode(unit));
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

			MyBotModule.Broodwar.drawBoxMap(x - 2, y - 1, x + 75, y + 14, Color.Black, true);
			MyBotModule.Broodwar.drawTextMap(x, y, UxColor.CHAR_WHITE + " Workers: " + WorkerManager.Instance().getWorkerData().getNumAssignedWorkers(depot));
		}
	}

	/// Worker Unit 의 자원채취 현황을 Map 에 표시합니다
	public void drawWorkerMiningStatusOnMap() {
		WorkerData  workerData = WorkerManager.Instance().getWorkerData();

		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			Position pos = worker.getTargetPosition();

			MyBotModule.Broodwar.drawTextMap(worker.getPosition().getX(), worker.getPosition().getY() - 5, "" + UxColor.CHAR_WHITE + workerData.getJobCode(worker));
			
			MyBotModule.Broodwar.drawLineMap(worker.getPosition().getX(), worker.getPosition().getY(), pos.getX(), pos.getY(), Color.Cyan);

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
			MyBotModule.Broodwar.drawTextScreen(x, y, "Enemy MainBaseLocation : (" + enemyBaseLocation.getTilePosition().getX() + ", " + enemyBaseLocation.getTilePosition().getY() + ")");
		}
		else {
			MyBotModule.Broodwar.drawTextScreen(x, y, "Enemy MainBaseLocation : Unknown");
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
//						Prebot.Broodwar.drawTextScreen(x, y + 20, "Target = (" + scoutMoveTo.getX() / CommonConfig.UxConfig.TILE_SIZE + ", " + scoutMoveTo.getY() / CommonConfig.UxConfig.TILE_SIZE + ") Distance = " + currentScoutTargetDistance);
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
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit != null && unit.isCompleted() && !unit.getType().isBuilding() && !unit.getType().isWorker())
			{
				Unit targetUnit = unit.getTarget();
				if (targetUnit != null && targetUnit.getPlayer() != MyBotModule.Broodwar.self()) {
					MyBotModule.Broodwar.drawCircleMap(unit.getPosition(), dotRadius, Color.Red, true);
					MyBotModule.Broodwar.drawCircleMap(targetUnit.getTargetPosition(), dotRadius, Color.Red, true);
					MyBotModule.Broodwar.drawLineMap(unit.getPosition(), targetUnit.getTargetPosition(), Color.Red);
				}
				else if (unit.isMoving()) {
					MyBotModule.Broodwar.drawCircleMap(unit.getPosition(), dotRadius, Color.Orange, true);
					MyBotModule.Broodwar.drawCircleMap(unit.getTargetPosition(), dotRadius, Color.Orange, true);
					MyBotModule.Broodwar.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.Orange);
				}

			}
		}
	}

	/// Bullet 을 Map 에 표시합니다 <br>
	/// Cloaking Unit 의 Bullet 표시에 쓰입니다
	public void drawBulletsOnMap()
	{
		for (Bullet b : MyBotModule.Broodwar.getBullets())
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
			MyBotModule.Broodwar.drawLineMap(p, new Position(p.getX() + (int)velocityX, p.getY() + (int)velocityY), b.getPlayer() == MyBotModule.Broodwar.self() ? Color.Green : Color.Red);
			if(b.getType() != null)
			{
				MyBotModule.Broodwar.drawTextMap(p, (b.getPlayer() == MyBotModule.Broodwar.self() ? "" + UxColor.CHAR_TEAL : "" + UxColor.CHAR_RED) + bulletTypeName);
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
			
			StrategyCode.SmallFightPredict smallFightPredict = null;
			if (squad instanceof WatcherSquad) {
				smallFightPredict = ((WatcherSquad) squad).getSmallFightPredict();
			}
			
			if (squadName.length() > 4) {
				squadName = squadName.substring(0, 4);
			}

			for (Unit unit : squad.unitList) {
				MyBotModule.Broodwar.drawCircleMap(unit.getPosition(), 10, color);
				MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX() - 20, unit.getPosition().getY() - 30, squadName);
				if (smallFightPredict != null && smallFightPredict == StrategyCode.SmallFightPredict.BACK) {
					MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX() - 20, unit.getPosition().getY() - 15, UxColor.CHAR_RED + smallFightPredict.toString());
				}
			}
			
			Map<Integer, Integer> checkerSiteMap = VultureTravelManager.Instance().getCheckerSiteMap2();
			List<BaseLocation> baseList = VultureTravelManager.Instance().getBaseLocationsCheckerOrdered();
			for (Integer checkerId : checkerSiteMap.keySet()) {
				Unit unit = MyBotModule.Broodwar.getUnit(checkerId);
				if (UnitUtils.isValidUnit(unit)) {
					Integer index = checkerSiteMap.get(checkerId);
					if (index != null) {
						MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX() - 20, unit.getPosition().getY() - 5, UxColor.CHAR_ORANGE + baseList.get(index).getPosition().toString());
					}
					
				}
			}
		}
	}
	
	private void drawSquadInfoOnMap(int x, int y) {
		// TODO Auto-generated method stub
		/// ConstructionQueue 를 Screen 에 표시합니다
		MyBotModule.Broodwar.drawTextScreen(x, y, UxColor.CHAR_WHITE + "<Squad Name>");
		MyBotModule.Broodwar.drawTextScreen(x +110, y, UxColor.CHAR_WHITE + " <Unit Size>");
		
		y += 15;
		MyBotModule.Broodwar.drawTextScreen(x, y, "" + "*" + "SCV");
		MyBotModule.Broodwar.drawTextScreen(x +120, y, "" + UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_SCV));
		y += 10;
		for (Squad squad : CombatManager.Instance().squadData.getSquadMap().values()) {
			Color squadColor = UxColor.SQUAD_COLOR.get(squad.getClass());
			if (squadColor != null) {
				MyBotModule.Broodwar.drawTextScreen(x, y, "" + UxColor.COLOR_TO_CHARACTER.get(squadColor) + squad.getSquadName());
			} else {
				MyBotModule.Broodwar.drawTextScreen(x, y, "" + "*" + squad.getSquadName());
			}
			String unitIds = " ... ";
			for (Unit unit : squad.unitList) {
				unitIds = unitIds + unit.getID() + "/";
			}
			MyBotModule.Broodwar.drawTextScreen(x +120, y, "" + squad.unitList.size() + unitIds);
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
				CombatManager.Instance(),
				AttackDecisionMaker.Instance());
		
		
		int currentY = y;
		for (GameManager gameManager : gameManagers) {
			currentY += 10;
			MyBotModule.Broodwar.drawTextScreen(x, currentY, UxColor.CHAR_PURPLE + gameManager.getClass().getSimpleName());

			char drawColor = UxColor.CHAR_WHITE;
			if (gameManager.getRecorded() > 10L) {
				drawColor = UxColor.CHAR_TEAL;
			} else if (gameManager.getRecorded() > 30L) {
				drawColor = UxColor.CHAR_RED;
			}
			MyBotModule.Broodwar.drawTextScreen(x + 103, currentY, ": " + drawColor + gameManager.getRecorded());
		}

		MyBotModule.Broodwar.drawTextScreen(x, currentY += 15, "* group size: " + LagObserver.groupsize());
		MyBotModule.Broodwar.drawTextScreen(x, currentY += 10, "* manager rotation size: " + LagObserver.managerRotationSize());
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
			MyBotModule.Broodwar.drawTextScreen(10, currentY += 10, UxColor.CHAR_WHITE + tag + " : " + resultTime + " / " + drawColor + recordTime);
		}
	}
	
	private void drawPathData(){
		for (Unit depot : UnitUtils.getUnitList(UnitType.Terran_Command_Center)) {
			List<Minerals> mineralsList = WorkerData.depotMineral.get(depot);
			if (mineralsList == null) { // TODO
				return;
			}
			
			for(Minerals minr : mineralsList){
				if(minr.mineralTrick != null ){
					MyBotModule.Broodwar.drawCircleMap(minr.mineralUnit.getPosition().getX(),minr.mineralUnit.getPosition().getY(),4,Color.Blue,true );
					MyBotModule.Broodwar.drawCircleMap(minr.mineralTrick.getPosition().getX(),minr.mineralTrick.getPosition().getY(),4,Color.Purple,true );
				}
			}


			for(Minerals minr : WorkerData.depotMineral.get(depot)){
				if( minr.posTrick != bwapi.Position.None ){
					MyBotModule.Broodwar.drawCircleMap( minr.posTrick.getX(),minr.posTrick.getY(),4,Color.Red,true );
					MyBotModule.Broodwar.drawCircleMap(minr.mineralUnit.getPosition().getX(),minr.mineralUnit.getPosition().getY(),4,Color.Yellow,true );
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
		
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "[" + strategy.name() + " ...(phase "+ phase + ")]");
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "FAC RATIO : " + StrategyIdea.factoryRatio + ".. (" + UnitUtils.myFactoryUnitSupplyCount() + ")");
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "UPGRADE   : " + upgradeString);
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "MARINE CNT : " + StrategyIdea.marineCount);
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "ADDON     : " + StrategyIdea.addOnOption);
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "EXPANSION : " + StrategyIdea.expansionOption);
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "WRAITH CNT : " + StrategyIdea.wraithCount);
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "VALKYRIE CNT : " + StrategyIdea.valkyrieCount);
		MyBotModule.Broodwar.drawTextScreen(20, y += 12, UxColor.CHAR_YELLOW + "MISSION    : " + strategy.missionTypeList);
		MyBotModule.Broodwar.drawTextScreen(20, y += 20, UxColor.CHAR_YELLOW + "" + strategy.buildTimeMap);
		
		MyBotModule.Broodwar.drawTextScreen(20, 260, "" + UxColor.CHAR_YELLOW + ClueManager.Instance().getClueInfoList());

		y = 10;
		for (EnemyStrategy enemyStrategy : EnemyStrategy.values()) {
			if (enemyStrategy.name().startsWith(enemyRace.toString().toUpperCase())) {
				MyBotModule.Broodwar.drawTextScreen(400, y += 10, "" + UxColor.CHAR_YELLOW + enemyStrategy.name());
			}
		}
	}
	
	private void drawEnemyAirDefenseRange() {
		List<UnitInfo> airDefenseEuiList = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL, UnitUtils.enemyAirDefenseUnitType());
		for (UnitInfo eui : airDefenseEuiList) {
			if (eui.getType() == UnitType.Terran_Bunker) {
				MyBotModule.Broodwar.drawCircleMap(eui.getLastPosition(), MyBotModule.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 96, Color.White);
			} else {
				MyBotModule.Broodwar.drawCircleMap(eui.getLastPosition(), eui.getType().airWeapon().maxRange(), Color.White);
			}
		}
		List<UnitInfo> wraithKillerEuiList = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL, UnitUtils.wraithKillerUnitType());
		for (UnitInfo eui : wraithKillerEuiList) {
			MyBotModule.Broodwar.drawCircleMap(eui.getLastPosition(), eui.getType().airWeapon().maxRange(), Color.Grey);
		}
	}
	
	private void drawAirForceInformation() {
		// wraith moving
		for (Unit unit : UnitUtils.getUnitList(UnitType.Terran_Wraith)) {
			if (unit.isMoving()) {
				MyBotModule.Broodwar.drawCircleMap(unit.getPosition(), dotRadius, Color.Orange, true);
				MyBotModule.Broodwar.drawCircleMap(unit.getTargetPosition(), dotRadius, Color.Orange, true);
				MyBotModule.Broodwar.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.Orange);
			}
		}
		
		// target position
		List<Position> targetPositions = AirForceManager.Instance().getTargetPositions();
		for (int i = 0; i < targetPositions.size(); i++) {
			MyBotModule.Broodwar.drawTextMap(targetPositions.get(i), "position#" + i);
		}
		
		// air force team
		int y = 190;
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
			MyBotModule.Broodwar.drawTextMap(position.getX(), position.getY() - 10, color + "leader#" + airForceTeam.leaderUnit.getID());
			
			Position targetPosition = new Position(airForceTeam.getTargetPosition().getX(), airForceTeam.getTargetPosition().getY() - 10);
			MyBotModule.Broodwar.drawTextMap(targetPosition, UxColor.CHAR_RED + "*" + airForceTeam.leaderUnit.getID());
			MyBotModule.Broodwar.drawTextScreen(20, y += 15, "" + UxColor.CHAR_YELLOW + airForceTeam.toString());
		}
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "Defense Mode? " + AirForceManager.Instance().isAirForceDefenseMode());
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "strike level=" + AirForceManager.Instance().getStrikeLevel());
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "total achievement=" + AirForceManager.Instance().getAchievementEffectiveFrame());
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "accumulated achievement=" + AirForceManager.Instance().getAccumulatedAchievement());
		MyBotModule.Broodwar.drawTextScreen(20, y += 15, "wraith count=" + StrategyIdea.wraithCount);
	}

	private void drawVulturePolicy() {
		int y = 10;
		MyBotModule.Broodwar.drawTextScreen(400, y += 15, "[vulture policy]");
		MyBotModule.Broodwar.drawTextScreen(400, y += 15, "checkerMaxNumber=" + StrategyIdea.checkerMaxNumber);
		MyBotModule.Broodwar.drawTextScreen(400, y += 15, "spiderMineNumberPerPosition=" + StrategyIdea.spiderMineNumberPerPosition);
		MyBotModule.Broodwar.drawTextScreen(400, y += 15, "spiderMineNumberPerGoodPosition=" + StrategyIdea.spiderMineNumberPerGoodPosition);
		MyBotModule.Broodwar.drawTextScreen(400, y += 15, "watcherMinePositionLevel=" + StrategyIdea.watcherMinePositionLevel);
	}
	
	private void drawEnemyBaseToBaseTime() {
		int y = 0;
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "campPosition : " + StrategyIdea.campPosition + " / " + StrategyIdea.campType);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "mainPosition : " + StrategyIdea.mainPosition);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "watcherPosition : " + StrategyIdea.watcherPosition);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "mainSquadCenter : " + StrategyIdea.mainSquadCenter);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "enemyGroundSquadPosition : " + StrategyIdea.nearGroundEnemyPosition + " / " + StrategyIdea.enemyUnitStatus);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "enemyAirSquadPosition : " + StrategyIdea.nearAirEnemyPosition);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "enemyDropEnemyPosition : " + StrategyIdea.dropEnemyPosition);
		
		y += 10;
		Position enemyBasePosition = null;
		Position enemyExpansionPosition = null;
		if (InfoUtils.enemyBase() != null) {
			enemyBasePosition = InfoUtils.enemyBase().getPosition();
			enemyExpansionPosition = InfoUtils.enemyBase().getPosition();
			
		}
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "enemyBase : " + enemyBasePosition);
		MyBotModule.Broodwar.drawTextScreen(10, y += 15, "enemyFirstExpansion : " + enemyExpansionPosition);
		
		if (StrategyIdea.enemyBaseExpected != null) {
			MyBotModule.Broodwar.drawTextScreen(10, y += 15, "enemyBase (Expect) : " + StrategyIdea.enemyBaseExpected.getPosition());
		}
//		for (Entry<UnitType, Integer> unitType : InformationManager.Instance().baseToBaseUnit.entrySet()) {
//			Prebot.Broodwar.drawTextScreen(20, y += 10, "" + UxColor.CHAR_YELLOW + unitType.getKey() + " : " + unitType.getValue());
//		}
	}

	private void drawPositionInformation() {

		if (StrategyIdea.mainSquadLeaderPosition != null) {
			MyBotModule.Broodwar.drawTextMap(PositionUtils.positionAdjsuted(StrategyIdea.mainSquadLeaderPosition, 0, -20), UxColor.CHAR_WHITE + "V");
		}
		if (StrategyIdea.campPosition.equals(StrategyIdea.mainPosition)) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.campPosition, UxColor.CHAR_ORANGE + "camp & main");
		} else {
			if (StrategyIdea.campPosition != null) {
				MyBotModule.Broodwar.drawTextMap(StrategyIdea.campPosition, UxColor.CHAR_YELLOW + "camp");
			}
			if (StrategyIdea.mainPosition != null) {
				MyBotModule.Broodwar.drawTextMap(PositionUtils.positionAdjsuted(StrategyIdea.mainPosition, 0, -10), UxColor.CHAR_RED + "main");
			}
		}
		if (StrategyIdea.campPositionSiege != null) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.campPositionSiege, UxColor.CHAR_YELLOW + "camp (siege)");
		}
		if (StrategyIdea.watcherPosition != null) {
			MyBotModule.Broodwar.drawTextMap(PositionUtils.positionAdjsuted(StrategyIdea.watcherPosition, 0, -20), UxColor.CHAR_BLUE + "watcherPos");
		}
		if (StrategyIdea.mainSquadCenter != null) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.mainSquadCenter, "mainSqCntr");
			MyBotModule.Broodwar.drawCircleMap(StrategyIdea.mainSquadCenter.getX(), StrategyIdea.mainSquadCenter.getY(), StrategyIdea.mainSquadCoverRadius, Color.Cyan);
		}
		if (StrategyIdea.nearGroundEnemyPosition != null) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.nearGroundEnemyPosition, UxColor.CHAR_RED + "nearEnemySq(Ground)");
			MyBotModule.Broodwar.drawCircleMap(StrategyIdea.nearGroundEnemyPosition, 150, Color.Red);
		}
		if (StrategyIdea.nearAirEnemyPosition != null) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.nearAirEnemyPosition, UxColor.CHAR_RED + "nearEnemySq(Air)");
			MyBotModule.Broodwar.drawCircleMap(StrategyIdea.nearAirEnemyPosition, 150, Color.Red);
		}
		if (StrategyIdea.dropEnemyPosition != null) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.dropEnemyPosition, UxColor.CHAR_RED + "dropEnemySq");
			MyBotModule.Broodwar.drawCircleMap(StrategyIdea.dropEnemyPosition, 150, Color.Red);
		}
		if (StrategyIdea.totalEnemyCneterPosition != null) {
			MyBotModule.Broodwar.drawTextMap(StrategyIdea.totalEnemyCneterPosition, "totalEnemySq");
			MyBotModule.Broodwar.drawCircleMap(StrategyIdea.totalEnemyCneterPosition, 250, Color.Red);
		}
		if (InfoUtils.myReadyToPosition() != null) {
			MyBotModule.Broodwar.drawTextMap(InfoUtils.myReadyToPosition(), "myReadyTo");
		}
		if (InfoUtils.enemyReadyToPosition() != null) {
			MyBotModule.Broodwar.drawTextMap(InfoUtils.enemyReadyToPosition(), "enemyReadyTo");
		}
//		if (VultureTravelManager.Instance().getTravelSites() != null) {
//			for (TravelSite site : VultureTravelManager.Instance().getTravelSites()) {
//				MyBotModule.Broodwar.drawTextMap(site.baseLocation.getPosition(), "travel site\n" + site);
//			}
//		}
	}
	
	private void drawCCtoScvCount() {
		
		int y = 100;
		for (Unit depot : UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Command_Center)) {
			// update workerData with the new job
			MyBotModule.Broodwar.drawTextScreen(500 , y,"depot.getID() : " + depot.getID() +  " cnt : " + WorkerData.depotWorkerCount.get(depot.getID()) );
			y += 10;
		}
	}
	
/// turret 건설 지점의 반경 표시
	public void drawTurretMap() 
	{
		BaseLocation myBase = InfoUtils.myBase();
		BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
		Chokepoint myFirstChoke = InfoUtils.myFirstChoke();
		Chokepoint mySecondChoke = InfoUtils.mySecondChoke();
		
		int turretCount = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);
		
		Position firstChokeMainHalf = new Position((myBase.getPosition().getX() + myFirstChoke.getX() * 2) / 3 - 60,
				(myBase.getPosition().getY() + myFirstChoke.getY() * 2) / 3 - 60);
		
		Position firstChokeExpHalf = new Position((myFirstExpansion.getPosition().getX() * 2 + myFirstChoke.getX()) / 3,
				(myFirstExpansion.getPosition().getY() * 2 + myFirstChoke.getY()) / 3);
		
////		Position betweenChoke = new Position((myFirstChoke.getX() * 2 + myFirstChoke.getX()) / 3,
////				(mySecondChoke.getY() * 2 + mySecondChoke.getY()) / 3);
////		

//		MyBotModule.Broodwar.drawTextMap(firstChokeExpHalf.getX() + 20, firstChokeExpHalf.getY() + 10, "(" + (int) (firstChokeExpHalf.getX()) + ", " + (int) (firstChokeExpHalf.getY()) + ")");
//		
//		MyBotModule.Broodwar.drawCircleMap(firstChokeExpHalf, 150, Color.Orange, false);
//		
//		MyBotModule.Broodwar.drawCircleMap(firstChokeExpHalf, 150 + turretCount * 15, Color.Orange, false);
//		
//		MyBotModule.Broodwar.drawTextMap(mySecondChoke.getCenter().getX() + 20, mySecondChoke.getCenter().getY() + 10, "(" + (int) (mySecondChoke.getCenter().getX()) + ", " + (int) (mySecondChoke.getCenter().getY()) + ")");
//		
//		MyBotModule.Broodwar.drawCircleMap(mySecondChoke.getCenter(), 150, Color.Cyan, false);
//		
//		MyBotModule.Broodwar.drawCircleMap(mySecondChoke.getCenter(), 150 + turretCount * 15, Color.Cyan, false);  
//		
//		Position betweenChoke2 = Position.None;
//		
//		if (InformationManager.Instance().getMapSpecificInformation().getMap() == MapSpecificInformation.GameMap.FIGHTING_SPIRITS) {
//			betweenChoke2 = new Position((firstChokeMainHalf.getX() * 4 + mySecondChoke.getX() * 7) / 11,
//			(firstChokeMainHalf.getY() * 4 + mySecondChoke.getY() * 7) / 11);
//		}else {
//			betweenChoke2 = new Position((firstChokeMainHalf.getX() * 3 + mySecondChoke.getX() * 4) / 7,
//			(firstChokeMainHalf.getY() * 4 + mySecondChoke.getY() * 7) / 11);
//		}
//		
////		Position betweenChoke2 = new Position((firstChokeMainHalf.getX() * 4 + mySecondChoke.getX() * 7) / 11,
////				(firstChokeMainHalf.getY() * 4 + mySecondChoke.getY() * 7) / 11);
//		
//		MyBotModule.Broodwar.drawTextMap(betweenChoke2.getX() + 20, betweenChoke2.getY() + 10, "(" + (int) (betweenChoke2.getX()) + ", " + (int) (betweenChoke2.getY()) + ")");
//		
//		MyBotModule.Broodwar.drawCircleMap(betweenChoke2, 120, Color.White, false);
//		
//		MyBotModule.Broodwar.drawCircleMap(betweenChoke2, 120 + turretCount * 15, Color.White, false);
//		
////		radius1 + turretCount * 15
		
	}

}