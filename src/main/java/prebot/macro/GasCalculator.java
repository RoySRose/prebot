package prebot.macro;

import bwapi.Position;

public interface GasCalculator {

    boolean hasGasBuilding();
    boolean isVisible();
    int getGas();
    void updateResources();
    Position getGeyserPoint();
    
    int getRealGas();
    
}
