package prebot.macro;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.main.Prebot;
import prebot.common.util.internal.UnitCache;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackDecisionMaker {

    Decision decision;

    private static AttackDecisionMaker instance = new AttackDecisionMaker();
    public static AttackDecisionMaker Instance() {
        return instance;
    }

    public Map<UnitInfo, EnemyCommandInfo> enemyCommandInfoMap;

    public void onStart() {
        enemyCommandInfoMap = new HashMap<>();
    }

    public void update() {
        decision = Decision.DEFENCE;

        addNewResourceDepot(InformationManager.Instance().enemyRace);
        updateResources(InformationManager.Instance().enemyRace);
    }

    private void addNewResourceDepot(Race race) {

        List<UnitInfo> enemyResourceDepot = UnitCache.getCurrentCache().enemyAllUnitInfos(InformationManager.Instance().getBasicResourceDepotBuildingType(race));

        for(UnitInfo unitInfo : enemyResourceDepot){

            if(enemyCommandInfoMap.get(unitInfo) == null){

                EnemyCommandInfo enemyCommandInfo = new EnemyCommandInfo(unitInfo);
                enemyCommandInfoMap.put(unitInfo, enemyCommandInfo);
            }
        }
    }

    private void updateResources(Race race) {
        List<UnitInfo> enemyResourceDepot = UnitCache.getCurrentCache().enemyAllUnitInfos(InformationManager.Instance().getBasicResourceDepotBuildingType(race));

        for(UnitInfo unitInfo : enemyResourceDepot){

            EnemyCommandInfo enemyCommandInfo = enemyCommandInfoMap.get(unitInfo);

            if(unitInfo.getUnit().isVisible()) {
                enemyCommandInfo.setLastCheckFrame(Prebot.Broodwar.getFrameCount());
            }

            enemyCommandInfo.updateInfo();
        }
    }

    private int getMineralsNearDepot(Unit depot)
    {
        if (depot == null) { return 0; }

        int mineralsNearDepot = 0;

        for (Unit unit : Prebot.Broodwar.getAllUnits())
        {
            if ((unit.getType() == UnitType.Resource_Mineral_Field) && unit.getDistance(depot) < 320)
            {
                mineralsNearDepot++;
            }
        }

        return mineralsNearDepot;
    }

    private int getMineralsSumNearDepot(Unit depot)
    {
        if (depot == null) { return 0; }

        int mineralsNearDepot = 0;

        for (Unit unit : Prebot.Broodwar.getMinerals())
        {
            if (unit.getDistance(depot) < 320)
            {
                mineralsNearDepot += unit.getResources();
            }
        }

        return mineralsNearDepot;
    }

    private Unit getGasNearDepot(BaseLocation base) {
        if (base == null) {
            return null;
        }

        for (Unit geyser : Prebot.Broodwar.getGeysers()) {
            if (geyser.getDistance(base) < 320) {
                return geyser;
            }
        }
        return null;
    }

}
