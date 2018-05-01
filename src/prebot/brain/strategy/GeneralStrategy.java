package prebot.brain.strategy;

import prebot.brain.buildaction.BuildActions;
import prebot.brain.buildaction.FactoryUnitSelector;
import prebot.brain.buildaction.TrainActions;
import prebot.brain.squad.DefaultSquads;

public class GeneralStrategy extends Strategy {
	
	protected void setUpDefaultGeneralStrategy() {
		buildActionList.add(new TrainActions.TrainSCV());
		buildActionList.add(new BuildActions.BuildSupplyDepot());

		squadList.add(new DefaultSquads.MainDefenseSquad());
		squadList.add(new DefaultSquads.WatcherSquad());
	}

	protected void setUpFactoryUnitTrainActionByRatio(int ratioVulture, int ratioTank, int ratioGoliath) {
		FactoryUnitSelector factoryUnitSelector = new FactoryUnitSelector(ratioVulture, ratioTank, ratioGoliath);
		buildActionList.add(new TrainActions.TrainVulture(factoryUnitSelector));
		buildActionList.add(new TrainActions.TrainTank(factoryUnitSelector));
		buildActionList.add(new TrainActions.TrainGoliath(factoryUnitSelector));
	}
}
