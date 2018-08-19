package prebot.macro;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.common.main.Prebot;
import prebot.common.util.PositionUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.internal.UnitCache;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.ArrayList;
import java.util.List;

public class EnemyCommandInfo {

//    static final double MINERAL_INCREMENT_RATE_Line1 = 0.04663;
//    static final double MINERAL_INCREMENT_RATE_Line2 = 0.037514;

//	static final double MINERAL_INCREMENT_RATE_Line1 =0.04763;
//    static final double MINERAL_INCREMENT_RATE_Line2 = 0.036922;

	static final double MINERAL_INCREMENT_RATE_Line1 =0.04734;
//    static final double MINERAL_INCREMENT_RATE_Line2 = 0.03926;
    static final double MINERAL_INCREMENT_RATE_Line2 = 0.034924;
    
    UnitInfo unitInfo;
    public int lastCheckFrame;
    public int lastFullCheckFrame;
    public int fullWorkerFrame;
    public int halfWorkerFrame;
    
    public int lastFullCheckWorkerCount;
    boolean hasGas;

    public int uxmineral=0;
    public int uxgas=0;
    
    public WorkerCounter workerCounter;
    public MineralCalculator mineralCalculator;
    public GasCalculator gasCalculator;
    
    public boolean isMainBase;
    
    private int maxMineral;
    
	public EnemyCommandInfo(UnitInfo unitInfo, boolean isMainBase) {

        this.unitInfo = unitInfo;
        this.isMainBase = isMainBase;

        if(unitInfo.isCompleted()) {
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount();
        }else{
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount() + unitInfo.getRemainingBuildTime();
        }
        
        Unit geyser = getGeyser(unitInfo.getLastPosition());
        if(geyser == null){
            hasGas = false;
            gasCalculator = new NoGasCalculator();
        }else{
            hasGas = true;
            gasCalculator = new DefaultGasCalculator(geyser);
        }

        List<EnemyMineral> mineralList = getMineralPatchesNearDepot(unitInfo.getLastPosition());

        this.mineralCalculator = new MineralCalculator(mineralList);
        this.workerCounter = new WorkerCounter(unitInfo, mineralCalculator.getMineralCount());

        this.maxMineral = mineralCalculator.getMineralCount()*1500;
       
        if(isMainBase) {
        	this.lastFullCheckWorkerCount=4;
        }else {
        	this.lastFullCheckWorkerCount=0;
        }
        this.lastFullCheckFrame =0;
        this.fullWorkerFrame =0;
        this.halfWorkerFrame =0;
        
    }

    private Unit getGeyser(Position unitPosition) {
        Unit gasUnit =null;
        Unit findGeyser = ConstructionPlaceFinder.Instance().getGeyserNear(unitPosition.toTilePosition());
        if (findGeyser != null) {
            if (findGeyser.getInitialPosition().getDistance(unitPosition) <= 320) {
                gasUnit = findGeyser;
            }
        }
        return gasUnit;
    }

    public static List<EnemyMineral> getMineralPatchesNearDepot(Position depotPosition) {
        int radius = 280;
        ArrayList<EnemyMineral> mineralList = new ArrayList<>();
        
        for (Unit unit : Prebot.Broodwar.getStaticMinerals()) {
            if (unit.getInitialPosition().getDistance(depotPosition) < radius) {
                EnemyMineral newMineral = new EnemyMineral(unit);
                mineralList.add(newMineral);
            }
        }
        return mineralList;
    }

    public void setLastCheckFrame(int lastCheckFrame) {
        this.lastCheckFrame = lastCheckFrame;
    }
    
    public int getLastFullCheckFrame() {
        return lastFullCheckFrame;
    }
    

    public void updateInfo() {

        if(unitInfo.getUnit() != null && unitInfo.getUnit().isVisible() && mineralCalculator.allVisible()){

            lastFullCheckFrame = Prebot.Broodwar.getFrameCount();
            workerCounter.setLastFullCheckFrame(lastFullCheckFrame);
            
            workerCounter.updateCount(unitInfo, mineralCalculator, gasCalculator);
            
            lastFullCheckWorkerCount = workerCounter.getRealWorkerCount();

            mineralCalculator.updateFullVisibleResources(lastFullCheckFrame);
        }
        
        if (hasGas) {
            gasCalculator.updateResources();
        }
        workerCounter.updateCount(unitInfo, mineralCalculator, gasCalculator);

        mineralCalculator.updateResources();
       
    }

