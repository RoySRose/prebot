package prebot.brain.strategy;

import prebot.brain.buildaction.BuildActions;
import prebot.brain.buildaction.FactoryUnitSelector;
import prebot.brain.buildaction.TrainActions;
import prebot.brain.squad.Squads;

public class GeneralStrategy extends Strategy {
	
	public void setUpDefaultGeneralStrategy() {
		buildActionList.add(new TrainActions.TrainSCV());
		buildActionList.add(new BuildActions.BuildSupplyDepot());

		squadList.add(new Squads.IdleSquad());
		squadList.add(new Squads.MainDefenseSquad());
		squadList.add(new Squads.WatcherSquad());
	}

	public void setUpFactoryUnitTrainActionByRatio(int ratioVulture, int ratioTank, int ratioGoliath) {
		FactoryUnitSelector factoryUnitSelector = new FactoryUnitSelector(ratioVulture, ratioTank, ratioGoliath);
		buildActionList.add(new TrainActions.TrainVulture(factoryUnitSelector));
		buildActionList.add(new TrainActions.TrainTank(factoryUnitSelector));
		buildActionList.add(new TrainActions.TrainGoliath(factoryUnitSelector));
	}
}
