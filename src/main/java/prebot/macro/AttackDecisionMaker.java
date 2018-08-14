package prebot.macro;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.main.GameManager;
import prebot.common.main.Prebot;
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

    Decision decision;

    private static AttackDecisionMaker instance = new AttackDecisionMaker();
    public static AttackDecisionMaker Instance() {
        return instance;
    }

    public Map<UnitInfo, EnemyCommandInfo> enemyResourceDepotInfoMap;
    public List<UnitInfo> skipResourceDepot;

    public int enemyMineralToPredict;
    public int enemyGasToPredict;

    public Map<UnitType, MutableInt> predictedTotalEnemy;

    public void onStart() {
        this.enemyResourceDepotInfoMap = new HashMap<>();
        this.skipResourceDepot = new ArrayList<>();
        this.enemyMineralToPredict = 0;
        this.enemyGasToPredict = 0;
        this.predictedTotalEnemy = new HashMap<>();
    }

    public void update() {
        this.decision = Decision.DEFENCE;

        addNewResourceDepot(InformationManager.Instance().enemyRace);
        updateResources(InformationManager.Instance().enemyRace);

        //summary();

//        predictEnemyUnit();
//        int myForcePoint = calculateMyForce();
//        int enemyForcePoint = calculateEnemyForce();
//        decision = makeDecision(myForcePoint, enemyForcePoint);
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
        int enemyMineralToCalculateCombatUnit =0;
        int enemyGasToCalculateCombatUnit =0;
        for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

            EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();
            enemyMineralToCalculateCombatUnit += enemyCommandInfo.getMineral();
            enemyGasToCalculateCombatUnit += enemyCommandInfo.getGas();
        }

        List<UnitInfo> allEnemyUnits = UnitCache.getCurrentCache().enemyAllUnitInfos(UnitType.AllUnits);
        for(UnitInfo enemyUnitInfo: allEnemyUnits){
            UnitType enemyUnitType = enemyUnitInfo.getType();

            if(enemyUnitType == UnitType.Terran_Vulture_Spider_Mine){
                continue;
            }
            enemyMineralToCalculateCombatUnit -= enemyUnitType.mineralPrice();
            enemyGasToCalculateCombatUnit -= enemyUnitType.gasPrice();

            if(!enemyUnitType.isBuilding() && !enemyUnitType.isWorker() && enemyUnitType != UnitType.Zerg_Overlord) {

                MutableInt count = predictedTotalEnemy.get(enemyUnitType);
                if (count == null) {
                    predictedTotalEnemy.put(enemyUnitType, new MutableInt());
                }else {
                    count.increment();
                }
            }
        }


        for(UnitType enemyUnitType : UnitTypeList.getAllType()){

            int killedUnitCount = Prebot.Broodwar.self().killedUnitCount(enemyUnitType);

            MutableInt count = predictedTotalEnemy.get(enemyUnitType);
            if (count == null) {
                predictedTotalEnemy.put(enemyUnitType, new MutableInt(killedUnitCount));
            }else {
                count.increment(killedUnitCount);
            }
        }

        enemyMineralToPredict = enemyMineralToCalculateCombatUnit;
        enemyGasToPredict = enemyGasToCalculateCombatUnit;
    }

    private void addNewResourceDepot(Race race) {

        List<UnitInfo> enemyResourceDepot = UnitCache.getCurrentCache().enemyAllUnitInfos(InformationManager.Instance().getBasicResourceDepotBuildingType(race));

        for(UnitInfo unitInfo : enemyResourceDepot){

            if(enemyResourceDepotInfoMap.get(unitInfo) == null){
                if(skipResourceDepot.contains(unitInfo)){
                    continue;
                }

                if(getMineralPatchesNearDepot(unitInfo.getLastPosition()).size() > 6 ){
                    EnemyCommandInfo enemyCommandInfo = new EnemyCommandInfo(unitInfo);
                    enemyResourceDepotInfoMap.put(unitInfo, enemyCommandInfo);
                }else{
                    skipResourceDepot.add(unitInfo);
                }
            }
        }
    }

    private void updateResources(Race race) {

        for (Map.Entry<UnitInfo, EnemyCommandInfo> enemyResourceDepot : enemyResourceDepotInfoMap.entrySet()){

            //UnitInfo unitInfo = enemyResourceDepot.getKey();
            EnemyCommandInfo enemyCommandInfo = enemyResourceDepot.getValue();

//            if(unitInfo.getUnit().isVisible()) {
//                enemyCommandInfo.setLastCheckFrame(Prebot.Broodwar.getFrameCount());
//            }

            enemyCommandInfo.updateInfo();
        }
    }
}
