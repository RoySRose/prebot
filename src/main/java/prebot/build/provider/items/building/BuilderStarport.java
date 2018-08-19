package prebot.build.provider.items.building;

import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.BuildQueueProvider;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions;

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
				if(StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.ONE_STARPORT || StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.TWO_STARPORT) {
					setTilePosition(BlockingEntrance.Instance().starport1);
				}
			} else if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 1) {
				if(StrategyIdea.expansionOption == EnemyStrategyOptions.ExpansionOption.TWO_STARPORT) {
					setTilePosition(BlockingEntrance.Instance().starport2);
				}
			}
			return true;
		}

		int activatedCommandCount = UnitUtils.activatedCommandCenterCount();
		if (activatedCommandCount >= 2) {
			int wraithCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Wraith);
			if (StrategyIdea.wraithCount > wraithCount) {
				if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
//					setTilePosition(BlockingEntrance.Instance().starport1);
					return true;
				}
			}
			
			if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
				int vihicleWeaponUpgradeLevel = Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons);
				if (vihicleWeaponUpgradeLevel == 0) {
					int upgradeRemainingFrame = BuildQueueProvider.Instance().upgradeRemainingFrame(UpgradeType.Terran_Vehicle_Weapons);
					if (upgradeRemainingFrame != CommonCode.UNKNOWN && upgradeRemainingFrame < UpgradeType.Terran_Vehicle_Weapons.upgradeTime() * 2 / 3) {
	//					setTilePosition(BlockingEntrance.Instance().starport1);
						return true;
					}
				}
			}
		}
		
		// TODO 필요한 정보 추가
		if(Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Starport) == 0) {
			boolean needVessel = UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Arbiter, UnitType.Protoss_Arbiter_Tribunal);
//			setTilePosition(BlockingEntrance.Instance().starport1);
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