    public void setUnitInfo(UnitInfo unitInfo) {
		this.unitInfo = unitInfo;
	}
    
    public int getMineral(){

    	uxmineral=0;
    	
    	if(unitInfo.getRemainingBuildTime() > 0) {
    		return 0;
    	};
    	
        int total;
        double workerCount;
        int term1;
        int term2 = 0;
        int term3 =0;
        double appliedWorkerCount;
        double appliedWorkerCount2 = 0;
        int predictedIncrementMineral;

        
        workerCount = workerCounter.getWorkerCount(lastFullCheckFrame);
        if(workerCount < mineralCalculator.getMineralCount()*2) {
        	fullWorkerFrame =0;
        }
        if(workerCount < mineralCalculator.getMineralCount()) {
        	halfWorkerFrame =0;
        }
        
        if(fullWorkerFrame == 0 && workerCount == mineralCalculator.getMineralCount()*2  && Prebot.Broodwar.getFrameCount()>7000) {
        	fullWorkerFrame = Prebot.Broodwar.getFrameCount();
        	 //System.out.println("update fullWorkerFrame: " + fullWorkerFrame+", " + workerCount);
        }
        
        if(halfWorkerFrame == 0 && workerCount > mineralCalculator.getMineralCount() && Prebot.Broodwar.getFrameCount()>3800) {
        	halfWorkerFrame = Prebot.Broodwar.getFrameCount();
        	 //System.out.println("update halfWorkerFrame: " + halfWorkerFrame+", " + workerCount);
        }
        
        EnemyMineral enemyMineral = mineralCalculator.getMaxLastCheckFrame();
        
        total = mineralCalculator.getFullCheckMineral();

        int lastCheckedFrame=0;
        if(lastFullCheckFrame==0) {
        	lastCheckedFrame = lastCheckFrame;
        }else {
        	lastCheckedFrame = lastFullCheckFrame;
        }
        
        
        if(fullWorkerFrame > 0) {
        	if(halfWorkerFrame > lastCheckedFrame) {
            	term1 = halfWorkerFrame - lastCheckedFrame;
      			term2 =	fullWorkerFrame - halfWorkerFrame;
            	term3 = Prebot.Broodwar.getFrameCount() - fullWorkerFrame;
            	appliedWorkerCount = (lastFullCheckWorkerCount + workerCounter.halfWorker) / 2;
                appliedWorkerCount2 = (workerCounter.halfWorker + workerCounter.maxWorker) / 2 - workerCounter.halfWorker;
        	}else if (fullWorkerFrame > lastCheckedFrame){
        		term1 = 0;
            	term2 = fullWorkerFrame - lastCheckedFrame;
            	term3 =	Prebot.Broodwar.getFrameCount() - fullWorkerFrame;
            	appliedWorkerCount = 0;
                appliedWorkerCount2 = (lastFullCheckWorkerCount + workerCounter.maxWorker) / 2 - workerCounter.halfWorker;
        	}else {
        		term1 = 0;
        		term2 = 0;
            	term3 = Prebot.Broodwar.getFrameCount() - lastCheckedFrame;
            	appliedWorkerCount = 0;
                appliedWorkerCount2 = 0;
        	}
        	
            int firstSetIncremental = 0;
            int secondSetIncremental = 0;
            
            firstSetIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
            firstSetIncremental += (int) (workerCounter.halfWorker * (term2+term3) * MINERAL_INCREMENT_RATE_Line1);
            
            secondSetIncremental += (int) (appliedWorkerCount2 * term2 * MINERAL_INCREMENT_RATE_Line2);
            secondSetIncremental += (int) (workerCounter.halfWorker * term3 * MINERAL_INCREMENT_RATE_Line2);
            
//                if(appliedWorkerCount <= mineralCalculator.getMineralCount()) {
//                	firstTermIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
//                }else {
//                	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term1 * MINERAL_INCREMENT_RATE_Line1);
//                	firstTermIncremental += (int) ((appliedWorkerCount-mineralCalculator.getMineralCount()) * term1 * MINERAL_INCREMENT_RATE_Line2);
//                }
//                
//                secondTermIncremental += (int) (mineralCalculator.getMineralCount() * term2 * MINERAL_INCREMENT_RATE_Line1);
//            	secondTermIncremental += (int) ((appliedWorkerCount2-mineralCalculator.getMineralCount())  * term2 * MINERAL_INCREMENT_RATE_Line2);
            
            predictedIncrementMineral = firstSetIncremental + secondSetIncremental;
            
        }else if(halfWorkerFrame > 0) {
        	
        	if(halfWorkerFrame > lastCheckedFrame) {
            	term1 = halfWorkerFrame - lastCheckedFrame;
      			term2 =	Prebot.Broodwar.getFrameCount() - halfWorkerFrame;
            	appliedWorkerCount = (lastFullCheckWorkerCount + workerCounter.halfWorker) / 2;
                appliedWorkerCount2 = (workerCounter.halfWorker + workerCount) / 2 - workerCounter.halfWorker;
        	}else {
        		term1 = 0;
            	term2 = Prebot.Broodwar.getFrameCount() - lastCheckedFrame;
            	appliedWorkerCount = 0;
                appliedWorkerCount2 = (lastFullCheckWorkerCount + workerCount) / 2 - workerCounter.halfWorker;
        	}
        	
            int firstSetIncremental = 0;
            int secondSetIncremental = 0;
            
            firstSetIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
            firstSetIncremental += (int) (workerCounter.halfWorker * (term2) * MINERAL_INCREMENT_RATE_Line1);
            
            secondSetIncremental += (int) (appliedWorkerCount2 * term2 * MINERAL_INCREMENT_RATE_Line2);
            
            predictedIncrementMineral = firstSetIncremental + secondSetIncremental;
            
            
//	            term1 = Prebot.Broodwar.getFrameCount() - lastCheckedFrame;
//	            appliedWorkerCount = (lastFullCheckWorkerCount + (workerCount)) / 2;
//	   
//	            int firstTermIncremental = 0;
//	            if(appliedWorkerCount <= mineralCalculator.getMineralCount()) {
//                	firstTermIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
//                }else {
//                	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term1 * MINERAL_INCREMENT_RATE_Line1);
//                	firstTermIncremental += (int) ((appliedWorkerCount-mineralCalculator.getMineralCount()) * term1 * MINERAL_INCREMENT_RATE_Line2);
//                }
//	            
//	            predictedIncrementMineral = firstTermIncremental;
        }else {
        	//(if(enemyMineral.getLastCheckFrame() == 0)
        	term1 = Prebot.Broodwar.getFrameCount() - lastCheckedFrame;
        	appliedWorkerCount = (lastFullCheckWorkerCount + workerCount) / 2;

        	int firstSetIncremental = 0;
            
            firstSetIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
            
            predictedIncrementMineral = firstSetIncremental;
        }
        //|| (enemyMineral.getLastCheckFrame() == 0)
            //TODO 세분화 필요 + 미네랄 일부만 보였을때 최근 2건이 있다면 그거로 확인하는 로직이 필요할까? 좋을거 같기도..
            //workerCount = workerCount;

            
            
//            total = enemyMineral.getRealMineral() * mineralCalculator.getMineralCount();
//             
//            term1 = Prebot.Broodwar.getFrameCount() - enemyMineral.getLastCheckFrame();
//
//            int firstTermIncremental = 0;
//            if(workerCount <= mineralCalculator.getMineralCount()) {
//            	firstTermIncremental += (int) (workerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
//            }else {
//            	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term1 * MINERAL_INCREMENT_RATE_Line1);
//            	firstTermIncremental += (int) ((workerCount-mineralCalculator.getMineralCount()) * term1 * MINERAL_INCREMENT_RATE_Line2);
//            }
//            
//            predictedIncrementMineral = firstTermIncremental;

    	return uxmineral = (total + predictedIncrementMineral) > maxMineral ? maxMineral : (total + predictedIncrementMineral);
    }

    public int getGas(){
    	uxgas =0 ;
        return uxgas = gasCalculator.getGas();
    }
}
