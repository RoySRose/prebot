package prebot.macro;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.common.main.Prebot;
import prebot.strategy.UnitInfo;

import java.util.ArrayList;
import java.util.List;

public class EnemyCommandInfo {

    static final double MINERAL_INCREMENT_RATE = 0.04663;

    UnitInfo unitInfo;
    public int lastCheckFrame;
    public int lastFullCheckFrame;
    public int lastFullCheckWorkerCount;
    boolean hasGas;

    public WorkerCounter workerCounter;
    public MineralCalculator mineralCalculator;
    public GasCalculator gasCalculator;

    public EnemyCommandInfo(UnitInfo unitInfo) {

        this.unitInfo = unitInfo;
        Unit unit = unitInfo.getUnit();

        if(unitInfo.isCompleted()) {
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount();
        }else{
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount() + unitInfo.getRemainingBuildTime();
        }

        Unit geyser = getGeyser(unit);
        if(geyser == null){
            hasGas = false;
        }else{
            hasGas = true;
            gasCalculator = new GasCalculator(geyser);
        }

        List<EnemyMineral> mineralList = getMineralPatchesNearDepot(unit);

        mineralCalculator = new MineralCalculator(mineralList);
        workerCounter = new WorkerCounter();

        lastFullCheckWorkerCount=0;
        lastFullCheckFrame =0;
    }

    private Unit getGeyser(Unit unit) {
        Unit gasUnit =null;
        Unit findGeyser = ConstructionPlaceFinder.Instance().getRefineryNear(unit.getTilePosition());
        if (findGeyser != null) {
            if (findGeyser.getTilePosition().getDistance(unit.getTilePosition()) * 32 <= 320) {
                gasUnit = findGeyser;
            }
        }
        return gasUnit;
    }

    private List<EnemyMineral> getMineralPatchesNearDepot(Unit depot) {
        int radius = 320;
        ArrayList<EnemyMineral> mineralList = new ArrayList<>();
        for (Unit unit : Prebot.Broodwar.getStaticMinerals()) {
            if (unit.getType() == UnitType.Resource_Mineral_Field && unit.getDistance(depot) < radius) {
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
            lastFullCheckWorkerCount = workerCounter.getWorkerCount();
            mineralCalculator.updateFullVisibleResources(lastFullCheckFrame);
        }
        
        if (hasGas) {
            gasCalculator.updateResources();
        }
        workerCounter.updateCount(unitInfo, mineralCalculator, gasCalculator);

        mineralCalculator.updateResources();
       
    }

    public int getMineral(){

        int total;
        int workerCount;
        int term;
        int appliedWorkerCount;
        int predictedIncrementMineral;
        int predictionMinusMineral =0;

        if(lastFullCheckWorkerCount > 0) {
            total = mineralCalculator.getRealMineral();

            workerCount = workerCounter.getWorkerCount();
            term = Prebot.Broodwar.getFrameCount() - lastFullCheckFrame;
            appliedWorkerCount = (lastFullCheckWorkerCount * (workerCount)) / 2;

            predictedIncrementMineral = (int) (appliedWorkerCount * term * MINERAL_INCREMENT_RATE);
            predictionMinusMineral = mineralCalculator.getPredictionMinusMineral();
        }else{

            workerCount = workerCounter.getWorkerCount() * 2 /3;

            EnemyMineral enemyMineral = mineralCalculator.getMaxLastCheckFrame();
            total = enemyMineral.getRealMineral() * mineralCalculator.getMineralCount();

            term = Prebot.Broodwar.getFrameCount() - enemyMineral.getLastCheckFrame();

            predictedIncrementMineral = (int) (workerCount * term * MINERAL_INCREMENT_RATE);
        }

        return total + predictedIncrementMineral - predictionMinusMineral;
    }

    public int getGas(){
        return gasCalculator.getGas();
    }
}
