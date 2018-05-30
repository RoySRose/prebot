package prebot.common.util;

import bwapi.TechType;
import bwapi.UpgradeType;
import prebot.common.main.Prebot;

public class UpgradeUtils {

	public static boolean isUpgraded(UpgradeType upgradeType) {
		return Prebot.Broodwar.self().getUpgradeLevel(upgradeType) > 0;
	}
	
	public static boolean hasResearched(TechType techType) {
		return Prebot.Broodwar.self().hasResearched(techType);
	}

	public static boolean isEnemyUpgraded(UpgradeType upgradeType) {
		return Prebot.Broodwar.enemy().getUpgradeLevel(upgradeType) > 0;
	}

	public static boolean hasEnemyResearched(TechType techType) {
		return Prebot.Broodwar.enemy().hasResearched(techType);
	}

}
