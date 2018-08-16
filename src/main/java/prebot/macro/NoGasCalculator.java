package prebot.macro;

import bwapi.Position;

public class NoGasCalculator implements GasCalculator{

    public NoGasCalculator() {
    }
    public int getGas() {
        return 0;
    }

    public int getRealGas() {
        return 0;
    }

    public boolean hasGasBuilding() {
        return false;
    }

    public void updateResources() {
    }
    
    public Position getGeyserPoint() {
        return Position.None;
    }

    public boolean isVisible() {
        return false;
    }
}
