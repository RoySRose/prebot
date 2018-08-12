package prebot.macro;


import bwapi.Unit;
import prebot.build.constant.BuildConfig;
import prebot.common.main.Prebot;

import java.util.List;

public class GasCalculator {

    static final int GAS_RATE = 1;

    private final Unit geyser;
    private int realGas;

    private boolean hasGasBuilding;

    private int lastCheckFrame;


    public GasCalculator(Unit geyser) {
        this.geyser = geyser;
        this.hasGasBuilding = false;
    }

    public int getGas() {
        if(hasGasBuilding) {
            return 0;
        }else{
            return realGas + (Prebot.Broodwar.getFrameCount() - lastCheckFrame) * GAS_RATE;
        }
    }

    public boolean hasGasBuilding() {
        return hasGasBuilding;
    }

    private boolean getGasBuilding()
    {
        List<Unit> alreadyBuiltUnits = Prebot.Broodwar.getUnitsInRadius(geyser.getPosition(), 4 * BuildConfig.TILE_SIZE);
        for (Unit u : alreadyBuiltUnits) {
            if (u.getType().isRefinery() && u.exists()) {
                return true;
            }
        }

//        List<UnitInfo> enemyGasBuilding = UnitCache.getCurrentCache().enemyAllUnitInfos(InformationManager.Instance().getRefineryBuildingType(InformationManager.Instance().enemyRace));
//
//        for (UnitInfo unitInfo : enemyGasBuilding) {
//            if(geyser.getDistance(unitInfo.getUnit()) < 320){
//                return true;
//            }
//        }
        return false;
    }


    public void updateResources() {

        if(!hasGasBuilding){
            hasGasBuilding = getGasBuilding();
        }

        if(geyser.isVisible()){
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount();
            this.realGas = 5000 - geyser.getResources();

            if(!hasGasBuilding && geyser.getResources() < 5000){
                System.out.println("something is wrong with gasCalculator calculation");
            }
        }
    }

    public boolean isVisible() {
        if(geyser.isVisible()){
            return true;
        }else{
            return false;
        }
    }
}
