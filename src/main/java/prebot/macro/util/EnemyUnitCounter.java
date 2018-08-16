package prebot.macro.util;

import bwapi.UnitType;

import java.util.HashMap;
import java.util.Map;

public class EnemyUnitCounter {

    private Map<UnitType, Integer> unitTypes;

    public int mineralSet;
    public int gasSet;

    public EnemyUnitCounter() {
        this.unitTypes = new HashMap<>();
        this.mineralSet = 0;
        this.gasSet = 0;
    }

    public Map<UnitType, Integer> getUnitTypes() {
        return unitTypes;
    }

    public int getMineralSet() {
        return mineralSet;
    }

    public int getGasSet() {
        return gasSet;
    }
    public void add(UnitType unitType, int i) {
        unitTypes.put(unitType, i);

        recalculateSet();
    }
    private void recalculateSet() {

        for (Map.Entry<UnitType, Integer> enemyUnit : unitTypes.entrySet()){

            int mineral = enemyUnit.getKey().mineralPrice();
            int gas = enemyUnit.getKey().gasPrice();

            if(enemyUnit.getKey() == UnitType.Zerg_Lurker) {
                mineral = 125;
                gas = 125;
            }

            this.mineralSet +=  mineral * enemyUnit.getValue();
            this.gasSet +=  gas * enemyUnit.getValue();
        }
    }
}
