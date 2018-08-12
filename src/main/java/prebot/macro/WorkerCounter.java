package prebot.macro;


import bwapi.Unit;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.List;

public class WorkerCounter {

    public int realWorkerCount;
    public int lastCheckFrame;
    
    //GAS_INCREMENT_RATE
    
    private double workerTrainRate;

    public WorkerCounter() {
    	workerTrainRate = 0.00333333;
    	lastCheckFrame = 0;
    }

    public void setWorkerCount(int realWorkerCount, MineralCalculator mineralCalculator) {
        //recalculateWorkerTrainRate(realWorkerCount);

        if(realWorkerCount > mineralCalculator.getMineralCount() * 2){
            realWorkerCount = mineralCalculator.getMineralCount() * 2;
        }
        this.lastCheckFrame = Prebot.Broodwar.getFrameCount();
        this.realWorkerCount = realWorkerCount;
    }

    public int getWorkerCount() {
        return (int)(realWorkerCount + (Prebot.Broodwar.getFrameCount() - lastCheckFrame) * workerTrainRate);
    }

//    private final void recalculateWorkerTrainRate(int realWorkerCount) {
//        int oldWorkerCount = this.realWorkerCount;
//        int newWorkerCount= realWorkerCount;
//        int term = Prebot.Broodwar.getFrameCount() - lastCheckFrame;
//        workerTrainRate = (newWorkerCount - oldWorkerCount) / term;
//    }

    public void updateCount(UnitInfo unitInfo, MineralCalculator mineralCalculator, GasCalculator gasCalculator) {
        Unit enemyResourceDepot= unitInfo.getUnit();
        if(enemyResourceDepot.isVisible() && mineralCalculator.allVisible()){

            List<Unit> workerList =  UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, enemyResourceDepot.getPosition(), 320, InformationManager.Instance().getWorkerType());
            int workerCnt = workerList.size();
            if(gasCalculator.isVisible() && gasCalculator.hasGasBuilding()){
                workerCnt -= 3;
            }
            setWorkerCount(workerCnt, mineralCalculator);
        }else if(enemyResourceDepot.isVisible()){

            List<Unit> workerList =  UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, enemyResourceDepot.getPosition(), 320, InformationManager.Instance().getWorkerType());
            int workerCnt = workerList.size();

            if(gasCalculator.isVisible() && gasCalculator.hasGasBuilding()){
                workerCnt -= 3;
            }

            double visibleMineral = (double)mineralCalculator.getVisibleCnt();

            if(visibleMineral > 2) {
                workerCnt = (int) ((double) workerCnt * (double) mineralCalculator.getMineralCount()/visibleMineral);

                setWorkerCount(workerCnt, mineralCalculator);
            }
        }
    }
}
