package prebot.macro;

import bwapi.Unit;
import prebot.common.main.Prebot;

public class EnemyMineral {


    private final Unit mineralUnit;
    private int realMineral;

    public int lastFullCheckFrame;
    public int lastFullCheckMineral;

    private int lastCheckFrame;

    public EnemyMineral(Unit mineralUnit) {
        this.mineralUnit = mineralUnit;
        this.realMineral = 0;
        this.lastFullCheckFrame = 0;
        this.lastFullCheckMineral = 0;
        this.lastCheckFrame = 0;
    }

    public Unit getMineralUnit() {
        return mineralUnit;
    }

    public int getRealMineral() {
        return realMineral;
    }

    public int getLastCheckFrame() {
        return lastCheckFrame;
    }

    public void setLastCheckFrame(int lastCheckFrame) {
        this.lastCheckFrame = lastCheckFrame;
    }

    public void updateResources() {
        if(mineralUnit.isVisible()){
            this.lastCheckFrame = Prebot.Broodwar.getFrameCount();
            this.realMineral = 1500 - mineralUnit.getResources();
        }
    }

    public void updateFullVisibleResources(int lastFullCheckFrame) {
        this.lastFullCheckFrame = lastFullCheckFrame;
        this.lastFullCheckMineral = 1500 - mineralUnit.getResources();
    }

    public int getFullCheckMineral() {
        return lastFullCheckMineral;
    }
}
