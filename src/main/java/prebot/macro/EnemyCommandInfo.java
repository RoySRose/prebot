package prebot.macro;

import bwapi.Position;
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

        if(unitInfo.isCompleted()) {
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount();
        }else{
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount() + unitInfo.getRemainingBuildTime();
        }

        Unit geyser = getGeyser(unitInfo.getLastPosition());
        if(geyser == null){
            System.out.println("Initialize with NO GAS");
            hasGas = false;
            gasCalculator = new NoGasCalculator();
        }else{
            System.out.println("Initialize with YES GAS");
            hasGas = true;
            gasCalculator = new DefaultGasCalculator(geyser);
        }

        System.out.println("Initializing Mineral");
        List<EnemyMineral> mineralList = getMineralPatchesNearDepot(unitInfo.getLastPosition());

        this.mineralCalculator = new MineralCalculator(mineralList);
        this.workerCounter = new WorkerCounter();

        this.lastFullCheckWorkerCount=0;
        this.lastFullCheckFrame =0;
    }

    private Unit getGeyser(Position unitPosition) {
        Unit gasUnit =null;
        Unit findGeyser = ConstructionPlaceFinder.Instance().getRefineryNear(unitPosition.toTilePosition());
        if (findGeyser != null) {
            if (findGeyser.getTilePosition().getDistance(unitPosition.toTilePosition()) * 32 <= 320) {
                gasUnit = findGeyser;
            }
        }
        return gasUnit;
    }

    public static List<EnemyMineral> getMineralPatchesNearDepot(Position depotPosition) {
        int radius = 320;
        ArrayList<EnemyMineral> mineralList = new ArrayList<>();
        for (Unit unit : Prebot.Broodwar.getStaticMinerals()) {
            if (unit.getType() == UnitType.Resource_Mineral_Field && unit.getDistance(depotPosition) < radius) {
                EnemyMineral newMineral = new EnemyMineral(unit);
                mineralList.add(newMineral);
            }
        }
        System.out.println("MineralCnt near Depot: " + mineralList.size());
        return mineralList;
    }

    public void setLastCheckFrame(int lastCheckFrame) {
        this.lastCheckFrame = lastCheckFrame;
    }

    public void updateInfo() {

        if(unitInfo.getUnit() != null && unitInfo.getUnit().isVisible() && mineralCalculator.allVisible()){

            lastFullCheckFrame = Prebot.Broodwar.getFrameCount();

            System.out.println("Full Vision at: " + lastFullCheckFrame);

            lastFullCheckWorkerCount = workerCounter.getWorkerCount(mineralCalculator.getMineralCount());

            System.out.println("Full Vision at: " + lastFullCheckWorkerCount);

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
            total = mineralCalculator.getFullCheckMineral();

            workerCount = workerCounter.getWorkerCount(mineralCalculator.getMineralCount());
            term = Prebot.Broodwar.getFrameCount() - lastFullCheckFrame;
            appliedWorkerCount = (lastFullCheckWorkerCount * (workerCount)) / 2;

            predictedIncrementMineral = (int) (appliedWorkerCount * term * MINERAL_INCREMENT_RATE);
            //predictionMinusMineral = mineralCalculator.getPredictionMinusMineral();
        }else{

            //TODO 세분화 필요 + 미네랄 일부만 보였을때 최근 2건이 있다면 그거로 확인하는 로직이 필요할까? 좋을거 같기도..
            workerCount = workerCounter.getWorkerCount(mineralCalculator.getMineralCount()) * 2 /3;

            EnemyMineral enemyMineral = mineralCalculator.getMaxLastCheckFrame();
            if(enemyMineral.getLastCheckFrame() == 0){
                System.out.println("Can't predict no mineral info for this command");
                return 0;
            }
            total = enemyMineral.getRealMineral() * mineralCalculator.getMineralCount();

            term = Prebot.Broodwar.getFrameCount() - enemyMineral.getLastCheckFrame();

            predictedIncrementMineral = (int) (workerCount * term * MINERAL_INCREMENT_RATE);
        }

        return total + predictedIncrementMineral;
    }

    public int getGas(){
        return gasCalculator.getGas();
    }
}
