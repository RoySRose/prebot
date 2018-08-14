package prebot.macro;

public interface GasCalculator {

    boolean hasGasBuilding();
    boolean isVisible();
    int getGas();
    void updateResources();

    int getRealGas();
}
