package prebot.macro;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.UnitCache;
import prebot.macro.util.MutableInt;
import prebot.macro.util.UnitTypeList;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static prebot.macro.EnemyCommandInfo.getMineralPatchesNearDepot;

public class AttackDecisionMaker extends GameManager {

    public Decision decision;
    Boolean foundEnemyMainBase;

    private static AttackDecisionMaker instance = new AttackDecisionMaker();
    public static AttackDecisionMaker Instance() {
        return instance;
    }

    public Map<UnitInfo, EnemyCommandInfo> enemyResourceDepotInfoMap;
    public List<UnitInfo> skipResourceDepot;

    public int enemyMineralToPredict;
    public int enemyGasToPredict;

    public Map<UnitType, MutableInt> predictedTotalEnemyAttackUnit;

    public void onStart() {
        this.enemyResourceDepotInfoMap = new HashMap<>();
        this.skipResourceDepot = new ArrayList<>();
        this.enemyMineralToPredict = 0;
        this.enemyGasToPredict = 0;
        this.predictedTotalEnemyAttackUnit = new HashMap<>();
        this.foundEnemyMainBase = false;
    }

    public void update() {
        this.decision = Decision.DEFENCE;

        //removeDestroyedDepot(InformationManager.Instance().enemyRace);
        addFakeMainDepot(InformationManager.Instance().enemyRace);
        addNewResourceDepot(InformationManager.Instance().enemyRace);
        updateResources(InformationManager.Instance().enemyRace);

        summaryResource();

//        predictEnemyUnit();
        int myForcePoint = calculateMyForce();
        int enemyForcePoint = calculateEnemyForce();
        decision = makeDecision(myForcePoint, enemyForcePoint);
    }

	private int calculateMyForce() {

        int point=0;

        int vulture = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Vulture);
        int tank = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
        int valkyrie = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Valkyrie);
        int goliath = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Goliath);

        return point;
        //내 유닛 수치화
    }

    private int calculateEnemyForce() {

        int point=0;

        int vulture = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Vulture);
        int tank = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
        int valkyrie = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Valkyrie);
        int goliath = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Goliath);

        return point;
        //내 유닛 수치화
    }

    private Decision makeDecision(int myForcePoint, int enemyForcePoint) {

        //상대가 다크나, 마인이 있는데 공격 판단하면 안된다. 어덯게 할지?
        if(myForcePoint > enemyForcePoint){
            return Decision.FULL_ATTACK;
        }else{
            return Decision.DEFENCE;
        }
    }

    private void predictEnemyUnit() {


        //predictedTotalEnemy 에 실제 본 상대 유닛 들어가 있음.
//        enemyMineralToPredict;
//        enemyGasToPredict;
        // 두개 자원을 가지고 상대의 추가 추정 유닛들을 찾아서 predictedTotalEnemy에 넣기

        //predictedTotalEnemy 의 수치화
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
		    }
        }


        //killedUnit + unitCount
        for(UnitType enemyUnitType : UnitTypeList.getAllType()){

            //confirmedUnit
            int unitCount = UnitCache.getCurrentCache().enemyAllCount(enemyUnitType);
            int killedUnitCount = Prebot.Broodwar.self().killedUnitCount(enemyUnitType);

            if(!enemyUnitType.isBuilding() && !enemyUnitType.isWorker() && enemyUnitType != UnitType.Zerg_Overlord) {

                MutableInt count = predictedTotalEnemyAttackUnit.get(enemyUnitType);
                if (count == null) {
                    predictedTotalEnemyAttackUnit.put(enemyUnitType, new MutableInt(unitCount));
                }else {
                    count.increment(unitCount);
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
	            enemyMineralToCalculateCombatUnit -= enemyUnitType.mineralPrice()*killedUnitCount;
	            enemyGasToCalculateCombatUnit -= enemyUnitType.gasPrice()*killedUnitCount;
            }
        }
        enemyMineralToPredict = enemyMineralToCalculateCombatUnit;
        enemyGasToPredict = enemyGasToCalculateCombatUnit;
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

        List<UnitInfo> enemyResourceDepot = UnitCache.getCurrentCache().enemyAllUnitInfos(InformationManager.Instance().getBasicResourceDepotBuildingType(race));
        List<Position> resourceDepotPosition = new ArrayList<>();
        
        for(UnitInfo unitInfo : enemyResourceDepot){

        	if(!enemyResourceDepotInfoMap.containsKey(unitInfo)){
                if(skipResourceDepot.contains(unitInfo)){
                	System.out.println("skipped by skipper : " + unitInfo.getLastPosition());
                    continue;
                }

                if(getMineralPatchesNearDepot(unitInfo.getLastPosition()).size() > 6 ){
                	
                	//TODO 여기서 판단해서. 정말 본진을 찾은건지... 아니면 멀티를 찾은건지 구분하고. 본진을 찾았으면 그냥 진행하면 되겠지? 멀티를 찾고. 본진 못 찾았다면. 파악됬다고 하더라도 공격하면 안된다. 처리하자.
                	
                	System.out.println("add : " + unitInfo.getLastPosition());
                	
                	BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
                    
                	boolean isMainBase = PositionUtils.equals(enemyBaseLocation.getPosition(), unitInfo.getLastPosition());
                    if(isMainBase) {
                    	System.out.println("found real main: " + unitInfo.getLastPosition());
                    	foundEnemyMainBase = true;
                    }
                	
                    EnemyCommandInfo enemyCommandInfo;
                    if(isMainBase) {
                    	enemyCommandInfo = removefake(unitInfo);
                    }else {
                    	enemyCommandInfo = new EnemyCommandInfo(unitInfo, isMainBase);
                    }
                    enemyResourceDepotInfoMap.put(unitInfo, enemyCommandInfo);
                    resourceDepotPosition.add(unitInfo.getLastPosition());
                }else{
                	
                	System.out.println("too less mineral : " + unitInfo.getLastPosition() + ", " + getMineralPatchesNearDepot(unitInfo.getLastPosition()).size());
                    skipResourceDepot.add(unitInfo);
                }
            }else {
            	//For moving CommandCenter
            	if(InformationManager.Instance().enemyRace == Race.Terran && Prebot.Broodwar.getFrameCount()>30000 && !resourceDepotPosition.contains(unitInfo.getLastPosition())){
            		if(!unitInfo.getUnit().isFlying() && getMineralPatchesNearDepot(unitInfo.getLastPosition()).size() > 6 ){

            			EnemyCommandInfo enemyCommandInfo = new EnemyCommandInfo(unitInfo, false);
            			enemyResourceDepotInfoMap.remove(unitInfo);
                        enemyResourceDepotInfoMap.put(unitInfo, enemyCommandInfo);
                        resourceDepotPosition.add(unitInfo.getLastPosition());
                    }
            	}
            }
        }
    }

    private EnemyCommandInfo removefake(UnitInfo unitInfo) {
    	
    	EnemyCommandInfo preserve=null;
    	for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

            EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();

           if(enemyCommandInfo.isMainBase) {
        	   System.out.println("removing fake!: " + enemyResourceDepot.getKey().getLastPosition());
        	   preserve = enemyResourceDepot.getValue();
        	   preserve.setUnitInfo(unitInfo);
        	   enemyResourceDepotInfoMap.remove(enemyResourceDepot.getKey());
           }
        }
    	if(preserve == null) {
    		System.out.println("should not happen in removing false");
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

            enemyCommandInfo.updateInfo();
        }
    }
}
