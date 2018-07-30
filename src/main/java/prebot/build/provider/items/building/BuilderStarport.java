package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class BuilderStarport extends DefaultBuildableItem {

	public BuilderStarport(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport);
		if (buildQueueCount > 0) {
			return false;
		}
		int constructionQueueItemCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null);
		if (constructionQueueItemCount > 0) {
			return false;
		}

		if (needStarportToTrainWraith()) {
			if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
				setTilePosition(BlockingEntrance.Instance().starport1);
			} else if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 1) {
				setTilePosition(BlockingEntrance.Instance().starport2);
			}
			return true;
		}

		int activatedCommandCount = UnitUtils.activatedCommandCenterCount();
		int wraithCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith);
		if (StrategyIdea.wraithCount > wraithCount && activatedCommandCount >= 2) {
			if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
				setTilePosition(BlockingEntrance.Instance().starport1);
				return true;
			}
		}
		
		// TODO 필요한 정보 추가
		if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
			boolean needVessel = UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Arbiter, UnitType.Protoss_Arbiter_Tribunal);
			setTilePosition(BlockingEntrance.Instance().starport1);
			return (needVessel && activatedCommandCount >= 2) || activatedCommandCount >= 3;
		}
		return false;
	}

	private boolean needStarportToTrainWraith() {
		int wraithToTrainCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Wraith) - StrategyIdea.wraithCount;
		if (wraithToTrainCount >= 5 && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) <= 1) {
			return true;
		}
		if (wraithToTrainCount > 0 && Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
			return true;
		}
		return false;
	}
}
