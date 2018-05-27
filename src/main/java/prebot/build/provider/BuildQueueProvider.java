package prebot.build.provider;

import java.util.ArrayList;
import java.util.List;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.items.BuilderBarrack;
import prebot.build.provider.items.BuilderCharonBoosters;
import prebot.build.provider.items.BuilderIonThrusters;
import prebot.build.provider.items.BuilderSiegeTank;
import prebot.build.provider.items.BuilderSpiderMines;
import prebot.build.provider.items.BuilderSupply;
import prebot.build.provider.items.BuilderTankSiegeMode;
import prebot.common.MetaType;

public final class BuildQueueProvider {

    private static BuildQueueProvider instance = new BuildQueueProvider();
    /// static singleton 객체를 리턴합니다
    public static BuildQueueProvider Instance() {
        return instance;
    }

    List<BuildableItem> buildableList = new ArrayList<>();
    BuildableItem tankSiegeMode;
    BuildableItem spiderMines;
    BuildableItem charonBoosters;
    BuildableItem ionThrusters;
    ResearchSelector researchSelector;
    FactoryUnitSelector factoryUnitSelector;

    public BuildQueueProvider(){

        researchSelector = new ResearchSelector();
        factoryUnitSelector = new FactoryUnitSelector();

        //Unit
        buildableList.add(new BuilderBarrack(new MetaType(UnitType.Terran_Barracks)));
        buildableList.add(new BuilderSupply(new MetaType(UnitType.Terran_Supply_Depot)));
        buildableList.add(new BuilderSiegeTank(new MetaType(UnitType.Terran_Siege_Tank_Tank_Mode), factoryUnitSelector));

        //Research
        tankSiegeMode = new BuilderTankSiegeMode(new MetaType(TechType.Tank_Siege_Mode), researchSelector);
        spiderMines = new BuilderSpiderMines(new MetaType(TechType.Spider_Mines), researchSelector);
        charonBoosters = new BuilderCharonBoosters(new MetaType(UpgradeType.Charon_Boosters), researchSelector);
        ionThrusters = new BuilderIonThrusters(new MetaType(UpgradeType.Ion_Thrusters), researchSelector);

        //Upgrade


        //Activate
        buildableList.add(tankSiegeMode);
        buildableList.add(spiderMines);
        buildableList.add(charonBoosters);
        buildableList.add(ionThrusters);

    }

    public void turnOffReseach(){

        //Deactivate
        buildableList.remove(tankSiegeMode);
        buildableList.remove(spiderMines);
        buildableList.remove(charonBoosters);
        buildableList.remove(ionThrusters);
    }

    public void process(){

        researchSelector.select();
        factoryUnitSelector.select();

        for(BuildableItem buildableItem: buildableList) {
            buildableItem.process();
        }
    }
}
