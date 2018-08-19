package prebot.macro;

import static prebot.macro.EnemyCommandInfo.getMineralPatchesNearDepot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.UnitCache;
import prebot.macro.scorecalculator.GoliathScoreCalculator;
import prebot.macro.scorecalculator.ScienceVesselScoreCalculator;
import prebot.macro.scorecalculator.ScoreCalculator;
import prebot.macro.scorecalculator.TankScoreCalculator;
import prebot.macro.scorecalculator.ValkyrieScoreCalculator;
import prebot.macro.scorecalculator.VultureScoreCalculator;
import prebot.macro.scorecalculator.WraithScoreCalculator;
import prebot.macro.util.EnemyUnitCounter;
import prebot.macro.util.MutableFloat;
import prebot.macro.util.ScoreBoard;
import prebot.macro.util.UnitTypeList;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitData;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class AttackDecisionMaker extends GameManager {

    public Decision decision;
    Boolean foundEnemyMainBase;
    public ScoreBoard scoreBoard;

    private static AttackDecisionMaker instance = new AttackDecisionMaker();
    public static AttackDecisionMaker Instance() {
        return instance;
    }

    public Map<UnitInfo, EnemyCommandInfo> enemyResourceDepotInfoMap;
    public List<UnitInfo> skipResourceDepot;

    public int enemyMineralToPredict;
    public int enemyGasToPredict;

    public Map<UnitType, MutableFloat> predictedTotalEnemyAttackUnit;

    public int tempMypoint=0;
    public int tempEnemypoint=0;
    public boolean tempPhase=false;
    public int UXMineralToPredict;
    public int UXGasToPredict;
    public int UXMinusMineralToPredict;
    public int UXMinusGasToPredict;
    
    public void onStart() {
        this.enemyResourceDepotInfoMap = new HashMap<>();
        this.skipResourceDepot = new ArrayList<>();
        this.enemyMineralToPredict = 0;
        this.enemyGasToPredict = 0;
        this.predictedTotalEnemyAttackUnit = new HashMap<>();
        this.foundEnemyMainBase = false;
        this.decision = Decision.DEFENCE;
        this.scoreBoard = new ScoreBoard();
    }

    public void update() {

    	predictedTotalEnemyAttackUnit.clear();
    	enemyMineralToPredict=0;
    	enemyGasToPredict=0;
        //removeDestroyedDepot(InformationManager.Instance().enemyRace);
        addFakeMainDepot(InformationManager.Instance().enemyRace);
        addNewResourceDepot(InformationManager.Instance().enemyRace);
        updateResources(InformationManager.Instance().enemyRace);

        if(tempPhase = checkPhase3()) {
            summaryResource();

            if (InformationManager.Instance().enemyRace == Race.Terran) {
                predictEnemyUnitTerran();
            } else if (InformationManager.Instance().enemyRace == Race.Protoss) {
                predictEnemyUnitProtoss();
            } else {
                predictEnemyUnitZerg();
            }

            int myForcePoint = calculateMyForce();
            int enemyForcePoint = calculateEnemyForce();
            decision = makeDecision(myForcePoint, enemyForcePoint);
        }else{
            decision = Decision.DEFENCE;
        }
    }

    private boolean checkPhase3() {

//        if(StrategyAnalyseManager.Instance().getPhase() == 3){
//            return true;
//        }
        return true;
    }

    private int calculateMyForce() {

    	float point=0;

        List<ScoreCalculator> scoreCalculatorList = new ArrayList<>();
        
        ScoreCalculator vultureScoreCalculator = new VultureScoreCalculator(scoreBoard, UnitType.Terran_Vulture);
        ScoreCalculator tankScoreCalculator = new TankScoreCalculator(scoreBoard, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
        ScoreCalculator valkyrieScoreCalculator = new ValkyrieScoreCalculator(scoreBoard, UnitType.Terran_Valkyrie);
        ScoreCalculator goliathScoreCalculator = new GoliathScoreCalculator(scoreBoard, UnitType.Terran_Goliath);
        ScoreCalculator wraithScoreCalculator = new WraithScoreCalculator(scoreBoard, UnitType.Terran_Wraith);
        ScoreCalculator scienceVesselScoreCalculator = new ScienceVesselScoreCalculator(scoreBoard, UnitType.Terran_Science_Vessel);
        
        scoreCalculatorList.add(vultureScoreCalculator);
        scoreCalculatorList.add(tankScoreCalculator);
        scoreCalculatorList.add(valkyrieScoreCalculator);
        scoreCalculatorList.add(goliathScoreCalculator);
        scoreCalculatorList.add(wraithScoreCalculator);
        scoreCalculatorList.add(scienceVesselScoreCalculator);
        
        for(ScoreCalculator scoreCalculator : scoreCalculatorList) {
        	point += scoreCalculator.getPoint();
        }
        
        tempMypoint=0;
        return tempMypoint=(int) point;
        //내 유닛 수치화
    }

    private int calculateEnemyForce() {

        int point=0;

        if(Prebot.Broodwar.getFrameCount() % 500 ==0) {
//        System.out.println(predictedTotalEnemyAttackUnit);
        }
        for (Map.Entry<UnitType, MutableFloat> enemyUnit : predictedTotalEnemyAttackUnit.entrySet()){
            point += (enemyUnit.getValue().get() * scoreBoard.getPoint(enemyUnit.getKey()));
        }
        tempEnemypoint=0;
        return tempEnemypoint=point;
    }

    private Decision makeDecision(int myForcePoint, int enemyForcePoint) {
    	if (!detectorPrepared()) {
    		return Decision.DEFENCE;
    	}
    	
    	if(myForcePoint > 1200) {
			if (myForcePoint > enemyForcePoint) {
				// TODO 노머시 포인트 조절. 테테전은 타종족의 2배로 좀 여유있게 함
				int noMercyPoint = enemyForcePoint + 2000;
				if (InfoUtils.enemyRace() == Race.Terran) {
					noMercyPoint *= 2;
				}
				if (decision == Decision.NO_MERCY_ATTACK) {
					noMercyPoint = enemyForcePoint;
				}
				
				if (myForcePoint > noMercyPoint) {
					if (decision != Decision.NO_MERCY_ATTACK) {
						Prebot.Broodwar.sendText("NO MERCY ATTACK@@@ GOGOGO@@@@");
					}
					return Decision.NO_MERCY_ATTACK;
				}
				
				if (decision != Decision.FULL_ATTACK) {
					Prebot.Broodwar.sendText("FULL ATTACK@@@ GOGOGO@@@@");
				}
				return Decision.FULL_ATTACK;
	        }
    	}
    	
    	if (quickAttack()) {
    		return Decision.FULL_ATTACK;
    	}
		if (decision == Decision.FULL_ATTACK) {
			Prebot.Broodwar.sendText("BACK TO DEFENCE JOTBAB IN JUNG@@@@");
		}
        return Decision.DEFENCE;
    }

    private boolean detectorPrepared() {
    	if (InfoUtils.enemyRace() == Race.Terran) {
    		return true;
    	} else {
    		if (UnitUtils.invisibleEnemyDiscovered()) {
        		if (UnitUtils.availableScanningCount() == 0 && UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Science_Vessel) == 0) {
        			return false;
        		}
        	}
    	}
    	return true;
	}
    
    private boolean quickAttack() {
    	if (InfoUtils.enemyRace() == Race.Terran) {
			int myTankSupplyCount = UnitUtils.myUnitSupplyCount(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
			return myTankSupplyCount >= 4 * 4;
			
		} else if (InfoUtils.enemyRace() == Race.Protoss) {
			if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.QUICK_ATTACK)) {
				return true;
			}
		}
    	return false;
	}


	private void predictEnemyUnitZerg() {

        EnemyStrategy strategyToApply = StrategyIdea.currentStrategy;

//        ZERG_GROUND3 히드라, 럴커, 뮤탈 1 : 1 : 0
//        ZERG_GROUND2 히드라, 럴커, 뮤탈 3 : 2 : 1
//        ZERG_GROUND1 히드라, 럴커, 뮤탈 2 : 1 : 1
//        ZERG_MIXED      히드라, 럴커, 뮤탈 1 : 0 : 2
//        ZERG_AIR1        히드라, 럴커, 뮤탈 1 : 0 : 5
//        ZERG_AIR2        히드라, 럴커, 뮤탈 1 : 0 : 9
        EnemyUnitCounter enemyUnitCounter = new EnemyUnitCounter();

        if(strategyToApply == EnemyStrategy.ZERG_GROUND3){
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 1);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 1);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 0);
        }else if(strategyToApply == EnemyStrategy.ZERG_GROUND2){
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 3);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 2);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 1);
        }else if(strategyToApply == EnemyStrategy.ZERG_GROUND1){
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 2);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 1);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 1);
        }else if(strategyToApply == EnemyStrategy.ZERG_MIXED){
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 1);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 0);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 2);
        }else if(strategyToApply == EnemyStrategy.ZERG_AIR1){
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 1);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 0);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 5);
        }else if(strategyToApply == EnemyStrategy.ZERG_AIR2){
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 1);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 0);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 9);
        }else {
            enemyUnitCounter.add(UnitType.Zerg_Hydralisk, 2);
            enemyUnitCounter.add(UnitType.Zerg_Lurker, 1);
            enemyUnitCounter.add(UnitType.Zerg_Mutalisk, 1);
        }

        analyzeSet(enemyUnitCounter);

        analyzeRemainMineral(UnitType.Zerg_Zergling);
    }

    private void predictEnemyUnitProtoss() {
        EnemyStrategy strategyToApply = StrategyIdea.currentStrategy;

        EnemyUnitCounter enemyUnitCounter = new EnemyUnitCounter();

        if(strategyToApply == EnemyStrategy.PROTOSS_GROUND){
            enemyUnitCounter.add(UnitType.Protoss_Dragoon, 5);
            enemyUnitCounter.add(UnitType.Protoss_Dark_Templar, 1);
            enemyUnitCounter.add(UnitType.Protoss_Arbiter, 0);
            enemyUnitCounter.add(UnitType.Protoss_Carrier, 0);
        }else if(strategyToApply == EnemyStrategy.PROTOSS_PROTOSS_AIR1){
        	enemyUnitCounter.add(UnitType.Protoss_Dragoon, 8);
            enemyUnitCounter.add(UnitType.Protoss_Dark_Templar, 0);
            enemyUnitCounter.add(UnitType.Protoss_Arbiter, 1);
            enemyUnitCounter.add(UnitType.Protoss_Carrier, 0);
        }else if(strategyToApply == EnemyStrategy.PROTOSS_PROTOSS_AIR2){
        	enemyUnitCounter.add(UnitType.Protoss_Dragoon, 4);
            enemyUnitCounter.add(UnitType.Protoss_Dark_Templar, 0);
            enemyUnitCounter.add(UnitType.Protoss_Arbiter, 0);
            enemyUnitCounter.add(UnitType.Protoss_Carrier, 1);
        }else if(strategyToApply == EnemyStrategy.PROTOSS_PROTOSS_AIR3){
        	enemyUnitCounter.add(UnitType.Protoss_Dragoon, 1);
            enemyUnitCounter.add(UnitType.Protoss_Dark_Templar, 0);
            enemyUnitCounter.add(UnitType.Protoss_Arbiter, 0);
            enemyUnitCounter.add(UnitType.Protoss_Carrier, 1);
        }else {
        	enemyUnitCounter.add(UnitType.Protoss_Dragoon, 5);
            enemyUnitCounter.add(UnitType.Protoss_Dark_Templar, 1);
            enemyUnitCounter.add(UnitType.Protoss_Arbiter, 0);
            enemyUnitCounter.add(UnitType.Protoss_Carrier, 0);
        }

        analyzeSet(enemyUnitCounter);

        analyzeRemainMineral(UnitType.Protoss_Zealot);
    }

    private void predictEnemyUnitTerran() {
        EnemyStrategy strategyToApply = StrategyIdea.currentStrategy;

        EnemyUnitCounter enemyUnitCounter = new EnemyUnitCounter();

        if(strategyToApply == EnemyStrategy.TERRAN_MECHANIC_VULTURE_TANK){
            enemyUnitCounter.add(UnitType.Terran_Siege_Tank_Tank_Mode, 5);
            enemyUnitCounter.add(UnitType.Terran_Goliath, 1);
            enemyUnitCounter.add(UnitType.Terran_Wraith, 0);
        }else if(strategyToApply == EnemyStrategy.TERRAN_MECHANIC_GOLIATH_TANK){
        	enemyUnitCounter.add(UnitType.Terran_Siege_Tank_Tank_Mode, 5);
            enemyUnitCounter.add(UnitType.Terran_Goliath, 1);
            enemyUnitCounter.add(UnitType.Terran_Wraith, 1);
        }else if(strategyToApply == EnemyStrategy.TERRAN_MECHANIC_GOL_GOL_TANK){
        	enemyUnitCounter.add(UnitType.Terran_Siege_Tank_Tank_Mode, 3);
            enemyUnitCounter.add(UnitType.Terran_Goliath, 0);
            enemyUnitCounter.add(UnitType.Terran_Wraith, 5);
        }else {
        	enemyUnitCounter.add(UnitType.Terran_Siege_Tank_Tank_Mode, 5);
            enemyUnitCounter.add(UnitType.Terran_Goliath, 1);
            enemyUnitCounter.add(UnitType.Terran_Wraith, 1);
        }

//        if(Prebot.Broodwar.getFrameCount() % 100 ==0) {
//        System.out.println(enemyUnitCounter.getMineralSet() +", " + enemyUnitCounter.getGasSet());
//        
//        }
        
        analyzeSet(enemyUnitCounter);
        analyzeRemainMineral(UnitType.Terran_Vulture);
    }

    private void analyzeSet(EnemyUnitCounter enemyUnitCounter) {
    	float setCount=0;
        	
    	float setByMineral = ((float)enemyMineralToPredict/(float)enemyUnitCounter.getMineralSet());
    	float setByGas = ((float)enemyGasToPredict/(float)enemyUnitCounter.getGasSet());
        
    	if(setByGas > setByMineral) {
    		setCount = setByMineral;
    	}else {
    		setCount = setByGas;
    	}
	
        if(Prebot.Broodwar.getFrameCount() % 100 ==0) {
        //System.out.println("counting: " + setCount);
        }
        enemyMineralToPredict -= enemyUnitCounter.getMineralSet()*setCount;
        enemyGasToPredict -= enemyUnitCounter.getGasSet()*setCount;
            
        for (Map.Entry<UnitType, Integer> enemyUnit : enemyUnitCounter.getUnitTypes().entrySet()){
            MutableFloat count = predictedTotalEnemyAttackUnit.get(enemyUnit.getKey());
            if (count == null) {
                predictedTotalEnemyAttackUnit.put(enemyUnit.getKey(), new MutableFloat(enemyUnit.getValue() * setCount));
                if(Prebot.Broodwar.getFrameCount() % 100 ==0) {
                //System.out.println("increase map: " + enemyUnit.getKey() + ", " + enemyUnit.getValue() * setCount);
                }
            }else {
                count.increment(enemyUnit.getValue() * setCount);
                if(Prebot.Broodwar.getFrameCount() % 100 ==0) {
                //System.out.println("increase map: " + enemyUnit.getKey() + ", " + enemyUnit.getValue() * setCount);
                }
            }
        }
    }
    private void analyzeRemainMineral(UnitType unitType) {
        int baseUnitCount=0;

        while(true){
            if(enemyMineralToPredict >= unitType.mineralPrice()){
                baseUnitCount++;
                enemyMineralToPredict -= unitType.mineralPrice();
            }else{
                break;
            }
        }

        MutableFloat count = predictedTotalEnemyAttackUnit.get(unitType);
        int d = 1;
        if(unitType == UnitType.Zerg_Zergling){
            d = 2;
        }
        if (count == null) {
            predictedTotalEnemyAttackUnit.put(unitType, new MutableFloat(d * baseUnitCount));
            if(Prebot.Broodwar.getFrameCount() % 100 ==0) {
            //System.out.println("increase map: " + unitType + ", " + d * baseUnitCount);
            }
        }else {
            count.increment(d * baseUnitCount);
            if(Prebot.Broodwar.getFrameCount() % 100 ==0) {
            //System.out.println("increase map: " + unitType + ", " + d * baseUnitCount);  
            }
        }
    }



    private void summaryResource() {
        int enemyMineralToCalculateCombatUnit = 200; //4workers
        int enemyGasToCalculateCombatUnit = 0;

        //Initial resource
        if (InformationManager.Instance().enemyRace == Race.Terran) {
            enemyMineralToCalculateCombatUnit += 400;
        } else if (InformationManager.Instance().enemyRace == Race.Protoss) {
            enemyMineralToCalculateCombatUnit += 400;
        } else{
            enemyMineralToCalculateCombatUnit += 300;
        }
        
        //TODO 존나 빠른 앞마당 저그 같은 경우는? 일단 상대 본진에서 앞마당 못 찾으면...
        if(enemyResourceDepotInfoMap.size() == 0) {
        	enemyMineralToCalculateCombatUnit += Prebot.Broodwar.self().gatheredMinerals() * 0.97;
	        enemyGasToCalculateCombatUnit += Prebot.Broodwar.self().gatheredGas() * 0.97;
        }else {
		    for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){
		
		        EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();
		        enemyMineralToCalculateCombatUnit += enemyCommandInfo.getMineral();
		        enemyGasToCalculateCombatUnit += enemyCommandInfo.getGas();
		        enemyMineralToCalculateCombatUnit += 50;
		    }
        }

        UXMinusMineralToPredict = enemyMineralToCalculateCombatUnit;
        UXMinusGasToPredict = enemyGasToCalculateCombatUnit;
        for(UnitType enemyUnitType : UnitTypeList.getAllType()){

            //confirmedUnit
            int unitCount = UnitCache.getCurrentCache().enemyAllCount(enemyUnitType);
            int killedUnitCount = Prebot.Broodwar.self().killedUnitCount(enemyUnitType);

            if(!enemyUnitType.isBuilding() && !enemyUnitType.isWorker() && unitCount>0) {

            	if(enemyUnitType.canAttack()) {
	                MutableFloat count = predictedTotalEnemyAttackUnit.get(enemyUnitType);
	                if (count == null) {
	                    predictedTotalEnemyAttackUnit.put(enemyUnitType, new MutableFloat(unitCount));
	                }else {
	                    count.increment(unitCount);
	                }
            	}
            }

            killedUnitCount += unitCount;

           
            if (InformationManager.Instance().enemyRace == Race.Terran) {

            } else if (InformationManager.Instance().enemyRace == Race.Protoss) {
            	if(enemyUnitType == UnitType.Protoss_Archon){
                	enemyMineralToCalculateCombatUnit -= 100*killedUnitCount;
                	enemyGasToCalculateCombatUnit -= 300*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Protoss_Dark_Archon){
                	enemyMineralToCalculateCombatUnit -= 300*killedUnitCount;
                	enemyGasToCalculateCombatUnit -= 200*killedUnitCount;
                }

            } else {
            	if(enemyUnitType == UnitType.Zerg_Lair){
     	            enemyMineralToCalculateCombatUnit -= 300*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Hive){
                	enemyMineralToCalculateCombatUnit -= 450*killedUnitCount;
     	            enemyGasToCalculateCombatUnit -= 100*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Greater_Spire){
                    enemyMineralToCalculateCombatUnit -= 200*killedUnitCount;
                    enemyGasToCalculateCombatUnit -= 150*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Sunken_Colony || enemyUnitType == UnitType.Zerg_Spore_Colony){
                    enemyMineralToCalculateCombatUnit -= 75*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Egg){
                	enemyMineralToCalculateCombatUnit -= 50*killedUnitCount;
                	enemyMineralToCalculateCombatUnit += 1*killedUnitCount;
                	enemyGasToCalculateCombatUnit += 1*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Lurker_Egg){
                	enemyMineralToCalculateCombatUnit -= 125*killedUnitCount;
                	enemyGasToCalculateCombatUnit -= 125*killedUnitCount;
                	enemyMineralToCalculateCombatUnit += 1*killedUnitCount;
                	enemyGasToCalculateCombatUnit += 1*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Lurker){
                	enemyMineralToCalculateCombatUnit -= 75*killedUnitCount;
                	enemyGasToCalculateCombatUnit -= 25*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Guardian || enemyUnitType == UnitType.Zerg_Devourer){
                	enemyMineralToCalculateCombatUnit -= 100*killedUnitCount;
                	enemyGasToCalculateCombatUnit -= 100*killedUnitCount;
                }
                if(enemyUnitType == UnitType.Zerg_Cocoon){
                    enemyMineralToCalculateCombatUnit -= 100*killedUnitCount;
                    enemyGasToCalculateCombatUnit -= 80*killedUnitCount;
                    enemyMineralToCalculateCombatUnit += 1*killedUnitCount;
                    enemyGasToCalculateCombatUnit += 1*killedUnitCount;
                }
                if(enemyUnitType.isBuilding()){
                    enemyMineralToCalculateCombatUnit -= 50*killedUnitCount;
                }
            }
            
            if(enemyUnitType == UnitType.Zerg_Zergling || enemyUnitType == UnitType.Zerg_Scourge || enemyUnitType == UnitType.Zerg_Nydus_Canal){
            	enemyMineralToCalculateCombatUnit -= enemyUnitType.mineralPrice()/2*killedUnitCount;
	            enemyGasToCalculateCombatUnit -= enemyUnitType.gasPrice()/2*killedUnitCount;
            }else {
            	if(Prebot.Broodwar.getFrameCount() % 1000 ==0)
            	if(killedUnitCount > 0 ) {
//            		System.out.println(enemyUnitType +": " + killedUnitCount);
            	}
            	
	            enemyMineralToCalculateCombatUnit -= enemyUnitType.mineralPrice()*killedUnitCount;
	            enemyGasToCalculateCombatUnit -= enemyUnitType.gasPrice()*killedUnitCount;
            }
        }
        UXMinusMineralToPredict = UXMinusMineralToPredict - enemyMineralToCalculateCombatUnit;
        UXMinusGasToPredict = UXMinusGasToPredict - enemyGasToCalculateCombatUnit;
        
        
        if(enemyMineralToCalculateCombatUnit < 0) {
        	enemyMineralToCalculateCombatUnit = 0;
        }
        if(enemyGasToCalculateCombatUnit < 0) {
        	enemyGasToCalculateCombatUnit = 0;
        }
        
        UXMineralToPredict=(int) (enemyMineralToCalculateCombatUnit*0.75);
        UXGasToPredict=(int) (enemyGasToCalculateCombatUnit*0.75);
        
        
        
        enemyMineralToPredict = (int) (enemyMineralToCalculateCombatUnit*0.75);
        enemyGasToPredict = (int) (enemyGasToCalculateCombatUnit*0.75);
    }


    private void addFakeMainDepot(Race enemyRace) {

    	if(foundEnemyMainBase) {
    		return;
    	}
    	
    	for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

            EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();

           if(enemyCommandInfo.isMainBase) {
        	   return;
           }
        }
    	
    	BaseLocation enemyMainBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		
    	if(enemyMainBase == null) {
    		return;
    	}
    	
    	UnitInfo falseDepot = new UnitInfo();
    	falseDepot.setType(InformationManager.Instance().getBasicResourceDepotBuildingType(InformationManager.Instance().enemyRace));
    	falseDepot.setPlayer(InformationManager.Instance().enemyPlayer);
    	falseDepot.setLastPosition(enemyMainBase.getPosition());
    	falseDepot.setCompleted(true);
    	falseDepot.setUpdateFrame(0);
    	falseDepot.setRemainingBuildTime(0);
    	
    	System.out.println("add false: " + falseDepot.getLastPosition());
    	
        EnemyCommandInfo enemyCommandInfo = new EnemyCommandInfo(falseDepot, true);
        enemyCommandInfo.setLastCheckFrame(0);
        enemyResourceDepotInfoMap.put(falseDepot, enemyCommandInfo);
	}

    
    private void addNewResourceDepot(Race race) {

        //List<UnitInfo> enemyResourceDepot = UnitCache.getCurrentCache().enemyAllUnitInfos(InformationManager.Instance().getBasicResourceDepotBuildingType(race));
        UnitData unitData = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer);
        List<UnitInfo> enemyResourceDepot = new ArrayList<>();
        
        Iterator<Integer> it = null;
		if (unitData != null) {
			it = unitData.getUnitAndUnitInfoMap().keySet().iterator();

			while (it.hasNext()) {
				final UnitInfo ui = unitData.getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType() == InformationManager.Instance().getBasicResourceDepotBuildingType(race)) {

					enemyResourceDepot.add(ui);
				}
			}
		}
        
        
        
        List<Position> resourceDepotPosition = new ArrayList<>();
        
        boolean isMainBase = false;
        UnitInfo selectedUnitInfo = null;
        UnitInfo selectedUnitInfo2 = null;
        for(UnitInfo unitInfo : enemyResourceDepot){

        	if(!enemyResourceDepotInfoMap.containsKey(unitInfo)){
//                if(skipResourceDepot.contains(unitInfo)){
//                	System.out.println("skipped by skipper : " + unitInfo.getLastPosition());
//                    continue;
//                }

                if(getMineralPatchesNearDepot(unitInfo.getLastPosition()).size() > 6 ){
                	
                	//TODO 여기서 판단해서. 정말 본진을 찾은건지... 아니면 멀티를 찾은건지 구분하고. 본진을 찾았으면 그냥 진행하면 되겠지? 멀티를 찾고. 본진 못 찾았다면. 파악됬다고 하더라도 공격하면 안된다. 처리하자.
                	
                	System.out.println("add : " + unitInfo + ", " + unitInfo.getLastPosition());
                	
                	BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
                    
                	if(enemyBaseLocation !=null)
                	isMainBase = PositionUtils.equals(enemyBaseLocation.getPosition(), unitInfo.getLastPosition());
                    
                	selectedUnitInfo = unitInfo;
                    resourceDepotPosition.add(unitInfo.getLastPosition());
                    
                    break;
                }
            }else {
            	//For moving CommandCenter
            	if(InformationManager.Instance().enemyRace == Race.Terran && Prebot.Broodwar.getFrameCount()>30000 && !resourceDepotPosition.contains(unitInfo.getLastPosition())){
            		if(!unitInfo.getUnit().isFlying() && getMineralPatchesNearDepot(unitInfo.getLastPosition()).size() > 6 ){
            			selectedUnitInfo2 = unitInfo;
                    }
            	}
            }
        }
        
        if(selectedUnitInfo != null) {
	        if(isMainBase) {
	        	System.out.println("found real main: " + selectedUnitInfo.getLastPosition());
	        	foundEnemyMainBase = true;
	        }
	    	
	        EnemyCommandInfo enemyCommandInfo;
	        if(isMainBase) {
	        	enemyCommandInfo = removefake(selectedUnitInfo);
	        }else {
	        	enemyCommandInfo = new EnemyCommandInfo(selectedUnitInfo, isMainBase);
	        }
	        enemyResourceDepotInfoMap.put(selectedUnitInfo, enemyCommandInfo);
        }
        if(selectedUnitInfo2 != null) {
        	EnemyCommandInfo enemyCommandInfo = new EnemyCommandInfo(selectedUnitInfo2, false);
        	enemyResourceDepotInfoMap.remove(selectedUnitInfo2);
            enemyResourceDepotInfoMap.put(selectedUnitInfo2, enemyCommandInfo);
            resourceDepotPosition.add(selectedUnitInfo2.getLastPosition());
        }
    }

    private EnemyCommandInfo removefake(UnitInfo unitInfo) {
    	EnemyCommandInfo preserve=null;
    	UnitInfo selectedUnitInfo=null;
    	for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

           EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();

           if(enemyCommandInfo.isMainBase) {
        	   System.out.println("removing fake!: " + enemyResourceDepot.getKey().getLastPosition());
        	   preserve = enemyResourceDepot.getValue();
        	   preserve.setUnitInfo(unitInfo);
        	   selectedUnitInfo = enemyResourceDepot.getKey();
        	   break;
           }
        }
    	
    	if(selectedUnitInfo!=null) {
    		enemyResourceDepotInfoMap.remove(selectedUnitInfo);
    	}
    	
    	return preserve;
	}

	public void removeDestroyedDepot(Unit unit) {
		
		UnitInfo key = null;
		for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

            UnitInfo unitInfo = enemyResourceDepot.getKey();
            if(unitInfo.getUnit() == unit) {
            	key = unitInfo;
            	break;
            }
        }
		if(key!= null) {
			enemyResourceDepotInfoMap.remove(key);
		}
	}
		
	private void updateResources(Race race) {

        for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

            UnitInfo unitInfo = enemyResourceDepot.getKey();
            if(!unitInfo.isCompleted()){
            	continue;
            }
            EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();

//            if(unitInfo.getUnit().isVisible()) {
//                enemyCommandInfo.setLastCheckFrame(Prebot.Broodwar.getFrameCount());
//            }

            if(enemyCommandInfo==null) {
            	System.out.println("should not aeofhawoefaowe @@@@@@@@@@@@@@@@@@@@@");
            }else {
            	enemyCommandInfo.updateInfo();
            }
        }
    }
}
