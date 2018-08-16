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

	static final double MINERAL_INCREMENT_RATE_Line1 =0.04763;
    static final double MINERAL_INCREMENT_RATE_Line2 = 0.036922;

    
    UnitInfo unitInfo;
    public int lastCheckFrame;
    public int lastFullCheckFrame;
    public int fullWorkerFrame;
    
    public double lastFullCheckWorkerCount;
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
       
        this.lastFullCheckWorkerCount=0;
        this.lastFullCheckFrame =0;
        this.fullWorkerFrame =0;
        
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
        int radius = 320;
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

    public void updateInfo() {

        if(unitInfo.getUnit() != null && unitInfo.getUnit().isVisible() && mineralCalculator.allVisible()){

            lastFullCheckFrame = Prebot.Broodwar.getFrameCount();
            workerCounter.setLastFullCheckFrame(lastFullCheckFrame);
            
            workerCounter.updateCount(unitInfo, mineralCalculator, gasCalculator);
            
            lastFullCheckWorkerCount = workerCounter.getWorkerCount();

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
        double appliedWorkerCount;
        double appliedWorkerCount2 = 0;
        int predictedIncrementMineral;

        workerCount = workerCounter.getWorkerCount();
        if(fullWorkerFrame == 0 && workerCount == workerCounter.maxWorker) {
        	fullWorkerFrame = Prebot.Broodwar.getFrameCount();
        	 System.out.println("update fullWorkerFrame: " + fullWorkerFrame+", " + workerCount);
        }
        
        //System.out.println("fullWorkerFrame: " + fullWorkerFrame);
        if(lastFullCheckFrame > 0) {
            total = mineralCalculator.getFullCheckMineral();

            if(fullWorkerFrame > 0) {
            	if(fullWorkerFrame > lastFullCheckFrame) {
	            	term1 = fullWorkerFrame - lastFullCheckFrame;
	            	term2 = Prebot.Broodwar.getFrameCount() - fullWorkerFrame;
            	}else {
            		term1 = 0;
	            	term2 = Prebot.Broodwar.getFrameCount() - lastFullCheckFrame;
            	}
            	
                appliedWorkerCount = (lastFullCheckWorkerCount + (workerCount)) / 2;
                appliedWorkerCount2 = workerCount;
       
                int firstTermIncremental = 0;
                int secondTermIncremental = 0;
                if(appliedWorkerCount <= mineralCalculator.getMineralCount()) {
                	firstTermIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
                }else {
                	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term1 * MINERAL_INCREMENT_RATE_Line1);
                	firstTermIncremental += (int) ((appliedWorkerCount-mineralCalculator.getMineralCount()) * term1 * MINERAL_INCREMENT_RATE_Line2);
                }
                
                secondTermIncremental += (int) (mineralCalculator.getMineralCount() * term2 * MINERAL_INCREMENT_RATE_Line1);
            	secondTermIncremental += (int) ((appliedWorkerCount2-mineralCalculator.getMineralCount())  * term2 * MINERAL_INCREMENT_RATE_Line2);
                
                predictedIncrementMineral = firstTermIncremental + secondTermIncremental;
            }else {
	            term1 = Prebot.Broodwar.getFrameCount() - lastFullCheckFrame;
	            appliedWorkerCount = (lastFullCheckWorkerCount + (workerCount)) / 2;
	   
	            int firstTermIncremental = 0;
	            if(appliedWorkerCount <= mineralCalculator.getMineralCount()) {
                	firstTermIncremental += (int) (appliedWorkerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
                }else {
                	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term1 * MINERAL_INCREMENT_RATE_Line1);
                	firstTermIncremental += (int) ((appliedWorkerCount-mineralCalculator.getMineralCount()) * term1 * MINERAL_INCREMENT_RATE_Line2);
                }
	            
	            predictedIncrementMineral = firstTermIncremental;
            }
//	        System.out.println("worker : " + appliedWorkerCount + ", term1: " + term1 + ", predictedIncrementMineral: " + predictedIncrementMineral + ", " + lastFullCheckWorkerCount);
//	        System.out.println("worker2: " + appliedWorkerCount2 + ", term1: " + term2 + ", predictedIncrementMineral: " + predictedIncrementMineral + ", " + lastFullCheckWorkerCount);
            //predictionMinusMineral = mineralCalculator.getPredictionMinusMineral();
        }else{

            //TODO 세분화 필요 + 미네랄 일부만 보였을때 최근 2건이 있다면 그거로 확인하는 로직이 필요할까? 좋을거 같기도..
            //workerCount = workerCount;

            EnemyMineral enemyMineral = mineralCalculator.getMaxLastCheckFrame();
            if(enemyMineral.getLastCheckFrame() == 0){//모든 미네랄을 한번도 못 봤고.
                uxmineral =0;
                int completedTime =0;
                //이게 상대 본진이고 내가 멀티가 없으면 내꺼로 추정, 그게 아니면 추정 불가
                if(isMainBase) {
                	completedTime = 0;
                }else {
                	completedTime = unitInfo.completFrame();
                }
                
            	int term = Prebot.Broodwar.getFrameCount() - completedTime;
            	int firstTermIncremental = 0;
                if(workerCount <= mineralCalculator.getMineralCount()) {
                	firstTermIncremental += (int) (workerCount * term * MINERAL_INCREMENT_RATE_Line1);
                }else {
                	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term * MINERAL_INCREMENT_RATE_Line1);
                	firstTermIncremental += (int) ((workerCount-mineralCalculator.getMineralCount()) * term * MINERAL_INCREMENT_RATE_Line2);
                }
                
            	return firstTermIncremental;
            }
            total = enemyMineral.getRealMineral() * mineralCalculator.getMineralCount();
             
            term1 = Prebot.Broodwar.getFrameCount() - enemyMineral.getLastCheckFrame();

            int firstTermIncremental = 0;
            if(workerCount <= mineralCalculator.getMineralCount()) {
            	firstTermIncremental += (int) (workerCount * term1 * MINERAL_INCREMENT_RATE_Line1);
            }else {
            	firstTermIncremental += (int) (mineralCalculator.getMineralCount() * term1 * MINERAL_INCREMENT_RATE_Line1);
            	firstTermIncremental += (int) ((workerCount-mineralCalculator.getMineralCount()) * term1 * MINERAL_INCREMENT_RATE_Line2);
            }
            
            predictedIncrementMineral = firstTermIncremental;
        }

        return uxmineral = total + predictedIncrementMineral > maxMineral ? maxMineral : total + predictedIncrementMineral;
    }

    public int getGas(){
    	uxgas =0 ;
        return uxgas = gasCalculator.getGas();
    }
}
