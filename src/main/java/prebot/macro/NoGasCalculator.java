package prebot.macro;

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

    public boolean isVisible() {
        return false;
    }
}
