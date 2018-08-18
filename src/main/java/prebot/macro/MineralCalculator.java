package prebot.macro;


import prebot.common.main.Prebot;

import java.util.List;

public class MineralCalculator {

    public List<EnemyMineral> mineralsList;

    public MineralCalculator(List<EnemyMineral> mineralsList) {
        this.mineralsList = mineralsList;
    }

    public int getRealMineral() {

        int mineralSum = 0;
        for(EnemyMineral minerals : mineralsList) {
            mineralSum += minerals.getRealMineral();
        }
        return mineralSum;
    }

    public void updateResources() {
        for(EnemyMineral minerals : mineralsList) {
            minerals.updateResources();
        }
    }

    public boolean allVisible() {
        for(EnemyMineral minerals : mineralsList) {
            if(!minerals.getMineralUnit().isVisible()){
                return false;
            }
        }
        return true;
    }

    public int getVisibleCnt() {
        int cnt=0;
        for(EnemyMineral minerals : mineralsList) {
            if(!minerals.getMineralUnit().isVisible()){
                cnt++;
            }
        }
        return cnt;
    }

    public int getMineralCount(){
        return mineralsList.size();
    }

    public void updateFullVisibleResources(int lastFullCheckFrame) {
        for(EnemyMineral minerals : mineralsList) {
            minerals.updateFullVisibleResources(lastFullCheckFrame);
        }
    }

    public EnemyMineral getMaxLastCheckFrame() {
        EnemyMineral enemyMineral=null;
        int maxFrame= -1;
        for(EnemyMineral minerals : mineralsList) {
            if(maxFrame < minerals.getLastCheckFrame()){
                maxFrame = minerals.getLastCheckFrame();
                enemyMineral = minerals;
            }
        }
        return enemyMineral;
    }
    
    public void getPredictionWithNoFullVision() {

    }

    public int getFullCheckMineral() {
        int mineralSum = 0;
        for(EnemyMineral minerals : mineralsList) {
            mineralSum += minerals.getFullCheckMineral();
        }
        return mineralSum;
    }
}
