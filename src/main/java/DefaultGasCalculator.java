


import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.List;

public class DefaultGasCalculator implements GasCalculator{

    static final double GAS_INCREMENT_RATE = 0.204806;

    public final Unit geyser;
    public int realGas;
    
    public int gasDepletedFrame;

    private boolean hasGasBuilding;

    private int lastCheckFrame;

    private boolean isMainBase;

    public DefaultGasCalculator(Unit geyser, boolean isMainBase) {
        this.geyser = geyser;
        this.hasGasBuilding = false;
        this.gasDepletedFrame = 0;
        this.isMainBase = isMainBase;
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
        	int predictedGas = (int) (realGas + (MyBotModule.Broodwar.getFrameCount() - lastCheckFrame) * GAS_INCREMENT_RATE);
        	if(realGas <= 5000 && predictedGas > 5000) {
        		gasDepletedFrame = MyBotModule.Broodwar.getFrameCount();
        	}
        	
        	if(gasDepletedFrame>0) {
    			predictedGas = (int) (5000 + (MyBotModule.Broodwar.getFrameCount() - gasDepletedFrame) * GAS_INCREMENT_RATE/4);
        	}
        	return predictedGas;
        }
    }

    public boolean hasGasBuilding() {
        return hasGasBuilding;
    }

    private boolean getGasBuilding()
    {
        List<Unit> alreadyBuiltUnits = MyBotModule.Broodwar.getUnitsInRadius(geyser.getPosition(), 4 * BuildConfig.TILE_SIZE);
        for (Unit u : alreadyBuiltUnits) {
            if (u.getType().isRefinery() && u.exists()) {
                return true;
            }
        }
        
        if(isMainBase) {
	        List<UnitInfo> enemy = UnitCache.getCurrentCache().enemyAllUnitInfos(UnitType.AllUnits);
	
	        for (UnitInfo unitInfo : enemy) {
	        	if(unitInfo.getType().gasPrice() > 0) {
	        		return true;
	        	}
	        }
        }
        return false;
    }


    public void updateResources() {

        if(!hasGasBuilding){
            hasGasBuilding = getGasBuilding();
        }

        if(geyser.isVisible()){
            this.lastCheckFrame = MyBotModule.Broodwar.getFrameCount();
            this.realGas = 5000 - geyser.getResources();

            if(!hasGasBuilding && geyser.getResources() < 5000){
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
