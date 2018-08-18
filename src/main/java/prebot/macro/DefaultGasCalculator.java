package prebot.macro;


import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.constant.BuildConfig;
import prebot.common.main.Prebot;
import prebot.common.util.internal.UnitCache;
import prebot.strategy.UnitInfo;

import java.util.List;

public class DefaultGasCalculator implements GasCalculator{

    static final double GAS_INCREMENT_RATE = 0.204806;

    public final Unit geyser;
    public int realGas;
    
    public int gasDepletedFrame;

    private boolean hasGasBuilding;

    private int lastCheckFrame;


    public DefaultGasCalculator(Unit geyser) {
        this.geyser = geyser;
        this.hasGasBuilding = false;
        this.gasDepletedFrame = 0;
    }

    public Position getGeyserPoint() {
        return geyser.getInitialPosition();
    }
    
    public int getRealGas() {
        return realGas;
    }

    public int getGas() {
        if(!hasGasBuilding) {
            return 0;
        }else{
        	int predictedGas = (int) (realGas + (Prebot.Broodwar.getFrameCount() - lastCheckFrame) * GAS_INCREMENT_RATE);
        	if(realGas <= 5000 && predictedGas > 5000) {
        		gasDepletedFrame = Prebot.Broodwar.getFrameCount();
        	}
        	
        	if(gasDepletedFrame>0) {
    			predictedGas = (int) (5000 + (Prebot.Broodwar.getFrameCount() - gasDepletedFrame) * GAS_INCREMENT_RATE/4);
        	}
        	return predictedGas;
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
        
        List<UnitInfo> enemy = UnitCache.getCurrentCache().enemyAllUnitInfos(UnitType.AllUnits);

        for (UnitInfo unitInfo : enemy) {
        	if(unitInfo.getType().gasPrice() > 0) {
        		return true;
        	}
        }
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
