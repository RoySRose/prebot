


import java.util.List;

import bwapi.Race;
import bwapi.Unit;

public class WorkerCounter {

    public int realWorkerCount;
    public int lastCheckFrame;
    public int maxWorker;
    public int halfWorker;
    public int lastFullCheckFrame;
    public int fullCheckStartFrame;
    private UnitInfo depotInfo;
    //GAS_INCREMENT_RATE
    
    private double workerTrainRate;

    public WorkerCounter(UnitInfo depotInfo, int maxMineral) {
    	if (InformationManager.Instance().enemyRace == Race.Terran) {
    		this.workerTrainRate = 0.00333333;
        } else if (InformationManager.Instance().enemyRace == Race.Protoss) {
        	this.workerTrainRate = 0.00333333;
        } else {
        	this.workerTrainRate = 0.004;
        }
    	this.lastCheckFrame = 0;
    	this.lastFullCheckFrame = 0;
    	this.maxWorker = maxMineral*2;
    	this.halfWorker = maxMineral;
    	this.fullCheckStartFrame =0;
    	this.realWorkerCount = 4;
    	this.depotInfo = depotInfo;
    }

    public void setWorkerCount(int realWorkerCount) {
        //recalculateWorkerTrainRate(realWorkerCount);

        if(realWorkerCount > maxWorker){
            realWorkerCount = maxWorker;
        }
        
        if(realWorkerCount < 7) {
        	return;
        }
        
        this.realWorkerCount = realWorkerCount;
    }

    public int getRealWorkerCount() {
        
        return realWorkerCount;
    }

    
    public void setLastFullCheckFrame(int lastFullCheckFrame) {
    	this.lastFullCheckFrame = lastFullCheckFrame;
    }
    
    public double getWorkerCount(int lastFullCheckFrame) {
    	if(!depotInfo.isCompleted()) {
    		return 0;
    	}
    	double result=0;
    	
    	if(lastFullCheckFrame>0) {
	        result=(realWorkerCount + (MyBotModule.Broodwar.getFrameCount() - lastFullCheckFrame) * workerTrainRate);
    	}else if(lastCheckFrame>0){
    		result=(realWorkerCount + (MyBotModule.Broodwar.getFrameCount() - lastCheckFrame) * workerTrainRate);
    	}else {
    		result=((MyBotModule.Broodwar.getFrameCount() - depotInfo.completFrame()) * workerTrainRate);
    	}
    	
    	if(lastCheckFrame == 0) {
    		result *= 0.9;
    	}
        
        return result > maxWorker ? maxWorker : result ;
    }

    public void updateCount(UnitInfo unitInfo, MineralCalculator mineralCalculator, GasCalculator gasCalculator) {
        Unit enemyResourceDepot= unitInfo.getUnit();
        if(enemyResourceDepot == null) {
        	return;
        }
        //System.out.println("visible: " + enemyResourceDepot.isVisible() + ", " + mineralCalculator.allVisible());
        
        if(enemyResourceDepot.isVisible() && mineralCalculator.allVisible()){
        	if(MyBotModule.Broodwar.getFrameCount() - fullCheckStartFrame > 300) {
        		fullCheckStartFrame = MyBotModule.Broodwar.getFrameCount();
        	
	            List<Unit> workerList =  UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, enemyResourceDepot.getPosition(), 320, InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace));
	            int workerCnt = workerList.size();
	            
	            int buildWorkerCnt =0;
	            int gasWorkerCnt =0;
	            for(Unit worker : workerList) {
	        		if(worker.isConstructing()) {
	        			buildWorkerCnt++;
	        		}
	        		
	        		if(gasCalculator.hasGasBuilding()){
		        		if(worker.isGatheringGas()) {
		        			gasWorkerCnt++;
		        		}
	        		}
	        	}

	        	if(gasWorkerCnt > 3) {
	        		gasWorkerCnt=3;
	        	}
	        	//System.out.println("workerCnt: " + workerCnt + ", buildWorkerCnt: " + buildWorkerCnt + ", gasWorkerCnt: " + gasWorkerCnt); 
	        	workerCnt -= buildWorkerCnt;
	            workerCnt -= gasWorkerCnt;
	            
	            this.lastFullCheckFrame = MyBotModule.Broodwar.getFrameCount();
	            //System.out.println("setting realworker1: " + workerCnt);
	            setWorkerCount(workerCnt);
        	}
        }else if(enemyResourceDepot.isVisible()){
        	
        	if(MyBotModule.Broodwar.getFrameCount() - lastFullCheckFrame < 1000) {
        		return;
        	}

            List<Unit> workerList =  UnitUtils.getUnitsInRadius(CommonCode.PlayerRange.ENEMY, enemyResourceDepot.getPosition(), 320, InformationManager.Instance().getWorkerType());
            int workerCnt = workerList.size();

            int buildWorkerCnt =0;
            int gasWorkerCnt =0;
            for(Unit worker : workerList) {
        		if(worker.isConstructing()) {
        			buildWorkerCnt++;
        		}
        		
        		if(gasCalculator.hasGasBuilding()){
	//            		if(gasWorker.isGatheringGas() || gasWorker.isCarryingGas()) {
	//        			gasWorkerCnt++;
	//        		}else if(gasWorker.getDistance(PositionUtils.center(gasCalculator.getGeyserPoint(), enemyResourceDepot.getPoint())) < 60 ){
	//        			if(!gasWorker.isCarryingMinerals())
	//        			gasWorkerCnt++;
	//        		}
	        		if(worker.isGatheringGas()) {
	        			gasWorkerCnt++;
	        		}
        		}
        	}

        	if(gasWorkerCnt > 3) {
        		gasWorkerCnt=3;
        	}
        	//System.out.println("2 workerCnt: " + workerCnt + ", buildWorkerCnt: " + buildWorkerCnt + ", gasWorkerCnt: " + gasWorkerCnt); 
        	workerCnt -= buildWorkerCnt;
            workerCnt -= gasWorkerCnt;

            double visibleMineral = (double)mineralCalculator.getVisibleCnt();

            if(visibleMineral > 2) {
                workerCnt = (int) ((double) workerCnt + (double) mineralCalculator.getMineralCount()/visibleMineral);

                this.lastCheckFrame = MyBotModule.Broodwar.getFrameCount();
                //System.out.println("setting realworker2: " + workerCnt);
                setWorkerCount(workerCnt);
            }
        }
    }
}
